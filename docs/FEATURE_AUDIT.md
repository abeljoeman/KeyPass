# Feature Audit — Out-of-Scope Features vs. Retained Prototype Features

**Status:** Draft
**Created:** 2026-08-24
**Task:** TASKS.md T020
**Inputs:** `PRD.md`, `TSD.md`, `ENGINEERING_PRINCIPLES.md`, `docs/adr/`

## 1. Purpose and Method

This document identifies features inherited from the KeyPass base application that fall
outside the `PRD.md` prototype scope. It is an **audit only**. It does not remove, disable,
refactor, or modify any source code, Gradle dependency, manifest, resource, navigation, or
build file.

Findings are based on repository evidence (source files, manifest, Gradle files, resources)
searched on branch `prototype/v0.1`. Where a conclusion could not be fully verified from the
repository, it is explicitly marked **"needs verification"**.

## 2. Summary

| Feature | In code? | Recommended action | Primary PRD conflict |
|---|---|---|---|
| TOTP / authenticator | Yes | REMOVE LATER (T021) | PRD §3, §6 |
| Passkeys | No (docs only) | N/A — nothing to remove (T022) | PRD §3 |
| Autofill Service | Yes | DISABLE / REMOVE LATER (T025) | PRD §3 |
| Backup / restore | Yes | REMOVE LATER (T024) | PRD §3, ADR-0004 |
| Import / export (CSV) | Yes | REMOVE LATER (T024) | PRD §3 |
| Cloud / network behavior | No | N/A — already local-first (ADR-0004) | PRD §3 |
| WebDAV / remote storage | No | N/A — not present | PRD §3 |
| Credit-card / non-credential storage | No | N/A — not present | PRD §3 |
| Multiple-vault functionality | No | N/A — not present | PRD §3 |
| Analytics / telemetry | No | N/A — not present | PRD §3, FR-010 |

## 3. Out-of-Scope Features

### 3.1 TOTP / Authenticator

1. **Feature name:** TOTP / one-time-password authenticator.
2. **Implementation status:** Implemented and reachable in the UI. TOTP secrets are stored in
   `AccountModel.secret`, live codes are rendered in the credential list, and the detail page
   can scan `otpauth://` URIs.
3. **User-facing entry points / navigation routes:**
   - Credential detail (`AccountDetailState`) → "Secret Key" field → QR scan (`ScannerType.Secret`).
   - Home credential list: live countdown ring and rolling OTP when `secret != null`
     (`ui/home/components/AccountsList.kt`).
   - Legacy `AccountType.TOTP` constant is `@Deprecated` and normalized to `DEFAULT` during
     restore (`common/.../dbhelper/DbBackupRestore.kt`).
