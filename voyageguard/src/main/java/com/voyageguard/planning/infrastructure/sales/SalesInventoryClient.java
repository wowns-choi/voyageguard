package com.voyageguard.planning.infrastructure.sales;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voyageguard.planning.application.inventory.InventoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** InventoryClient의 REST 어댑터 - Sales가 노출하는 GET /api/v1/inventories/{departureId}를 호출한다. */
@Component
public class SalesInventoryClient implements InventoryClient {

    private final RestClient restClient;

    public SalesInventoryClient(@Value("${sales.service.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public int getRemainingCount(Long departureId) {
        try {
            InventoryRemainingResponse response = restClient.get()
                    .uri("/api/v1/inventories/{departureId}", departureId)
                    .retrieve()
                    .body(InventoryRemainingResponse.class);
            return response.remainingCount();
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("존재하지 않는 재고입니다. departureId=" + departureId);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record InventoryRemainingResponse(Integer remainingCount) {
    }
}
