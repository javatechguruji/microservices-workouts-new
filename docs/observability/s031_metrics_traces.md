# S031 — Metrics and traces

[Overview](00-observability-overview.md) · [Roadmap](../../topics.md#s031) · [Previous: logging](s030_logging.md)

## 1. An order takes longer than usual

A customer places an order. Order-service saves it and asks inventory-service to
reserve BOOK-1. The customer waits while that call finishes.

Logs tell us what happened to the order. Now we want to answer two more questions:
**Are reservation requests getting slower? Which step is taking the time?**
Metrics answer the first question; traces help answer the second.

<a id="metrics"></a>
## Part A — Metrics: measure inventory reservations

A **metric** is a number we measure over time. For this example, we measure how many
reservation requests inventory receives and how long they take.

| Metric type | What it means | Simple example |
| --- | --- | --- |
| **Counter** | Counts how many times something has happened. | Inventory has received 3 reservation requests. One more arrives, so the count becomes 4. Finishing a request does not reduce the count. **Other counter examples:** total failed requests, total orders created, total messages processed. |
| **Gauge** | Shows how much of something is being used right now. | Order-service uses 100 MB of memory now, then 120 MB, then 90 MB. The value can go up or down. **Other gauge examples:** requests currently being handled, active database connections, messages currently waiting in a queue. |
| **Histogram** | Groups requests by how long they took, so we can see how many were fast or slow. | Of 10 requests, 6 finished within 0.1 seconds and 9 finished within 1 second. The second count includes the first 6; only 1 request took longer than 1 second. **Other histogram examples:** database query times, outgoing service-call times, request sizes in bytes. |

Think of them as **how many so far**, **how much right now**, and **how the times are
spread out**. These numbers are examples; the demonstration below uses actual measurements.

### Our implementation

The Java agent measures supported HTTP requests automatically. We do not add a counter
in the controller. Each service's [otel.properties](../../inventory-service/src/main/resources/otel.properties)
contains these settings:

```properties
# Send the application's measurements to the Collector.
otel.metrics.exporter=otlp
# Send measurements every 15 seconds.
otel.metric.export.interval=15000
```

Prometheus collects measurements from the Collector. In **Grafana → Explore →
Prometheus → Code**, use this query to see the total reservation requests received:

```promql
sum(http_server_request_duration_seconds_count{service_name="inventory-service",deployment_environment_name="observability",http_route="/api/inventory/reservations"})
```

The `_count` value counts the requests measured by the duration histogram.
`service_name` selects inventory, `http_route` selects its reservation endpoint, and
`sum` combines the matching series. This is a request count, not the number of books
reserved; rejected requests that reach this endpoint can count too.

For response time, use:

```promql
histogram_quantile(0.95,
  sum by (le) (rate(http_server_request_duration_seconds_bucket{service_name="inventory-service",deployment_environment_name="observability",http_route="/api/inventory/reservations"}[5m]))
)
```

This estimates **p95 in seconds**: roughly 95 out of 100 requests finished within that
time. `[5m]` uses the last five minutes; `le` keeps the histogram's time ranges separate
while combining measurements. Send several requests; one or two give a poor estimate.

A gauge example is current memory use:

```promql
jvm_memory_used_bytes{service_name="order-service",deployment_environment_name="observability"}
```

These values are in bytes and may appear separately for different memory areas.

<a id="traces"></a>
## Part B — Traces: locate the slow step

A **trace** follows one request through the services. A **span** is one timed step
within that request, such as calling inventory or querying its database.

```text
Checkout request in order-service
  └─ InventoryServiceClient calls inventory
       └─ inventory-service handles the reservation
            └─ Inventory database work
```

If inventory takes longer, checkout takes longer too because order-service waits for
its reply. The trace helps distinguish time spent in inventory from its database work.

### Our implementation

[InventoryServiceClient](../../order-service/src/main/java/com/tip/ecommerce/client/InventoryServiceClient.java)
makes the outgoing HTTP call. The agent follows that call automatically. These settings
in each service's `otel.properties` enable trace export and sharing request information:

```properties
# Send traces to the Collector.
otel.traces.exporter=otlp
# Carry trace information between services in HTTP headers.
otel.propagators=tracecontext,baggage
```

`tracecontext` carries the trace information that connects service calls. `baggage`
can carry extra application-defined values, such as `salesChannel=mobile`, between
services. This example does not set any baggage values; `tracecontext` alone is enough
for its tracing. Baggage values are not automatically added to logs or span attributes.

The order-service agent puts the **trace ID** and the **outgoing call’s span ID** in
the `traceparent` HTTP header, along with a format version and trace flags (including
whether the trace is marked for sampling). The trace ID connects the request across
services; the outgoing span ID tells inventory which calling step its work belongs to.
The inventory agent reads this header, keeps the same trace ID, and creates a new span
ID for its own step. This is called **trace propagation**: passing trace information between services.

### Connect an order, its requests, and their steps

For order 41, order-service and inventory-service include `orderId=41` in their logs.
This ID identifies the business order. The checkout also has a `trace_id` that links
its request steps across both services. Each step has its own `span_id`.

A later operation on order 41 can have a different trace ID while keeping the same
order ID. Search by order ID for its history; use a trace ID to investigate one request.
That is why order ID can be called a **business correlation ID**—it connects activity
about the same order.

### Create a visible delay

[SlowInventoryFilter](../../inventory-service/src/main/java/com/tip/ecommerce/s032_incident_diagnosis/SlowInventoryFilter.java)
adds a delay only to reservation POST requests when its profile is enabled.
These excerpts show the relevant statements:

```java
@Profile("s032-slow-inventory") // Enable this filter only when this profile is selected.
```

```java
// Wait before letting inventory process the reservation.
Thread.sleep(delayMs);
// Continue handling the request after the wait.
chain.doFilter(request, response);
```

The filter adds a **1200-millisecond (1.2-second)** wait before inventory processes
the reservation. For example, a request could take:

```text
Inventory request:  1,220 ms total
  Added wait:       1,200 ms
  Database work:       20 ms
```

In this example, the added wait takes 1,200 ms and the database takes 20 ms.
The total is 1,220 ms. The database's 20 ms is already part of that total.

In Tempo, compare the inventory request time with the database time: this shows
that the added wait, rather than the database, caused the delay.

## 3. Prepare the demonstration

Use the [Java agent options in S030](s030_logging.md#how-order-service-enables-the-java-agent)
for both services and keep the existing Collector connection available. Start both
applications in IntelliJ. Use **No Auth** in Postman and import each curl through
**Import → Raw text**. All requests below go to order-service.

Start with the delay profile disabled. A fresh inventory has 100 copies of BOOK-1;
each successful checkout reserves one. Restart both applications for fresh data when
needed. A restart resets the application's current counters, but does not erase metrics already
stored in Prometheus or logs stored in Loki. You can view a week's history if their
retention and persistent storage are configured to keep it. For totals across restarts,
Prometheus `increase()` accounts for counter resets; for this demo's raw counter,
compare values within the same run.

## 4. Testing

Responses and timings below are illustrative. Use the actual returned order ID.
Allow about 30–60 seconds for measurements to become visible in Grafana.

### Verify the key metrics in Grafana

The agents supply the measurements automatically; Grafana displays them. Start with
these four useful interview examples on your existing dashboard:

1. Open **Grafana → Dashboards** and select **Commerce — requests, JVM and checkout**.
2. Set **Environment = observability** and **Service = order-service** or
   **inventory-service**. Select **Last 15 minutes**.
3. Send a few checkout requests from the testing scenarios below. Wait 30–60 seconds,
   then refresh the dashboard.

| Dashboard panel | What to look for | What to explain in an interview |
| --- | --- | --- |
| **HTTP request rate** | Send several orders; requests per second should rise. | How much traffic is the service handling? This is a rate, not the total request count. |
| **HTTP p95 latency** | Enable the slow-inventory scenario and compare response times. | Are requests becoming slower? Roughly 95% finished within the displayed time. |
| **HTTP 5xx ratio** | Stop inventory and send checkout requests; select order-service. | What share of requests failed with a server error? A value of 0.1 means 10%. |
| **JVM heap used** | Select either service and observe memory while sending requests. | How much Java heap memory is in use? It can rise and fall as the application runs. |

These panels already contain queries, so you do not need to type PromQL. They summarize
all matching HTTP routes for the selected service, not only reservations. The other
business and infrastructure panels on this shared dashboard are outside this lesson.
The panel names above match the dashboard definition in the reference workspace;
the dashboard must be provisioned in your running Grafana.

**To inspect just reservation requests without writing a query:**

1. Open **Explore**, select **Prometheus**, then select **Builder**.
2. In the metric selector, choose `http_server_request_duration_seconds_count`.
3. Add label filters: `service_name = inventory-service`,
   `deployment_environment_name = observability`, and
   `http_route = /api/inventory/reservations`.
4. Click **Run query**. Send another checkout, wait for collection, and run it again.
   The total increases; separate lines can represent different response statuses.

The metric selector lists measurements actually received by Prometheus. If the metric
is missing, first send requests and check that both agents and the Collector connection
are active. Request rate, p95, and error ratio use a five-minute window, so one request
or a recent restart may not immediately produce a useful chart.

Grafana's [Prometheus query editor guide](https://grafana.com/docs/grafana/latest/datasources/prometheus/query-editor/)
explains Builder mode. The queries in Part A are optional when you want to inspect exactly
how a measurement is selected or calculated.

### Scenario 1 — Observe normal reservations

Keep both services running without the delay profile. Send this request several times:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **202 Accepted**, for example:

```json
{"orderId":41,"status":"PENDING","state":"RESERVED"}
```

In Prometheus, run the reservation count query from Part A. On a fresh run, three
completed requests should eventually produce a count of 3 if there is no other traffic.

In **Grafana → Explore → Loki**, find the returned order:

```logql
{service_name=~"order-service|inventory-service",deployment_environment_name="observability"} | orderId="41"
```

Copy its `trace_id`. In **Explore → Tempo**, look up that trace ID. Inspect the checkout,
outgoing inventory call, inventory request, and database spans. Note their times as your
starting comparison. The order log's `inventory.confirmed` event also has `durationMs`.

### Scenario 2 — Locate a slow reservation

In inventory-service's IntelliJ environment variables, set
`SPRING_PROFILES_ACTIVE=s032-slow-inventory` and restart inventory. Send several orders:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **202 Accepted** with `state=RESERVED`, now taking roughly 1.2 seconds or more
under normal demo conditions. Inventory writes a log containing:

```text
WARN [inventory-service] - Demo inventory delay applied event="inventory.delayed" delayMs="1200"
```

Find this order's trace as in Scenario 1. The inventory request and the enclosing
order-service call include the wait. Database spans need not become slower.

Run the p95 query from Part A. After enough slow requests and time for collection, it
should reflect slower responses. The exact value depends on the requests in the time
window and the histogram ranges; it will not necessarily equal 1.2 seconds.

### Scenario 3 — Inventory cannot be reached

Stop inventory-service and keep order-service running. Send:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **503 Service Unavailable**, with detail `Inventory reservation failed` and an
`orderId`. Order-service records `event=inventory.failed` at ERROR.

Find its trace using that order's logs. The outgoing inventory call shows a failure;
there is no inventory receiving span for this request because inventory is stopped.
Inventory cannot count a request it never receives. Its earlier metrics may remain
visible for a while, so absence of new inventory errors does not mean checkout is healthy.

### Scenario 4 — Verify recovery

Remove `s032-slow-inventory` from inventory-service's active profiles and start it again.
Keep order-service running and send a new checkout:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

Expect **202 Accepted** with `state=RESERVED`. Order-service records
`event=inventory.confirmed`, `outcome=success`, and the call's `durationMs`.
Compare this new trace with Scenario 2: the intentional 1.2-second wait is gone.
The earlier failed order is not automatically retried.

The five-minute p95 chart can still contain earlier slow requests. A new trace shows
recovery for this request immediately; the chart needs more time and fresh requests.

## Interview practice

### “What are metrics and traces?”

“Metrics are numbers that show how the application behaves over time, such as request
counts and response times. A trace follows one request across services and shows how
long each step took.”

### “How would you investigate slow checkout?”

“I check response-time metrics to see whether requests are getting slower. Then I open
a slow request's trace to find the step taking time. In this example, inventory waits
before processing, so its request is slow even when the database work is fast.”

### “How do both services get the same trace ID?”

“The Java agent carries trace information in the HTTP call from order-service.
Inventory's agent reads it and continues the same trace, with separate spans for its steps.”

### “What is the difference between order ID and trace ID?”

“Order ID identifies the order. Trace ID identifies one request's journey. Several
requests about the same order can have different trace IDs.”

### “What is p95?”

“It is a time within which roughly 95 out of 100 requests finished. Here it is estimated
from measured time ranges, so I need enough requests to make the result useful.”

### “Does zero inventory-server errors prove checkout is healthy?”

“No. If inventory is stopped, it cannot receive or measure the failed call. I check
order-service's errors and the outgoing call's trace too.”
