# RAHSA Implementation Tasks

**EXECUTION_STATUS: PLANNING_FREEZE**  
**ACTIVE_TASK: NONE**  
**ACTIVE_KIT: NONE**  
**Stable baseline:** `v0.2-prototype` (`5d6acd3 release: prepare v0.2 prototype`)

## Current execution gate

There is currently **no active implementation task**.

Codex MUST NOT modify application code while `ACTIVE_TASK: NONE` or `ACTIVE_KIT: NONE`.

Phase 13 product requirements are owner-approved. **ADR 0005 (one-LKG safe promotion + SAF backup/restore architecture), ADR 0007 (Change Master Password re-key, acknowledgment, and crash consistency), ADR 0008 (resumable destructive reset), and ADR 0009 (manual external KDBX backup via provider-neutral SAF) are owner-approved and accepted.** The remaining Phase 13 technical design, ADR 0006, Threat Model extension, and the draft tasks below still require owner review/approval before any task is activated.

## Completed historical work

Tasks T001–T125 for the original prototype, KDBX integration, CRUD, security hardening, UI/UX rollout, accessibility, and physical-device validation are complete in the `v0.2-prototype` baseline.

The full historical checklist remains available in Git history and at the `v0.2-prototype` tag. It is not the current execution backlog.

# Phase 13 — Access & Data Safety Foundation

**Phase status:** DRAFT TASK PLAN — NOT ACTIVE  
**Authoritative product scope:** `PRD.md` Phase 13 amendment  
**Technical review:** `TSD.md` Phase 13 draft; ADR 0005 accepted; ADR 0006 proposed; ADR 0007 accepted; ADR 0008 accepted; ADR 0009 accepted; `docs/THREAT_MODEL.md` under review

Only one approved task may later be activated at a time, with a JIT Implementation Kit prepared against the latest checkpoint.

## T126 — Harden Password Generator randomness

**Objective:** Replace non-cryptographic/default Kotlin randomness in secret generation with `java.security.SecureRandom` and select from the combined allowed-character alphabet without changing generator UX.

**References:** `PRD.md` P13-FR-001..004; `TSD.md` §8; Threat Model T1/T3/T11.

**Acceptance:**
- Production password generation uses CSPRNG-backed bounded selection.
- Existing length/category configuration and generator entry points remain working.
- No passphrase or new complexity policy is added.
- Unit tests cover allowed alphabet/length/category behavior.

**Validation:** targeted unit tests + `:app:assembleFreeDebug` smoke build.

**Out of scope:** generator redesign, password-strength UI here, passphrase.

**Reuse:** Java `SecureRandom`; existing generator/config code.

## T127 — Add Create Vault unrecoverability acknowledgment

**Objective:** Require an explicit recovery warning and swipe-to-acknowledge before Create Vault can be enabled.

**References:** `PRD.md` P13-FR-010..015; `TSD.md` §9; Threat Model T13.

**Acceptance:**
- Warning clearly states RAHSA cannot recover/decrypt vault if Master Password is forgotten.
- User must complete swipe acknowledgment before final Create Vault action is enabled.
- Swipe does not create vault automatically.
- Uses existing Material 3/platform swipe component/pattern; no custom gesture framework/dependency.
- TalkBack/accessibility can intentionally acknowledge via semantics/action.
- Duplicate Create Vault execution is prevented.

**Validation:** Compose/unit/instrumentation tests where practical + TalkBack/physical-device smoke.

**Out of scope:** recovery key, repeated warning on every unlock, vault-storage redesign.

## T128 — Establish one-LKG safe promotion foundation

**Planning status:** Architecture approved via accepted ADR 0005; implementation task remains NOT ACTIVE.

**Objective:** Extend internal vault persistence with exactly one encrypted LKG role and a reusable narrow validate-before-promote path for future re-key/restore operations.

**References:** `PRD.md` P13-FR-050..055, P13-SR-004; `TSD.md` §11/§16; accepted ADR 0005; Threat Model T14/T15/T18.

**Acceptance:**
- Exactly one internal LKG artifact is managed by RAHSA.
- Candidate is written/validated separately before active promotion.
- Unverified candidate never becomes LKG.
- Failure paths preserve a usable previous state.
- Active-corrupt + valid-LKG path can surface an explicit restore offer; no silent auto-restore.
- Existing credential mutations remain non-destructive.

**Validation:** repository unit tests with failure injection; corrupt-active/LKG cases; build.

**Out of scope:** external backup, version history, automatic restore, merge.

## T129 — Implement real Change Master Password

**Planning status:** Architecture approved via accepted ADR 0007; implementation task remains NOT ACTIVE.

**Objective:** Add current-password reauthentication, advisory strength, optional hint edit, mandatory consequence acknowledgment, real Kotpass KDBX credential modification, candidate verification, crash-consistent safe promotion, and post-commit cleanup.