4. **Important source files / packages:**
   - `common/src/main/java/com/yogeshpaliyal/common/utils/TOTPHelper.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/utils/TokenCalculator.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/constants/ScannerType.kt` (`Secret = 2`)
   - `common/src/main/java/com/yogeshpaliyal/common/constants/AccountType.kt` (`TOTP = 2`, deprecated)
   - `common/src/main/java/com/yogeshpaliyal/common/data/AccountModel.kt` (`secret`, `getOtp()`, `getTOtpProgress()`)
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/detail/AccountDetailPage.kt` (scans `otpauth://`)
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/detail/QRScanner.kt`
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/detail/components/Fields.kt` (Secret Key input)
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/home/components/AccountsList.kt` (OTP display/copy)
5. **Android services / receivers / providers / manifest entries:**
   - `CAMERA` permission (`app/src/main/AndroidManifest.xml`) — used by the QR scanner
     (`com.journeyapps.barcodescanner.CaptureActivity`).
   - No dedicated service; TOTP is UI + utility code.
6. **Gradle / library dependencies used primarily by this feature:**
   - `com.journeyapps:zxing-android-embedded:4.3.0` (QR scan) — shared with QR export, see 7.
   - `commons-codec:commons-codec:1.18.0` (Base32/Hex used by `TOTPHelper` / `TokenCalculator`).
7. **Shared dependencies with retained prototype features:**
   - `com.journeyapps:zxing-android-embedded` is also used by the QR-code export on the detail
     page (`DetailViewModel.generateQrCode`) and the password QR scan
     (`ScannerType.Password`). Removing TOTP does **not** automatically make zxing removable if
     QR export/scan for passwords is retained.
   - `AccountModel` / Room schema: `secret` column and DB migration `DB_VERSION_6 → DB_VERSION_7`
     exist for TOTP. Schema cleanup affects retained CRUD persistence.
8. **Recommended action:** REMOVE LATER (maps to TASKS.md T021).
9. **Risk of removal / likely coupling:** Medium. TOTP logic is embedded in the shared
   `AccountModel`, the Room schema/migrations, the detail form, and the home list. Removal
   requires coordinated edits across `common` and `app` plus a schema migration decision.
   `commons-codec` is primarily owned by TOTP (`TOTPHelper`, `TokenCalculator`) and `Tools`
   (Hex). It is a **CANDIDATE FOR REMOVAL AFTER** the owning feature (TOTP, T021) is removed —
   not unconditionally removable. Require repository-wide usage verification, a successful
   build, and relevant tests after removal (see §5.1).

### 3.2 Passkeys

1. **Feature name:** Passkeys / FIDO credential management.
2. **Implementation status:** **Not present in code.** Only occurrences are in
   `PRD.md`, `ENGINEERING_PRINCIPLES.md`, and `TASKS.md` (non-goal / removal task).
   No `passkey`/`fido` symbols exist in Kotlin or manifest sources.
3. **User-facing entry points:** None.
4. **Important source files:** None found.
5. **Manifest entries:** None found.
6. **Dependencies:** None found.
7. **Shared dependencies:** N/A.
8. **Recommended action:** N/A — nothing to remove; T022 can be closed after verification on a
   fresh checkout.
9. **Risk of removal:** None. Flag "needs verification" for completeness if a stale branch or
   resource contains references.

### 3.3 Autofill Service

1. **Feature name:** Android AutoFill Service (`KeyPassAutofillService`).
2. **Implementation status:** Implemented and declared in the manifest. The service reads the
   credential database to fill and save fields in other apps (API 26+).
3. **User-facing entry points / navigation routes:**
   - Settings → "AutoFill Service" (`PreferenceType.AUTO_FILL` in `MySettingsFragment.kt`),
     enables/disables via `common/.../utils/GetAutoFillService.kt`
     (`isAutoFillServiceEnabled`, `enableAutoFillService`).
4. **Important source files / packages:**
   - `app/src/main/java/com/yogeshpaliyal/keypass/autofill/` (whole package)
     - `MyAutofillService.kt` (declares `KeyPassAutofillService`)
     - `AutofillHelper.kt`, `AutofillFieldMetadata.kt`, `AutofillFieldMetadataCollection.kt`,
       `CommonUtil.kt`, `PackageVerifier.kt`, `StructureParser.kt`
     - `datasource/AutofillRepository.kt`, `datasource/SharedPrefsAutofillRepository.kt`
     - `model/FilledAutofillField.kt`, `model/FilledAutofillFieldCollection.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/utils/GetAutoFillService.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/db/DbDao.kt`
     (`getAllAccountsListByPackageName`, used only by autofill)
5. **Android services / receivers / providers / manifest entries:**
   - `app/src/main/AndroidManifest.xml`:
     `<service android:name=".autofill.KeyPassAutofillService"
     android:permission="android.permission.BIND_AUTOFILL_SERVICE" ...>` with
     `android.service.autofill.AutofillService` intent-filter and
     `<meta-data android:name="android.autofill" android:resource="@xml/autofill_service" />`.
   - `app/src/main/res/xml/autofill_service.xml` (empty placeholder).
6. **Gradle / library dependencies used primarily by this feature:** None third-party;
   platform `android.service.autofill` / `android.view.autofill` APIs (API 26+).
7. **Shared dependencies with retained prototype features:**
   - `AppDatabase` / `DbDao` (writes and reads `AccountModel`; saved autofill fields become
     credential rows).
   - `gson` is used by `SharedPrefsAutofillRepository` (also used by backup and QR export).
8. **Recommended action:** DISABLE now / REMOVE LATER (maps to TASKS.md T025).
9. **Risk of removal / likely coupling:** Low–Medium. The manifest service and `autofill`
   package are self-contained; coupling points are the Settings entry and the
   `getAllAccountsListByPackageName` DAO method (adds no value to retained features).
   `SharedPrefsAutofillRepository` stores data in SharedPreferences for a package keyed by
   app package — removing the service does not touch retained screens.

### 3.4 Backup / Restore (KeyPass `.keypass` encrypted backup)

1. **Feature name:** Encrypted credentials backup and restore (manual + auto via WorkManager).
2. **Implementation status:** Implemented and reachable. Backups are AES-CBC encrypted JSON
   (custom `EncryptionHelper`) written to a user-selected SAF directory; auto-backup is a
   WorkManager worker triggered on credential mutations.
3. **User-facing entry points / navigation routes:**
   - Settings → "Credentials Backups" (`BackupScreenState`) — turn on/off, choose folder,
     create backup, set keyphrase.
   - Settings → "Import Credentials" → "KeyPass Backup" (`RestoreKeyPassBackupState`).
   - Home: "Validate Keyphrase" dialog (`ValidateKeyPhrase` / `ForgotKeyPhraseState`) shown
     after 7 days without keyphrase entry.
4. **Important source files / packages:**
   - `common/src/main/java/com/yogeshpaliyal/common/dbhelper/DbBackupRestore.kt`
     (`createBackup`, `restoreBackup`)
   - `common/src/main/java/com/yogeshpaliyal/common/dbhelper/EncryptionHelper.kt`
     (custom AES/CBC/PKCS5Padding with zero IV — violates Principle II, must not be reused)
   - `common/src/main/java/com/yogeshpaliyal/common/utils/BackupUtils.kt`
     (`backupAccounts`, `canUserAccessBackupDirectory`, `getRandomString`)
   - `common/src/main/java/com/yogeshpaliyal/common/data/BackupData.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/worker/AutoBackupWorker.kt`,
     `ExecuteAutoBackup.kt`
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/backup/` (whole package, incl.
     `BackupScreen.kt`, `KeyPassBackupDirectoryPick.kt`, `components/*`)
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/dialogs/RestoreKeyPassBackupDialog.kt`
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/home/components/ForgotKeyPhraseDialog.kt`
   - Settings / keyphrase state in `ui/redux/states/*` (`BackupScreenState`, `ValidateKeyPhrase`,
     `ForgotKeyPhraseState`, `ShowKeyphrase`, `SelectKeyphraseType`, `CustomKeyphrase`).
