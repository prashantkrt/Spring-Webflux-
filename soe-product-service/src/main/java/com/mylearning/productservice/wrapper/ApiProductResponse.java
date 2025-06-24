package com.mylearning.productservice.wrapper;

import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiProductResponse")
public class ApiProductResponse extends ApiResponse<ProductDto> {}