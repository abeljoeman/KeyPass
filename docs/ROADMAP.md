# RAHSA Product Roadmap

**Updated:** 2026-09-06

This roadmap communicates sequencing and decision gates. It is not a promise of release dates and it does not authorize implementation by itself.

## Completed — v0.2 Stable Baseline

RAHSA `v0.2-prototype` includes:

- Local KDBX source of truth through Kotpass.
- Create/unlock/manual-lock/background-lock lifecycle.
- Credential CRUD and persistence.
- Search and password generator.
- Secure-screen, logging, clipboard, and corrupt-vault protections.
- Write-failure and lifecycle-race hardening.
- Dark Material 3 UI rollout.
- Auth, Vault, Credential Detail, Create/Edit, Generator, and Settings refinement.
- Loading/error/accessibility regression work.
- Physical-device validation on Samsung Galaxy A11.
- RAHSA product name, logo, and Android identity surfaces.

The stable baseline tag is `v0.2-prototype`.

## Now — Product & Security Expansion Planning

There is no approved implementation task yet. The owner has approved the following **roadmap direction** for planning, while detailed requirements remain undecided.

### Planning Priority 1 — Access, Master Password, and Recovery Policy

Resolve product/security policy for:

- Master-password change.
- Forgotten-master-password behavior.
- Whether RAHSA provides any recovery mechanism beyond destructive reset.
- Re-authentication requirements for sensitive settings/actions.
- Clear separation between recovery and convenience unlock.

Biometric must not be treated as password recovery by default.

### Planning Priority 2 — Data Safety, Backup, and Restore Policy

Protect against loss caused by vault corruption, failed storage, app-data loss, device loss, or accidental destructive actions.

Planning direction:

- Keep KDBX as the encrypted backup artifact; do not invent a custom backup encryption format.
- Evaluate an internal last-known-good / versioned recovery-copy strategy for local corruption/write-failure protection.
- Evaluate manual external encrypted KDBX backup/restore using Android's user-selected document/storage provider flow.
- Prefer provider-neutral Android platform integration (for example Google Drive through the system document provider when available) over a direct Google/cloud API integration unless a later requirement justifies otherwise.
- Prefer versioned backups / restore points over continuously overwriting one backup file.
- Restore must validate a candidate vault before replacing the active vault and must preserve fail-closed/non-destructive behavior.
- Backup protects data availability; it does not recover a forgotten master password.

Detailed retention, naming, restore, merge/replace, automatic-vs-manual, and UX rules are still open decisions.

### Planning Priority 3 — Session and Lock Policy

Define authoritative unlocked-session behavior before biometric and Autofill implementation, including backgrounding, screen-off, timeout, process death, re-authentication, and lock-state semantics.

### Planning Priority 4 — Biometric Quick Unlock

Design biometric as a secure convenience unlock only after master-password/recovery and session semantics are clear. Cover enable/disable, Android Keystore integration, fallback, invalidation, enrollment changes, lockout, and master-password-change interaction.

### Planning Priority 5 — Backup / Restore Implementation

Implementation planning follows only after the Data Safety policy and required PRD/TSD/ADR/Threat Model changes are approved. Depending on the final risk assessment, backup/restore implementation may be prioritized before biometric implementation.

### Planning Priority 6 — Android Autofill

Autofill planning follows the access/session/security foundation. Reuse retained/upstream work where safe, but re-audit its security assumptions against current RAHSA architecture.

### Planning Priority 7 — Productivity Expansion

Candidates include passphrase generation, sorting/filtering, tags/categories, typed items, and other owner-approved usability improvements.

### Not Current Planning by Default

Cloud sync, accounts, shared vaults, TOTP, passkeys, browser extensions, OCR/camera capture, and other broader capabilities require separate owner approval before they enter the active planning sequence.

## Documentation Gate Before Implementation

For each expansion capability:

1. Discuss product behavior and security policy.
2. Audit reuse options: retained RAHSA → upstream KeyPass → Android/Jetpack → mature OSS → minimum local code.
3. Amend `PRD.md`.
4. Amend `TSD.md` and ADRs where architecture/security choices change.
5. Update `docs/THREAT_MODEL.md` where trust boundaries or secrets change.
6. Create small `TASKS.md` tasks with acceptance criteria.
7. Obtain owner approval.
8. Only then hand one active task at a time to Codex.

Each implementation task must preserve the `v0.2-prototype` baseline unless an approved requirement explicitly changes behavior.

## Parked — Public / Play Store Release

Release preparation is intentionally paused.

Parked work includes:

- Trademark/name clearance for launch markets.
- Final Android `applicationId` / package identity migration.
- Release signing/store identity.
- Store listing identity and compliance.
- Privacy/data-safety/support URLs as applicable.
- Final release-oriented attribution presentation.

Do not resume this workstream until the owner explicitly requests it and active tasks are authored.
