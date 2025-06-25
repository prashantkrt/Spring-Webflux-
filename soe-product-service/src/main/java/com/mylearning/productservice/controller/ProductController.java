package com.mylearning.productservice.controller;

import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.service.ProductService;
import com.mylearning.productservice.wrapper.ApiErrorResponse;
import com.mylearning.productservice.wrapper.ApiPriceResponse;
import com.mylearning.productservice.wrapper.ApiProductListResponse;
import com.mylearning.productservice.wrapper.ApiProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get product details by ID",
            description = "Returns full product information for the given ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product details fetched",
            content = @Content(schema = @Schema(implementation = ApiProductResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid product ID or validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected internal error or aggregator failure",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}/details")
    public Mono<ApiResponse<ProductDto>> getProductDetails(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id,
            ServerWebExchange exchange) {

        log.info("Received request to fetch product details for ID: {}", id);

        return productService.getProductDetails(id)
                .doOnNext(product -> log.info("Fetched product: {}", product))
                .map(product -> buildSuccess(exchange, "Product fetched", product));
    }

    @Operation(
            summary = "Get all products",
            description = "Returns the complete product catalogue"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product list fetched",
            content = @Content(schema = @Schema(implementation = ApiProductListResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No products available",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected internal error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )

    @GetMapping
    public Mono<ApiResponse<List<ProductDto>>> getAllProducts(ServerWebExchange exchange) {
        log.info("Received request to fetch all products");

        return productService.getAllProducts()
                .doOnNext(product -> log.debug("Product: {}", product))
                .collectList()
                .doOnNext(list -> log.info("Fetched {} products", list.size()))
                .map(products -> buildSuccess(exchange, "All products fetched", products));
    }

    @Operation(
            summary = "Get product price by ID",
            description = "Returns only the price for the given product ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Price fetched",
            content = @Content(schema = @Schema(implementation = ApiPriceResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid product ID or validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product/price not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected internal error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )

    @GetMapping("/{id}/price")
    public Mono<ApiResponse<Double>> getPrice(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id,
            ServerWebExchange exchange) {

        log.info("Received request to fetch price for product ID: {}", id);

        return productService.getProductPrice(id)
                .doOnNext(price -> log.info("Fetched price for ID {}: {}", id, price))
                .map(price -> buildSuccess(exchange, "Price fetched successfully", price));
    }

    private <T> ApiResponse<T> buildSuccess(ServerWebExchange exchange, String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .path(exchange.getRequest().getPath().value())
                .message(message)
                .data(data)
                .build();
    }
}
