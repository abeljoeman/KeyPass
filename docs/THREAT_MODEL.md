# Threat Model — Android Password Manager Prototype

**Status:** Draft  
**Created:** 2026-08-24  
**Scope:** Local-only Android prototype

This is intentionally lightweight. It identifies prototype assets, trust boundaries, major threats, and required mitigations.

## 1. Assets

High-value assets:

- Master password
- `vault.kdbx`
- Stored credentials
- Decrypted credentials in application memory
- Passwords copied to clipboard
- Generated passwords before saving

## 2. Trust Boundaries

```text
User
 |
 v
Android App Process
 |
 +--> UI / ViewModels
 |
 +--> VaultRepository
        |
        v
      Kotpass
        |
        v
   Local vault.kdbx
```

External trust boundaries:

- Android operating system
- Device lock / physical access
- Clipboard subsystem
- Local filesystem
- Third-party libraries included in the app

No backend trust boundary exists in the prototype.

## 3. Threat Actors

- Person with temporary physical access to an unlocked phone
- Person who steals the phone
- Malicious Android application on the same device
- Malware with elevated/device-level privileges
- Developer accidentally leaking secrets through logs/debug tooling
- Future code change accidentally weakening storage handling

The prototype does not claim to protect against a fully compromised/rooted operating system.

## 4. Threats and Mitigations

### T1 — Credentials persisted as plaintext

**Risk:** Critical

**Mitigation:**
- Persist credentials only through KDBX/Kotpass.
- Do not maintain a secondary plaintext database.
- Test app-private files after normal flows.

### T2 — Master password persisted

**Risk:** Critical

**Mitigation:**
- Master password exists only for the active unlock operation/session.
- Do not store it in SharedPreferences, DataStore, Room, files, or logs.
- Biometric key wrapping is deferred rather than improvised.

### T3 — Secret values leaked to logs

**Risk:** High

**Mitigation:**
- Audit logging statements.
- Never log credential payloads, master passwords, generated passwords, or decrypted vault data.
- Review crash/error messages.

### T4 — Screenshot / screen-record leakage

**Risk:** Medium / High

**Mitigation:**
- Use Android secure-screen protection on sensitive screens where supported.
- Test screenshot behavior on a physical device.

### T5 — Clipboard leakage

**Risk:** High

**Mitigation:**
- Copy only on explicit user action.
- Prefer clearing copied password after a short interval where reliable.
- Avoid copying automatically.
- Document Android-version limitations.

### T6 — Vault remains unlocked in background

**Risk:** High

**Mitigation:**
- Manual lock.
- Lock after background/timeout based on prototype policy.
- Test process/background transitions.

### T7 — Corrupted/tampered vault is silently accepted

**Risk:** High

**Mitigation:**
- Rely on KDBX integrity/authentication behavior.
- Treat decode/open failure as locked/error state.
- Never silently replace the original vault after decode failure.

### T8 — Network exfiltration

**Risk:** High

**Mitigation:**
- No backend.
- No analytics.
- Prefer no INTERNET permission.
- Review dependency behavior and Android manifest.

### T9 — Secrets exposed in recent-apps preview

**Risk:** Medium

**Mitigation:**
- Secure-screen behavior should cover recent-app preview where Android supports it.
- Verify on target device.

### T10 — Decrypted state retained longer than intended

**Risk:** Medium / High

**Mitigation:**
- Clear reachable decrypted application state on lock.
- Do not cache decrypted credentials to disk.
- Minimize unnecessary copies of password strings.

### T11 — Supply-chain dependency risk

**Risk:** Medium

**Mitigation:**
- Minimize dependencies.
- Pin versions.
- Record source and license.
- Prefer AndroidX and established open-source libraries.
- Review dependency changes before upgrades.

### T12 — AI-generated insecure implementation

**Risk:** High

**Mitigation:**
- AI agents implement narrowly scoped tasks.
- Security decisions come from TSD/ADRs, not agent invention.
- Review security-sensitive diffs.
- Reject new crypto/storage mechanisms not approved in ADRs.

