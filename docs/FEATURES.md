# RAHSA Feature Inventory

**Updated:** 2026-09-06

Status taxonomy:

- `[CURRENT]` implemented and part of the `v0.2-prototype` baseline.
- `[PLANNING]` candidate under product/security discussion; not approved for implementation.
- `[PARKED]` intentionally deferred workstream.

## Vault and Security

- `[CURRENT]` Single app-private `vault.kdbx` backed by Kotpass.
- `[CURRENT]` First-launch vault creation and master-password unlock.
- `[CURRENT]` Manual lock and background auto-lock behavior.
- `[CURRENT]` Secure-screen protection.
- `[CURRENT]` Sensitive logging hardening.
- `[CURRENT]` Reviewed secure clipboard behavior.
- `[CURRENT]` Corrupted-vault overwrite protection.
- `[CURRENT]` Storage-write failure handling and lock/load race protection.
- `[PLANNING]` Master-password change semantics.
- `[PLANNING]` Forgotten-master-password / recovery policy.
- `[PLANNING]` Biometric quick-unlock after dedicated security design/review.
- `[PLANNING]` Lock/session policy refinements.

## Credentials

- `[CURRENT]` CRUD with title, username, password, URL, and notes.
- `[CURRENT]` Search by title/username.
- `[CURRENT]` Read-first Credential Detail.
- `[CURRENT]` Create/Edit validation and unsaved-change handling.
- `[CURRENT]` Safe copy/reveal behavior.
- `[PLANNING]` Typed items, tags/categories, or other credential-model expansion only after explicit requirement review.

## Password Generator

- `[CURRENT]` Standalone Generator.
- `[CURRENT]` Contextual generator use in credential editing.
- `[CURRENT]` Length and character-category configuration.
- `[CURRENT]` Secure copy path and generated-secret logging protections.
- `[PLANNING]` Passphrase generation.

## Navigation and UI

- `[CURRENT]` Compose UI with retained navigation/state architecture.
- `[CURRENT]` Top-level Vault / Generator / Settings navigation.
- `[CURRENT]` Fixed dark Material 3 visual direction.
- `[CURRENT]` Loading/error conventions.
- `[CURRENT]` Accessibility baseline including touch targets, semantics, font scaling, and basic TalkBack validation.

## Settings

- `[CURRENT]` Password hint.
- `[CURRENT]` Auto-lock toggle.
- `[CURRENT]` Help/About surfaces.
- `[CURRENT]` Legacy biometric-related code/settings may still exist internally but are not an approved unlock implementation.
- `[PLANNING]` Secure biometric enablement/disablement UX and lifecycle.

## Integration / Data Portability

- `[CURRENT]` Core application requires no backend and no Android `INTERNET` permission.
- `[PLANNING]` Android Autofill.
- `[PLANNING]` Safe KDBX backup/export/import strategy.
- `[PLANNING]` Cloud sync only if separately approved in the future; it is not part of current planning by default.

## Branding and Release

- `[CURRENT]` Product name: **RAHSA**.
- `[CURRENT]` RAHSA shield/logo and launcher identity assets.
- `[PARKED]` Trademark/name clearance.
- `[PARKED]` Final package/applicationId migration.
- `[PARKED]` Release signing/store identity.
- `[PARKED]` Play Store listing/compliance/readiness.

Planning status does not authorize Codex implementation. Only an approved active `Txxx` in `TASKS.md` does.
