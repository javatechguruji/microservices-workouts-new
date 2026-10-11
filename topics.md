# Microservices learning roadmap

One project: `microservices-workouts`. Small scenarios for learning and interview practice.
Start with S001, then S002 and S004. Add infrastructure only when a scenario requires it.

## Status and conventions

- Every checkbox means implemented, run, reviewed, and explained. All scenarios below are planned.
- The previous ecommerce implementation is archived; it does not count as scenario completion.
- IDs are permanent and local to this project. Next unused ID: `S041`. Never reuse retired IDs.
- Planned paths are plain text. Turn them into links only after the artifact exists.
- Java base package: `com.tip.ecommerce`, retained from the original project.
- Each scenario will be a standalone Maven project under `scenarios/<stem>/`.
- All scenario Java types go directly in its one package; tests use the same package.
- For two processes, use profiles or scenario-specific configuration in that project. Shared startup code stays outside the scenario package. Split deployable modules only when the exercise needs that boundary.
- Default tools: Java 17, Maven, Spring Boot, and WebClient for outgoing HTTP. Pin dependencies when the first runnable example is added.

See [workspace guide](README.md) and [document conventions](docs/README.md).

## Service boundaries

<a id="s001"></a>
- [ ] **S001 — Service ownership** · Scenario: **Orders and stock have different owners**
  - Try: Identify who owns each rule and record before creating services.
  - Planned implementation: `scenarios/s001_service_ownership/src/main/java/com/tip/ecommerce/s001_service_ownership/`
  - Planned note: `docs/service-boundaries/s001_service_ownership.md`

<a id="s002"></a>
- [ ] **S002 — Independent processes** · Scenario: **Restart stock while orders stays running**
  - Try: Use two small processes and observe independent availability.
  - Planned implementation: `scenarios/s002_independent_services/src/main/java/com/tip/ecommerce/s002_independent_services/`
  - Planned note: `docs/service-boundaries/s002_independent_services.md`

<a id="s003"></a>
- [ ] **S003 — Architect Level: boundary design** · Scenario: **Split a growing checkout module**
  - Try: Compare a modular monolith with services using change rate, coupling, and team ownership.
  - Planned implementation: `scenarios/s003_boundary_tradeoffs/src/main/java/com/tip/ecommerce/s003_boundary_tradeoffs/`
  - Planned note: `docs/service-boundaries/s003_boundary_tradeoffs.md`

## HTTP communication

<a id="s004"></a>
- [ ] **S004 — Synchronous HTTP** · Scenario: **Order asks stock for availability**
  - Try: Make one WebClient call with explicit request and response DTOs.
  - Planned implementation: `scenarios/s004_service_call/src/main/java/com/tip/ecommerce/s004_service_call/`
  - Planned note: `docs/http-communication/s004_service_call.md`

<a id="s005"></a>
- [ ] **S005 — API contracts** · Scenario: **Stock adds a response field**
  - Try: Keep consumers compatible with additive changes and clear error responses.
  - Planned implementation: `scenarios/s005_api_contract/src/main/java/com/tip/ecommerce/s005_api_contract/`
  - Planned note: `docs/http-communication/s005_api_contract.md`

<a id="s006"></a>
- [ ] **S006 — Service discovery** · Scenario: **Stock moves to a different address**
  - Try: Start with configured URLs; later compare discovery and DNS.
  - Planned implementation: `scenarios/s006_service_discovery/src/main/java/com/tip/ecommerce/s006_service_discovery/`
  - Planned note: `docs/http-communication/s006_service_discovery.md`

<a id="s007"></a>
- [ ] **S007 — Response aggregation** · Scenario: **Build an order summary from two owners**
  - Try: Distinguish required data from optional data.
  - Planned implementation: `scenarios/s007_aggregation/src/main/java/com/tip/ecommerce/s007_aggregation/`
  - Planned note: `docs/http-communication/s007_aggregation.md`

<a id="s008"></a>
- [ ] **S008 — Architect Level: latency budgets** · Scenario: **Three downstream calls slow one request**
  - Try: Allocate a deadline across fan-out calls and define partial results.
  - Planned implementation: `scenarios/s008_latency_budget/src/main/java/com/tip/ecommerce/s008_latency_budget/`
  - Planned note: `docs/http-communication/s008_latency_budget.md`

