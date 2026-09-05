package com.voyageguard.planning.application.inventory;

/**
 * Sales의 잔여 재고 조회 포트(port).
 * MSA 분리 대비 - Planning은 Sales의 InventoryConcurrencyStrategy를 직접 참조하지 않고 이
 * 인터페이스로만 의존한다.
 */
public interface InventoryClient {

    int getRemainingCount(Long departureId);
}
