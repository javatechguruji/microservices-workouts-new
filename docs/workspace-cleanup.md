# Workspace cleanup

[Workspace](../README.md) · [New roadmap](../topics.md)

## Direction

This workspace now follows the [provided prompt](../reference/WORKSPACE_SETUP_PROMPT.md):
organize one project, establish stable scenarios, and stop before implementing the roadmap.
The referenced sample topics.md was not present during organization. The prompt alone
provided the structure. No previous scenario IDs existed in the old Topics.md.

## Preserved work

A verified archive of the complete pre-cleanup working tree, including ignored files,
old notes, build outputs, infrastructure state, IDE settings, and the reference prompt,
was written before removal:

`/private/tmp/microservices-workouts-before-cleanup-20261011-084116.tar.gz`

This archive is outside the workspace and has owner-only permissions. It excludes `.git`;
the existing repository and index were left intact. Pre-existing staged changes and deleted
UI files were not restored or reset. The user's separate backup remains the long-term copy;
this temporary archive may be cleared by the operating system.

To recover something, extract the archive into a separate directory and copy only the
needed files back. Avoid extracting over this reorganized workspace. Infrastructure state
and local configuration may contain credentials; keep the archive private.

## Removed from the active workspace

- Nine ecommerce services and common-library: checkout, stock, payment, catalog,
  customer preferences, discounts, ratings, notifications, and gateway implementations.
- Root Maven aggregation and Compose configuration tied to those applications.
- Docker support, Kubernetes manifests, AWS/Terraform tooling and local data files.
- Previous ecommerce documentation and Topics.md, replaced by the focused roadmap.
- Generated service build outputs and stale IntelliJ project/run configurations.

The old notes remain in the archive for selective adaptation. They require review because
their commands and links describe the removed ecommerce system. Frontend-specific topics
are outside this roadmap. Distributed systems concepts remain as small planned exercises.

This was filesystem cleanup only. No containers, volumes, cloud resources, or running
processes were stopped or deleted. There is no Java package move and no remaining build
to run. Verification checks roadmap IDs, anchors, local links, and planned path consistency.
