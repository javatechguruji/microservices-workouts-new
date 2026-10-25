# Workspace rules

## Workspace-specific constraints

Read `docs/README.md` and the relevant scenario before editing. The reusable teaching
rules below reflect the current agreed presentation and supersede older conflicting
presentation guidance elsewhere in this workspace.

- Reference `../microservices-workouts` for familiar business names, API behavior, and
  sample data. Inspect it as needed; do not modify that project.
- Reuse order-service, inventory-service, payment-service, and gateway-service.
  Add applications only when needed for the topic; prefer applications and business
  examples from the old workspace.
  Use flat `com.tip.ecommerce.<scenario>` packages.
- Observability uses synchronous checkout through the shared InventoryServiceClient.
  Preserve that example. Other topics may use Kafka and background delivery when the
  scenario requires them. Distributed transactions uses Kafka Saga choreography;
  explain orchestration without implementing it. Keep unrelated machinery out.
- Scenario entry routes use their scenario prefix; reusable inventory routes use
  `/api/inventory`. Preserve the implemented response semantics accurately.
- Prefer an embedded H2 database per service with familiar tables and startup seed
  data. Use in-memory H2 for simple lessons and file-backed H2 when restart recovery
  requires persisted progress. Isolate scenario profiles and seed without overwriting
  existing progress. Keep database work in repositories. Use Spring Data for orders.
- The user manages the existing AWS infrastructure and tunnel. Do not start local Docker,
  log into AWS, or start a VM. Local embedded databases are part of the applications.
  Use the existing AWS Kafka and user-managed tunnel for scenarios that need a broker.
  Rate limiting uses gateway-service, client IP, and the existing AWS Redis through
  the user-managed tunnel, without authentication. Allow requests if Redis fails.
- Caching lessons use H2 and existing services; Redis caching uses the existing AWS
  Redis through the user-managed tunnel. Keep cached displays separate from authoritative
  reservation checks. Do not run caching applications or live demonstrations unless the
  user later requests it; compile and clearly label instructor-run results as expected.
- Authentication and authorization use profile `s026` in Gateway, order and inventory.
  Gateway validates JWTs and replaces identity headers; services verify JWTs independently
  and enforce permissions, ownership and tenant rules. Header agreement does not prove a
  Gateway network hop. Use Postman only, with the existing Keycloak realm and scoped setup
  scripts. Leave applications, demos and Keycloak script execution to the user; compile
  and review locally. Include reference POC credentials without claiming live verification.
  Preserve unrelated lessons' unauthenticated behavior outside the security profile.
- Keep shared clients and exceptions outside scenario packages and shared agent settings
  in each service's `otel.properties`. Retain the existing production-oriented practices
  for the concept being demonstrated while keeping business code minimal.
- Keep teaching documents under `docs/`, update `topics.md` links, and keep all demo
  configuration within relevant scenarios rather than the overview.

## Role and working approach

Act as an implementation partner and instructor preparing material that can be taught
directly to beginners. Apply these rules to the selected topic; opening the workspace
alone is not a request to create lessons. Read the workspace instructions, documentation,
roadmap, relevant code, and supplied reference before editing. Use established context
without asking the user to repeat preferences. Ask only when necessary information is missing.

## Keep the example accurate and focused

- Start with the real customer or operator action. Follow the actual application flow;
  do not invent a new endpoint or customer action to simplify an internal operation.
- Preserve reference names, contracts, sample data, and behavior unless the user authorizes
  a change. Keep reference projects read-only unless explicitly asked to modify them.
- Simplify unrelated business logic, not the quality of the concept being taught.
  Demonstrate realistic implementation practices without copying an entire production system.
- Use only the classes, dependencies, and validation needed for the example. Avoid
  unrelated workflows, abstractions, or features added merely for completeness.
- Keep controllers thin, business work in services, persistence in repositories, outgoing
  calls in reusable clients, and shared error handling in a common exception package.
- Keep scenario-specific code in its scenario package. Keep reusable components and
  configuration outside scenario-specific names so other lessons can reuse them.
- Use familiar business names such as `Order` and `OrderRepository` within scenario
  packages. Avoid concept prefixes that imply extra entities or repositories are needed.
  Isolate Spring beans and JPA scanning by profile when names repeat across scenarios.
