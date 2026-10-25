# Scenario notes

[Roadmap](../topics.md) · [Workspace](../README.md) · [Workspace rules](../AGENTS.md)

Create a subject folder only when its first scenario note is written. Group by concept
and retain the roadmap scenario ID. For new or reorganized lesson series, use a two-digit
reading-order prefix and descriptive topic name; keep roadmap links in sync. Keep Architect
Level notes in the same subject folder. Do not create empty notes for the full roadmap.

For image/document pairs, follow the authoritative
[handwritten teaching image and Markdown rules](../AGENTS.md#handwritten-teaching-images-and-matching-markdown).
Use matching filename stems, place PNGs in `images/`, display them inline in Markdown,
and explain the same numbered steps in both artifacts. Each step needs a short image
snippet and a fuller Markdown excerpt with simple inline comments; clearly label
application code versus framework code.

## Short story format

Write finished lessons for learners. Omit editing history, progress reports, explanations
of document organization, and commentary about simplifying or revising the material.
Use business names such as inventory DB in the story; keep hosting and connection details
in configuration instructions. Describe the actual code behavior accurately.

Teach through one familiar story, using ordinary language. Start with what a customer
or operator experiences, then introduce the concept needed to explain it. Define technical
words at first use. Explain each setting next to the behavior it controls. Keep complex
architect discussions for a later teaching stage, not as prerequisites for the first lesson.

Follow the scenario structure in [AGENTS.md](../AGENTS.md):

1. Story, problem and the concept needed to solve it.
2. Implementation steps with focused excerpts and source links.
3. Required application configuration.
4. Demonstration and honest expected or observed evidence.
5. Numbered testing scenarios with complete requests and relevant results.
6. Interview practice at the end, using short answers tied to the example.

Label broad-topic lessons Beginner or Architect Level. Keep advanced design-only
material in linked lessons and identify it as explanation rather than implemented
behavior. Reuse one implementation for closely related variations instead of
copying controllers and services for every test case. Use Grafana only when the
selected concept needs it; ordinary logs and response bodies are often sufficient.

Start with [S030 — Logging: follow an order into inventory](observability/s030_logging.md). The workspace contains four independent Maven applications: `order-service`,
`inventory-service`, `payment-service`, and `gateway-service`. Reuse them for later exercises.
[Distributed transactions](data-consistency/00-distributed-transactions-overview.md)
uses the payment application with Kafka choreography.
[Request rate limiting](gateway-security/00-request-rate-limiting-overview.md)
uses the gateway and existing inventory reads with shared Redis state.
[Authentication and authorization](authentication-authorization/00-authentication-authorization-overview.md)
uses Keycloak, Gateway JWT validation and service-level order/inventory access rules.
Study [Gateway authentication](gateway-security/01-s026-gateway-authentication.md) first,
then order-service authentication and authorization.
Shared setup, credentials and Postman login are in the Gateway lesson; S027 and S029 reuse them.
[Caching](scaling-performance/00-caching-overview.md) uses S042 order history for Hibernate
and S038 order/inventory flows for local and Redis stock-display caching.

At the top, link to the exact roadmap anchor and existing implementation package.
Keep documentation under `docs/`; do not place README files inside Java packages.
The roadmap links to the lesson and implementation once they exist. Planned paths stay plain text.

## Java organization

Keep all scenario controllers, services, DTOs, entities, repositories, helpers, exceptions,
and scenario-specific Java configuration directly in `com.tip.ecommerce.<scenario_stem>`.
Use Postman demos to demonstrate scenarios. Do not add JUnit tests or layer subpackages.
Application entry points and truly shared configuration stay outside the scenario package.
Shared exception handlers and reusable exception types belong in the service’s common
`exception` package.
`application.yml` goes in `src/main/resources`; other scenario resources include the ID
in their filename or folder. Preserve third-party library boundaries.

IDs do not isolate execution. Use profiles or explicit configuration so
unrelated examples do not start together. Avoid a shared business-code library initially.
The learner runs the required applications in IntelliJ and uses Postman. Prefer a
local embedded H2 database per service with familiar tables and startup seed data.
Simple scenarios use in-memory H2; restart-recovery scenarios use file-backed H2 and
seed only missing data. Kafka and background delivery are allowed when the concept
requires them, using the existing AWS Kafka and user-managed tunnel. Observability
retains its synchronous two-service flow and existing Grafana infrastructure. Explain
expected evidence and recovery without duplicating infrastructure provisioning or
terminal run/test commands.

Distinguish implementation/compilation from live verification. Keep roadmap completion
boxes open until the required demonstrations have been run, reviewed and explained.
When the user reserves live execution for themselves, provide expected results and
leave that verification pending.
Recaps may reuse an implementation, but must say which scenario they reuse.
