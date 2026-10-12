# S032 — Investigate a slow inventory reservation

[Overview](00-observability-overview.md) · [Roadmap](../../topics.md#s032)

## The story and problem

A customer places an order. Checkout takes too long because its inventory call is slow.
We use the request trace and logs to locate the delay.

## Our implementation

The [OrderService](../../order-service/src/main/java/com/tip/ecommerce/s030_correlation/OrderService.java)
calls inventory directly during checkout. The optional
[SlowInventoryFilter](../../inventory-service/src/main/java/com/tip/ecommerce/s032_incident_diagnosis/SlowInventoryFilter.java)
adds a 1200 ms wait to `POST /api/inventory/reservations` and logs
`inventory.delayed`. It runs before the database work.

## Demonstrate and investigate

Set `SPRING_PROFILES_ACTIVE=s032-slow-inventory` for inventory-service in IntelliJ
and restart it. Send this request in Postman:

```bash
curl --request POST 'http://localhost:9101/s030/api/orders/checkout' \
  --header 'Content-Type: application/json' \
  --data '{"customerId":"customer1","expectedAmount":22.00,"items":[{"sku":"BOOK-1","quantity":1}]}'
```

1. Expect 202. Copy the returned order ID and find its service logs as in S030.
2. Use the [S031 metrics and trace queries](s031_metrics_traces.md#traces).
   Compare checkout duration with the inventory reservation duration.
3. Open the service's inventory-call trace. Compare the inventory span and database spans.
4. Find `inventory.delayed` by trace ID. This explains the time before the SQL work.
5. Remove the profile, restart inventory, and submit a new order. Compare the new
   reservation trace with the delayed one.
6. Release each successful reservation using its order ID:

```bash
curl --request POST 'http://localhost:9105/api/inventory/reservations/{{orderId}}/release'
```

For an outage example, stop inventory and submit checkout again. Checkout returns
503, and the global exception handler logs `inventory.failed`. Restart inventory and place
a fresh order to demonstrate recovery. Each checkout request makes one inventory call.

| Evidence | What it suggests |
| --- | --- |
| Slow checkout, long inventory span | The direct inventory call is delaying the response |
| Long inventory span and delay log, fast SQL spans | Delay before database work |
| Failed outgoing call, no receiving inventory span | Investigate inventory availability and address |
| Request succeeds but no new telemetry | Check the agent, export connection, time range, and environment filter |

Missing telemetry alone does not prove a service is unavailable. Compare multiple signals.

## Later discussion for architects

- How would you measure the time from order acceptance to completed processing?
- Which failures need retries, and how would you persist unfinished work across restarts?
- Which latency or failure levels should trigger alerts?
- Which traces would you retain at high traffic?
- Why should order IDs appear in logs instead of metric labels with unlimited values?

## Interview practice

### “Checkout is slow. What do you inspect?”

“I compare the whole request with its inventory-call span and database spans. Then I
read logs with the same trace ID to explain the time spent in the slow step.”

### “How do you demonstrate recovery?”

“I repeat the operation after restoring the service or removing the delay. I compare
the new order's reservation outcome and trace with the earlier evidence.”

### “Does a timeout prove inventory did nothing?”

“No. The server may have processed the reservation before the caller stopped waiting.
I use the order ID to inspect the outcome before deciding whether to retry.”
