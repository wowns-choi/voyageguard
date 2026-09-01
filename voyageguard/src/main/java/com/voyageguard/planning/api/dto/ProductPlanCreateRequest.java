package com.voyageguard.planning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductPlanCreateRequest(@Schema(description = "기획 제목") String title) {
}
