package com.voyageguard.sales.application;

import com.voyageguard.common.outbox.OutboxEvent;
import com.voyageguard.common.outbox.OutboxEventRepository;
import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.planning.domain.departure.DepartureStatus;
import com.voyageguard.sales.domain.inventory.Inventory;
import com.voyageguard.sales.domain.inventory.InventoryRepository;
import com.voyageguard.sales.domain.reservation.Reservation;
import com.voyageguard.sales.domain.reservation.ReservationCancelledEvent;
import com.voyageguard.sales.domain.reservation.ReservationRepository;
import com.voyageguard.sales.infrastructure.redis.WaitlistRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final DepartureRepository departureRepository;
    private final InventoryRepository inventoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final WaitlistRankRepository waitlistRankRepository;

    public Long request(Long departureId, Integer headcount, String travelerName) {
        Departure departure = departureRepository.findById(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다. id=" + departureId));
        if (departure.getStatus() != DepartureStatus.OPEN) {
            throw new IllegalStateException("모집중 상태의 회차만 예약할 수 있습니다. 현재 상태: " + departure.getStatus());
        }
        // 대기열이 있으면 새치기 방지 - 신규 예약을 막고 대기 등록으로 유도
        if (waitlistRankRepository.hasWaiting(departureId)) {
            throw new IllegalStateException("대기 중인 인원이 있어 새 예약을 받을 수 없습니다. 대기 등록을 이용해주세요.");
        }

        Inventory inventory = inventoryRepository.findByDepartureIdForUpdate(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다. departureId=" + departureId));
        inventory.decrease(headcount);

        Reservation reservation = Reservation.create(departureId, headcount, travelerName);
        return reservationRepository.save(reservation).getId();
    }

    public void cancel(Long id) {

        // 예약 취소
        Reservation reservation = getReservation(id);
        reservation.cancel();

        // 재고 증가
        Inventory inventory = inventoryRepository.findByDepartureIdForUpdate(reservation.getDepartureId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다. departureId=" + reservation.getDepartureId()));
        inventory.increase(reservation.getHeadcount());

        // 대기열 조정을 위해서, 카프카로 보내야함
        // Kafka로 바로 안 보내고, 같은 트랜잭션 안에서 outbox 테이블에 "보낼 것"만 원자적으로 기록
        ReservationCancelledEvent event = new ReservationCancelledEvent(reservation.getDepartureId(), reservation.getHeadcount());
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JacksonException e) {
            throw new IllegalStateException("ReservationCancelledEvent 직렬화 실패", e);
        }
        outboxEventRepository.save(
                OutboxEvent.create(
                        "ReservationCancelled",
                        "reservation.cancelled", // 토픽 : "예약이 취소됨"
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
