package com.mylearning.productaggregatorservice.service;

import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.exception.DownstreamException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProductAggregatorServiceImpl implements ProductAggregatorService {

    private static final String CB_NAME = "productServiceCB";
    private static final String RETRY_NAME = "productServiceRetry";

    private final WebClient aggregatorWebClient;
    private final CircuitBreakerRegistry cbRegistry;
    private final RetryRegistry retryRegistry;

    public ProductAggregatorServiceImpl(WebClient.Builder builder,
                                    @Value("${product.service.base-url}") String baseUrl,
                                    CircuitBreakerRegistry cbRegistry,
                                    RetryRegistry retryRegistry) {
        this.aggregatorWebClient = builder.baseUrl(baseUrl).build();
        this.cbRegistry = cbRegistry;
        this.retryRegistry = retryRegistry;
    }

    private CircuitBreaker getCircuitBreaker() {
        return cbRegistry.circuitBreaker(CB_NAME);
    }

    private Retry getRetry() {
        return retryRegistry.retry(RETRY_NAME);
    }

    @Override
    public Flux<ProductDto> getAllProducts() {
        log.info("Fetching all products");

        return aggregatorWebClient.get()
                .uri("")
                .retrieve()
                .bodyToFlux(ProductDto.class)
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .transformDeferred(RetryOperator.of(getRetry()))
                .doOnNext(product -> log.debug("Received product: {}", product))
                .doOnError(ex -> log.error("Error fetching all products: {}", ex.getMessage()))
                .onErrorResume(ex -> {
                    if (ex instanceof WebClientResponseException webEx) {
                        String body = webEx.getResponseBodyAsString();
                        log.warn("Downstream 4xx/5xx while fetching all products – {}: {}", webEx.getStatusCode(), body);
                        return Flux.error(new DownstreamException("Failed to fetch all products: " + body, webEx));
                    }
                    return Flux.error(new DownstreamException("Failed to fetch all products", ex));
                });
    }

    @Override
    public Mono<ProductDto> getProduct(String id) {
        log.info("Fetching product id {}", id);

        return aggregatorWebClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .transformDeferred(RetryOperator.of(getRetry()))
                .doOnSuccess(product -> log.info("Product {} fetched", id))
                .doOnError(ex -> log.error("Error fetching product {}: {}", id, ex.getMessage()))
                .onErrorResume(ex -> {
                    fallbackProductAction(id, ex);
                    if (ex instanceof WebClientResponseException webEx) {
                        String body = webEx.getResponseBodyAsString();
                        log.warn("Downstream 4xx/5xx for product {} – {}: {}", id, webEx.getStatusCode(), body);
                        return Mono.error(new DownstreamException("Failed to fetch product " + id + ": " + body, webEx));
                    }
                    return Mono.error(new DownstreamException("Failed to fetch product " + id, ex));
                });
    }

    @Override
    public Mono<Double> getProductPrice(String id) {
        log.info("Fetching price for product id {}", id);

        return aggregatorWebClient.get()
                .uri("/{id}/price", id)
                .retrieve()
                .bodyToMono(Double.class)
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .transformDeferred(RetryOperator.of(getRetry()))
                .doOnSuccess(price -> log.info("Price for id {} is {}", id, price))
                .doOnError(ex -> log.error("Error fetching price for id {}: {}", id, ex.getMessage()))
                .onErrorResume(ex -> {
                    fallbackProductPriceAction(id, ex);
                    if (ex instanceof WebClientResponseException webEx) {
                        String body = webEx.getResponseBodyAsString();
                        log.warn("Downstream 4xx/5xx for price {} – {}: {}", id, webEx.getStatusCode(), body);
                        return Mono.error(new DownstreamException("Failed to fetch price for product " + id + ": " + body, webEx));
                    }
                    return Mono.error(new DownstreamException("Failed to fetch price for product " + id, ex));
                });
    }

    private void fallbackProductAction(String id, Throwable ex) {
        log.warn("Fallback triggered for product {}: {}", id, ex.getMessage());
    }

    private void fallbackProductPriceAction(String id, Throwable ex) {
        log.warn("Fallback triggered for price {}: {}", id, ex.getMessage());
    }
}
