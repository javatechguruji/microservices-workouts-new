# Microservices workouts — observability

Follow an accepted order into inventory reservation using logs, metrics, and traces.
Start with the [observability overview](docs/observability/00-observability-overview.md).

| Project | Responsibility |
| --- | --- |
| [order-service](order-service/README.md) | Accept checkout and request inventory reservation |
| [inventory-service](inventory-service/README.md) | Reserve stock and release reservations |

Run the applications in IntelliJ, demonstrate through Postman, and inspect console
logs or Grafana. Each service owns an embedded H2 database, reused across scenarios.
Inventory starts with the familiar product data from `microservices-workouts`.
Grafana uses the existing observability tools.

[Roadmap](topics.md) · [Implementation boundary](docs/observability/source-map.md) · [Document conventions](docs/README.md)
