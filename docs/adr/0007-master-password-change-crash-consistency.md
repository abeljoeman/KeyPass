# ADR 0007 — Change Master Password Re-key, Acknowledgment, and Crash Consistency

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA must support changing the Master Password without reviving the inherited settings-only password flow. The operation changes the KDBX root credential, can invalidate biometric quick-unlock state, and can be interrupted by navigation, backgrounding, process death, force-close, or crash.

The owner also requires an explicit warning that the new Master Password is unrecoverable if forgotten, plus an intentional swipe acknowledgment before the final change action is enabled.

This decision builds on accepted ADR 0005 for one-LKG validate-before-promote storage safety.

## Decision

1. Change Master Password is available only from an unlocked vault, but the current Master Password must still be re-authenticated against the active KDBX before re-key.
2. KDBX credential change uses the existing Kotpass `0.13.0` credential-modification path (`KeePassDatabase.modifyCredentials` and existing credential primitives). RAHSA does not implement custom KDBX cryptography.
3. The user enters the current Master Password, new Master Password, confirmation, optional prefilled/editable hint, and sees an advisory-only strength indicator.
4. Before the final action is enabled, RAHSA shows a clear consequence warning: the old Master Password will stop working and RAHSA cannot recover/open the vault if the new Master Password is forgotten.
5. The warning requires swipe-to-acknowledge using the same existing Material 3/platform swipe pattern approved for Create Vault. Do not introduce a new gesture framework or separate bespoke control.
6. Swipe completion records acknowledgment only. It never starts re-key. The explicit `Change Master Password` action remains a separate intentional step.
7. The acknowledgment remains accessible through appropriate Android accessibility semantics/action for TalkBack users.
8. Draft sensitive state is not persisted. Before the final action starts, Back/navigation away, Home/background followed by recreation, process death, or force-close discards current/new/confirmation password input and acknowledgment. Re-entering the flow starts with password fields empty and acknowledgment false; the existing hint may be prefilled again from its current persisted value.
9. Existing RAHSA Auto-Lock/session behavior is not redesigned. If normal lifecycle rules lock the vault while the flow is abandoned/backgrounded, the user must unlock through the existing path.
10. After the user starts the final operation, Back/Home/navigation is not interpreted as a transaction cancel. The operation is single-flight and follows fail-safe storage semantics.
11. Re-key creates a separate candidate KDBX, verifies that candidate with the new password, then uses accepted ADR 0005 to preserve the valid old active vault as LKG and promote the verified candidate.
12. The promotion of the verified candidate to active KDBX is the commit point:
    - before commit, the old active vault/password/hint remain authoritative;
    - after commit, the new active vault/password are authoritative and RAHSA must not speculatively roll back merely because UI completion did not run.
13. A minimal app-private **non-secret recovery/finalization marker** may be used to make post-commit cleanup deterministic across process death/crash. It must never contain the old/new Master Password or decrypted vault data.
14. After commit, required finalization includes applying the retained/edited hint, invalidating old biometric quick-unlock state when present, cleaning temporary artifacts/marker, and reconciling in-memory/session state when the process is alive.
15. If the app restarts with a post-commit marker, startup completes only the required non-secret finalization/cleanup before normal use. The promoted KDBX remains authoritative.
16. A failed operation before commit leaves the old vault, old Master Password, old hint, and prior biometric state usable.
17. On success, the old Master Password fails, the new Master Password opens the vault, the current live session remains unlocked when the process survives, and biometric quick unlock is disabled/invalidation-notified only if it had been active.
18. External backup is not a prerequisite for this operation.

## Consequences

### Positive

- Real KDBX re-key uses the existing vetted library path instead of settings-only or custom crypto.
- User must explicitly acknowledge the unrecoverability consequence before committing the change.
- Back/Home/process-death behavior is deterministic and does not persist sensitive drafts.
- Crash timing around promotion has an explicit authoritative-state boundary.
- A small recovery marker avoids a large transaction framework while preventing stale hint/biometric metadata after a post-commit crash.
- Reuses ADR 0005 rather than inventing a second persistence mechanism.

### Costs / limitations

- Change Master Password needs lifecycle/process-death tests in addition to normal success/failure tests.
- Post-commit finalization requires careful idempotency.
- The recovery marker adds a small amount of app-private state that must be cleaned correctly.
- The advisory strength dependency, if adopted, still needs task-level dependency/license/security verification.

## Rejected alternatives

- Revive inherited settings-only `ChangePassword` behavior — does not re-key KDBX.
- Trust an already-unlocked session instead of current-password reauthentication — insufficient for this sensitive operation.
- Let swipe directly execute re-key — conflates acknowledgment with destructive/security-sensitive action.
- Persist draft password fields to survive recreation — unnecessary secret persistence risk.
- Treat Back/Home after operation start as rollback/cancel — unsafe once persistence may have crossed the commit boundary.
- Automatically roll back after verified candidate promotion because success UI was interrupted — creates ambiguous credential authority.
- Build a general transaction/workflow framework — over-engineered for the approved requirement; a narrow idempotent recovery marker is sufficient.
