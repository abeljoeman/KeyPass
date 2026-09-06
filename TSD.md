# Technical Design — RAHSA v0.2 Baseline + Phase 13 Draft

**Status:** v0.2 baseline approved; Phase 13 technical design **DRAFT — owner review required**  
**Updated:** 2026-09-06  
**Related:** `PRD.md`, `ENGINEERING_PRINCIPLES.md`, `docs/THREAT_MODEL.md`, `docs/adr/0005-data-safety-kdbx-lkg-saf.md`, `docs/adr/0006-biometric-quick-unlock-keystore.md`

This TSD preserves the technical baseline at `v0.2-prototype` and proposes the implementation architecture for the owner-approved Phase 13 requirements. It does not activate implementation.

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
ViewModels / operation state
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
  └─ short-lived candidate/temp files
```

The internal file names may differ, but roles and invariants must remain explicit.

## 8. Password Generator hardening

### 8.1 Random source

Replace Kotlin default `.random()` calls used to generate secrets with an injected/owned `java.security.SecureRandom` instance.

### 8.2 Character selection

Build the allowed alphabet from enabled categories and select each output character using secure bounded integer selection over that combined alphabet.

Do not add a new generator UI, passphrase mode, or mandatory complexity behavior.

### 8.3 Testability

Keep the production default CSPRNG secure. If deterministic tests require an abstraction, keep it narrow to random-index selection; do not create a generic crypto framework.

## 9. Create-vault recovery acknowledgment

### 9.1 UI state

Create-vault state gains an `recoveryWarningAcknowledged` (or equivalent) state that is false on entry. The final Create Vault action is enabled only when normal password validation passes and acknowledgment is true.

### 9.2 Swipe interaction

Reuse the Material 3 swipe state/component already available in the current UI dependency (prefer `SwipeToDismissBox`/its state if compatible with the current Material 3 API) rather than implementing low-level drag/pointer gesture handling.

The swipe completion callback changes only acknowledgment state. It does not invoke vault creation.

### 9.3 Accessibility

Expose clear content description/state description and an accessibility action that completes the same acknowledgment transition for TalkBack users. Visual-only dragging must not be the sole operable path.

## 10. Real KDBX Master Password re-key

### 10.1 Existing Kotpass capability

Kotpass `0.13.0` exposes `KeePassDatabase.modifyCredentials { ... }`. Its implementation replaces `KeePassDatabase.credentials` and updates `Meta.masterKeyChanged` when passphrase material changes. Use this established API rather than implementing KDBX cryptography locally.

New passphrase credentials are constructed using the existing Kotpass `Credentials`/`EncryptedValue` APIs already used for vault creation/opening.

### 10.2 Repository API

Add a narrow repository operation for changing Master Password. It must:

1. require an unlocked vault;
2. verify the supplied current Master Password against the active KDBX, not merely UI/session state;
3. derive a candidate database with new Kotpass credentials;
4. encode candidate KDBX to a temporary app-private path;
5. decode/open the candidate using the new password to verify it;
6. preserve the valid old active as LKG according to the storage transaction design;
7. promote the verified candidate atomically/best-effort safely using the existing safe-write pattern;
8. keep the verified new database as the active in-memory session;
9. update the password hint only after successful vault promotion; and
10. invalidate biometric quick-unlock state if such state exists.

Failure before successful promotion must leave the old active vault and old hint usable.

### 10.3 Password strength

The strength indicator is advisory only. Preferred candidate is `zxcvbn4j` (`com.nulab-inc:zxcvbn`, previously researched MIT library). Adoption still requires task-level version/license/security verification before adding the dependency. Do not send Master Password text off-device.

## 11. Last-Known-Good and safe promotion

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
→ verify active / update in-memory state
→ clean temporary artifacts
```

Do not update LKG from an unverified candidate. If a promotion step fails, prefer preserving a usable active/LKG pair over cleanup perfection.

### 11.3 Corrupt active recovery

When active KDBX cannot open due to corruption and an LKG exists, validate LKG separately. If valid, present an explicit restore offer and warn that the latest changes may be lost. No silent restore.

