package com.mylearning.productaggregatorservice.controller;

import com.mylearning.productaggregatorservice.dto.ApiResponse;
import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.service.ProductAggregatorService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/aggregator/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductAggregatorController {

    private final ProductAggregatorService productAggregatorService;

    /** GET /api/aggregator/products – every product (wrapped) */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ProductDto>>>> getAllProducts() {
        log.info("Request: all products");

        return productAggregatorService.getAllProducts()
                .collectList()                          // wrap Flux into a single list
                .map(this::buildSuccess)
                .map(ResponseEntity::ok)                // 200 OK with body
                .doOnSuccess(resp -> log.info("Returned {} products",
                        resp.getBody().getData().size()));
    }

    /** GET /api/aggregator/products/{id} – product details */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductDto>>> getProductById(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {

        log.info("Request: product details for id {}", id);

        return productAggregatorService.getProduct(id)
                .map(this::buildSuccess)
                .map(ResponseEntity::ok)                // 200 OK
                .doOnSuccess(resp -> log.info("Response for id {}: status={}",
                        id, resp.getStatusCode()));
        /* Any error (e.g., DownstreamException) will bubble to GlobalExceptionHandler,
           which will return ApiResponse with errors populated. */
    }

    /** GET /api/aggregator/products/{id}/price – price only */
    @GetMapping("/{id}/price")
    public Mono<ResponseEntity<ApiResponse<Double>>> getPriceById(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {

        log.info("Request: price for id {}", id);

        return productAggregatorService.getProductPrice(id)
                .map(this::buildSuccess)
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info("Price response for id {}: status={}",
                        id, resp.getStatusCode()));
    }


    private <T> ApiResponse<T> buildSuccess(T data) {
        return ApiResponse.<T>builder()
                .apiSuccess(true)
                .timeStamp(Instant.now())
                .data(data)
                .errors(null)
                .build();
    }

}
