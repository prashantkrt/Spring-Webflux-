package com.mylearning.productservice.wrapper;

import com.mylearning.productservice.dto.ApiResponse;
import com.mylearning.productservice.dto.ProductDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ApiProductListResponse")
public class ApiProductListResponse extends ApiResponse<List<ProductDto>> {}