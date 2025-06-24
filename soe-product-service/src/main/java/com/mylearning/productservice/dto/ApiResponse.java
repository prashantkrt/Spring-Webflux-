package com.mylearning.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ApiResponse", description = "Standard envelope for all REST responses")
public class ApiResponse<T> {

    @Schema(description = "Timestamp (ISO-8601) when the response was generated",
            example = "2025-06-24T12:34:56Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Schema(description = "Request path for easy tracing", example = "/api/products/123/details")
    private String path;

    @Schema(description = "Short description of the result", example = "Product fetched")
    private String message;

    @Schema(description = "Payload data; null in error cases")
    private T data;

    @Schema(description = "High-level errors unrelated to specific fields")
    private List<ApiError> errors;

    @Schema(
            description = "Validation errors keyed by field name; " +
                    "present only for 4xx responses where input fails validation"
    )
    private Map<String, String> fieldErrors;
}