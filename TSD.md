# Technical Design — RAHSA v0.2 Baseline + Phase 13 Consolidated Review

**Status:** v0.2 baseline approved; Phase 13 technical design **CONSOLIDATED — final owner approval required**  
**Updated:** 2026-09-06  
**Related:** `PRD.md`, `ENGINEERING_PRINCIPLES.md`, `docs/THREAT_MODEL.md`, accepted ADRs `0005`–`0012`

This TSD preserves the technical baseline at `v0.2-prototype` and consolidates the owner-approved task architectures for Phase 13. It does **not** activate implementation. All Phase 13 task architectures T126–T133 are owner-approved through accepted ADRs; this consolidated document and the aligned Threat Model still require final owner approval before a JIT Implementation Kit may be prepared.

## 1. Technical context

- Platform: Android.
- Language: Kotlin / Java 17 compatibility.
- UI: Jetpack Compose + Material 3.
- Vault format/engine: KDBX via Kotpass `0.13.0`.
- Base/reference application: upstream KeyPass.
- Persisted active storage: app-private `vault.kdbx`.
- Backend/network requirement for core flow: none.
- Implementation executor after planning approval: Codex.

## 2. Reuse-first technical strategy

For a new capability, evaluate:

1. retained RAHSA implementation;
2. upstream KeyPass implementation/pattern;
3. Android/AndroidX/Jetpack APIs;
4. mature maintained license-compatible OSS;
5. minimum local implementation.

Security-sensitive reuse is reviewed before adoption. Do not add a third-party dependency without purpose/source/license/justification/security-impact documentation.

## 3. Baseline architecture

```text
Jetpack Compose UI
        |
        v
ViewModel / UI State
        |
        v
VaultRepository
        |
        v
Kotpass
        |
        v
vault.kdbx
```

`VaultRepository` isolates application/UI behavior from KDBX-specific operations and remains the approved vault boundary.

## 4. Baseline VaultRepository responsibilities

- Create vault.
- Open/unlock vault using runtime-supplied credentials.
- Lock/clear repository access to decrypted state.
- List credentials.
- Create/update/delete credentials.
- Search credentials.
- Persist safe KDBX mutations.

The repository MUST NOT implement cryptographic primitives itself.

## 5. Baseline storage/session invariants

- KDBX remains the persisted source of truth.
- Do not introduce a second plaintext credential database.
- Master Password is not persisted as plaintext.
- Decrypted vault/application data is accessible only during an unlocked session.
- Manual/background lock clears normal application access to decrypted state.
- Failed open/decode is fail-closed and non-destructive.
- Failed writes preserve the last known valid vault according to baseline behavior.

## 6. Existing product layers to preserve

Unless an approved requirement explicitly changes them:

- retained Compose/navigation/state structure;
- credential CRUD/search flows;
- secure clipboard path;
- secure-screen behavior;
- current lock lifecycle and Auto-Lock behavior;
- current error/write-failure protections.

Avoid whole-app rewrites, new state/navigation frameworks, or broad architecture migration.

## 7. Phase 13 architecture overview

```text
Compose flows
  ├─ Create Vault warning + acknowledgment
  ├─ Change/Forgot/Reset Master Password
  ├─ Backup / Restore
  └─ Biometric Quick Unlock
          |
          v
ViewModels / local operation state
          |
          v
VaultRepository (+ narrow Phase 13 APIs)
          |
          +--------------------+
          |                    |
          v                    v
Kotpass / KDBX           Android platform
          |              ├─ SAF document URIs
          |              ├─ BiometricPrompt
          |              └─ Android Keystore
          v
app-private storage
  ├─ vault.kdbx          (active)
  ├─ vault.lkg.kdbx      (exactly one encrypted LKG; exact filename implementation-defined)
  ├─ short-lived candidate/temp files
  └─ narrow non-secret operation markers only where explicitly approved
```

Approved non-secret markers are limited to operations that require deterministic completion after an authoritative storage transition: Change Master Password post-commit finalization, destructive reset in progress, and restore post-commit finalization. Create Vault, manual backup, and biometric enable/disable do not introduce dedicated recovery markers.

## 8. Password Generator hardening — T126 / ADR 0011

### 8.1 Random source