## 12. Forgot Master Password and destructive reset

Forgot flow reads only the stored hint and product explanation; it does not attempt recovery cryptography.

Destructive reset uses exact typed `DELETE` confirmation and a single-flight repository/application operation that removes only RAHSA-managed vault-state artifacts:

- active KDBX;
- LKG KDBX;
- RAHSA-owned candidate/temp/recovery files;
- password hint;
- biometric quick-unlock wrapped state and Keystore alias.

Do not delete SAF/external document URIs or user-selected backup files.

## 13. Manual backup via Storage Access Framework

### 13.1 Destination selection

Use Android's system document creation flow (`ACTION_CREATE_DOCUMENT` or the Activity Result equivalent). RAHSA writes encrypted KDBX bytes to the returned URI through `ContentResolver`.

No direct cloud provider SDK is introduced. Any provider exposed by Android's DocumentsProvider system (local storage, Drive when installed/exposed, SD/USB, etc.) is treated uniformly.

### 13.2 Backup source

Backup the current verified encrypted active KDBX artifact. Do not decrypt and reserialize credentials merely to create an external backup unless a concrete implementation constraint requires it.

### 13.3 Failure behavior

Destination open/write/close failure surfaces an error and does not mutate active or LKG vault state.

## 14. Safe restore via Storage Access Framework

### 14.1 Candidate acquisition

Use Android's system document open flow (`ACTION_OPEN_DOCUMENT` or Activity Result equivalent). Treat the returned URI as an external input only.

Copy the selected document to a short-lived app-private candidate path before promotion. Do not operate on the external file as RAHSA's active vault.

### 14.2 Validation

Ask for the selected backup's password and decode/open the copied candidate with Kotpass. Validate only supported KDBX versions/capabilities. Wrong password and decode/format failures stop before any active mutation.

### 14.3 Confirmation and promotion

Only after successful candidate validation show the replacement confirmation. On confirmation, preserve current valid active as LKG, promote the validated candidate, verify the promoted active, replace the in-memory database/session with the restored database, and invalidate biometric quick unlock.

Initial setup restore uses the same validation/promotion pipeline but has no current active vault to preserve as LKG.

## 15. Backup/restore operation status UI

Represent operation state as a small sealed state/enum such as `Idle`, `Copying`, `Validating`, `Restoring`, `Success`, `Error`; do not create a generic workflow engine.

Use existing Compose/Material 3 primitives:

- `LinearProgressIndicator(progress = ...)` only when byte-copy progress is accurately measurable;
- indeterminate `LinearProgressIndicator()` for decrypt/validation or other non-measurable stages;
- `Crossfade` or `AnimatedContent` for subtle status-label transitions if it stays simple;
- live-region/state semantics so accessibility services receive meaningful operation status.

Never synthesize percentages from arbitrary stage count or elapsed time.

## 16. Critical-operation single-flight

Reuse the existing `AtomicBoolean`/state-guard pattern already used by credential persistence where suitable. Critical operations must have both UI disabling/in-progress state and a logic-layer guard when multiple events could reach the same mutation.

Keep guards local to each operation/repository boundary rather than building a global transaction manager.

## 17. Biometric Quick Unlock design

See proposed ADR `docs/adr/0006-biometric-quick-unlock-keystore.md`.

### 17.1 Secure state

RAHSA does not store plaintext Master Password. When biometric is enabled after explicit current-password reauthentication:

1. generate a non-exportable Android Keystore symmetric key scoped to RAHSA;
2. require qualifying biometric authentication for use of that key;
3. encrypt/wrap the minimum runtime vault-unlock secret needed to open the KDBX (currently the Master Password bytes) using an authenticated platform cipher such as AES-GCM;
4. persist only ciphertext + required non-secret cipher metadata (for example IV) in app-private settings/storage; and
5. clear transient plaintext/byte copies as soon as practical.

This is envelope protection using Android platform cryptography, not a custom vault encryption format.

### 17.2 Authentication

