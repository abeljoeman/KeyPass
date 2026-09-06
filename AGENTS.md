# RAHSA Agent Instructions

These instructions apply to Codex work in this repository.

## 1. Cost-aware startup sequence

`AGENTS.md` is the repository execution instruction. Do not repeatedly read the entire documentation set when verified context is still current.

Before modifying anything, always read:

1. `docs/GOVERNANCE_STATUS.md` — current status and workstream.
2. `TASKS.md` — exact execution gate and active task.
3. The active Implementation Kit manifest referenced by `ACTIVE_KIT`, when implementation is active.
4. The exact PRD/TSD/ADR/threat-model sections referenced by the active task/kit.

Read `ENGINEERING_PRINCIPLES.md` and `docs/WORKFLOW.md` when starting governance/planning work, when their revision has changed since the last verified context, or when a conflict/ambiguity requires the full rule text. `docs/IMPLEMENTATION_KIT.md` is primarily the kit-preparation standard; Codex should consult it when the active manifest is incomplete/ambiguous or when kit handling itself is in question.

Reuse verified context when safe instead of repeating broad documentation scans. Re-check security-critical assumptions and any source-of-truth document that changed.

Verify branch, HEAD, and working-tree state before work.

## 2. Terminology

- **RAHSA** = the product being developed.
- **KeyPass** = upstream open-source reference/base or legacy technical identifiers that still contain `keypass`.

Do not rename legacy package/class paths casually. Do not call the current product KeyPass in new planning or product documentation.

## 3. Hard execution gate

Codex may modify application code only when `TASKS.md` exposes exactly one approved active `Txxx` task and one active Implementation Kit.

If `TASKS.md` says `EXECUTION_STATUS: PLANNING_FREEZE`, `ACTIVE_TASK: NONE`, or `ACTIVE_KIT: NONE`:

- do not implement features;
- do not opportunistically refactor code;
- do not execute parked release tasks;
- stop after reporting that no implementation task is active.

Documentation/planning changes are allowed only when the owner explicitly asks for planning/governance documentation work.

Run the repository preflight before implementation:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

If preflight fails, do not bypass it. Resolve the governance/task/kit state first.

## 4. One task at a time

For an active task:

- Work only on the exact Txxx scope.
- Use only the active kit referenced by `ACTIVE_KIT`.
- Do not start the following task in the same run.
- Do not change unrelated files.
- If the task requires a product/security/architecture decision not already approved, stop and return the issue to planning.

After a successful task checkpoint, reset the execution gate to `PLANNING_FREEZE / NONE / NONE` and stop. The next task requires a new just-in-time kit and explicit activation.

## 5. Implementation Kit handling

Every active implementation task requires a manifest following `docs/IMPLEMENTATION_KIT.md`.

Implementation Kits are non-authoritative execution aids. If a kit conflicts with `TASKS.md` or a higher-level source of truth, follow the higher-level document and stop if the conflict affects implementation semantics.

When a kit contains a patch or helper script:

- inspect affected current files first;
- run `git apply --check` before applying a patch when practical;
- never force a patch through conflicts;
- inspect the full resulting diff;
- do not allow helper scripts to bypass governance, tests, staging review, or checkpoint rules;
- treat stale-kit conditions in the manifest as blockers or refresh triggers.

A prepared patch may reduce exploration work. It does not transfer correctness/security responsibility away from Codex.

## 6. Reuse-first audit

Before writing a new implementation or adding a dependency, check in order:

1. retained RAHSA implementation;
2. upstream KeyPass implementation/pattern;
3. Android / AndroidX / Jetpack;
4. mature, maintained, license-compatible open source;
5. minimum local implementation.

Document a new dependency's purpose, source repository/project, license, why earlier options are insufficient, and relevant security/privacy impact.

Never reuse security-sensitive inherited code blindly.

The active kit may contain reuse findings to reduce repeated exploration, but Codex must re-check security-critical assumptions and any finding that is stale against current source.

## 7. Preserve the v0.2 baseline

`v0.2-prototype` is the stable baseline. Preserve working vault lifecycle, KDBX/Kotpass persistence, CRUD, search, generator, locking, clipboard protections, fail-closed behavior, and corrupt-vault protections unless the active approved requirement explicitly changes them.

Do not perform broad rewrites or architecture migrations as incidental work.

## 8. No custom cryptography

Do not invent crypto primitives, KDFs, key wrapping formats, encrypted containers, recovery formats, or authentication schemes. Use approved Android platform APIs and established libraries/formats.

## 9. Cost-aware model and reasoning policy

Choose the lowest-cost model and lowest reasoning effort reasonably likely to complete the task correctly and safely. State the chosen model, reasoning effort, and brief justification before an execution task.

Classify inherent task risk **before** considering kit completeness. A prepared patch can reduce exploration/coding cost; it cannot lower a security-sensitive task into a low-risk task.

### Preferred starting points

- `gpt-5.6-luna` + `low`: repository inspection, grep/search, status, smoke tests, formatting, deterministic low-risk kit application, mechanical edits, narrow low-risk tests.
- `gpt-5.6-luna` + `low` or `medium`: low-risk kit requiring limited adaptation.
- `gpt-5.6-terra` + `medium`: normal focused implementation/review with clear scope and moderate coding judgment.
- `gpt-5.6-sol`: reserve for security-sensitive vault/master-password/authentication/Keystore/recovery semantics, difficult lifecycle/concurrency races, architecture-critical work, or a failed cheaper attempt.

Do not use Sol automatically. Do not raise reasoning effort merely because a higher option exists. `xhigh` or `max` require an explicit reason.

The goal is lowest expected total cost while preserving correctness, security, and sufficient verification.

## 10. Execution cycle

For each active Txxx:

1. Verify repo/branch/status.
2. Read the exact task, source-of-truth references, and active kit manifest.
3. Run governance preflight.
4. Validate kit assumptions against current source and inspect targeted reuse/current implementation.
5. Apply the smallest sufficient change, using prepared patch/script only when valid.
6. Review full diff for unrelated changes.
7. Run targeted tests/build required by acceptance criteria.
8. Run smoke/device validation when required.
9. Only after validation, mark that one task complete.
10. Reset `TASKS.md` execution gate to `PLANNING_FREEZE / NONE / NONE`.
11. Stage only task-owned files and the task-completion gate change.
12. Review staged diff/check.
13. Commit one focused checkpoint.
14. Report checkpoint and stop.

Never mark a task complete because code merely compiles.

## 11. Public release work is parked

Do not perform Play Store/public-release preparation, trademark clearance, package/applicationId migration, release signing/store identity, listing work, or release compliance work until the owner explicitly resumes that workstream and `TASKS.md` activates it.