Replace Kotlin default `.random()` calls used to generate secrets with direct `java.security.SecureRandom` bounded index selection. No third-party randomness dependency is introduced.

### 8.2 Character selection

Build one allowed alphabet from the currently enabled character categories and select every output character directly from that combined alphabet.

```text
enabled categories
→ combined allowed alphabet
→ SecureRandom.nextInt(alphabet.size)
→ output character
```

Do not perform category-first random selection. Enabled categories define the allowed alphabet only; Phase 13 does not add a requirement that every enabled category appear at least once in each generated password.

### 8.3 Scope/testability

Preserve existing length/category configuration, persistence, and generator entry points. Do not add passphrase mode, strength UI, complexity enforcement, a generic crypto/random abstraction, or a dependency solely for test determinism. Tests verify output length and membership in the configured allowed alphabet without depending on deterministic production output.

## 9. Create-vault recovery acknowledgment — T127 / ADR 0012

### 9.1 UI state

Create-vault state gains `recoveryWarningAcknowledged` (or equivalent), initialized `false` on entry. The final Create Vault action is enabled only when normal password validation passes and acknowledgment is true.

The warning states that RAHSA cannot open or recover the vault if the Master Password is forgotten.

### 9.2 Swipe interaction

Reuse an existing Material 3/platform swipe primitive compatible with the current dependency. Do not build low-level pointer/drag infrastructure or add a new gesture dependency.

Swipe completion changes acknowledgment state only. It never creates the vault. Vault creation requires a separate explicit Create Vault action.

### 9.3 Accessibility and lifecycle

Expose appropriate state/content semantics and an accessibility action that performs the same intentional acknowledgment transition for TalkBack users.

Acknowledgment and sensitive password drafts are ephemeral. Back/navigation away, abandonment, recreation, process death, or force-close before the final Create action does not persist acknowledgment; re-entering the flow starts with acknowledgment false. T127 does not add a recovery marker. After explicit Create starts, single-flight protection prevents duplicate execution and the existing safe vault-creation path remains authoritative.

## 10. Real KDBX Master Password re-key — T129 / ADR 0007

### 10.1 Existing Kotpass capability

Kotpass `0.13.0` exposes `KeePassDatabase.modifyCredentials { ... }`. Use this established API rather than implementing KDBX cryptography locally. New passphrase credentials use the existing Kotpass credential primitives already used by vault creation/opening.

### 10.2 User flow and acknowledgment

```text
Vault unlocked
→ current Master Password
→ new Master Password + confirmation
→ optional prefilled/editable hint
→ advisory strength
→ unrecoverability/consequence warning
→ swipe acknowledgment
→ explicit Change Master Password
```

The current password is verified against the active KDBX even when the session is already unlocked or was opened via biometric. The warning states that the old password will stop working and RAHSA cannot recover/open the vault if the new password is forgotten. The swipe uses the same approved acknowledgment pattern as Create Vault and never directly triggers re-key.

### 10.3 Repository operation and safe promotion

The narrow repository operation must:

1. verify the supplied current Master Password against active KDBX;
2. derive a re-keyed candidate with Kotpass credential modification;
3. encode candidate to an app-private temporary path;
4. decode/open candidate using the new password;
5. preserve the valid old active as LKG per ADR 0005;
6. promote the verified candidate;
7. treat verified candidate promotion as the authoritative **commit point**;
8. update in-memory/session state when the process remains alive;
9. finalize hint and biometric invalidation after commit; and
10. clean temporary/finalization state idempotently.

Before commit, the old active/password/hint and prior biometric state are authoritative. After commit, the new active/password are authoritative and must not be rolled back merely because UI completion was interrupted.

### 10.4 Lifecycle/crash consistency

Before the explicit final action starts, password drafts and acknowledgment are not persisted. Back/navigation away, abandoned background flow, recreation, process death, or force-close performs no vault mutation. Existing Auto-Lock behavior remains unchanged.

Once the operation starts, Back/Home/navigation is not treated as transaction cancellation. A minimal app-private **non-secret post-commit finalization marker** may be used so a restart can finish hint application, stale biometric invalidation, and temp cleanup. The marker must contain no old/new Master Password or decrypted vault data. Finalization must be idempotent.

