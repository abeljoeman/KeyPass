# Product Requirements Document — RAHSA v0.2 Baseline + Phase 13 Amendment

**Status:** Approved stable baseline; Phase 13 product requirements approved; technical planning in progress  
**Updated:** 2026-09-06  
**Baseline:** `v0.2-prototype`

This PRD preserves the approved RAHSA v0.2 product baseline and adds the owner-approved Phase 13 product amendment. Phase 13 product approval does **not** authorize implementation by itself; the downstream TSD/ADR/Threat Model/TASKS package and execution gate still apply.

Historical prototype/UI-phase constraints remain available in Git history.

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
- Master Password is the root credential; biometric is convenience only; backup protects availability only.
- Critical vault replacement operations fail non-destructively.

## 4. Baseline functional requirements

- **FR-001:** Core RAHSA behavior MUST work without a backend.
- **FR-002:** Persisted credentials MUST use the established KDBX format through the approved vault engine.
- **FR-003:** RAHSA MUST NOT persist the Master Password in plaintext.
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

## 5. Baseline security requirements

- KDBX remains the persisted source of truth.
- Decrypted application state is available only during an unlocked session.
- Manual/background lock removes normal application access to decrypted credential state.
- Wrong Master Password does not expose credentials.
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

The v0.2 baseline does not expose an external vault picker.

## 7. Phase 13 — Access & Data Safety Foundation

Phase 13 is approved product scope for security and data-safety improvements. It does not add cloud sync, accounts, broad credential types, or productivity expansion.

### 7.1 Password Generator security hardening

- **P13-FR-001:** Password generation MUST use a cryptographically secure random source, using the platform `java.security.SecureRandom` or equivalent approved primitive.
- **P13-FR-002:** Enabled character categories SHOULD be combined into the allowed alphabet and characters selected securely from that alphabet; avoid category-first selection that creates unequal per-character probability without a requirement for it.
- **P13-FR-003:** This hardening MUST NOT add passphrase generation, complexity enforcement, or generator UX expansion.
- **P13-FR-004:** Existing generator entry points and configuration behavior MUST remain available.

This is intentionally a low-usage, low-effort security-hygiene item rather than a major product priority.

### 7.2 Create-vault recovery warning and acknowledgment

- **P13-FR-010:** Before a new vault can be created, RAHSA MUST clearly state that RAHSA cannot recover or decrypt the vault if the user forgets the Master Password.
- **P13-FR-011:** The warning MUST require an intentional **swipe-to-acknowledge** action before the final Create Vault action is enabled.
- **P13-FR-012:** The swipe acknowledgment MUST reuse an existing Material 3/platform swipe component or established interaction primitive; RAHSA MUST NOT invent a bespoke gesture framework for this requirement.
- **P13-FR-013:** Completing the swipe MUST only record acknowledgment and enable the explicit Create Vault action. It MUST NOT create the vault automatically.
- **P13-FR-014:** The acknowledgment MUST remain operable with Android accessibility services/TalkBack through appropriate semantics/actions.
- **P13-FR-015:** This warning applies to Create New Vault, not every normal unlock.

### 7.3 Change Master Password

Approved flow:

```text
Vault unlocked
→ Change Master Password
→ re-enter current Master Password
→ new Master Password
→ confirm
→ optional existing hint prefilled/editable
→ advisory strength indicator
→ safe KDBX re-key / persist / verify
→ success
```

- **P13-FR-020:** Current Master Password MUST be verified even when the current session is already unlocked or was opened through biometric quick unlock.
- **P13-FR-021:** New Master Password minimum validation is non-empty plus matching confirmation. No mandatory complexity, expiry, or history policy is introduced.
- **P13-FR-022:** Password strength MAY be shown as advisory information but MUST NOT block the user's chosen password.
- **P13-FR-023:** Change Master Password MUST actually change the KDBX credentials/re-key the vault. Settings-only password changes are invalid.
- **P13-FR-024:** A successful change leaves the current vault session unlocked.
- **P13-FR-025:** After success, the old password MUST fail to open the vault and the new password MUST open it.
- **P13-FR-026:** External backup is not a prerequisite for changing the Master Password.
- **P13-FR-027:** The existing password hint is prefilled and may be retained, changed, or cleared. Failed password change MUST leave the old hint unchanged.
- **P13-FR-028:** If biometric quick unlock was active, a successful Master Password change MUST invalidate/disable it and inform the user that it must be enabled again manually.

### 7.4 Password hint and forgotten Master Password

- **P13-FR-030:** Password hint remains optional and is not a recovery mechanism.
- **P13-FR-031:** RAHSA does not add heuristic validation or warnings about hint contents in Phase 13.
- **P13-FR-032:** `Forgot Master Password?` MUST show the existing hint when available and clearly state that RAHSA cannot decrypt the vault without the correct Master Password.
- **P13-FR-033:** Forgot Master Password MUST NOT present normal backup as password recovery.
- **P13-FR-034:** Biometric MUST NOT be presented as Master Password recovery.
- **P13-FR-035:** Recovery key, account recovery, server reset, and cloud recovery are not part of Phase 13.

