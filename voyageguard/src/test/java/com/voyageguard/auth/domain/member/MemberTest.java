package com.voyageguard.auth.domain.member;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MemberTest {

    @Test
    void createLocal_시_LOCAL_provider와_USER_role로_생성된다() {
        Member member = Member.createLocal("test@voyageguard.com", "hashedPassword", "홍길동");

        assertEquals("test@voyageguard.com", member.getEmail());
        assertEquals("hashedPassword", member.getPassword());
        assertEquals(AuthProvider.LOCAL, member.getProvider());
        assertNull(member.getProviderId());
        assertEquals(MemberRole.USER, member.getRole());
    }

    @Test
    void createSocial_시_해당_provider로_생성되고_password는_없다() {
        Member member = Member.createSocial("test@gmail.com", "홍길동", AuthProvider.GOOGLE, "google-sub-12345");

        assertEquals("test@gmail.com", member.getEmail());
        assertNull(member.getPassword());
        assertEquals(AuthProvider.GOOGLE, member.getProvider());
        assertEquals("google-sub-12345", member.getProviderId());
        assertEquals(MemberRole.USER, member.getRole());
    }
}