### 10.5 Password strength

Strength is advisory only. `zxcvbn4j` remains the preferred candidate but requires JIT task-level version/license/security verification before dependency addition. Master Password text never leaves the device.

## 11. Last-Known-Good and safe promotion — T128 / ADR 0005

### 11.1 Roles

- `active`: current internal KDBX used by RAHSA.
- `lkg`: exactly one previous verified encrypted KDBX.
- `candidate`: short-lived replacement being validated.

### 11.2 Promotion invariant

```text
valid active
→ write candidate separately
→ decode/open candidate with expected credentials
→ preserve old active as LKG
→ promote candidate to active
→ verify promoted active
→ reconcile in-memory/session state
→ clean temporary artifacts
```

An unverified candidate never becomes active or LKG. LKG is sourced only from a previously valid active vault. Failure handling favors preserving a usable active/LKG state over cleanup perfection.

### 11.3 Corrupt active recovery

When active KDBX cannot open due to corruption and an LKG exists, validate LKG separately. If valid, present an explicit restore offer and warn that latest changes may be lost. Never silently auto-restore.

## 12. Forgot Master Password and destructive reset — T130 / ADR 0008

Forgot flow reads only the stored hint and product explanation; it does not attempt password-recovery cryptography and does not present backup or biometric as recovery.

Reset requires exact typed `DELETE`. Before the final Reset action starts, Back/navigation away, backgrounding, recreation, process death, or force-close performs no deletion and typed confirmation need not persist.

Once Reset starts, it converges to a fully reset RAHSA-managed state and is not cancellable through Back/Home navigation. Persist a minimal app-private **non-secret `RESET_IN_PROGRESS` marker** (or equivalent) before destructive cleanup. It contains no Master Password, credential data, decrypted vault data, or other secret material.

Idempotent cleanup removes:

- active KDBX;
- LKG KDBX;
- RAHSA-owned candidate/temp/recovery artifacts;
- password hint;
- biometric wrapped state; and
- related Keystore alias when present.

On startup with a pending reset marker, complete cleanup before normal vault routing, clear the marker, then route to Create New Vault. Missing/already-deleted reset artifacts are treated as already-cleaned where safe. Do not attempt rollback or reconstruction. Preserve unrelated UI preferences and never delete user-managed external SAF backups. No secure-wipe or generic transaction framework is introduced.

## 13. Manual backup via Storage Access Framework — T131 / ADR 0009

### 13.1 Destination selection

Use Android's system document creation flow (`ACTION_CREATE_DOCUMENT` or Activity Result equivalent). Write to the returned URI through `ContentResolver`. No direct cloud SDK/OAuth integration is introduced.

### 13.2 Backup source

Copy the current verified encrypted active KDBX bytes directly. Do not decrypt and reserialize credentials merely to produce backup. The external destination is output/transport only and never becomes the live active vault.

### 13.3 Failure/lifecycle behavior

Destination open/write/close/finalization failure surfaces an error and leaves active/LKG unchanged. Success is reported only after output completion/finalization succeeds.

T131 does **not** use an internal backup recovery marker because it does not mutate authoritative internal vault state. Process death/crash may leave an incomplete external document; internal state remains unchanged and the user can run backup again. The operation is single-flight.

## 14. Safe restore via Storage Access Framework — T132 / ADR 0010

### 14.1 Candidate acquisition and validation

Use Android's system document open flow (`ACTION_OPEN_DOCUMENT` or Activity Result equivalent). Treat the external URI as untrusted input only. Copy it to an app-private candidate, ask for the selected backup password, and decode/open the copied candidate with Kotpass.

Wrong password, copy failure, unsupported/corrupt KDBX, decode failure, or validation failure stops with no active/LKG mutation.

### 14.2 Validate-before-confirm

Only after successful candidate validation show the replacement confirmation. Before explicit Restore starts, Back/navigation away, cancellation, background abandonment, recreation, process death, or force-close performs no authoritative vault mutation and candidate/password UI state need not persist.

### 14.3 Promotion and commit point

When a current active exists:

```text
validated candidate
→ current verified active → LKG
→ candidate → ACTIVE
→ verify promoted ACTIVE
```

During initial setup there is no old active to preserve as LKG; use the same validation discipline before promotion.

