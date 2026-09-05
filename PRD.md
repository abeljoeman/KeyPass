# Product Requirements Document — Android Password Manager Prototype

**Status:** Approved prototype baseline; UI/UX refinement planned
**Created:** 2026-08-24  
**Updated:** 2026-09-05
**Target:** Android prototype  
**Primary goal:** Validate the core password-manager experience with the smallest reasonable implementation and without creating security-critical infrastructure from scratch.

## 1. Problem

Users need a simple Android application that can store credentials locally and retrieve them when needed without requiring an account, backend, or cloud synchronization.

The prototype must prove that the core UX is useful and that an existing encrypted vault format can be integrated cleanly into an Android-native application.

## 2. Prototype Goal

Build a functional Android prototype that allows a user to:

1. Create the app-private encrypted vault on first launch when no vault exists.
2. Unlock the vault using a master password.
3. View stored credentials.
4. Add, edit, and delete credentials.
5. Search credentials.
6. Generate passwords.
7. Copy usernames or passwords when needed.
8. Lock the vault again.

The prototype is successful if this end-to-end flow works reliably on a physical Android device.

## 3. Non-Goals

The following are explicitly out of scope for the prototype unless separately approved:

- User accounts
- Backend services
- Cloud sync
- Team/shared vaults
- Browser extensions
- TOTP / authenticator features
- Passkeys
- Credit-card storage
- Autofill service
- Multi-device sync
- Analytics or telemetry
- Advertising
- Custom cryptographic algorithms or custom encrypted-vault formats
- External vault picker/import/export flows
- Biometric unlock expansion during the next UI/UX refinement
- OCR/camera capture
- Theme selector or light theme
- Rebranding implementation before the Brand Decision Gate

## 4. Product Principles

- Local-first.
- Reuse before build.
- No custom cryptography when a mature format/library can be reused.
- Prototype scope must remain narrow.
- Security-sensitive shortcuts are not acceptable even for the prototype.
- No network dependency is required for the core prototype.

## 5. User Stories

### US1 — Create or Unlock Local Vault (P1)

As a user, I want the app to create my encrypted local vault on first launch and unlock that same vault on later launches.

**Acceptance scenarios**

1. Given the app-private `vault.kdbx` does not exist, when the app launches, then the user enters the create-master-password flow.
2. Given the app-private `vault.kdbx` exists, when the app launches, then the user enters the unlock flow.
3. Given an existing vault, when the correct master password is entered, then the vault opens.
4. Given an existing vault, when an incorrect master password is entered, then access is denied and vault contents are not exposed.
5. The current prototype does not offer an external KDBX picker or an "Open Existing Vault" choice.

### US2 — View Credentials (P1)

As a user, I want to see my saved credentials after unlocking the vault.

**Acceptance scenarios**

1. Given an unlocked vault with entries, when the vault screen opens, then credential entries are listed.
2. Given a locked vault, when the user attempts to access credentials, then the app requires unlocking first.

### US3 — Add Credential (P1)

As a user, I want to add a credential to my vault.

A credential contains:

- Title
- Username
- Password
- URL (optional)
- Notes (optional)

**Acceptance scenarios**

1. Given an unlocked vault, when valid credential data is saved, then the new entry appears in the vault.
2. After the app is restarted and the vault is reopened, the entry still exists.

### US4 — Edit and Delete Credential (P1)

As a user, I want to update or remove credentials.

**Acceptance scenarios**

1. Edited values persist after vault reopen.
2. Deleted entries no longer appear after vault reopen.
3. Canceling an edit does not modify the stored entry.

### US5 — Search Credentials (P2)

As a user, I want to search by title or username.

**Acceptance scenarios**

1. Search returns relevant matching entries.
2. Clearing search restores the normal credential list.

### US6 — Generate Password (P2)

As a user, I want to generate a strong password when creating or editing a credential.

**Acceptance scenarios**

