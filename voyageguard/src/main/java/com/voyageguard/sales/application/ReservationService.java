package com.voyageguard.sales.application;

import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.planning.domain.departure.DepartureStatus;
import com.voyageguard.sales.domain.inventory.Inventory;
import com.voyageguard.sales.domain.inventory.InventoryRepository;
import com.voyageguard.sales.domain.reservation.Reservation;
import com.voyageguard.sales.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final DepartureRepository departureRepository;
    private final InventoryRepository inventoryRepository;

    public Long request(Long departureId, Integer headcount, String travelerName) {
        Departure departure = departureRepository.findById(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다. id=" + departureId));
        if (departure.getStatus() != DepartureStatus.OPEN) {
            throw new IllegalStateException("모집중 상태의 회차만 예약할 수 있습니다. 현재 상태: " + departure.getStatus());
        }

        Inventory inventory = inventoryRepository.findByDepartureIdForUpdate(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다. departureId=" + departureId));
        inventory.decrease(headcount);

        Reservation reservation = Reservation.create(departureId, headcount, travelerName);
        return reservationRepository.save(reservation).getId();
    }

    public void cancel(Long id) {
        Reservation reservation = getReservation(id);
        reservation.cancel();

        Inventory inventory = inventoryRepository.findByDepartureIdForUpdate(reservation.getDepartureId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다. departureId=" + reservation.getDepartureId()));
        inventory.increase(reservation.getHeadcount());
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. id=" + id));
    }
}
