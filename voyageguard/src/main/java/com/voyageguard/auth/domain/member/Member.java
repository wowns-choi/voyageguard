package com.voyageguard.auth.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 Aggregate Root. 로컬(이메일/비밀번호)과 구글 소셜 로그인, 두 가입 경로를 하나의
 * Aggregate로 표현한다. 같은 이메일이라도 LOCAL과 GOOGLE 계정은 별개로 취급한다 - 계정 연동은
 * 스코프에서 의도적으로 제외(Payment의 반복 부분환불 제외와 같은 이유 - 별도의 복잡한 기능).
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "provider"}),
        @UniqueConstraint(columnNames = {"provider", "providerId"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password; // LOCAL만 값 있음(BCrypt 해시) - GOOGLE 계정은 null

    private String name;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId; // GOOGLE만 값 있음(구글이 부여한 사용자 고유 id)

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private LocalDateTime createdAt;

    private Member(String email, String password, String name, AuthProvider provider, String providerId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.role = MemberRole.USER; // 가입 시점엔 항상 USER - ADMIN 승격 절차는 아직 미구현
        this.createdAt = LocalDateTime.now();
    }

    // 로컬 회원가입 - hashedPassword는 이미 해시된 값을 받는다(엔티티가 PasswordEncoder에 의존하지 않도록 하기 위함)
    public static Member createLocal(String email, String hashedPassword, String name) {
        return new Member(email, hashedPassword, name, AuthProvider.LOCAL, null);
    }

    // 구글 소셜 로그인 최초 진입 시 자동 가입
    public static Member createSocial(String email, String name, AuthProvider provider, String providerId) {
        return new Member(email, null, name, provider, providerId);
    }
}
