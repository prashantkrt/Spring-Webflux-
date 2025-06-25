package com.mylearning.productdomainservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productdomainservice.exception.ProductDataLoadException;
import com.mylearning.productdomainservice.exception.ProductDataNotLoadedException;
import com.mylearning.productdomainservice.exception.ProductNotFoundException;
import com.mylearning.productdomainservice.model.Product;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ObjectMapper mapper = new ObjectMapper();
    private List<Product> products;

    @PostConstruct
    @WithSpan("ProductService.loadData")
    public void loadData() throws IOException {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("data/productData.json");
            if (is == null) {
                throw new IllegalStateException("productData.json file not found");
            }

            JsonNode root = mapper.readTree(is);
            JsonNode productListNode = root.path("data").path("gridWall").path("productList");

            if (!productListNode.isArray()) {
                throw new IllegalStateException("'productList' is not an array");
            }

            products = StreamSupport.stream(productListNode.spliterator(), false)
                    .map(node -> {
                        Product product = mapper.convertValue(node, Product.class);

                        JsonNode childSkusNode = node.path("childSkus");

                        Double price = extractPriceFromChildSkus(childSkusNode);
                        if (price != null) {
                            product.setPrice(price);
                        } else {
                            log.warn("No valid price for {}", product.getProductId());
                        }

                        String color = extractColor(childSkusNode);
                        if (color != null) {
                            product.setColor(color);
                        }
                        return product;
                    }).toList();

            log.info("Loaded {} products successfully", products.size());
        } catch (Exception ex) {
            log.error("Failed to load product data from JSON", ex);
            throw new ProductDataLoadException("Unable to load product data from product data source", ex);
        }
    }


    private Double extractPriceFromChildSkus(JsonNode childSkusNode) {
        if (childSkusNode == null || !childSkusNode.isArray()) return null;

        for (JsonNode sku : childSkusNode) {
            JsonNode devicePayArr = sku.path("price").path("devicePaymentPrice");
            if (!devicePayArr.isArray()) continue;

            for (JsonNode priceItem : devicePayArr) {
                JsonNode p = priceItem.path("originalPrice");
                if (p.isNumber()) {
                    return p.asDouble();
                }
            }
        }
        return null;
    }

    private String extractColor(JsonNode childSkusNode) {

        if (!childSkusNode.isArray()) return null;

        for (JsonNode sku : childSkusNode) {
            JsonNode colorText = sku.path("color").path("displayName");
            if (!colorText.isMissingNode() && !colorText.asText().isBlank()) {
                return colorText.asText();
            }
        }
        return null;
    }


    @Override
    @WithSpan("ProductService.getAllProducts")
    public Flux<Product> getAllProducts() {
        log.info("Fetching all products");
        return Flux.defer(() -> {
            if (products == null || products.isEmpty()) {
                log.warn("Product data not loaded or empty");
                return Flux.error(new ProductDataNotLoadedException("Product data is not loaded or empty"));
            }
            log.info("Total products available: {}", products.size());
            return Flux.fromIterable(products);
        });
    }

    @Override
    @WithSpan("ProductService.getProductById")
    public Mono<Product> getProductById(String id) {
        log.info("Fetching product by ID: {}", id);
        return Mono.justOrEmpty(
                        products.stream()
                                .filter(p -> p.getProductId().equals(id))
                                .findFirst()
                )
                .doOnNext(p -> log.info("Found product: {}", p))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Product not found for ID: {}", id);
                    return Mono.error(new ProductNotFoundException(id));
                }));
    }

    @Override
    @WithSpan("ProductService.getPriceById")
    public Mono<Double> getPriceById(String id) {
        log.info("Fetching price for product ID: {}", id);

        return getProductById(id)
                .map(product -> {
                    log.info("Product price for {} is {}", id, product.getPrice());
                    return product.getPrice();
                });
    }
}