5. **Android services / receivers / providers / manifest entries:**
   - WorkManager-backed `AutoBackupWorker` (no manifest `service`; registered via
     `Configuration.Provider` in `CommonMyApplication` with `HiltWorkerFactory`).
   - `androidx.startup.InitializationProvider` is disabled in both manifests (not backup-specific).
   - No backup-specific manifest entries.
6. **Gradle / library dependencies used primarily by this feature:**
   - `androidx.documentfile:documentfile:1.1.0` (SAF `DocumentFile`).
   - `androidx.work:work-runtime-ktx:2.8.1` + `androidx.hilt:hilt-work:1.2.0`
     (auto-backup worker).
   - `com.google.code.gson:gson:2.13.2` (`BackupData` serialization).
   - `commons-codec` is **not** used here (key generation uses `SecureRandom` in
     `BackupUtils.getRandomString`); TOTP is the `commons-codec` consumer.
   - `androidx.documentfile:documentfile`, `androidx.work:work-runtime-ktx`, and
     `androidx.hilt:hilt-work` are owned by this feature and are **CANDIDATE FOR REMOVAL
     AFTER backup/restore is removed** (see §5.1).
7. **Shared dependencies with retained prototype features:**
   - WorkManager/Hilt worker plumbing (`CommonMyApplication.getWorkManagerConfiguration`,
     `HiltWorkerFactory`) currently exists **only** for `AutoBackupWorker`. `hilt-work` /
     `work-runtime-ktx` are candidates for removal only after backup is removed and
     repository-wide usage is verified.
   - `gson` is shared with autofill repository and `DetailViewModel.generateQrCode`.
   - `documentfile` is used only by backup; candidate for removal after backup is removed.
   - Write path coupling: `DetailViewModel.insertOrUpdate` / `deleteAccount` call
     `executeAutoBackup()` after every mutation; Homepage triggers the keyphrase dialog.
8. **Recommended action:** REMOVE LATER (maps to TASKS.md T024).
9. **Risk of removal / likely coupling:** **High.** Backup is woven into every credential
   mutation (auto-backup trigger), the Home screen (keyphrase reminders), Settings, and the
   shared `UserSettings`/DataStore keys (`backupKey`, `backupDirectory`, `backupTime`,
   `autoBackupEnable`, `overrideAutoBackup`). Removal must be coordinated to avoid leaving
   dead settings and worker code; the custom `EncryptionHelper` must not be reused under
   Principle II.

