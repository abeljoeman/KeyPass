# RAHSA Feature Inventory

**Updated:** 2026-09-06

Status taxonomy:

- `[CURRENT]` implemented in the `v0.2-prototype` baseline.
- `[APPROVED-P13]` owner-approved Phase 13 product scope; implementation not yet active.
- `[PARKED]` intentionally deferred to a future roadmap/workstream.
- `[CLOSED]` reviewed and intentionally no change for the current roadmap.

## Vault and Security

- `[CURRENT]` Single app-private active `vault.kdbx` backed by Kotpass.
- `[CURRENT]` First-launch vault creation and Master Password unlock.
- `[CURRENT]` Manual lock and background Auto-Lock behavior.
- `[CURRENT]` Secure-screen, sensitive logging, secure clipboard, corruption/write-failure protections.
- `[APPROVED-P13]` Explicit Master Password unrecoverability warning + mandatory swipe acknowledgment before Create Vault.
- `[APPROVED-P13]` Real Master Password change/re-key with current-password reauthentication, advisory strength, and safe promotion.
- `[APPROVED-P13]` Forgot Master Password explanation/hint plus typed-`DELETE` destructive reset.
- `[APPROVED-P13]` Exactly one encrypted internal Last-Known-Good vault copy.
- `[APPROVED-P13]` Secure Biometric Quick Unlock as opt-in convenience only.
- `[CLOSED]` Session/Auto-Lock redesign — existing behavior accepted.

## Data Safety / Backup / Restore

- `[APPROVED-P13]` Exactly one LKG, not version history.
- `[APPROVED-P13]` Manual external encrypted KDBX backup through Android user-selected document providers/SAF.
- `[APPROVED-P13]` Provider-neutral destinations; no direct Google/cloud API by default.
- `[APPROVED-P13]` Safe restore: copy/select candidate, ask its password, decrypt/validate first, explicit full replacement, preserve current active as LKG, verify.
- `[APPROVED-P13]` Restore available from Settings and initial setup.
- `[APPROVED-P13]` Truthful subtle animated operation statuses using existing Material 3 progress/animation primitives; no fake percentages.
- `[APPROVED-P13]` Backup is availability, not forgotten-password recovery.

## Credentials

- `[CURRENT]` CRUD with title, username, password, URL, and notes.
- `[CURRENT]` Search by title/username.
- `[CURRENT]` Existing sorting by Title/Username and direction.
- `[CURRENT]` Read-first detail, create/edit validation, safe copy/reveal.
- `[CLOSED]` Additional Sort/Filter expansion for current roadmap.
- `[CLOSED]` Tags/Categories for current roadmap.
- `[CLOSED]` Typed Items for current roadmap.
- `[PARKED]` Camera/OCR Scan Credential; explored for future use, not Phase 13.
- `[PARKED]` Website/favicon retrieval.

## Password Generator

- `[CURRENT]` Standalone and contextual generator; length/category configuration.
- `[APPROVED-P13]` Security hardening to CSPRNG/`SecureRandom` with no UX expansion.
- `[CLOSED]` Passphrase Generator for current roadmap.

## Navigation and UI

- `[CURRENT]` Compose UI with retained navigation/state architecture.
- `[CURRENT]` Top-level Vault / Generator / Settings navigation.
- `[CURRENT]` Dark Material 3 visual direction, loading/error conventions, accessibility baseline.
- `[APPROVED-P13]` Create-vault swipe acknowledgment must reuse existing Material/platform interaction and remain accessible.
- `[APPROVED-P13]` Backup/restore status uses subtle existing Material 3 progress animation and light text transitions.

## Settings

- `[CURRENT]` Password hint and Auto-Lock toggle.
- `[CURRENT]` Help/About surfaces.
- `[APPROVED-P13]` Change Master Password.
- `[APPROVED-P13]` Backup / Restore.
- `[APPROVED-P13]` Secure biometric enable/disable lifecycle.

## Integration / Data Portability

- `[CURRENT]` Core application requires no backend and no Android `INTERNET` permission.
- `[APPROVED-P13]` External KDBX backup/restore through SAF only.
- `[PARKED]` Android Autofill.
- `[PARKED]` Cloud sync unless explicitly reopened.

## Branding and Release

- `[CURRENT]` Product name: **RAHSA**.
- `[CURRENT]` RAHSA shield/logo and launcher identity assets.
- `[PARKED]` Trademark/name clearance.
- `[PARKED]` Final package/applicationId migration.
- `[PARKED]` Release signing/store identity.
- `[PARKED]` Play Store listing/compliance/readiness.

`[APPROVED-P13]` does not authorize Codex implementation. Only an approved active `Txxx` plus READY JIT Implementation Kit in `TASKS.md` does.