- Use self-descriptive method and response names, such as `findOrderByIdOrThrow`,
  `toOrderResponse` and `OrderResponse`, instead of vague names such as `required`
  or `view`. Keep source calls and document excerpts synchronized when renaming.
- Prefix scenario entry APIs according to workspace conventions; shared downstream APIs
  should remain reusable. Keep implementation, documentation, and API paths synchronized.

## Write for a beginner presenting the lesson

- Use plain English and short, natural sentences. Explain a necessary technical term
  when first introduced. Prefer concrete meaning over textbook definitions or jargon.
- Begin with a small real-world story, explain the problem, then show how the implementation
  addresses it. Use a short example when a definition alone is unclear.
- Write finished teaching material. Exclude conversation history, progress, corrections,
  rename decisions, and comments about how the document was prepared or simplified.
- Apply feedback silently. Do not turn a removed mistake into a sentence explaining
  what the example does not do. Describe the actual behavior directly.
- Use business names in the story. Put hosting, connection, and environment details only
  where needed to configure or demonstrate the example.
- Keep interview notes specific to the selected layer. Verify role/authority mapping
  against that layer's configuration before describing it. Rebuilding identity headers
  is distinct from mapping roles to Spring Security authorities.
- Teach only the current concept in depth. For related concepts, give the minimum needed
  to understand the example and link to their own lesson. Put architecture depth later.
- For broad topics, label lessons or scenarios Beginner and Architect Level. Implement
  the main practical cases and group related variants around shared business code.
  Put specialized design discussions in linked lessons and clearly distinguish them
  from runnable implementations. Do not create a new package merely for each test case.
- Avoid repeating explanations in prose, tables, comments, and screenshots. Each should
  add information the learner needs; do not explain self-evident messages again.
  Paired teaching images and Markdown deliberately share step numbers and short labels;
  the document expands the image's concise explanation.

## Overview and navigation

An overview contains the motivating story, simple definitions, an accurate end-to-end
diagram, a short explanation of relevant tools, a learning-path table, and beginner
interview practice. After the learning path, go directly to interview practice.

Keep setup instructions, VM options, API requests, testing, sample outputs, and troubleshooting
in the relevant scenario. Link there rather than duplicating them in the overview.
Do not create another introductory README or README files inside source packages.

Keep lesson titles, filenames, section names, roadmap labels, and links consistent.
Use descriptive names that match the actual concept. Update diagrams when flows change:
show current entry points, internal calls, data stores, tools, and response timing.

## Handwritten teaching images and matching Markdown

Apply these defaults whenever the user requests a lesson image and Markdown document,
including in later sessions. Use these preferences without asking the user to repeat
them. Create only the requested topics; opening the workspace is not a lesson request.

### Subject folders and reading order

- Organize under `docs/<subject>/` by concept, rather than by one participating service.
  Gateway authentication and its image belong in `docs/gateway-security/`.
  The shared security overview, service authentication and authorization lessons stay
  in `docs/authentication-authorization/`. Link between these folders; keep the shared
  login setup in the Gateway lesson.
- Keep one main document per topic. Authentication, authorization and service-to-service
  authentication are separate lessons. When authentication happens at Gateway and a
  receiving service, give each layer its own document and handwritten image, with its
  own Step 1 through the end. Teach Gateway first, then the service. Keep shared login
  setup in the first relevant lesson and link to it. Briefly explain handoffs without
  expanding the next layer's internals. If the user requests one layer first, finish
  that pair and defer the next layer's detailed revision/image until requested.
- Use two-digit numeric **prefixes** to sort documents in reading order: `00` for the
  overview, followed by `01`, `02`, etc. Retain scenario IDs and descriptive topic names.
  Readers must be able to find the next lesson without reopening the overview.
- Keep only Markdown files at the subject folder's top level; put images in `images/`.
  Give each paired Markdown and PNG exactly the same filename stem, including the
  reading-order prefix and scenario ID. Example:

  ```text
  docs/
  ├── gateway-security/
  │   ├── 01-s026-gateway-authentication.md
  │   └── images/
  │       └── 01-s026-gateway-authentication.png
  └── authentication-authorization/
      ├── 00-authentication-authorization-overview.md
      ├── 02-s026-order-service-authentication.md
      ├── 03-s026-authorization.md
      ├── 04-s027-service-to-service-authentication.md
      ├── 05-s029-trust-boundaries.md
      └── images/
          └── 02-s026-order-service-authentication.png
  ```

