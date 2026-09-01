package com.voyageguard.planning.api;

import com.voyageguard.planning.api.dto.ProductPlanCreateRequest;
import com.voyageguard.planning.api.dto.ProductPlanRejectRequest;
import com.voyageguard.planning.api.dto.ProductPlanReviseRequest;
import com.voyageguard.planning.application.ProductPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ProductPlan", description = "상품 기획 상태 전이 API (DRAFT -> PENDING_REVIEW -> APPROVED/REJECTED)")
@RestController
@RequestMapping("/api/v1/product-plans")
@RequiredArgsConstructor
public class ProductPlanController {

    private final ProductPlanService productPlanService;

    @Operation(summary = "상품 기획 생성", description = "DRAFT 상태의 ProductPlan을 새로 생성한다.")
    @PostMapping
    public Long create(@RequestBody ProductPlanCreateRequest request) {
        return productPlanService.create(request.title());
    }

    @Operation(summary = "검수 요청", description = "DRAFT 상태의 기획을 PENDING_REVIEW로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 기획")
    @ApiResponse(responseCode = "409", description = "DRAFT 상태가 아니어서 검수 요청 불가")
    @PostMapping("/{id}/request-review")
    public void requestReview(@PathVariable Long id) {
        productPlanService.requestReview(id);
    }

    @Operation(summary = "검수 승인", description = "PENDING_REVIEW 상태의 기획을 APPROVED로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 기획")
    @ApiResponse(responseCode = "409", description = "PENDING_REVIEW 상태가 아니어서 승인 불가")
    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        productPlanService.approve(id);
    }

    @Operation(summary = "검수 반려", description = "PENDING_REVIEW 상태의 기획을 사유와 함께 REJECTED로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 기획")
    @ApiResponse(responseCode = "409", description = "PENDING_REVIEW 상태가 아니어서 반려 불가")
    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id, @RequestBody ProductPlanRejectRequest request) {
        productPlanService.reject(id, request.reason());
    }

    @Operation(summary = "반려 후 재수정", description = "REJECTED 상태의 기획 제목을 수정하고 다시 DRAFT로 되돌린다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 기획")
    @ApiResponse(responseCode = "409", description = "REJECTED 상태가 아니어서 재수정 불가")
    @PostMapping("/{id}/revise")
    public void revise(@PathVariable Long id, @RequestBody ProductPlanReviseRequest request) {
        productPlanService.revise(id, request.newTitle());
    }
}