## 5. Security Assumptions

- Android OS is not fully compromised.
- The user can protect the device with an OS lock.
- KDBX/Kotpass performs vault cryptography correctly for supported files.
- The application does not attempt to defend against forensic extraction from a fully rooted live device.

## 6. Prototype Security Exit Checklist

- [ ] Master password not persisted in plaintext.
- [ ] Credential data not persisted outside encrypted KDBX.
- [ ] No secret logging found in defined test flow.
- [ ] Wrong password fails closed.
- [ ] Corrupted vault fails visibly and non-destructively.
- [ ] Secure-screen behavior enabled.
- [ ] Manual lock works.
- [ ] Background/timeout lock works.
- [ ] Clipboard behavior reviewed.
- [ ] INTERNET permission absent or exception documented.

## 7. T077 Threat-Model Review Record

Review performed at checkpoint `a0df20b` after Phase 7 hardening.

| Threat | Review finding | Remaining verification |
| --- | --- | --- |
| T1 — Plaintext credential persistence | Mitigated in the prototype architecture: credential CRUD persists through Kotpass/KDBX, legacy Room runtime consumers were removed, and no active plaintext credential export path was found. | Inspect app-private storage on a physical device in Phase 8. |
| T2 — Master password persistence | Mitigated in source: unlock uses transient password input and the static storage/logging audits found no intended persistence path. | Re-check app-private files and Logcat during device testing. |
| T3 — Secret logging | Application logging paths were hardened and static audits found no intentional secret logging. | Run representative flows under Logcat in Phase 8. |
| T4 — Screenshot / screen-record leakage | `FLAG_SECURE` is enabled on sensitive app activities. | Verify screenshot/screen-record behavior on the target device. |
| T5 — Clipboard leakage | Copy is explicit-only; shared sensitive clipboard handling is used, with Android 13+ platform expiration and a 60-second best-effort cleanup on older supported Android versions. | Verify clipboard behavior on representative target Android versions. |
| T6 — Vault remains unlocked in background | Manual lock and prototype background/timeout locking are implemented, and reachable decrypted UI/repository state is cleared on lock. | Exercise background, foreground, process-kill, and timeout transitions on-device. |
| T7 — Corrupted/tampered vault | Failed decode leaves the repository locked; a unit test verifies corrupted source bytes are not overwritten. | Verify the UI presents a recoverable, non-destructive error on-device. |
| T8 — Network exfiltration | No backend is required; FreeDebug merged-manifest audit found no `INTERNET` or network-state permissions, and no analytics/telemetry SDK is apparent in current app/common runtime dependencies. | Confirm core flows in airplane mode and observe runtime behavior in Phase 8. |
| T9 — Recent-app preview leakage | Sensitive activities use the same secure-screen protection as T4. | Verify the recent-app preview on the target device. |
| T10 — Decrypted state lifetime | Lock clears repository session state and dashboard sensitive state; T074 found no decrypted credential disk cache in the active prototype flow. | Validate lock/process transitions and storage after normal flows on-device. |
| T11 — Supply-chain dependency risk | Dependencies are explicit and version-pinned in Gradle, including Kotpass `0.13.0`; Phase 2 removed dependencies tied only to removed features. | Continue dependency/license review before release or future upgrades. |
| T12 — AI-generated insecure implementation | Security work was split into narrow tasks with diff review, unit/build checks, and task-per-commit history; no new crypto mechanism was introduced. | Continue the same review discipline for Phase 8 fixes and later changes. |

### Review conclusion

No new unresolved Critical or High implementation finding was identified during T077. The remaining items are primarily runtime/device verification gates already represented in Phase 8; they must not be treated as passed until those checks are executed.

The Prototype Security Exit Checklist above intentionally remains unchecked at this checkpoint. It is a release-exit checklist, while T077 records the current evidence and residual verification without prematurely claiming the physical-device gates have passed.