### 7.5 Destructive reset

Approved flow:

```text
Forgot Master Password
→ hint + unrecoverable explanation
→ Reset RAHSA / Delete Vault and Start Over
→ type DELETE
→ destructive reset
→ Create New Vault
```

- **P13-FR-040:** User MUST type exact text `DELETE` before destructive reset is enabled.
- **P13-FR-041:** One confirmation flow is sufficient; no countdown or repeated dialogs are required.
- **P13-FR-042:** Reset MUST delete the active vault, internal Last-Known-Good copy, RAHSA-managed temporary/recovery files, password hint, and biometric quick-unlock state tied to the old vault.
- **P13-FR-043:** Reset MUST preserve unrelated UI preferences such as theme.
- **P13-FR-044:** Reset MUST NOT delete user-managed external backups.
- **P13-FR-045:** No special secure-wipe framework is required; normal app-managed deletion is sufficient for this scope.

### 7.6 Internal Last-Known-Good vault

- **P13-FR-050:** RAHSA MUST maintain exactly one internal encrypted Last-Known-Good (LKG) vault copy.
- **P13-FR-051:** LKG protects against failed/corrupt local writes and active-vault corruption. It is not version history, undo, sync, password recovery, or external backup.
- **P13-FR-052:** Before promoting a verified replacement candidate, the valid old active vault is preserved as LKG.
- **P13-FR-053:** An unverified candidate MUST NOT overwrite LKG.
- **P13-FR-054:** If the active vault is corrupt and a valid LKG is available, RAHSA MUST inform the user and offer restore; it MUST NOT silently auto-restore.
- **P13-FR-055:** Destructive reset deletes LKG.

### 7.7 Manual external KDBX backup

Approved flow:

```text
Settings
→ Backup Vault
→ Android system document picker
→ choose provider/location/name
→ write encrypted .kdbx
→ success
```

- **P13-FR-060:** Backup MUST use Android's provider-neutral system document flow/Storage Access Framework.
- **P13-FR-061:** RAHSA MUST NOT add direct Google Drive/cloud API or OAuth integration for this flow.
- **P13-FR-062:** The backup artifact is the encrypted `.kdbx` itself; no custom backup container is introduced.
- **P13-FR-063:** Phase 13 backup is manual only; no scheduled/background backup or cloud synchronization.
- **P13-FR-064:** The user selects destination and filename; a suggested dated filename is allowed.
- **P13-FR-065:** Backup failure MUST NOT modify the active vault.
- **P13-FR-066:** Backup success is reported only after destination writing completes.

### 7.8 Restore backup

Approved flow:

```text
Select external KDBX
→ request password for selected backup
→ decrypt / validate candidate
→ confirmation
→ preserve current active as LKG
→ safe replace internal active vault
→ verify
→ success
```

- **P13-FR-070:** Restore is available from Settings and from initial setup as `Restore Existing Vault`.
- **P13-FR-071:** Selecting a file MUST NOT replace the active vault.
- **P13-FR-072:** Wrong password, invalid/unsupported/corrupt file, decode failure, or validation failure MUST leave the active vault unchanged.
- **P13-FR-073:** The selected backup password may differ from the current vault password; after successful restore, the restored vault's password applies.
- **P13-FR-074:** Restore is full-vault replacement only; no merge, deduplication, conflict resolution, or partial import.
- **P13-FR-075:** External KDBX remains a backup/transport artifact; the active vault remains an internal app-managed KDBX.
- **P13-FR-076:** Successful restore invalidates/disables prior biometric quick-unlock state and informs the user that biometric must be enabled again manually.

### 7.9 Backup/restore progress UX

- **P13-FR-080:** Long-running backup/restore stages MUST provide visible status such as `Menyalin backup…`, `Memvalidasi vault…`, and `Memulihkan vault…`.
- **P13-FR-081:** Progress feedback SHOULD use subtle existing Material 3 animation rather than a custom shimmer/loading framework.
- **P13-FR-082:** Use determinate progress only when progress is actually measurable (for example bytes copied). Use indeterminate progress for operations such as decrypt/validation when accurate percentage is unavailable.
- **P13-FR-083:** RAHSA MUST NOT display fake percentages.
- **P13-FR-084:** Status text MAY use a light existing Compose transition such as `Crossfade`/`AnimatedContent`; accessibility semantics MUST communicate the active operation state.

### 7.10 Critical-operation single execution

- **P13-FR-090:** Change Master Password, backup, restore, destructive reset, create vault, and biometric enable/disable MUST prevent duplicate concurrent execution.
- **P13-FR-091:** UI actions MUST be disabled or show in-progress state while the operation is executing.
- **P13-FR-092:** A logic-layer single-flight guard MUST be used where UI-only prevention is insufficient; reuse existing project patterns when adequate.

### 7.11 Biometric Quick Unlock

Biometric is an opt-in convenience unlock, never password recovery.

