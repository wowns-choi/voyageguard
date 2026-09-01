package com.voyageguard.planning.application;

import com.voyageguard.planning.domain.product.Product;
import com.voyageguard.planning.domain.product.ProductRepository;
import com.voyageguard.planning.domain.productplan.ProductPlan;
import com.voyageguard.planning.domain.productplan.ProductPlanRepository;
import com.voyageguard.planning.domain.productplan.ProductPlanStatus;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;

    public Long create(Long planId, String title, String description, LocalDate saleStartDate, LocalDate saleEndDate) {
        ProductPlan plan = productPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기획입니다. id=" + planId));
        if (plan.getStatus() != ProductPlanStatus.APPROVED) {
            throw new IllegalStateException("승인된 기획만 상품으로 등록할 수 있습니다. 현재 상태: " + plan.getStatus());
        }

        Product product = Product.create(planId, title, description, saleStartDate, saleEndDate);
        return productRepository.save(product).getId();
    }

    public void updateInfo(Long id, String title, String description) {
        Product product = getProduct(id);
        product.updateInfo(title, description);
    }

    public void startSale(Long id) {
        Product product = getProduct(id);
        product.startSale();
    }

    public void pause(Long id) {
        Product product = getProduct(id);
        product.pause();
    }

    public void resume(Long id) {
        Product product = getProduct(id);
        product.resume();
    }

    public void endSale(Long id) {
        Product product = getProduct(id);
        product.endSale();
    }

    public void discard(Long id) {
        Product product = getProduct(id);
        product.discard();
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + id));
    }
}
