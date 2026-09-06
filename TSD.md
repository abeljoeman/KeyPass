# Technical Design — RAHSA v0.2 Baseline

**Status:** Approved stable technical baseline; expansion design not yet approved  
**Updated:** 2026-09-06  
**Related:** `PRD.md`, `ENGINEERING_PRINCIPLES.md`, `docs/THREAT_MODEL.md`

This TSD describes the technical baseline at `v0.2-prototype`. It does not yet define Phase 13 feature implementations.

## 1. Technical context

- Platform: Android.
- Language: Kotlin / Java 17 compatibility.
- UI: Jetpack Compose + Material 3.
- Vault format/engine: KDBX via Kotpass.
- Base/reference application: upstream KeyPass.
- Persisted storage: app-private `vault.kdbx`.
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

`VaultRepository` isolates application/UI behavior from KDBX-specific operations and remains the approved vault boundary unless a future TSD/ADR explicitly changes it.

## 4. Baseline VaultRepository responsibilities

- Create vault.
- Open/unlock vault using runtime-supplied credentials.
- Lock/clear repository access to decrypted state.
- List credentials.
- Create credential.
- Update credential.
- Delete credential.
- Search credentials.
- Persist safe KDBX mutations.

The repository MUST NOT implement cryptographic primitives itself.

## 5. Storage and session invariants

- KDBX remains the persisted source of truth.
- Do not introduce a second plaintext credential database.
- The master password is not persisted as plaintext.
- Decrypted vault/application data is accessible only during an unlocked session.
- Manual/background lock clears normal application access to decrypted state.
- Failed open/decode is fail-closed and non-destructive.
- Failed writes preserve the last known valid vault according to the baseline implementation.

## 6. Existing product layers to preserve

Unless an approved requirement explicitly changes them:

- retained Compose/navigation/state structure;
- credential CRUD/search flows;
- password generator engine/config;
- secure clipboard path;
- secure-screen behavior;
- current lock lifecycle;
- current error/write-failure protections.

Avoid whole-app rewrites, new state/navigation frameworks, or broad architecture migration as incidental feature work.

## 7. Dependency strategy

Prefer existing dependencies and platform APIs.

Avoid unless an approved design justifies them:

- new crypto libraries;
- new database engines;
- new DI/navigation/state frameworks;
- networking/cloud SDKs;
- generic frameworks created for one feature;
- native/Rust/JNI infrastructure.

Any security-sensitive dependency requires additional review.

## 8. Error/security behavior

- Wrong authentication credentials → deny access; do not expose decrypted data.
- Corrupt/unreadable vault → non-destructive failure; do not overwrite automatically.
- Write failure → surface failure and preserve last known valid persisted vault.
- Missing vault at normal first launch → create-vault flow.
- Secrets MUST NOT be intentionally logged.
- Biometric UI success alone MUST NOT be treated as an unlocked vault. Any future biometric design must result in an actual approved vault-open/session transition.

## 9. Testing strategy

Prioritize risk boundaries:

- vault create/open/reopen;
- wrong credentials;
- KDBX mapping and persistence;
- credential mutation failures;
- lock/unlock and process/background lifecycle;
- corruption handling;
- clipboard/screen privacy;
- any new master-password or biometric security boundary once approved.

Device/instrumentation validation is required where acceptance criteria depend on Android lifecycle, platform security, biometric behavior, or physical-device performance.

## 10. Expansion design status

No technical design is yet approved for:

- master-password change/re-keying;
- forgotten-master-password/recovery;
- biometric quick-unlock;
- Autofill;
- KDBX backup/export/import expansion;
- other Phase 13 features.

Do not infer a solution from inherited KeyPass code. These topics require explicit design review, reuse research, threat-model updates, and approved downstream tasks.

## 11. Planning-to-implementation gate

A future architecture/security change follows:

```text
approved PRD behavior
→ TSD design
→ ADR when a durable architecture/security choice is made
→ Threat Model update when trust/secrets change
→ approved TASKS.md Txxx
→ Codex implementation
```

Codex must stop and return to planning if implementation requires an unapproved architecture or security decision.

## 12. Release status

Public/Play Store release engineering, package/applicationId migration, release signing/store identity, and listing/compliance work are PARKED until explicitly resumed by the owner.