**References:** `PRD.md` P13-FR-020..028; `TSD.md` §10; accepted ADR 0005; accepted ADR 0007; Threat Model T14/T21.

**Acceptance:**
- Current password is verified against the active KDBX before re-key.
- New password is non-empty and confirmation matches.
- Strength indicator is advisory only.
- Existing hint is prefilled; hint changes only after successful promotion/finalization.
- Before the final action is enabled, the UI clearly warns that the old Master Password will stop working and that RAHSA cannot recover/open the vault if the new Master Password is forgotten.
- User must complete a swipe-to-acknowledge using the same existing Material 3/platform pattern as Create Vault; the swipe only records acknowledgment and does not execute re-key.
- TalkBack/accessibility can perform an equivalent intentional acknowledgment action.
- Uses Kotpass 0.13.0 credential-modification API; no custom KDBX crypto.
- Before the final action starts, Back/navigation away, abandoned Home/background flow, process death, force-close, or recreation does not persist password drafts or acknowledgment; reopening starts password fields empty and acknowledgment false, with current persisted hint allowed to prefill again.
- Existing Auto-Lock/session behavior is preserved; this task does not redesign lock policy.
- After the operation starts, Back/Home/navigation is not treated as a transaction cancel.
- Candidate is verified with the new password before promotion and uses accepted one-LKG safe-promotion semantics.
- Verified candidate promotion is the commit point: before commit the old vault/password/hint are authoritative; after commit the new vault/password are authoritative.
- A minimal app-private non-secret recovery/finalization marker may be used to complete post-commit hint/biometric/temp cleanup after crash/process death; it must contain no Master Password or decrypted vault data.
- Post-commit finalization is idempotent and invalidates prior biometric quick-unlock state when present.
- Any pre-commit failure preserves old vault/password/hint and prior biometric state.
- Success leaves session unlocked when the process survives; old password fails and new password opens.
- If biometric state existed, success invalidates it and informs the user; otherwise no irrelevant biometric state/message is created.
- Operation is single-flight.

**Validation:** repository/UI tests + old/new password reopen tests + failure injection + Back/Home/recreation/process-death/force-close boundary tests + pre-commit/post-commit recovery tests + build/device smoke.

**Out of scope:** password history/expiry/complexity enforcement, recovery key, custom transaction framework, external-backup prerequisite.

**Dependency note:** zxcvbn4j remains the preferred strength-estimator candidate and must pass version/license/security verification in the JIT kit before being added.

## T130 — Add Forgot Master Password and destructive reset

**Planning status:** Architecture approved via accepted ADR 0008; implementation task remains NOT ACTIVE.

**Objective:** Add honest forgotten-password guidance, hint display, typed-`DELETE` destructive reset, and resumable crash-consistent cleanup scoped to RAHSA-managed local vault/security state.

**References:** `PRD.md` P13-FR-030..045; `TSD.md` §12; accepted ADR 0008; Threat Model T23.

**Acceptance:**
- Forgot flow shows hint when present and says RAHSA cannot recover/decrypt without correct Master Password.
- Backup and biometric are not presented as recovery.
- Exact `DELETE` is required to enable reset.
- Before the final Reset action starts, Back/navigation away, Home/background, process death, force-close, or recreation performs no deletion; typed confirmation is not required to persist.
- Once Reset starts, Back/Home/navigation is not a transaction cancel; the operation converges to the fully reset state.
- A minimal app-private non-secret `RESET_IN_PROGRESS` marker (or equivalent) is persisted before destructive cleanup and contains no Master Password, credential data, decrypted vault data, or other secret material.
- Reset cleanup is idempotent and deletes active KDBX, LKG, RAHSA-owned temp/candidate/recovery files, password hint, biometric wrapped state, and related Keystore alias when present.
- On startup with a pending reset marker, RAHSA completes cleanup before normal vault routing, then clears the marker and returns to Create New Vault.
- Missing/already-deleted reset artifacts are treated as already-cleaned where safe; reset does not attempt rollback or reconstruction.
- UI preferences remain.
- External user-managed backups are untouched.
- No secure-wipe framework or generic transaction/workflow framework is introduced.
- Operation is single-flight.

**Validation:** unit/UI tests + filesystem/state assertions + failure injection + Back/Home/recreation/process-death/force-close tests before and after reset start + idempotent restart cleanup tests + physical smoke.

**Out of scope:** secure-wipe framework, account/server recovery, recovery key, rollback of a started destructive reset, generic transaction framework.

## T131 — Add manual external KDBX backup via SAF

**Planning status:** Architecture approved via accepted ADR 0009; implementation task remains NOT ACTIVE.

**Objective:** Export the current verified encrypted active KDBX directly to a user-selected Android document destination through provider-neutral SAF with truthful subtle progress UX and no internal recovery marker.

**References:** `PRD.md` P13-FR-060..066, P13-FR-080..092; `TSD.md` §13/§15/§16; accepted ADR 0005; accepted ADR 0009; Threat Model T17/T18/T22.

