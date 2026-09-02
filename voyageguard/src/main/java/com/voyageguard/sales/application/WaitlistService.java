package com.voyageguard.sales.application;

import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.planning.domain.departure.DepartureStatus;
import com.voyageguard.sales.domain.waitlist.Waitlist;
import com.voyageguard.sales.domain.waitlist.WaitlistRepository;
import com.voyageguard.sales.infrastructure.redis.WaitlistRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final DepartureRepository departureRepository;
    private final WaitlistRankRepository waitlistRankRepository;

    public Long join(Long departureId, Integer headcount, String travelerName) {
        Departure departure = departureRepository.findById(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다. id=" + departureId));
        if (departure.getStatus() != DepartureStatus.OPEN) {
            throw new IllegalStateException("모집중 상태의 회차만 대기 등록할 수 있습니다. 현재 상태: " + departure.getStatus());
        }

        Waitlist waitlist = Waitlist.create(departureId, headcount, travelerName);
        Long id = waitlistRepository.save(waitlist).getId();
        try {
            waitlistRankRepository.add(departureId, id);
        } catch (DataAccessException e) {
            waitlistRepository.deleteById(id);
            throw e;
        }

        return id;
    }

    @Transactional(readOnly = true)
    public Long rank(Long id) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대기열입니다. id=" + id));
        return waitlistRankRepository.rank(waitlist.getDepartureId(), id);
    }
}
