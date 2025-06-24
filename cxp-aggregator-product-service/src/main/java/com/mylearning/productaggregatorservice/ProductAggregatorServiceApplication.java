package com.mylearning.productaggregatorservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Product Aggregator Service API",
                version = "1.0.0",
                description = """
                        **CXP Aggregator Layer**
                        
                        Orchestrates calls to Domain services (`product-domain-service`) to gather product
                        details and pricing, merges the results, and returns a single response to the SOE
                        layer (`product-service`).  Built with Spring Boot 3.x, WebFlux, and Resilience4j.
                        """,
                contact = @Contact(
                        name = "Prashant",
                        email = "prashantdummy@me.com",
                        url = "http://localhost:8081"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
        },
        tags = {
                @Tag(name = "Aggregator",
                        description = "Endpoints that aggregate product data and pricing")
        }
)
public class ProductAggregatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductAggregatorServiceApplication.class, args);
    }

}
