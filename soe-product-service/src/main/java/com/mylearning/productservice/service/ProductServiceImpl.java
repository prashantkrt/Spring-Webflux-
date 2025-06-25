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

import java.util.List;
import java.util.Map;

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

    private static final ParameterizedTypeReference<ApiResponse<ProductDto>> PRODUCT_REF = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<ApiResponse<List<ProductDto>>> LIST_REF = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<ApiResponse<Double>> PRICE_REF = new ParameterizedTypeReference<>() {
    };

    private static final String ERROR_PREFIX = "Aggregator error while fetching ";
    private static final String STATUS_KEY = "status";

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

    // fallback error handling
    private <T> Mono<T> handleError(String context, Throwable ex) {
        log.warn("Fallback triggered for {}: {}", context, ex.getMessage());
        return Mono.error(toAggregatorUnavailable(context, ex));
    }

    // fallback error handling
    private <T> Flux<T> handleErrorFlux(String context, Throwable ex) {
        log.warn("Fallback triggered for {}: {}", context, ex.getMessage());
        return Flux.error(toAggregatorUnavailable(context, ex));
    }

    //parsing error from aggregator
    @WithSpan("ProductService.parseAggregatorError")
    private AggregatorUnavailableException toAggregatorUnavailable(String context, Throwable ex) {
        if (ex instanceof WebClientResponseException wex) {
            try {
                Map<String, Object> responseBody = objectMapper.readValue(
                        wex.getResponseBodyAsString(),
                        new TypeReference<>() {
                        }
                );

                String productId = context.replace("product ", "");

                int status = (int) responseBody.getOrDefault(STATUS_KEY, wex.getStatusCode().value());

                String message = switch (status) {
                    case 400 -> "Bad request sent to aggregator";
                    case 404 -> "Product not found in aggregator: '" + productId + "'";
                    case 502, 503, 504 -> "Aggregator service is currently unavailable";
                    default -> "Unexpected error from aggregator (status " + status + ")";
                };

                // Prepare clean key-value details
                Map<String, Object> detailsMap = Map.of(
                        STATUS_KEY, responseBody.getOrDefault(STATUS_KEY, 500),
                        "error", responseBody.getOrDefault("error", "Unknown"),
                        "path", responseBody.getOrDefault("path", "N/A"),
                        "requestId", responseBody.getOrDefault("requestId", "N/A"),
                        "timestamp", responseBody.getOrDefault("timestamp", "N/A")
                );

                ApiError apiError = ApiError.builder()
                        .code("PRODUCT_NOT_FOUND")
                        .message(message)
                        .details(detailsMap)
                        .build();

                return new AggregatorUnavailableException(
                        ERROR_PREFIX + context + ": " + message,
                        List.of(apiError)
                );

            } catch (Exception e) {
                log.warn("Error parsing aggregator response: {}", e.getMessage());
                return new AggregatorUnavailableException(ERROR_PREFIX + context, wex);
            }
        }

        return new AggregatorUnavailableException(ERROR_PREFIX + context, ex);
    }

}
