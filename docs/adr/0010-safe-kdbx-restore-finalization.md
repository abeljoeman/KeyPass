# ADR 0010 — Safe KDBX Restore Validation, Promotion, and Post-Commit Finalization

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA must restore user-selected external KDBX backups without treating the external document as the live vault, without mutating the current vault before the selected backup is proven valid, and without leaving stale biometric/security metadata if the process dies after a successful replacement.

The approved product behavior requires provider-neutral SAF restore, password-based validation before replacement confirmation, full-vault replacement, preservation of the current active vault as LKG when one exists, and invalidation of prior biometric quick-unlock state after a successful restore.

This decision builds on accepted ADR 0005 for one-LKG validate-before-promote storage safety.

## Decision

1. Restore is available from Settings and from initial setup.
2. The external document selected through Android Storage Access Framework is treated only as transport/input. It never becomes RAHSA's live active vault.
3. RAHSA copies the selected external KDBX into a short-lived app-private candidate before decode/validation.
4. The user supplies the Master Password for the selected backup. RAHSA validates the copied candidate using Kotpass before any replacement confirmation is shown.
5. Wrong password, unsupported/invalid/corrupt KDBX, decode failure, copy failure, or validation failure stops the operation without mutating current active/LKG state.
6. The replacement confirmation is shown only after successful candidate validation.
7. Before the final Restore action starts, Back/navigation away, Home/background followed by recreation, process death, force-close, or cancellation performs no authoritative vault mutation. Candidate/password UI state need not persist and may be cleaned safely.
8. After the final Restore action starts, Back/Home/navigation is not treated as a transaction cancel. The operation is single-flight.
9. When a current active vault exists, restore reuses accepted ADR 0005: preserve the verified current active as the single LKG, then promote the already-validated candidate to active.
10. During initial setup, there is no prior active vault to preserve as LKG; the validated candidate may be promoted directly to active using the same verification discipline.
11. Promotion of the validated candidate to active KDBX is the commit point:
    - before commit, the prior active vault (if any) remains authoritative;
    - after commit, the restored active KDBX and its Master Password are authoritative.
12. RAHSA does not roll back the restored vault merely because the process dies, UI navigation changes, or the success screen does not run after the commit point.
13. A minimal app-private **non-secret post-commit restore-finalization marker** may be used to complete deterministic cleanup after process death/crash. It must never contain the backup Master Password, external document URI, credential data, decrypted vault data, or any other secret material.
14. Post-commit finalization is idempotent and includes, as applicable:
    - invalidating/disabling prior biometric quick-unlock wrapped state and related Keystore material;
    - cleaning app-private candidate/temp artifacts;
    - reconciling session/routing state when the process survives; and
    - clearing the finalization marker after required cleanup completes.
15. On startup with a pending restore-finalization marker, RAHSA keeps the promoted restored KDBX authoritative, completes only required non-secret finalization/cleanup before normal use, then clears the marker.
16. Successful restore is full-vault replacement. No merge, deduplication, conflict resolution, or partial import is performed.
17. The restored vault's Master Password becomes the effective password after commit, even if it differs from the previously active vault password.
18. If biometric quick unlock was previously active, successful restore invalidates it and the user is informed that it must be enabled again manually.
19. Restore progress uses truthful existing Material 3 progress/animation primitives: determinate only for measurable copy progress and indeterminate for validation/decrypt/promotion stages where an accurate percentage is unavailable.
20. No generic transaction/workflow framework is introduced; restore reuses ADR 0005 plus the narrow non-secret finalization marker defined here.

## Consequences

### Positive

- External input is proven valid before the user can confirm replacement.
- Existing active data remains untouched on wrong-password, invalid-file, copy, and validation failures.
- Restore reuses the already-approved one-LKG safe-promotion architecture instead of creating a separate vault transaction model.
- The commit point clearly defines which vault/password is authoritative across crashes.
- A small non-secret finalization marker prevents stale biometric state or temp artifacts after a post-commit crash without persisting secrets.
- Initial-setup restore and in-app restore can share the same validation/promotion pipeline while respecting the absence of an old active vault during first setup.

### Costs / limitations

- Restore requires failure-injection and process-death tests around pre-commit and post-commit boundaries.
- Post-commit cleanup must be idempotent.
- The selected external KDBX may be unsupported by Kotpass/RAHSA and must fail non-destructively.
- Full replacement deliberately provides no merge behavior.

## Rejected alternatives

- Use the external SAF document directly as the live vault — weakens app-managed lifecycle and provider-availability assumptions.
- Confirm replacement before candidate validation — exposes the current vault to avoidable invalid-input risk.
- Replace active immediately after file selection — violates non-destructive restore requirements.
- Skip LKG preservation when an active vault exists — discards the approved rollback-protection boundary.
- Roll back after a successful promotion because the process/UI did not finish — creates ambiguous password/vault authority.
- Persist the backup password or URI in the recovery marker — unnecessary sensitive state.
- Build a second generic transaction engine for restore — over-engineered; ADR 0005 plus a narrow finalization marker is sufficient.
