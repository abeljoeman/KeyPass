# RAHSA Implementation Tasks

**EXECUTION_STATUS: PLANNING_FREEZE**  
**ACTIVE_TASK: NONE**  
**ACTIVE_KIT: NONE**  
**Stable baseline:** `v0.2-prototype` (`5d6acd3 release: prepare v0.2 prototype`)

## Current execution gate

There is currently **no active implementation task**.

Codex MUST NOT modify application code while `ACTIVE_TASK: NONE` or `ACTIVE_KIT: NONE`.

Phase 13 product requirements are owner-approved. **ADR 0005 (one-LKG safe promotion + SAF backup/restore architecture), ADR 0006 (fail-closed biometric quick unlock with Android Keystore), ADR 0007 (Change Master Password re-key, acknowledgment, and crash consistency), ADR 0008 (resumable destructive reset), ADR 0009 (manual external KDBX backup via provider-neutral SAF), ADR 0010 (safe KDBX restore validation/promotion/finalization), ADR 0011 (password generator SecureRandom + combined alphabet), and ADR 0012 (Create Vault ephemeral recovery acknowledgment) are owner-approved and accepted.** The remaining Phase 13 technical design, Threat Model extension, and the draft tasks below still require owner review/approval before any task is activated.

## Completed historical work

Tasks T001–T125 for the original prototype, KDBX integration, CRUD, security hardening, UI/UX rollout, accessibility, and physical-device validation are complete in the `v0.2-prototype` baseline.

The full historical checklist remains available in Git history and at the `v0.2-prototype` tag. It is not the current execution backlog.

# Phase 13 — Access & Data Safety Foundation

**Phase status:** DRAFT TASK PLAN — NOT ACTIVE  
**Authoritative product scope:** `PRD.md` Phase 13 amendment  
**Technical review:** `TSD.md` Phase 13 draft; ADR 0005 accepted; ADR 0006 accepted; ADR 0007 accepted; ADR 0008 accepted; ADR 0009 accepted; ADR 0010 accepted; ADR 0011 accepted; ADR 0012 accepted; `docs/THREAT_MODEL.md` under review

Only one approved task may later be activated at a time, with a JIT Implementation Kit prepared against the latest checkpoint.

## T126 — Harden Password Generator randomness

**Planning status:** Architecture approved via accepted ADR 0011; implementation task remains NOT ACTIVE.

**Objective:** Replace non-cryptographic/default Kotlin randomness in secret generation with direct `java.security.SecureRandom` bounded selection from one combined allowed-character alphabet, without changing generator UX or adding dependencies.

**References:** `PRD.md` P13-FR-001..004; `TSD.md` §8; accepted ADR 0011; Threat Model T1/T3/T11.

**Acceptance:**
- Production password generation uses `java.security.SecureRandom` for CSPRNG-backed bounded index selection.
- The allowed alphabet is built directly from the existing enabled character categories and each output character is selected from that combined alphabet.
- Category-first random selection is removed for generated secret characters.
- No new requirement guarantees at least one character from every enabled category; enabled categories define the allowed alphabet only.
- Existing requested length, category configuration, configuration persistence, and generator entry points remain working.
- No passphrase, new complexity policy, strength UI, dependency, or generic crypto/random framework is added.
- Unit tests cover requested length, allowed alphabet/category combinations, and existing generator behavior without relying on deterministic production output.

**Validation:** targeted unit tests + `:app:assembleFreeDebug` smoke build.

**Out of scope:** generator redesign, password-strength UI here, passphrase, mandatory category-presence policy, third-party randomness dependency.

**Reuse:** Java `SecureRandom`; existing generator/config code.

## T127 — Add Create Vault unrecoverability acknowledgment

**Planning status:** Architecture approved via accepted ADR 0012; implementation task remains NOT ACTIVE.

**Objective:** Require a clear unrecoverability warning and ephemeral swipe-to-acknowledge before an explicit final Create Vault action can be enabled, without adding a recovery marker or new gesture dependency.

**References:** `PRD.md` P13-FR-010..015; `TSD.md` §9; accepted ADR 0012; Threat Model T13.

**Acceptance:**
- Warning clearly states RAHSA cannot recover/decrypt the vault if the Master Password is forgotten.
- User must complete swipe acknowledgment before the final Create Vault action can be enabled.
- The final Create Vault action remains a separate explicit action and is enabled only when existing password validation passes and acknowledgment is true.
- Swipe completion records acknowledgment only; it never creates the vault automatically.
- Uses an existing Material 3/platform/project swipe primitive or established pattern; no custom gesture framework or new dependency is added for this requirement.
- TalkBack/accessibility exposes an equivalent intentional semantic action rather than requiring a physical swipe only.
- Acknowledgment is ephemeral and not persisted; reopening after Back/navigation away, abandoned Home/background flow, process death, force-close, or non-preserving recreation starts with acknowledgment false.
- Password drafts are not persisted merely to preserve this acknowledgment flow.
- Before the explicit Create Vault action starts, abandoning the flow performs no authoritative vault mutation.
- After Create Vault starts, duplicate execution is prevented and existing reviewed vault-creation/safe-write behavior is reused.
- No `CREATE_VAULT_IN_PROGRESS` or other new T127-specific recovery/transaction marker is introduced.

**Validation:** Compose/unit/instrumentation tests where practical + Back/navigation/recreation/process-death state tests + TalkBack/physical-device smoke + duplicate-action test.

**Out of scope:** recovery key, repeated warning on every unlock, vault-storage redesign, third-party swipe dependency, T127-specific recovery marker.

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

**Planning status:** Architecture approved via accepted ADR 0010; implementation task remains NOT ACTIVE.

