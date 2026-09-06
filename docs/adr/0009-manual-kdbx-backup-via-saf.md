# ADR 0009 — Manual External KDBX Backup via Provider-Neutral SAF

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA needs a manual external backup flow that preserves the existing KDBX security model, remains provider-neutral, and does not introduce a cloud account, provider-specific SDK, custom backup container, or unnecessary internal transaction machinery.

The approved product policy requires the user to choose the destination through Android's system document flow, with the encrypted `.kdbx` itself as the external backup artifact. Backup protects data availability; it is not Master Password recovery.

ADR 0005 already establishes that the active vault remains app-private and that external KDBX files are transport/backup artifacts rather than the live vault.

## Decision

1. Manual backup starts from the current verified app-private active KDBX.
2. RAHSA uses Android Storage Access Framework/system document creation APIs to let the user choose provider, location, and filename.
3. RAHSA copies the encrypted active KDBX bytes directly to the selected destination through `ContentResolver`; it does not decrypt credentials and reserialize them merely for backup.
4. The external artifact is the `.kdbx` itself. No RAHSA-specific backup container or additional encryption format is introduced.
5. The flow remains provider-neutral. RAHSA does not add direct Google Drive/cloud APIs, OAuth, or provider-specific storage SDKs for Phase 13 backup.
6. The external destination is output only. It never becomes RAHSA's active vault.
7. Backup is a non-mutating operation for internal authoritative state: success or failure must not modify the active KDBX or LKG.
8. Backup success is reported only after destination writing and close/finalization complete without error.
9. If byte-copy progress is accurately measurable, RAHSA may show determinate progress. If total/progress cannot be known reliably for the provider, RAHSA uses indeterminate Material 3 progress feedback.
10. RAHSA does not synthesize fake percentages. Status text may use the already-approved subtle Compose/Material animation pattern.
11. Backup execution is single-flight; duplicate backup actions are prevented while an operation is in progress.
12. No internal recovery/transaction marker is introduced for backup. Because backup does not mutate authoritative internal vault state, a process death/crash can leave an incomplete external destination artifact, and the user can run backup again.
13. RAHSA does not attempt to delete or repair a partially written provider document after an interrupted write unless the provider/API gives a simple, reliable, task-local way to do so. The internal vault remains unaffected either way.
14. No scheduled/background backup, retention manager, version history, or sync is introduced in this task.

## Consequences

### Positive

- Preserves KDBX as the only backup artifact and avoids custom cryptography.
- Reuses Android's provider-neutral storage UI and permission model.
- Keeps backup independent of provider-specific accounts/SDKs.
- Does not add internal transactional state for a non-mutating operation.
- Internal active/LKG safety is unaffected by external provider failure.

### Costs / limitations

- Provider behavior, write reliability, and incomplete-document cleanup are partly outside RAHSA's control.
- Some providers may not expose reliable total-size/progress information, so progress can be indeterminate.
- A crash during write may leave a partial external file that the user should overwrite/delete or replace with a new backup later.
- Manual backup can become stale because Phase 13 does not schedule backups automatically.

## Rejected alternatives

- Decrypt and reserialize credentials into a new backup object — unnecessary secret handling and transformation.
- Custom `.rahsa-backup` container — unnecessary format/crypto surface.
- Direct Google Drive/cloud SDK or OAuth — provider-specific complexity outside approved scope.
- Make the external document the live vault — violates the approved internal-active-vault architecture.
- Add an internal `BACKUP_IN_PROGRESS` recovery marker — unnecessary because backup does not mutate authoritative internal vault state.
- Fake stage-based percentage — misleading when actual byte progress is not measurable.
