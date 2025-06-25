package com.mylearning.productdomainservice.controller;

import com.mylearning.productdomainservice.dto.ApiErrorResponse;
import com.mylearning.productdomainservice.model.Product;
import com.mylearning.productdomainservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(("/api/products"))
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "List all products",
            description = "Returns the full product catalog."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products fetched successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Product.class)))),
            @ApiResponse(responseCode = "404", description = "No products available",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public Flux<Product> getAllProducts() {
        log.info("Received request to fetch all products");
        return productService.getAllProducts()
                .doOnNext(p -> log.info("Product ID: {}", p.getProductId()));
    }

    @Operation(summary = "List all products", description = "Returns the entire product catalog as a reactive stream")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product list",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Mono<Product> getProductById(@Parameter(description = "Product ID", required = true) @PathVariable String id) {
        log.info("Fetching product with ID: {}", id);
        return productService.getProductById(id);
    }

    @Operation(summary = "Find product by ID", description = "Returns a single product. 404 if not found")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}/price")
    public Mono<Double> getProductPrice(@Parameter(description = "Product ID", required = true) @PathVariable String id) {
        log.info("Fetching price for product ID: {}", id);
        return productService.getPriceById(id);
    }
}
