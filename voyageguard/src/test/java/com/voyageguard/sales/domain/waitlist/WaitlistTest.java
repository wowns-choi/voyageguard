package com.voyageguard.sales.domain.waitlist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaitlistTest {

    private Waitlist createWaitlist() {
        return Waitlist.create(1L, 2, "홍길동");
    }

    @Test
    void create_시_WAITING_상태로_생성된다() {
        Waitlist waitlist = createWaitlist();

        assertEquals(1L, waitlist.getDepartureId());
        assertEquals(2, waitlist.getHeadcount());
        assertEquals(WaitlistStatus.WAITING, waitlist.getStatus());
    }

    @Test
    void WAITING_상태에서_promote_하면_PROMOTED로_전이된다() {
        Waitlist waitlist = createWaitlist();

        waitlist.promote();

        assertEquals(WaitlistStatus.PROMOTED, waitlist.getStatus());
    }

    @Test
    void WAITING이_아닌_상태에서_promote_하면_예외가_발생한다() {
        Waitlist waitlist = createWaitlist();
        waitlist.promote();

        assertThrows(IllegalStateException.class, waitlist::promote);
    }

    @Test
    void WAITING_상태에서_expire_하면_EXPIRED로_전이된다() {
        Waitlist waitlist = createWaitlist();

        waitlist.expire();

        assertEquals(WaitlistStatus.EXPIRED, waitlist.getStatus());
    }

    @Test
    void WAITING이_아닌_상태에서_expire_하면_예외가_발생한다() {
        Waitlist waitlist = createWaitlist();
        waitlist.expire();

        assertThrows(IllegalStateException.class, waitlist::expire);
    }
}
