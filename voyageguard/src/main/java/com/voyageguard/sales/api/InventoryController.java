package com.voyageguard.sales.api;

import com.voyageguard.sales.api.dto.InventoryRemainingResponse;
import com.voyageguard.sales.application.inventory.InventoryConcurrencyStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 잔여 재고 조회 전용 API. MSA 대비 1단계 - Planning이 회차 목록/상세 조회 시 잔여재고를
 * DB로 직접 안 읽고 이 API를 동기 REST로 호출하도록 하기 위해 신설.
 */
@Tag(name = "Inventory", description = "잔여 재고 조회 API")
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryConcurrencyStrategy inventoryConcurrencyStrategy;

    @Operation(summary = "잔여 재고 조회", description = "회차의 현재 잔여 재고를 조회한다(활성화된 동시성 전략 기준).")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 재고")
    @GetMapping("/{departureId}")
    public InventoryRemainingResponse getRemaining(@PathVariable Long departureId) {
        return new InventoryRemainingResponse(departureId, inventoryConcurrencyStrategy.getRemainingCount(departureId));
    }
}
