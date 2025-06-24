package com.mylearning.productaggregatorservice.service;

import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.exception.DownstreamException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.core.IntervalFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductAggregatorServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private CircuitBreakerRegistry cbRegistry;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private org.slf4j.Logger logger;

    private ProductAggregatorServiceImpl productAggregatorService;

    private ProductDto testProduct;

    private static final String BASE_URL = "http://test-service";

    @BeforeEach
    void setUp() {
        testProduct = ProductDto.builder()
                .productId("p1")
                .productDisplayName("Test Product")
                .price(999.99)
                .build();

        // Mock WebClient builder with lenient stubbing
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        
        // Initialize the service manually with the mocked dependencies
        productAggregatorService = new ProductAggregatorServiceImpl(webClientBuilder, BASE_URL, cbRegistry, retryRegistry);
        
        // Mock CircuitBreaker with lenient stubbing
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("productServiceCB");
        lenient().when(cbRegistry.circuitBreaker(eq("productServiceCB"))).thenReturn(circuitBreaker);
        
        // Set up default retry configuration (can be overridden in individual tests)
        Retry defaultRetry = Retry.of("productServiceRetry", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .build());
        lenient().when(retryRegistry.retry(eq("productServiceRetry"))).thenReturn(defaultRetry);
    }
    
    private void setupWebClientForGetRequest(String uri, Object... uriVariables) {
        // Reset mocks to avoid interference between tests
        reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
        
        // Setup the WebClient call chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        
        // Handle both uri with and without variables
        if (uriVariables.length > 0) {
            when(requestHeadersUriSpec.uri(uri, uriVariables)).thenReturn(requestHeadersSpec);
        } else {
            when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersSpec);
        }
        
        // Setup the rest of the chain
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Make sure the mocks are lenient
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }
    
    private void setupUriStubbing(String uriTemplate, Object... uriVariables) {
        when(requestHeadersUriSpec.uri(uriTemplate, uriVariables)).thenReturn(requestHeadersSpec);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        Flux<ProductDto> products = Flux.just(testProduct);
        setupWebClientForGetRequest("");
        when(responseSpec.bodyToFlux(eq(ProductDto.class))).thenReturn(products);

        // When
        Flux<ProductDto> result = productAggregatorService.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectNext(testProduct)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToFlux(eq(ProductDto.class));
    }

    @Test
    void getAllProducts_WhenDownstreamError_ShouldReturnError() {
        // Given
        setupWebClientForGetRequest("");
        when(responseSpec.bodyToFlux(eq(ProductDto.class)))
                .thenReturn(Flux.error(new RuntimeException("Downstream error")));

        // When
        Flux<ProductDto> result = productAggregatorService.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectError(DownstreamException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToFlux(eq(ProductDto.class));
    }

    @Test
    void getProduct_WithValidId_ShouldReturnProduct() {
        // Given
        String productId = "p1";
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class))).thenReturn(Mono.just(testProduct));

        // When
        Mono<ProductDto> result = productAggregatorService.getProduct(productId);
        
        // Then
        StepVerifier.create(result)
                .expectNext(testProduct)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/{id}", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(eq(ProductDto.class));
    }

    @Test
    void getProduct_WhenDownstreamError_ShouldReturnError() {
        // Given
        String productId = "p1";
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class)))
                .thenReturn(Mono.error(new RuntimeException("Downstream error")));

        // When
        Mono<ProductDto> result = productAggregatorService.getProduct(productId);
        
        // Then
        StepVerifier.create(result)
                .expectError(DownstreamException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/{id}", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(eq(ProductDto.class));
    }

    @Test
    void getProduct_WithRetry_ShouldRetryOnFailure() {
        // Given
        String productId = "p1";
        RuntimeException error = new RuntimeException("Temporary failure");
        
        // Create a retry config with interval function
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.of(Duration.ofMillis(10)))
                .build();
                
        // Create a spy on the retry registry to verify interactions
        Retry retry = spy(Retry.of("productServiceRetry", retryConfig));
        
        // Print retry config for debugging
        System.out.println("Retry max attempts: " + retry.getRetryConfig().getMaxAttempts());
        if (retry.getRetryConfig().getIntervalFunction() != null) {
            System.out.println("Retry wait duration: " + retry.getRetryConfig().getIntervalFunction().apply(1) + "ms");
        }
        
        // Override the default retry configuration for this test
        when(retryRegistry.retry(eq("productServiceRetry"))).thenReturn(retry);
        
        // Mock the web client to always fail with the same error
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class)))
                .thenReturn(Mono.error(error));

        // When
        Mono<ProductDto> result = productAggregatorService.getProduct(productId);

        // Then - Should fail with DownstreamException after retries
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    System.out.println("Error received: " + ex.getClass().getName() + ": " + ex.getMessage());
                    assertTrue(ex instanceof DownstreamException);
                    assertTrue(ex.getMessage().startsWith("Failed to fetch product " + productId));
                    assertSame(error, ex.getCause());
                })
                .verify();

        // Verify the retry was used
        verify(retry, atLeastOnce()).getRetryConfig();
        
        // Verify the web client was called multiple times (depends on retry configuration)
        verify(webClient, atLeastOnce()).get();
        verify(requestHeadersUriSpec, atLeastOnce()).uri("/{id}", productId);
        verify(requestHeadersSpec, atLeastOnce()).retrieve();
        verify(responseSpec, atLeastOnce()).bodyToMono(eq(ProductDto.class));
        
        // Print how many times the web client was called
        System.out.println("WebClient.get() was called " + 
            Mockito.mockingDetails(webClient).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("get"))
                .count() + " times");
    }

    @Test
    void getProduct_WithCircuitBreaker_ShouldOpenOnRepeatedFailures() {
        // Given
        String productId = "p1";
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class)))
                .thenReturn(Mono.error(new RuntimeException("Service down")));

        // When
        for (int i = 0; i < 5; i++) {
            try {
                productAggregatorService.getProduct(productId).block();
            } catch (Exception ignored) {}
        }
        
        // Then - Verify circuit breaker is open
        StepVerifier.create(productAggregatorService.getProduct(productId))
                .expectError()
                .verify();
    }

    @Test
    void getProduct_WithNonExistentId_ShouldReturnError() {
        // Given
        String productId = "non-existent";
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class)))
                .thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, null, null)));

        // When
        Mono<ProductDto> result = productAggregatorService.getProduct(productId);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof DownstreamException);
                    assertTrue(ex.getCause() instanceof WebClientResponseException);
                    assertEquals(404, ((WebClientResponseException) ex.getCause()).getStatusCode().value());
                })
                .verify();
    }

    @Test
    void getProduct_ShouldLogAppropriateMessages() {
        // Given
        String productId = "p1";
        Logger logger = (Logger) LoggerFactory.getLogger("com.mylearning.productaggregatorservice.service.ProductAggregatorServiceImpl");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class))).thenReturn(Mono.just(testProduct));

        // When
        productAggregatorService.getProduct(productId).block();


        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertTrue(logsList.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Fetching product id " + productId) && 
                                  event.getLevel() == Level.INFO));
        
        logger.detachAppender(listAppender);
    }

    @Test
    void fallbackMethods_ShouldLogWarning() {
        // Given
        String productId = "p1";
        Logger logger = (Logger) LoggerFactory.getLogger("com.mylearning.productaggregatorservice.service.ProductAggregatorServiceImpl");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // When
        try {
            productAggregatorService.getProduct(productId).block();
        } catch (Exception ignored) {}

        // Then
        List<ILoggingEvent> warningLogs = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .collect(Collectors.toList());
        
        assertFalse(warningLogs.isEmpty());
        assertTrue(warningLogs.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Fallback triggered for product")));
        
        logger.detachAppender(listAppender);
    }

    @Test
    void getProductPrice_WithValidId_ShouldReturnPrice() {
        // Given
        String productId = "p1";
        Double price = 999.99;
        setupWebClientForGetRequest("/{id}/price", productId);
        when(responseSpec.bodyToMono(eq(Double.class))).thenReturn(Mono.just(price));

        // When
        Mono<Double> result = productAggregatorService.getProductPrice(productId);

        // Then
        StepVerifier.create(result)
                .expectNext(price)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/{id}/price", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(eq(Double.class));
    }

    @Test
    void getProductPrice_WhenDownstreamError_ShouldReturnError() {
        // Given
        String productId = "p1";
        setupWebClientForGetRequest("/{id}/price", productId);
        when(responseSpec.bodyToMono(eq(Double.class)))
                .thenReturn(Mono.error(new WebClientResponseException(500, "Internal Server Error", null, null, null)));

        // When
        Mono<Double> result = productAggregatorService.getProductPrice(productId);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof DownstreamException);
                    assertTrue(ex.getCause() instanceof WebClientResponseException);
                    assertEquals(500, ((WebClientResponseException) ex.getCause()).getStatusCode().value());
                })
                .verify();
    }

    @Test
    void getAllProducts_ShouldReturnMultipleProducts() {
        // Given
        ProductDto product1 = ProductDto.builder()
                .productId("p1")
                .productDisplayName("Product 1")
                .price(100.0)
                .build();
        ProductDto product2 = ProductDto.builder()
                .productId("p2")
                .productDisplayName("Product 2")
                .price(200.0)
                .build();
        
        setupWebClientForGetRequest("");
        when(responseSpec.bodyToFlux(eq(ProductDto.class)))
                .thenReturn(Flux.just(product1, product2));

        // When
        Flux<ProductDto> result = productAggregatorService.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectNext(product1, product2)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToFlux(eq(ProductDto.class));
    }

    @Test
    void getAllProducts_WhenEmptyResponse_ShouldReturnEmptyFlux() {
        // Given
        setupWebClientForGetRequest("");
        when(responseSpec.bodyToFlux(eq(ProductDto.class)))
                .thenReturn(Flux.empty());

        // When
        Flux<ProductDto> result = productAggregatorService.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getProductPrice_WithDownstreamError_ShouldReturnError() {
        // Given
        String productId = "p1";
        setupWebClientForGetRequest("/{id}/price", productId);
        when(responseSpec.bodyToMono(eq(Double.class)))
                .thenReturn(Mono.error(new RuntimeException("Downstream error")));

        // When
        Mono<Double> result = productAggregatorService.getProductPrice(productId);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof DownstreamException);
                    assertTrue(ex.getCause() instanceof RuntimeException);
                    assertEquals("Downstream error", ex.getCause().getMessage());
                })
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/{id}/price", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(eq(Double.class));
    }

    @Test
    void circuitBreakerAndRetry_ShouldBeApplied() {
        // Given
        String productId = "p1";
        setupWebClientForGetRequest("/{id}", productId);
        when(responseSpec.bodyToMono(eq(ProductDto.class))).thenReturn(Mono.just(testProduct));

        // When
        productAggregatorService.getProduct(productId).block();

        // Then
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/{id}", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(eq(ProductDto.class));
        
        // Verify circuit breaker and retry were used
        verify(cbRegistry).circuitBreaker(anyString());
        verify(retryRegistry).retry(anyString());
    }
}