### 3.5 Import / Export (CSV: Chrome/Google, KeePass)

1. **Feature name:** CSV credential import (Chrome/Google password export, KeePass CSV export)
   and KeyPass backup import.
2. **Implementation status:** Implemented. `BackupImporter` offers three import options;
   Chrome and KeePass imports parse CSV; KeyPass backup import is part of the backup feature.
3. **User-facing entry points / navigation routes:**
   - Settings → "Import Credentials" (`BackupImporterState`) → choose Google/Chrome CSV,
     KeePass CSV, or KeyPass Backup.
   - Dialogs: `RestoreChromeBackupDialog`, `RestoreKeePassBackupDialog`,
     `RestoreKeyPassBackupDialog`.
4. **Important source files / packages:**
   - `app/src/main/java/com/yogeshpaliyal/keypass/importer/`
     (`AccountsImporter`, `ChromeAccountImporter`, `KeePassAccountImporter`,
     `KeyPassAccountImporter`)
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/backupsImport/BackupImporter.kt`
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/dialogs/RestoreChromeBackupDialog.kt`,
     `RestoreKeePassBackupDialog.kt`
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/settings/OpenKeyPassBackup.kt`
5. **Android services / receivers / providers / manifest entries:** None (SAF
   `ACTION_OPEN_DOCUMENT` via `ActivityResultContracts.OpenDocument`).
6. **Gradle / library dependencies used primarily by this feature:**
   - `com.opencsv:opencsv:5.11` (CSV parsing in the two restore dialogs).
7. **Shared dependencies with retained prototype features:**
   - `DashboardViewModel.restoreBackup` → `AppDatabase.saveToDb` (import writes credential
     rows); same path retained by CRUD.
   - `gson` used by `DbBackupRestore` / restore dialogs.
8. **Recommended action:** REMOVE LATER (maps to TASKS.md T024).
9. **Risk of removal / likely coupling:** Medium. Import dialogs write directly into the same
   `AppDatabase` used by retained CRUD, but the importers/dialogs themselves are leaf nodes.
    `opencsv` is a **CANDIDATE FOR REMOVAL AFTER** both CSV dialogs are gone (import/export
    removal) — not unconditionally removable. Require repository-wide usage verification, a
    successful build, and relevant tests after removal (see §5.1). The KeyPass-backup importer
    should be removed together with the backup feature (3.4).

### 3.6 Cloud / Network-Dependent Behavior

1. **Feature name:** Any network-dependent behavior.
2. **Implementation status:** **Not present.** The manifest removes
   `android.permission.ACCESS_NETWORK_STATE` (`tools:node="remove"`) and never declares
   `android.permission.INTERNET`. No OkHttp/Retrofit/HttpURLConnection usage exists. The only
   `URL` usage is `TOTPHelper` parsing `otpauth://` URI text (no connection).
3. **User-facing entry points:** None for network features.
4. **Important source files:** None.
5. **Manifest entries:** `ACCESS_NETWORK_STATE` removed at merge time.
6. **Dependencies:** None found. **"needs verification":** confirm no transitive dependency
   (e.g., `androidx.profileinstaller:profileinstaller`, `androidx.activity`) introduces an
   implicit network requirement; no permission is declared regardless.
7. **Shared dependencies:** N/A.
8. **Recommended action:** KEEP (already conforms to PRD FR-001 / FR-010 and ADR-0004).
9. **Risk:** N/A — nothing to remove.

### 3.7 WebDAV or Remote Storage

1. **Feature name:** WebDAV / remote vault storage.
2. **Implementation status:** **Not present.** Backups are written only to local SAF
   directories via `DocumentFile`; there is no network storage integration.
3. **User-facing entry points:** None.
4. **Important source files:** None.
5. **Manifest entries:** None.
6. **Dependencies:** None found.
7. **Shared dependencies:** N/A.
8. **Recommended action:** N/A — not present; do not add under Principle III/IV.
9. **Risk:** None.

### 3.8 Credit-Card or Non-Credential Storage

1. **Feature name:** Credit-card / non-credential item storage.
2. **Implementation status:** **Not present.** The single data entity is `AccountModel`
   (credentials with title, username, password, secret, site, notes, tags). The only "card"
   matches are Compose `Card` UI components.
3. **User-facing entry points:** None.
4. **Important source files:** `common/.../data/AccountModel.kt`.
5. **Manifest entries:** None.
6. **Dependencies:** None.
7. **Shared dependencies:** N/A.
8. **Recommended action:** N/A — not present.
9. **Risk:** None.

