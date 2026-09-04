package com.voyageguard.planning.api.dto;

import com.voyageguard.planning.domain.departure.DepartureStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record DepartureResponse(
        @Schema(description = "회차 id") Long id,
        @Schema(description = "상품 id") Long productId,
        @Schema(description = "상품명") String productTitle,
        @Schema(description = "출발일자") LocalDate departureDate,
        @Schema(description = "최소출발인원") Integer minParticipants,
        @Schema(description = "정원") Integer capacity,
        @Schema(description = "잔여 좌석") Integer remainingCount,
        @Schema(description = "상품일정") String itinerary,
        @Schema(description = "판매 시작일") LocalDate saleStartDate,
        @Schema(description = "판매 종료일") LocalDate saleEndDate,
        @Schema(description = "판매가") Integer salePrice,
        @Schema(description = "회차 상태") DepartureStatus status) {
}
