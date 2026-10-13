# S017 — Saga choreography and compensation

[Overview](00-distributed-transactions-overview.md) · [Roadmap](../../topics.md#s017) · [Order](../../order-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/) · [Inventory](../../inventory-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/) · [Payment](../../payment-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/) · [Next: recovery](s018_saga_recovery.md)

## 1. The story and problem

A customer orders BOOK-1 for ₹22. Inventory reserves the book, but payment is declined.
The customer should see a cancelled order and the book should become available again.

A local rollback cannot undo another service's committed reservation. We use a
**Saga**: a sequence of local transactions, with a compensating transaction when a
later step fails. Here compensation releases the stock.

In **choreography**, services react to events rather than receiving instructions from
a central coordinator. `PAYMENT_FAILED` reports a result. Inventory decides to release
its reservation, and order-service records progress.

| Event | Service that reacts | Local change and next event |
| --- | --- | --- |
| `ORDER_CREATED` | Inventory | Reserve stock → `INVENTORY_RESERVED`; unavailable stock → `INVENTORY_REJECTED` |
| `INVENTORY_RESERVED` | Payment | Save SUCCESS → `PAYMENT_COMPLETED`; save FAILED → `PAYMENT_FAILED` |
| `PAYMENT_COMPLETED` | Inventory | Finalize reservation → `INVENTORY_COMMITTED` |
| `PAYMENT_FAILED` | Inventory and order | Inventory restores stock → `INVENTORY_RELEASED`; order marks COMPENSATING |
| `INVENTORY_COMMITTED` | Order | Mark CONFIRMED |
| `INVENTORY_RELEASED` or `INVENTORY_REJECTED` | Order | Mark CANCELLED |

The [overview diagram](00-distributed-transactions-overview.md#follow-checkout) shows
this complete flow and when Postman gets its response.

## 2. Our implementation

### Step 1 — Add Kafka support to each service

The [order](../../order-service/pom.xml), [inventory](../../inventory-service/pom.xml),
and [payment](../../payment-service/pom.xml) POMs contain:

```xml
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>
```

Spring Boot 3.5.9 manages the version. The dependency supplies the Kafka client and
Spring integration. Profile `s017` activates our listeners, topic bean, and publisher.
`@KafkaListener` registers each listener; `@EnableScheduling` activates the outbox
publisher. No Saga framework or orchestration engine is needed.

Order uses Spring Data JPA. Inventory and payment use Spring JDBC. Each service has
its own H2 database and transaction manager.

### Step 2 — Save the order and outgoing event together

In [order SagaService](../../order-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/SagaService.java),
`checkout()` is transactional. Excerpt after validation and duplicate-request checking:

```java
var order = orders.save(new SagaOrder(request.customerId(), normalized.expectedAmount(), requestKey, payload));
// The same H2 transaction stores both the order and the event to deliver later.
events.append(new SagaEvent(UUID.randomUUID().toString(), "ORDER_CREATED", order.getId(),
    request.customerId(), normalized.expectedAmount(), normalized.items(), null));
return view(order); // Accepted, but the Saga is not yet finished.
```

The **outbox** is a table of outgoing events. Saving it in the same transaction avoids
committing the order and then losing its event if the process stops. A scheduled
publisher sends committed rows to Kafka. Delivery details are in [S018](s018_saga_recovery.md#2-our-implementation).

`Idempotency-Key` identifies a checkout attempt. Reusing the same key and request
returns the same order; changing the request with that key returns 409. It prevents a
Postman retry from creating another order. The key has a unique database constraint.
Concurrent requests for one new key can produce a 409; retry the same key and body.

### Step 3 — Reserve inventory in its own transaction

[Inventory SagaService](../../inventory-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/SagaService.java)
reacts to `ORDER_CREATED`. Its transactional method calls the
[repository](../../inventory-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/SagaInventoryRepository.java)
to lock and check all requested stock rows before deducting anything.

```java
boolean reserved = inventory.reserve(event);
// Commit either the reservation and its event, or a rejection event with no stock changes.
events.append(event.next(reserved ? "INVENTORY_RESERVED" : "INVENTORY_REJECTED",
    reserved ? null : "Insufficient stock"));
```

Items arrive sorted by SKU from checkout. Locking in that order reduces conflicting
lock acquisition. The familiar `stock` and `reservation` tables hold available
quantities and the reservation's state. A reservation reduces available stock once;
marking it COMMITTED does not deduct it again.

### Step 4 — Record the payment result

[Payment SagaService](../../payment-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/SagaService.java)
reacts to `INVENTORY_RESERVED`:

```java
// The local simulator chooses a result; no bank is called.
boolean success = result == DemoResult.SUCCESS;
payments.record(event, success ? "SUCCESS" : "FAILED");
// Payment and its result event commit in one payment DB transaction.
events.append(event.next(success ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED",
    success ? null : "Payment declined"));
```

The `payment` table keeps the familiar order ID, amount, status, and creation time.
The demo trusts checkout's supplied amount. Real pricing and a real payment provider
would have their own validation and outcome lookup. This lesson demonstrates the
local payment record and its effect on the Saga.

### Step 5 — Compensate and finish the order

On `PAYMENT_FAILED`, inventory runs a new local transaction. Excerpt:

```java
// Restore quantities from the stored reservation, then mark it RELEASED.
inventory.finish(event.orderId(), target);
// Tell order-service about the committed release through the outbox.
events.append(event.next(target.equals("RELEASED") ? "INVENTORY_RELEASED" : "INVENTORY_COMMITTED", event.reason()));
```

Here `target` is RELEASED for payment failure and COMMITTED for success. The inbox
marker, reservation update, stock restoration, and outgoing event share the transaction.
A duplicate delivery cannot restore stock twice.

Order first marks COMPENSATING on `PAYMENT_FAILED`. It waits for `INVENTORY_RELEASED`
before marking CANCELLED. If the release cannot complete, the customer is not shown
a false final result. Successful payment follows the corresponding COMMITTED →
CONFIRMED path.

## 3. Configure IntelliJ

### Databases and familiar data

Use Java 17 and reload the root Maven project, which includes all three applications.
Profile `s017` loads these complete configuration files:

- [Order application-s017.yml](../../order-service/src/main/resources/application-s017.yml)
- [Inventory application-s017.yml](../../inventory-service/src/main/resources/application-s017.yml)
- [Payment application-s017.yml](../../payment-service/src/main/resources/application-s017.yml)

Each uses a file-backed embedded H2 URL of this form:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:${SAGA_DATA_DIR:./data}/${SAGA_RUN_ID}/order-service-db;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE
  sql:
    init:
      mode: always
      schema-locations: classpath:s017-schema.sql
      data-locations: optional:classpath:s017-data.sql
```

Inventory and payment use their own database filenames. The schema scripts create
missing tables. Inventory's [seed script](../../inventory-service/src/main/resources/s017-data.sql)
inserts missing products, including BOOK-1 with 100 available. Restarting does not
reset existing stock, orders, payments, or delivery progress.

The order [persistence configuration](../../order-service/src/main/java/com/tip/ecommerce/config/PersistenceConfiguration.java)
selects the Saga entity/repository for this profile. The normal observability profile
keeps its original entity and in-memory database. The two lessons' checkout controllers
and inventory mutation routes do not run together in profile `s017`.

### Kafka connection and event delivery

Use your existing AWS Kafka and tunnel. The old workspace's tunnel forwards port
9092. Set `KAFKA_BOOTSTRAP_SERVERS` only if your reachable bootstrap address differs
from `localhost:9092`. Kafka's advertised broker address must also be reachable through
the tunnel; a reachable bootstrap port alone is insufficient.

Each profile supplies this essential consumer configuration:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false
      auto-offset-reset: earliest
      max-poll-records: 1
    listener:
      ack-mode: record
      concurrency: 1
```

The full files also configure string serializers, `acks: all`, and producer
idempotence. Events are JSON strings, decoded by Jackson in
[SagaListener](../../order-service/src/main/java/com/tip/ecommerce/s017_saga_compensation/SagaListener.java).
Each service uses a separate consumer group, such as `payment-service-class01`.
Sharing one group across services would divide events between them instead of giving
each service its own copy.

`SagaKafkaConfiguration` declares `workouts.s017.<run-id>.events`, with one partition
and one replica for the existing single-broker demo. The application needs permission
to create this dedicated topic. It does not publish into the old project's topics.
Replication factor 1 does not provide broker redundancy.

### Complete run selections

Set the **working directory to this workspace root** for every configuration.
Use a new run ID, such as `class01`, for a fresh class demo. Use only letters,
numbers, and hyphens. All three applications must use the same run ID.

| Start order | Main class | Active profiles | Environment variables | VM options |
| --- | --- | --- | --- | --- |
| 1 | `com.tip.ecommerce.InventoryServiceApplication` | `s017` | `SAGA_RUN_ID=class01` | empty |
| 2 | `com.tip.ecommerce.PaymentServiceApplication` | `s017` | `SAGA_RUN_ID=class01` | empty |
| 3 | `com.tip.ecommerce.OrderServiceApplication` | `s017` | `SAGA_RUN_ID=class01` | empty |

Use the matching Maven module's classpath. Ports are 9105, 9103, and 9101. Wait for
startup and Kafka partition assignment in the consoles. The telemetry agent is
optional; this lesson uses Postman and console evidence.

**Restart:** keep the run ID and working directory to resume unfinished work.
**Fresh exercise:** stop all three, choose a new run ID in all three, then start them.
The new run ID selects fresh H2 files, a fresh Kafka topic, and fresh consumer groups.
Do not reset one database while retaining its old Kafka progress. Old demo files and
topics remain available until you deliberately clean them up.

## 4. Demonstrate checkout

Run Testing Scenario 1, then Scenario 2. Use the returned order ID in the read requests.
The examples show order 1 from a fresh run. Postman's 202 response is acceptance;
repeat the order GET until the final state appears. The default publisher checks its
outbox every second, so completion is asynchronous rather than an exact timing promise.

Read-only inventory and payment requests below inspect each participant's committed
result. All business changes begin with the order checkout request.

Local checks exercised the transactional service methods against real H2 databases:
success, payment decline, stock rejection, duplicate events, and restart persistence
passed. Local HTTP checks also verified acceptance, validation, conflicts, read-only
evidence, and profile isolation with Kafka delivery disabled. Kafka delivery and the
complete choreography have not been run against AWS here. The following outputs
are expected examples for your live demo.

## 5. Testing

### Scenario 1 — Complete checkout successfully

Prerequisite: fresh run ID `s017-success` in all three applications; payment result
uses its default SUCCESS.

```bash
curl --request POST 'http://localhost:9101/s017/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --header 'Idempotency-Key: success-1' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expected HTTP 202:

```json
{"orderId":1,"status":"PENDING","reason":null}
```

```bash
curl --request GET 'http://localhost:9101/s017/api/orders/1'
curl --request GET 'http://localhost:9105/api/inventory/reservations/1'
curl --request GET 'http://localhost:9105/api/inventory/BOOK-1'
curl --request GET 'http://localhost:9103/s017/api/payments/orders/1'
```

After processing, each GET returns HTTP 200. Sample bodies, in request order:

```json
{"orderId":1,"status":"CONFIRMED","reason":null}
```

```json
{"orderId":1,"state":"COMMITTED","items":[{"sku":"BOOK-1","quantity":1}]}
```

```json
{"sku":"BOOK-1","available":99}
```

```json
{"paymentId":1,"orderId":1,"amount":22.00,"status":"SUCCESS"}
```

The service consoles show `Event handled type=... orderId=1 eventId=...`. Event IDs
are generated UUIDs. `Event handled` is written after the transactional service
returns; duplicate or irrelevant events can also be handled without a new write.

### Scenario 2 — Release stock after payment is declined

Prerequisite: fresh run ID `s017-decline` in all three applications. Set this
**payment-service VM option** and restart payment:

```text
-Dsaga.demo.payment-result=DECLINE
```

```bash
curl --request POST 'http://localhost:9101/s017/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --header 'Idempotency-Key: decline-1' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expected HTTP 202: `{"orderId":1,"status":"PENDING","reason":null}`.

```bash
curl --request GET 'http://localhost:9101/s017/api/orders/1'
curl --request GET 'http://localhost:9105/api/inventory/reservations/1'
curl --request GET 'http://localhost:9105/api/inventory/BOOK-1'
curl --request GET 'http://localhost:9103/s017/api/payments/orders/1'
```

After compensation, HTTP 200 bodies are:

```json
{"orderId":1,"status":"CANCELLED","reason":"Payment declined"}
```

```json
{"orderId":1,"state":"RELEASED","items":[{"sku":"BOOK-1","quantity":1}]}
```

```json
{"sku":"BOOK-1","available":100}
```

```json
{"paymentId":1,"orderId":1,"amount":22.00,"status":"FAILED"}
```

In the consoles, follow PAYMENT_FAILED and INVENTORY_RELEASED by order ID. You may
briefly see COMPENSATING in Postman. A fast release can finish before your GET.
The failed payment remains recorded; compensation does not delete history.
Remove the payment VM override before success exercises.

### Scenario 3 — Reject a checkout with insufficient stock

Prerequisite: fresh run ID `s017-stock`; payment SUCCESS. First reserve and pay for 99 books:

```bash
curl --request POST 'http://localhost:9101/s017/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --header 'Idempotency-Key: stock-fill-1' \
  --data '{"customerId":"customer1","expectedAmount":2178.00,"items":[{"sku":"BOOK-1","quantity":99}]}'
curl --request GET 'http://localhost:9101/s017/api/orders/1'
```

POST returns 202; repeat GET until HTTP 200 reports
`{"orderId":1,"status":"CONFIRMED","reason":null}`. One book remains. Now request two:

```bash
curl --request POST 'http://localhost:9101/s017/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --header 'Idempotency-Key: stock-reject-2' \
  --data '{"customerId":"customer1","expectedAmount":44.00,"items":[{"sku":"BOOK-1","quantity":2}]}'
curl --request GET 'http://localhost:9101/s017/api/orders/2'
curl --request GET 'http://localhost:9105/api/inventory/BOOK-1'
curl --request GET 'http://localhost:9103/s017/api/payments/orders/2'
```

POST returns 202 with order 2 PENDING. After processing, the order GET returns 200:

```json
{"orderId":2,"status":"CANCELLED","reason":"Insufficient stock"}
```

Stock GET returns 200 with `{"sku":"BOOK-1","available":1}`. Payment GET returns
404 because no payment was created for order 2. Inventory's console shows
`INVENTORY_REJECTED` being published/handled by the flow. No compensation is needed
because the rejected reservation changed no stock.

### Scenario 4 — Reject missing checkout input

Prerequisite: applications running with profile `s017`.

```bash
curl --request POST 'http://localhost:9101/s017/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --header 'Idempotency-Key: invalid-1' \
  --data '{"expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expected HTTP 400 in Postman. Relevant response fields:

```json
{"status":400,"detail":"Valid Idempotency-Key, customer, amount and unique items are required"}
```

Order's shared handler logs `checkout.rejected`; no order or outgoing event is saved.

For service unavailability, duplicate delivery, and recovery, continue with
[S018's complete exercises](s018_saga_recovery.md#5-testing).

## 6. Interview practice

### “How does your choreography work?”

“Order saves an order and publishes its creation. Inventory reacts and reserves
stock. Payment reacts to that reservation. On payment failure, inventory releases
stock, and order marks cancellation complete after receiving the release event.”

### “Why return 202?”

“Checkout committed the initial order, but other services still need to process
events. The client reads the order status later. A 202 is not payment success.”

### “What if payment fails?”

“We save the failure and publish PAYMENT_FAILED. Inventory performs a new transaction
to restore stock. We keep the order COMPENSATING until that release is confirmed.”

### “What prevents losing an event after a database commit?”

“The business change and an outbox row commit together. A publisher sends that row
later. If sending fails, the row stays pending. Consumers must still tolerate repeats.”

### “Does choreography remove coupling?”

“It removes direct calls between these steps, but services still depend on event
meaning and the business sequence. With many participants, it becomes harder to
follow, so I would compare it with orchestration.”
