package com.mylearning.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ProductDto", description = "Core product attributes exposed to clients")
public class ProductDto {

    @Schema(description = "Unique internal product ID", example = "P12345")
    private String productId;

    @Schema(description = "Sequence number / SKU",
            example = "SKU-001")
    private String seqNo;

    @Schema(description = "Display name shown in UI", example = "iPhone 15 Pro Max")
    private String productDisplayName;

    @Schema(description = "Brand name", example = "Apple")
    private String brandName;

    @Schema(description = "Product category", example = "Smartphone")
    private String productType;

    @Schema(description = "Operating system", example = "iOS 17")
    private String operatingSystem;

    @Schema(description = "Current price", example = "1299.99")
    private Double price;

    @Schema(description = "Colour", example = "Space Black")
    private String color;
}