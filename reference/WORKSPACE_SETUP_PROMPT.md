# Reusable workspace organization prompt

## How to use

1. Open the target workspace in a new session.
2. Attach your latest approved `topics.md` and copy the workspace `AGENTS.md` into the target workspace root. The rules keep concept documents simple and preserve scenario organization.
3. Copy the prompt below and replace `<PROJECT NAME>` with the project to organize.
4. Review that project's result before moving to the next project.

Keep the latest `topics.md` with each project. It records stable scenario IDs, planned paths, and progress so work can resume without the original conversation.

## Prompt

Help me organize this workspace for interview preparation.

First inspect the workspace structure, existing code, documentation, and repository instructions. Use the attached topics.md as the reference for structure, writing style, and organization. Adapt the content to the target project; do not blindly copy its topics.

Work on ONE project at a time.

Start with: `<PROJECT NAME>`

Complete its roadmap and organization, then stop for my review before proceeding to another project.

### 1. Project grouping

- Check whether concepts belong in the correct project.
- Keep related concepts together and explain any proposed moves.
- Preserve existing work and avoid unrelated changes.

### 2. Topic roadmap

- Maintain `topics.md` at the project root.
- Rename `INTERVIEW_TOPICS.md` if it exists. If both files exist, reconcile their content without losing work.
- Group topics under clear, consistent subject headings.
- Cover important interview concepts and practical failure scenarios.
- Give every entry a concept name, a named scenario, and a checkbox.
- End every subject group with **Architect Level scenarios** for complex design, concurrency, scaling, and recovery exercises.
- Do not label the main content “beginner.”

### 3. Stable scenario IDs

- Assign permanent IDs such as `S001`, `S002`, and `S003`.
- Preserve existing IDs. Never renumber them when adding or moving topics.
- Give new scenarios the next unused ID. Never reuse a retired ID.
- Use a descriptive shared stem, such as `s007_bean_selection`.
- Add a stable roadmap anchor such as `#s007`.
- IDs are local to each project. When discussing multiple projects, qualify them as `<project-name>/S007`.

### 4. Implementation organization

- Each scenario owns one Java package, for example `com.example.project.s007_bean_selection`. Use the target project's actual base package.
- Keep all Java files written for that scenario directly in that package: controllers, services, repositories, entities, DTOs, helpers, exceptions, validators, and scenario-specific Java configuration.
- Avoid global controller/service/repository folders or scenario subpackages that scatter the example.
- Tests use the same package under `src/test/java`.
- Keep the application entry point and shared configuration outside scenario packages. `application.yml` belongs in `src/main/resources`.
- Include the scenario ID in the names or folders of scenario-specific resources.
- Preserve external library boundaries.
- Numbering does not isolate execution. Where examples conflict, use appropriate scenario-specific configuration or focused tests.

### 5. Document organization

- Group notes by subject, for example `docs/spring-core/s007_bean_selection.md` or `docs/spring-data-jpa/sXXX_optimistic_locking.md`. `sXXX` is a placeholder for an assigned ID.
- Use descriptive section folder names without section-number prefixes.
- Keep Architect Level scenario documents in their subject folder.
- Preserve the scenario ID and filename stem when moving a document.
- Create a subject folder when its first document is added.

### 6. Connect all three artifacts

- `topics.md` links to existing implementation packages and documents.
- Each document links back to its exact roadmap anchor and implementation.
- Each implementation package contains a small `README.md` linking back to the roadmap entry and document.
- Planned scenarios show intended package and document paths as plain text, not broken links.
- Do not create hundreds of empty documents or implementation packages.
- An interview recap may reference an existing scenario's implementation instead of copying it; document that reuse explicitly.

### 7. Writing style

- Use simple English, short explanations, and concrete examples.
- Explain technical terms when needed.
- Future concept notes follow a small story: situation → problem → idea → try it → observed result → change one thing → interview answer → Architect Level scenarios.
- Avoid huge theory sections.
- Preserve existing notes during organization and clearly identify anything that still needs review.

### 8. Scope and verification

- Initially build the roadmap and organize existing artifacts.
- Do not implement every planned scenario or write every concept note.
- Existing code does not automatically mean a scenario is complete. Mark completion only after running and reviewing the example and explaining its result.
- Verify unique IDs, local links, anchors, and package/path consistency.
- If Java packages move, update references and run an appropriate build.
- Report changes and any verification limitations.

The attached `topics.md` is an approved example, not evidence that its scenarios are implemented in this workspace.
