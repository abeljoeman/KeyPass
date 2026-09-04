# Prototype Test Plan — Android Password Manager

**Status:** Draft  
**Target:** Physical Android device

This checklist is the minimum release gate for `v0.1-prototype`.

## 1. Environment

Record before execution:

- Device:
- Android version:
- App build/commit:
- Build type:
- Tester:
- Date:

## 2. Build and Installation

- [ ] Clean build succeeds.
- [ ] APK installs via `adb`.
- [ ] Application launches.
- [ ] Application relaunches after force stop.
- [ ] Application launches after device reboot.

## 3. Vault Creation

- [ ] New vault can be created.
- [ ] Empty master password is rejected if disallowed by product rule.
- [ ] Canceling creation does not leave a broken vault.
- [ ] Created vault exists after app restart.

## 4. Vault Unlock

- [ ] Correct master password opens vault.
- [ ] Wrong master password does not open vault.
- [ ] Multiple wrong attempts do not expose data.
- [ ] Opening a missing vault shows a recoverable error.
- [ ] Opening a corrupted vault shows a non-destructive error.

## 5. Credential CRUD

Create at least 20 entries.

For each field type, include normal and long values.

- [ ] Add credential works.
- [ ] Title persists.
- [ ] Username persists.
- [ ] Password persists.
- [ ] URL persists.
- [ ] Notes persist.
- [ ] Edit credential works.
- [ ] Cancel edit does not persist changes.
- [ ] Delete credential works.
- [ ] CRUD results remain correct after vault close/reopen.
- [ ] CRUD results remain correct after app restart.

## 6. Search

- [ ] Search by title works.
- [ ] Search by username works.
- [ ] No-result state is clear.
- [ ] Clearing query restores list.

## 7. Password Generator

- [ ] Generates password.
- [ ] Length setting is respected.
- [ ] Character-category settings are respected.
- [ ] Generated value can be inserted into credential.
- [ ] Generated value is not visible in normal logs.

### T063 static logging audit

Source audit completed for the password-generation path at checkpoint `e68596f`:

- `GeneratePasswordViewModel` generates into in-memory UI state and contains no logging calls.
- `GeneratePasswordScreen` passes the generated value only to explicit copy/use callbacks and contains no logging calls.
- `PasswordGenerator` returns the generated string and contains no logging calls.
- `CopyTextToClipboard` writes only to the Android clipboard and contains no logging calls.

Result: no application logging path for generated password values was found in the audited generator flow.

This static verification does not replace the runtime Logcat checks in section 12; those remain part of the physical-device release test.

## 8. Lock Behavior

- [ ] Manual lock works.
- [ ] After lock, credential screens cannot be accessed without unlock.
- [ ] Background/timeout lock works according to configured prototype behavior.
- [ ] After process kill, app does not resume directly into decrypted vault state.

## 9. Clipboard

- [ ] Username copy works.
- [ ] Password copy works.
- [ ] Copy requires explicit user action.
- [ ] Clipboard clearing behavior matches implementation/documentation.

## 10. Screen Privacy

- [ ] Screenshot of sensitive screen is blocked or blank where supported.
- [ ] Recent-app preview does not reveal credential content where supported.

## 11. Storage Inspection

Using permitted development/debug tooling:

- [ ] No master-password plaintext file is found.
- [ ] No plaintext credential database/file is found.
- [ ] Persisted vault is KDBX.
- [ ] Temporary files do not contain obvious plaintext credentials after normal close/lock.

## 12. Logging Inspection

Run representative flows while monitoring logs:

- [ ] Master password absent.
- [ ] Credential password absent.
- [ ] Generated password absent.
- [ ] Full decrypted vault payload absent.

## 13. Network / Permissions

- [ ] Core prototype works in airplane mode.
- [ ] No backend endpoint is required.
- [ ] INTERNET permission is absent, or the approved exception is documented.
- [ ] No analytics/telemetry SDK sends data.

## 14. Failure and Recovery

- [ ] App handles storage-write failure without silently claiming success.
- [ ] Decode failure does not overwrite source vault.
- [ ] App can return to unlock/create flow after recoverable failure.

## 15. Exit Decision

### Pass

All P1 product stories work and no unresolved high-risk security finding exists.

### Conditional Pass

Functional prototype works but one or more documented medium-risk prototype limitations remain.

### Fail

Any of the following occurs:

- Plaintext credential persistence
- Plaintext master-password persistence
- Wrong password opens vault
- Silent vault corruption/destruction
- Unexplained secret leakage to logs/network
