# Observability source map

[Overview](00-observability-overview.md) · [Workspace](../../README.md)

Reference: `../microservices-workouts`. Its source files remain unchanged.
The order entry point uses `/s030/api/orders/checkout`. Inventory APIs stay under
`/api/inventory` so multiple scenarios can reuse them.

| Lesson API and behavior | Implementation in this workspace |
| --- | --- |
| `POST /s030/api/orders/checkout` accepts an order | [OrderController](../../order-service/src/main/java/com/tip/ecommerce/s030_correlation/OrderController.java) and [OrderService](../../order-service/src/main/java/com/tip/ecommerce/s030_correlation/OrderService.java) |
| Inventory reservation follows acceptance | [OrderService](../../order-service/src/main/java/com/tip/ecommerce/s030_correlation/OrderService.java) |
| `POST /api/inventory/reservations` reserves items for an order | [InventoryService](../../inventory-service/src/main/java/com/tip/ecommerce/s030_correlation/InventoryService.java) |
| Reservation release restores stock | `POST /api/inventory/reservations/{id}/release` in the same inventory service |
| Read stock | `GET /api/inventory/{sku}` in [InventoryController](../../inventory-service/src/main/java/com/tip/ecommerce/s030_correlation/InventoryController.java) |
| Export application telemetry | [Order agent settings](../../order-service/src/main/resources/otel.properties) and [inventory agent settings](../../inventory-service/src/main/resources/otel.properties) |

## Service communication

[InventoryServiceClient](../../order-service/src/main/java/com/tip/ecommerce/client/InventoryServiceClient.java)
is shared within order-service. OrderService delegates reservation calls to it; the
client owns HTTP configuration and its request/response records without depending on
an S030 class. Other scenarios can inject the same client.

## Persistence responsibilities

`OrderController` delegates to `OrderService`, which saves an `Order` through Spring
Data `OrderRepository`. `InventoryController` delegates to `InventoryService`, which
uses `InventoryRepository` for stock and reservation queries. The inventory repository
uses JDBC for its conditional stock update and reservation locking. Both are repository
boundaries; services own the flow and repositories own database access.

## Implemented boundary

The example covers accepting an order and making one direct inventory-reservation
call. Each application owns an embedded H2 database. The order schema creates `orders`;
the inventory schema creates `stock` and `reservation`. Inventory's `data.sql` uses the
same product SKUs and initial quantities as the reference project. These databases
are shared across the scenarios within their owning service. Successful reservations
change stock; release requests restore it.

- [Order schema](../../order-service/src/main/resources/schema.sql)
- [Inventory schema](../../inventory-service/src/main/resources/schema.sql)
- [Inventory sample data](../../inventory-service/src/main/resources/data.sql)

Both databases live for the application's process lifetime. Restart both services
together for a fresh demo so generated order IDs and existing reservations stay aligned.

Checkout accepts the original field names `customerId`, `expectedAmount`, and `items`.
The request and response are a subset of the full checkout contract. The amount is
supplied for the exercise, rather than calculated by product-service. Profile and address
validation, checkout-key deduplication, authentication, payment, order-item snapshots,
fulfillment APIs, and durable worker retries are outside this example. A submitted
checkout creates a new order each time. The reservation transaction retains its duplicate
protection so replaying the same reservation does not deduct stock twice.

The original checkout uses a background worker. This example keeps the checkout and
reservation API paths but deliberately calls inventory synchronously for one end-to-end
observability demonstration. A successful response returns 202 with state RESERVED;
an inventory-call failure returns 503. The response waits for the reservation attempt.

The order insert commits before the HTTP call. There is no cross-service transaction,
automatic compensation, or retry. An inventory failure leaves the order PENDING; a
timeout may leave its reservation committed too. The error response carries `orderId`
so the outcome can be investigated. This example does not persist a checkout state
machine or implement payment and final confirmation.

The loopback endpoints use the `demo` tenant and direct service calls. The full reference
project's gateway and authorization contract are not reproduced here.

## Demonstration

Follow the requests in [S030](s030_logging.md), [S031](s031_metrics_traces.md), and
[S032](s032_incident_diagnosis.md). Live telemetry still needs the instructor's demo;
compilation alone does not verify delivery to Grafana.

## Logging verification

A local HTTP demonstration with the pinned Java agents verified typed JSON fields,
service identity, matching trace IDs across services, different span IDs, successful
call duration, and an ERROR record with exception details for an unavailable inventory
service. The agent's logging exporter also showed `event`, `orderId`, and `outcome` as
exported attributes. This verification used local output; AWS Loki ingestion and Grafana
queries still require a live backend demonstration.
