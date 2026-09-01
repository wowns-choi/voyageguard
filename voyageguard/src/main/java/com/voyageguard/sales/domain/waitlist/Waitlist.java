package com.voyageguard.sales.domain.waitlist;

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
 * 재고 부족 시 대기 등록한 건의 상태 전이(WAITING -> PROMOTED/EXPIRED)를 캡슐화한 Aggregate Root.
 * 실제 대기 순번은 이 엔티티가 아니라 Redis Sorted Set이 원천(source of truth) - departureId별로
 * 여러 대기자의 순번을 원자적으로 관리해야 해서 RDB 비관적 락보다 Redis ZADD/ZRANK가 적합하기
 * 때문이다 (CLAUDE.md "Redis - 용도별 자료구조 구분" 참고). 이 엔티티는 대기 건의 상태(승격/만료
 * 여부)만 갖는다.
 * promote()/expire()는 원래 ReservationCancelled 이벤트(Kafka)가 트리거지만, 아직 Kafka
 * 연동이 없어 지금은 Service/Controller에서 호출하지 않는다 - Kafka 이벤트 흐름 붙을 때 이어서
 * 연결.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Departure 를 객체가 아닌 ID로 참조한다 (Reference Other Aggregates by Identity Only)
    private Long departureId;

    private Integer headcount;

    private String travelerName;

    @Enumerated(EnumType.STRING)
    private WaitlistStatus status;

    private Waitlist(Long departureId, Integer headcount, String travelerName) {
        this.departureId = departureId;
        this.headcount = headcount;
        this.travelerName = travelerName;
        this.status = WaitlistStatus.WAITING;
    }

    public static Waitlist create(Long departureId, Integer headcount, String travelerName) {
        return new Waitlist(departureId, headcount, travelerName);
    }

    public void promote() {
        if (status != WaitlistStatus.WAITING) {
            throw new IllegalStateException("대기중 상태에서만 승격할 수 있습니다. 현재 상태: " + status);
        }
        this.status = WaitlistStatus.PROMOTED;
    }

    public void expire() {
        if (status != WaitlistStatus.WAITING) {
            throw new IllegalStateException("대기중 상태에서만 만료시킬 수 있습니다. 현재 상태: " + status);
        }
        this.status = WaitlistStatus.EXPIRED;
    }
}
