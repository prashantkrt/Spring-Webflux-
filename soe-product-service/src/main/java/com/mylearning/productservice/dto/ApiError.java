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
}