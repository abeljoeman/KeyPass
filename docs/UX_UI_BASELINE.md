# UX/UI Baseline — Next Build

**Status:** Approved planning baseline
**Date:** 2026-09-05
**Product name:** RAHSA
**Brand status:** Name and primary mark selected; remaining gate items tracked in `docs/BRAND_DECISION.md`

This document defines the approved UX/UI direction for the next implementation phase. It is intentionally constrained to capabilities that already exist in the retained prototype.

## 1. Status Taxonomy

- `[CURRENT]` — implemented/current behavior.
- `[PROPOSED]` — approved or under-consideration change for the next build.
- `[FUTURE]` — expected later but unavailable now.
- `[IDEA]` — exploration only; not approved for implementation.

Future/idea items MUST NOT leak into the current UI implementation.

## 2. Scope Guardrails

The next build is a UI/UX refinement, not an architecture rewrite.

Do:

- Modernize visual hierarchy.
- Adopt a controlled dark-only Material 3 presentation.
- Improve loading/feedback/error states.
- Simplify navigation, forms, Detail, Generator, and Settings.
- Reuse current repository, state, actions, KDBX, generator, settings, secure-screen, and secure-clipboard behavior.
- Reuse Material 3/Jetpack capability before adding libraries.
- Keep changes small and reviewable.

Do not:

- Replace Redux/navigation architecture.
- Replace the repository or KDBX model.
- Create a second database/persistence layer.
- Add accounts/cloud/sync/OCR/typed vault items.
- Promote biometric unlock without dedicated review.
- Add a custom design-system framework.
- Add dependencies merely for visual effects.
- Add an external KDBX open/import choice.

## 3. Product Identity

`RAHSA` is the selected product name. The supplied blue shield with white R monogram and padlock is the primary mark.

Brand identity must be visible at key entry/identity moments but must not compete with credential content or security-critical actions.

Prepare replaceable identity slots for:

- Launch/splash app mark.
- First-launch/Auth app mark + product name.
- Empty-vault identity moment if useful.
- About identity.

Operational screens should express identity mainly through color, typography, spacing, icon style, and navigation rather than repeated logos.

Do not redraw or reinterpret the approved RAHSA mark without an explicit brand revision.

## 4. Global Navigation and Lock State

Top level:

```text
Vault | Generator | Settings
```

Rules:

- Bottom navigation only on Vault, Generator, Settings.
- Add credential Extended FAB only on Vault.
- Manual Lock is a top-app-bar action on top-level operational screens.
- Detail/Edit/Auth/sub-settings/contextual sheets hide bottom navigation.
- Back from Generator/Settings returns to Vault.
- Settings children return to Settings.
- Manual/background lock clears sensitive transient state and routes to Auth.
- Successful re-unlock always returns to Vault, not the previous sensitive destination.

Durable vs transient:

- Vault contents: durable.
- Generator settings: durable.
- Search query across Detail → Back: preserve when practical.
- Generated password: not durable.
- Reveal state: not durable.
- Unsaved form/search/dialog/destination after lock: not durable.

## 5. Auth / First Launch / Unlock

Launch routing:

```text
vault.kdbx missing → CreatePassword
vault.kdbx exists  → Login
```

No Create/Open choice and no external vault picker.

Keep existing `CreatePassword → ConfirmPassword → createVault` state flow.

First launch:

```text
[app mark]
KeyPass
Welcome / create your master password
supporting trust copy
Master password field
Continue
```

Confirm:

- Standard `arrow_back`.
- Confirm password field.
- Inline mismatch error.
- `Create vault` → disabled `Creating vault...`.

Returning unlock:

- App mark + product name.
- Master password OutlinedTextField.
- `visibility` / `visibility_off`.
- Password hint as a small text action only when a hint exists.
- `Unlock` → disabled `Unlocking...`.
- Wrong password inline.
- No biometric CTA.
- No skeleton before unlock.

Corrupt/unreadable vault gets a dedicated non-destructive state only if current exception signals support a reliable localized distinction. Never overwrite/create after open failure.

## 6. Vault / Dashboard

