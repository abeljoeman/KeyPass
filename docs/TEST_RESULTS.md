# Prototype Physical Test Results — v0.1

**Status:** Release regression complete; ready to tag `v0.1-prototype`
**Date:** 2026-09-05
**Test plan:** `docs/TEST_PLAN.md`

## Environment

- Device: Samsung Galaxy A11 (`SM-A115F`)
- Android: 12
- API level: 31
- Build type: `freeDebug`
- App package: `com.yogeshpaliyal.keypass.staging`
- Initial test checkpoint: `312035b` (`docs: record threat model review`)
- Final regression checkpoint: `f7fe324` (`fix: prevent duplicate credential saves`)
- Execution method: physical-device manual testing with `adb` inspection where appropriate

The initial physical test pass identified three release blockers. Each blocker was fixed in a separate commit, covered by targeted unit tests, and repeated on the physical device before the final smoke regression.

## Result Summary

| Test area | Result | Notes |
| --- | --- | --- |
| Build and installation | PASS | Clean build/install/launch, force-stop relaunch, and device-reboot launch verified. |
| Vault creation | PASS | Create, empty-password rejection, cancel, and restart persistence verified. |
| Vault unlock | PASS with UX limitation | Correct/wrong password behavior is fail-closed. Corrupted vault is non-destructive but is surfaced as an incorrect-password error. |
| Credential CRUD | PASS | 20+ credentials tested, including long values. Create/edit/cancel/delete/reopen/restart persistence verified. Rapid repeated Save no longer creates duplicates. |
| Search | PASS | Title, username, no-result, query-clear, and search/background regression behavior verified. |
| Password generator | PASS | Length/categories/use-in-editor verified; generated value was not observed in normal logs. |
| Lock behavior | PASS | Manual lock, background lock, process-kill recovery, and the prior lock/load race regression all pass without a fatal exception. |
| Clipboard | PASS with accepted platform limitation | Explicit copy and foreground 60-second cleanup pass. Background cleanup can fail on Android 12/API 31 and is accepted/documented for this prototype. |
| Screen privacy | PASS | ADB screenshot hides credentials and Recent Apps preview hides credential content. |
| Storage inspection | PASS | KDBX signature verified; no tested credential markers found in plaintext app storage or temporary files. |
| Logging inspection | PASS | Tested credential values, generated password, and decrypted payload markers were not observed in Logcat. |
| Network / permissions | PASS | Core flows work in airplane mode and `INTERNET` permission is absent. |
| Failure and recovery | PASS | Corrupt/decode failure remains non-destructive. Simulated storage-write failure now surfaces an error without crashing and preserves vault integrity. |

## Phase 8 Task Coverage

- **T080:** `docs/TEST_PLAN.md` executed on the physical device.
- **T081:** At least 20 credentials tested, including deliberately long field values.
- **T082:** Wrong master password rejected; repeated wrong attempts did not expose data.
- **T083:** App restart and vault reopen verified with persisted credential data.
- **T084:** Manual/background/foreground lock behavior verified.
- **T085:** Process kill returns to the unlock flow instead of decrypted credential state.
- **T086:** Known limitations and release results are captured below.
- **T087:** Ready to execute after this release-evidence update is committed and pushed.

## Resolved Release Blockers

### 1. Lock/load race crash — resolved

**Severity:** High  
**Fix commit:** `5eeffc7` (`fix: avoid credential load crash after vault lock`)  
**Status:** FIXED and physically regression-tested

The prior failure raised `java.lang.IllegalStateException: Vault is locked.` when a credential load raced with a lock transition. The fix gives the locked state a dedicated exception path and ignores that expected lifecycle race in the credential-loading ViewModel without swallowing unrelated repository failures.

Physical regression:

- auto-lock/reopen repeated 5 times: PASS;
- search + immediate background/reopen race: PASS;
- credentials intact after re-unlock: PASS;
- KeyPass fatal exception in Logcat: NO.

### 2. Storage-write failure crash — resolved

**Severity:** High  
**Fix commit:** `94d94ce` (`fix: handle vault write failures without crashing`)  
**Status:** FIXED and physically regression-tested

