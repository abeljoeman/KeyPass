# Next Build Notes — UI/UX Refinement

**Updated:** 2026-09-05
**Working name:** KeyPass
**Scope:** Incremental UI/UX refinement over completed v0.1 prototype

The approved UX contract is `docs/UX_UI_BASELINE.md`.

## Non-Negotiable Guardrails

- KDBX remains the single source of truth.
- Preserve current `VaultRepository`.
- Preserve existing Redux/navigation/state unless a tiny localized change is required by an approved flow.
- Reuse current ViewModels/actions/settings/generator/security behavior.
- No accounts/cloud/sync/OCR/biometric expansion/typed items/external vault picker.
- No custom design-system framework.
- No shimmer/icon mega dependency.
- Security/lock behavior wins over preserving transient UI state.

## Implementation Order

1. Theme/dark startup/typography.
2. Icon inventory + accessibility semantics.
3. Small reusable app-specific components.
4. Auth.
5. Vault/Dashboard.
6. Credential Detail.
7. Create/Edit + contextual generator.
8. Standalone Generator.
9. Settings.
10. Global loading/error/accessibility regression.

Do not attempt a whole-app rewrite in one task/commit.

## Theme / Identity

- Dark-only Material 3.
- Layered charcoal surfaces.
- Calm recognizable blue primary.
- Restrained warm amber/gold identity accent.
- No Dynamic Color initially.
- Sans for normal UI; monospace only for secret values when useful.
- Exact palette remains subject to physical contrast/daylight validation.

Keep identity replaceable: app mark + product name + brand accent. Do not invest in a new KeyPass-specific logo now.

## Targeted Checks

### Auth errors

Current Auth maps broad open failures to `incorrect_password`.

Before a separate corrupt/unreadable state, inspect existing exception signals. Add a localized distinction only if reliable. Never overwrite/create after failed open/decode.

### Create → Detail

Approved UX is:

```text
Vault → Add → Save → Detail → Back → Vault
```

Use the smallest localized change to expose the newly created ID/saved credential to existing navigation. Do not redesign repository/navigation.

### Generator

UX candidate length: 8–64, default ~20. Verify current config/constants/tests before coding.

Standalone Copy must use the existing reviewed secure-clipboard path + safe Snackbar rather than the current separate helper/Toast.

Remove blank-spaces and individual-symbol controls from refreshed UI without aggressive generator-model refactoring.

### Settings

Do not promote inherited biometric controls. Do not delete/rewrite biometric internals as part of this UI pass.

Generator becomes the single place for generator configuration; remove duplicate password-length row from refreshed Settings.

### Vault icon

Current candidate: `shield_lock` for Vault and `lock` for manual Lock. Visually verify before finalizing.

## Loading Rules

- Unlock → `Unlocking...`.
- Create vault → `Creating vault...`.
- Save → `Saving...`.
- Disable duplicate mutation action.
- Static placeholder only where latency is perceptible.
- No skeleton before unlock.
- No shimmer.

## Accessibility / Device Validation

At minimum:

- 48dp touch targets where applicable.
- Localized descriptions for icon-only actions.
- Decorative icons null.
- Font scaling.
- Long-title handling.
- Basic TalkBack.
- Password masked by default.

Physical baseline: Samsung Galaxy A11 (`SM-A115F`, Android 12/API 31) plus emulator.

## Brand Decision Gate Reminder

After UI implementation/pilot + UX validation, stop before release work.

Decide final name, trademark clearance, app icon/logo, final `applicationId`/package identity, signing/store identity, listing identity, and attribution/license presentation.

Until then: **KeyPass** is the working development name.
