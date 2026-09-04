package com.voyageguard.payment.application.reservation;

/**
 * Payment가 Sales의 Reservation에 대해 알아야 하는 정보만 담은 뷰.
 * Sales의 ReservationStatus를 그대로 안 쓰고 Payment 자체 enum으로 번역한다 - 이유는
 * sales.application.departure.DepartureView와 동일(MSA 분리 대비 컴파일 의존성 제거).
 */
public record ReservationView(Long id, Status status) {

    public enum Status { REQUESTED, CONFIRMED, CANCELLED, EXPIRED }
}
