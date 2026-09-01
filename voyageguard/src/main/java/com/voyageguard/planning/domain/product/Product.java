package com.voyageguard.planning.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품(Product)의 상태 전이(등록됨 -> 판매개시됨 -> 판매일시중지됨/재개됨 -> 판매종료됨 -> 폐기됨)를 캡슐화한 Aggregate Root.
 * ProductPlan이 APPROVED 상태일 때만 생성된다.
 * 외부에서 상태를 직접 바꿀 수 없고, 각 전이 메서드가 현재 상태를 검증한 뒤에만 상태를 변경한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ProductPlan을 객체(@ManyToOne)가 아닌 ID로 참조한다 (Reference Other Aggregates by Identity Only).
     * 객체 참조는 cascade·지연로딩을 통해 두 Aggregate를 같은 트랜잭션에 묶을 여지를 만들어,
     * Aggregate를 분리한 이유(같은 트랜잭션에 묶이지 않아도 됨)를 무효화하기 때문.
     */
    private Long planId;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private LocalDate saleStartDate;

    private LocalDate saleEndDate;

    private Product(Long planId, String title, String description, LocalDate saleStartDate, LocalDate saleEndDate) {
        this.planId = planId;
        this.title = title;
        this.description = description;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.status = ProductStatus.REGISTERED;
    }

    public static Product create(Long planId, String title, String description, LocalDate saleStartDate, LocalDate saleEndDate) {
        return new Product(planId, title, description, saleStartDate, saleEndDate);
    }

    public void updateInfo(String title, String description) {
        if (status != ProductStatus.REGISTERED && status != ProductStatus.PAUSED) {
            throw new IllegalStateException("등록됨 또는 판매일시중지됨 상태에서만 상품 정보를 수정할 수 있습니다. 현재 상태: " + status);
        }
        this.title = title;
        this.description = description;
    }

    public void startSale() {
        if (status != ProductStatus.REGISTERED) {
            throw new IllegalStateException("등록됨 상태에서만 판매를 개시할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductStatus.ON_SALE;
    }

    public void pause() {
        if (status != ProductStatus.ON_SALE) {
            throw new IllegalStateException("판매개시됨 상태에서만 일시중지할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductStatus.PAUSED;
    }

    public void resume() {
        if (status != ProductStatus.PAUSED) {
            throw new IllegalStateException("판매일시중지됨 상태에서만 재개할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductStatus.ON_SALE;
    }

    public void endSale() {
        if (status != ProductStatus.ON_SALE && status != ProductStatus.PAUSED) {
            throw new IllegalStateException("판매개시됨 또는 판매일시중지됨 상태에서만 판매를 종료할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductStatus.ENDED;
    }

    public void discard() {
        if (status != ProductStatus.ENDED) {
            throw new IllegalStateException("판매종료됨 상태에서만 폐기할 수 있습니다. 현재 상태: " + status);
        }
        this.status = ProductStatus.DISCARDED;
    }

    public boolean isSaleExpired() {
        return LocalDate.now().isAfter(saleEndDate);
    }

    public void expire() {
        if (status == ProductStatus.ENDED || status == ProductStatus.DISCARDED) {
            return;
        }
        this.status = ProductStatus.ENDED;
    }
}