## Failure handling

<a id="s009"></a>
- [ ] **S009 — Timeouts** · Scenario: **Stock responds too slowly**
  - Try: Bound the wait and return a clear failure.
  - Planned implementation: `scenarios/s009_timeouts/src/main/java/com/tip/ecommerce/s009_timeouts/`
  - Planned note: `docs/failure-handling/s009_timeouts.md`

<a id="s010"></a>
- [ ] **S010 — Retries and backoff** · Scenario: **A temporary connection failure**
  - Try: Retry only safe operations within a fixed budget.
  - Planned implementation: `scenarios/s010_safe_retries/src/main/java/com/tip/ecommerce/s010_safe_retries/`
  - Planned note: `docs/failure-handling/s010_safe_retries.md`

<a id="s011"></a>
- [ ] **S011 — Circuit breaker** · Scenario: **Stock fails repeatedly**
  - Try: Observe closed, open, and half-open states.
  - Planned implementation: `scenarios/s011_circuit_breaker/src/main/java/com/tip/ecommerce/s011_circuit_breaker/`
  - Planned note: `docs/failure-handling/s011_circuit_breaker.md`

<a id="s012"></a>
- [ ] **S012 — Bulkheads** · Scenario: **A slow dependency uses every worker**
  - Try: Limit concurrency so unrelated requests can still finish.
  - Planned implementation: `scenarios/s012_bulkhead/src/main/java/com/tip/ecommerce/s012_bulkhead/`
  - Planned note: `docs/failure-handling/s012_bulkhead.md`

<a id="s013"></a>
- [ ] **S013 — Architect Level: cascading failures** · Scenario: **Retries overload a recovering service**
  - Try: Combine deadlines, jitter, concurrency limits, and load shedding.
  - Planned implementation: `scenarios/s013_retry_storm/src/main/java/com/tip/ecommerce/s013_retry_storm/`
  - Planned note: `docs/failure-handling/s013_retry_storm.md`

## Data ownership and consistency

<a id="s014"></a>
- [ ] **S014 — Database per service** · Scenario: **Order cannot query the stock tables**
  - Try: Exchange contracts instead of joining another service database.
  - Planned implementation: `scenarios/s014_owned_data/src/main/java/com/tip/ecommerce/s014_owned_data/`
  - Planned note: `docs/data-consistency/s014_owned_data.md`

<a id="s015"></a>
- [ ] **S015 — Idempotency** · Scenario: **A caller submits the same order twice**
  - Try: Return the original outcome for the same operation key.
  - Planned implementation: `scenarios/s015_idempotency/src/main/java/com/tip/ecommerce/s015_idempotency/`
  - Planned note: `docs/data-consistency/s015_idempotency.md`

<a id="s016"></a>
- [ ] **S016 — Concurrent updates** · Scenario: **Two orders request the final item**
  - Try: Use an atomic update or version check to protect an invariant.
  - Planned implementation: `scenarios/s016_stock_concurrency/src/main/java/com/tip/ecommerce/s016_stock_concurrency/`
  - Planned note: `docs/data-consistency/s016_stock_concurrency.md`

<a id="s017"></a>
- [ ] **S017 — Saga and compensation** · Scenario: **Payment fails after stock reservation**
  - Try: Model reserve, pay, and release as explicit steps.
  - Planned implementation: `scenarios/s017_saga_compensation/src/main/java/com/tip/ecommerce/s017_saga_compensation/`
  - Planned note: `docs/data-consistency/s017_saga_compensation.md`

<a id="s018"></a>
- [ ] **S018 — Architect Level: recovery** · Scenario: **The coordinator crashes after payment**
  - Try: Persist progress and reconcile ambiguous outcomes without double charging.
  - Planned implementation: `scenarios/s018_saga_recovery/src/main/java/com/tip/ecommerce/s018_saga_recovery/`
  - Planned note: `docs/data-consistency/s018_saga_recovery.md`

## Messaging and events

<a id="s019"></a>
- [ ] **S019 — Asynchronous events** · Scenario: **Notify after an order is accepted**
  - Try: Introduce one broker only when this exercise needs it.
  - Planned implementation: `scenarios/s019_async_event/src/main/java/com/tip/ecommerce/s019_async_event/`
  - Planned note: `docs/messaging/s019_async_event.md`

