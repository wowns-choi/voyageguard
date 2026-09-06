package com.voyageguard.auth.infrastructure.oauth;

import com.voyageguard.auth.api.dto.MemberTokenResponse;
import com.voyageguard.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * 구글 로그인 최종 성공 지점 - 우리 자체 JWT를 발급해서 응답한다(구글 토큰을 그대로 내려주지 않음).
 * 이후로는 로컬 로그인과 완전히 동일한 토큰이라, 클라이언트/Gateway/다른 BC 어디에서도
 * "구글로 로그인했는지 이메일로 로그인했는지" 구분할 필요가 없다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        String token = jwtProvider.generateToken(principal.getMemberId());

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new MemberTokenResponse(token)));
    }
}
