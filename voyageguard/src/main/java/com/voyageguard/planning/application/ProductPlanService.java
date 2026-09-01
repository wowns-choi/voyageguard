package com.voyageguard.planning.application;

import com.voyageguard.planning.domain.productplan.ProductPlan;
import com.voyageguard.planning.domain.productplan.ProductPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductPlanService {
    private final ProductPlanRepository productPlanRepository;

    public Long create(String title) {
        ProductPlan plan = ProductPlan.create(title);
        return productPlanRepository.save(plan).getId();
    }

    public void requestReview(Long id) {
        ProductPlan plan = getPlan(id);
        plan.requestReview();
    }

    public void approve(Long id) {
        ProductPlan plan = getPlan(id);
        plan.approve();
    }

    public void reject(Long id, String reason) {
        ProductPlan plan = getPlan(id);
        plan.reject(reason);
    }

    public void revise(Long id, String newTitle) {
        ProductPlan plan = getPlan(id);
        plan.revise(newTitle);
    }

    private ProductPlan getPlan(Long id) {
        return productPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기획입니다. id=" + id));
    }

}
