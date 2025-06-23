package com.mylearning.productservice.service;

import com.mylearning.productservice.dto.ProductDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<ProductDto> getProductDetails(String id);

    Flux<ProductDto> getAllProducts();

    Mono<Double> getProductPrice(String id);
}
