package com.voyageguard.planning.application;

import com.voyageguard.planning.api.dto.DepartureResponse;
import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.planning.domain.departure.DepartureStatus;
import com.voyageguard.planning.domain.product.Product;
import com.voyageguard.planning.domain.product.ProductRepository;
import com.voyageguard.planning.domain.product.ProductStatus;
import com.voyageguard.planning.application.inventory.InventoryClient;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DepartureService {
    private final DepartureRepository departureRepository;
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long create(Long productId, LocalDate departureDate, Integer minParticipants, Integer capacity,
                        String itinerary, LocalDate saleStartDate, LocalDate saleEndDate, Integer salePrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));
        if (product.getStatus() == ProductStatus.ENDED || product.getStatus() == ProductStatus.DISCARDED) {
            throw new IllegalStateException("판매종료 또는 폐기된 상품에는 회차를 추가할 수 없습니다. 현재 상태: " + product.getStatus());
        }

        Departure departure = Departure.create(productId, departureDate, minParticipants, capacity,
                itinerary, saleStartDate, saleEndDate, salePrice);
        Long departureId = departureRepository.save(departure).getId(); // 1단계 - 여기서 즉시 커밋됨

        /// SAGA
        try {
            inventoryClient.create(departureId, capacity); // 2단계
        } catch (RuntimeException e) {
            compensateFailedCreate(departureId, e); // 보상 트랜잭션
            throw e;
        }

        return departureId;
    }

    // 보상 트랜잭션 - 2단계(재고 생성) 실패 시 1단계(회차 저장)를 되돌림
    private void compensateFailedCreate(Long departureId, RuntimeException cause) {
        log.warn("재고 초기화 실패로 회차 생성을 보상(삭제)함. departureId={}", departureId, cause);
        try {
            departureRepository.deleteById(departureId);
        } catch (RuntimeException compensationError) {
            log.error("회차 생성 보상 트랜잭션(삭제)까지 실패 - 수동 확인 필요. departureId={}", departureId, compensationError);
        }
    }

    @Transactional(readOnly = true)
    public List<DepartureResponse> listOpen() {
        return departureRepository.findByStatus(DepartureStatus.OPEN).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartureResponse get(Long id) {
        return toResponse(getDeparture(id));
    }

    private DepartureResponse toResponse(Departure departure) {
        Product product = productRepository.findById(departure.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + departure.getProductId()));
        int remainingCount = inventoryClient.getRemainingCount(departure.getId());

        return new DepartureResponse(
                departure.getId(),
                departure.getProductId(),
                product.getTitle(),
                departure.getDepartureDate(),
                departure.getMinParticipants(),
                departure.getCapacity(),
                remainingCount,
                departure.getItinerary(),
                departure.getSaleStartDate(),
                departure.getSaleEndDate(),
                departure.getSalePrice(),
                departure.getStatus()
        );
    }

    public void close(Long id) {
        Departure departure = getDeparture(id);
        departure.close();
    }

    public void cancel(Long id) {
        Departure departure = getDeparture(id);
        departure.cancel();
    }

    private Departure getDeparture(Long id) {
        return departureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다. id=" + id));
    }
}
