package com.mylearning.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    @NotBlank(message = "Product ID must not be blank")
    private String id;

    @NotBlank(message = "Product name must not be blank")
    private String name;

    @NotNull(message = "Price must be provided")
    @Positive(message = "Price must be positive")
    private Double price;
}