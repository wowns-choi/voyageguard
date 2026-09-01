package com.voyageguard.planning.application;

import com.voyageguard.planning.domain.departure.Departure;
import com.voyageguard.planning.domain.departure.DepartureRepository;
import com.voyageguard.planning.domain.product.Product;
import com.voyageguard.planning.domain.product.ProductRepository;
import com.voyageguard.planning.domain.product.ProductStatus;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartureService {
    private final DepartureRepository departureRepository;
    private final ProductRepository productRepository;

    public Long create(Long productId, LocalDate departureDate, Integer minParticipants, Integer capacity,
                        String itinerary, LocalDate saleStartDate, LocalDate saleEndDate, Integer salePrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));
        if (product.getStatus() == ProductStatus.ENDED || product.getStatus() == ProductStatus.DISCARDED) {
            throw new IllegalStateException("판매종료 또는 폐기된 상품에는 회차를 추가할 수 없습니다. 현재 상태: " + product.getStatus());
        }

        Departure departure = Departure.create(productId, departureDate, minParticipants, capacity,
                itinerary, saleStartDate, saleEndDate, salePrice);
        return departureRepository.save(departure).getId();
    }

    public void close(Long id) {
        Departure departure = getDeparture(id);
        departure.close();
    }

    public void cancel(Long id) {
        Departure departure = getDeparture(id);
        departure.cancel();
    }

    private Departure getDeparture(Long id) {
        return departureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다. id=" + id));
    }
}
