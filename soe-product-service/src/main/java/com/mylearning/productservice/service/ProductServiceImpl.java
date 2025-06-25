package com.mylearning.productservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productservice.dto.ApiError;
import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.exception.AggregatorUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final WebClient aggregatorWebClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(WebClient aggregatorWebClient,
                              CircuitBreakerRegistry circuitBreakerRegistry,
                              ObjectMapper objectMapper) {
        this.aggregatorWebClient = aggregatorWebClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.objectMapper = objectMapper;
    }
    private static final String CB_NAME = "productServiceCB";

    private CircuitBreaker getCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker(CB_NAME);
    }

    private static final ParameterizedTypeReference<ApiResponse<ProductDto>> PRODUCT_REF = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<List<ProductDto>>> LIST_REF = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<Double>> PRICE_REF = new ParameterizedTypeReference<>() {};

    private static final String ERROR_PREFIX = "Aggregator error while fetching ";
    @Override
    @WithSpan("ProductService.getProductDetails")
    public Mono<ProductDto> getProductDetails(String id) {
        log.info("Fetching product details for id {}", id);

        return aggregatorWebClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(PRODUCT_REF)
                .map(ApiResponse::getData)
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .onErrorResume(ex -> handleError("product " + id, ex));
    }

    @Override
    @WithSpan("ProductService.getAllProducts")
    public Flux<ProductDto> getAllProducts() {
        log.info("Fetching all products");

        return aggregatorWebClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(LIST_REF)
                .map(ApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .onErrorResume(ex -> handleErrorFlux("all products", ex));
    }

    @Override
    @WithSpan("ProductService.getProductPrice")
    public Mono<Double> getProductPrice(String id) {
        log.info("Fetching price for product {}", id);

        return aggregatorWebClient.get()
                .uri("/{id}/price", id)
                .retrieve()
                .bodyToMono(PRICE_REF)
                .map(ApiResponse::getData)
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .onErrorResume(ex -> handleError("price for product " + id, ex));
    }

    private <T> Mono<T> handleError(String context, Throwable ex) {
        return Mono.error(toAggregatorUnavailable(context, ex));
    }

    private <T> Flux<T> handleErrorFlux(String context, Throwable ex) {
        return Flux.error(toAggregatorUnavailable(context, ex));
    }

    @WithSpan("ProductService.parseAggregatorError")
    private AggregatorUnavailableException toAggregatorUnavailable(String context, Throwable ex) {
        if (ex instanceof WebClientResponseException wex) {
            try {
                // Parse with generic Object since ApiResponse is generic
                ApiResponse<Object> raw = objectMapper.readValue(
                        wex.getResponseBodyAsString(),
                        new TypeReference<>() {}
                );

                // Try mapping each entry of 'errors' to ApiError
                List<ApiError> errors = raw.getErrors() != null ? raw.getErrors() : Collections.emptyList();

                String message = errors.stream()
                        .findFirst()
                        .map(ApiError::getMessage)
                        .orElse("Unknown aggregator error");

                return new AggregatorUnavailableException(ERROR_PREFIX + context + ": " + message, errors);

            } catch (Exception e) {
                log.warn("Failed to parse error body from aggregator: {}", e.getMessage());
                return new AggregatorUnavailableException(ERROR_PREFIX + context, wex);
            }
        }

        return new AggregatorUnavailableException(ERROR_PREFIX+ context, ex);
    }
}
