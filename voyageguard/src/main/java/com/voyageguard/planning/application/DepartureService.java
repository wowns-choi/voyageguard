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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartureService {
    private final DepartureRepository departureRepository;
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    // Departure는 "실제 예약 가능한 단위"라 Inventory 없이 존재하면 안 되므로, 재고 생성을
    // REST로 동기 호출한다 - 실패하면 예외가 전파되어 Departure 저장도 함께 롤백됨(카프카로
    // 비동기 처리하면 재고 없는 회차가 존재하는 창이 생겨서 채택하지 않음).
    public Long create(Long productId, LocalDate departureDate, Integer minParticipants, Integer capacity,
                        String itinerary, LocalDate saleStartDate, LocalDate saleEndDate, Integer salePrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));
        if (product.getStatus() == ProductStatus.ENDED || product.getStatus() == ProductStatus.DISCARDED) {
            throw new IllegalStateException("판매종료 또는 폐기된 상품에는 회차를 추가할 수 없습니다. 현재 상태: " + product.getStatus());
        }

        Departure departure = Departure.create(productId, departureDate, minParticipants, capacity,
                itinerary, saleStartDate, saleEndDate, salePrice);
        Long departureId = departureRepository.save(departure).getId();

        inventoryClient.create(departureId, capacity);

        return departureId;
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
