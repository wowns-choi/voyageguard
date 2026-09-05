package com.voyageguard.sales.application.departure;

import java.time.LocalDate;

/**
 * Sales가 Planning의 Departure에 대해 알아야 하는 정보만 담은 뷰.
 * Planning의 DepartureStatus를 그대로 안 쓰고 여기 별도 enum으로 번역하는 이유: MSA로 실제
 * 분리되면 Sales는 Planning의 도메인 타입을 아예 import할 수 없어지므로(다른 레포가 되니까),
 * 지금부터 컴파일 의존성 자체를 없애두기 위함.
 */
public record DepartureView(Status status, Integer capacity, LocalDate saleEndDate) {

    public enum Status { OPEN, CLOSED, CANCELLED }
}
