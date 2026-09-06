package com.voyageguard.auth.api.dto;

import com.voyageguard.auth.domain.member.AuthProvider;
import com.voyageguard.auth.domain.member.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberResponse(
        @Schema(description = "회원 id") Long id,
        @Schema(description = "이메일") String email,
        @Schema(description = "이름") String name,
        @Schema(description = "가입 경로") AuthProvider provider,
        @Schema(description = "권한") MemberRole role) {
}
