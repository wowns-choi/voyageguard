package com.voyageguard.auth.infrastructure.oauth;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/** 구글이 준 원본 OAuth2User에, 우리 Member의 id를 얹어서 로그인 성공 핸들러에 전달하기 위한 래퍼 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final Long memberId;

    public CustomOAuth2User(OAuth2User delegate, Long memberId) {
        this.delegate = delegate;
        this.memberId = memberId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
