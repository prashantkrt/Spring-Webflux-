package com.mylearning.productdomainservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Product Domain API",
		version = "1.0",
		description = "API for accessing product catalog and pricing"
))
public class ProductDomainServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductDomainServiceApplication.class, args);
	}

}
