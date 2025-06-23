package com.mylearning.productdomainservice.dto;

import com.mylearning.productdomainservice.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiSuccessResponse", description = "API response containing a list of products")
public class ApiSuccessResponse extends ApiResponse<Product> {}