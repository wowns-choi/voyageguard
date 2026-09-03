package com.voyageguard.common.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * OutboxEvent 테이블을 주기적으로 폴링해서, 아직 발행 안 된 행을 실제로 Kafka에 발행하는 릴레이.
 * 이벤트 하나가 발행 실패해도(카프카 장애 등) 나머지 이벤트 처리에 영향 없게 개별적으로 처리하고,
 * 실패한 건 published=false로 남겨둬서 다음 폴링 때 자동 재시도된다.
 *
 * 폴링 방식이라 지연(최대 fixedDelay만큼)과 불필요한 조회가 있지만, "이벤트 유실 방지"라는
 * 정합성 목표는 이것만으로 완전히 달성된다 - 지연/효율 개선은 별도 단계(Debezium CDC)의 몫.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void relay() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByPublishedFalse();
        for (OutboxEvent event : pendingEvents) {
            try {
                // .get() 을 붙여, 실제로 Kafka에 보내고, 성공 확인까지 기다림
                // kafkaTemplate.send()는 원래 비동기(즉시 반환)라서 .get() 없이 바로 markPublished()를
                // 호출하면 Kafka에 진짜 도착했는지 확인도 안 하고 발행완료 표시를 해버리게 됨
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
                        .get();
                event.markPublished();
            } catch (Exception e) {
                log.warn("OutboxEvent 발행 실패, 다음 폴링에서 재시도. id={}, topic={}", event.getId(), event.getTopic(), e);
            }
        }
    }
}
