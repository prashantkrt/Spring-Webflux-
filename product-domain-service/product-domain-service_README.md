# Product Domain Service

This service provides access to product data including product details and price information.

## Endpoints

- `GET /api/products` — List all products
- `GET /api/products/{id}` — Get product details by ID
- `GET /api/products/{id}/price` — Get product price by ID

## How to Run

```bash
mvn clean install -DskipTests
docker build -t product-domain-service .
docker run -p 8082:8082 product-domain-service
```

## Swagger

Visit [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
