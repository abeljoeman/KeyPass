# Threat Model — RAHSA Android Password Manager

**Status:** v0.2 review preserved; Phase 13 extension **CONSOLIDATED — final owner approval required**  
**Updated:** 2026-09-06  
**Scope:** Local-first Android app, including owner-approved Phase 13 access/data-safety architectures

## 1. Assets

High-value assets:

- Master Password and transient password input.
- Active `vault.kdbx`.
- Internal encrypted Last-Known-Good KDBX.
- External user-managed KDBX backups.
- Short-lived candidate/temp KDBX artifacts.
- Stored credentials and decrypted credentials in application memory.
- Passwords copied to clipboard.
- Generated passwords before saving.
- Restore-candidate password input.
- Biometric wrapped unlock ciphertext/metadata.
- Android Keystore key protecting biometric quick-unlock state.
- Non-secret operation markers used for re-key finalization, reset completion, and restore finalization.

## 2. Trust boundaries

```text
User
 |
 v
Android App Process
 |
 +--> Compose UI / ViewModels
 |       |
 |       +--> Android BiometricPrompt
 |
 +--> VaultRepository
 |       |
 |       +--> Kotpass → active/LKG/candidate KDBX
 |       |
 |       +--> Android Keystore → biometric wrapping key
 |
 +--> ContentResolver / SAF
         |
         v
   External DocumentsProvider
   (local / Drive / SD / USB / other provider)
```

External trust boundaries:

- Android operating system and device lock.
- Biometric subsystem / Trusted Execution Environment where provided by device.
- Android Keystore.
- Clipboard subsystem.
- App-private filesystem.
- User-selected external document providers/storage.
- Third-party libraries included in the app.

No RAHSA backend trust boundary exists for Phase 13.

## 3. Threat actors

- Person with temporary physical access to an unlocked phone.
- Person who steals the phone.
- Malicious Android application on the same device.
- Malware with elevated/device-level privileges.
- Malicious/corrupt external KDBX input.
- Buggy/unavailable external document provider.
- Developer accidentally leaking secrets through logs/debug tooling.
- Future code change accidentally weakening storage/authentication handling.

RAHSA does not claim to protect against a fully compromised/rooted operating system.

## 4. Baseline threats and mitigations

### T1 — Credentials persisted as plaintext — Critical

- Persist credentials only through KDBX/Kotpass.
- Do not maintain a secondary plaintext database.

### T2 — Master Password persisted — Critical

- Master Password exists transiently for active operations/session needs.
- Never store plaintext in SharedPreferences, DataStore, Room, files, logs, or recovery markers.
- Phase 13 biometric stores only Keystore-protected wrapped secret material.

### T3 — Secret values leaked to logs — High

- Never log credential payloads, Master Password, generated passwords, restore passwords, wrapped-secret plaintext, or decrypted vault data.
- Review error/crash/finalization paths.

### T4 — Screenshot/screen-record leakage — Medium/High

- Preserve secure-screen protection on sensitive screens.
- Verify on physical device.

### T5 — Clipboard leakage — High

- Copy only on explicit action using the reviewed secure clipboard path.
- Preserve existing expiration/cleanup behavior.

### T6 — Vault remains unlocked in background — High

- Preserve current manual lock and approved Auto-Lock behavior.
- Phase 13 does not redesign session policy.

### T7 — Corrupted/tampered vault silently accepted — High

- Rely on KDBX integrity/authentication behavior.
- Decode/open failure stays locked/error.
- Never silently replace original vault after decode failure.

### T8 — Network exfiltration — High

- No backend/analytics.
- Core flow should retain no INTERNET permission.
- Backup/restore uses SAF, not a direct cloud/network SDK.

### T9 — Secrets exposed in recent-apps preview — Medium

- Preserve secure-screen behavior and verify on device.

### T10 — Decrypted state retained too long — Medium/High

- Clear reachable decrypted application state on lock/lifecycle boundaries.
- Do not cache plaintext credentials to disk.
- Minimize unnecessary secret copies.

### T11 — Supply-chain dependency risk — Medium

- Minimize dependencies; pin versions; record source/license/security impact.
- Prefer AndroidX/platform and established OSS.
- T126 adds no randomness dependency; T129 strength dependency remains JIT-reviewed only.

### T12 — AI-generated insecure implementation — High

- Use narrow owner-approved tasks and JIT kits.
- Security decisions come from PRD/TSD/ADR/Threat Model, not implementation-agent invention.
- Reject unapproved custom crypto/storage mechanisms.

## 5. Phase 13 threats and mitigations

### T13 — User creates vault without understanding password irrecoverability — High product/data-loss risk

**Mitigation:**
- Explicit unrecoverability warning before Create New Vault.
- Mandatory swipe acknowledgment using existing Material/platform interaction.
- Swipe only acknowledges; explicit Create Vault action remains required.
- Accessibility semantics/action provides equivalent intentional acknowledgment.
- Acknowledgment is ephemeral and resets when the flow is abandoned/recreated; no recovery marker is introduced.

