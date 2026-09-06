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
7. Implementation code

If documents conflict, resolve the higher-authority decision before implementation.

Execution/status documents (`AGENTS.md`, `docs/WORKFLOW.md`, `docs/GOVERNANCE_STATUS.md`) describe how and when work is performed; they do not override approved product/security decisions.

## Current operational documents

### `docs/GOVERNANCE_STATUS.md`

First-read status page: RAHSA naming, stable baseline, current workstream, active/parked/historical documents, implementation gate, and model-cost policy.

### `ENGINEERING_PRINCIPLES.md`

Non-negotiable engineering governance: reuse-first, no custom crypto, baseline preservation, explicit OSS provenance, planning/implementation separation, and cost-effective AI development.

### `AGENTS.md`

Mandatory Codex repository instructions. Codex may modify application code only when `TASKS.md` exposes one approved active Txxx and preflight passes.

### `PRD.md`

Approved RAHSA v0.2 product baseline. Expansion features require explicit amendment before implementation.

### `TSD.md`

Approved RAHSA v0.2 technical baseline. New architecture/security designs require explicit amendment/ADR before implementation.

### `docs/THREAT_MODEL.md`

Security assets, threats, boundaries, and mitigations. Update when a planned feature changes trust boundaries, stored secrets, authentication, recovery, or external interaction.

### `TASKS.md`

Machine-readable execution gate plus approved implementation tasks. Roadmap/ideas do not authorize coding.

### `docs/WORKFLOW.md`

Planning in ChatGPT → documentation approval → one-task-at-a-time Codex execution.

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

Do not paste the whole governance into every prompt. Keep governance in the repository.

A handoff should identify only the repo/branch/checkpoint and exact active task, then instruct Codex to obey repository governance.

Before implementation Codex runs:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

If `TASKS.md` has `ACTIVE_TASK: NONE`, implementation must not start.
