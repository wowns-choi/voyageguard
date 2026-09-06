package com.voyageguard.auth.infrastructure.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 지금(모놀리스) 단계에서 Gateway 역할을 임시로 대신하는 필터 - 요청마다 JWT를 직접 검증하고
 * principal에 memberId(Long)만 채운다. 나중에 진짜 Gateway로 분리되면, 이 필터는 검증 로직
 * 없이 헤더(X-Member-Id)만 읽는 걸로 교체되고 SecurityContext를 채우는 부분은 그대로 유지된다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && jwtProvider.validateToken(token)) {
            Long memberId = jwtProvider.getMemberId(token);
            Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
