package com.voyageguard.sales.domain.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회차(Departure)의 재고를 관리하는 Aggregate Root.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Departure 를 객체가 아닌 ID로 참조한다 (Reference Other Aggregates by Identity Only)
    private Long departureId;

    private Integer totalCapacity;

    private Integer remainingCount;

    private Inventory(Long departureId, Integer totalCapacity) {
        this.departureId = departureId;
        this.totalCapacity = totalCapacity;
        this.remainingCount = totalCapacity;
    }

    public static Inventory create(Long departureId, Integer totalCapacity) {
        return new Inventory(departureId, totalCapacity);
    }

    public void decrease(int quantity) {
        if (remainingCount < quantity) {
            throw new InsufficientInventoryException("잔여 재고가 부족합니다. 잔여: " + remainingCount + ", 요청: " +
                    quantity);
        }
        this.remainingCount -= quantity;
    }

    public void increase(int quantity) {
        this.remainingCount += quantity;
    }

}
