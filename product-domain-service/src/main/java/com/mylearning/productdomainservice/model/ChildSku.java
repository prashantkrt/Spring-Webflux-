package com.mylearning.productdomainservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildSku {
    private Price price;
    private Color color;
}