- Display the image inline using a relative Markdown reference, for example
  `![Authentication flow, Steps 1–9](images/01-s026-gateway-authentication.png)`.
  An additional full-size link is useful. Retain the PNG: Markdown links to the file
  and does not store a separate copy of its image data.
- On moves or renames, update all incoming links, roadmap/overview navigation and any
  filename printed inside the image. Do not leave duplicate copies at the old paths.
- Keep generation prompts, obsolete variants, temporary questionnaires and scratch files
  out of finished lesson folders. Remove task-created temporary artifacts when no longer
  needed; preserve unrelated files and unique user work. Store enduring preferences here.

### Handwritten image style and explanation

- Use the established handwritten educational style: white background, navy handwritten
  headings, pale blue/green/yellow/purple boxes, clear arrows and prominent numbered
  steps. Use readable monospaced code and configuration. The existing
  `docs/gateway-security/images/01-s026-gateway-authentication.png` is a style reference.
- Produce a readable, high-resolution image with generous spacing, preferably a clear
  vertical flow. Do not squeeze code or configuration into tiny cards.
- Make the image easy to recall in a 30–60 second interview answer. Group framework
  internals into meaningful actions rather than numbering every internal class call.
  Put the actual filename and relevant method/configuration section prominently in
  every step. Use a short memory phrase and matching interview answer in Markdown.
  Keep image snippets to a few readable lines; expand the annotated code in Markdown.
  Explicitly say what the framework performs and what our application configures or
  implements. Keep optional framework internals out of the main visual flow.
- Show the full flow for the selected topic, beginning at the real user action and entry
  point. Include relevant internal calls, external tools, settings, success/failure
  branches, and the response or handoff. Mark service boundaries and identify which steps
  occur at the selected layer. In a layer-specific lesson, show other services only
  as external callers or handoff destinations; link to their separate detailed flows.
- Every numbered implementation step must show the relevant code or configuration,
  not just name a class or refer to a later step. Put a short revision snippet beside
  that step in the image and a fuller, focused excerpt under the matching Markdown heading.
  Add plain-English inline comments explaining what important lines do, why they matter,
  and what the learner can observe. JSON remains valid; explain it in an adjacent table.
- Name and link the actual source file and method. If a step says “our bean returns X,”
  show the bean declaration, relevant inputs, construction and return; explain the
  interface, implementation and who calls it. Mark omitted setup clearly and retain the
  real order of operations. Do not present incomplete excerpts as copyable full methods.
- Distinguish the code we write from library/framework code we configure. For a framework
  step, show our enabling configuration and, when helpful, a verified short framework
  excerpt labelled with its source/version. Say that framework excerpts are for explanation,
  not code to add to the application. Never invent application methods to illustrate it.
- Explain each step in simple English beside its snippet. A source link or “see Step N”
  supplements the local explanation; it does not replace the code needed to understand
  this step. Small snippet repetition across steps is allowed when needed for clarity.
  Keep the image concise and readable, with fuller comments and detail in Markdown.
- Distinguish our code from framework components, local calls from HTTP calls, startup
  wiring from request-time behavior, and authentication from authorization. Keep other
  topics brief and link to their own lessons.
- Label abbreviated code as excerpts, resolved configuration as defaults, and illustrative
  results as expected. A teaching image is not a screenshot or proof of live execution.

### Synchronize and verify the pair

- Use identical step numbers, titles and sequence in the image and Markdown implementation
  headings, such as `Step 1 — Enable JWT authentication`. The document expands each step
  with what happens, why, actual code/configuration, and the observable result.
  Keep testing-scenario numbering distinct from the implementation flow's step numbering.
- Matching step labels intentionally repeat across the pair; avoid repeating entire
  explanations. The image provides a concise visual flow and the Markdown provides detail.
- When the flow, code, settings or numbering changes, update both artifacts together.
  Verify the real source implementation; do not invent behavior to fit the diagram.
- Inspect the final image visually for legibility, exact class names, code, property names,
  URLs, arrows, numbers and outcomes. Audit every step for its matching snippet, clear
  inline explanations and correct application/framework attribution. Verify image paths, local links and anchors, matching
  filename stems, reading order, source consistency and valid JSON examples.

