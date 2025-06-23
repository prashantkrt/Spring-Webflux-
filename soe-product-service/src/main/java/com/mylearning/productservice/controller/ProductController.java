package com.mylearning.productservice.controller;

import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.service.ProductService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}/details")
    public Mono<ResponseEntity<ProductDto>> getProductDetails(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {
        return productService.getProductDetails(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Flux<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}/price")
    public Mono<ResponseEntity<Double>> getPrice(
            @PathVariable @NotBlank(message = "Product ID must not be blank") String id) {
        return productService.getProductPrice(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