1. The generator creates a password using user-selected length and character categories.
2. Generated passwords can be inserted into the credential form.

### US7 — Copy Credential Fields (P2)

As a user, I want to copy username or password values.

**Acceptance scenarios**

1. Username can be copied.
2. Password can be copied.
3. Password is not printed to application logs.

### US8 — Lock Vault (P1)

As a user, I want the vault to lock when I leave it or explicitly lock it.

**Acceptance scenarios**

1. Manual lock removes access to credential screens.
2. After the configured inactivity/background condition, reopening credential screens requires unlock.

## 6. Functional Requirements

- **FR-001:** The application MUST work without a backend.
- **FR-002:** Credential storage MUST use an encrypted vault format supplied by an established library.
- **FR-003:** The application MUST NOT persist the master password in plaintext.
- **FR-004:** The application MUST support credential create/read/update/delete operations.
- **FR-005:** The application MUST support credential search.
- **FR-006:** The application MUST provide password generation.
- **FR-007:** The application MUST provide manual vault locking.
- **FR-008:** Sensitive values MUST NOT be intentionally written to logs.
- **FR-009:** Sensitive screens SHOULD prevent screenshots/screen recording where supported.
- **FR-010:** The prototype SHOULD run without Android INTERNET permission unless a dependency makes it strictly necessary and the exception is documented.
- **FR-011:** The prototype MUST fail closed when vault decryption/opening fails.
- **FR-012:** The prototype MUST use the app-private `vault.kdbx` as its current single persisted vault and MUST NOT silently create/overwrite it after an open/decode failure.
- **FR-013:** The next UI/UX refinement MUST preserve the current architecture and feature scope unless a separate requirement explicitly changes them.
- **FR-014:** The next UI/UX refinement MUST follow `docs/UX_UI_BASELINE.md`.
- **FR-015:** The product MUST NOT proceed from UX validation into public/store release preparation until the Brand Decision Gate is completed.

## 7. Key Entities

### Vault

Represents the current single app-private encrypted credential store (`vault.kdbx`).

### Credential

Core fields:

- id
- title
- username
- password
- url
- notes

The exact encrypted representation is delegated to the vault library and format.

## 8. Edge Cases

The prototype must define behavior for:

- Wrong master password
- Empty vault
- Duplicate titles
- Very long username/password/notes
- App backgrounded while vault is unlocked
- Android process killed while vault is unlocked
- Vault file missing
- Vault file corrupted
- Storage write failure
- User cancels credential edit
- User cancels vault creation

## 9. Success Criteria

Prototype is considered successful when:

- A new vault can be created on a physical Android device.
- The same vault can be reopened after app restart.
- At least 20 credentials can be created and searched without functional issues.
- CRUD operations persist correctly.
- Incorrect master password does not expose vault contents.
- No credential or master password appears in normal application logs during the defined test flow.
- The app can be demonstrated end-to-end without any backend service.

## 10. Assumptions

- Prototype targets Android only.
- Development uses Kotlin and Jetpack Compose.
- Existing open-source Android password-manager code is reused where practical.
- Existing KDBX support is preferred over inventing a new encrypted database format.
- Physical-device testing is available.

## 11. Next UI/UX Refinement

The approved next-build UX/UI contract is `docs/UX_UI_BASELINE.md`.

The refinement is successful when:

- Approved flows are implemented without architecture expansion.
- Dark-only presentation is consistent from launch through operational screens.
- Loading/error feedback is clear on the physical test device.
- Core CRUD, search, generator, clipboard, and lock behavior remain regression-safe.
- Accessibility basics are validated, including touch targets, localized icon semantics, font scaling, and a basic TalkBack pass.

Working product name:

```text
KeyPass
```

Final public branding remains undecided. The team MUST stop at the Brand Decision Gate before public/store release preparation to decide the final name, trademark clearance, app icon/logo, final `applicationId`/package identity strategy, signing/store identity, and attribution presentation.
