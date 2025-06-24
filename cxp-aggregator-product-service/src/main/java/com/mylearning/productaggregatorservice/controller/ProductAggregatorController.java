package com.mylearning.productaggregatorservice.controller;

import com.mylearning.productaggregatorservice.dto.ApiResponse;
import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.service.ProductAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

    @Operation(summary = "Get all aggregated products", description = "Fetches the complete list of aggregated products")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ProductDto>>>> getAllProducts() {
        log.info("Request: all products");
        return productAggregatorService.getAllProducts()
                .collectList()
                .map(this::buildSuccess)
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info("Returned {} products", resp.getBody().getData().size()));
    }

    @Operation(summary = "Get product by ID", description = "Fetches a product's details by its ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid product ID",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductDto>>> getProductById(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {

        if (id == null || id.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Product ID must not be null or empty"));
        }

        log.info("Request: product details for id {}", id);

        return productAggregatorService.getProduct(id)
                .map(this::buildSuccess)
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info("Response for id {}: status={}", id, resp.getStatusCode()));
    }

    @Operation(summary = "Get product price by ID", description = "Fetches the price of a product by its ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Price fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid product ID",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}/price")
    public Mono<ResponseEntity<ApiResponse<Double>>> getPriceById(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {

        if (id == null || id.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Product ID must not be null or empty"));
        }

        log.info("Request: price for id {}", id);

        return productAggregatorService.getProductPrice(id)
                .map(this::buildSuccess)
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info("Price response for id {}: status={}", id, resp.getStatusCode()));
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
