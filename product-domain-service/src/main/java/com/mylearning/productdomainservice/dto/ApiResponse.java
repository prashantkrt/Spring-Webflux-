package com.mylearning.productdomainservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(name = "ApiResponse", description = "Standard API response wrapper")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    @Schema(description = "Timestamp of the response")
    private Instant timestamp;

    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "Request path for debugging")
    private String path;

    @Schema(description = "Actual response data payload")
    private T data;
}