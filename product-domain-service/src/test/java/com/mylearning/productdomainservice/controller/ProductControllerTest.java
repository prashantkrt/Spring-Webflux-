package com.mylearning.productdomainservice.controller;

import com.mylearning.productdomainservice.model.Product;
import com.mylearning.productdomainservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private WebTestClient webTestClient;
    private List<Product> testProducts;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(productController).build();

        testProduct = new Product(
                "1", "1", "Test Product", "Test Brand",
                "SMARTPHONE", "ANDROID", 999.99, "BLACK"
        );
        testProducts = Arrays.asList(
                testProduct,
                new Product("2", "2", "Another Product", "Test Brand",
                        "TABLET", "IOS", 1299.99, "SILVER")
        );
    }

    @Test
    @DisplayName("GET /api/products - Success")
    void getAllProducts_WhenProductsExist_ShouldReturnProducts() {
        // Given
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));

        // When & Then
        webTestClient.get().uri("/api/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Product.class)
                .hasSize(2)
                .contains(testProduct);

        // Verify
        verify(productService).getAllProducts();
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Success")
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        String productId = "1";
        when(productService.getProductById(productId)).thenReturn(Mono.just(testProduct));

        // When & Then
        webTestClient.get().uri("/api/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Product.class)
                .isEqualTo(testProduct);

        // Verify
        verify(productService).getProductById(productId);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Not Found")
    void getProductById_WhenProductNotExists_ShouldReturnEmpty() {
        // Given
        String invalidId = "999";
        when(productService.getProductById(invalidId)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get().uri("/api/products/{id}", invalidId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        // Verify
        verify(productService).getProductById(invalidId);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("GET /api/products/{id}/price - Success")
    void getProductPrice_WhenProductExists_ShouldReturnPrice() {
        // Given
        String productId = "1";
        double expectedPrice = 999.99;
        when(productService.getPriceById(productId)).thenReturn(Mono.just(expectedPrice));

        // When & Then
        webTestClient.get().uri("/api/products/{id}/price", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Double.class)
                .isEqualTo(expectedPrice);

        // Verify
        verify(productService).getPriceById(productId);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("GET /api/products/{id}/price - Not Found")
    void getProductPrice_WhenProductNotExists_ShouldReturnEmpty() {
        // Given
        String invalidId = "999";
        when(productService.getPriceById(invalidId)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get().uri("/api/products/{id}/price", invalidId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        // Verify
        verify(productService).getPriceById(invalidId);
        verifyNoMoreInteractions(productService);
    }
    
    @Test
    @DisplayName("GET /api/products - Empty List")
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Given
        when(productService.getAllProducts()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get().uri("/api/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Product.class)
                .hasSize(0);

        // Verify
        verify(productService).getAllProducts();
        verifyNoMoreInteractions(productService);
    }
    
    @Test
    @DisplayName("GET /api/products - Invalid Accept Header")
    void getAllProducts_WithInvalidAcceptHeader_ShouldReturnInternalServerError() {
        // When & Then - Default Spring behavior for unsupported media type
        webTestClient.get().uri("/api/products")
                .accept(MediaType.APPLICATION_XML) // Unsupported media type
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
