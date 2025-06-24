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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class)
@Import(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private WebTestClient webTestClient;

    private final String BASE_URL = "/api/products";
    private final String PRODUCT_ID = "test-product-123";
    private final ProductDto testProduct = new ProductDto(
            PRODUCT_ID,
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
        when(productService.getProductDetails(PRODUCT_ID)).thenReturn(Mono.just(testProduct));
        when(productService.getAllProducts()).thenReturn(Flux.just(testProduct));
        when(productService.getProductPrice(PRODUCT_ID)).thenReturn(Mono.just(999.99));
    }

    @Test
    void getProductDetails_shouldReturnProduct() {
        webTestClient.get()
                .uri(BASE_URL + "/{id}/details", PRODUCT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.productId").isEqualTo(PRODUCT_ID)
                .jsonPath("$.data.productDisplayName").isEqualTo(testProduct.getProductDisplayName())
                .jsonPath("$.message").exists();
    }

    @Test
    void getProductDetails_withInvalidId_shouldReturnBadRequest() {
        webTestClient.get()
                .uri(BASE_URL + "/ /details")
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
                .uri(BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0].productId").isEqualTo(PRODUCT_ID)
                .jsonPath("$.message").exists();
    }

    @Test
    void getProductPrice_shouldReturnPrice() {
        webTestClient.get()
                .uri(BASE_URL + "/{id}/price", PRODUCT_ID)
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
                .uri(BASE_URL + "/ /price")
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