- **P13-FR-100:** Biometric Quick Unlock is OFF by default for fresh vaults/installs.
- **P13-FR-101:** User explicitly enables it from Settings.
- **P13-FR-102:** RAHSA MAY show one non-blocking discoverability card after setup/open when the device is capable; `Not Now` MUST NOT cause recurring nagging.
- **P13-FR-103:** Only qualifying Android biometric authentication is accepted. Device PIN/pattern/password is not the fallback for this feature.
- **P13-FR-104:** Enable flow requires re-entry and verification of the current Master Password, then Android biometric authentication, then creation of secure quick-unlock state.
- **P13-FR-105:** Wrong password, cancel, or biometric failure leaves the feature OFF.
- **P13-FR-106:** RAHSA MUST NOT persist the Master Password in plaintext.
- **P13-FR-107:** Biometric success MUST result in a real vault open/decrypt/session transition; navigation-only success is invalid.
- **P13-FR-108:** When enabled and valid, locked entry MAY auto-prompt biometric once. Cancel MUST NOT loop the prompt.
- **P13-FR-109:** Manual actions remain available: `Unlock with Biometrics` and `Use Master Password`.
- **P13-FR-110:** No custom biometric retry counter is introduced; platform behavior applies.
- **P13-FR-111:** Unsupported/not-enrolled state keeps biometric OFF and leaves Master Password available.
- **P13-FR-112:** Biometric enrollment/security-state invalidation disables quick unlock and requires Master Password before optional manual re-enable.
- **P13-FR-113:** Disable uses confirmation, invalidates secure quick-unlock state, and does not lock the already-unlocked session.
- **P13-FR-114:** Reinstall/new device does not migrate biometric quick-unlock state.

## 8. Phase 13 security requirements

- **P13-SR-001:** Plaintext Master Password MUST NOT be persisted, logged, sent to analytics, or transmitted over the network.
- **P13-SR-002:** Temporary candidate vault bytes, backup/restore passwords, generated passwords, and biometric unlock secret material MUST NOT be intentionally logged.
- **P13-SR-003:** KDBX/Kotpass remains the cryptographic source of truth; Phase 13 introduces no custom vault encryption format.
- **P13-SR-004:** Replacement operations MUST retain a valid previous state until the replacement candidate is proven valid.
- **P13-SR-005:** Sensitive UI/ViewModel state MUST be cleared when the relevant lifecycle ends, using existing RAHSA patterns where suitable.
- **P13-SR-006:** Biometric secure state MUST be protected by approved Android Keystore/biometric platform primitives and invalidated when its assumptions no longer hold.

## 9. Explicitly out of Phase 13

### Parked for a future roadmap

- Android Autofill.
- Website/favicon retrieval.
- Camera/OCR credential scanning (future concept may use prefilled title `Scanned Credential`).
- Public/Play Store release work.

### Closed / no change for current roadmap

- Passphrase Generator.
- Additional sort/filter modes beyond existing Title/Username behavior.
- Favorites/Newest/Oldest expansion.
- Tags/Categories.
- Typed credential/item models.
- Session/Auto-Lock redesign.

Also out of scope: cloud sync, accounts, shared vaults, TOTP, passkeys, browser extensions, recovery key, server-side recovery, custom KDBX formats, and restore merge.

## 10. Compatibility

- **P13-CR-001:** Existing valid RAHSA v0.2 vaults MUST continue to open.
- **P13-CR-002:** Existing credentials SHOULD NOT require a manual migration unless the underlying approved KDBX change makes it unavoidable.
- **P13-CR-003:** Compatible external KDBX files MAY be restored when safely supported by the pinned Kotpass implementation.

## 11. Phase 13 product success criteria

Phase 13 is product-complete only when:

1. generated passwords use secure randomness;
2. new-vault creation requires explicit unrecoverability acknowledgment;
3. Master Password change performs real KDBX re-key and is non-destructive on failure;
4. old password fails and new password opens after successful change;
5. Forgot Master Password honestly communicates the recovery limitation;
6. destructive reset requires exact `DELETE` and does not touch external user backups;
7. exactly one internal LKG behaves according to policy;
8. encrypted KDBX backup and safe restore operate through Android's provider-neutral document flow;
9. restore failure cannot damage the active vault;
10. progress UX never fabricates measurable progress;
11. biometric performs real vault unlock and correctly invalidates on password change/restore/enrollment change;
12. critical operations are single-execution;
13. existing approved Auto-Lock behavior remains unchanged; and
14. parked/closed features do not enter implementation accidentally.

## 12. Governance gate

The owner-approved product requirements above feed the technical planning package:

```text
approved PRD Phase 13 amendment
→ TSD / ADR / Threat Model review
→ Phase 13 TASKS review
→ owner approval
→ JIT Implementation Kit for exactly the next task
→ activate exactly one task
→ Codex implementation
```

No Phase 13 application-code work is authorized while `TASKS.md` remains `PLANNING_FREEZE / NONE / NONE`.
