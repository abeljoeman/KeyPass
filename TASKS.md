# Implementation Tasks — Android Password Manager Prototype

**Status:** Active backlog  
**Inputs:** `PRD.md`, `TSD.md`, `ENGINEERING_PRINCIPLES.md`

## Execution Rules

- Complete tasks in order unless marked `[P]`.
- Keep each change narrowly scoped.
- Do not add features not present in `PRD.md`.
- Every task should end with a build/test check.
- If implementation conflicts with `ENGINEERING_PRINCIPLES.md`, stop and resolve the conflict before proceeding.

## Phase 0 — Documentation and Baseline

- [ ] **T001** Review and approve `PRD.md`.
- [ ] **T002** Review and approve `TSD.md`.
- [ ] **T003** Review ADRs in `docs/adr/`.
- [ ] **T004** Review `docs/THREAT_MODEL.md`.
- [ ] **T005** Confirm KeyPass and Kotpass license notices required for reuse/distribution.

**Checkpoint:** scope and architecture are explicit.

## Phase 1 — Establish Working Base

- [x] **T010** Fork or create a working branch from `yogeshpaliyal/KeyPass`.
- [x] **T011** Build the unmodified base project with the repository Gradle Wrapper.
- [x] **T012** Install and run the base APK on a physical Android device.
- [x] **T013** Record the exact build command and environment prerequisites in project README.
- [x] **T014** Create a clean baseline commit/tag before prototype modifications.

**Acceptance:** clean checkout builds and runs.

## Phase 2 — Reduce Scope

- [x] **T020** Identify features outside prototype PRD.
- [x] **T021** Remove/disable TOTP UI and flows.
- [x] **T022** Remove/disable passkey flows.
- [x] **T023** Remove/disable cloud/network-dependent features if present.
- [x] **T024** Remove/disable backup/import/export flows not required for prototype.
- [x] **T025** Remove/disable Autofill Service for v0.1.
- [x] **T026** Remove unused dependencies introduced only by removed features.
- [x] **T027** Verify app still builds and launches.

**Checkpoint:** retained app matches prototype scope.

## Phase 3 — Kotpass Integration

- [x] **T030** Add Kotpass dependency using a pinned stable version.
- [x] **T031** Create `Credential` application model if not already suitable.
- [x] **T032** Create explicit KDBX ↔ `Credential` mapper.
- [x] **T033** Add mapper unit tests using representative values.
- [x] **T034** Implement `VaultRepository` interface.
- [x] **T035** Implement `KotpassVaultRepository`.
- [x] **T036** Add test fixture KDBX vaults where license-safe.
- [x] **T037** Verify repository can open a known test vault.
- [x] **T038** Verify wrong password fails closed.

**Checkpoint:** repository can read a KDBX vault independently of UI.

## Phase 4 — Vault Lifecycle

- [x] **T040** Implement create-vault flow.
- [x] **T041** Implement unlock-vault flow.
- [x] **T042** Implement explicit lock action.
- [x] **T043** Ensure master password is not persisted in plaintext.
- [x] **T044** Clear accessible decrypted application state on lock.
- [x] **T045** Implement simple background/timeout lock behavior.
- [x] **T046** Verify vault reopens correctly after process/app restart.

**Checkpoint:** vault lifecycle works end-to-end.

## Phase 5 — Credential CRUD

- [x] **T050 [P]** Adapt credential-list UI.
- [x] **T051 [P]** Adapt credential-detail UI.
- [x] **T052 [P]** Adapt add/edit credential form.
- [x] **T053** Connect list UI to `VaultRepository`.
- [x] **T054** Implement create credential.
- [x] **T055** Implement edit credential.
- [x] **T056** Implement delete credential.
- [x] **T057** Verify every mutation persists after vault reopen.
- [x] **T058** Handle cancel/edit navigation without unintended writes.

**Checkpoint:** full CRUD persists through KDBX.

## Phase 6 — Search and Password Generator

- [x] **T060 [P]** Reuse/adapt existing search behavior.
- [x] **T061 [P]** Reuse/adapt existing password generator.
- [x] **T062** Add search tests.
- [x] **T063** Verify password generator does not log generated values.

## Phase 7 — Security Hardening for Prototype

- [x] **T070** Verify sensitive screens use secure-screen protection.
- [x] **T071** Audit logging statements for username/password/master-password leakage.
- [x] **T072** Review clipboard behavior.
- [x] **T073** Implement clipboard clearing if practical for target Android versions.
- [x] **T074** Verify no unintended plaintext credential files are created.
- [x] **T075** Verify no INTERNET permission is present unless explicitly justified.
- [x] **T076** Verify corrupted vault is not silently overwritten.
- [x] **T077** Run threat-model checklist and record findings.

