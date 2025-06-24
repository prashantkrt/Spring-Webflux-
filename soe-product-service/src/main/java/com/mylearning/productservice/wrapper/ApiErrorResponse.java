package com.mylearning.productservice.wrapper;

import com.mylearning.productservice.dto.ApiError;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "ApiErrorResponse",
        description = "Standard error response body"
)
public class ApiErrorResponse {

    @Schema(description = "When the error occurred", example = "2025-06-24T12:34:56Z")
    private Instant timestamp;

    @Schema(description = "HTTP status")
    private int status;

    @Schema(description = "Request path")
    private String path;

    @Schema(description = "Brief error message")
    private String message;

    @Schema(description = "Business or system errors")
    private List<ApiError> errors;

    @Schema(description = "Validation errors per field")
    private Map<String, String> fieldErrors;
}