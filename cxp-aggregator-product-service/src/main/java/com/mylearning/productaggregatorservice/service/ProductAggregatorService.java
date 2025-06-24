package com.mylearning.productaggregatorservice.service;

import com.mylearning.productaggregatorservice.dto.ProductDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductAggregatorService {

    Flux<ProductDto> getAllProducts();

    Mono<ProductDto> getProduct(String id);

    Mono<Double> getProductPrice(String id);
}