## Phase 8 — Prototype Test Pass

- [x] **T080** Execute `docs/TEST_PLAN.md` on a physical device.
- [x] **T081** Test with at least 20 credentials.
- [x] **T082** Test wrong master password.
- [x] **T083** Test app restart and vault reopen.
- [x] **T084** Test background/foreground lock behavior.
- [x] **T085** Test process kill and recovery.
- [x] **T086** Capture known limitations in `docs/TEST_RESULTS.md`.
- [x] **T087** Tag prototype release `v0.1-prototype`.

## Definition of Done for v0.1

Prototype is done only when:

- [x] Vault creation works.
- [x] Vault unlock works.
- [x] Wrong password fails safely.
- [x] Credential CRUD persists.
- [x] Search works.
- [x] Password generator works.
- [x] Manual lock works.
- [x] Basic background/timeout lock works.
- [x] Sensitive data is not found in normal logs during test flows.
- [x] Prototype runs without a backend.
- [x] Physical-device test checklist passes or failures are explicitly documented.

## Phase 9 — UI/UX Source of Truth

- [x] **T090** Complete screen-by-screen UX-flow review for the retained prototype capability set.
- [x] **T091** Complete component mapping for Vault, Credential Detail, Create/Edit, Auth, Generator, and Settings.
- [x] **T092** Define dark-only visual direction, icon rules, loading/error conventions, accessibility baseline, and replaceable brand layer.
- [ ] **T093** Apply and review the consolidated source-of-truth documentation patch.
- [ ] **T094** Confirm the implementation task sequence below before starting UI code changes.

**Checkpoint:** UI implementation does not begin until the consolidated documentation is accepted in the repository.

## Phase 10 — UI Foundation

- [ ] **T100** Implement fixed dark Material 3 theme and dark startup/window treatment without changing product architecture.
- [ ] **T101** Replace global Roboto Mono UI typography with Material/platform sans-serif roles; retain monospace only where useful for secrets.
- [ ] **T102** Establish the small Material Symbols icon set and localized accessibility descriptions without adding a mega icon dependency.
- [ ] **T103** Add only justified reusable application-specific UI components needed by multiple approved screens.
- [ ] **T104** Verify the foundation on emulator and Samsung Galaxy A11 before broad rollout.

## Phase 11 — Approved Screen Rollout

- [ ] **T110** Refine Auth/Create/Confirm/Unlock using existing `AuthState` and repository operations; add explicit button loading states.
- [ ] **T111** Refine Vault/Dashboard, including top-app-bar lock, search/sort, list rows, empty state, Extended FAB, and 3-item bottom navigation.
- [ ] **T112** Refine Credential Detail into a read-first presentation with safe copy/reveal and low-prominence delete flow.
- [ ] **T113** Refine Create/Edit form, contextual generator access, validation, saving feedback, and lightweight unsaved-change confirmation.
- [ ] **T114** Refine Standalone Generator using the existing engine/config, automatic fresh generation, simplified options, and secure copy behavior.
- [ ] **T115** Refine Settings into simple sections/list rows; preserve Password Hint/Auto-lock/Help/About and do not promote biometric UI.
- [ ] **T116** Ensure global Back/lock/navigation state follows `docs/UX_UI_BASELINE.md`.

## Phase 12 — UX Feedback, Accessibility, and Regression

- [ ] **T120** Standardize Snackbar, inline error, AlertDialog, and operation-loading behavior.
- [ ] **T121** Add static credential placeholders only where device testing shows perceptible content loading; do not add shimmer.
- [ ] **T122** Validate touch targets, localized icon semantics, font scaling, and a basic TalkBack pass.
- [ ] **T123** Validate long-title, no-results, dirty-form, wrong-password, save/delete failure, and lock-during-transient-state flows.
- [ ] **T124** Run relevant unit tests and full regression build/test pass.
- [ ] **T125** Run physical-device UX validation on Samsung Galaxy A11 and record findings.

## Brand Decision Gate — Required Before Public Release Preparation

Do not proceed from UX validation into public/store release preparation until all items below are resolved:

- [ ] **B001** Select the final product name; `KeyPass` remains the working development name until then.
- [ ] **B002** Perform appropriate trademark/name clearance for intended markets.
- [ ] **B003** Finalize app icon/logo and brand assets.
- [ ] **B004** Finalize Android `applicationId` / package identity strategy.
- [ ] **B005** Finalize signing/store identity and store-facing product name.
- [ ] **B006** Confirm upstream/open-source attribution and third-party license presentation.

**Hard gate:** stop and explicitly review branding here before public release work.
