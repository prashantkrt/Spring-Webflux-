package com.mylearning.productaggregatorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiError<T>
{
    private String code;
    private String message;
    private List<FieldValidationError> fieldErrors;
}
