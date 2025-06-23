package com.mylearning.productdomainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse", description = "API response when no products are found")
public class ApiErrorResponse extends ApiResponse<Void> {
}