package com.voyageguard.common.outbox;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도메인 상태 변경과 "이 이벤트를 나중에 Kafka로 발행하겠다"는 의도를 같은 DB 트랜잭션으로 묶기 위한 Outbox 패턴의 저장 단위.
 * 발행 자체(Kafka로 실제 전송)는 별도 릴레이가 이 테이블을 폴링해서 처리한다
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType; // 예: "ReservationCancelled", 로그 디버깅 필터링용

    private String topic; // 예: "reservation.cancelled"

    private String messageKey; // Kafka 파티션 키로 쓸 값 (예: departureId)

    @Lob
    private String payload; // JSON 문자열

    private boolean published;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    private OutboxEvent(String eventType, String topic, String messageKey, String payload) {
        this.eventType = eventType;
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.published = false;
        this.createdAt = LocalDateTime.now();
    }

    public static OutboxEvent create(String eventType, String topic, String messageKey, String payload) {
        return new OutboxEvent(eventType, topic, messageKey, payload);
    }

    public void markPublished() {
        if (published) {
            throw new IllegalStateException("이미 발행된 이벤트입니다. id=" + id);
        }
        this.published = true;
        this.publishedAt = LocalDateTime.now();
    }
}
