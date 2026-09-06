package com.voyageguard.auth.application;

import com.voyageguard.auth.domain.member.AuthProvider;
import com.voyageguard.auth.domain.member.Member;
import com.voyageguard.auth.domain.member.MemberRepository;
import com.voyageguard.auth.infrastructure.jwt.JwtProvider;
import com.voyageguard.common.exception.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원 미존재/비밀번호 불일치 둘 다 같은 메시지로 응답 - 어느 쪽이 틀렸는지 알려주면
    // 공격자가 "가입된 이메일 목록"을 추측해낼 수 있음(계정 존재 여부 노출 방지)
    public String login(String email, String rawPassword) {
        Member member = memberRepository.findByEmailAndProvider(email, AuthProvider.LOCAL)
                .orElseThrow(() -> new AuthenticationFailedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new AuthenticationFailedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.generateToken(member.getId());
    }
}
