# Prototype Physical Test Results — v0.1

**Status:** Test execution complete; release tag blocked pending fixes
**Date:** 2026-09-05
**Test plan:** `docs/TEST_PLAN.md`

## Environment

- Device: Samsung Galaxy A11 (`SM-A115F`)
- Android: 12
- API level: 31
- Build type: `freeDebug`
- App package: `com.yogeshpaliyal.keypass.staging`
- Test checkpoint: `312035b` (`docs: record threat model review`)
- Execution method: physical-device manual testing with `adb` inspection where appropriate

No application code was changed during this test pass.

## Result Summary

| Test area | Result | Notes |
| --- | --- | --- |
| Build and installation | PASS | Clean build/install/launch, force-stop relaunch, and device-reboot launch verified. |
| Vault creation | PASS | Create, empty-password rejection, cancel, and restart persistence verified. |
| Vault unlock | PASS with UX limitation | Correct/wrong password behavior is fail-closed. Corrupted vault is non-destructive but is surfaced as an incorrect-password error. |
| Credential CRUD | PASS with findings | 20 credentials tested, including long values. Create/edit/cancel/delete/reopen/restart persistence verified. Double-tap Save can create duplicates. |
| Search | PASS | Title, username, no-result, and query-clear behavior verified. |
| Password generator | PASS | Length/categories/use-in-editor verified; generated value was not observed in normal logs. |
| Lock behavior | PASS with crash finding | Manual lock, background lock, and process-kill recovery pass their functional checks. A separate lock/load race can crash the app. |
| Clipboard | PARTIAL | Explicit copy works and foreground 60-second cleanup works. Background cleanup failed on Android 12/API 31. |
| Screen privacy | PASS | ADB screenshot hides credentials and Recent Apps preview hides credential content. |
| Storage inspection | PASS | KDBX signature verified; no tested credential markers found in plaintext app storage or temporary files. |
| Logging inspection | PASS | Tested credential values, generated password, and decrypted payload markers were not observed in Logcat. |
| Network / permissions | PASS | Core flows work in airplane mode and `INTERNET` permission is absent. |
| Failure and recovery | PARTIAL | Corrupt/decode failure is non-destructive. Simulated storage-write failure preserves vault integrity but crashes the app. |

## Phase 8 Task Coverage

- **T080:** `docs/TEST_PLAN.md` executed on the physical device.
- **T081:** At least 20 credentials tested, including deliberately long field values.
- **T082:** Wrong master password rejected; repeated wrong attempts did not expose data.
- **T083:** App restart and vault reopen verified with persisted credential data.
- **T084:** Manual/background/foreground lock behavior verified.
- **T085:** Process kill returns to the unlock flow instead of decrypted credential state.
- **T086:** Known limitations and release blockers are captured below.
- **T087:** Not complete. Do not tag `v0.1-prototype` until release blockers are fixed and relevant physical tests are repeated.

## Confirmed Release Blockers

### 1. Lock/load race can crash the application

**Severity:** High
**Status:** Confirmed on physical device

Observed exception:

```text
java.lang.IllegalStateException: Vault is locked.
```

Observed path:

```text
DashboardViewModel.loadCredentials()
 -> VaultRepository.listCredentials()
 -> KotpassVaultRepository.unlockedDatabase()
```

The repository can become locked while credential loading is still running or is triggered during the UI transition to the authentication screen. The exception is not handled and terminates the application.

**Expected:** A lock transition cancels/ignores stale credential loads and returns to authentication without crashing.

### 2. Storage-write failure crashes the application

**Severity:** High
**Status:** Confirmed on physical device

A controlled test made the app's `files` directory temporarily non-writable, then attempted to save an edited credential. The app crashed with:

```text
java.io.IOException: Permission denied
```

The failure originated while `persistDatabase()` attempted to create its temporary file.

Security/integrity behavior during this failure was good:

- the pre-test and post-crash SHA-256 of `vault.kdbx` were identical;
- the vault reopened normally;
- the credential remained present;
- the failed edit was not persisted.

**Expected:** The save failure is surfaced to the UI without crashing, while retaining the same vault-integrity behavior.

### 3. Double-tap Save can create duplicate credentials

**Severity:** Medium
**Status:** Confirmed on the slower physical device

Rapidly tapping Save twice during credential creation produced two identical credentials.

**Expected:** Save is single-flight. The UI disables Save while persistence is running and the ViewModel/repository-facing action ignores a second concurrent save request.

## Known Security / Platform Limitation

### Clipboard auto-clear can fail while KeyPass is backgrounded on Android 12

**Severity:** Medium
**Device:** Samsung Galaxy A11, Android 12 / API 31

Observed:

- explicit username/password copy: PASS;
- foreground automatic cleanup after about 60 seconds: PASS;
- automatic cleanup after KeyPass is backgrounded: FAIL.

The implementation performs an ownership check before clearing. On Android versions that restrict clipboard reads when an app lacks focus, that ownership check may be unavailable, so cleanup is skipped rather than risking deletion of clipboard content written by another application.

Android 13+ has platform clipboard expiration, but this device does not provide that fallback.

## Lower-Severity UX Findings

- **Credential title accepts newline/multiline input.** The value persists and displays on multiple lines in list/detail views, while the editor visually behaves like a single-line title field. The title should be explicitly single-line.
- **Corrupted vault error is ambiguous.** Corrupted KDBX data fails closed and is not overwritten, but the physical UI reports an incorrect-password-style error rather than distinguishing corruption/decode failure.
- **Very long credential title consumes substantial initial detail-screen height.** Remaining fields are accessible by scrolling.
- **List ordering observation:** one tested credential appeared at a different list position after reload. No data loss occurred and the vault hash did not change. Treat as an observation unless reproduced under a defined sort setting.

## Security-Preserving Findings

- Wrong master password does not open the vault.
- Repeated wrong-password attempts do not expose credential data.
- Corrupted-vault decode failure does not overwrite the source vault.
- Storage-write failure did not alter the existing KDBX file in the controlled test.
- Persisted vault starts with the expected KDBX signature `03 d9 a2 9a 67 fb 4b b5`.
- Tested credential markers were not found in plaintext app-private storage.
- Tested credential values and decrypted payload markers were not observed in normal Logcat inspection.
- Sensitive screenshots and Recent Apps previews were protected.
- Core prototype functionality works offline.
- The installed prototype does not request `android.permission.INTERNET`.

## Release Gate

`v0.1-prototype` should **not** be tagged from this checkpoint.

Before T087:

1. Fix the lock/load race crash.
2. Handle storage-write failures without crashing.
3. Prevent duplicate creation from repeated Save actions.
4. Repeat the affected unit/integration checks.
5. Repeat focused physical-device regression tests on the Galaxy A11.
6. Decide whether the Android 12 background clipboard limitation is accepted/documented or requires additional mitigation.

T080–T086 are considered complete because the required physical testing was executed and the observed failures/limitations are explicitly documented. T087 remains open until the release blockers are resolved.
