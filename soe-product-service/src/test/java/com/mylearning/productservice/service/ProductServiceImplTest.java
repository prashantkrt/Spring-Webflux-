package com.mylearning.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productservice.dto.ApiError;
import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.exception.AggregatorUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link ProductServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class ProductServiceImplTest {

    @Mock
    private WebClient aggregatorWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private final String productId = "test-product-123";
    private final ProductDto testProduct = new ProductDto(
            productId, "SEQ-001", "Test Product", "Test Brand", "SMARTPHONE", "ANDROID", 99.99, "Black"
    );
    private final List<ProductDto> testProducts = List.of(
            testProduct,
            new ProductDto(
                    "test-product-456", "SEQ-002", "Another Product", "Test Brand", "SMARTPHONE", "IOS", 149.99, "White"
            )
    );

    @BeforeEach
    void setUp() {
        // Configure lenient stubbing for all mocks to avoid unnecessary stubbing exceptions
        lenient().when(aggregatorWebClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Configure CircuitBreaker
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(2)
                .build();

        lenient().when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        lenient().when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        lenient().when(circuitBreaker.tryAcquirePermission()).thenReturn(true);
        lenient().when(circuitBreaker.getCircuitBreakerConfig()).thenReturn(config);
    }

    @Test
    void getProductDetails_Success() {
        // Setup
        ApiResponse<ProductDto> response = new ApiResponse<>();
        response.setData(testProduct);
        
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductDto>>() {}))
                .thenReturn(Mono.just(response));

        // Execute & Verify
        StepVerifier.create(productService.getProductDetails(productId))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void getProductDetails_Error() {
        // Arrange
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null, null, null)));

        // Act & Assert
        StepVerifier.create(productService.getProductDetails(productId))
                .expectError(AggregatorUnavailableException.class)
                .verify();

        verify(aggregatorWebClient.get()).uri("/{id}", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllProducts_Success() {
        // Setup
        ApiResponse<List<ProductDto>> response = new ApiResponse<>();
        response.setData(testProducts);
        
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<ProductDto>>>() {}))
                .thenReturn(Mono.just(response));

        // Execute & Verify
        StepVerifier.create(productService.getAllProducts())
                .expectNextSequence(testProducts)
                .verifyComplete();
    }

    @Test
    void getProductPrice_Success() {
        // Arrange
        double testPrice = 99.99;
        ApiResponse<Double> response = new ApiResponse<>();
        response.setData(testPrice);
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<ApiResponse<Double>>() {}))
                .thenReturn(Mono.just(response));

        // Act & Assert
        StepVerifier.create(productService.getProductPrice(productId))
                .expectNext(testPrice)
                .verifyComplete();

        verify(aggregatorWebClient.get()).uri("/{id}/price", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));
    }

    @Test
    void getProductPrice_Error() {
        // Arrange
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null, null, null)));

        // Act & Assert
        StepVerifier.create(productService.getProductPrice(productId))
                .expectError(AggregatorUnavailableException.class)
                .verify();

        verify(aggregatorWebClient.get()).uri("/{id}/price", productId);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));
    }

    @Test
    void toAggregatorUnavailable_WithMalformedErrorResponse() throws Exception {
        // Setup
        WebClientResponseException ex = mock(WebClientResponseException.class);
        when(ex.getResponseBodyAsString()).thenReturn("invalid-json");
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Execute
        Throwable result = ReflectionTestUtils.invokeMethod(
                productService, "toAggregatorUnavailable", "test-context", ex);

        // Verify
        assertThat(result).isInstanceOf(AggregatorUnavailableException.class);
        assertThat(result.getMessage()).contains("test-context");
    }
    
    @Test
    void toAggregatorUnavailable_WithDifferentStatusCodes() throws Exception {
        // Test with 400 Bad Request
        testErrorResponse(400, "Bad request sent to aggregator");
        
        // Test with 404 Not Found
        testErrorResponse(404, "Product not found in aggregator: 'test-id'");
        
        // Test with 502 Bad Gateway
        testErrorResponse(502, "Aggregator service is currently unavailable");
        
        // Test with 503 Service Unavailable
        testErrorResponse(503, "Aggregator service is currently unavailable");
        
        // Test with 504 Gateway Timeout
        testErrorResponse(504, "Aggregator service is currently unavailable");
        
        // Test with unknown status code
        testErrorResponse(418, "Unexpected error from aggregator (status 418)");
    }
    
    private void testErrorResponse(int statusCode, String expectedMessage) throws Exception {
        // Setup
        WebClientResponseException ex = mock(WebClientResponseException.class);
        when(ex.getResponseBodyAsString()).thenReturn("{}");
        when(ex.getStatusCode()).thenReturn(HttpStatus.valueOf(statusCode));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of("status", statusCode));

        // Execute
        String context = statusCode == 404 ? "product test-id" : "test-context";
        Throwable result = ReflectionTestUtils.invokeMethod(
                productService, "toAggregatorUnavailable", context, ex);

        // Verify
        assertThat(result).isInstanceOf(AggregatorUnavailableException.class);
        assertThat(result.getMessage()).contains(expectedMessage);
        
        // Verify ApiError details when status is 400
        if (statusCode == 400) {
            List<ApiError> errors = ((AggregatorUnavailableException) result).getErrors();
            assertThat(errors).isNotEmpty();
            assertThat(errors.get(0).getMessage()).isEqualTo(expectedMessage);
        }
    }
    
    @Test
    void handleError_WithWebClientRequestException() {
        // Setup
        WebClientRequestException ex = new WebClientRequestException(
            new RuntimeException("Connection refused"),
            HttpMethod.GET,
            URI.create("http://test-uri"),
            new HttpHeaders());
            
        // Execute
        Mono<?> result = (Mono<?>) ReflectionTestUtils.invokeMethod(
            productService, "handleError", "test-context", ex);
            
        // Verify
        StepVerifier.create((Mono<?>) result)
            .expectErrorSatisfies(error -> {
                assertThat(error).isInstanceOf(AggregatorUnavailableException.class);
                assertThat(error.getMessage()).contains("test-context");
            })
            .verify();
    }
    
    @Test
    void handleErrorFlux_WithWebClientRequestException() {
        // Setup
        WebClientRequestException ex = new WebClientRequestException(
            new RuntimeException("Connection refused"),
            HttpMethod.GET,
            URI.create("http://test-uri"),
            new HttpHeaders());
            
        // Execute
        Flux<?> result = (Flux<?>) ReflectionTestUtils.invokeMethod(
            productService, "handleErrorFlux", "test-context", ex);
            
        // Verify
        StepVerifier.create((Flux<?>) result)
            .expectErrorSatisfies(error -> {
                assertThat(error).isInstanceOf(AggregatorUnavailableException.class);
                assertThat(error.getMessage()).contains("test-context");
            })
            .verify();
    }
    
    @Test
    void circuitBreaker_IsConfiguredCorrectly() {
        // Setup - Make a call that would trigger circuit breaker
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
            .thenReturn(Mono.error(new WebClientResponseException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                null, null, null)));

        // Execute
        productService.getProductDetails("test-id").subscribe();

        // Verify
        verify(circuitBreakerRegistry).circuitBreaker("productServiceCB");
        assertThat(circuitBreaker).isNotNull();
    }
}
