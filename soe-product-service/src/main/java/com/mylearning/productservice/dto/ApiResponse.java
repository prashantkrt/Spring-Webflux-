package com.mylearning.productservice.dto;

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
public class ApiResponse<T> {

    private Instant timestamp;
    private int status;
    private String path;
    private String message;
    private T data;

    private List<ApiError> errors; ;
    private Map<String, String> fieldErrors;
}