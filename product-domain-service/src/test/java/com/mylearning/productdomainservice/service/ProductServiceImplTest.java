package com.mylearning.productdomainservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.productdomainservice.exception.InvalidProductDataException;
import com.mylearning.productdomainservice.exception.ProductNotFoundException;
import com.mylearning.productdomainservice.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode rootNode;

    @Mock
    private JsonNode dataNode;

    @Mock
    private JsonNode gridWallNode;

    @Mock
    private JsonNode productListNode;

    @Mock
    private JsonNode childSkusNode;

    @Mock
    private JsonNode priceNode;

    @Mock
    private JsonNode devicePayArrNode;

    @Mock
    private JsonNode originalPriceNode;

    @Mock
    private JsonNode colorNode;

    @Mock
    private JsonNode displayNameNode;

    @InjectMocks
    private ProductServiceImpl productService;

    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        // Setup mock products with all required fields
        Product product1 = new Product(
            "p1", "1", "iPhone 13", "Apple", "Smartphone", "iOS", 999.99, "Black"
        );
        
        Product product2 = new Product(
            "p2", "2", "Galaxy S21", "Samsung", "Smartphone", "Android", 899.99, "Silver"
        );
        
        mockProducts = List.of(product1, product2);
    }

    @Test
    void loadData_ShouldLoadProductsSuccessfully() throws Exception {
        // Given
        String jsonContent = "{\"data\":{\"gridWall\":{\"productList\":[]}}}";
        
        // Create a test instance
        ProductServiceImpl testService = new ProductServiceImpl();
        
        // Mock the ObjectMapper behavior
        when(objectMapper.readTree(any(InputStream.class)))
            .thenAnswer(invocation -> {
                // Read the input stream and parse it as JSON
                try (InputStream is = new ByteArrayInputStream(jsonContent.getBytes())) {
                    return new ObjectMapper().readTree(is);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to parse test JSON", e);
                }
            });
        
        // Inject mock ObjectMapper
        ReflectionTestUtils.setField(testService, "mapper", objectMapper);
        
        // When
        testService.loadData();
        
        // Then - No exception should be thrown
        assertNotNull(testService);
        
        // Verify the mock was called
        verify(objectMapper).readTree(any(InputStream.class));
    }

    @Test
    void loadData_WhenFileNotFound_ShouldThrowException() throws IOException {
        // Given
        ProductServiceImpl service = new ProductServiceImpl();
        
        // Mock the ObjectMapper to throw IOException when reading the stream
        when(objectMapper.readTree(any(InputStream.class)))
            .thenThrow(new IllegalStateException("File not found"));
        
        // Inject mock ObjectMapper
        ReflectionTestUtils.setField(service, "mapper", objectMapper);

        // When & Then
        assertThrows(InvalidProductDataException.class, service::loadData);
    }

    
    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        setTestProducts(mockProducts);
        
        // When
        Flux<Product> result = productService.getAllProducts();
        
        // Then
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Given
        setTestProducts(mockProducts);
        String productId = "p1";
        
        // When
        Mono<Product> result = productService.getProductById(productId);
        
        // Then
        StepVerifier.create(result)
                .expectNextMatches(product -> 
                    product.getProductId().equals(productId) && 
                    product.getPrice() == 999.99 &&
                    product.getColor().equals("Black")
                )
                .verifyComplete();
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        // Given
        setTestProducts(mockProducts);
        String invalidId = "non-existent-id";
        
        // When
        Mono<Product> result = productService.getProductById(invalidId);
        
        // Then
        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();
    }
    
    @Test
    void getPriceById_WithValidId_ShouldReturnPrice() {
        // Given
        setTestProducts(mockProducts);
        String productId = "p1";
        
        // When
        Mono<Double> result = productService.getPriceById(productId);
        
        // Then
        StepVerifier.create(result)
                .expectNext(999.99)
                .verifyComplete();
    }
    
    @Test
    void getPriceById_WithInvalidId_ShouldThrowException() {
        // Given
        setTestProducts(mockProducts);
        String invalidId = "non-existent-id";
        
        // When
        Mono<Double> result = productService.getPriceById(invalidId);
        
        // Then
        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();
    }
    
    // Helper method to set test products using reflection
    private void setTestProducts(List<Product> products) {
        ReflectionTestUtils.setField(productService, "products", products);
    }
}
