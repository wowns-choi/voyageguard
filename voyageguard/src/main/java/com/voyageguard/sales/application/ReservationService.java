package com.voyageguard.sales.application;

import com.voyageguard.common.outbox.OutboxEvent;
import com.voyageguard.common.outbox.OutboxEventRepository;
import com.voyageguard.sales.api.dto.ReservationResponse;
import com.voyageguard.sales.application.departure.DepartureClient;
import com.voyageguard.sales.application.departure.DepartureView;
import com.voyageguard.sales.application.inventory.InventoryConcurrencyStrategy;
import com.voyageguard.sales.domain.reservation.Reservation;
import com.voyageguard.sales.domain.reservation.ReservationCancelledEvent;
import com.voyageguard.sales.domain.reservation.ReservationRepository;
import com.voyageguard.sales.domain.reservation.ReservationStatus;
import com.voyageguard.sales.infrastructure.redis.WaitlistRankRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final DepartureClient departureClient;
    private final InventoryConcurrencyStrategy inventoryConcurrencyStrategy;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final WaitlistRankRepository waitlistRankRepository;

    // MSA 대비 1단계: Planning의 Departure를 DB로 직접 안 읽고 DepartureClient(동기 REST)로 조회
    public Long request(Long departureId, Integer headcount, String travelerName) {
        DepartureView departure = departureClient.get(departureId);
        if (departure.status() != DepartureView.Status.OPEN) {
            throw new IllegalStateException("모집중 상태의 회차만 예약할 수 있습니다. 현재 상태: " + departure.status());
        }
        // 대기열이 있으면 새치기 방지 - 신규 예약을 막고 대기 등록으로 유도
        if (waitlistRankRepository.hasWaiting(departureId)) {
            throw new IllegalStateException("대기 중인 인원이 있어 새 예약을 받을 수 없습니다. 대기 등록을 이용해주세요.");
        }

        inventoryConcurrencyStrategy.decrease(departureId, headcount);

        Reservation reservation = Reservation.create(departureId, headcount, travelerName, departure.saleEndDate());
        return reservationRepository.save(reservation).getId();
    }

    public void cancel(Long id) {
        Reservation reservation = getReservation(id);
        reservation.cancel();
        releaseInventoryAndNotify(reservation, "ReservationCancelled");
    }

    // MSA 대비 1단계: Payment가 예약 상태를 직접 DB로 안 읽고 이 API(동기 REST)로 조회하게 함
    @Transactional(readOnly = true)
    public ReservationResponse get(Long id) {
        Reservation reservation = getReservation(id);
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDepartureId(),
                reservation.getHeadcount(),
                reservation.getTravelerName(),
                reservation.getStatus(),
                reservation.getExpiresAt()
        );
    }

    /**
     * 예약 -> 재고 있나? -> 있다 -> 결제로 간 경우,
     * 결제 유예시간(Reservation.expiresAt) 안에 결제하지 않은 예약은 만료시킨다.
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 - 유예기간 자체가 10분으로 짧아서 스캔 주기도 짧게
    public void expireStaleReservations() {
        List<Reservation> targets = reservationRepository.findByStatusAndExpiresAtBefore(
                ReservationStatus.REQUESTED, LocalDateTime.now());

        for (Reservation reservation : targets) {
            reservation.expire(); // 대기열 만료(X), 예약 만료(O)
            releaseInventoryAndNotify(reservation, "ReservationExpired");
        }
    }

    /**
     * 재고 반납 + 대기열 재평가 트리거(Outbox 경유).
     *
     * 취소든 만료든 "이 회차 재고가 늘었다"는 사실은 같아서 로직은 공유하고,
     * eventType 라벨만 다르게 남겨 원인을 구분해둔다.
     */
    private void releaseInventoryAndNotify(Reservation reservation, String eventType) {

        // 재고 반납
        inventoryConcurrencyStrategy.increase(reservation.getDepartureId(), reservation.getHeadcount());

        // Kafka로 바로 안 보내고, 같은 트랜잭션 안에서 outbox 테이블에 "보낼 것"만 원자적으로 기록
        ReservationCancelledEvent event = new ReservationCancelledEvent(reservation.getDepartureId(), reservation.getHeadcount());
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JacksonException e) {
            throw new IllegalStateException(eventType + " 직렬화 실패", e);
        }
        outboxEventRepository.save(
                OutboxEvent.create(
                        eventType,
                        "reservation.cancelled", // 토픽 : "예약이 취소됨" - 원인(취소/만료)과 무관하게 구독측 처리는 동일
                        reservation.getDepartureId().toString(), // Key : 회차 id
                        payload // 회차 id, 인원수
                )
        );
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. id=" + id));
    }
}
