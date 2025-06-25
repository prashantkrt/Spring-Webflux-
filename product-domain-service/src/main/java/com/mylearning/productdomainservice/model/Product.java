package com.mylearning.productdomainservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(name = "Product", description = "Product details")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Schema(description = "Unique product ID")
    private String productId;

    @Schema(description = "Sequence number")
    private String seqNo;

    @Schema(description = "Display name of the product")
    private String productDisplayName;

    @Schema(description = "Brand name")
    private String brandName;

    @Schema(description = "Type of the product")
    private String productType;

    @Schema(description = "Operating system")
    private String operatingSystem;

    @Schema(description = "Price of the product")
    private Double price;

    @Schema(description = "Color of the product")
    private String color;
}