### 3.9 Multiple-Vault Functionality

1. **Feature name:** Multiple-vault support.
2. **Implementation status:** **Not present.** Persistence is a single Room database
   (`AppDatabase`); there is no vault abstraction, vault selection, or multi-vault UI.
3. **User-facing entry points:** None.
4. **Important source files:** `common/.../AppDatabase.kt`; TSD §5/§7 (KDBX vault is planned
   later, not implemented).
5. **Manifest entries:** None.
6. **Dependencies:** None (Kotpass/KDBX is not yet added; that is Phase 3 / ADR-0003).
7. **Shared dependencies:** N/A.
8. **Recommended action:** N/A — not present.
9. **Risk:** None. Note for later phases: the Room DB is the current persistence layer and will
   be superseded/adapted by the KDBX repository (TSD §5.2), so scope-reduction work must not
   remove the Room CRUD used by retained screens.

### 3.10 Analytics / Telemetry

1. **Feature name:** Analytics, telemetry, crash reporting to third parties.
2. **Implementation status:** **Not present.** No Firebase, Crashlytics, Mixpanel, Amplitude,
   or similar. The only app-level crash behavior is a local `CrashActivity` (`MyApplication`,
   `CommonMyApplication` default uncaught-exception handler) with no network sink.
3. **User-facing entry points:** None.
4. **Important source files:** `app/.../MyApplication.kt`, `app/.../ui/CrashActivity.kt`,
   `common/.../CommonMyApplication.kt`.
5. **Manifest entries:** None.
6. **Dependencies:** None found.
7. **Shared dependencies:** N/A.
8. **Recommended action:** KEEP the local crash screen (retained); do not add analytics
   (Principle III / PRD §3).
9. **Risk:** None.

## 4. Retained Prototype Features to Avoid Breaking

These are in-scope or reusable pieces from `PRD.md` / `TSD.md`. Scope-reduction tasks must
preserve them:

| Retained area | Primary files |
|---|---|
| Onboarding / master-password UI | `app/.../ui/auth/AuthScreen.kt`, `ui/auth/components/ButtonBar.kt`, `ui/auth/components/PasswordInputField.kt`, `ui/changePassword/ChangePassword.kt`, `ui/passwordHint/PasswordHintScreen.kt`, `ui/changeDefaultPasswordLength/*` |
| Credential list / detail / add / edit UI | `app/.../ui/home/Homepage.kt`, `ui/home/components/AccountsList.kt`, `ui/detail/AccountDetailPage.kt`, `ui/detail/DetailViewModel.kt`, `ui/detail/components/*`, `ui/commonComponents/*` |
| Search | `ui/home/components/SearchBar.kt`, `Homepage.kt`, `common/.../db/DbDao.kt` (`getAllAccountsAscending/Descending`) |
| Password generator | `app/.../ui/generate/*`, `common/.../utils/PasswordGenerator.kt`, `common/.../data/PasswordConfig.kt` |
| Biometric unlock UI (reusable later) | `app/.../ui/auth/components/BiometricPrompt.kt`, `androidx.biometric:biometric:1.1.0`, `UserSettings.isBiometricEnable` / `biometricLoginTimeoutEnable` |
| FLAG_SECURE / screenshot protection | `app/.../ui/nav/DashboardComposeActivity.kt` (sets `FLAG_SECURE` for non-debug) |
| Theme / navigation infrastructure | `app/.../ui/style/KeyPassTheme.kt`, `ui/nav/*` (`DashboardComposeActivity`, `BottomNavViewModel`, `KeyPassBottomBar`, `DashboardBottomSheet`, `NavigationModel*`), `ui/redux/*` (store, states, actions, middlewares) |
| App shell / lifecycle / DI | `app/.../MyApplication.kt`, `common/.../CommonMyApplication.kt`, `common/.../di/module/AppModule.kt`, `common/.../AppDatabase.kt`, `common/.../db/DbDao.kt`, `common/.../data/AccountModel.kt`, `common/.../data/UserSettings.kt`, `common/.../utils/SharedPreferenceUtils.kt` |
| Clipboard copy | `ui/redux/middlewares/UtilityMiddleware.kt` (`CopyToClipboard`), retained by US7 |

## 5. Additional Observations

