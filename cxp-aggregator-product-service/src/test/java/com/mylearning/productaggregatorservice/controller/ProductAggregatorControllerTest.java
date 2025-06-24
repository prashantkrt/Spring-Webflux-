package com.mylearning.productaggregatorservice.controller;

import com.mylearning.productaggregatorservice.dto.ApiResponse;
import com.mylearning.productaggregatorservice.dto.ProductDto;
import com.mylearning.productaggregatorservice.service.ProductAggregatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAggregatorControllerTest {

    @Mock
    private ProductAggregatorService productAggregatorService;

    @InjectMocks
    private ProductAggregatorController productAggregatorController;
    
    private WebTestClient webTestClient;
    private ProductDto testProduct;

    @BeforeEach
    void setUp() {
        testProduct = ProductDto.builder()
                .productId("p1")
                .productDisplayName("Test Product")
                .price(999.99)
                .build();
                
        webTestClient = WebTestClient.bindToController(productAggregatorController).build();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        Flux<ProductDto> products = Flux.just(testProduct);
        when(productAggregatorService.getAllProducts())
                .thenReturn(products);

        // When
        Mono<ResponseEntity<ApiResponse<List<ProductDto>>>> result = productAggregatorController.getAllProducts();

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    ApiResponse<List<ProductDto>> apiResponse = response.getBody();
                    assertNotNull(apiResponse);
                    assertTrue(apiResponse.isApiSuccess());
                    assertNotNull(apiResponse.getTimeStamp());
                    assertNotNull(apiResponse.getData());
                    assertEquals(1, apiResponse.getData().size());
                    assertEquals(testProduct, apiResponse.getData().get(0));
                })
                .verifyComplete();

        verify(productAggregatorService).getAllProducts();
    }

    @Test
    void getAllProducts_WhenServiceError_ShouldReturnError() {
        // Given
        when(productAggregatorService.getAllProducts())
                .thenReturn(Flux.error(new RuntimeException("Service error")));

        // When
        Mono<ResponseEntity<ApiResponse<List<ProductDto>>>> result = productAggregatorController.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productAggregatorService).getAllProducts();
    }

    @Test
    void getProduct_WithValidId_ShouldReturnProduct() {
        // Given
        String productId = "p1";
        Mono<ProductDto> product = Mono.just(testProduct);
        when(productAggregatorService.getProduct(productId))
                .thenReturn(product);

        // When
        Mono<ResponseEntity<ApiResponse<ProductDto>>> result = productAggregatorController.getProductById(productId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    ApiResponse<ProductDto> apiResponse = response.getBody();
                    assertNotNull(apiResponse);
                    assertTrue(apiResponse.isApiSuccess());
                    assertNotNull(apiResponse.getTimeStamp());
                    assertNotNull(apiResponse.getData());
                    assertEquals(testProduct, apiResponse.getData());
                })
                .verifyComplete();

        verify(productAggregatorService).getProduct(productId);
    }

    @Test
    void getProduct_WithInvalidId_ShouldReturnError() {
        // Given
        String productId = "invalid";
        when(productAggregatorService.getProduct(productId))
                .thenReturn(Mono.error(new RuntimeException("Invalid ID")));

        // When
        Mono<ResponseEntity<ApiResponse<ProductDto>>> result = productAggregatorController.getProductById(productId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productAggregatorService).getProduct(productId);
    }

    @Test
    void getProductPrice_WithValidId_ShouldReturnPrice() {
        // Given
        String productId = "p1";
        Double price = 999.99;
        Mono<Double> priceMono = Mono.just(price);
        when(productAggregatorService.getProductPrice(productId))
                .thenReturn(priceMono);

        // When
        Mono<ResponseEntity<ApiResponse<Double>>> result = productAggregatorController.getPriceById(productId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    ApiResponse<Double> apiResponse = response.getBody();
                    assertNotNull(apiResponse);
                    assertTrue(apiResponse.isApiSuccess());
                    assertNotNull(apiResponse.getTimeStamp());
                    assertNotNull(apiResponse.getData());
                    assertEquals(price, apiResponse.getData());
                })
                .verifyComplete();

        verify(productAggregatorService).getProductPrice(productId);
    }

    @Test
    void getProductPrice_WithInvalidId_ShouldReturnError() {
        // Given
        String productId = "invalid";
        when(productAggregatorService.getProductPrice(productId))
                .thenReturn(Mono.error(new RuntimeException("Invalid ID")));

        // When
        Mono<ResponseEntity<ApiResponse<Double>>> result = productAggregatorController.getPriceById(productId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productAggregatorService).getProductPrice(productId);
    }

    @Test
    void getProductById_WithNullId_ShouldThrowException() {
        // When
        Mono<ResponseEntity<ApiResponse<ProductDto>>> result = productAggregatorController.getProductById(null);
        
        // Then
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Given
        String productId = "p1";
        Mono<ProductDto> product = Mono.just(testProduct);
        when(productAggregatorService.getProduct(productId))
                .thenReturn(product);

        // When
        Mono<ResponseEntity<ApiResponse<ProductDto>>> result = productAggregatorController.getProductById(productId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    ApiResponse<ProductDto> apiResponse = response.getBody();
                    assertNotNull(apiResponse);
                    assertTrue(apiResponse.isApiSuccess());
                    assertNotNull(apiResponse.getTimeStamp());
                    assertNotNull(apiResponse.getData());
                    assertEquals(testProduct, apiResponse.getData());
                })
                .verifyComplete();

        verify(productAggregatorService).getProduct(productId);
    }

    @Test
    void getProductById_WithInvalidId_ShouldReturnError() {
        // Given
        String invalidId = "invalid";
        when(productAggregatorService.getProduct(invalidId))
                .thenReturn(Mono.error(new RuntimeException("Product not found")));

        // When
        Mono<ResponseEntity<ApiResponse<ProductDto>>> result = productAggregatorController.getProductById(invalidId);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productAggregatorService).getProduct(invalidId);
    }

    @Test
    void getPriceById_WithValidId_ShouldReturnPrice() {
        // Given
        String productId = "p1";
        Double price = 999.99;
        when(productAggregatorService.getProductPrice(productId)).thenReturn(Mono.just(price));

        // When
        Mono<ResponseEntity<ApiResponse<Double>>> result = productAggregatorController.getPriceById(productId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    ApiResponse<Double> apiResponse = response.getBody();
                    assertNotNull(apiResponse);
                    assertTrue(apiResponse.isApiSuccess());
                    assertNotNull(apiResponse.getTimeStamp());
                    assertNotNull(apiResponse.getData());
                    assertEquals(price, apiResponse.getData());
                })
                .verifyComplete();

        verify(productAggregatorService).getProductPrice(productId);
    }

    @Test
    void getPriceById_WithInvalidId_ShouldReturnError() {
        // Given
        String invalidId = "invalid";
        when(productAggregatorService.getProductPrice(invalidId))
                .thenReturn(Mono.error(new RuntimeException("Product not found")));

        // When
        Mono<ResponseEntity<ApiResponse<Double>>> result = productAggregatorController.getPriceById(invalidId);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();

        verify(productAggregatorService).getProductPrice(invalidId);
    }

    @Test
    void getPriceById_WithNullId_ShouldThrowException() {
        // When
        Mono<ResponseEntity<ApiResponse<Double>>> result = productAggregatorController.getPriceById(null);
        
        // Then
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
