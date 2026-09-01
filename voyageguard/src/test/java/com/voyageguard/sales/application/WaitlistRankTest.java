package com.voyageguard.sales.application;

import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 실제 Redis(Sorted Set)를 사용해 대기 등록 순서대로 순번이 매겨지는지 검증한다.
 * 로컬 인프라(docker compose up -d)가 필요해 @Tag("integration")으로 분리.
 */
@Tag("integration")
@SpringBootTest
class WaitlistRankTest {

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private DepartureRepository departureRepository;

    @Test
    void 여러_명이_순서대로_대기_등록하면_등록한_순서대로_순번이_매겨진다() {
        Departure departure = departureRepository.save(Departure.create(1L, LocalDate.of(2026, 12, 20), 10, 30,
                "발리 5박 6일 일정", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 10), 1500000));

        Long first = waitlistService.join(departure.getId(), 2, "첫번째");
        Long second = waitlistService.join(departure.getId(), 1, "두번째");
        Long third = waitlistService.join(departure.getId(), 3, "세번째");

        assertEquals(1L, waitlistService.rank(first));
        assertEquals(2L, waitlistService.rank(second));
        assertEquals(3L, waitlistService.rank(third));
    }
}
