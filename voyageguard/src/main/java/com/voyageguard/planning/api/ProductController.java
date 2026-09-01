package com.voyageguard.planning.api;

import com.voyageguard.planning.api.dto.ProductCreateRequest;
import com.voyageguard.planning.api.dto.ProductUpdateInfoRequest;
import com.voyageguard.planning.application.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 상태 전이 API (등록됨 -> 판매개시됨 -> 판매일시중지됨/재개됨 -> 판매종료됨 -> 폐기됨)")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록", description = "승인된(APPROVED) ProductPlan을 기반으로 상품을 등록됨 상태로 생성한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 기획")
    @ApiResponse(responseCode = "409", description = "기획이 APPROVED 상태가 아니어서 상품 등록 불가")
    @PostMapping
    public Long create(@RequestBody ProductCreateRequest request) {
        return productService.create(request.planId(), request.title(), request.description(),
                request.saleStartDate(), request.saleEndDate());
    }

    @Operation(summary = "상품 정보 수정", description = "등록됨/판매일시중지됨 상태의 상품 제목·설명을 수정한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "등록됨/판매일시중지됨 상태가 아니어서 수정 불가")
    @PutMapping("/{id}")
    public void updateInfo(@PathVariable Long id, @RequestBody ProductUpdateInfoRequest request) {
        productService.updateInfo(id, request.title(), request.description());
    }

    @Operation(summary = "판매 개시", description = "등록됨 상태의 상품을 판매개시됨으로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "등록됨 상태가 아니어서 판매개시 불가")
    @PostMapping("/{id}/start-sale")
    public void startSale(@PathVariable Long id) {
        productService.startSale(id);
    }

    @Operation(summary = "판매 일시중지", description = "판매개시됨 상태의 상품을 판매일시중지됨으로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "판매개시됨 상태가 아니어서 일시중지 불가")
    @PostMapping("/{id}/pause")
    public void pause(@PathVariable Long id) {
        productService.pause(id);
    }

    @Operation(summary = "판매 재개", description = "판매일시중지됨 상태의 상품을 판매개시됨으로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "판매일시중지됨 상태가 아니어서 재개 불가")
    @PostMapping("/{id}/resume")
    public void resume(@PathVariable Long id) {
        productService.resume(id);
    }

    @Operation(summary = "판매 종료", description = "판매개시됨/판매일시중지됨 상태의 상품을 판매종료됨으로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "판매개시됨/판매일시중지됨 상태가 아니어서 종료 불가")
    @PostMapping("/{id}/end-sale")
    public void endSale(@PathVariable Long id) {
        productService.endSale(id);
    }

    @Operation(summary = "상품 폐기", description = "판매종료됨 상태의 상품을 폐기됨으로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "판매종료됨 상태가 아니어서 폐기 불가")
    @PostMapping("/{id}/discard")
    public void discard(@PathVariable Long id) {
        productService.discard(id);
    }
}
