package com.mylearning.productaggregatorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDto {
    private String productId;
    private String seqNo;
    private String productDisplayName;
    private String brandName;
    private String productType;
    private String operatingSystem;
    private Double price;
    private String color;
}