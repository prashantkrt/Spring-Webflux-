package com.mylearning.productdomainservice.model;

import lombok.Data;

import java.util.List;

@Data
public class Price {
    private List<DevicePaymentPrice> devicePaymentPrice;
}