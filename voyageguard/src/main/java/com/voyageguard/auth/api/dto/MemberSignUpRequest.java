package com.voyageguard.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberSignUpRequest(
        @Schema(description = "이메일(로그인 id로 사용)") String email,
        @Schema(description = "비밀번호") String password,
        @Schema(description = "이름") String name) {
}
