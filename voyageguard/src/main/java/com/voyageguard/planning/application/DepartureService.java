package com.voyageguard.planning.application;

import com.voyageguard.planning.api.dto.DepartureResponse;
import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.planning.domain.departure.DepartureStatus;
import com.voyageguard.planning.domain.product.Product;
import com.voyageguard.planning.domain.product.ProductRepository;
import com.voyageguard.planning.domain.product.ProductStatus;
import com.voyageguard.planning.application.inventory.InventoryClient;
import com.voyageguard.sales.application.InventoryService;
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
    private final InventoryService inventoryService;
    private final InventoryClient inventoryClient;

    /**
     * Departure는 정의상 "실제 예약 가능한 단위"라 Inventory 없이 존재하면 안 되므로, 같은
     * 트랜잭션에서 Inventory까지 함께 생성한다. Planning이 Sales(InventoryService)를 직접
     * 호출하는 임시 조치 - 원래는 Kafka(DepartureCreated 이벤트)로 비동기 생성하는 게 BC
     * 경계상 맞지만, 그러면 이벤트 릴레이 지연 동안 "회차는 있는데 예약은 안 되는" 창이 실제로
     * 생겨서 지금은 동기 호출로 그 창 자체를 없앴다. Sales를 실제로 분리 배포하는 시점엔
     * 이 호출을 Kafka 이벤트로 전환해야 함 (CLAUDE.md 참고).
     */
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

        inventoryService.create(departureId, capacity);

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

    // 조회 전용 - Departure/Product는 Planning 자체 DB에서, 잔여재고는 InventoryClient(동기 REST)로
    // Sales에서 가져와 합침. MSA 대비 1단계 적용 후엔 더 이상 Sales DB를 직접 안 읽으므로
    // readOnly=true(Planning 자신의 DB 트랜잭션 기준)로 다시 둬도 안전함.
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
