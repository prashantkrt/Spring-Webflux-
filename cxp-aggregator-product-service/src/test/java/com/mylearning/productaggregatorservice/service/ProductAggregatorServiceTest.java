package com.mylearning.productaggregatorservice.service;

import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.exception.DownstreamException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    private ProductAggregatorService productAggregatorService;

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
        productAggregatorService = new ProductAggregatorService(webClientBuilder, BASE_URL, cbRegistry, retryRegistry);
        
        // Mock CircuitBreaker and Retry with lenient stubbing
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("test");
        lenient().when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        
        Retry retry = Retry.ofDefaults("test");
        lenient().when(retryRegistry.retry(anyString())).thenReturn(retry);
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
                .expectError(DownstreamException.class)
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
