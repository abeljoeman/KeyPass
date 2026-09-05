# Feature Inventory

**Updated:** 2026-09-05

Status taxonomy:

- `[CURRENT]` implemented/current capability.
- `[PROPOSED]` approved change for the next UI/UX build.
- `[FUTURE]` expected later, not currently available/approved.
- `[IDEA]` exploration only.

## Vault and Security

- `[CURRENT]` Single app-private `vault.kdbx`.
- `[CURRENT]` First-launch vault creation and master-password unlock.
- `[CURRENT]` Manual lock and background auto-lock behavior.
- `[CURRENT]` Secure-screen protection.
- `[CURRENT]` Sensitive logging hardening.
- `[CURRENT]` Reviewed secure clipboard behavior.
- `[CURRENT]` Corrupted-vault overwrite protection.
- `[PROPOSED]` Clearer loading/error presentation and dark startup.
- `[FUTURE]` Biometric quick-unlock after dedicated security review.
- `[FUTURE]` External vault selection/import only if separately approved.

## Credentials

- `[CURRENT]` CRUD with title, username, password, URL, notes.
- `[CURRENT]` Search and copy username/password.
- `[PROPOSED]` Read-first Credential Detail.
- `[PROPOSED]` Simplified Create/Edit + validation + unsaved-change confirmation.
- `[PROPOSED]` Improved long-title handling.
- `[IDEA]` Typed items such as PIN, Wi-Fi, bank account, secure note.
- `[IDEA]` Tags/categories expansion.

## Password Generator

- `[CURRENT]` Existing generator engine/config and persisted settings.
- `[PROPOSED]` Standalone top-level Generator.
- `[PROPOSED]` Contextual generator bottom sheet.
- `[PROPOSED]` Fresh generation on entry/settings changes.
- `[PROPOSED]` Uppercase/Lowercase/Numbers/Symbols baseline.
- `[PROPOSED]` Remove blank-spaces toggle and individual-symbol matrix from refreshed UI.
- `[FUTURE]` Passphrase mode.
- `[IDEA]` Profiles/history.

## Navigation and UI

- `[CURRENT]` Compose UI with retained Redux/navigation state.
- `[PROPOSED]` Top-level `Vault | Generator | Settings`.
- `[PROPOSED]` Lock in top app bar and Add Extended FAB on Vault.
- `[PROPOSED]` Fixed dark Material 3 visual system.
- `[PROPOSED]` Material Symbols Rounded direction.
- `[PROPOSED]` Material/platform sans typography for normal UI.
- `[PROPOSED]` Static placeholders only where latency is perceptible.
- `[PROPOSED]` Accessibility baseline: touch targets, semantics, font scaling, TalkBack.

## Settings

- `[CURRENT]` Password hint, binary auto-lock, feedback/share/about.
- `[CURRENT]` Biometric-related inherited code/preferences exist.
- `[PROPOSED]` Simplified Settings list presentation.
- `[PROPOSED]` Do not surface biometric controls.
- `[PROPOSED]` Remove Settings search/collapsible cards.
- `[PROPOSED]` Remove duplicate generator-length setting.

## Connectivity and Branding

- `[CURRENT]` No backend required for core prototype.
- `[CURRENT]` `KeyPass` is the working development name.
- `[PROPOSED]` Replaceable product-name/app-mark/brand-accent slots.
- `[FUTURE]` Cloud sync, OCR, Play Store release readiness.
- `[FUTURE]` Final brand/name/trademark/logo/`applicationId`/store identity at the mandatory Brand Decision Gate.
