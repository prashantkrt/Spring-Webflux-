package com.mylearning.productservice.wrapper;

import com.mylearning.productservice.dto.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiPriceResponse")
public class ApiPriceResponse extends ApiResponse<Double> {}