package com.voyageguard.planning.domain.departure;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회차(Departure)의 상태 전이(OPEN -> CLOSED/CANCELLED)를 캡슐화한 Aggregate Root.
 * 상품의 특정 출발일자를 기준으로 정원/판매기간/판매가가 개별 관리되는 실제 예약 가능 단위.
 * 외부에서 상태를 직접 바꿀 수 없고, 각 전이 메서드가 현재 상태를 검증한 뒤에만 상태를 변경한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Departure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Product를 객체(@ManyToOne)가 아닌 ID로 참조한다 (Reference Other Aggregates by Identity Only).
     * 이유는 Product.planId와 동일 - CLAUDE.md의 "다른 Aggregate는 ID로만 참조한다" 원칙 참고.
     */
    private Long productId;

    private LocalDate departureDate;

    private Integer minParticipants;

    private Integer capacity;

    private String itinerary;

    private LocalDate saleStartDate;

    private LocalDate saleEndDate;

    private Integer salePrice;

    @Enumerated(EnumType.STRING)
    private DepartureStatus status;

    private Departure(Long productId, LocalDate departureDate, Integer minParticipants, Integer capacity,
                       String itinerary, LocalDate saleStartDate, LocalDate saleEndDate, Integer salePrice) {
        this.productId = productId;
        this.departureDate = departureDate;
        this.minParticipants = minParticipants;
        this.capacity = capacity;
        this.itinerary = itinerary;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.salePrice = salePrice;
        this.status = DepartureStatus.OPEN;
    }

    public static Departure create(Long productId, LocalDate departureDate, Integer minParticipants, Integer capacity,
                                    String itinerary, LocalDate saleStartDate, LocalDate saleEndDate, Integer salePrice) {
        return new Departure(productId, departureDate, minParticipants, capacity, itinerary, saleStartDate, saleEndDate, salePrice);
    }

    public void close() {
        if (status != DepartureStatus.OPEN) {
            throw new IllegalStateException("모집중 상태에서만 마감할 수 있습니다. 현재 상태: " + status);
        }
        this.status = DepartureStatus.CLOSED;
    }

    public void cancel() {
        if (status != DepartureStatus.OPEN && status != DepartureStatus.CLOSED) {
            throw new IllegalStateException("모집중 또는 마감 상태에서만 취소할 수 있습니다. 현재 상태: " + status);
        }
        this.status = DepartureStatus.CANCELLED;
    }
}
