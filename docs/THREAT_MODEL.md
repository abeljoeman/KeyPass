# Threat Model — RAHSA Android Password Manager

**Status:** v0.2 review preserved; Phase 13 extension **DRAFT — owner review required**  
**Updated:** 2026-09-06  
**Scope:** Local-first Android app, including proposed Phase 13 access/data-safety boundaries

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
- Never store plaintext in SharedPreferences, DataStore, Room, files, or logs.
- Phase 13 biometric stores only Keystore-protected wrapped secret material.

### T3 — Secret values leaked to logs — High

- Never log credential payloads, Master Password, generated passwords, restore passwords, wrapped-secret plaintext, or decrypted vault data.
- Review error/crash paths.

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
- Accessibility semantics/action must provide equivalent intentional acknowledgment.

### T14 — Master Password change corrupts or permanently replaces the only valid vault — Critical

**Mitigation:**
- Verify current password.
- Create new-credential KDBX candidate separately using Kotpass credential-modification APIs.
- Validate candidate with new password before promotion.
- Preserve valid old active as LKG before verified candidate promotion.
- Update hint only after successful promotion.
- Test failure injection around write/verify/promotion steps.

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
- Wrong password/unsupported/corrupt file stops with zero active mutation.
- Full replacement only after explicit confirmation and safe promotion.

### T17 — Backup operation leaks plaintext data — Critical

**Mitigation:**
- Backup the already-encrypted KDBX artifact through SAF.
- No plaintext export/container.
- No secret logging.
- External location is explicitly user selected and outside RAHSA's confidentiality guarantee once written.

### T18 — Duplicate critical operation races produce inconsistent vault state — High

**Mitigation:**
- UI in-progress disablement plus logic-layer single-flight guard.
- Serialize relevant vault replacement mutations.
- No concurrent change-password/restore/reset/create operations.

### T19 — Biometric bypass via UI-only success — Critical

**Mitigation:**
- Biometric success alone never marks repository/session unlocked.
- `BiometricPrompt.CryptoObject` unlocks the Keystore-protected wrapped secret.
- Normal `VaultRepository.openVault` must succeed before unlocked navigation.

### T20 — Biometric wrapped secret becomes usable after security assumptions change — High

**Mitigation:**
- Keystore key requires qualifying biometric authentication.
- Configure enrollment invalidation where supported.
- Missing/invalid key, enrollment/security change, unwrap failure, Master Password change, restore, or reset disables quick unlock.
- No device-credential fallback for this feature.

### T21 — Wrapped Master Password or transient plaintext leaks through persistence/logs — Critical

**Mitigation:**
- Persist ciphertext + non-secret cipher metadata only.
- Never log wrapped plaintext/cipher operation inputs.
- Clear transient buffers/state as soon as practical.
- Wrapped state is app-private and not exported in KDBX backup.

### T22 — Fake progress causes unsafe user interruption/false confidence — Low/Medium

**Mitigation:**
- Determinate progress only when measurable.
- Indeterminate progress for validation/decrypt stages.
- Subtle Material 3 animation is status feedback, not fabricated completion percentage.

### T23 — Destructive reset deletes user-managed backups — High

**Mitigation:**
- Reset deletion scope is limited to RAHSA-managed internal vault/LKG/temp, hint, and biometric state.
- Never delete external SAF backup documents.
- Require exact typed `DELETE`.

## 6. Security assumptions

- Android OS is not fully compromised.
- User can protect the device with an OS lock.
- Kotpass/KDBX cryptography behaves correctly for supported files.
- Android Keystore/BiometricPrompt enforce their documented device guarantees on qualifying hardware.
- External document providers may fail, disappear, or expose files according to their own policies; RAHSA only controls what it writes.
- RAHSA does not attempt forensic secure erase against a fully compromised/rooted live device.

## 7. Phase 13 security validation gates

- [ ] Generator no longer uses non-security Kotlin default randomness for secret generation.
- [ ] New-vault recovery warning cannot be bypassed through ordinary UI flow and is accessible.
- [ ] Master Password re-key: wrong current password is non-destructive.
- [ ] Re-key: old password fails/new password opens after success.
- [ ] Re-key failure injection preserves old valid vault and hint.
- [ ] LKG never accepts unverified candidate data.
- [ ] Active corruption + valid LKG requires user confirmation to restore.
- [ ] Backup produces encrypted KDBX through a user-selected SAF destination.
- [ ] Invalid/wrong-password restore cannot mutate active vault.
- [ ] Successful restore preserves old active as LKG when applicable.
- [ ] Restore/password-change/reset invalidate biometric state.
- [ ] Biometric success performs actual repository vault open.
- [ ] Biometric cancellation does not loop prompts.
- [ ] Enrollment/key invalidation falls back to Master Password.
- [ ] No Phase 13 secret values appear in Logcat during representative tests.
- [ ] Critical operations reject duplicate execution.
- [ ] External user-managed backups survive destructive reset.

## 8. Historical v0.2 review record

The Phase 7/T077 review at checkpoint `a0df20b` found no new unresolved Critical/High implementation issue at that time. The v0.2 baseline validated KDBX persistence, no intended plaintext Master Password persistence, logging hardening, secure screen, reviewed clipboard behavior, fail-closed corruption handling, no core network requirement, and decrypted-state cleanup patterns. Those findings remain baseline evidence but do not pre-validate new Phase 13 code.

Phase 13 checklist items stay unchecked until implementation/device validation is actually performed.