A controlled non-writable `files` directory previously caused an unhandled `java.io.IOException: Permission denied`. The save flow now reports a generic vault-write failure, keeps the editor/draft available, and does not invoke the success/navigation callback.

Physical regression:

- failed write does not crash: PASS;
- error message displayed: PASS;
- editor/draft preserved: PASS;
- `vault.kdbx` SHA-256 unchanged after failed write: PASS;
- retry succeeds after write permission is restored: PASS;
- successful retry persists after reopen: PASS;
- KeyPass fatal exception in Logcat: NO.

### 3. Double-tap Save duplicate creation — resolved

**Severity:** Medium  
**Fix commit:** `f7fe324` (`fix: prevent duplicate credential saves`)  
**Status:** FIXED and physically regression-tested

Save is now single-flight. The ViewModel rejects a second concurrent save request, and the UI disables the Save action while persistence is running.

Physical regression:

- rapid Save test 01: PASS;
- rapid Save test 02: PASS;
- rapid Save test 03: PASS;
- exactly one credential per create: PASS;
- KeyPass fatal exception in Logcat: NO.

## Final Smoke Regression

Final smoke regression was run on the Galaxy A11 from commit `f7fe324`.

- Launch/unlock: PASS.
- Create exactly once: PASS.
- Edit + persistence: PASS.
- Search: PASS.
- Password generator + save: PASS.
- Manual lock/re-unlock: PASS.
- Background lock/re-unlock: PASS.
- Search + background race: PASS.
- Force-stop recovery: PASS.
- Wrong password followed by correct password: PASS.
- Delete + persistence: PASS.
- KeyPass fatal exception in Logcat: NO.

The full `testFreeDebugUnitTest` suite also completed successfully before the final push of `f7fe324`.

## Accepted Security / Platform Limitation

### Clipboard auto-clear can fail while KeyPass is backgrounded on Android 12

**Severity:** Medium  
**Device:** Samsung Galaxy A11, Android 12 / API 31  
**Release decision:** Accepted and documented for `v0.1-prototype`

Observed:

- explicit username/password copy: PASS;
- foreground automatic cleanup after about 60 seconds: PASS;
- automatic cleanup after KeyPass is backgrounded: FAIL.

The implementation performs an ownership check before clearing. On Android versions that restrict clipboard reads when an app lacks focus, that ownership check may be unavailable, so cleanup is skipped rather than risking deletion of clipboard content written by another application.

Android 13+ provides platform clipboard expiration. No additional clipboard behavior change is being introduced immediately before the prototype release.

## Lower-Severity UX Findings

- **Credential title accepts newline/multiline input.** The value persists and displays on multiple lines in list/detail views, while the editor visually behaves like a single-line title field. The title should be explicitly single-line.
- **Corrupted vault error is ambiguous.** Corrupted KDBX data fails closed and is not overwritten, but the physical UI reports an incorrect-password-style error rather than distinguishing corruption/decode failure.
- **Very long credential title consumes substantial initial detail-screen height.** Remaining fields are accessible by scrolling.
- **List ordering observation:** one tested credential appeared at a different list position after reload. No data loss occurred and the vault hash did not change. Treat as an observation unless reproduced under a defined sort setting.

## Security-Preserving Findings

- Wrong master password does not open the vault.
- Repeated wrong-password attempts do not expose credential data.
- Corrupted-vault decode failure does not overwrite the source vault.
- Storage-write failure does not alter the existing KDBX file in the controlled regression test.
- Persisted vault starts with the expected KDBX signature `03 d9 a2 9a 67 fb 4b b5`.
- Tested credential markers were not found in plaintext app-private storage.
- Tested credential values and decrypted payload markers were not observed in normal Logcat inspection.
- Sensitive screenshots and Recent Apps previews were protected.
- Core prototype functionality works offline.
- The installed prototype does not request `android.permission.INTERNET`.

## Release Gate

All previously confirmed release blockers are resolved and physically regression-tested. The remaining Android 12 background clipboard behavior and lower-severity UX findings are explicitly accepted/documented prototype limitations.

The release evidence is ready for T087. After this documentation update is committed and pushed, tag the resulting commit as `v0.1-prototype`.
