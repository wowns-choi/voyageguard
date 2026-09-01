package com.voyageguard.planning.domain;

public enum ProductPlanStatus {
    DRAFT, // 기획중 (작성 시작)
    PENDING_REVIEW, // 검수요청됨
    APPROVED, // 승인됨
    REJECTED // 반려됨
}
