Product Micro‑Services Demo (Reactive Spring)

✨ Objective

Design and build a modern Java Spring Reactive proof‑of‑concept that clearly separates responsibilities across three back‑end layers:

Layer

Module / Port

Responsibility

SOE (System of Engagement)

product-service (:8080)

Public façade that exposes a single `` endpoint and orchestrates requests to the Aggregator layer.

CXP Aggregator

product-aggregator-service (:8081)

Combines data coming from one or more domain services.

Domain

product-domain-service (:8082)

Owns product data, exposes CRUD‑like APIs (/api/products, /api/products/{id}, /price).

The app must be fully reactive, follow SOLID design principles, and demonstrate production‑grade practices (logging, tracing, testing, CI/CD, Docker, Swagger, JaCoCo, etc.).

🛠  Technology Stack

Java 21

Spring Boot 3.x + Spring WebFlux

GraphQL (experimental ‑ optional)

Swagger / OpenAPI 3 for API docs

Lombok (boiler‑plate reduction)

JUnit 5 + Mockito for tests – coverage measured with JaCoCo

Docker / Docker Compose for containerisation

Resilience4j (Circuit Breaker, Retry)

OpenTelemetry for distributed tracing

SLF4J + Logback for centralised JSON logging

🗂  Project Structure (non‑monorepo)

📦 product-domain-service
 ├── pom.xml
 └── src/main/…/com/example/domain
     └── resources/data/products.json  ← sample data store

📦 product-aggregator-service
 ├── pom.xml
 └── src/main/…/com/example/aggregator

📦 product-service               (SOE façade)
 ├── pom.xml
 └── src/main/…/com/example/productservice

Each folder is an independent Maven project that can be built & run on its own.

🔗 REST End‑points

1️⃣ SOE product-service  (http://localhost:8080)

Method

Path

Description

GET

/api/products/{id}/details

Returns combined product & price details

GET

/api/products/{id}/price

Price only (pass‑through)

2️⃣ Aggregator product-aggregator-service  (http://localhost:8081)

Method

Path

Description

GET

/api/aggregator/products

List all products

GET

/api/aggregator/products/{id}

Product details

GET

/api/aggregator/products/{id}/price

Price only

3️⃣ Domain product-domain-service  (http://localhost:8082)

Method

Path

Description

GET

/api/products

List all products (reads products.json)

GET

/api/products/{id}

Product by id

GET

/api/products/{id}/price

Price by id

📝 Example:  curl http://localhost:8080/api/products/dev21412086/details

🗄  Data Source

products.json contains 24 sample records (smart‑phone catalogue). Only the fields shown below are persisted:

{
  "productId": "dev21412086",
  "productDisplayName": "iPhone 16",
  "brandName": "Apple",
  "price": 23.05,
  "color": "Black"
}

(See full file under product-domain-service/src/main/resources/data/)

🚀 How to Run Locally

# 1. Build everything
mvn -pl \*

# 2. Start Domain Service
docker run -p 8082:8082 product-domain-service

# 3. Start Aggregator Service
mvn -f product-aggregator-service/pom.xml spring-boot:run

# 4. Start SOE Service
mvn -f product-service/pom.xml spring-boot:run

Or simply docker-compose up --build from the repo root to spin up all three containers.

📜 Swagger / OpenAPI

SOE – http://localhost:8080/swagger-ui/index.html

Aggregator – http://localhost:8081/swagger-ui/index.html

Domain – http://localhost:8082/swagger-ui/index.html

🧪 Testing & Code Coverage

mvn test jacoco:report

Unit & integration tests stitched with JUnit 5 & Mockito.

Branch & instruction coverage enforced (≥ 80 %) – DTOs are excluded with <excludes> in jacoco-maven-plugin.

🔒 Error Handling & Resilience

**Global **`` converts exceptions → structured ApiError JSON.

Resilience4j Circuit Breaker & Retry around WebClient calls in Aggregator & SOE layers.

Distributed tracing via OpenTelemetry – every request shares the same trace‑id across layers.

🏗  CI/CD & Docker

Dockerfile in each service creates a tini‑based slim image.

Optional GitHub / GitLab CI pipeline:

Build → Test → JaCoCo → Docker push → Deploy (K8s / Kind).

👣 Roadmap / Improvements

Add GraphQL gateway for flexible querying.

Switch to R2DBC + Postgres when persistence is required.

Integrate Prometheus / Grafana for metrics.

🤝 Contributing

Fork & clone the repo

Create feature branch git checkout -b feat/<name>

Commit with conventional commit messages

Open a Pull Request – CI must pass

© License

MIT

