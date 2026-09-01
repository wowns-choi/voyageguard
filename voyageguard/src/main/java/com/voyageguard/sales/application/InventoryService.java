package com.voyageguard.sales.application;

import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.sales.domain.inventory.Inventory;
import com.voyageguard.sales.domain.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final DepartureRepository departureRepository;

    public Long create(Long departureId) {
        Departure departure = departureRepository.findById(departureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다. id=" + departureId));

        Inventory inventory = Inventory.create(departureId, departure.getCapacity());
        return inventoryRepository.save(inventory).getId();
    }

    public void decrease(Long id, int quantity) {
        Inventory inventory = getInventoryForUpdate(id);
        inventory.decrease(quantity);
    }

    public void increase(Long id, int quantity) {
        Inventory inventory = getInventoryForUpdate(id);
        inventory.increase(quantity);
    }

    private Inventory getInventoryForUpdate(Long id) {
        return inventoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다. id=" + id));
    }

}
