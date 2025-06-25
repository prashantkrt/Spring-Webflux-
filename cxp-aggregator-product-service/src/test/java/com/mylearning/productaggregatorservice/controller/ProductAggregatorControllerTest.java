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
    
    private ProductDto testProduct;

    @BeforeEach
    void setUp() {
        testProduct = ProductDto.builder()
                .productId("p1")
                .productDisplayName("Test Product")
                .price(999.99)
                .build();
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
                    assertEquals(HttpStatus.OK, response.getStatusCode());
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
                    assertEquals(HttpStatus.OK, response.getStatusCode());
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
}
