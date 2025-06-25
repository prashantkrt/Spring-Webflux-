package com.mylearning.productdomainservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productdomainservice.exception.ProductDataLoadException;
import com.mylearning.productdomainservice.exception.ProductDataNotLoadedException;
import com.mylearning.productdomainservice.exception.ProductNotFoundException;
import com.mylearning.productdomainservice.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceExceptionTest {

    @Mock
    private ObjectMapper mapper;
    
    @Mock
    private JsonNode mockRootNode;
    
    @Mock
    private JsonNode mockDataNode;
    
    @Mock
    private JsonNode mockGridWallNode;
    
    @Mock
    private JsonNode mockProductListNode;

    private ProductServiceImpl productService;

    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
        // Initialize the products list to avoid NPE in tests
        ReflectionTestUtils.setField(productService, "mapper", mapper);
        ReflectionTestUtils.setField(productService, "products", new ArrayList<>());
        
        testProducts = List.of(
            new Product("p1", "1", "Test Product 1", "Brand", "CATEGORY", "OS", 100.0, "Black"),
            new Product("p2", "2", "Test Product 2", "Brand", "CATEGORY", "OS", 200.0, "White")
        );
        
        // Initialize the products list in the service
        ReflectionTestUtils.setField(productService, "products", testProducts);
    }

    @Test
    void loadData_WhenJsonParsingFails_ShouldThrowProductDataLoadException() throws Exception {
        // Given
        when(mapper.readTree(any(InputStream.class)))
            .thenThrow(new IOException("Failed to parse JSON"));

        // When & Then
        assertThrows(ProductDataLoadException.class, () -> productService.loadData());
    }

    @Test
    void loadData_WhenProductListIsNotAnArray_ShouldThrowProductDataLoadException() throws Exception {
        // Given
        when(mapper.readTree(any(InputStream.class))).thenReturn(mockRootNode);
        when(mockRootNode.path("data")).thenReturn(mockDataNode);
        when(mockDataNode.path("gridWall")).thenReturn(mockGridWallNode);
        when(mockGridWallNode.path("productList")).thenReturn(mockProductListNode);
        when(mockProductListNode.isArray()).thenReturn(false);

        // When & Then
        assertThrows(ProductDataLoadException.class, () -> productService.loadData());
    }

    @Test
    void getAllProducts_WhenProductsListIsNull_ShouldThrowProductDataNotLoadedException() {
        // Given
        ReflectionTestUtils.setField(productService, "products", null);

        // When
        Flux<Product> result = productService.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectError(ProductDataNotLoadedException.class)
                .verify();
    }

    @Test
    void getAllProducts_WhenProductsListIsEmpty_ShouldThrowProductDataNotLoadedException() {
        // Given
        ReflectionTestUtils.setField(productService, "products", Collections.emptyList());

        // When
        Flux<Product> result = productService.getAllProducts();

        // Then
        StepVerifier.create(result)
                .expectError(ProductDataNotLoadedException.class)
                .verify();
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldThrowProductNotFoundException() {
        // Given
        String nonExistentId = "non-existent-id";

        // When
        Mono<Product> result = productService.getProductById(nonExistentId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof ProductNotFoundException &&
                    throwable.getMessage().contains(nonExistentId)
                )
                .verify();
    }

    @Test
    void getPriceById_WhenProductNotFound_ShouldThrowProductNotFoundException() {
        // Given
        String nonExistentId = "non-existent-id";

        // When
        Mono<Double> result = productService.getPriceById(nonExistentId);

        // Then
        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    // Removed tests for private methods as they shouldn't be tested directly
}