### T14 — Master Password change corrupts or permanently replaces the only valid vault — Critical

**Mitigation:**
- Verify current password against active KDBX.
- Create a new-credential KDBX candidate separately using Kotpass credential-modification APIs.
- Validate candidate with new password before promotion.
- Preserve valid old active as LKG before verified candidate promotion.
- Treat candidate promotion as the explicit commit point.
- Before commit, old vault/password/hint remain authoritative.
- Test failure injection around write/verify/promotion boundaries.

### T15 — LKG is overwritten by corrupt/unverified data — High

**Mitigation:**
- Exactly one LKG, sourced only from a previously valid active vault.
- Candidate validation occurs before LKG replacement/promotion sequence.
- Never use unverified candidate as LKG.
- No silent auto-restore.

### T16 — Malicious/corrupt external restore file destroys current data — Critical

**Mitigation:**
- Treat SAF URI as untrusted external input.
- Copy to app-private candidate first.
- Decode/open with user-supplied backup password using Kotpass before confirmation.
- Wrong password/unsupported/corrupt/copy failure stops with zero active mutation.
- Show replacement confirmation only after validation.
- Full replacement only after explicit confirmation and accepted safe promotion.

### T17 — Backup operation leaks plaintext data — Critical

**Mitigation:**
- Copy the already-encrypted active KDBX artifact directly through SAF.
- No plaintext export/container.
- No secret logging.
- External location is explicitly user selected and outside RAHSA's confidentiality guarantee once written.
- Backup never decrypts/re-serializes merely for transport.

### T18 — Duplicate critical operation races produce inconsistent vault state — High

**Mitigation:**
- UI in-progress disablement plus logic-layer single-flight guard.
- Serialize relevant vault replacement mutations.
- No concurrent change-password/restore/reset/create operations.
- Avoid a broad global transaction framework; keep guards local and explicit.

### T19 — Biometric bypass via UI-only success — Critical

**Mitigation:**
- Biometric success alone never marks repository/session unlocked.
- `BiometricPrompt.CryptoObject` unwraps the Keystore-protected secret.
- Normal `VaultRepository.openVault` must succeed before unlocked navigation.

### T20 — Biometric wrapped secret becomes usable after security assumptions change — High

**Mitigation:**
- Keystore key requires qualifying `BIOMETRIC_STRONG` authentication.
- Configure enrollment invalidation where supported.
- Missing/invalid key, enrollment/security change, unwrap failure, Master Password change, restore, reset, reinstall, or new device disables/removes quick unlock.
- No device-credential fallback for this feature.

### T21 — Wrapped Master Password or transient plaintext leaks through persistence/logs — Critical

**Mitigation:**
- Persist ciphertext + non-secret cipher/state metadata only.
- Never log wrapped plaintext/cipher operation inputs.
- Clear transient buffers/state as soon as practical.
- Wrapped state is app-private and not exported in KDBX backup.

### T22 — Fake progress causes unsafe user interruption/false confidence — Low/Medium

**Mitigation:**
- Determinate progress only when measurable.
- Indeterminate progress for validation/decrypt/promotion or providers without reliable size.
- Subtle Material 3 animation is status feedback, not fabricated completion percentage.

### T23 — Destructive reset deletes user-managed backups — High

**Mitigation:**
- Reset deletion scope is limited to RAHSA-managed internal vault/LKG/temp, hint, biometric state, and related Keystore alias.
- Never delete external SAF backup documents.
- Require exact typed `DELETE`.
- Preserve unrelated UI preferences.

### T24 — Re-key crashes after vault promotion but before metadata/biometric cleanup — High

**Risk:** The new KDBX may already be authoritative while old hint or biometric state remains, creating misleading or stale security metadata.

**Mitigation:**
- Promotion is the explicit commit point; after commit the new KDBX/password remain authoritative.
- Use a minimal non-secret post-commit finalization marker when needed.
- On restart, complete hint application, biometric invalidation, and temp cleanup idempotently.
- Marker contains no Master Password or decrypted vault data.
- Never speculatively roll back the promoted KDBX because UI completion was interrupted.

### T25 — Destructive reset is interrupted and leaves a partially deleted old security state — Critical data/state consistency risk

**Mitigation:**
- Persist non-secret `RESET_IN_PROGRESS` (or equivalent) before destructive cleanup.
- Once reset starts, Back/Home/navigation is not a transaction cancel.
- Cleanup is idempotent and converges toward fully reset state.
- Startup completes pending reset before normal vault routing.
- Missing/already-deleted artifacts are treated as already-cleaned where safe.
- Do not reconstruct or roll back a started destructive reset.

