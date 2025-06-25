# Design Patterns & SOLID Principles Summary

## Design Patterns Used

| Pattern | Description | How It's Used |
|--------|-------------|----------------|
| **Factory Pattern** | Creates objects without specifying the exact class. | ObjectMapper or WebClient Beans are configured via factory-style `@Configuration` or builder patterns. |
| **Singleton Pattern** | Restricts class to one instance. | `@Service`, `@Component`, and `@Configuration` beans are singletons in Spring. Used for services like `ProductService`, `ProductAggregatorServiceImpl`. |
| **Observer Pattern** | Reacts to state changes or events. | Project Reactor (`Mono`, `Flux`) is inherently reactive — your subscriber reacts to upstream data changes. |
| **Builder Pattern** | Used to build complex objects in a step-by-step way. | WebClient, ObjectMapper, and Response objects like `ApiResponse<T>` use fluent builder-style APIs. |
| **Proxy Pattern (Implicit)** | Adds an additional layer to manage access. | CircuitBreaker and Retry from Resilience4j wrap WebClient calls, acting like proxies. |

---

## SOLID Principles Applied

| Principle | Description | Application in Code |
|----------|-------------|----------------------|
| **S - Single Responsibility Principle** | Each class should have one reason to change. | Each service (SOE, Aggregator, Domain) has its own clearly defined job. Controllers only handle web requests. Services handle business logic. |
| **O - Open/Closed Principle** | Classes should be open for extension but closed for modification. | You can add new APIs or fallback logic without modifying core service logic using `onErrorResume`, fallback methods. |
| **L - Liskov Substitution Principle** | Subtypes must be substitutable for their base types. | Interfaces like `ProductAggregatorService` and `ProductService` have clear implementations, and clients depend only on interfaces. |
| **I - Interface Segregation Principle** | No client should be forced to depend on methods it does not use. | Services expose specific interfaces, e.g., `getProduct`, `getPrice`, instead of bloated ones. |
| **D - Dependency Inversion Principle** | Depend on abstractions, not concrete classes. | Spring handles this via constructor injection. `WebClient`, `CircuitBreakerRegistry`, and `RetryRegistry` are injected as abstractions. |

---

## Microservices Layer Responsibilities

### SOE Layer (`product-service`)
- Exposes `getProductDetails` API to clients.
- Forwards calls to the Aggregator Layer using **WebClient**.
- Uses `ApiResponse<T>` as a generic wrapper.

### Aggregator Layer (`product-aggregator-service`)
- Consumes `getProduct` and `getPrice` from Domain Layer.
- Applies **Circuit Breaker** & **Retry** with Resilience4j.
- Handles fallback scenarios & logs cleanly.
- Aggregates results via `Mono.zip`.

### Domain Layer (`product-domain-service`)
- Provides `getProduct` & `getPrice` APIs.
- Reads from static JSON data source.

---

## Testing & JaCoCo
- JUnit + Mockito for unit & integration tests.
- Global exception handling tested via `@RestControllerAdvice`.
- JaCoCo excludes  
  `com.mylearning.productservice.wrapper` :

```xml
<excludes>
  <exclude>com/mylearning/productservice/wrapper/**</exclude>
</excludes>
```

---

## Additional Tools Integrated
| Tool | Purpose |
|------|---------|
| **Swagger/OpenAPI** | API contract & documentation |
| **Docker** | Containerization for each service |
| **Spring DevTools** | Hot reload during development |
| **Micrometer + OpenTelemetry** | Metrics & tracing |
| **Central Logging** | Unified structured logs |
| **Global Exception Handler** | Standardized error responses |

---