```text
Scaffold
├── TopAppBar: Your Vault + Lock
├── Search field + Sort
├── Credential list / Empty / Loading
├── Extended FAB: Add credential
└── NavigationBar: Vault | Generator | Settings
```

Rules:

- Reuse current search/sort source of truth.
- Keep search as a simple OutlinedTextField.
- Prefer Material list semantics + subtle divider/surface over a large Card per credential.
- Candidate leading visual: ~40dp tonal initial circle; no favicon database/network work.
- List title one line + ellipsis.
- Search non-secret metadata only; never password.
- Empty state: small meaningful icon, concise copy, Add CTA.
- Static placeholder only if list latency is perceptible; no shimmer.

Search Back behavior: keyboard closes first; then query clears; normal Vault Back exits. Preserve query/results through Detail → Back where practical.

## 7. Credential Detail

Principle: **view first → actions available → edit on request**.

```text
← Credential details                         Edit

Credential title (2–3 lines max)

Username
value                                      Copy

Password
masked value                       Reveal  Copy

Website
value                              Open if supported

Notes

Delete credential
```

Rules:

- Stable app-bar title `Credential details`.
- Actual credential title is content.
- Read-only values use label + value + optional action, not read-only OutlinedTextField.
- Password masked by default.
- Keep existing secure copy behavior.
- `open_in_new` only if existing/lightweight behavior is available.
- Delete is low-prominence destructive TextButton + AlertDialog.
- No bottom navigation.
- Lock clears reveal/dialog and routes via Auth to Vault.

## 8. Create / Edit

```text
← Add credential / Edit credential

Title *
Username
Password                      Reveal
Generate   Options
Website
Notes

Save credential / Save changes
```

Rules:

- Standard TopAppBar; no delete in Edit and no generator top-bar icon.
- No copy buttons on editable fields.
- OutlinedTextField.
- Title required/single-line; candidate max ~100.
- Username/password/website optional and single-line.
- Notes optional/multiline.
- Username is not email-only keyboard.
- Save disabled when title blank.
- Use normal Material validation; no validation framework.
- Full-width filled Save button, not icon-only FAB.
- Save → disabled `Saving...`, reusing current `isSaving`/duplicate-save guard.
- Save failure keeps input.

Generator:

- Quick `Generate` reuses current generator/config.
- `Options` opens contextual Material 3 bottom sheet.
- One shared generator engine/config.
- No profiles.

Unsaved changes:

- Untouched new form → Back directly.
- Dirty new/edit form → `Discard changes?`.
- Reuse edit baseline.
- Lock may discard unsaved state; security wins.

Create success should land on newly created Detail using the smallest localized ID/callback/navigation change.

## 9. Password Generator

Shared controls:

```text
Password length
[-] value [+]
Slider
Uppercase
Lowercase
Numbers
Symbols
Regenerate
```

Contextual wrapper:

- `Use password`.

Standalone wrapper:

- Top-level destination + Lock + bottom navigation.
- Masked generated value + Reveal.
- `Copy password`.

Rules:

- Load persisted settings and immediately generate a fresh password on entry.
- Settings changes may regenerate automatically if simple.
- At least one category remains enabled.
- Remove blank-spaces control from refreshed UI.
- Remove individual-symbol matrix from refreshed baseline.
- Generated value is never persisted and is cleared on leave/lock.
- Standalone Copy uses existing secure clipboard path + safe Snackbar, not a separate helper/Toast.
- Candidate length UX: 8–64, default ~20; verify current constants/tests before coding.

## 10. Settings

```text
TopAppBar: Settings + Lock

SECURITY
Password hint                            >
Auto-lock                            [switch]

HELP & ABOUT
Send feedback                           >
Share KeyPass                            >
About                                   >

KeyPass version x.x.x

Bottom navigation
```

Rules:

- Remove Settings search and collapsible section cards.
- Password Hint reuses existing child flow; do not show hint contents in the row.
- Auto-lock remains current binary on/off; no timeout choices.
- Generator itself becomes the single place to edit generator config; remove duplicate password-length setting.
- Do not promote biometric controls. Existing code/preferences can remain pending dedicated review.
- Reuse feedback/share/about.
- Version is low-emphasis footer.
- No global Save.
- No theme/account/cloud/sync UI.

