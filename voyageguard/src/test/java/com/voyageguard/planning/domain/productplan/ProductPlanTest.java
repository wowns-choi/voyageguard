package com.voyageguard.planning.domain.productplan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductPlanTest {

    @Test
    void create_시_DRAFT_상태로_생성된다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");

        assertEquals("2026 여름 발리 패키지", plan.getTitle());
        assertEquals(ProductPlanStatus.DRAFT, plan.getStatus());
    }

    @Test
    void DRAFT_상태에서_requestReview_하면_PENDING_REVIEW로_전이된다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");

        plan.requestReview();

        assertEquals(ProductPlanStatus.PENDING_REVIEW, plan.getStatus());
    }

    @Test
    void DRAFT가_아닌_상태에서_requestReview_하면_예외가_발생한다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");
        plan.requestReview();

        assertThrows(IllegalStateException.class, plan::requestReview);
    }

    @Test
    void PENDING_REVIEW_상태에서_approve_하면_APPROVED로_전이된다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");
        plan.requestReview();

        plan.approve();

        assertEquals(ProductPlanStatus.APPROVED, plan.getStatus());
    }

    @Test
    void PENDING_REVIEW가_아닌_상태에서_approve_하면_예외가_발생한다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");

        assertThrows(IllegalStateException.class, plan::approve);
    }

    @Test
    void PENDING_REVIEW_상태에서_reject_하면_REJECTED로_전이되고_사유가_저장된다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");
        plan.requestReview();

        plan.reject("예산 초과");

        assertEquals(ProductPlanStatus.REJECTED, plan.getStatus());
        assertEquals("예산 초과", plan.getRejectionReason());
    }

    @Test
    void PENDING_REVIEW가_아닌_상태에서_reject_하면_예외가_발생한다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");

        assertThrows(IllegalStateException.class, () -> plan.reject("예산 초과"));
    }

    @Test
    void REJECTED_상태에서_revise_하면_DRAFT로_되돌아가고_제목과_반려사유가_갱신된다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");
        plan.requestReview();
        plan.reject("예산 초과");

        plan.revise("2026 여름 발리 패키지 (예산 조정)");

        assertEquals(ProductPlanStatus.DRAFT, plan.getStatus());
        assertEquals("2026 여름 발리 패키지 (예산 조정)", plan.getTitle());
        assertNull(plan.getRejectionReason());
    }

    @Test
    void REJECTED가_아닌_상태에서_revise_하면_예외가_발생한다() {
        ProductPlan plan = ProductPlan.create("2026 여름 발리 패키지");

        assertThrows(IllegalStateException.class, () -> plan.revise("수정된 제목"));
    }
}