<a id="s020"></a>
- [ ] **S020 — At-least-once delivery** · Scenario: **The same event arrives twice**
  - Try: Track event identity and make the side effect safe to repeat.
  - Planned implementation: `scenarios/s020_consumer_deduplication/src/main/java/com/tip/ecommerce/s020_consumer_deduplication/`
  - Planned note: `docs/messaging/s020_consumer_deduplication.md`

<a id="s021"></a>
- [ ] **S021 — Transactional outbox** · Scenario: **The database commits while the broker is down**
  - Try: Write the business record and outgoing event in one local transaction.
  - Planned implementation: `scenarios/s021_transactional_outbox/src/main/java/com/tip/ecommerce/s021_transactional_outbox/`
  - Planned note: `docs/messaging/s021_transactional_outbox.md`

<a id="s022"></a>
- [ ] **S022 — Dead letters and replay** · Scenario: **One invalid event blocks progress**
  - Try: Separate transient failures from poison events and replay deliberately.
  - Planned implementation: `scenarios/s022_dead_letter/src/main/java/com/tip/ecommerce/s022_dead_letter/`
  - Planned note: `docs/messaging/s022_dead_letter.md`

<a id="s023"></a>
- [ ] **S023 — Event versioning and ordering** · Scenario: **Old and new consumers run together**
  - Try: Define compatible schemas and ordering boundaries.
  - Planned implementation: `scenarios/s023_event_evolution/src/main/java/com/tip/ecommerce/s023_event_evolution/`
  - Planned note: `docs/messaging/s023_event_evolution.md`

<a id="s024"></a>
- [ ] **S024 — Architect Level: event recovery** · Scenario: **Replay a day of events after an outage**
  - Try: Handle deduplication, retention, ordering, and recovery throughput.
  - Planned implementation: `scenarios/s024_event_recovery/src/main/java/com/tip/ecommerce/s024_event_recovery/`
  - Planned note: `docs/messaging/s024_event_recovery.md`

## Gateway and security

<a id="s025"></a>
- [ ] **S025 — Gateway routing** · Scenario: **One entry point routes to two services**
  - Try: Add a gateway after direct service calls are understood.
  - Planned implementation: `scenarios/s025_gateway_routing/src/main/java/com/tip/ecommerce/s025_gateway_routing/`
  - Planned note: `docs/gateway-security/s025_gateway_routing.md`

<a id="s026"></a>
- [ ] **S026 — Authentication and authorization** · Scenario: **A token is valid but the action is forbidden**
  - Try: Distinguish identity checks from action and ownership checks.
  - Planned implementation: `scenarios/s026_token_validation/src/main/java/com/tip/ecommerce/s026_token_validation/`
  - Planned note: `docs/gateway-security/s026_token_validation.md`

<a id="s027"></a>
- [ ] **S027 — Service identity** · Scenario: **Orders calls stock as a machine**
  - Try: Separate user identity from a service credential.
  - Planned implementation: `scenarios/s027_service_identity/src/main/java/com/tip/ecommerce/s027_service_identity/`
  - Planned note: `docs/gateway-security/s027_service_identity.md`

<a id="s028"></a>
- [ ] **S028 — Rate limiting** · Scenario: **One caller floods the stock endpoint**
  - Try: Define a key, request budget, and rejection behavior.
  - Planned implementation: `scenarios/s028_rate_limiting/src/main/java/com/tip/ecommerce/s028_rate_limiting/`
  - Planned note: `docs/gateway-security/s028_rate_limiting.md`

<a id="s029"></a>
- [ ] **S029 — Architect Level: trust boundaries** · Scenario: **A caller bypasses the gateway**
  - Try: Design downstream verification, credential rotation, and least privilege.
  - Planned implementation: `scenarios/s029_trust_boundaries/src/main/java/com/tip/ecommerce/s029_trust_boundaries/`
  - Planned note: `docs/gateway-security/s029_trust_boundaries.md`

## Observability

