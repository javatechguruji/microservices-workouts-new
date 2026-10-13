# Microservices workouts

Learn through focused ecommerce scenarios, run the applications in IntelliJ, and
practice with Postman.

| Topic | Start here |
| --- | --- |
| Observability | [Follow checkout using logs, metrics, and traces](docs/observability/00-observability-overview.md) |
| Distributed transactions | [Local boundaries, Saga choreography, and recovery](docs/data-consistency/00-distributed-transactions-overview.md) |

| Project | Responsibility |
| --- | --- |
| [order-service](order-service/README.md) | Accept checkout and record its result |
| [inventory-service](inventory-service/README.md) | Reserve, finalize, and release stock |
| [payment-service](payment-service/src/main/java/com/tip/ecommerce/PaymentServiceApplication.java) | Record simulated payment outcomes for choreography |

Each application owns an embedded H2 database with familiar ecommerce tables and
seed data. Observability uses synchronous checkout. Distributed transactions adds
Kafka choreography using the existing AWS broker and local databases. Configuration
and demo requests are in the relevant lessons.

[Roadmap](topics.md) · [Document conventions](docs/README.md) · [Workspace rules](AGENTS.md)
