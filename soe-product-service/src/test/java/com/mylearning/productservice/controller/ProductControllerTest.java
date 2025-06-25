package com.mylearning.productservice.controller;

import com.mylearning.productservice.dto.ProductDto;
import com.mylearning.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ProductController.class)
@Import(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private WebTestClient webTestClient;

    private final String baseUrl = "/api/products";
    private final String productId = "test-product-123";
    private final ProductDto testProduct = new ProductDto(
            productId,
            "1",
            "Test Product",
            "Test Brand",
            "Smartphone",
            "Android",
            999.99,
            "Black"
    );

    @BeforeEach
    void setUp() {
        // Setup test data
        when(productService.getProductDetails(productId)).thenReturn(Mono.just(testProduct));
        when(productService.getAllProducts()).thenReturn(Flux.just(testProduct));
        when(productService.getProductPrice(productId)).thenReturn(Mono.just(999.99));
    }

    @Test
    void getProductDetails_shouldReturnProduct() {
        webTestClient.get()
                .uri(baseUrl + "/{id}/details", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.productId").isEqualTo(productId)
                .jsonPath("$.data.productDisplayName").isEqualTo(testProduct.getProductDisplayName())
                .jsonPath("$.message").exists();
    }


    @Test
    void getProductDetails_withInvalidId_shouldReturnBadRequest() {
        webTestClient.get()
                .uri(baseUrl + "/ /details")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.path").isNotEmpty()
                .jsonPath("$.timestamp").isNotEmpty()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.fieldErrors").exists();
    }

    @Test
    void getAllProducts_shouldReturnProductList() {
        webTestClient.get()
                .uri(baseUrl)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0].productId").isEqualTo(productId)
                .jsonPath("$.message").exists();
    }

    @Test
    void getProductPrice_shouldReturnPrice() {
        webTestClient.get()
                .uri(baseUrl + "/{id}/price", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isNumber()
                .jsonPath("$.message").exists();
    }

    @Test
    void getProductPrice_withInvalidId_shouldReturnBadRequest() {
        webTestClient.get()
                .uri(baseUrl + "/ /price")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.path").isNotEmpty()
                .jsonPath("$.timestamp").isNotEmpty()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.fieldErrors").exists();
    }
}
