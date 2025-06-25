package com.mylearning.productservice.controller;

import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = ProductControllerUpdatedTest.TestConfig.class)
@ExtendWith(SpringExtension.class)
class ProductControllerUpdatedTest {

    @Configuration
    @Import(ProductController.class)
    static class TestConfig {
        @Bean
        public ProductService productService() {
            return Mockito.mock(ProductService.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductService productService;

    private ProductDto testProduct;
    private final String baseUrl = "/api/products";

    @BeforeEach
    void setUp() {
        testProduct = ProductDto.builder()
                .productId("P12345")
                .productDisplayName("iPhone 15 Pro Max")
                .brandName("Apple")
                .productType("Smartphone")
                .price(1299.99)
                .color("Space Black")
                .build();
    }

    @Test
    void getProductDetails_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        when(productService.getProductDetails(anyString()))
                .thenReturn(Mono.just(testProduct));

        // Act & Assert
        webTestClient.get()
                .uri(baseUrl + "/p1/details")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.data.productId").isEqualTo(testProduct.getProductId())
                .jsonPath("$.data.productDisplayName").isEqualTo(testProduct.getProductDisplayName())
                .jsonPath("$.message").isNotEmpty();

        Mockito.verify(productService).getProductDetails("p1");
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        // Arrange
        when(productService.getAllProducts())
                .thenReturn(Flux.just(testProduct));

        // Act & Assert
        webTestClient.get()
                .uri(baseUrl)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.data[0].productId").isEqualTo(testProduct.getProductId())
                .jsonPath("$.data[0].productDisplayName").isEqualTo(testProduct.getProductDisplayName())
                .jsonPath("$.message").isEqualTo("All products fetched");
    }

    @Test
    void getProductPrice_ShouldReturnPrice_WhenProductExists() {
        // Arrange
        when(productService.getProductPrice(anyString()))
                .thenReturn(Mono.just(testProduct.getPrice()));

        // Act & Assert
        webTestClient.get()
                .uri(baseUrl+ "/p1/price")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.data").isEqualTo(testProduct.getPrice())
                .jsonPath("$.message").isEqualTo("Price fetched successfully");
    }

}
