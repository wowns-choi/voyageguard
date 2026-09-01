package com.voyageguard.planning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductUpdateInfoRequest(
        @Schema(description = "상품명") String title,
        @Schema(description = "상품 설명") String description) {
}
