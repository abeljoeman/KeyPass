# ADR 0008 — Resumable Destructive Reset

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA must provide an honest forgotten-Master-Password path. Because the Master Password is the root credential and RAHSA has no recovery key/account/server recovery in Phase 13, a user who cannot remember the password may only delete the inaccessible local vault state and start over.

The destructive reset removes several app-managed artifacts: the active KDBX, exactly one internal LKG KDBX, RAHSA-owned candidate/temp/recovery files, password hint, biometric wrapped state, and the related Android Keystore alias. A crash, force-close, process death, navigation, or background transition can occur around this multi-step cleanup.

The owner requires deterministic behavior: before the final Reset action nothing is deleted; after reset starts, interruption must not leave RAHSA in an ambiguous half-reset state. External user-managed backups and unrelated UI preferences must remain untouched.

## Decision

1. `Forgot Master Password?` may show the persisted password hint when available, but it must clearly state that RAHSA cannot recover/decrypt the vault without the correct Master Password. Backup and biometric are not presented as password recovery.
2. Destructive reset requires the user to type the exact text `DELETE` before the final `Reset RAHSA` action is enabled. One confirmation flow is sufficient; no countdown or repeated confirmation dialogs are required.
3. Before the final Reset action starts, Back/navigation away, Home/background followed by recreation, process death, or force-close performs no deletion. The typed `DELETE` draft is not required to persist; re-entering the confirmation starts from an unconfirmed state.
4. Once the user explicitly starts Reset, Back/Home/navigation is not interpreted as a transaction cancel. Reset is single-flight and must converge to the fully reset state.
5. RAHSA may persist a minimal app-private **non-secret `RESET_IN_PROGRESS` marker** (or equivalent narrowly scoped state) immediately before destructive cleanup begins. The marker must contain no Master Password, credential data, decrypted vault data, backup password, or other secret material.
6. While the reset marker exists, the desired authoritative outcome is **reset completion**, not rollback of already deleted artifacts.
7. Resumable cleanup removes only RAHSA-managed vault/security state tied to the old vault:
   - active internal KDBX;
   - exactly one internal LKG KDBX;
   - RAHSA-owned candidate/temp/recovery artifacts;
   - password hint;
   - biometric quick-unlock wrapped state; and
   - the related Android Keystore alias when present.
8. Cleanup steps must be idempotent. A missing file, missing preference, or already-deleted Keystore alias is treated as already cleaned where safe, so restart can repeat cleanup without recreating old state.
9. After all required cleanup is complete, RAHSA clears the reset marker and routes to the normal Create New Vault flow.
10. If RAHSA starts and finds `RESET_IN_PROGRESS`, it completes the pending cleanup before normal vault use. It must not attempt to reopen or reconstruct the partially deleted old vault.
11. User-managed external backups selected through SAF are never deleted by destructive reset.
12. Unrelated UI preferences such as theme remain intact.
13. No secure-wipe/overwrite framework is introduced. Normal app-managed deletion is sufficient for Phase 13.
14. No generic transaction/workflow framework is introduced; the reset marker is deliberately narrow and exists only to make this destructive operation crash-consistent.

## Consequences

### Positive

- Reset behavior is deterministic across Back/Home, force-close, process death, and crash.
- A partially completed destructive reset cannot later masquerade as a normal intact vault state.
- The marker contains no secret material and does not create a recovery credential.
- Cleanup can safely resume after restart through idempotent operations.
- External backups and unrelated preferences remain outside the destructive boundary.
- The design stays small and purpose-specific instead of introducing a general transaction system.

### Costs / limitations

- Startup must check and finish pending reset cleanup before normal vault routing.
- Reset cleanup requires failure-injection/process-death tests, including already-missing artifact cases.
- Keystore deletion and app-private file cleanup must be tolerant of partial prior completion.
- Because reset is intentionally destructive, once the final action has begun there is no supported rollback to the old vault.

## Rejected alternatives

- Treat Back/Home after Reset starts as cancellation — unsafe after any artifact may already have been deleted.
- Attempt rollback after partial deletion — cannot reliably reconstruct deleted encrypted/security state and creates ambiguous authority.
- Persist the typed `DELETE` confirmation across sessions — unnecessary state for an intentional destructive action.
- Delete user-managed external backups — outside RAHSA's app-managed reset boundary and contrary to the approved backup policy.
- Add secure multi-pass overwrite — not required for the approved scope and unreliable as a general guarantee on modern storage.
- Build a generic transaction/workflow engine — over-engineered; a narrow idempotent reset marker is sufficient.
