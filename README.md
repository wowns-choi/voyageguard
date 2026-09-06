# voyageguard

여행 패키지 예약 시스템을 소재로, DDD(Domain-Driven Design)와 모듈러 모놀리스 → MSA 추출 과정을
실전 적용해본 포트폴리오 프로젝트입니다. 이벤트 스토밍으로 Bounded Context/Aggregate 경계를
도출하고, 그 경계를 코드 레벨(패키지) → DB 레벨(물리 분리) → 레포/서비스 레벨(물리 분리) 순으로
단계적으로 강제해나갔습니다.

## 저장소 구성

이 레포(`voyageguard`)는 **허브 레포**입니다 - 4개의 독립 서비스 레포에 대한 설명과 로컬 개발
인프라(Docker Compose)만 갖고 있고, 실제 백엔드 코드는 각 서비스 레포에 있습니다.

| 서비스 | 레포 | 담당 Aggregate | 포트 |
|---|---|---|---|
| Planning | [voyageguard-planning](https://github.com/wowns-choi/voyageguard-planning) | ProductPlan / Product / Departure | 8081 |
| Sales | [voyageguard-sales](https://github.com/wowns-choi/voyageguard-sales) | Inventory / Reservation / Waitlist | 8082 |
| Payment | [voyageguard-payment](https://github.com/wowns-choi/voyageguard-payment) | Payment | 8083 |
| Auth | [voyageguard-auth](https://github.com/wowns-choi/voyageguard-auth) | Member | 8084 |

원래 하나의 Spring Boot 모놀리스(패키지로만 BC를 분리)로 시작해서, BC 간 결합점을 REST/Kafka로
끊고 → DB를 서비스별로 물리 분리하고 → 마지막으로 레포 자체를 분리하는 순서로 진행했습니다.
각 단계를 먼저 검증 가능한 범위(같은 프로세스, 같은 레포)에서 끝내고 다음 단계로 넘어가서,
문제가 생겼을 때 원인이 항상 "방금 바꾼 변수 하나"로 좁혀지게 했습니다.

## BC 간 통신

- **동기 REST**: 호출자가 결과를 그 자리에서 기다렸다가 성공/실패를 결정해야 하는 경우
  (예: 예약 요청 시 회차 OPEN 여부 확인, 결제 요청 시 예약 상태/소유자 확인)
- **비동기 Kafka(Transactional Outbox)**: 이벤트 A가 이벤트 B를 트리거하되 즉시 기다릴 필요는
  없는 경우 (예: 결제 승인 → 예약 확정, Choreography Saga)
- 서비스가 각자 자신의 DB에 도메인 상태 변경과 Outbox 이벤트를 같은 트랜잭션으로 기록하고,
  별도 릴레이가 폴링하며 Kafka로 발행 - DB 커밋과 메시지 발행 사이의 유실 구간을 없앱니다.

## 로컬 실행

```bash
# 1. 인프라 기동 (MySQL x4 + Redis + Kafka)
docker compose up -d

# 2. 각 서비스 레포를 clone 후 개별 실행
cd voyageguard-planning && ./gradlew bootRun   # :8081
cd voyageguard-sales && ./gradlew bootRun      # :8082
cd voyageguard-payment && ./gradlew bootRun    # :8083
cd voyageguard-auth && ./gradlew bootRun       # :8084
```

프론트엔드(`voyageguard-front/`)는 손님용 예약 플로우(회차 목록 조회 + 예약하기) 1차 슬라이스만
구현되어 있습니다.

## 부하테스트

`loadtest/`에 Sales BC의 재고 동시성 제어 3가지 전략(비관적 락 / 낙관적 락 / Redis DECR)을
k6로 비교한 스크립트와 결과가 있습니다.
