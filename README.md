# Microservices workouts

A small, scenario-based workspace for learning microservices and preparing for interviews.
The ecommerce application has been removed from the active workspace. Orders and stock
can still supply simple examples without recreating a shopping platform.

Start with [topics.md](topics.md). It contains 40 planned scenarios with permanent IDs,
concrete problems, and intended code and note paths. Nothing is marked complete yet.

## Learn one scenario at a time

1. Begin with S001: decide what orders owns and what stock owns.
2. Use S002 to run two tiny independent processes.
3. Use S004 for one HTTP call, then S009 to see what happens when it is slow.
4. Add databases, a broker, a gateway, or monitoring only for the relevant exercise.

The initial organization is complete. Scenario implementation is the next phase after
roadmap review; there is currently no runnable application, Maven build, or Docker stack.

## Layout

- `topics.md`: roadmap, permanent IDs, and planned artifact paths.
- `docs/`: writing conventions and cleanup record; subject folders appear with their first note.
- `reference/`: your original organization prompt and any later reference samples.
- `scenarios/`: created when the first example is implemented, with one small Maven project per scenario.

Use [document conventions](docs/README.md) for the short story format and code links.
See [cleanup and recovery](docs/workspace-cleanup.md) for what was preserved and removed.
