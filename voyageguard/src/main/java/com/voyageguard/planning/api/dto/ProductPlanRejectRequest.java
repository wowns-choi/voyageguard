package com.voyageguard.planning.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductPlanRejectRequest(@Schema(description = "반려 사유") String reason) {
}
