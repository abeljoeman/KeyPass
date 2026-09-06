# ADR 0006 — Biometric Quick Unlock Uses Android Keystore-Protected Wrapped Vault Secret

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA needs optional biometric convenience unlock while preserving Master Password as the root credential. The inherited KeyPass biometric path is not acceptable because UI navigation success alone does not decrypt/open the KDBX vault.

RAHSA must not persist the Master Password in plaintext and must fall back to Master Password when biometric state is unavailable or invalid. Enable/disable lifecycle must fail closed across cancellation, backgrounding, process death, force-close, and partial cleanup without introducing a biometric-specific recovery marker.

## Decision

1. Biometric Quick Unlock is opt-in and OFF by default.
2. Enabling requires explicit reauthentication with the current Master Password against the active KDBX.
3. RAHSA verifies device biometric capability/enrollment before attempting enablement.
4. RAHSA generates a non-exportable symmetric key in Android Keystore whose use requires qualifying biometric authentication.
5. RAHSA encrypts/wraps the minimum KDBX unlock secret required for the current vault (currently the Master Password bytes) using an authenticated Android platform cipher such as AES-GCM.
6. Persist only wrapped ciphertext and required non-secret cipher/state metadata; never persist plaintext Master Password.
7. Unlock uses AndroidX `BiometricPrompt` with a `CryptoObject` and `BIOMETRIC_STRONG`; `DEVICE_CREDENTIAL` is not an allowed fallback for this feature.
8. Biometric success decrypts the wrapped secret only in memory and invokes the normal vault-open repository path. Navigation to unlocked UI occurs only after the KDBX open succeeds.
9. Locked entry may auto-prompt biometric at most once per entry when enabled/valid. Cancellation must not loop the prompt; manual `Unlock with Biometrics` and `Use Master Password` actions remain available.
10. Enable is fail-closed: the persisted enabled state becomes true only after key creation, successful biometric authentication, secret wrapping, complete secure-state persistence, and consistency verification have succeeded.
11. If enablement is interrupted or fails before that final step, biometric remains OFF. Any orphan Keystore alias or incomplete wrapped state is treated as cleanup-only state and may be removed idempotently.
12. No biometric-specific transaction/recovery marker is introduced. Incomplete state is distinguished by the absence of a complete enabled state.
13. Missing/invalidated Keystore key, biometric enrollment/security-state change, unwrap failure, inconsistent secure state, or real vault-open failure keeps the vault locked, disables quick unlock where applicable, removes unusable wrapped state, and falls back to Master Password.
14. Disable requires confirmation but does not lock the already-unlocked session. Disable is fail-closed toward OFF: make quick unlock unusable, remove wrapped state, remove the Keystore alias, and leave the feature OFF. Cleanup is idempotent so interruption cannot safely resurrect a partially removed configuration.
15. Back/Home/process death while a biometric prompt is active never creates an unlocked session or an enabled state by itself.
16. Plaintext secret material obtained during reauthentication or unwrap is kept only transiently in memory and cleared as soon as practical.
17. Master Password change, restored vault, destructive reset, reinstall, or new device invalidates/removes biometric quick-unlock state.
18. Biometric state is local device state and is never included in KDBX backup.
19. The one-time discoverability card remains non-blocking; `Not Now` does not create recurring nagging.
20. Enable/disable operations are single-flight and reuse existing operation-guard patterns where adequate.

## Consequences

### Positive

- Uses Android platform security primitives rather than custom authentication cryptography.
- Keeps KDBX as the vault source of truth and uses the same repository open path as password unlock.
- Meets the product rule that biometric is convenience, not recovery.
- Enable/disable behavior converges to a safe OFF state after interruption instead of relying on a custom recovery transaction.
- Avoids a biometric-specific recovery marker or general transaction framework.
- Supports deterministic invalidation/fallback behavior.

### Costs / limitations

- Availability depends on Android API/device biometric and Keystore capabilities.
- Device/enrollment changes can intentionally invalidate convenience unlock.
- Some API-level details require guarded configuration and physical-device testing.
- RAHSA still needs the Master Password for recovery/re-enable; biometric cannot recover a forgotten password.
- Cleanup and state-consistency checks must be idempotent across process death and partial enable/disable operations.

## Rejected alternatives

- Store plaintext Master Password — prohibited.
- Biometric success navigates directly to Home — does not open the vault and violates the security boundary.
- Device PIN/pattern/password fallback inside quick unlock — not approved product behavior.
- Custom cryptographic key wrapping format/library — unnecessary when Android Keystore/CryptoObject is available.
- Store only a boolean `biometricEnabled` flag — insufficient to unlock KDBX securely.
- Set `enabled = true` before wrapped state is complete — creates unsafe partial state.
- Biometric-specific recovery marker/transaction journal — unnecessary when enablement can commit only after complete state and cleanup can be idempotent.
- Treat Back/Home/cancel as biometric success or preserve partial enablement — violates fail-closed behavior.
