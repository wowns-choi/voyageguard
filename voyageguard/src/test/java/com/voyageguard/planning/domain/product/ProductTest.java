package com.voyageguard.planning.domain.product;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    private static final LocalDate SALE_START = LocalDate.of(2026, 3, 1);
    private static final LocalDate SALE_END = LocalDate.of(2026, 8, 31);

    private Product createProduct() {
        return Product.create(1L, "2026 여름 발리 패키지", "발리 5박 6일", SALE_START, SALE_END);
    }

    @Test
    void create_시_REGISTERED_상태로_생성된다() {
        Product product = createProduct();

        assertEquals(1L, product.getPlanId());
        assertEquals("2026 여름 발리 패키지", product.getTitle());
        assertEquals(ProductStatus.REGISTERED, product.getStatus());
    }

    @Test
    void REGISTERED_상태에서_startSale_하면_ON_SALE로_전이된다() {
        Product product = createProduct();

        product.startSale();

        assertEquals(ProductStatus.ON_SALE, product.getStatus());
    }

    @Test
    void REGISTERED가_아닌_상태에서_startSale_하면_예외가_발생한다() {
        Product product = createProduct();
        product.startSale();

        assertThrows(IllegalStateException.class, product::startSale);
    }

    @Test
    void ON_SALE_상태에서_pause_하면_PAUSED로_전이된다() {
        Product product = createProduct();
        product.startSale();

        product.pause();

        assertEquals(ProductStatus.PAUSED, product.getStatus());
    }

    @Test
    void ON_SALE가_아닌_상태에서_pause_하면_예외가_발생한다() {
        Product product = createProduct();

        assertThrows(IllegalStateException.class, product::pause);
    }

    @Test
    void PAUSED_상태에서_resume_하면_ON_SALE로_전이된다() {
        Product product = createProduct();
        product.startSale();
        product.pause();

        product.resume();

        assertEquals(ProductStatus.ON_SALE, product.getStatus());
    }

    @Test
    void PAUSED가_아닌_상태에서_resume_하면_예외가_발생한다() {
        Product product = createProduct();

        assertThrows(IllegalStateException.class, product::resume);
    }

    @Test
    void ON_SALE_상태에서_endSale_하면_ENDED로_전이된다() {
        Product product = createProduct();
        product.startSale();

        product.endSale();

        assertEquals(ProductStatus.ENDED, product.getStatus());
    }

    @Test
    void PAUSED_상태에서_endSale_하면_ENDED로_전이된다() {
        Product product = createProduct();
        product.startSale();
        product.pause();

        product.endSale();

        assertEquals(ProductStatus.ENDED, product.getStatus());
    }

    @Test
    void ON_SALE나_PAUSED가_아닌_상태에서_endSale_하면_예외가_발생한다() {
        Product product = createProduct();

        assertThrows(IllegalStateException.class, product::endSale);
    }

    @Test
    void ENDED_상태에서_discard_하면_DISCARDED로_전이된다() {
        Product product = createProduct();
        product.startSale();
        product.endSale();

        product.discard();

        assertEquals(ProductStatus.DISCARDED, product.getStatus());
    }

    @Test
    void ENDED가_아닌_상태에서_discard_하면_예외가_발생한다() {
        Product product = createProduct();

        assertThrows(IllegalStateException.class, product::discard);
    }

    @Test
    void REGISTERED_상태에서_updateInfo_하면_제목과_설명이_갱신된다() {
        Product product = createProduct();

        product.updateInfo("수정된 제목", "수정된 설명");

        assertEquals("수정된 제목", product.getTitle());
        assertEquals("수정된 설명", product.getDescription());
    }

    @Test
    void PAUSED_상태에서_updateInfo_하면_제목과_설명이_갱신된다() {
        Product product = createProduct();
        product.startSale();
        product.pause();

        product.updateInfo("수정된 제목", "수정된 설명");

        assertEquals("수정된 제목", product.getTitle());
    }

    @Test
    void ON_SALE_상태에서_updateInfo_하면_예외가_발생한다() {
        Product product = createProduct();
        product.startSale();

        assertThrows(IllegalStateException.class, () -> product.updateInfo("수정된 제목", "수정된 설명"));
    }

    @Test
    void saleEndDate가_지나지_않았으면_isSaleExpired는_false다() {
        Product product = Product.create(1L, "제목", "설명", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertFalse(product.isSaleExpired());
    }

    @Test
    void saleEndDate가_지났으면_isSaleExpired는_true다() {
        Product product = Product.create(1L, "제목", "설명", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));

        assertTrue(product.isSaleExpired());
    }

    @Test
    void ENDED가_아닌_상태에서_expire_하면_ENDED로_전이된다() {
        Product product = createProduct();
        product.startSale();

        product.expire();

        assertEquals(ProductStatus.ENDED, product.getStatus());
    }

    @Test
    void DISCARDED_상태에서_expire_해도_상태가_유지된다() {
        Product product = createProduct();
        product.startSale();
        product.endSale();
        product.discard();

        product.expire();

        assertEquals(ProductStatus.DISCARDED, product.getStatus());
    }
}
