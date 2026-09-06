package com.voyageguard.auth.infrastructure.security;

import com.voyageguard.auth.infrastructure.jwt.JwtAuthenticationFilter;
import com.voyageguard.auth.infrastructure.oauth.CustomOAuth2UserService;
import com.voyageguard.auth.infrastructure.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 이 필터체인 자체가 지금(모놀리스) 단계에서 "임시 Gateway" 역할을 겸한다.
 *
 * - 기존 BC(Planning/Sales/Payment) 엔드포인트는 아직 로그인 강제로 안 잠금 - 회원 도입과
 *   "어디에 인증을 강제할지"는 별개 결정이라 분리함(한 번에 변수 하나씩).
 * - 지금 실제로 보호되는 건 MemberController.me() 하나뿐이고, 그것도 URL 매처가 아니라
 *   컨트롤러 안에서 CurrentMember로 직접 확인함 - 전역 인증 강제 없이 메커니즘만 검증하기 위함.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // JWT라 쿠키 세션 자체가 없어 CSRF 방어 불필요
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
