package com.voyageguard.sales.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고객 예약 건의 생애주기(REQUESTED -> CONFIRMED/CANCELLED)를 캡슐화한 Aggregate Root.
 * 외부에서 상태를 직접 바꿀 수 없고, 각 전이 메서드가 현재 상태를 검증한 뒤에만 상태를 변경한다.
 * confirm()은 결제완료가 트리거지만 Payment BC가 아직 없어 지금은 Service/Controller에서
 * 호출하지 않는다 - Payment BC 붙을 때 이어서 연결.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Departure 를 객체가 아닌 ID로 참조한다 (Reference Other Aggregates by Identity Only)
    private Long departureId;

    private Integer headcount; // 예약 인원수

    private String travelerName; // 예약한 사람 이름

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Reservation(Long departureId, Integer headcount, String travelerName) {
        this.departureId = departureId;
        this.headcount = headcount;
        this.travelerName = travelerName;
        this.status = ReservationStatus.REQUESTED;
    }

    public static Reservation create(Long departureId, Integer headcount, String travelerName) {
        return new Reservation(departureId, headcount, travelerName);
    }

    public void confirm() {
        if (status != ReservationStatus.REQUESTED) {
            throw new IllegalStateException("예약요청 상태에서만 확정할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (status != ReservationStatus.REQUESTED && status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("예약요청 또는 확정 상태에서만 취소할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ReservationStatus.CANCELLED;
    }
}
