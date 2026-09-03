package com.voyageguard.sales.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고객 예약 건의 생애주기(REQUESTED -> CONFIRMED/CANCELLED/EXPIRED)를 캡슐화한 Aggregate Root.
 * 외부에서 상태를 직접 바꿀 수 없고, 각 전이 메서드가 현재 상태를 검증한 뒤에만 상태를 변경한다.
 */
@Entity
@Table(indexes = @Index(name = "idx_reservation_status_expires_at", columnList = "status, expiresAt"))
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

    private LocalDateTime requestedAt;

    // Departure.saleEndDate 스냅샷(Waitlist와 같은 패턴) - expiresAt 계산에 필요해서
    // 생성 시점에 복사해둔다.
    private LocalDate saleEndDate;

    // 결제 유예시간 만료 시점 - 실시간 체크아웃 세션이라 짧게(10분) 잡음.
    private static final long GRACE_MINUTES = 10;

    private LocalDateTime expiresAt;

    private Reservation(Long departureId, Integer headcount, String travelerName, LocalDate saleEndDate) {
        this.departureId = departureId;
        this.headcount = headcount;
        this.travelerName = travelerName;
        this.status = ReservationStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
        this.saleEndDate = saleEndDate;
        this.expiresAt = capBySaleEnd(requestedAt.plusMinutes(GRACE_MINUTES));
    }

    public static Reservation create(Long departureId, Integer headcount, String travelerName, LocalDate saleEndDate) {
        return new Reservation(departureId, headcount, travelerName, saleEndDate);
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

    // CANCELLED와 상태 전이 모양은 같지만, "고객이 취소" vs "결제 방치로 시스템이 자동 취소"를 구분해서 기록하기 위해 별도 메서드/상태로 둠.
    public void expire() {
        if (status != ReservationStatus.REQUESTED) {
            throw new IllegalStateException("예약요청 상태에서만 만료시킬 수 있습니다. 현재 상태: " + status);
        }
        this.status = ReservationStatus.EXPIRED;
    }

    // 하드캡과 판매종료일 중 더 이른 시점을 만료 시점으로 삼는다.
    private LocalDateTime capBySaleEnd(LocalDateTime hardCap) {
        LocalDateTime saleEnd = saleEndDate.atStartOfDay();
        return hardCap.isBefore(saleEnd) ? hardCap : saleEnd;
    }
}
