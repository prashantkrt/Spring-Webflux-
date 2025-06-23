package com.mylearning.productservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${aggregator.base-url}")
    private String aggregatorBaseUrl;

    @Bean
    public WebClient aggregatorWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(aggregatorBaseUrl)
                .build();
    }
}