Candidate promotion is the authoritative **commit point**. Before commit the old active is authoritative. After commit the restored active KDBX and restored Master Password are authoritative. Do not roll back solely because the success UI or process was interrupted after commit.

### 14.4 Post-commit finalization

A minimal app-private **non-secret restore-finalization marker** may be used after commit. It must not contain the backup password, external URI, credentials, decrypted vault data, or other secrets.

Idempotent finalization invalidates stale biometric quick-unlock/Keystore state when present, cleans candidate/temp artifacts, reconciles routing/session state, and clears the marker. Startup with pending finalization treats the restored KDBX as authoritative and completes required non-secret cleanup before normal use.

Restore remains full-vault replacement only; no merge, dedupe, conflict resolution, or partial import.

## 15. Backup/restore operation status UI

Represent operation status with small local state such as `Idle`, `Copying`, `Validating`, `Restoring`, `Success`, `Error`; do not build a generic workflow engine.

Use existing Compose/Material 3 primitives:

- determinate `LinearProgressIndicator` only when byte progress is accurately measurable;
- indeterminate progress for decrypt/validation/promotion or providers without reliable size/progress;
- optional lightweight `Crossfade`/`AnimatedContent` for status text;
- live-region/state semantics for accessibility.

Never synthesize percentages from elapsed time or arbitrary stage counts.

## 16. Critical-operation single-flight

Use existing local state/`AtomicBoolean`-style guards where suitable. Critical operations require UI in-progress/disablement plus a logic-layer single-flight guard when duplicate events can reach the same mutation.

Keep guards local to the operation/repository boundary. Do not create a global transaction manager.

## 17. Biometric Quick Unlock — T133 / ADR 0006

### 17.1 Secure state

Biometric Quick Unlock is OFF by default. Enable requires current Master Password reauthentication against active KDBX and successful qualifying biometric authentication.

RAHSA:

1. generates a non-exportable Android Keystore symmetric key;
2. requires `BIOMETRIC_STRONG` authentication for key use;
3. wraps the minimum KDBX unlock secret needed at runtime (currently Master Password bytes) using an authenticated platform cipher such as AES-GCM;
4. persists wrapped ciphertext plus required non-secret cipher/state metadata only; and
5. clears transient plaintext/byte copies as soon as practical.

`DEVICE_CREDENTIAL` is not a quick-unlock fallback.

### 17.2 Fail-closed enable lifecycle

`enabled = true` is committed only after key creation, biometric authentication, wrapping, complete secure-state persistence, and consistency verification all succeed. Interrupted/failed enablement remains OFF. Orphaned aliases or incomplete wrapped state are cleanup-only and removable idempotently.

No biometric-specific transaction/recovery marker is introduced.

### 17.3 Real vault-open boundary

Biometric success unwraps the runtime secret transiently in memory and passes it through the normal `VaultRepository.openVault(...)` path. The session/UI becomes unlocked only after real KDBX open succeeds. Biometric prompt success by itself never constitutes unlock.

Auto-prompt occurs at most once per locked entry when enabled/valid. Cancel does not loop. Manual `Unlock with Biometrics` and `Use Master Password` remain available.

### 17.4 Invalidation and fail-closed disable

Missing/invalidated key, enrollment/security-state change, unwrap failure, inconsistent secure state, or real vault-open failure keeps the vault locked, disables/removes unusable quick-unlock state where appropriate, and falls back to Master Password.

Disable requires confirmation but does not lock the current live session. Disable transitions fail closed toward OFF; wrapped state/Keystore alias cleanup is idempotent and a crash must not resurrect a partially removed configuration.

Master Password change, restore, reset, reinstall, or new device invalidates/removes biometric quick-unlock state. Wrapped biometric state is never included in external KDBX backup.

## 18. Dependency strategy for Phase 13

Prefer existing dependencies and platform APIs.

Expected existing/platform reuse:

- Kotpass `0.13.0` for KDBX encode/decode/credential modification;
- Android Storage Access Framework for external backup/restore;
- AndroidX Biometric `1.1.0` + Android Keystore for biometric quick unlock;
- Material 3/Compose progress/animation/swipe primitives;
- `java.security.SecureRandom` for generator hardening.

