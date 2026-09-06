# RAHSA Governance Status

**Updated:** 2026-09-06  
**Status:** Current operational source of truth

Read this file first when starting a new planning or Codex session.

## Product identity

- Product/application name: **RAHSA**.
- Upstream open-source reference/base: **KeyPass** (`yogeshpaliyal/KeyPass`).
- Legacy package/class/path identifiers containing `keypass` remain technical debt and MUST NOT be renamed casually.

## Stable baseline

- Stable product baseline: `v0.2-prototype`.
- Baseline commit: `5d6acd3115d4553bf72f5b5d925196ca909db839` (`release: prepare v0.2 prototype`).
- The baseline tag is immutable for planning purposes; new work happens after/from that checkpoint.

## Current workstream

**Product & Security Expansion Planning.**

There is no active implementation task. `TASKS.md` must currently show:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
```

Current approved roadmap direction prioritizes planning in this order:

1. Access, master-password, and recovery policy.
2. Data safety, backup, and restore policy.
3. Session and lock policy.
4. Biometric quick-unlock design.
5. Backup/restore implementation planning after policy approval; it may be implemented before biometric if final risk/priority review supports that.
6. Android Autofill.
7. Lower-risk productivity expansion.

The Data Safety direction includes evaluating internal last-known-good/versioned recovery copies plus manual external encrypted KDBX backup/restore through Android user-selected document/storage providers. Provider-neutral Android integration is preferred over a direct Google/cloud API by default. These are roadmap/planning decisions, not approved implementation requirements.

Phase 13 implementation has not yet been authored. No planning candidate becomes executable until PRD/TSD/ADR/Threat Model changes are approved and `TASKS.md` activates exactly one Txxx.

## Public release status

Public/Play Store release work is **PARKED** until the owner explicitly resumes it.

Trademark clearance, package/applicationId migration, signing/store identity, listing/compliance work, and final release attribution are not current tasks.

## Governance authority

Product/security decision authority:

```text
ENGINEERING_PRINCIPLES.md
        ↓
      PRD.md
        ↓
      TSD.md
        ↓
      ADRs
        ↓
docs/THREAT_MODEL.md
        ↓
     TASKS.md
        ↓
       Code
```

Execution/status documents:

- `AGENTS.md` — mandatory Codex repository instructions.
- `docs/WORKFLOW.md` — planning-to-execution procedure.
- `docs/GOVERNANCE_STATUS.md` — current operational status.

Execution/status documents do not override approved product/security decisions.

## Active vs historical documents

### Active

- `ENGINEERING_PRINCIPLES.md`
- `AGENTS.md`
- `PRD.md` — v0.2 approved baseline; expand only by explicit amendment.
- `TSD.md` — v0.2 approved technical baseline; expand only by explicit amendment.
- `docs/THREAT_MODEL.md`
- `docs/WORKFLOW.md`
- `TASKS.md`
- `docs/ROADMAP.md`
- `docs/FEATURES.md`

### Historical / superseded as execution instructions

- Prototype-era completed T001–T125 checklist at tag `v0.2-prototype`.
- `docs/NEXT_BUILD_NOTES.md` UI/UX rollout instructions; rollout is complete.
- Old prototype non-goal lists when interpreted as permanent bans.

Historical material remains useful evidence/context but MUST NOT be treated as current execution scope when superseded by current governance.

### Parked

- Remaining brand/public-release gate activities in `docs/BRAND_DECISION.md`.

## Reuse-first rule

For new capability design and implementation, evaluate in order:

1. Retained RAHSA implementation.
2. Upstream KeyPass implementation/pattern.
3. Android/AndroidX/Jetpack.
4. Mature, maintained, license-compatible OSS.
5. Minimum local implementation.

Security-sensitive reuse requires review. New dependencies require purpose/source/license/justification/security-impact documentation.

## Planning and Codex gate

Planning occurs before implementation:

```text
Discussion/research in ChatGPT
→ PRD
→ TSD / ADR / Threat Model as needed
→ TASKS.md
→ owner approval
→ Codex executes one active task
```

Codex may modify application code only when `TASKS.md` exposes exactly one approved active Txxx and repository preflight passes.

If no task is active, Codex must stop without application-code changes.

## Cost policy

Use the lowest-cost model/reasoning that is reasonably sufficient:

- Luna + low for inspection/mechanical/smoke work.
- Terra + medium for normal focused implementation.
- Sol only when security-sensitive semantics, architecture-critical complexity, difficult concurrency/lifecycle problems, or failed cheaper attempts justify it.

Do not default to the highest model or reasoning level.
