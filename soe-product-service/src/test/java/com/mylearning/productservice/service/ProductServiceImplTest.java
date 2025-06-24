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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

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
}
