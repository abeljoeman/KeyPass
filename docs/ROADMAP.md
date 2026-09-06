# RAHSA Product Roadmap

**Updated:** 2026-09-06

This roadmap communicates sequencing and decision gates. It is not a promise of release dates and does not authorize implementation by itself.

## Completed — v0.2 Stable Baseline

RAHSA `v0.2-prototype` includes local KDBX/Kotpass source of truth, vault create/unlock/lock lifecycle, credential CRUD/search, password generator, reviewed security hardening, Material 3 UI/accessibility work, physical-device validation, and RAHSA product identity surfaces.

Stable baseline tag: `v0.2-prototype`.

## Now — Phase 13 Access & Data Safety Planning

Owner product review is complete. The following product scope is **approved for Phase 13 planning**, but implementation remains frozen until the technical package and task definitions are approved and exactly one JIT kit/task is activated.

### Phase 13 scope

1. **Password Generator Security Hardening** — CSPRNG (`SecureRandom`) and cleaner allowed-character selection; no UX expansion.
2. **Create Vault Recovery Acknowledgment** — explicit unrecoverability warning plus mandatory swipe acknowledgment using existing Material/platform components before Create Vault can be enabled.
3. **Internal Data-Safety Foundation** — exactly one encrypted Last-Known-Good vault and validate-before-promote replacement behavior.
4. **Change Master Password** — current-password reauthentication, advisory strength, optional hint edit, real KDBX re-key, safe verification/promotion.
5. **Forgot Master Password / Destructive Reset** — honest unrecoverability explanation, hint, typed `DELETE`, local RAHSA-state reset only.
6. **Manual External KDBX Backup** — Android provider-neutral system document picker/SAF, manual only.
7. **Safe Restore** — validate selected KDBX/password first, explicit full-replacement confirmation, preserve current active as LKG.
8. **Biometric Quick Unlock** — opt-in strong biometric convenience unlock backed by Android Keystore and a real vault-open transition.
9. **Critical-operation single execution** — prevent duplicate create/change/reset/backup/restore/biometric mutations.
10. **Truthful subtle operation progress** — existing Material 3 progress/animation primitives; determinate only when measurable.

### Closed — no change for current roadmap

- Session/Auto-Lock redesign: existing behavior is accepted.
- Passphrase Generator.
- Extra sort/filter modes; Favorites/Newest/Oldest.
- Tags/Categories.
- Typed Items.

### Parked — future roadmap

- Android Autofill.
- Website/favicon retrieval.
- Camera/OCR credential scanning (explored, but deferred; future scan concept may use default title `Scanned Credential`).
- Cloud sync/accounts/shared vaults and other broad integration features unless separately reopened.

## Phase 13 planning gate

Current sequence:

```text
Owner product review COMPLETE
→ PRD Phase 13 amendment APPROVED
→ TSD / ADR / Threat Model DRAFTED
→ T126–T133 DRAFTED
→ owner technical/task approval
→ JIT Implementation Kit for next task only
→ activate exactly one task
→ Codex implementation / validation / focused checkpoint
```

`TASKS.md` must remain `PLANNING_FREEZE / NONE / NONE` until the owner approves the technical/task package and a next-task kit is READY.

## Parked — Public / Play Store Release

Public release preparation remains intentionally paused, including trademark/name clearance, package/applicationId migration, release signing/store identity, store listing/compliance, privacy/support URLs, and final release attribution presentation.
