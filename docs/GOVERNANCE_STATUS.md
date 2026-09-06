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
- Baseline tag remains immutable for planning purposes.

## Current workstream

**Phase 13 — Access & Data Safety Foundation technical planning approved; JIT T126 preparation pending.**

Owner product review is complete and the Phase 13 PRD amendment is approved. All task-level architectures T126–T133 are owner-approved through accepted ADRs 0005–0012. The consolidated `TSD.md`, `docs/THREAT_MODEL.md`, accepted ADRs, and task architecture package are now **owner-approved as the Phase 13 technical package**.

There is **no active implementation task**. `TASKS.md` must remain:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

No application code may be modified under this gate. The next permitted planning/preparation step is a JIT Implementation Kit for **T126 only** against the latest checkpoint; T126 remains inactive until that kit is READY and `TASKS.md` explicitly activates exactly T126.

## Approved Phase 13 product direction

Phase 13 includes:

1. Password Generator CSPRNG hardening with direct combined-alphabet `SecureRandom` selection and no dependency/UX expansion.
2. Mandatory Create Vault unrecoverability warning with ephemeral swipe acknowledgment, explicit Create action, and accessibility equivalent.
3. Exactly one internal encrypted LKG plus validate-before-promote storage behavior.
4. Real KDBX Master Password re-key with current-password reauthentication, advisory strength, mandatory consequence acknowledgment, safe promotion, explicit commit boundary, and crash-consistent non-secret finalization.
5. Forgot Master Password guidance plus typed-`DELETE` destructive reset using a resumable non-secret reset marker.
6. Manual provider-neutral external KDBX backup via Android SAF as direct encrypted-KDBX copy, with no internal backup recovery marker.
7. Safe full-replacement restore via SAF with candidate validation before confirmation, LKG preservation, explicit commit point, and non-secret post-commit restore finalization.
8. Truthful subtle Material 3 progress/status UX for backup/restore.
9. Secure opt-in Biometric Quick Unlock using Android Keystore/BiometricPrompt, real vault-open transition, fail-closed enable/disable lifecycle, and no biometric-specific recovery marker.
10. Single-execution protection for critical operations.

Existing Session/Auto-Lock behavior is closed/no-change.

## Accepted Phase 13 ADRs

- `0005` — one-LKG safe promotion + provider-neutral SAF backup/restore architecture.
- `0006` — fail-closed Biometric Quick Unlock with Android Keystore/BiometricPrompt.
- `0007` — real Master Password re-key, consequence acknowledgment, and crash consistency.
- `0008` — resumable destructive reset.
- `0009` — manual direct encrypted-KDBX backup via SAF without internal recovery marker.
- `0010` — safe KDBX restore validation/promotion/post-commit finalization.
- `0011` — password generator `SecureRandom` combined-alphabet selection.
- `0012` — Create Vault ephemeral unrecoverability acknowledgment before explicit creation.

All are **Accepted — owner-approved Phase 13 architecture**.

## Parked / closed decisions

### Parked future roadmap

- Android Autofill.
- Website/favicon retrieval.
- Camera/OCR credential scanning.
- Public/Play Store release.

### Closed/no change current roadmap

- Passphrase Generator.
- Extra Sort/Filter/Favorites/Newest/Oldest.
- Tags/Categories.
- Typed Items.
- Session/Auto-Lock redesign.

## Phase 13 task sequence

```text
T126 Password Generator Security Hardening
T127 Create Vault Recovery Acknowledgment
T128 One-LKG Safe Promotion Foundation
T129 Change Master Password / Real KDBX Re-key
T130 Forgot Master Password / Destructive Reset
T131 Manual External KDBX Backup via SAF
T132 Safe KDBX Restore via SAF
T133 Secure Biometric Quick Unlock
```

The consolidated Phase 13 technical package and every task architecture are owner-approved, but all implementation tasks remain **NOT ACTIVE**. JIT preparation for T126 may now proceed; activation is a separate explicit gate after a repository-local READY kit exists.

## Standard planning-to-execution workflow

```text
Discussion / research in ChatGPT
→ PRD
→ TSD / ADR / Threat Model as needed
→ phase TASKS drafted
→ owner approval
→ ChatGPT prepares JIT Implementation Kit for next Txxx
→ TASKS activates exactly one Txxx + ACTIVE_KIT
→ Codex preflight
→ Codex executes/reviews/builds/tests exactly one task
→ focused checkpoint
→ TASKS returns to PLANNING_FREEZE / NONE / NONE
→ return to ChatGPT/owner before next task
```

Implementation Kit rules are defined in `docs/IMPLEMENTATION_KIT.md`.

## Governance authority

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
Implementation Kit
        ↓
       Code
```

Execution/status documents and kits never override higher-level product/security decisions.

## Active documents

- `ENGINEERING_PRINCIPLES.md`
- `AGENTS.md`
- `PRD.md` — v0.2 baseline + approved Phase 13 product amendment.
- `TSD.md` — owner-approved consolidated Phase 13 technical design.
- `docs/adr/0005-data-safety-kdbx-lkg-saf.md` — accepted.
- `docs/adr/0006-biometric-quick-unlock-keystore.md` — accepted.
- `docs/adr/0007-master-password-change-crash-consistency.md` — accepted.
- `docs/adr/0008-resumable-destructive-reset.md` — accepted.
- `docs/adr/0009-manual-kdbx-backup-via-saf.md` — accepted.
- `docs/adr/0010-safe-kdbx-restore-finalization.md` — accepted.
- `docs/adr/0011-password-generator-secure-random.md` — accepted.
- `docs/adr/0012-create-vault-recovery-acknowledgment.md` — accepted.
- `docs/THREAT_MODEL.md` — owner-approved consolidated Phase 13 threat extension; implementation validation gates remain unchecked until execution/testing.
- `docs/WORKFLOW.md`
- `docs/IMPLEMENTATION_KIT.md`
- `TASKS.md` — T126–T133 approved definitions; execution frozen.
- `docs/ROADMAP.md`
- `docs/FEATURES.md`

Historical material remains useful evidence/context but is not current execution scope when superseded.

## Reuse-first rule

For new capability design and implementation, evaluate in order:

1. Retained RAHSA implementation.
2. Upstream KeyPass implementation/pattern.
3. Android/AndroidX/Jetpack.
4. Mature, maintained, license-compatible OSS.
5. Minimum local implementation.

Phase 13 verified reuse findings include Kotpass `0.13.0` `modifyCredentials()` for KDBX credential changes, Android SAF for provider-neutral document I/O, AndroidX Biometric + Android Keystore for quick unlock, Material 3 for swipe/progress/animation UX, and `java.security.SecureRandom` for generator hardening.

Security-critical assumptions and optional new dependencies must be rechecked during the relevant JIT kit/task.

## Public release status

Public/Play Store release work remains **PARKED** until explicitly resumed.

## Implementation Kit / Codex gate

Codex may modify application code only when `TASKS.md` exposes exactly one approved active Txxx and repository-local READY kit, and governance preflight passes.

After every successful task checkpoint, return to:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

Codex does not automatically continue to the next task.

## Cost policy

Use the lowest-cost model/reasoning sufficient for the task, but classify inherent risk first. Prepared kits reduce exploration cost, not security classification. Vault/Master Password/Keystore/recovery semantics remain security-sensitive work even with a prepared patch.
