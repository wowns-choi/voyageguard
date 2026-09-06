package com.voyageguard.auth.application;

import com.voyageguard.auth.domain.member.AuthProvider;
import com.voyageguard.auth.domain.member.Member;
import com.voyageguard.auth.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long signUp(String email, String rawPassword, String name) {
        if (memberRepository.existsByEmailAndProvider(email, AuthProvider.LOCAL)) {
            throw new IllegalStateException("이미 가입된 이메일입니다. email=" + email);
        }

        Member member = Member.createLocal(email, passwordEncoder.encode(rawPassword), name);
        return memberRepository.save(member).getId();
    }

    // 소셜 로그인 - 이미 가입된 적 있으면 그대로 조회, 처음이면 자동 가입
    public Member findOrCreateSocial(String email, String name, AuthProvider provider, String providerId) {
        return memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> memberRepository.save(Member.createSocial(email, name, provider, providerId)));
    }

    @Transactional(readOnly = true)
    public Member get(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id=" + id));
    }
}
