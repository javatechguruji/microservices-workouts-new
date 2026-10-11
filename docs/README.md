# Scenario notes

[Roadmap](../topics.md) · [Workspace](../README.md)

Create a subject folder only when its first scenario note is written. Use the exact
ID and descriptive stem from the roadmap. Keep Architect Level notes in the same subject
folder as the related concepts. Do not create empty notes for the full roadmap.

## Short story format

Each note uses these sections, with simple English and a concrete example:

1. **Situation** — who is calling which service, and why?
2. **Problem** — what breaks or becomes difficult?
3. **Idea** — explain the concept and any new terms briefly.
4. **Try it** — exact prerequisites, startup steps, request, and cleanup commands.
5. **Observed result** — record actual output after running; distinguish expected from observed.
6. **Change one thing** — one controlled failure or variation and its result.
7. **Interview answer** — a short explanation, tradeoff, and limitation.
8. **Architect Level scenarios** — connect to the advanced roadmap entry for this subject.

At the top, link to the exact roadmap anchor and existing implementation package.
Every implemented package contains a small README linking back to both the note and roadmap.
The roadmap links to both artifacts once they exist. Planned paths stay plain text.

## Java organization

Keep all scenario controllers, services, DTOs, entities, repositories, helpers, exceptions,
and scenario-specific Java configuration directly in `com.tip.ecommerce.<scenario_stem>`.
Tests use the same package under `src/test/java`. Do not add layer subpackages.
Application entry points and truly shared configuration stay outside the scenario package.
`application.yml` goes in `src/main/resources`; other scenario resources include the ID
in their filename or folder. Preserve third-party library boundaries.

IDs do not isolate execution. Use focused tests, profiles, or explicit configuration so
unrelated examples do not start together. Avoid a shared business-code library initially.
For each example, document exactly how many processes it starts and how to stop them.

Check completion only after running and reviewing the example and explaining its result.
Recaps may reuse an implementation, but must say which scenario they reuse.
