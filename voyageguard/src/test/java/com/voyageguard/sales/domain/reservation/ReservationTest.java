package com.voyageguard.sales.domain.reservation;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    private Reservation createReservation() {
        return createReservation(LocalDate.now().plusMonths(2));
    }

    private Reservation createReservation(LocalDate saleEndDate) {
        return Reservation.create(1L, 2, "홍길동", saleEndDate);
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

    @Test
    void create_시_expiresAt은_요청_시점_10분_후다() {
        Reservation reservation = createReservation();

        assertEquals(reservation.getRequestedAt().plusMinutes(10), reservation.getExpiresAt());
    }

    @Test
    void create_시_판매종료일이_10분보다_가까우면_expiresAt은_판매종료일이다() {
        LocalDate saleEndDate = LocalDate.now();
        Reservation reservation = createReservation(saleEndDate);

        assertEquals(saleEndDate.atStartOfDay(), reservation.getExpiresAt());
    }

    @Test
    void REQUESTED_상태에서_expire_하면_EXPIRED로_전이된다() {
        Reservation reservation = createReservation();

        reservation.expire();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
    }

    @Test
    void REQUESTED가_아닌_상태에서_expire_하면_예외가_발생한다() {
        Reservation reservation = createReservation();
        reservation.confirm();

        assertThrows(IllegalStateException.class, reservation::expire);
    }
}
