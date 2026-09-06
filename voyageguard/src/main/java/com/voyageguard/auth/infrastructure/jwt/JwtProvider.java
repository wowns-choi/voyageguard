package com.voyageguard.auth.infrastructure.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 우리 자체 JWT 발급/검증. Gateway만 검증하는 구조를 전제로, 대칭키(HMAC) 하나로 충분함
 * - 여러 서비스가 각자 검증(Zero Trust)해야 한다면 비대칭키(개인키로 서명, 공개키로 검증)가
 * 필요해지지만, 지금은 이 컴포넌트(현재는 모놀리스 진입점 필터, 나중엔 Gateway) 하나만
 * 검증하므로 대칭키로 단순하게 간다.
 */
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(subject);
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