## 11. Visual System

Character:

```text
dark-only
Android-native
Material 3
calm
secure
minimal
recognizable
high readability
low visual noise
```

Minimal must not mean visually anonymous.

Avoid pure black everywhere, neon security styling, glassmorphism, decorative gradients everywhere, excessive cards/icons, and custom animation systems.

Color direction:

- Layered charcoal surfaces.
- Calm, recognizable blue primary for action/selection.
- Restrained warm amber/gold brand accent for identity moments only.
- Dedicated semantic error color.
- No Dynamic Color initially.
- Exact hex values remain candidates until contrast/daylight testing on physical device.

Typography:

- Material/platform sans-serif for normal UI.
- Monospace only for secret/generated-password values when useful.
- Material typography roles; no auto-shrink for long titles.

Spacing:

```text
4 / 8 / 12 / 16 / 24 / 32 dp
```

Typical horizontal screen padding: 16dp.

Use Material shapes/components first. Do not wrap every credential, setting section, field, app version, or entire Generator in Card.

## 12. Component Conventions

Buttons:

- Filled: primary completion.
- Outlined/tonal: secondary (`Regenerate`, `Generate`).
- TextButton: tertiary/cancel/destructive.
- IconButton: familiar compact actions only.

Editable vs read-only:

```text
editable → OutlinedTextField
read-only → label + value + optional action
```

Loading:

- Action loading → small indicator + action text inside button.
- Content loading → small static placeholder only when needed.
- Security boundary → explicit `Unlocking...`.
- No shimmer dependency.

Snackbar must be concise and never contain secret values.

AlertDialog only for decisions such as Delete and Discard Changes; dismissal cancels destructive action.

Motion uses Material defaults only unless motion communicates a necessary state change.

## 13. Icon System

Direction: **Material Symbols Rounded**.

Do not add `material-icons-extended` merely for the icon set. Prefer a small vector set or retained equivalent icons during incremental migration.

| Function | Symbol |
|---|---|
| Vault | `shield_lock` candidate |
| Generator | `password` |
| Settings | `settings` |
| Manual lock | `lock` |
| Back | `arrow_back` |
| Search | `search` |
| Clear | `close` |
| Sort | `sort` |
| Add | `add` |
| Copy | `content_copy` |
| Reveal/Hide | `visibility` / `visibility_off` |
| Open website | `open_in_new` |
| Delete | `delete` |
| Regenerate | `refresh` |
| Decrease | `remove` |
| Feedback | `feedback` |
| Share | `share` |
| About | `info` |
| Child navigation | `chevron_right` |

Prefer text `Edit` on Credential Detail.

Bottom-nav labels stay visible. Typical action icon ~24dp inside a touch target of at least 48dp.

## 14. Accessibility

Accessibility is acceptance criteria, not final polish.

Required:

- Interactive targets at least 48dp where applicable.
- Localized meaningful descriptions for icon-only actions.
- Decorative icons use null semantics.
- Actions not distinguished by color alone.
- Switch state readable to accessibility services.
- Contrast verification.
- Larger font settings do not hide critical controls.
- Long titles do not hide actions.
- Passwords masked by default.
- Basic TalkBack pass.

## 15. Out of Scope / Future

Not part of this UI rollout:

- Biometric quick-unlock.
- OCR/camera capture.
- Cloud/online sync.
- Accounts.
- Typed vault items (PIN, Wi-Fi, bank account, secure note, etc.).
- Tags/categories expansion.
- Passphrase mode.
- Generator history/profiles.
- External KDBX chooser/import/export.
- Light theme/theme picker.
- Play Store release work.

## 16. Brand Decision Gate

After UI implementation/pilot + UX validation, STOP before public/store release preparation.

Resolve:

1. Final product name.
2. Trademark/name clearance in intended markets.
3. Final app icon/logo and identity assets.
4. Final Android `applicationId` / package identity strategy.
5. Signing/store identity.
6. Store-facing name/listing identity.
7. Upstream MIT attribution and third-party license presentation.

Current decision:

```text
Product name: RAHSA
Primary mark: approved and integrated
Release identity: blocked pending B002, B004, B005, and B006
```

This gate is mandatory.
