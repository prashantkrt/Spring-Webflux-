package com.mylearning.productdomainservice.service;

import com.mylearning.productdomainservice.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    Flux<Product> getAllProducts();
    Mono<Product> getProductById(String id);
    Mono<Double> getPriceById(String id);
}
