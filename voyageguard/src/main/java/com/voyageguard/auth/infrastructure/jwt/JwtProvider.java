package com.voyageguard.auth.infrastructure.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 우리 자체 JWT 발급/검증. 비대칭키(RSA) 사용 - 발급은 개인키로만, 검증은 공개키로 가능함.
 * 대칭키 대신 이걸 쓴 이유: 나중에 AWS API Gateway로 실제 분리되면, Gateway의 내장 JWT
 * 검증 기능(JWKS 기반)을 그대로 쓸 수 있음 - 공개키만 JWKS로 공개해두면 되고, 대칭키처럼
 * "검증하려면 시크릿을 Gateway와 공유해야 하는"(그러면 더 이상 비밀이 아님) 문제가 없어서
 * Lambda Authorizer를 직접 짤 필요가 없어짐.
 */
@Component
public class JwtProvider {

    @Value("${jwt.private-key}")
    private String privateKeyValue;

    @Value("${jwt.public-key}")
    private String publicKeyValue;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(privateKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(publicKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        String subject = Jwts.parser()
                .verifyWith(publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(subject);
    }

    private PrivateKey privateKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(privateKeyValue);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("JWT 개인키 로딩 실패", e);
        }
    }

    private PublicKey publicKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(publicKeyValue);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("JWT 공개키 로딩 실패", e);
        }
    }
}
