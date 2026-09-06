# ADR 0005 — Keep Internal Active KDBX, One LKG Copy, and SAF External Backup/Restore

**Status:** Proposed — Phase 13 technical review  
**Date:** 2026-09-06

## Context

RAHSA needs local corruption/write-failure protection plus manual external backup/restore without introducing cloud accounts, custom backup formats, or an external file as the live database.

The approved product policy requires exactly one internal Last-Known-Good (LKG) encrypted copy, provider-neutral external KDBX backup, and validate-before-replace restore.

## Decision

1. The active vault remains an app-private, RAHSA-managed KDBX.
2. RAHSA maintains exactly one additional app-private encrypted LKG KDBX.
3. Candidate replacement files are short-lived app-private artifacts and are validated before promotion.
4. A verified old active vault becomes LKG immediately before a verified candidate is promoted.
5. Unverified candidate data never replaces LKG.
6. External backup/restore uses Android Storage Access Framework/system document providers.
7. The external artifact is the KDBX itself; no RAHSA-specific encryption container is introduced.
8. External KDBX is transport/backup input/output only, not the live active vault.
9. Restore is full-vault replacement, not merge.
10. Active corruption never triggers silent LKG restore; the user must opt in after being informed about possible loss of latest changes.

## Consequences

### Positive

- Reuses KDBX/Kotpass and Android platform storage integration.
- Keeps core app provider-neutral and backend-free.
- Protects the previous verified state around risky replacement operations.
- Avoids custom backup cryptography and merge complexity.

### Costs / limitations

- Exactly one LKG is not historical versioning.
- LKG consumes roughly one additional encrypted-vault file internally.
- User-managed external backups may be stale because backup is manual.
- SAF provider reliability/behavior is outside RAHSA's direct control; failures must be surfaced non-destructively.

## Rejected alternatives

- Custom `.rahsa-backup` encrypted container — unnecessary custom format/crypto surface.
- Direct Google Drive API/OAuth — provider-specific complexity without an approved need.
- Versioned internal backup history — over scope; owner approved exactly one LKG.
- Use external document as active vault — weakens app-managed lifecycle and provider availability assumptions.
- Silent auto-restore from LKG — risks unnoticed rollback/data loss.