### T26 — Restore crashes after candidate promotion but before biometric/session/temp finalization — High

**Risk:** Restored KDBX is authoritative but stale biometric state or temp artifacts may remain.

**Mitigation:**
- Promotion is the explicit commit point.
- After commit, restored KDBX/password remain authoritative.
- Use a minimal non-secret restore-finalization marker when needed.
- Restart completes biometric invalidation, temp cleanup, and routing/session reconciliation idempotently before normal use.
- Marker stores no backup password, external URI, credentials, or decrypted vault data.

### T27 — Partial biometric enable/disable accidentally leaves quick unlock usable — Critical

**Mitigation:**
- Enable is fail-closed: `enabled = true` only after key generation, biometric authentication, wrapping, complete state persistence, and consistency verification all succeed.
- Interrupted/failed enable remains OFF; incomplete state/orphan key is cleanup-only.
- Disable fails closed toward OFF; cleanup is idempotent and partial removal cannot safely resurrect the feature.
- No biometric-specific recovery marker is introduced.

### T28 — Non-cryptographic randomness weakens generated credentials — High

**Mitigation:**
- Use direct `java.security.SecureRandom` bounded selection.
- Build one allowed alphabet from enabled character categories and select each character from that alphabet.
- Remove category-first/default Kotlin random selection from secret generation.
- Add no third-party randomness dependency.

### T29 — Operation marker accidentally becomes a new secret store — Critical

**Mitigation:**
- Markers are narrow, app-private, non-secret state only.
- Re-key marker stores no old/new Master Password or decrypted vault data.
- Reset marker stores no credentials or vault contents.
- Restore marker stores no backup password, external URI, credentials, or decrypted vault data.
- No markers are added for Create Vault, manual backup, or biometric enable/disable.
- Marker use must be task-specific and idempotent, not a generic workflow database.

## 6. Security assumptions

- Android OS is not fully compromised.
- User can protect the device with an OS lock.
- Kotpass/KDBX cryptography behaves correctly for supported files.
- Android Keystore/BiometricPrompt enforce their documented device guarantees on qualifying hardware.
- External document providers may fail, disappear, or expose files according to their own policies; RAHSA only controls what it writes.
- RAHSA does not attempt forensic secure erase against a fully compromised/rooted live device.

## 7. Phase 13 security validation gates

Planning architecture is approved, but these implementation gates remain unchecked until code/device validation occurs:

- [ ] Generator uses `SecureRandom` combined-alphabet selection and no non-security Kotlin default randomness for secret generation.
- [ ] New-vault recovery warning cannot be bypassed through ordinary UI flow, acknowledgment is ephemeral, explicit Create remains required, and accessibility equivalent works.
- [ ] Master Password re-key: wrong current password is non-destructive.
- [ ] Re-key: old password fails/new password opens after success.
- [ ] Re-key failure injection preserves old valid vault/hint/biometric state before commit.
- [ ] Re-key post-commit process death preserves new authoritative vault and resumes non-secret finalization idempotently.
- [ ] LKG never accepts unverified candidate data.
- [ ] Active corruption + valid LKG requires user confirmation to restore.
- [ ] Destructive reset deletes nothing before final action and completes idempotently after restart once started.
- [ ] Destructive reset preserves UI preferences and external user-managed backups.
- [ ] Backup produces encrypted KDBX through a user-selected SAF destination without decrypt/re-serialize transport.
- [ ] Backup failure/interruption leaves active/LKG unchanged and creates no internal recovery marker.
- [ ] Invalid/wrong-password/corrupt restore cannot mutate active vault.
- [ ] Restore confirmation is shown only after candidate validation.
- [ ] Successful restore preserves old active as LKG when applicable.
- [ ] Restore post-commit process death preserves restored authoritative vault and resumes non-secret finalization idempotently.
- [ ] Restore/password-change/reset invalidate biometric state when applicable.
- [ ] Biometric partial enable remains OFF; partial disable fails closed toward OFF.
- [ ] Biometric success performs actual repository vault open.
- [ ] Biometric cancellation does not loop prompts.
- [ ] Enrollment/key/inconsistent-state invalidation falls back to Master Password.
- [ ] No Phase 13 secret values appear in Logcat during representative tests.
- [ ] Critical operations reject duplicate execution.
- [ ] Approved operation markers contain no secret material and are cleaned idempotently.

## 8. Historical v0.2 review record

The Phase 7/T077 review at checkpoint `a0df20b` found no new unresolved Critical/High implementation issue at that time. The v0.2 baseline validated KDBX persistence, no intended plaintext Master Password persistence, logging hardening, secure screen, reviewed clipboard behavior, fail-closed corruption handling, no core network requirement, and decrypted-state cleanup patterns. Those findings remain baseline evidence but do not pre-validate new Phase 13 code.

Phase 13 checklist items stay unchecked until implementation/device validation is actually performed.
