package com.mylearning.productservice.controller;

import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.service.ProductService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductGraphQLController {

    private final ProductService productService;

    @QueryMapping
    public Mono<ApiResponse<ProductDto>> getProductDetails(@Argument @NotBlank String id) {
        log.info("GraphQL product({})", id);
        return productService.getProductDetails(id)
                .map(p -> buildSuccess("/graphql?query=product", "Product fetched", p));
    }


    @QueryMapping
    public Mono<ApiResponse<List<ProductDto>>> getAllProducts() {
        log.info("GraphQL products()");
        return productService.getAllProducts()
                .collectList()
                .map(list -> buildSuccess("/graphql?query=products", "All products fetched", list));
    }


    @QueryMapping
    public Mono<ApiResponse<Double>> getPrice(@Argument @NotBlank String id) {
        log.info("GraphQL price({})", id);
        return productService.getProductPrice(id)
                .map(price -> buildSuccess("/graphql?query=price", "Price fetched", price));
    }


    private <T> ApiResponse<T> buildSuccess(String path, String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .path(path)
                .message(message)
                .data(data)
                .build();
    }
}
