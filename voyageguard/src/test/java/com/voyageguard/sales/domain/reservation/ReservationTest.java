package com.voyageguard.sales.domain.reservation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    private Reservation createReservation() {
        return Reservation.create(1L, 2, "홍길동");
    }

    @Test
    void create_시_REQUESTED_상태로_생성된다() {
        Reservation reservation = createReservation();

        assertEquals(1L, reservation.getDepartureId());
        assertEquals(2, reservation.getHeadcount());
        assertEquals(ReservationStatus.REQUESTED, reservation.getStatus());
    }

    @Test
    void REQUESTED_상태에서_confirm_하면_CONFIRMED로_전이된다() {
        Reservation reservation = createReservation();

        reservation.confirm();

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void REQUESTED가_아닌_상태에서_confirm_하면_예외가_발생한다() {
        Reservation reservation = createReservation();
        reservation.confirm();

        assertThrows(IllegalStateException.class, reservation::confirm);
    }

    @Test
    void REQUESTED_상태에서_cancel_하면_CANCELLED로_전이된다() {
        Reservation reservation = createReservation();

        reservation.cancel();

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void CONFIRMED_상태에서_cancel_하면_CANCELLED로_전이된다() {
        Reservation reservation = createReservation();
        reservation.confirm();

        reservation.cancel();

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void REQUESTED나_CONFIRMED가_아닌_상태에서_cancel_하면_예외가_발생한다() {
        Reservation reservation = createReservation();
        reservation.cancel();

        assertThrows(IllegalStateException.class, reservation::cancel);
    }
}