Potential new dependency only if approved during T129 JIT preparation:

- zxcvbn4j for advisory Master Password strength.

Do not introduce a new crypto library, database, network/cloud SDK, gesture framework, shimmer library, backup container, or generic transaction/workflow framework.

## 19. Error/security behavior

- Wrong authentication credentials → deny access; no decrypted data.
- Corrupt/unreadable vault → non-destructive failure.
- Failed candidate creation/validation → current authoritative state unchanged.
- Pre-commit re-key/restore failure → old active state remains authoritative.
- Post-commit interruption → promoted active remains authoritative; approved non-secret finalization resumes idempotently.
- Destructive reset once started → converge to fully reset state; never reconstruct partially deleted old vault.
- External backup write failure → active/LKG unchanged.
- Restore selection alone → no mutation.
- Biometric UI success without successful repository open → vault remains locked.
- Partial biometric enable/disable → fail closed toward OFF.
- Secrets, candidate payloads, passwords, external restore password, and transient biometric plaintext → never intentionally log.

## 20. Testing strategy

Prioritize risk boundaries.

### Password Generator
- requested length and configured allowed alphabet;
- production `SecureRandom` path;
- existing category configuration/entry points;
- no deterministic production-output assumption.

### Create Vault warning
- Create Vault disabled until acknowledgment;
- swipe/action only acknowledges;
- explicit Create action required;
- acknowledgment resets across abandoned/recreated flow;
- TalkBack/accessibility equivalent works;
- duplicate Create execution rejected.

### Re-key
- wrong current password rejected without mutation;
- warning + acknowledgment required;
- candidate/write/verification failures preserve old vault/hint/biometric state;
- pre-commit crash leaves old state authoritative;
- post-commit crash leaves new vault authoritative and finalization resumes idempotently;
- success: old password fails, new password opens, live session remains unlocked;
- biometric state invalidated when previously active.

### LKG
- unverified candidate never becomes active/LKG;
- exactly one LKG role;
- active corruption + valid LKG offers recovery rather than silent restore.

### Destructive reset
- exact `DELETE` required;
- before final action, lifecycle/process interruption deletes nothing;
- after reset starts, restart completes cleanup idempotently;
- internal vault/LKG/temp/hint/biometric state removed;
- UI preferences and user-managed external backups preserved.

### Backup
- direct encrypted KDBX output remains openable;
- provider/write/close failure leaves internal state unchanged;
- interrupted write does not create internal recovery state;
- truthful determinate vs indeterminate progress.

### Restore
- wrong password/corrupt/unsupported/copy failure leaves active unchanged;
- candidate validated before confirmation;
- current active becomes LKG when applicable;
- pre-commit interruption leaves old active authoritative;
- post-commit interruption leaves restored active authoritative and finalization resumes;
- restored password becomes effective;
- biometric state invalidated when applicable;
- Settings and initial-setup flows both work.

### Biometric
- enable requires current-password reauth;
- partial/interrupted enable remains OFF;
- biometric success causes real vault open;
- cancellation does not auto-loop;
- key/enrollment/inconsistent-state invalidation falls back to Master Password;
- partial disable fails closed toward OFF and does not resurrect;
- password change/restore/reset invalidation hooks work;
- test on a qualifying physical device.

Device/instrumentation validation is mandatory for SAF provider behavior, biometric/Keystore behavior, accessibility swipe behavior, and relevant lifecycle/process-death boundaries.

## 21. Explicit non-goals

No Phase 13 implementation of Autofill, favicon retrieval, camera/OCR scan, passphrase generator, Tags/Categories, Typed Items, additional sort/filter modes, Auto-Lock redesign, cloud sync, recovery key, TOTP, passkeys, accounts, or public-release work.

## 22. Planning-to-implementation gate

```text
approved PRD Phase 13 behavior
→ accepted ADRs for T126–T133
→ final owner approval of consolidated TSD + Threat Model + task package
→ JIT Implementation Kit for exactly T126
→ TASKS activates exactly T126 + kit
→ Codex preflight / implement / validate / checkpoint
→ PLANNING_FREEZE
→ next task requires a new JIT kit and explicit activation
```

Until final technical-package approval and the JIT gate are completed, application-code changes are not authorized.