Use AndroidX `BiometricPrompt` with `CryptoObject` and request `BIOMETRIC_STRONG` only. Do not include `DEVICE_CREDENTIAL` as an allowed authenticator for quick unlock.

On biometric success, decrypt the wrapped unlock secret in memory and pass it through the normal `VaultRepository.openVault(...)` path. Home navigation happens only after real vault open succeeds.

### 17.3 Invalidation

Treat missing Keystore key, key invalidation, changed biometric enrollment/security state, decrypt/authentication error, or inconsistent wrapped state as quick-unlock invalidation:

- remove/disable biometric quick-unlock state;
- do not loop prompts;
- retain Master Password unlock path;
- require explicit re-enable after password authentication.

Configure enrollment invalidation where supported by the Android API level and qualifying device capability.

### 17.4 Lifecycle

Wrapped biometric state is tied to the current vault credential. Master Password change, restore, destructive reset, reinstall, or new device invalidates/removes it. It is never included in external KDBX backup.

## 18. Dependency strategy for Phase 13

Prefer existing dependencies and platform APIs.

Expected existing/platform reuse:

- Kotpass `0.13.0` for KDBX encode/decode/credential modification;
- Android Storage Access Framework for external backup/restore;
- AndroidX Biometric `1.1.0` + Android Keystore for biometric quick unlock;
- Material 3/Compose animation/progress primitives for acknowledgment/progress UX;
- `java.security.SecureRandom` for generator hardening.

Potential new dependency only if approved during task preparation:

- zxcvbn4j for advisory Master Password strength.

Do not introduce a new crypto library, database, network/cloud SDK, gesture framework, shimmer library, or backup container.

## 19. Error/security behavior

- Wrong authentication credentials → deny access; no decrypted data.
- Corrupt/unreadable vault → non-destructive failure.
- Failed candidate creation/validation → current active/LKG unchanged.
- External backup write failure → active/LKG unchanged.
- Restore selection alone → no mutation.
- Biometric UI success without a successful repository open → vault remains locked.
- Secrets and candidate payloads → never intentionally log.

## 20. Testing strategy

Prioritize risk boundaries:

### Password Generator
- generated length/allowed alphabet;
- secure RNG path is used;
- all existing entry points still work.

### Create Vault warning
- Create Vault disabled until acknowledgment;
- swipe completion only acknowledges;
- explicit Create action required;
- TalkBack/accessibility action works.

### Re-key
- wrong current password rejected without mutation;
- candidate/write/verification failures preserve old vault and hint;
- success: old password fails, new password opens, session stays unlocked;
- biometric state invalidated when present.

### LKG / backup / restore
- no unverified candidate becomes active/LKG;
- active corruption + valid LKG offers recovery rather than silent restore;
- external backup bytes form an openable KDBX;
- wrong restore password/invalid file leaves active untouched;
- successful restore replaces full vault and invalidates biometric state;
- measurable vs indeterminate progress behavior is truthful.

### Biometric
- enable requires current-password reauth;
- cancel/failure leaves OFF;
- biometric success causes real vault open;
- cancellation does not auto-loop;
- enrollment/key invalidation falls back to Master Password;
- password change/restore/reset invalidates wrapped state;
- test on a physical qualifying device when available.

Device/instrumentation validation is mandatory for SAF provider behavior, biometric/Keystore behavior, accessibility swipe behavior, and relevant lifecycle transitions.

## 21. Explicit non-goals

No Phase 13 implementation of Autofill, favicon retrieval, camera/OCR scan, passphrase generator, Tags/Categories, Typed Items, additional sort/filter modes, Auto-Lock redesign, cloud sync, recovery key, TOTP, passkeys, accounts, or public-release work.

## 22. Planning-to-implementation gate

```text
approved PRD Phase 13 behavior
→ owner reviews this TSD + proposed ADRs + Threat Model
→ owner reviews Phase 13 T126–T133 definitions
→ JIT Implementation Kit for exactly next approved Txxx
→ TASKS activates exactly one Txxx + kit
→ Codex preflight / implement / validate / checkpoint
→ PLANNING_FREEZE
```

Until that gate is completed, application-code changes are not authorized.
