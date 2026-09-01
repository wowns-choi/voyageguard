package com.voyageguard.planning.api;

import com.voyageguard.planning.application.ProductPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product-plans")
@RequiredArgsConstructor
public class ProductPlanController {

    private final ProductPlanService productPlanService;

    @PostMapping
    public Long create(@RequestBody CreateRequest request) {
        return productPlanService.create(request.title());
    }

    @PostMapping("/{id}/request-review")
    public void requestReview(@PathVariable Long id) {
        productPlanService.requestReview(id);
    }

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        productPlanService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id, @RequestBody RejectRequest request) {
        productPlanService.reject(id, request.reason());
    }

    @PostMapping("/{id}/revise")
    public void revise(@PathVariable Long id, @RequestBody ReviseRequest request) {
        productPlanService.revise(id, request.newTitle());
    }

    public record CreateRequest(String title) {
    }

    public record RejectRequest(String reason) {
    }

    public record ReviseRequest(String newTitle) {
    }
}
