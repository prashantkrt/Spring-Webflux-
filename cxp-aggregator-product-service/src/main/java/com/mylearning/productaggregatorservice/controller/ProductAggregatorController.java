package com.mylearning.productaggregatorservice.controller;

import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.service.ProductAggregatorService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/aggregator/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductAggregatorController {

    private final ProductAggregatorService productAggregatorService;

    /** GET /api/aggregator/products  – return every product. */
    @GetMapping
    public Flux<ProductDto> getAllProducts() {
        log.info("Request: all products");
        return productAggregatorService.fetchAllProducts()
                .doOnComplete(() -> log.info("Returned all products"));
    }

    /** GET /api/aggregator/products/{id} – product details (with price) */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductDto>> getProductById(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {

        log.info("Request: product details for id {}", id);

        return productAggregatorService.fetchProductById(id)
                .map(ResponseEntity::ok)                                     // 200 with body
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))  // 404 if absent
                .doOnSuccess(resp -> log.info("Response for id {}: status={}",
                        id, resp.getStatusCode()));
    }

    /** GET /api/aggregator/products/{id}/price – price only */
    @GetMapping("/{id}/price")
    public Mono<ResponseEntity<Double>> getPriceById(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {

        log.info("Request: price for id {}", id);

        return productAggregatorService.fetchPriceById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnSuccess(resp -> log.info("Price response for id {}: status={}",
                        id, resp.getStatusCode()));
    }
}