<a id="s030"></a>
- [ ] **S030 — Correlated logs** · Scenario: **Follow one request through two services**
  - Try: Carry a request identifier without logging credentials.
  - Planned implementation: `scenarios/s030_correlation/src/main/java/com/tip/ecommerce/s030_correlation/`
  - Planned note: `docs/observability/s030_correlation.md`

<a id="s031"></a>
- [ ] **S031 — Metrics and traces** · Scenario: **Find the slow hop**
  - Try: Compare request rate, failures, latency, and a distributed trace.
  - Planned implementation: `scenarios/s031_metrics_traces/src/main/java/com/tip/ecommerce/s031_metrics_traces/`
  - Planned note: `docs/observability/s031_metrics_traces.md`

<a id="s032"></a>
- [ ] **S032 — Architect Level: incident diagnosis** · Scenario: **Latency rises while errors stay low**
  - Try: Use service objectives and saturation signals to distinguish causes.
  - Planned implementation: `scenarios/s032_incident_diagnosis/src/main/java/com/tip/ecommerce/s032_incident_diagnosis/`
  - Planned note: `docs/observability/s032_incident_diagnosis.md`

## Testing and delivery

<a id="s033"></a>
- [ ] **S033 — Contract testing** · Scenario: **A provider changes a required field**
  - Try: Catch incompatible behavior before deploying both services.
  - Planned implementation: `scenarios/s033_contract_testing/src/main/java/com/tip/ecommerce/s033_contract_testing/`
  - Planned note: `docs/testing-delivery/s033_contract_testing.md`

<a id="s034"></a>
- [ ] **S034 — Integration and failure testing** · Scenario: **A dependency disappears during a request**
  - Try: Verify bounded failure with controlled dependencies.
  - Planned implementation: `scenarios/s034_failure_testing/src/main/java/com/tip/ecommerce/s034_failure_testing/`
  - Planned note: `docs/testing-delivery/s034_failure_testing.md`

<a id="s035"></a>
- [ ] **S035 — Health checks and graceful shutdown** · Scenario: **A service stops during traffic**
  - Try: Separate readiness from liveness and finish in-flight work.
  - Planned implementation: `scenarios/s035_health_shutdown/src/main/java/com/tip/ecommerce/s035_health_shutdown/`
  - Planned note: `docs/testing-delivery/s035_health_shutdown.md`

<a id="s036"></a>
- [ ] **S036 — Architect Level: compatible rollout** · Scenario: **Old and new versions overlap**
  - Try: Plan expand-and-contract changes and rollback limits.
  - Planned implementation: `scenarios/s036_safe_rollout/src/main/java/com/tip/ecommerce/s036_safe_rollout/`
  - Planned note: `docs/testing-delivery/s036_safe_rollout.md`

## Scaling and performance

<a id="s037"></a>
- [ ] **S037 — Stateless services** · Scenario: **Two replicas handle the same caller**
  - Try: Keep correctness independent of which instance receives a request.
  - Planned implementation: `scenarios/s037_stateless_scaling/src/main/java/com/tip/ecommerce/s037_stateless_scaling/`
  - Planned note: `docs/scaling-performance/s037_stateless_scaling.md`

<a id="s038"></a>
- [ ] **S038 — Caching** · Scenario: **Stock changes while a cached value remains**
  - Try: Choose TTL, invalidation, and stale-data behavior.
  - Planned implementation: `scenarios/s038_cache_consistency/src/main/java/com/tip/ecommerce/s038_cache_consistency/`
  - Planned note: `docs/scaling-performance/s038_cache_consistency.md`

<a id="s039"></a>
- [ ] **S039 — Backpressure** · Scenario: **Events arrive faster than consumers can work**
  - Try: Bound queues and reason about lag and partition capacity.
  - Planned implementation: `scenarios/s039_backpressure/src/main/java/com/tip/ecommerce/s039_backpressure/`
  - Planned note: `docs/scaling-performance/s039_backpressure.md`

<a id="s040"></a>
- [ ] **S040 — Architect Level: capacity and recovery** · Scenario: **A traffic spike coincides with an outage**
  - Try: Balance resource budgets, degraded behavior, and recovery objectives.
  - Planned implementation: `scenarios/s040_capacity_recovery/src/main/java/com/tip/ecommerce/s040_capacity_recovery/`
  - Planned note: `docs/scaling-performance/s040_capacity_recovery.md`
