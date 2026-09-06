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

No expansion feature is approved for implementation yet.

Planning candidates include:

- Master-password management/change.
- Forgotten-master-password and recovery policy.
- Biometric quick-unlock.
- Lock/session policy refinements.
- Android Autofill.
- Safe KDBX backup/export/import strategy.
- Passphrase generation.
- Other owner-approved product capabilities.

These items are candidates for discussion and research, not executable tasks.

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

## Later — Expansion Implementation

Implementation sequencing will be decided only after planning. Do not infer an implementation order from the candidate list above.

Each task must preserve the `v0.2-prototype` baseline unless an approved requirement explicitly changes behavior.

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
