# RAHSA Implementation Tasks

**EXECUTION_STATUS: PLANNING_FREEZE**  
**ACTIVE_TASK: NONE**  
**ACTIVE_KIT: NONE**  
**Stable baseline:** `v0.2-prototype` (`5d6acd3 release: prepare v0.2 prototype`)

## Current execution gate

There is currently **no active implementation task**.

Codex MUST NOT modify application code while `ACTIVE_TASK: NONE` or `ACTIVE_KIT: NONE`.

Phase 13 product requirements are owner-approved. The technical design, proposed ADRs, Threat Model extension, and the draft tasks below require owner review/approval before any task is activated.

## Completed historical work

Tasks T001–T125 for the original prototype, KDBX integration, CRUD, security hardening, UI/UX rollout, accessibility, and physical-device validation are complete in the `v0.2-prototype` baseline.

The full historical checklist remains available in Git history and at the `v0.2-prototype` tag. It is not the current execution backlog.

# Phase 13 — Access & Data Safety Foundation

**Phase status:** DRAFT TASK PLAN — NOT ACTIVE  
**Authoritative product scope:** `PRD.md` Phase 13 amendment  
**Technical review:** `TSD.md` Phase 13 draft, ADR 0005/0006, `docs/THREAT_MODEL.md`

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

**Objective:** Extend internal vault persistence with exactly one encrypted LKG role and a reusable narrow validate-before-promote path for future re-key/restore operations.

**References:** `PRD.md` P13-FR-050..055, P13-SR-004; `TSD.md` §11/§16; ADR 0005; Threat Model T14/T15/T18.

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

**Objective:** Add current-password reauthentication, advisory strength, optional hint edit, real Kotpass KDBX credential modification, candidate verification, and safe promotion.

**References:** `PRD.md` P13-FR-020..035; `TSD.md` §10; ADR 0005; Threat Model T14/T21.

**Acceptance:**
- Current password is verified against KDBX before re-key.
- New password non-empty and confirmation matches.
- Strength indicator is advisory only.
- Existing hint is prefilled; hint changes only after successful promotion.
- Uses Kotpass 0.13.0 credential-modification API; no custom KDBX crypto.
- Success leaves session unlocked; old password fails; new password opens.
- Any pre-promotion failure preserves old vault/password/hint.
- If biometric state exists, success invalidates it; otherwise no irrelevant message/state is created.
- Operation is single-flight.

**Validation:** repository/UI tests + old/new password reopen tests + failure injection + build/device smoke.

**Out of scope:** password history/expiry/complexity enforcement, recovery key.

**Dependency note:** zxcvbn4j remains the preferred strength-estimator candidate and must pass version/license/security verification in the JIT kit before being added.

## T130 — Add Forgot Master Password and destructive reset

**Objective:** Add honest forgotten-password guidance, hint display, and typed-`DELETE` destructive reset scoped to RAHSA-managed local vault state.

**References:** `PRD.md` P13-FR-030..045; `TSD.md` §12; Threat Model T23.

**Acceptance:**
- Forgot flow shows hint when present and says RAHSA cannot recover/decrypt without correct Master Password.
- Backup and biometric are not presented as recovery.
- Exact `DELETE` is required to enable reset.
- Reset deletes active, LKG, RAHSA temp/recovery files, hint, and biometric state/alias.
- UI preferences remain.
- External user-managed backups are untouched.
- Success returns to Create New Vault flow.
- Operation is single-flight.

**Validation:** unit/UI tests + filesystem/state assertions + physical smoke.

**Out of scope:** secure-wipe framework, account/server recovery, recovery key.

## T131 — Add manual external KDBX backup via SAF

**Objective:** Export the current encrypted active KDBX to a user-selected Android document destination with truthful subtle progress UX.

**References:** `PRD.md` P13-FR-060..066, P13-FR-080..092; `TSD.md` §13/§15/§16; ADR 0005; Threat Model T17/T18/T22.

**Acceptance:**
- Uses system document create flow/provider-neutral SAF.
- Writes encrypted `.kdbx` directly; no custom container/cloud SDK.
- Destination write failure does not mutate active/LKG.
- Success only after output completes.
- Status uses existing Material 3 progress/animation primitives; determinate only when measurable, otherwise indeterminate.
- No fake percentages; accessible operation status.
- Operation is single-flight.

**Validation:** unit/instrumentation tests + physical provider smoke (at least local provider; Drive/provider if available) + open resulting KDBX with correct password.

**Out of scope:** scheduled backup, cloud API/OAuth, backup retention manager.

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
