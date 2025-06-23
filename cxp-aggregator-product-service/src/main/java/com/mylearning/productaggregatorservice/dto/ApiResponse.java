package com.mylearning.productaggregatorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApiResponse<T>
{
    private boolean apiSuccess;
    private Instant timeStamp;
    private T data;                      // happy path
    private List<ApiError> errors;       // unhappy path
}
