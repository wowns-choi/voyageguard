package com.voyageguard.planning.domain.departure;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DepartureTest {

    private Departure createDeparture() {
        return Departure.create(1L, LocalDate.of(2026, 12, 20), 10, 30, "발리 5박 6일 일정",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 10), 1500000);
    }

    @Test
    void create_시_OPEN_상태로_생성된다() {
        Departure departure = createDeparture();

        assertEquals(1L, departure.getProductId());
        assertEquals(DepartureStatus.OPEN, departure.getStatus());
    }

    @Test
    void OPEN_상태에서_close_하면_CLOSED로_전이된다() {
        Departure departure = createDeparture();

        departure.close();

        assertEquals(DepartureStatus.CLOSED, departure.getStatus());
    }

    @Test
    void OPEN이_아닌_상태에서_close_하면_예외가_발생한다() {
        Departure departure = createDeparture();
        departure.close();

        assertThrows(IllegalStateException.class, departure::close);
    }

    @Test
    void OPEN_상태에서_cancel_하면_CANCELLED로_전이된다() {
        Departure departure = createDeparture();

        departure.cancel();

        assertEquals(DepartureStatus.CANCELLED, departure.getStatus());
    }

    @Test
    void CLOSED_상태에서_cancel_하면_CANCELLED로_전이된다() {
        Departure departure = createDeparture();
        departure.close();

        departure.cancel();

        assertEquals(DepartureStatus.CANCELLED, departure.getStatus());
    }

    @Test
    void OPEN이나_CLOSED가_아닌_상태에서_cancel_하면_예외가_발생한다() {
        Departure departure = createDeparture();
        departure.cancel();

        assertThrows(IllegalStateException.class, departure::cancel);
    }
}
