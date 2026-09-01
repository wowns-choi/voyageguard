package com.voyageguard.planning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record ProductCreateRequest(
        @Schema(description = "승인된 기획 id") Long planId,
        @Schema(description = "상품명") String title,
        @Schema(description = "상품 설명") String description,
        @Schema(description = "판매 시작일") LocalDate saleStartDate,
        @Schema(description = "판매 종료일") LocalDate saleEndDate) {
}
