package com.voyageguard.auth.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder를 SecurityConfig 안에 두면 순환 참조가 생김 - SecurityConfig의 생성자가
 * 필요로 하는 CustomOAuth2UserService -> MemberService가 다시 PasswordEncoder를 필요로 해서,
 * "SecurityConfig 인스턴스가 있어야 꺼낼 수 있는 빈을, SecurityConfig 인스턴스를 만드는 데
 * 필요로 하는" 순환이 생기기 때문에 별도 설정 클래스로 분리한다.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
