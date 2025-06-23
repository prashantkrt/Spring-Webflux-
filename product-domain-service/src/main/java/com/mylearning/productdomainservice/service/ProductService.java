package com.mylearning.productdomainservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productdomainservice.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

@Service
public class ProductService {
    private List<Product> products;

    @PostConstruct
    public void loadData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/products.json");
        if (is == null) {
            throw new IllegalStateException("products.json file not found in classpath");
        }
        products = mapper.readValue(is, new TypeReference<List<Product>>() {});
    }

    public Mono<Product> getProductById(String id) {
        return Mono.justOrEmpty(
                products.stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
        );
    }

    public Mono<Double> getPriceById(String id) {
        return getProductById(id).map(Product::getPrice);
    }

    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(products);
    }
}
