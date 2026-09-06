# RAHSA Documentation Guide

**Updated:** 2026-09-06

Start every new planning or Codex session with `docs/GOVERNANCE_STATUS.md`.

## Decision authority

1. `ENGINEERING_PRINCIPLES.md`
2. `PRD.md`
3. `TSD.md`
4. `docs/adr/*.md`
5. `docs/THREAT_MODEL.md`
6. `TASKS.md`
7. active Implementation Kit
8. implementation code

If documents conflict, resolve the higher-authority decision before implementation.

Execution/status documents (`AGENTS.md`, `docs/WORKFLOW.md`, `docs/GOVERNANCE_STATUS.md`) describe how and when work is performed; they do not override approved product/security decisions. Implementation Kits are non-authoritative execution aids.

## Current operational documents

### `docs/GOVERNANCE_STATUS.md`

First-read status page: RAHSA naming, stable baseline, current workstream, planning/preparation/execution gate, active/parked/historical documents, and model-cost policy.

### `ENGINEERING_PRINCIPLES.md`

Non-negotiable engineering governance: reuse-first, no custom crypto, baseline preservation, explicit OSS provenance, planning/preparation/implementation separation, and cost-effective AI development.

### `AGENTS.md`

Mandatory Codex repository instructions. Codex may modify application code only when `TASKS.md` exposes one approved active Txxx + active kit and preflight passes.

### `PRD.md`

Approved RAHSA v0.2 product baseline. Expansion features require explicit amendment before implementation.

### `TSD.md`

Approved RAHSA v0.2 technical baseline. New architecture/security designs require explicit amendment/ADR before implementation.

### `docs/THREAT_MODEL.md`

Security assets, threats, boundaries, and mitigations. Update when a planned feature changes trust boundaries, stored secrets, authentication, recovery, or external interaction.

### `TASKS.md`

Machine-readable execution gate plus approved implementation tasks. Roadmap/ideas do not authorize coding. Active implementation requires `EXECUTION_STATUS`, `ACTIVE_TASK`, and `ACTIVE_KIT` to agree.

### `docs/WORKFLOW.md`

Standard lifecycle: planning in ChatGPT → documentation/task approval → just-in-time Implementation Kit → one-task-at-a-time Codex execution → validation/checkpoint → planning freeze.

### `docs/IMPLEMENTATION_KIT.md`

Defines the required manifest and optional patch/PowerShell helper standard used to reduce repeated Codex exploration while keeping task/security authority and validation intact.

Use `docs/IMPLEMENTATION_KIT_TEMPLATE.md` when preparing `implementation-kits/Txxx/README.md`.

### `implementation-kits/`

Repository workspace for just-in-time task execution packages. Only the manifest is mandatory; patch/helper files are optional. Never store secrets, keystores, signing material, or real vault data here.

### `docs/ROADMAP.md`

Sequencing and planning candidates. Roadmap items do not authorize implementation.

### `docs/FEATURES.md`

Feature inventory using `[CURRENT]`, `[PLANNING]`, and `[PARKED]`.

## Historical / evidence documents

### `docs/UX_UI_BASELINE.md`

Source-of-truth for the UI/UX rollout completed by `v0.2-prototype`. It remains useful for preserving current UX behavior but is not the current expansion backlog.

### `docs/NEXT_BUILD_NOTES.md`

Archived/superseded UI rollout instructions. Do not use as current execution scope.

### `docs/TEST_PLAN.md` / `docs/TEST_RESULTS.md`

Baseline test plan/evidence and known limitations. New feature phases may extend these documents or add focused validation as approved.

### `docs/FEATURE_AUDIT.md`

Historical audit of inherited KeyPass features and prototype scope decisions. It is evidence/reuse context, not a permanent ban list.

### `docs/BRAND_DECISION.md`

Approved RAHSA identity decisions plus parked public-release items.

### `docs/adr/`

Architecture Decision Records. Accepted ADRs remain authoritative unless explicitly superseded.

## Verified Android baseline

Development baseline remains Windows + JDK 17 + Android SDK/ADB + repository Gradle Wrapper. A typical debug build is:

```bat
.\gradlew.bat :app:assembleFreeDebug
```

Build/test requirements for implementation are defined by the active Txxx acceptance criteria.

## Codex handoff pattern

Do not paste the whole governance or implementation plan into every prompt. Keep governance and the active Implementation Kit in the repository.

A handoff identifies only the repo/branch/checkpoint, exact active task, and active kit, then instructs Codex to obey repository governance.

Before implementation Codex runs:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

If `TASKS.md` has `ACTIVE_TASK: NONE` or `ACTIVE_KIT: NONE`, implementation must not start.
