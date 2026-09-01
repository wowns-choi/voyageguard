package com.voyageguard.sales.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Departure별 대기 순번을 Redis Sorted Set으로 관리한다. member는 Waitlist id, score는 대기
 * 등록 시각(epoch millis) - 여러 대기자의 순번을 원자적으로 매기고, "내 순번"을 트랜잭션 없이
 * 즉시 조회하기 위해 RDB 대신 Redis를 쓴다.
 */
@Repository
@RequiredArgsConstructor
public class WaitlistRankRepository {

    private final StringRedisTemplate redisTemplate;

    public void add(Long departureId, Long waitlistId) {
        redisTemplate.opsForZSet().add(key(departureId), waitlistId.toString(), System.currentTimeMillis());
    }

    public void remove(Long departureId, Long waitlistId) {
        redisTemplate.opsForZSet().remove(key(departureId), waitlistId.toString());
    }

    /**
     * 1부터 시작하는 순번을 반환한다. Redis Sorted Set에서 이미 제거된 경우(승격/만료 등) null.
     */
    public Long rank(Long departureId, Long waitlistId) {
        Long zeroBasedRank = redisTemplate.opsForZSet().rank(key(departureId), waitlistId.toString());
        return zeroBasedRank == null ? null : zeroBasedRank + 1;
    }

    private String key(Long departureId) {
        return "waitlist:departure:" + departureId;
    }
}
