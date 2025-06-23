package com.mylearning.productservice.controller;

import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.service.ProductService;
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

    @GetMapping("/{id}/details")
    public Mono<ApiResponse<ProductDto>> getProductDetails(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id,
            ServerWebExchange exchange) {

        return productService.getProductDetails(id)
                .map(product -> buildSuccess(exchange, "Product fetched", product));
    }

    @GetMapping
    public Mono<ApiResponse<List<ProductDto>>> getAllProducts(ServerWebExchange exchange) {
        return productService.getAllProducts()
                .collectList()
                .map(products -> buildSuccess(exchange, "All products fetched", products));
    }

    @GetMapping("/{id}/price")
    public Mono<ApiResponse<Double>> getPrice(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id,
            ServerWebExchange exchange) {

        return productService.getProductPrice(id)
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
