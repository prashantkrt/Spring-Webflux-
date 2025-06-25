# Product SOE Service

This service acts as a system-of-engagement API layer combining product information and exposing it externally.

## Endpoints

- `GET /api/products/{id}/details` — Combined product and price details
- `GET /api/products/{id}/price` — Price only (delegated)

## How to Run

```bash
mvn clean install -DskipTests
docker build -t product-service .
docker run -p 8080:8080 product-service
```

## Swagger

Visit [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
