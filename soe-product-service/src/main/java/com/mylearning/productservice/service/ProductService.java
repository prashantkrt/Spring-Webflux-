package com.mylearning.productservice.service;

import com.mylearning.productservice.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final WebClient aggregatorWebClient;

    /**
     * Calls aggregator to get product details (id + name + price).
     */
    public Mono<ProductDto> getProductDetails(String id) {
        log.info("Fetching product details for id {}", id);
        return aggregatorWebClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .doOnSuccess(product -> log.info("Product retrieved: {}", product))
                .doOnError(ex -> log.error("Error fetching product {}: {}", id, ex.getMessage()));
    }

    /**
     * Calls aggregator to get all products.
     */
    public Flux<ProductDto> getAllProducts() {
        log.info("Fetching all products");
        return aggregatorWebClient.get()
                .uri("")
                .retrieve()
                .bodyToFlux(ProductDto.class)
                .doOnComplete(() -> log.info("All products fetched"));
    }

    /**
     * Calls aggregator to get only product price by ID.
     */
    public Mono<Double> getProductPrice(String id) {
        log.info("Fetching price for product id {}", id);
        return aggregatorWebClient.get()
                .uri("/{id}/price", id)
                .retrieve()
                .bodyToMono(Double.class)
                .doOnSuccess(price -> log.info("Price for id {} is {}", id, price))
                .doOnError(ex -> log.error("Error fetching price for id {}: {}", id, ex.getMessage()));
    }
}
