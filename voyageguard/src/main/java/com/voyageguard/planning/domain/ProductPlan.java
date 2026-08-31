package com.voyageguard.planning.domain;

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
 * 상품 기획(ProductPlan)의 상태 전이(DRAFT → PENDING_REVIEW → APPROVED/REJECTED)를 캡슐화한 Aggregate Root.
 * 외부에서 상태를 직접 바꿀 수 없고, 각 전이 메서드가 현재 상태를 검증한 뒤에만 상태를 변경한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private ProductPlanStatus status;

    private String rejectionReason;

    private ProductPlan(String title) {
        this.title = title;
        this.status = ProductPlanStatus.DRAFT;
    }

    public static ProductPlan create(String title) {
        return new ProductPlan(title);
    }

    public void requestReview() {
        if (status != ProductPlanStatus.DRAFT) {
            throw new IllegalStateException("기획중 상태에서만 검수를 요청할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductPlanStatus.PENDING_REVIEW;
    }

    public void approve() {
        if (status != ProductPlanStatus.PENDING_REVIEW) {
            throw new IllegalStateException("검수요청 상태에서만 승인할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductPlanStatus.APPROVED;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        if (status != ProductPlanStatus.PENDING_REVIEW) {
            throw new IllegalStateException("검수요청 상태에서만 반려할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductPlanStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void revise(String newTitle) {
        if (status != ProductPlanStatus.REJECTED) {
            throw new IllegalStateException("반려 상태에서만 재수정할 수 있습니다. 현재 상태: " + status);
        }
        this.title = newTitle;
        this.status = ProductPlanStatus.DRAFT;
        this.rejectionReason = null;
    }
}
