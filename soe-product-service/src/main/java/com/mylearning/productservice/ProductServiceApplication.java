package com.mylearning.productservice;

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
				title = "Product Service API (SOE Layer)",
				version = "1.0.0",
				description = """
                        **SOE Layer**

                        This service exposes the public-facing API endpoint `getProductDetails`,
                        acts as the client interface, and forwards requests to the CXP Aggregator Layer.
                        It handles client interaction and returns enriched product + price data.
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
				@Tag(name = "SOE", description = "Public-facing APIs for product details")
		})
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
