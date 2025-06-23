package com.mylearning.productdomainservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productdomainservice.exception.InvalidProductDataException;
import com.mylearning.productdomainservice.exception.ProductNotFoundException;
import com.mylearning.productdomainservice.model.Product;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class ProductService {

    private List<Product> products;

    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("data/products.json");

            if (is == null) {
                throw new IllegalStateException("products.json file not found in classpath");
            }

            products = mapper.readValue(is, new TypeReference<>() {});
            log.info("Loaded {} products from products.json", products.size());

        } catch (Exception ex) {
            log.error("Failed to load product data from JSON", ex);
            throw new InvalidProductDataException("Failed to load product data", ex);
        }
    }

    public Mono<Product> getProductById(String id) {
        return Mono.justOrEmpty(
                        products.stream()
                                .filter(p -> p.getId().equals(id))
                                .findFirst()
                )
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    public Mono<Double> getPriceById(String id) {
        return getProductById(id).map(Product::getPrice);
    }

    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(products);
    }
}
