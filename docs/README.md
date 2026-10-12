# Scenario notes

[Roadmap](../topics.md) · [Workspace](../README.md) · [Workspace rules](../AGENTS.md)

Create a subject folder only when its first scenario note is written. Use the exact
ID and descriptive stem from the roadmap. Keep Architect Level notes in the same subject
folder as the related concepts. Do not create empty notes for the full roadmap.

## Short story format

Write finished lessons for learners. Omit editing history, progress reports, explanations
of document organization, and commentary about simplifying or revising the material.
Use business names such as inventory DB in the story; keep hosting and connection details
in configuration instructions. Describe the actual code behavior accurately.

Teach through one familiar story, using ordinary language. Start with what a customer
or operator experiences, then introduce the concept needed to explain it. Define technical
words at first use. Explain each setting next to the behavior it controls. Keep complex
architect discussions for a later teaching stage, not as prerequisites for the first lesson.

Each note develops these points (headings may follow the story):

1. **Situation** — who is calling which service, and why?
2. **Problem** — what breaks or becomes difficult?
3. **Idea** — explain the concept and any new terms briefly.
4. **Our implementation** — link the actual classes and explain the scenario-specific statements, why they are needed, and the evidence they produce. Include short code excerpts with inline comments and matching useful comments in the source code. Describe behavior rather than edit history.
5. **Practice in Postman** — copyable curl request, expected response, and the local IntelliJ change for the experiment. Infrastructure setup belongs to the original workspace; do not repeat run commands or test-execution instructions.
6. **Verify in AWS Grafana / Observed result** — record actual output after running; distinguish expected from observed.
7. **Change one thing** — one controlled failure or variation and its result.
8. **Explain it back** — a short explanation in the learner’s own words.
9. **Architect Level scenarios** — connect to the advanced roadmap entry for this subject.
10. **Interview practice (always at the end)** — include realistic questions and short spoken answers specific to this document. Start with the concept, connect it to the example, and explain evidence or limitations. Include “What is observability?” in the overall introduction. Keep advanced questions in the later architect lesson. Never imply that unrun exercises or proposed features are verified experience.

Start with [S030 — Logging: follow an order into inventory](observability/s030_logging.md). The workspace currently
contains two independent Maven projects, `order-service` and `inventory-service`; reuse them
for later exercises and keep the workspace within three application projects.

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
The learner runs the two local microservices in IntelliJ and uses Postman. Each service owns an in-memory database reused across scenarios, with familiar seeded data. Grafana uses the existing observability infrastructure. Explain expected evidence and recovery, without duplicating infrastructure setup or terminal run/test commands.

Check completion only after running and reviewing the example and explaining its result.
Recaps may reuse an implementation, but must say which scenario they reuse.
