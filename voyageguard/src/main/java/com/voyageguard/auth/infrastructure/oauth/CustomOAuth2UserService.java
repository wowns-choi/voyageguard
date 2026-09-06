package com.voyageguard.auth.infrastructure.oauth;

import com.voyageguard.auth.application.MemberService;
import com.voyageguard.auth.domain.member.AuthProvider;
import com.voyageguard.auth.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 구글 로그인 성공 시 스프링 시큐리티가 호출하는 지점. 구글이 준 사용자 정보(email/name/sub)로
 * 우리 Member를 조회하거나 없으면 자동 가입시키고, 그 결과(memberId)를 CustomOAuth2User에
 * 실어서 다음 단계(OAuth2LoginSuccessHandler)로 넘긴다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub"); // 구글이 부여한 사용자 고유 id

        Member member = memberService.findOrCreateSocial(email, name, AuthProvider.GOOGLE, providerId);

        return new CustomOAuth2User(oAuth2User, member.getId());
    }
}
