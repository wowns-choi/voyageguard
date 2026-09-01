package com.voyageguard.planning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record DepartureCreateRequest(
        @Schema(description = "상품 id") Long productId,
        @Schema(description = "출발일자") LocalDate departureDate,
        @Schema(description = "최소출발인원") Integer minParticipants,
        @Schema(description = "정원") Integer capacity,
        @Schema(description = "상품일정") String itinerary,
        @Schema(description = "판매 시작일") LocalDate saleStartDate,
        @Schema(description = "판매 종료일") LocalDate saleEndDate,
        @Schema(description = "판매가") Integer salePrice) {
}
