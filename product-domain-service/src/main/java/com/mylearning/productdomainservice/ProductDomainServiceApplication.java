package com.mylearning.productdomainservice;

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
                title = "Product Domain Service API",
                version = "1.0.0",
                description = """
                        **Domain Layer**
                        
                        This service provides APIs to access core product details (`getProduct`)
                        and pricing information (`getPrice`). It reads from a shared JSON data source
                        and serves as the foundational data layer for the Aggregator service.
                        """,
                contact = @Contact(
                        name = "Prashant",
                        email = "prashantdummy@me.com",
                        url = "http://localhost:8080"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Environment"),
        },
        tags = {
                @Tag(name = "Domain", description = "APIs for product and pricing data")
        })
public class ProductDomainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductDomainServiceApplication.class, args);
    }

}
