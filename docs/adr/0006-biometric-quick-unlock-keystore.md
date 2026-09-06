# ADR 0006 — Biometric Quick Unlock Uses Android Keystore-Protected Wrapped Vault Secret

**Status:** Proposed — Phase 13 technical review  
**Date:** 2026-09-06

## Context

RAHSA needs optional biometric convenience unlock while preserving Master Password as the root credential. The inherited KeyPass biometric path is not acceptable because UI navigation success alone does not decrypt/open the KDBX vault.

RAHSA must not persist the Master Password in plaintext and must fall back to Master Password when biometric state is unavailable or invalid.

## Decision

1. Biometric Quick Unlock is opt-in and OFF by default.
2. Enabling requires explicit reauthentication with the current Master Password.
3. RAHSA generates a non-exportable symmetric key in Android Keystore whose use requires qualifying biometric authentication.
4. RAHSA encrypts/wraps the minimum KDBX unlock secret required for the current vault (currently the Master Password bytes) using an authenticated Android platform cipher such as AES-GCM.
5. Persist only wrapped ciphertext and required non-secret cipher metadata; never persist plaintext Master Password.
6. Unlock uses AndroidX `BiometricPrompt` with a `CryptoObject` and `BIOMETRIC_STRONG`; `DEVICE_CREDENTIAL` is not an allowed fallback for this feature.
7. Biometric success decrypts the wrapped secret in memory and invokes the normal vault-open repository path. Navigation to unlocked UI occurs only after that open succeeds.
8. Missing/invalidated Keystore key, biometric enrollment/security-state change, unwrap failure, or inconsistent state disables quick unlock and falls back to Master Password.
9. Master Password change, restored vault, destructive reset, reinstall, or new device invalidates/removes biometric quick-unlock state.
10. Biometric state is local device state and is never included in KDBX backup.

## Consequences

### Positive

- Uses Android platform security primitives rather than custom authentication cryptography.
- Keeps KDBX as the vault source of truth and uses the same repository open path as password unlock.
- Meets the product rule that biometric is convenience, not recovery.
- Supports deterministic invalidation/fallback behavior.

### Costs / limitations

- Availability depends on Android API/device biometric and Keystore capabilities.
- Device/enrollment changes can intentionally invalidate convenience unlock.
- Some API-level details require guarded configuration and physical-device testing.
- RAHSA still needs the Master Password for recovery/re-enable; biometric cannot recover a forgotten password.

## Rejected alternatives

- Store plaintext Master Password — prohibited.
- Biometric success navigates directly to Home — does not open the vault and violates the security boundary.
- Device PIN/pattern/password fallback inside quick unlock — not approved product behavior.
- Custom cryptographic key wrapping format/library — unnecessary when Android Keystore/CryptoObject is available.
- Store only a boolean `biometricEnabled` flag — insufficient to unlock KDBX securely.
