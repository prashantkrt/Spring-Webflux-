package com.mylearning.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ApiError", description = "Represents a single business or system error")
public class ApiError {

    @Schema(description = "Application-specific error code", example = "AGGREGATOR_DOWN")
    private String code;

    @Schema(description = "Human-readable error message", example = "Aggregator service not reachable")
    private String message;

    @Schema(
            description = "Field-level validation messages (if applicable). " +
                    "Key = field name, Value = validation message",
            example = "{\"productId\": \"must not be blank\"}"
    )
    private Map<String, String> fieldErrors;

    //for structured downstream info
    @Schema(
            description = "Structured error details",
            example = "{\"status\": 503, \"error\": \"Service Unavailable\", " +
                    "\"path\": \"/products/123\", \"requestId\": \"req-123\", " +
                    "\"timestamp\": \"2025-06-24T12:34:56Z\"}")
    private Map<String, Object> details;
}