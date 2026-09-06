# Product Roadmap

**Updated:** 2026-09-06

This roadmap communicates sequencing and decision gates. It is not a promise of release dates.

## Completed — v0.1 Prototype Baseline

- Local KDBX source of truth via Kotpass.
- Create/unlock/lock lifecycle.
- Credential CRUD and persistence.
- Search and password generator.
- Secure-screen/logging/clipboard hardening.
- Corrupted-vault overwrite regression protection.
- Physical-device regression pass and `v0.1-prototype` tag.

## Completed — UX/UI Source of Truth

- Approved UX flows and component mapping.
- Dark-only Material 3 direction.
- Loading/error/accessibility rules.
- Feature inventory and out-of-scope boundaries.
- Mandatory Brand Decision Gate.

**Exit:** consolidated source-of-truth patch reviewed and committed.

## Completed — UI Foundation Pilot

1. Fixed dark Material 3 theme and dark startup.
2. Typography cleanup.
3. Small Material Symbols icon set.
4. Minimal reusable app-specific UI components.
5. Validate on emulator + Samsung Galaxy A11 before broad rollout.

**Principle:** no architecture migration.

## Completed — Screen Rollout

1. Auth/Create/Confirm/Unlock.
2. Vault/Dashboard.
3. Credential Detail.
4. Create/Edit + contextual generator.
5. Standalone Generator.
6. Settings.
7. Global navigation/back/lock consistency.

Each task is small, reviewable, built, and tested.

## Completed — UX Validation

- Loading feedback on slower physical hardware.
- Empty/no-results/error/destructive flows.
- Long-title behavior.
- Font scaling and basic TalkBack.
- Regression of lock/unlock, CRUD persistence, secure clipboard, and corrupt-vault protection.

## Now — Mandatory Brand Decision Gate

**STOP HERE before public/store release preparation.**

Resolved:

- Final product name: **RAHSA**.
- Final app icon/logo and brand assets: supplied RAHSA shield integrated.

Still required:

- Trademark/name clearance for intended markets.
- Final Android `applicationId` / package identity strategy.
- Signing/store identity.
- Store-facing product name/listing identity.
- Upstream/open-source attribution presentation.

Until complete:

```text
Final product name: RAHSA
Public release status: BLOCKED on remaining gate items
```

## After Brand Gate — Release Readiness

Revalidate Play Store requirements at that time, then address privacy/data-safety, AAB/signing, target API/testing, listing assets, support/privacy/contact URLs, and dependency/license attribution as applicable.

## Future — Product Expansion

Only after explicit requirement review:

- Biometric quick-unlock.
- OCR/camera-assisted capture.
- Online/cloud sync.
- Typed vault items.
- Passphrase generation.
