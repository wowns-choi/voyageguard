package com.voyageguard.auth.api;

import com.voyageguard.auth.api.dto.MemberLoginRequest;
import com.voyageguard.auth.api.dto.MemberTokenResponse;
import com.voyageguard.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로컬 로그인", description = "이메일/비밀번호로 로그인하고 JWT를 발급받는다.")
    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    @PostMapping("/login")
    public MemberTokenResponse login(@RequestBody MemberLoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return new MemberTokenResponse(token);
    }
}
