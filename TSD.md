# Technical Design — Android Password Manager Prototype

**Status:** Draft  
**Created:** 2026-08-24  
**Related:** `PRD.md`

## 1. Summary

The prototype will be an Android-native application built with Kotlin and Jetpack Compose.

The implementation follows a reuse-first strategy:

- Reuse suitable application/UI patterns from **yogeshpaliyal/KeyPass** where practical.
- Use **keemobile/kotpass** as the KDBX read/write engine.
- Store credentials in a single local KDBX vault file.
- Avoid backend services and custom encrypted-storage formats.

## 2. Technical Context

**Language / JVM:** Kotlin, Java 17 compatibility  
**Target platform:** Android  
**UI:** Jetpack Compose  
**Build:** Gradle Wrapper  
**Vault engine:** Kotpass (KDBX)  
**Base/reference application:** KeyPass  
**Storage:** Local KDBX file  
**Backend:** None  
**Network dependency:** None required for prototype core flow  
**IDE/editor:** VS Code  
**Coding agent:** Codex and/or DeepSeek  
**Testing:** Kotlin/JUnit + Android instrumentation/device testing where needed

## 3. Architectural Principles

1. Reuse existing code and libraries before creating new components.
2. Keep security-sensitive custom code to a minimum.
3. No custom cryptographic primitive, key derivation scheme, or encrypted file format.
4. Keep application architecture simple enough to understand in one sitting.
5. Add abstractions only where they reduce coupling or improve testability.
6. Prototype code may be refactored later, but security behavior may not be knowingly weakened.

## 4. High-Level Architecture

```text
Jetpack Compose UI
        |
        v
ViewModel / UI State
        |
        v
VaultRepository
        |
        v
Kotpass
        |
        v
vault.kdbx
```

The repository boundary isolates UI code from KDBX-specific operations.

## 5. Main Components

### 5.1 UI Layer

Responsibilities:

- Unlock/create-vault screens
- Credential list
- Credential detail
- Add/edit credential
- Search
- Password generator
- Lock action
- Error states

Reuse existing KeyPass UI/components where they fit without importing unnecessary features.

### 5.2 VaultRepository

Responsibilities:

- Create vault
- Open vault
- Close/lock vault
- List entries
- Create entry
- Update entry
- Delete entry
- Search entries
- Save vault after mutations

The repository MUST NOT implement cryptographic algorithms itself.

### 5.3 Kotpass Adapter

Responsibilities:

- Convert application credential model to KDBX entries.
- Convert KDBX entries to application credential model.
- Decode/open KDBX using credentials supplied at runtime.
- Encode/write KDBX after changes.

Keep this adapter small and explicit.

### 5.4 Session / Lock State

For the prototype:

- Master password is entered when opening the vault.
- Master password is not persisted as plaintext.
- Decrypted vault/application models only exist during an unlocked session.
- Manual lock clears in-memory application access to decrypted entries.
- Background/timeout behavior should trigger a lock according to prototype settings.

Biometric key wrapping is NOT required for v0.1.

## 6. Data Model

Application-level model:

```kotlin
data class Credential(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val url: String?,
    val notes: String?
)
```

KDBX remains the source of truth for persisted credential data.

Do not create a second plaintext credential database for convenience.

## 7. Storage Design

Preferred prototype layout:

```text
app-private-storage/
└── vault.kdbx
```

Alternative user-selected document storage may be considered later.

Prototype default should minimize storage permissions and complexity.

## 8. Dependency Strategy

### Required / Preferred

- AndroidX / Jetpack Compose
- Kotlin coroutines where already used by the base app
- Kotpass for KDBX
- Existing dependencies already required by retained KeyPass code

### Avoid Unless Justified

- New DI frameworks
- New database engines
- New crypto libraries
- Networking libraries
- Firebase
- Analytics SDKs
- Background sync frameworks
- Rust / JNI / NDK

Any new dependency must state:

1. Problem solved.
2. Why platform/existing dependency cannot solve it.
3. License.
4. Security impact.

## 9. Reuse Strategy

### Reuse / Adapt from KeyPass

Candidates include:

- Compose theme and app shell
- Navigation structure
- Credential list/detail/form components
- Password generator
- Authentication/biometric UI components where useful
- Secure-screen behavior
- Settings patterns if needed

### Do Not Reuse Blindly

- Existing encryption helpers
- Existing backup encryption logic
- Features outside PRD scope
- Components that force unnecessary architectural complexity

Code must be reviewed before reuse.

## 10. Error Handling

Vault open/write failures must be explicit and fail closed.

Examples:

- Wrong password → show unlock error, do not expose data.
- Corrupted vault → show non-destructive error, do not overwrite automatically.
- Write failure → show save failure and preserve last known valid file.
- Missing vault → offer create/select flow.

No exception stack trace containing secret values may be intentionally logged.

## 11. Security Controls for Prototype

- KDBX-backed encrypted persistence.
- No plaintext master-password persistence.
- No intentional secret logging.
- Secure-screen flag on sensitive UI where supported.
- Manual lock.
- Background/timeout lock.
- Clipboard handling reviewed and tested.
- No backend.
- Prefer no INTERNET permission.
- Fail closed on vault-open failure.

See `docs/THREAT_MODEL.md`.

## 12. Testing Strategy

### Unit Tests

Prioritize:

- KDBX ↔ application model mapping
- Search
- Password generator
- Repository behavior using test vaults

### Device / Instrumentation Tests

Prioritize:

- Create/open vault
- Wrong-password handling
- App restart
- Lock/unlock
- Screenshot protection
- Clipboard behavior
- Process/background lifecycle

See `docs/TEST_PLAN.md`.

## 13. Prototype Project Structure

Exact paths may evolve based on retained KeyPass structure.

Target logical structure:

```text
app/
└── src/main/java/.../
    ├── ui/
    ├── vault/
    │   ├── VaultRepository.kt
    │   ├── KotpassVaultRepository.kt
    │   └── CredentialMapper.kt
    └── security/
        └── LockState.kt
```

Avoid reorganizing the entire inherited codebase unless necessary.

## 14. Build and Tooling

Baseline expectations:

- JDK 17
- Android SDK compatible with the retained KeyPass project
- Gradle Wrapper from repository
- VS Code
- Physical Android test device
- `adb`

Do not require Docker, backend databases, Node.js, Rust, or NDK for the prototype.

## 15. Open Questions

These do not block the first implementation tasks:

- Exact auto-lock timeout defaults
- Clipboard auto-clear timing
- Whether a user-selected external KDBX file is required before v0.2
- Whether biometric quick-unlock belongs in prototype v0.2
