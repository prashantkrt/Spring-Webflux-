
# Product Micro‑Services Demo (Reactive Spring)

## Objective

Design and build a modern Java Spring Reactive proof‑of‑concept that clearly separates responsibilities across three back‑end layers:

| Layer                          | Module / Port                          | Responsibility                                                                                     |
| ------------------------------ | -------------------------------------- | -------------------------------------------------------------------------------------------------- |
| SOE (System of Engagement)     | `product-service` (:8080)              | Public façade that exposes a single endpoint and orchestrates requests to the Aggregator layer.   |
| CXP Aggregator                 | `product-aggregator-service` (:8081)   | Combines data coming from one or more domain services.                                             |
| Domain                         | `product-domain-service` (:8082)       | Owns product data, exposes CRUD-like APIs (/api/products, /api/products/{id}, /price).            |

The app must be fully reactive, follow SOLID design principles, and demonstrate production-grade practices (logging, tracing, testing, Docker, Swagger, JaCoCo, etc.).

---

## Technology Stack

- Java 21
- Spring Boot 3.x + Spring WebFlux
- Swagger / OpenAPI 3 for API docs
- Lombok
- JUnit 5 + Mockito + JaCoCo
- Docker / Docker Compose
- Resilience4j
- OpenTelemetry
- SLF4J + Logback

---

## Project Structure

```text
product-domain-service
 ├── pom.xml
 └── src/main/.../com/example/domain
     └── resources/data/products.json

product-aggregator-service
 ├── pom.xml
 └── src/main/.../com/example/aggregator

product-service
 ├── pom.xml
 └── src/main/.../com/example/productservice
```

Each folder is an independent Maven project.

---

## REST Endpoints

### product-service (http://localhost:8080)

- `GET /api/products/{id}/details` - Returns combined product & price details
- `GET /api/products/{id}/price` - Price only

### product-aggregator-service (http://localhost:8081)

- `GET /api/aggregator/products` - List all products
- `GET /api/aggregator/products/{id}` - Product details
- `GET /api/aggregator/products/{id}/price` - Price only

### product-domain-service (http://localhost:8082)

- `GET /api/products` - List all products (reads products.json)
- `GET /api/products/{id}` - Product by id
- `GET /api/products/{id}/price` - Price by id

Example:
```bash
curl http://localhost:8080/api/products/dev21412086/details
```

---

## Data Source

Example output from the data source:

```json
{
  "productId": "dev21412086",
  "seqNo": "1",
  "productDisplayName": "iPhone 16",
  "brandName": "Apple",
  "productType": "device",
  "operatingSystem": "Apple iOS",
  "price": 23.05,
  "color": "Black"
}
```

---

## How to Run Locally

```bash
# Build all
mvn clean install -DskipTests

# Build Docker images
docker build -t product-domain-service ./product-domain-service
docker build -t product-aggregator-service ./product-aggregator-service
docker build -t product-service ./product-service

# Run services
docker run -p 8082:8082 product-domain-service
docker run -p 8081:8081 product-aggregator-service
docker run -p 8080:8080 product-service

# Or use Docker Compose
docker-compose up --build
```

---

## Swagger / OpenAPI

- http://localhost:8080/swagger-ui/index.html (SOE)
- http://localhost:8081/swagger-ui/index.html (Aggregator)
- http://localhost:8082/swagger-ui/index.html (Domain)

---

## Testing & Code Coverage

```bash
mvn test jacoco:report
```

- Unit & integration tests with JUnit 5 & Mockito
- DTOs excluded using `<excludes>` in JaCoCo

---

## Error Handling & Resilience

- Global `@ControllerAdvice` returns structured `ApiError` JSON
- Resilience4j CircuitBreaker + Retry for WebClient
- OpenTelemetry for trace propagation
