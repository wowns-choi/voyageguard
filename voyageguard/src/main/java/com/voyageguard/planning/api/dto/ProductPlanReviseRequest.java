package com.voyageguard.planning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductPlanReviseRequest(@Schema(description = "재수정된 제목") String newTitle) {
}
