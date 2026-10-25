# Microservices workouts

Learn through focused ecommerce scenarios, run the applications in IntelliJ, and
practice with Postman.

| Topic | Start here |
| --- | --- |
| Observability | [Follow checkout using logs, metrics, and traces](docs/observability/00-observability-overview.md) |
| Request rate limiting | [Protect stock reads by client IP](docs/gateway-security/00-request-rate-limiting-overview.md) |
| Authentication and authorization | [Validate tokens, protect orders and call inventory as a service](docs/authentication-authorization/00-authentication-authorization-overview.md) |
| Caching | [Hibernate, local and shared caches, then architecture](docs/scaling-performance/00-caching-overview.md) |
| Distributed transactions | [Local boundaries, Saga choreography, and recovery](docs/data-consistency/00-distributed-transactions-overview.md) |

| Project | Responsibility |
| --- | --- |
| [order-service](order-service/README.md) | Accept checkout and record its result |
| [inventory-service](inventory-service/README.md) | Reserve, finalize, and release stock |
| [payment-service](payment-service/src/main/java/com/tip/ecommerce/PaymentServiceApplication.java) | Record simulated payment outcomes for choreography |
| [gateway-service](gateway-service/src/main/java/com/tip/ecommerce/GatewayServiceApplication.java) | Route requests; separate lessons demonstrate Redis rate limits and JWT validation |

The order, inventory, and payment applications each own an embedded H2 database with familiar ecommerce tables and
seed data. Observability uses synchronous checkout. Distributed transactions adds
Kafka choreography using the existing AWS broker and local databases. Configuration
and demo requests are in the relevant lessons. Request rate limiting uses the existing
AWS Redis through the user-managed tunnel.
Authentication and authorization reuse the existing Keycloak realm, with Postman
login, service-level access rules and machine credentials for inventory calls.

[Roadmap](topics.md) · [Document conventions](docs/README.md) · [Workspace rules](AGENTS.md)