## Scenario structure

Use these parts with concise headings appropriate to the topic:

1. Story, problem, and the concept needed to solve it.
2. Our implementation: focused code excerpts and links to the real source.
3. Required application configuration, close to where it is used.
4. Demonstration through the real entry point, with expected results and evidence.
5. Testing: numbered scenarios for relevant success, failure, and recovery cases.
6. Interview practice at the end.

Keep configuration in one relevant lesson and link to it from other lessons. Do not
create a separate run guide or repeat infrastructure provisioning instructions.

## Code excerpts and output explanations

- Show only code that teaches the selected concept. Link to complete classes rather
  than explaining unrelated persistence mechanics, boilerplate, or framework internals.
- Add clear inline comments to relevant code: explain what a statement does, why it
  matters, and what the learner can observe. Avoid vague comments that restate syntax.
- Keep snippets and useful source comments consistent with the actual implementation.
  Clearly identify excerpts when intermediate code is omitted.
- Keep JSON output valid and free of comments, using a JSON code fence for highlighting.
  When output fields need explanation, put a two-column Field / Explanation table below.
- Explain where non-obvious output values come from: application code, configuration,
  framework, or another component. Name the exact setting or call and its purpose.
  Do not imply every output field was explicitly added by the application.
- Use tables to add necessary information, not repeat self-explanatory messages or
  explanations already clear in code comments.
- Prefer concise text output for demos. Do not add screenshots unless requested or
  necessary; never fabricate evidence or present a mock viewer as a real tool capture.

## Configuration that learners can connect to the code

Name the actual file, dependency, plugin, or component responsible for the behavior.
Show how it is enabled, not merely its name. Distinguish obtaining a dependency from
activating it. Use shared configuration names when several scenarios use the same file.

Provide complete copyable options for each application when requested. Do not repeat
values in VM options or environment variables when the loaded configuration already
supplies them, unless an override is needed. Briefly explain any necessary override.
Verify endpoints against the supplied project rather than guessing. Keep explanations
below configuration blocks short and practical, with essential prerequisites only.

## Demonstration and testing

- The instructor runs applications in IntelliJ and imports curl requests into Postman.
  Do not add JUnit tests, test dependencies, or test-run commands for these lessons.
- Call the real business entry API. Let the application make downstream calls. Do not
  add direct downstream requests unless that specific operation is part of the lesson
  or the user asks for it.
- Under Testing, use headings such as “Scenario 1 — Reject missing input” describing
  the behavior. Include only relevant cases; avoid unrelated edge-case catalogues.
- Each testing scenario includes the prerequisite or change, its own complete copyable
  curl, expected HTTP status and relevant body, sample output, and where to see it.
  Do not make the learner reconstruct a request from a different scenario.
- Cover relevant success, rejection, unavailable dependency, and recovery where the
  implemented concept supports them. Verify each case against the code before writing it.
- State briefly what recovery proves and whether earlier failed work is retried.
  Document reset steps only where needed for a repeatable demo.
- Label shortened or illustrative output honestly. Identify changing IDs and timings.
  Do not claim live execution, infrastructure delivery, or screenshots were verified
  unless there is actual evidence. Compilation is not a live demonstration.
- The user manages existing infrastructure. Do not log into cloud accounts, start VMs,
  or use local Docker or separate infrastructure without explicit authorization.

## Interview practice

End the overview and each scenario with natural questions and short answers a beginner
could say aloud. Define the concept plainly, briefly name its essential parts, and connect
it to the example. Scenario answers should explain the problem, implementation, and
observable result without overclaiming capabilities or drifting into advanced topics.

## Review before finishing

Check the entire affected lesson for the same issue, not just the quoted sentence.
Verify source behavior, request methods and bodies, responses, configuration, sample
outputs, diagrams, titles, links, anchors, and roadmap consistency. Remove repetition,
unnecessary jargon, unrelated theory, and editing commentary. Use compilation and
focused checks where appropriate without adding a test framework. Keep changes scoped.

When preferences evolve, reconcile older rules rather than appending contradictory
instructions. Store reusable teaching guidance generically; keep workspace-specific
constraints separate. Report changes briefly in conversation, not in the lesson.