**Acceptance:**
- Uses system document create flow/provider-neutral SAF.
- Copies the verified encrypted active `.kdbx` bytes directly through `ContentResolver`; it does not decrypt and reserialize credentials merely for backup.
- No custom backup container, direct cloud SDK, or OAuth integration is added.
- External destination is output only and never becomes the active vault.
- Destination write/finalization failure does not mutate active or LKG.
- Success is reported only after destination write and close/finalization complete without error.
- Status uses existing Material 3 progress/animation primitives; determinate only when real byte progress is measurable, otherwise indeterminate.
- No fake percentages; accessible operation status.
- No internal `BACKUP_IN_PROGRESS` transaction/recovery marker is introduced because backup does not mutate authoritative internal vault state.
- Process death/crash during write may leave an incomplete external destination artifact; internal active/LKG remain unchanged and the user can run backup again.
- Operation is single-flight.

**Validation:** unit/instrumentation tests + interrupted/failing destination tests + physical provider smoke (at least local provider; Drive/provider if available) + open resulting KDBX with correct password.

**Out of scope:** scheduled backup, cloud API/OAuth, backup retention manager, internal backup transaction/recovery marker, custom external cleanup framework.

## T132 — Add safe KDBX restore via SAF

**Objective:** Restore a selected external KDBX only after password-based decode/validation and explicit confirmation, preserving current active as LKG before verified replacement.

**References:** `PRD.md` P13-FR-070..084, P13-FR-090..092; `TSD.md` §14/§15/§16; ADR 0005; Threat Model T16/T18/T22.

**Acceptance:**
- Restore available from Settings and initial setup.
- External URI is copied to app-private candidate; external file is never active vault.
- Wrong password/invalid/unsupported/corrupt input does not mutate active/LKG.
- Confirmation occurs only after successful candidate validation.
- Current active becomes LKG before verified candidate promotion when an active vault exists.
- Restore is full replacement; restored password becomes effective.
- Prior biometric state is invalidated/disabled and user informed when applicable.
- Truthful subtle progress/status UX; no fake percentage.
- Operation is single-flight.

**Validation:** valid/wrong-password/corrupt/unsupported cases + failure injection + initial-setup path + physical SAF smoke + reopen restored vault.

**Out of scope:** merge/dedupe/conflicts/partial import.

## T133 — Implement secure Biometric Quick Unlock

**Objective:** Implement opt-in strong-biometric quick unlock using Android Keystore-protected wrapped unlock secret and real repository vault opening.

**References:** `PRD.md` P13-FR-100..114, P13-SR-001/006; `TSD.md` §17; ADR 0006; Threat Model T19/T20/T21.

**Acceptance:**
- OFF by default; explicit enable from Settings.
- Enable requires correct current Master Password plus successful qualifying biometric prompt.
- Uses Android Keystore non-exportable key + authenticated cipher and AndroidX `BiometricPrompt.CryptoObject`.
- Allowed authenticator is strong biometric; device credential is not quick-unlock fallback.
- Persisted state contains no plaintext Master Password.
- Biometric success decrypts wrapped secret in memory and calls normal repository open; no navigation-only unlock.
- Cancel does not loop; manual biometric retry and Master Password paths remain.
- Missing key/enrollment change/invalidation/unwrap failure disables quick unlock and falls back to Master Password.
- Disable confirmation removes wrapped state/key without locking current session.
- Password change/restore/reset hooks invalidate quick-unlock state.
- One-time discoverability prompt/card does not nag after `Not Now`.
- Operations are single-flight.

**Validation:** unit/instrumentation tests + physical qualifying-device biometric/Keystore tests, including cancellation and invalidation scenarios.

**Out of scope:** device-credential fallback, biometric recovery, cloud migration of biometric state.

## Parked future roadmap

Do not create Phase 13 tasks for:

- Android Autofill.
- Website/favicon retrieval.
- Camera/OCR credential scan.
- Public/Play Store release work.

## Closed / no-change decisions

Do not create current-roadmap tasks for:

- Passphrase Generator.
- Extra sort/filter modes, Favorites, Newest/Oldest.
- Tags/Categories.
- Typed Items.
- Session/Auto-Lock redesign.

## Just-in-time Implementation Kit gate

The Phase 13 tasks above are draft definitions only. After owner approval, ChatGPT prepares a JIT kit for **exactly the next task** against the latest verified checkpoint.

Every active task requires:

```text
implementation-kits/Txxx/README.md
```

Do not pre-generate all Phase 13 patches.

## Activating exactly one task

Only after owner approval and a READY JIT kit, change the header to:

```text
EXECUTION_STATUS: ACTIVE_IMPLEMENTATION
ACTIVE_TASK: Txxx
ACTIVE_KIT: implementation-kits/Txxx/README.md
```

## End of each task

After build/test/validation and focused checkpoint, reset to:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

Stop. The next task requires a new kit and explicit activation.
