package com.voyageguard.sales.infrastructure.lock;

import com.voyageguard.sales.application.inventory.InventoryConcurrencyStrategy;
import com.voyageguard.sales.domain.inventory.Inventory;
import com.voyageguard.sales.domain.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** DB 비관적 락 기반 어댑터 */
@Component
@RequiredArgsConstructor
// inventory.lock-strategy=pessimistic(또는 미설정 시 기본값)일 때만 빈으로 등록
@ConditionalOnProperty(prefix = "inventory", name = "lock-strategy", havingValue = "pessimistic", matchIfMissing = true)
public class PessimisticLockInventoryStrategy implements InventoryConcurrencyStrategy {

    private final InventoryRepository inventoryRepository;

    // Departure id(회차 id) 로 잔여 자리 조회, 비관적 락을 건다.
    @Override
    public int getRemainingCount(Long departureId) {
        return findForUpdate(departureId).getRemainingCount();
    }

    @Override
    public void decrease(Long departureId, int quantity) {
        findForUpdate(departureId) // 비관적 락 걸고, Inventory 조회 후
                .decrease(quantity); // Inventory 감소시키기
    }

    @Override
    public void increase(Long departureId, int quantity) {
        findForUpdate(departureId) // 비관적 락 걸고, Inventory 조회 후
                .increase(quantity); // Inventory 증가시키기
    }

    /** 비관적 락 */
    private Inventory findForUpdate(Long departureId) {
        return inventoryRepository.findByDepartureIdForUpdate(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다. departureId=" + departureId));
    }
}
