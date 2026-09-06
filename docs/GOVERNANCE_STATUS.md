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

**Phase 13 — T126 Password Generator Security Hardening ACTIVE.**

Owner product review and the consolidated Phase 13 technical package are approved. All task-level architectures T126–T133 are owner-approved through accepted ADRs 0005–0012. A READY JIT Implementation Kit exists for T126 and the owner has explicitly activated **T126 only**.

The current execution gate in `TASKS.md` must be:

```text
EXECUTION_STATUS: ACTIVE_IMPLEMENTATION
ACTIVE_TASK: T126
ACTIVE_KIT: implementation-kits/T126/README.md
```

Codex may modify application code only within the approved T126 scope after `python scripts/governance_preflight.py --implementation T126` succeeds. T127–T133 remain NOT ACTIVE and must not be implemented, bundled, or opportunistically advanced during T126.

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
T126 Password Generator Security Hardening       ACTIVE
T127 Create Vault Recovery Acknowledgment        NOT ACTIVE
T128 One-LKG Safe Promotion Foundation           NOT ACTIVE
T129 Change Master Password / Real KDBX Re-key   NOT ACTIVE
T130 Forgot Master Password / Destructive Reset  NOT ACTIVE
T131 Manual External KDBX Backup via SAF         NOT ACTIVE
T132 Safe KDBX Restore via SAF                    NOT ACTIVE
T133 Secure Biometric Quick Unlock                NOT ACTIVE
```

T126 is the sole active implementation task. After T126 validation and focused checkpoint, execution returns to `PLANNING_FREEZE / NONE / NONE`; T127 requires a new JIT kit and separate explicit activation.

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
- `implementation-kits/T126/README.md` — READY JIT kit for the sole active task.
- `TASKS.md` — T126 active; T127–T133 inactive.
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

For active T126, use direct `java.security.SecureRandom` combined-alphabet selection and add no new dependency, UX, passphrase mode, or complexity policy.

## Public release status

Public/Play Store release work remains **PARKED** until explicitly resumed.

## Implementation Kit / Codex gate

Codex may modify application code only when `TASKS.md` exposes exactly one approved active Txxx and repository-local READY kit, and governance preflight passes. The current authorized command is:

```powershell
python scripts/governance_preflight.py --implementation T126
```

After T126 passes required validation and a focused checkpoint is created, return to:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

Codex must stop after that checkpoint and must not automatically continue to T127.

## Cost policy

Use the lowest-cost model/reasoning sufficient for the task, but classify inherent risk first. Prepared kits reduce exploration cost, not security classification. T126 is a narrow low-risk deterministic hardening task; its READY kit recommends Luna + low. Vault/Master Password/Keystore/recovery semantics in later tasks remain security-sensitive regardless of kit completeness.
