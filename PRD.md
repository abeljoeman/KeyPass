# Product Requirements Document — RAHSA v0.2 Baseline

**Status:** Approved stable baseline; product expansion planning in progress  
**Updated:** 2026-09-06  
**Baseline:** `v0.2-prototype`

This PRD describes the approved RAHSA v0.2 product baseline. It does **not** yet approve Phase 13 expansion features.

Historical prototype/UI-phase constraints remain available in Git history. Future capability expansion requires an explicit amendment to this PRD before implementation.

## 1. Product

RAHSA is a local-first Android password manager that stores credentials in an encrypted app-private KDBX vault without requiring an account, backend, cloud synchronization, analytics, or network access for core behavior.

## 2. Approved v0.2 capabilities

RAHSA currently supports:

1. Create an app-private encrypted vault on first launch.
2. Unlock the vault with a master password.
3. Explicitly lock and background/auto-lock the vault.
4. View credentials.
5. Add, edit, and delete credentials.
6. Search credentials by title or username.
7. Generate passwords.
8. Copy credential fields through the reviewed secure-clipboard path.
9. Configure password hint and basic auto-lock behavior.
10. Operate without a backend or Android `INTERNET` permission.

Core credential fields are title, username, password, optional URL, and optional notes.

## 3. Product principles

- Local first.
- Reuse before build.
- Established crypto formats/libraries instead of custom cryptography.
- Security-sensitive shortcuts are not acceptable.
- Preserve the stable baseline unless an approved requirement changes it.
- Product expansion is documented and approved before coding.

## 4. Baseline functional requirements

- **FR-001:** Core RAHSA behavior MUST work without a backend.
- **FR-002:** Persisted credentials MUST use the established KDBX format through the approved vault engine.
- **FR-003:** RAHSA MUST NOT persist the master password in plaintext.
- **FR-004:** RAHSA MUST support credential create/read/update/delete persistence.
- **FR-005:** RAHSA MUST support credential search.
- **FR-006:** RAHSA MUST provide password generation.
- **FR-007:** RAHSA MUST provide manual locking and current approved background/auto-lock behavior.
- **FR-008:** Sensitive values MUST NOT be intentionally written to logs.
- **FR-009:** Sensitive screens MUST use the current secure-screen protections where supported.
- **FR-010:** Core RAHSA SHOULD continue without Android `INTERNET` permission unless a future approved requirement explicitly justifies it.
- **FR-011:** Vault authentication/decryption failure MUST fail closed.
- **FR-012:** Failed vault open/decode MUST NOT silently create or overwrite the existing vault.
- **FR-013:** Credential mutations MUST preserve the last known valid vault on write failure as implemented in the baseline.
- **FR-014:** Existing correct CRUD/search/generator/lock behavior is presumed preserved unless an approved requirement explicitly changes it.
- **FR-015:** The product name in new product-facing work is RAHSA; KeyPass refers only to upstream/legacy technical context.

## 5. Security requirements

- KDBX remains the persisted source of truth.
- Decrypted application state is available only during an unlocked session.
- Manual/background lock removes normal application access to decrypted credential state.
- Wrong master password does not expose credentials.
- Corrupt/unreadable vault failures are non-destructive.
- Secrets are not intentionally logged.
- Existing reviewed clipboard and screen-privacy protections remain regression requirements.

See `docs/THREAT_MODEL.md`.

## 6. Current storage model

```text
app-private-storage/
└── vault.kdbx
```

Current launch behavior:

```text
vault.kdbx missing → create-master-password flow
vault.kdbx exists  → unlock flow
```

The baseline does not expose an external vault picker.

## 7. Expansion governance

The following topics are currently planning candidates only and are **not approved requirements yet**:

- master-password change;
- forgotten-master-password / recovery policy;
- biometric quick-unlock;
- lock/session refinements;
- Autofill;
- safe KDBX backup/export/import;
- passphrase generation;
- other future product capabilities.

Historical prototype non-goal lists are not permanent bans. A candidate becomes approved scope only when this PRD is amended and the required downstream TSD/ADR/Threat Model/TASKS changes are approved.

## 8. Release work

Public/Play Store release preparation is PARKED. Trademark, package/applicationId migration, signing/store identity, listing/compliance, and release-specific attribution work are not current product tasks.

## 9. Success condition for current planning stage

Before implementation of a new expansion capability:

1. behavior and security policy are explicit;
2. reuse options have been assessed;
3. this PRD is amended;
4. TSD/ADR/Threat Model are updated where required;
5. small implementation tasks and acceptance criteria are approved in `TASKS.md`.
