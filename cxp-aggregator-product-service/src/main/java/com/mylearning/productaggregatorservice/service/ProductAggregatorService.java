package com.mylearning.productaggregatorservice.service;

import com.mylearning.productaggregatorservice.dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductAggregatorService {

    private final WebClient webClient;

    public ProductAggregatorService(@Value("${product.service.base-url}") String baseUrl, WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }
    // Fetch all products
    public Flux<ProductDto> fetchAllProducts() {
        return webClient.get()
                .uri("")
                .retrieve()
                .bodyToFlux(ProductDto.class);
    }

    // Fetch product by ID
    public Mono<ProductDto> fetchProductById(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(ProductDto.class);
    }

    // Fetch price by product ID
    public Mono<Double> fetchPriceById(String id) {
        return webClient.get()
                .uri("/{id}/price", id)
                .retrieve()
                .bodyToMono(Double.class);
    }
}
