# Changelog

This file records changes that have actually landed in the project. Roadmap items and ideas belong in planning documents until implemented.

## Unreleased

### Brand

- Selected **RAHSA** as the product name and integrated the supplied shield logo.
- Replaced user-facing KeyPass labels in active prototype flows and generated legacy, adaptive, and monochrome Android icon assets.
- Added a brand decision record plus explicit KeyPass/Kotpass MIT attribution.
- Kept package and release-signing identities unchanged until their owner-controlled values are approved.

### Planning / UX

- Added approved UX/UI baseline for the next incremental interface refinement.
- Added feature inventory, roadmap, and next-build notes.
- Added the mandatory Brand Decision Gate before public/store release preparation.
- Kept `KeyPass` as the working development name until the Brand Decision Gate.
- Aligned PRD/TSD with the current app-private `vault.kdbx` flow and removal of external open/import choices.
- Defined dark-only Material 3, loading/error, accessibility, icon, and replaceable brand-layer directions without expanding feature scope.

## v0.1-prototype

### Completed

- Local KDBX-backed persistence using Kotpass.
- First-launch vault creation and returning-user unlock.
- Credential CRUD, search, and password generation.
- Manual/background locking.
- Secure-screen, sensitive-log, and clipboard hardening.
- Corrupted-vault overwrite protection and regression coverage.
- Physical-device prototype test pass.

### Scope

- No backend/cloud sync.
- No external backup/import/export flow in the retained prototype.
- No Autofill, TOTP, or passkey scope.
