# Teaching guide — reusable prompt and rules

Copy this file into another workspace as `AGENTS.md` to use these instructions there.
If an `AGENTS.md` already exists, merge the teaching guidance and preserve its project
rules. `AGENTS_REFERENCE.md` itself is a portable reference, not an automatically loaded
instruction file. Discover the destination project's topic and references from its own
context; these instructions do not mandate a particular business example or technology.

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
- Teach only the current concept in depth. For related concepts, give the minimum needed
  to understand the example and link to their own lesson. Put architecture depth later.
- Avoid repeating explanations in prose, tables, comments, and screenshots. Each should
  add information the learner needs; do not explain self-evident messages again.

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