**Objective:** Restore a selected external KDBX only after app-private copy, password-based decode/validation, and explicit confirmation, then use accepted one-LKG safe promotion plus minimal non-secret post-commit finalization.

**References:** `PRD.md` P13-FR-070..084, P13-FR-090..092; `TSD.md` §14/§15/§16; accepted ADR 0005; accepted ADR 0010; Threat Model T16/T18/T22.

**Acceptance:**
- Restore is available from Settings and initial setup.
- External URI is copied to an app-private candidate; the external file is transport/input only and never the active vault.
- The selected backup password is requested and the copied candidate is decoded/validated before replacement confirmation is shown.
- Wrong password, copy failure, invalid/unsupported/corrupt KDBX, decode failure, or validation failure leaves active/LKG unchanged.
- Confirmation occurs only after successful candidate validation.
- Before the final Restore action starts, Back/navigation away, Home/background, cancellation, process death, force-close, or recreation performs no authoritative vault mutation; candidate/password UI state need not persist.
- After the final Restore action starts, Back/Home/navigation is not treated as a transaction cancel.
- When an active vault exists, the current verified active becomes the single LKG before the already-validated candidate is promoted using accepted ADR 0005 semantics.
- During initial setup there is no old active vault to preserve as LKG; the validated candidate is promoted with the same verification discipline.
- Candidate promotion is the commit point: before commit the old active vault is authoritative; after commit the restored active vault and restored Master Password are authoritative.
- RAHSA does not roll back the restored vault merely because process death/crash or UI interruption occurs after commit.
- A minimal app-private non-secret restore-finalization marker may be used after commit; it contains no Master Password, external URI, credential data, decrypted vault data, or other secrets.
- Post-commit finalization is idempotent and, when applicable, invalidates prior biometric quick-unlock/Keystore state, cleans candidate/temp artifacts, reconciles session/routing, and clears the marker.
- On startup with pending restore finalization, the restored KDBX remains authoritative while required non-secret cleanup completes before normal use.
- Restore is full replacement; the restored password becomes effective; no merge/dedupe/conflict resolution/partial import is added.
- If prior biometric state existed, successful restore invalidates/disables it and informs the user that it must be manually enabled again.
- Truthful subtle progress/status UX uses existing Material 3 primitives; determinate only for measurable copy progress and indeterminate for validation/decrypt/promotion stages without accurate percentage.
- Operation is single-flight.

**Validation:** valid/wrong-password/corrupt/unsupported/copy-failure cases + failure injection + Settings and initial-setup paths + Back/Home/recreation/process-death/force-close tests across pre-commit/post-commit boundaries + idempotent finalization tests + physical SAF smoke + reopen restored vault.

**Out of scope:** merge/dedupe/conflicts/partial import, using external KDBX as live vault, generic transaction/workflow framework, persisting backup password/URI in recovery state.

## T133 — Implement secure Biometric Quick Unlock

**Planning status:** Architecture approved via accepted ADR 0006; implementation task remains NOT ACTIVE.

**Objective:** Implement opt-in strong-biometric quick unlock using Android Keystore-protected wrapped unlock secret, real repository vault opening, and fail-closed enable/disable lifecycle without a biometric-specific recovery marker.

**References:** `PRD.md` P13-FR-100..114, P13-SR-001/006; `TSD.md` §17; accepted ADR 0006; Threat Model T19/T20/T21.

**Acceptance:**
- OFF by default; explicit enable from Settings.
- Enable requires correct current Master Password verified against active KDBX plus successful qualifying biometric prompt.
- Uses Android Keystore non-exportable key + authenticated cipher and AndroidX `BiometricPrompt.CryptoObject`.
- Allowed authenticator is strong biometric; device credential is not quick-unlock fallback.
- Persisted state contains no plaintext Master Password; wrapped ciphertext plus non-secret cipher/state metadata only.
- Enable is fail-closed: `enabled = true` is committed only after key creation, successful biometric authentication, wrapping, complete secure-state persistence, and consistency verification all succeed.
- Interrupted/failed enablement remains OFF; orphan aliases/incomplete state are cleanup-only and removable idempotently.
- No biometric-specific transaction/recovery marker is introduced.
- Biometric success decrypts the wrapped secret only transiently in memory and calls the normal repository open path; no navigation-only unlock.
- Vault transitions to unlocked UI only after real KDBX open succeeds.
- Auto-prompt occurs at most once per locked entry when enabled/valid; cancellation does not loop; manual `Unlock with Biometrics` and `Use Master Password` remain available.
- Back/Home/process death while prompt/enablement is active does not produce an unlocked session or enabled state.
- Missing key, enrollment/security-state change, key invalidation, unwrap failure, inconsistent secure state, or real vault-open failure keeps the vault locked, disables/removes unusable quick-unlock state where applicable, and falls back to Master Password.
- Disable requires confirmation but does not lock the current unlocked session; disable lifecycle fails closed toward OFF and cleanup of wrapped state/Keystore alias is idempotent.
- A crash/process death during disable must not safely resurrect a partially removed configuration.
- Password change/restore/reset hooks invalidate quick-unlock state; reinstall/new device does not migrate it.
- One-time discoverability card does not nag after `Not Now`.
- Enable/disable operations are single-flight.

**Validation:** unit/instrumentation tests + partial/interrupted enable/disable state tests + Back/Home/process-death/cancellation cases + invalidation/fallback cases + physical qualifying-device biometric/Keystore tests.

**Out of scope:** device-credential fallback, biometric recovery, cloud migration of biometric state, biometric-specific recovery marker, custom transaction framework.

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