- **Legacy custom crypto is for removal, not reuse (Principle II):** `EncryptionHelper.kt`
  implements custom AES/CBC/PKCS5Padding with a hard-coded zero IV, and the surrounding
  backup-encryption code (`DbBackupRestore`, `BackupUtils`) is legacy KeyPass crypto. These
  are intended for **removal when the related backup/restore functionality is removed (T024)**.
  They must **not** be reused, adapted, or generalized for the prototype, and no replacement
  crypto is implemented by this audit task.
- **Room DB vs. KDBX:** The current app persists credentials in a SQLCipher-encrypted Room DB,
  not KDBX. KDBX/Kotpass integration is planned (ADR-0003, TSD §5, Phase 3). Scope reduction
  (T020–T027) should keep the Room CRUD path intact until the KDBX repository replaces it.
  Final persisted-vault cryptography relies on **Kotpass/KDBX**, not KeyPass legacy crypto.
- **`androidx.security:security-crypto:1.1.0-alpha07`** — classified as **CANDIDATE FOR
  REMOVAL / NEEDS VERIFICATION**. The prototype does not intend to retain KeyPass legacy
  crypto, so this dependency is not permanently retained. It is not removed during T020
  (audit-only). It may be removed later only after (a) repository-wide usage is verified,
  (b) the legacy feature/code that owns or depended on it is removed, and (c) the project
  successfully builds and relevant tests pass without it.
- **`androidx.profileinstaller:profileinstaller:1.4.1`** and the `baselineprofile` project are
  build-performance infrastructure: `app/build.gradle.kts` applies the
  `androidx.baselineprofile` plugin, the app depends on `androidx.profileinstaller`, and the
  app references the `:baselineprofile` project. Classified as **KEEP FOR NOW /
  BUILD-PERFORMANCE INFRASTRUCTURE** — not unused and not safely removable. It may only be
  reconsidered if baseline-profile infrastructure is intentionally removed in a separate task.
- **Flavors:** `free`/`pro` differ only in app label (`KeyPass Pro`); no feature gating.
- **`shared` module** is a KMP stub (`GetPlatformName`, `App`); `desktop` is commented out of
  `settings.gradle.kts`. Neither participates in the Android prototype features.

### 5.1 Dependency-Removal Conclusions

All dependency-removal claims in this document are **conditional**. A dependency may be
removed only after (a) repository-wide usage is verified, (b) the owning feature/code is
removed, and (c) the project successfully builds and relevant tests pass without it.

- **Feature-specific — CANDIDATE FOR REMOVAL AFTER OWNING FEATURES ARE REMOVED:**
  - `commons-codec` — owned by TOTP (T021).
  - `com.opencsv:opencsv` — owned by CSV import/export (T024).
  - `androidx.documentfile:documentfile` — owned by backup/restore (T024).
  - `androidx.work:work-runtime-ktx` and `androidx.hilt:hilt-work` — owned by auto-backup
    (T024).
- **Build-performance infrastructure — KEEP FOR NOW:** `androidx.profileinstaller` and the
  `baselineprofile` module/plugin. Not unused, not safely removable in scope reduction.
- **Legacy custom crypto — REMOVE WITH OWNING FEATURE:** `EncryptionHelper` and the
  backup-encryption code are removed with backup/restore (T024); never reused or adapted.
- **Legacy crypto dependency — CANDIDATE FOR REMOVAL / NEEDS VERIFICATION:**
  `androidx.security:security-crypto`.

## 6. Suggested Follow-Up Mapping

| TASKS.md task | Feature(s) covered | Notes |
|---|---|---|
| T021 | TOTP (3.1) | Coordinate schema (`secret`), `AccountModel`, detail form, home list, zxing/commons-codec |
| T022 | Passkeys (3.2) | Verify no hidden references on clean checkout; likely close as "not present" |
| T023 | Cloud/network (3.6, 3.7) | Confirm no permission reintroduced; verify transitive deps |
| T024 | Backup/restore (3.4) + import/export (3.5) | Remove `EncryptionHelper`, workers, dialogs, settings entries, DataStore keys together |
| T025 | Autofill (3.3) | Remove manifest service, `autofill/` package, Settings entry, DAO method |
| T026 | Dependency cleanup | Re-evaluate after T021–T025 per §5.1: `opencsv`, `commons-codec`, `documentfile`, `work-runtime-ktx`, `hilt-work`, `security-crypto` — candidates only after owning features are removed; each needs usage verification, a successful build, and passing tests |
| T027 | Build + launch verification | Required after any of the above |
