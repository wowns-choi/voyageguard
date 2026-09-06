package com.voyageguard.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberTokenResponse(
        @Schema(description = "발급된 JWT") String accessToken) {
}
