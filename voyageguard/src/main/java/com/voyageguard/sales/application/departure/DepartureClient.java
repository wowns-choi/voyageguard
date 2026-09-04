package com.voyageguard.sales.application.departure;

/**
 * Planning의 Departure 조회 포트(port).
 * MSA 분리 대비 - Sales는 Planning의 DepartureRepository를 직접 참조하지 않고 이 인터페이스로만
 * 의존한다. 지금은 동기 REST 어댑터(PlanningDepartureClient)뿐이지만, 실제 분리 후에도 이
 * 인터페이스 자체는 안 바뀌고 어댑터 내부 구현(호출 주소 등)만 바뀌면 된다.
 */
public interface DepartureClient {

    DepartureView get(Long departureId);
}
