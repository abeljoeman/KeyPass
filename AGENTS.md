# RAHSA Agent Instructions

These instructions apply to Codex work in this repository.

## 1. Mandatory startup sequence

Before modifying anything, read in this order:

1. `docs/GOVERNANCE_STATUS.md`
2. `ENGINEERING_PRINCIPLES.md`
3. `docs/WORKFLOW.md`
4. `TASKS.md`
5. The PRD/TSD/ADR/threat-model sections referenced by the exact active task.

Verify branch, HEAD, and working-tree state before work.

## 2. Terminology

- **RAHSA** = the product being developed.
- **KeyPass** = upstream open-source reference/base or legacy technical identifiers that still contain `keypass`.

Do not rename legacy package/class paths casually. Do not call the current product KeyPass in new planning or product documentation.

## 3. Hard execution gate

Codex may modify application code only when `TASKS.md` exposes exactly one approved active `Txxx` task.

If `TASKS.md` says `EXECUTION_STATUS: PLANNING_FREEZE` or `ACTIVE_TASK: NONE`:

- do not implement features;
- do not opportunistically refactor code;
- do not execute parked release tasks;
- stop after reporting that no implementation task is active.

Documentation/planning changes are allowed only when the owner explicitly asks for a planning/governance documentation task.

Run the repository preflight before implementation:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

If preflight fails, do not bypass it. Resolve the governance/task state first.

## 4. One task at a time

For an active task:

- Work only on the exact Txxx scope.
- Do not start the following task in the same run.
- Do not change unrelated files.
- If the task requires a product/security/architecture decision not already approved, stop and return the issue to planning.

## 5. Reuse-first audit

Before writing a new implementation or adding a dependency, check in order:

1. retained RAHSA implementation;
2. upstream KeyPass implementation/pattern;
3. Android / AndroidX / Jetpack;
4. mature, maintained, license-compatible open source;
5. minimum local implementation.

Document a new dependency's purpose, source repository/project, license, why earlier options are insufficient, and relevant security/privacy impact.

Never reuse security-sensitive inherited code blindly.

## 6. Preserve the v0.2 baseline

`v0.2-prototype` is the stable baseline. Preserve working vault lifecycle, KDBX/Kotpass persistence, CRUD, search, generator, locking, clipboard protections, fail-closed behavior, and corrupt-vault protections unless the active approved requirement explicitly changes them.

Do not perform broad rewrites or architecture migrations as incidental work.

## 7. No custom cryptography

Do not invent crypto primitives, KDFs, key wrapping formats, encrypted containers, recovery formats, or authentication schemes. Use approved Android platform APIs and established libraries/formats.

## 8. Cost-aware model and reasoning policy

Choose the lowest-cost model and lowest reasoning effort reasonably likely to complete the task correctly and safely. State the chosen model, reasoning effort, and brief justification before an execution task.

### Preferred starting points

- `gpt-5.6-luna` + `low`: repository inspection, grep/search, status, smoke tests, formatting, mechanical edits, narrow low-risk tests.
- `gpt-5.6-terra` + `medium`: normal focused implementation with clear scope and moderate coding judgment.
- `gpt-5.6-sol`: reserve for security-sensitive vault/master-password/authentication semantics, difficult lifecycle/concurrency races, architecture-critical work, or a failed cheaper attempt.

Do not use Sol automatically. Do not raise reasoning effort merely because a higher option exists. `xhigh` or `max` require an explicit reason.

The goal is lowest expected total cost while preserving correctness, security, and sufficient verification.

## 9. Execution cycle

For each active Txxx:

1. Verify repo/branch/status.
2. Read the exact task and source-of-truth references.
3. Run governance preflight.
4. Inspect reuse options and current implementation.
5. Apply the smallest sufficient change.
6. Review full diff for unrelated changes.
7. Run targeted tests/build required by acceptance criteria.
8. Run smoke/device validation when required.
9. Only after validation, mark that one task complete.
10. Stage only task-owned files.
11. Commit one focused checkpoint.
12. Report checkpoint and stop.

Never mark a task complete because code merely compiles.

## 10. Public release work is parked

Do not perform Play Store/public-release preparation, trademark clearance, package/applicationId migration, release signing/store identity, listing work, or release compliance work until the owner explicitly resumes that workstream and TASKS.md activates it.
