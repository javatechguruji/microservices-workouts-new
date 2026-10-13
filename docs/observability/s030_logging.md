# S030 — Logging: follow an order into inventory

[Overview](00-observability-overview.md) · [Roadmap](../../topics.md#s030) · [Next: metrics and traces](s031_metrics_traces.md)

## 1. The story and problem

A customer places an order for one copy of BOOK-1. Order-service saves the order
and calls inventory-service directly to reserve the book. Inventory-service
records the reservation and reduces the available quantity in the inventory DB
(`inventory-service-db`).

If the request is slow or fails, which step caused it? Logs let us follow the order
from checkout to inventory and find where processing stopped.

A **log** is a message about something that happened, such as “Order saved” or
“Stock reserved.” Suppose the customer creates **order 41**. Both messages include
`orderId=41`, so we can find the messages about that order together.

The checkout request travels from order-service to inventory-service. Its logs share
a **trace ID**, which lets us follow that one request across both services. If we later
send another request to release order 41's reservation, its logs still contain
`orderId=41`, but have a new trace ID: the same order, a different request.

## 2. Our implementation

### Controller — record request entry at DEBUG

In [OrderController](../../order-service/src/main/java/com/tip/ecommerce/s030_correlation/OrderController.java):

```java
// Show that the checkout request reached the controller.
log.atDebug().addKeyValue("event", "checkout.received")
    .log("Checkout request received");
```

This helps answer “Did the request reach this application?” DEBUG keeps routine
request-entry details out of the default INFO output.

### OrderService — record meaningful steps at INFO

In [OrderService](../../order-service/src/main/java/com/tip/ecommerce/s030_correlation/OrderService.java),
the log statements mark three points in the order flow:

```java
// After the order is saved: identify the order and its successful save.
log.atInfo().addKeyValue("event", "order.saved").addKeyValue("orderId", orderId)
    .addKeyValue("outcome", "success").log("Order saved");

// Before calling inventory: identify the dependency we are about to contact.
log.atInfo().addKeyValue("event", "inventory.requested").addKeyValue("orderId", orderId)
    .addKeyValue("dependency", "inventory-service").log("Requesting stock reservation");

// After inventory confirms: record the outcome and time spent in the call.
log.atInfo().addKeyValue("event", "inventory.confirmed").addKeyValue("orderId", orderId)
    .addKeyValue("dependency", "inventory-service").addKeyValue("outcome", "success")
    .addKeyValue("durationMs", (System.nanoTime() - started) / 1_000_000)
    .log("Stock reserved");
```

These excerpts show logging points, with the intervening business code omitted.
`started` records when the inventory call begins. `durationMs` shows how many
milliseconds the call took.

Each entry carries `orderId`. The agent supplies trace context to connect these messages
with inventory's logs. A duration in a log describes this call; response-time measurements across many calls are covered in S031.

### InventoryService — log success only after commit

In [InventoryService](../../inventory-service/src/main/java/com/tip/ecommerce/s030_correlation/InventoryService.java),
this statement runs after the reservation transaction commits:

```java
// Report success only when the reservation change has committed.
log.atInfo().addKeyValue("event", "reservation.committed").addKeyValue("orderId", orderId)
    .addKeyValue("state", state).addKeyValue("outcome", "success")
    .log("Reservation change committed");
```

The placement matters: logging success before commit could leave a misleading success
message for a change that later rolls back. The `state` field tells us whether the
committed change reserved, released, or committed the stock reservation.

### Avoid duplicate failure logs

The [inventory client](../../order-service/src/main/java/com/tip/ecommerce/client/InventoryServiceClient.java)
passes call failures to the global exception handler. The client and service do not
both write the same exception. The handler records it once, with the context needed
to investigate.

### Common error handling and log levels

[GlobalExceptionHandler](../../order-service/src/main/java/com/tip/ecommerce/exception/GlobalExceptionHandler.java)
handles rejected HTTP requests and returns a standard error body. An invalid checkout
returns 400. For an `InventoryCallException`, the same handler records
`Stock reservation failed` once and returns 503 with the order ID:

```java
// Log the failure once in the exception handler.
log.atError() // ERROR: checkout could not complete its inventory call.
    .addKeyValue("event", "inventory.failed") // Find all failures of this inventory call.
    .addKeyValue("orderId", failure.orderId()) // Identify which order was affected.
    .addKeyValue("dependency", "inventory-service") // Name the service we tried to call.
    .addKeyValue("outcome", "failure") // Record that this call did not succeed.
    // Record the exception's name to help identify what went wrong.
    .addKeyValue("errorType", failure.getCause().getClass().getSimpleName())
    .addKeyValue("httpStatus", 503) // Match the HTTP status returned to Postman.
    .setCause(failure) // Include the exception and stack trace to help locate the cause.
    .log("Stock reservation failed"); // Write the message, fields, and exception together.
```

| Place | Log level | What it explains |
| --- | --- | --- |
| Controller | DEBUG | Request entry |
| Service | INFO | Acceptance, reservation start, successful outcome |
| Global exception handler | WARN | Rejected checkout input |
| Global exception handler | ERROR | Inventory call failed and checkout returns 503 |

Log each failure once, where it is handled. Unexpected application errors
normally need an ERROR log with details to help find the cause. Routine start events can be DEBUG in a busy application.
To show controller messages too, set
`LOGGING_LEVEL_COM_TIP_ECOMMERCE_S030_CORRELATION=DEBUG` in IntelliJ and restart the apps.

### Readable messages and structured fields

A production log serves people and search tools. The **message** explains what happened.
The **event** is a stable name that queries can use even if the wording changes.
`addKeyValue` writes each field separately; it does not hide the order ID inside text.

Read this statement from left to right:

```java
log.atInfo() // INFO records a normal step in processing the order.
    // Name this step so we can find order-save logs even if the message wording changes.
    .addKeyValue("event", "order.saved")
    // Add this order's ID, such as 41, so we can find its logs among other orders.
    .addKeyValue("orderId", orderId)
    // Say that this save succeeded; this does not mean the whole checkout has finished.
    .addKeyValue("outcome", "success")
    // Write one log entry containing this message and all the fields above.
    .log("Order saved");
```

`atInfo()` starts an INFO log entry. Each `addKeyValue(name, value)` attaches one
named field. The final `log(...)` writes the entry. Adding fields alone does not write a log.
For order 41, `orderId` is the number `41`, not part of the sentence “Order saved.”

**Why does this help?** Suppose hundreds of orders are being handled together. Searching
for the words “Order saved” finds all of them. Filtering `orderId="41"` finds the one
you are investigating. Filtering `event="inventory.failed"` finds that type of failure
without depending on how its human-readable message is phrased.

```text
INFO [order-service] trace_id=f6cec42964fcda4036fbf389e6552ad7 span_id=857826715f2344ab - Order saved event="order.saved" orderId="1" outcome="success"
```

| Field | Explanation |
| --- | --- |
| Time | The console pattern prints when the entry was written. |
| `level` | Our `atInfo()` call chooses INFO; the console pattern displays that level. |
| Service name | The console pattern reads `spring.application.name`. The agent separately exports `service.name` from `otel.properties`. |
| Class name | `LoggerFactory.getLogger(OrderService.class)` supplies the class name, showing which class wrote the log. |
| `message` | Our `log("Order saved")` call supplies this readable message. |
| `event` | Our `addKeyValue("event", "order.saved")` call supplies the step's name so we can search for it even if the message wording changes. |
| `orderId` | Our `addKeyValue("orderId", orderId)` call supplies the current order's ID so we can find its logs among other orders. |
| `outcome` | Our `addKeyValue("outcome", "success")` call says the save succeeded; it does not mean the whole checkout has finished. |
| `trace_id` | The OpenTelemetry Java agent creates a trace ID or continues the incoming request's trace ID. It adds that ID to the logging context, and the console pattern prints it to connect logs from the same request across services. |
| `span_id` | The agent creates an ID for the current step and adds it to the logging context. The console pattern prints it so we can identify which step wrote the log. |

This is a shortened console example; time, thread, and class are omitted here. Your IDs differ.

`logging.pattern.console` in each service’s `application.yml` formats readable text.
`%kvp` prints the fields added with `addKeyValue`; `%X{trace_id}` and `%X{span_id}`
print the IDs supplied by the agent. The agent exports structured log records separately
using OTLP, including the key/value fields enabled in `otel.properties`.

### Structured log sent to the Collector

The console stays readable text. Separately, the agent sends the log and its fields
to the Collector. Our `http/protobuf` setting sends binary Protobuf; this is a shortened,
illustrative **OTLP JSON view of those fields**, not a captured network payload.

```json
{
  "resourceLogs": [
    {
      "resource": {
        "attributes": [
          {
            "key": "service.name",
            "value": {
              "stringValue": "order-service"
            }
          },
          {
            "key": "deployment.environment.name",
            "value": {
              "stringValue": "observability"
            }
          }
        ]
      },
      "scopeLogs": [
        {
          "scope": {
            "name": "com.tip.ecommerce.s030_correlation.OrderService"
          },
          "logRecords": [
            {
              "timeUnixNano": "1791767097060922000",
              "severityNumber": 9,
              "severityText": "INFO",
              "body": {
                "stringValue": "Order saved"
              },
              "attributes": [
                {
                  "key": "event",
                  "value": {
                    "stringValue": "order.saved"
                  }
                },
                {
                  "key": "orderId",
                  "value": {
                    "intValue": "1"
                  }
                },
                {
                  "key": "outcome",
                  "value": {
                    "stringValue": "success"
                  }
                }
              ],
              "traceId": "f6cec42964fcda4036fbf389e6552ad7",
              "spanId": "857826715f2344ab"
            }
          ]
        }
      ]
    }
  ]
}
```

| Field | Explanation |
| --- | --- |
| `resourceLogs` / `resource.attributes` | Groups logs by application; the agent supplies `service.name` and environment from `otel.properties`. |
| `scopeLogs` / `scope.name` | Groups entries by the logger that produced them, here `OrderService`. |
| `timeUnixNano` | Time the log was written, expressed as nanoseconds since 1 January 1970. |
| `severityText` / `severityNumber` | The level as text and its OpenTelemetry number; INFO is 9. |
| `body.stringValue` | The readable message passed to `log(...)`. |
| `attributes` | Fields from `addKeyValue`, such as `event`, `orderId`, and `outcome`. `intValue` preserves order ID as an integer even though OTLP JSON writes that integer as a quoted string. |
| `traceId` / `spanId` | The active trace and span supplied by the agent. These are the IDs shown as `trace_id` and `span_id` in the console. |

For how order IDs, trace IDs, and span IDs connect related activity, see
[S031 — Connect an order, its requests, and their steps](s031_metrics_traces.md#connect-an-order-its-requests-and-their-steps).

### How order-service enables the Java agent

Each service's `pom.xml` uses
`maven-dependency-plugin` to copy the Maven artifact
`io.opentelemetry.javaagent:opentelemetry-javaagent` into
`target/otel/opentelemetry-javaagent.jar` during `process-resources`.
In IntelliJ, open the order-service run configuration under **Run → Edit Configurations**
and add these **VM options** (with the workspace root as the project directory).

**order-service:**

```text
-javaagent:$PROJECT_DIR$/order-service/target/otel/opentelemetry-javaagent.jar
-Dotel.javaagent.configuration-file=$PROJECT_DIR$/order-service/src/main/resources/otel.properties
```

**inventory-service:**

```text
-javaagent:$PROJECT_DIR$/inventory-service/target/otel/opentelemetry-javaagent.jar
-Dotel.javaagent.configuration-file=$PROJECT_DIR$/inventory-service/src/main/resources/otel.properties
```

The first option loads the agent; the second loads `otel.properties`, which contains
the Collector address (`http://localhost:4318`) and protocol. Keep your existing AWS
tunnel connected. In Grafana, select environment `observability`.

If the agent JAR is missing, run **Lifecycle → process-resources** for that service
in IntelliJ's Maven tool window.

### Why attach the exception?

`errorType` names the error so we can search for similar failures.
`setCause(failure)` includes the exception and stack trace, helping us find where the code failed and why.

## 3. Demonstrate S030 in Postman

Start `OrderServiceApplication` and `InventoryServiceApplication` in IntelliJ with
[the agent options above](#how-order-service-enables-the-java-agent).
The default ports are 9101 and 9105. Inventory starts with 100 copies of BOOK-1;
orders start empty. Restart both applications for a fresh demo.

If those ports are occupied, set `SERVER_PORT=9201` for orders,
`SERVER_PORT=9205` for inventory, and `INVENTORY_BASE_URL=http://127.0.0.1:9205`
for orders. Use the corresponding ports in the requests below.

Save these requests in a **S030 — Logging** Postman folder. Import each curl using
**Import → Raw text**. These application endpoints use No Auth for the exercise.

### Place the order

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **202** with an acceptance response such as:

```json
{"orderId":41,"status":"PENDING","state":"RESERVED"}
```

Use the returned `orderId` to find this order in the logs. The response arrives
after inventory confirms the reservation. `state=RESERVED` describes that result;
`status=PENDING` means the order has not completed payment and confirmation.

In the order-service console, find the entries for the returned order ID. Check that
the final `inventory.confirmed` entry includes `durationMs` and `outcome=success`.

In the inventory-service console, find `reservation.started` and
`reservation.committed` for the same order ID. This inventory call is made by
order-service while it handles the checkout request.

## 4. Follow the order in Grafana

Choose **Explore → Loki → Code**, with **Last 15 minutes**. Allow time for log delivery.
Replace 41 with the accepted order ID:

```logql
{service_name=~"order-service|inventory-service",deployment_environment_name="observability"} | orderId="41"
```

These filters search the separate fields sent with each log entry. The log message
remains readable text.
Do not use a text search for `orderId=41`, because the ID is a separate attribute.
To find failed inventory calls:

```logql
{service_name="order-service",deployment_environment_name="observability"} | event="inventory.failed"
```

Find the service's inventory-call event and copy its `trace_id`. To inspect that operation:

```logql
{service_name=~"order-service|inventory-service",deployment_environment_name="observability"} | trace_id="PASTE_TRACE_ID"
```

The order ID connects the order's history. Trace context connects the service's HTTP
call with inventory handling. Use order ID to locate events even when they belong to
different requests or traces.

## 5. Testing

These examples show shortened text console logs. Your order IDs, trace IDs,
span IDs, and timings will differ. Trace and span IDs appear when the Java agent is enabled.

### Scenario 1 — Reject missing input (WARN)

Keep both applications running and send an empty checkout body:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{}'
```

Expect **400 Bad Request**, with detail `Customer, amount and items are required`.
The order-service console shows:

```text
WARN [order-service] trace_id=f6cec42964fcda4036fbf389e6552ad7 span_id=857826715f2344ab - Checkout request rejected event="checkout.rejected" httpStatus="400" outcome="rejected"
```

There is no `orderId` because validation failed before an order was saved. Inventory
is not called. Find this warning in Loki with:

```logql
{service_name="order-service",deployment_environment_name="observability"} | event="checkout.rejected"
```

### Scenario 2 — Inventory cannot be reached (ERROR)

Keep order-service running and stop inventory-service in IntelliJ. Send:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **503 Service Unavailable**, with detail `Inventory reservation failed` and the
created `orderId`. The order-service console shows `Order saved`, then
`Requesting stock reservation`, followed by an error like:

```text
ERROR [order-service] trace_id=f6cec42964fcda4036fbf389e6552ad7 span_id=857826715f2344ab - Stock reservation failed event="inventory.failed" orderId="41" dependency="inventory-service" outcome="failure" errorType="WebClientRequestException" httpStatus="503"
```

The full console output also includes the exception stack trace; for a refused connection, its cause shows
that the connection could not be opened. The error type can differ for a timeout.
Inventory produces no receiving log while it is stopped. The saved order remains in
the order DB even though the inventory call failed.

```logql
{service_name="order-service",deployment_environment_name="observability"} | event="inventory.failed"
```

### Scenario 3 — Inventory is available again (INFO)

Start inventory-service again, keep order-service running, and send a new checkout:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **202 Accepted**, for example:

```json
{"orderId":42,"status":"PENDING","state":"RESERVED"}
```

In the inventory-service console, find the reservation success:

```text
INFO [inventory-service] trace_id=a1b2c3d4e5f60718293a4b5c6d7e8f90 span_id=1122334455667788 - Reservation change committed event="reservation.committed" orderId="42" state="RESERVED" outcome="success"
```

In the order-service console, find the successful inventory call:

```text
INFO [order-service] trace_id=a1b2c3d4e5f60718293a4b5c6d7e8f90 span_id=8877665544332211 - Stock reserved event="inventory.confirmed" orderId="42" dependency="inventory-service" outcome="success" durationMs="35"
```

The matching order and trace IDs connect these two entries. This new order succeeds;
the earlier failed order is not automatically retried.

Implementation references: [Spring Boot structured JSON logging](https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured)
and [OpenTelemetry Logback attribute capture](https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/out-of-the-box-instrumentation/#logback).

## Interview practice

### “How do you follow an order across services?”

“I search by order ID to find its acceptance and processing events. Then I use the
inventory call's trace ID to connect order-service and inventory messages for that operation.”

### “What does the checkout response tell you in this example?”

“The call returns after inventory confirms reservation. The response contains the
order ID and RESERVED state. The order is still PENDING because payment and final
confirmation are separate steps.”

### “Where do you handle and log failures?”

“The controller delegates to OrderService. The service records the operation's progress.
The global exception handler logs a failed inventory call once and returns 503 with
its order ID. I use the trace ID to connect that failure with the earlier messages.”

### “Why use logs as well as the HTTP response?”

“A response summarizes the result. Logs show which steps ran and where the operation
stopped. That helps distinguish an inventory connection failure from later processing.”

### “What makes these logs useful in production?”

“They combine readable messages with structured fields. I search by a stable event or
order ID, then use trace and span IDs to follow the call. A failure is logged once with details that help explain it. A database change is
logged as successful only after it has been saved successfully. I keep sensitive
data out and avoid using unique IDs as metric or indexed log labels.”

### “Are logs a kind of metric?”

“No. Logs describe individual events. Metrics summarize measurements over time, and
traces connect the steps of an operation. They are complementary observability signals.”
