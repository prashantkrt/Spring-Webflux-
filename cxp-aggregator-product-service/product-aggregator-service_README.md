# Product Aggregator Service

This service aggregates product data from the domain layer to combine details and pricing.

## Endpoints

- `GET /api/aggregator/products` — List all products
- `GET /api/aggregator/products/{id}` — Product details
- `GET /api/aggregator/products/{id}/price` — Product price

## How to Run

```bash
mvn clean install -DskipTests
docker build -t product-aggregator-service .
docker run -p 8081:8081 product-aggregator-service
```

## Swagger

Visit [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
