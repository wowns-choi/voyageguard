/**
 * Inventory 재고 차감 동시성 부하테스트.
 *
 * 시나리오: 재고 CAPACITY(10)석짜리 회차에 VUS(100)명이 동시에 1석씩 예약 요청 ->
 * 정확히 CAPACITY명만 성공하고 나머지는 재고부족(409, code=INSUFFICIENT_INVENTORY)으로 막혀야 함
 * (초과판매 없음).
 *
 * inventory.lock-strategy(pessimistic/optimistic/redis-decr)를 바꿔가며 앱을 재기동한 뒤
 * 이 스크립트를 각각 실행해서 결과를 비교한다 - 전략 전환이 Spring 기동 시점(@ConditionalOnProperty)에
 * 결정되므로 이 스크립트 자체는 전략을 모르고, 재기동 사이 수동으로 바꿔가며 실행해야 한다.
 *
 * 실행: k6 run loadtest/inventory-decrease.js
 */
import http from 'k6/http'; // http 통신 내장 모듈
import { check } from 'k6'; // "이 응답이 내가 기대한 조건을 만족하나?"를 검사해서 통과/실패를 집계해주는 도구.
import { Counter } from 'k6/metrics'; // 우리가 원하는 커스텀 지표(숫자 세기)를 직접 만들 때 씀

const BASE_URL = 'http://localhost:8080/api/v1';
const CAPACITY = 10;
const VUS = 100;
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

const successCount = new Counter('reservation_success');
const insufficientCount = new Counter('reservation_insufficient_inventory');
const otherErrorCount = new Counter('reservation_other_error');

// options.scenarios : 어떻게 부하를 만들어낼지 설정하는 부분입니다. 
export const options = {
    scenarios: {
        burst: {
            executor: 'per-vu-iterations', // "가상 사용자(VU) 각각한테 정해진 반복 횟수를 준다"는 실행 방식.
            vus: VUS, // Virtual UserS(가상 사용자) 100명 설정
            iterations: 1, // 각 VU 가 딱 1번씩만 호출. 즉, 100명 한꺼번에 만들어서 각자 딱 한 번씩 요청을 동시에 쏘게함.
            maxDuration: '30s', // 최대 30초간 기다렸다가 강제종료 
        },
    },
};

/** K6 의 실행흐름은 다음과 같이 3단계로 나뉘어져 있습니다. 
 *  - setup 딱 한번 
 *  - 메인함수(VU 수만큼 반복)
 *  - teardown() 딱 한번 
 */
export function setup() {
    const today = toDateString(new Date());
    const saleEndDate = toDateString(addDays(new Date(), 30));
    const departureDate = toDateString(addDays(new Date(), 60));

    // 1. ProductPlan 생성 -> 검수요청 -> 승인
    const planId = post('/product-plans', { title: 'k6 부하테스트 상품' });
    post(`/product-plans/${planId}/request-review`);
    post(`/product-plans/${planId}/approve`);

    // 2. Product 등록 (planId가 APPROVED 상태여야 함)
    const productId = post('/products', {
        planId,
        title: 'k6 부하테스트 상품',
        description: '재고 동시성 부하테스트용 - k6로 생성됨',
        saleStartDate: today,
        saleEndDate,
    });

    // 3. Departure 등록 (capacity=CAPACITY) -> DepartureService.create() 가 같은 트랜잭션에서
    //    Inventory(DB row + Redis 키)까지 자동 생성함 - 별도 초기화 호출 불필요.
    const departureId = post('/departures', {
        productId,
        departureDate,
        minParticipants: 1,
        capacity: CAPACITY,
        itinerary: 'k6 부하테스트용 일정',
        saleStartDate: today,
        saleEndDate,
        salePrice: 1000000,
    });

    return { departureId };
}

// 메인 함수. 각 VU 가 동시에 실행하는 코드임. 
export default function (data) {
    // 예약하기
    const res = http.post(
        `${BASE_URL}/reservations`,
        JSON.stringify({
            departureId: data.departureId,
            headcount: 1,
            travelerName: `k6-vu-${__VU}`,
        }),
        JSON_HEADERS
    );

    check(res, { '200 또는 409': (r) => r.status === 200 || r.status === 409 });
    
    // 성공 카운트
    if (res.status === 200) {
        successCount.add(1);
        return;
    }

    if (res.status === 409) {
        const body = res.json();
        // BusinessException(InsufficientInventoryException)만 code 필드를 가짐(GlobalExceptionHandler 참고) -
        // 같은 409라도 IllegalStateException(예: 모집중 아님) 등 다른 원인과 구분하기 위함.
        if (body.code === 'INSUFFICIENT_INVENTORY') {
            insufficientCount.add(1); // 재고 부족
        } else {
            otherErrorCount.add(1);
        }
        return;
    }

    // res.status 가 200 도 아니고, 409 도 아닌 경우
    // ex) 낙관적 락 재시도(5회) 모두 실패하면 DataAccessException -> 500으로 떨어짐 - 이것도 여기서 잡힘.
    otherErrorCount.add(1);
}

export function teardown(data) {
    console.log(`[결과 확인용] departureId=${data.departureId}, 설정 재고=${CAPACITY}, 요청 수=${VUS}`);
}

/// --- util(helper) method ---
function post(path, body) {
    const res = http.post(`${BASE_URL}${path}`, body ? JSON.stringify(body) : null, JSON_HEADERS);
    if (res.status !== 200) {
        throw new Error(`setup 실패: POST ${path} -> ${res.status} ${res.body}`);
    }
    return res.body ? JSON.parse(res.body) : null;
}
function toDateString(date) {
    return date.toISOString().slice(0, 10);
}
function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}
