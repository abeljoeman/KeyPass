# RAHSA Implementation Kit — T127

TASK_ID: T127
KIT_STATUS: READY
PREPARED_AGAINST: ba1e86e6580bc6a3b07d1094692a8a98f4b641b5
RISK_CLASS: NORMAL
MODEL_RECOMMENDATION: Terra
REASONING_RECOMMENDATION: medium

## Objective

Implement only `T127 — Add Create Vault unrecoverability acknowledgment` from `TASKS.md`.

The Create New Vault flow must show the approved unrecoverability warning after password confirmation, require an **ephemeral swipe acknowledgment**, and enable a **separate explicit Create Vault action** only after the confirmation password is valid and acknowledgment is complete.

The swipe itself must never create the vault. TalkBack/accessibility must have an equivalent intentional acknowledgment action. Do not add a T127 recovery marker, dependency, or storage redesign.

## Source of truth

- `PRD.md`: Phase 13 Create Vault unrecoverability requirements (`P13-FR-010..015`).
- `TSD.md`: §9 `Create-vault recovery acknowledgment — T127 / ADR 0012`.
- `docs/adr/0012-create-vault-recovery-acknowledgment.md` — Accepted.
- `docs/THREAT_MODEL.md`: T13 and Phase 13 validation gates.
- `TASKS.md`: T127 definition and acceptance criteria.

If this kit conflicts with any source above, the higher-level source wins.

## Prepared against

- Branch: `prototype/v0.2`
- Checkpoint: `ba1e86e6580bc6a3b07d1094692a8a98f4b641b5` — `docs: record T126 completion checkpoint`
- T126 implementation checkpoint: `e69ee8021e1c50c1ae244be2ad25790f0fd31dc0`
- Expected working tree when execution starts: clean
- Expected execution gate before activation: `PLANNING_FREEZE / NONE / NONE`

## Verified current-source findings

### Create Vault UI/state

`app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/AuthScreen.kt`

- `AuthScreen(AuthState.ConfirmPassword)` is the existing confirmation screen immediately before vault creation.
- Password input is already ephemeral (`remember`, not persistent storage).
- `authenticationInProgress` is local Compose state keyed to the auth state.
- Back from `ConfirmPassword` returns to `CreatePassword` while creation is not in progress.
- No T127 acknowledgment state exists yet.

Use local non-saveable Compose state for `recoveryWarningAcknowledged` (or equivalent). Prefer keeping it in the auth UI flow; do **not** promote it into persisted settings or a recovery/transaction marker.

### Explicit create action / duplicate guard

`app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/ButtonBar.kt`

- `ConfirmPassword` currently calls `vaultRepository.createVault(...)` only from the explicit button action.
- `authenticationInProgress` already disables the button and has a logic-level early return for duplicate execution.
- The existing `CharArray` cleanup in `finally` must be preserved.
- The existing vault creation repository path is authoritative and must be reused.

Do not move vault creation into the swipe callback.

### Material 3

Current app dependencies already include Compose Material 3 `1.4.0-alpha14`; no dependency addition or upgrade is required for T127.

Material 3 provides the `SwipeToDismissBox` / `rememberSwipeToDismissBoxState` family in the existing artifact. Use an API shape compatible with the repository's pinned version. If the exact preferred call shape differs, use the closest existing Material 3/platform/project swipe primitive or established pattern without adding a new gesture framework or dependency.

### Strings

`app/src/main/res/values/strings.xml` contains the current Create/Confirm Vault copy but no T127 warning strings.

Approved warning content:

> Master Password tidak dapat dipulihkan. Jika Anda lupa Master Password, RAHSA tidak dapat membuka atau memulihkan data di vault ini. Pastikan Anda dapat mengingat Master Password Anda.

Approved swipe label:

> Geser untuk memahami dan melanjutkan

Keep product name **RAHSA**. Do not rename legacy package identifiers.

## Expected / allowed files

Primary allowed files:

- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/AuthScreen.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/ButtonBar.kt`
- `app/src/main/res/values/strings.xml`

A narrowly scoped new auth component is allowed when it improves isolation/testability, for example:

- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/RecoveryAcknowledgment.kt`

Focused tests are allowed under:

- `app/src/test/java/com/yogeshpaliyal/keypass/ui/auth/**`
- `app/src/androidTest/java/com/yogeshpaliyal/keypass/ui/auth/**`

`TASKS.md` may be changed only for the normal execution-gate/completion workflow.

Any other application/source file requires a concrete T127-scope justification. In particular, avoid changing `VaultRepository`, `KotpassVaultRepository`, Redux architecture, Gradle/dependencies, or persistence merely to implement acknowledgment.

## Required behavior / invariants

1. T127 applies only to `AuthState.ConfirmPassword` / Create New Vault, not normal Login/unlock.
2. Warning is visible before final creation.
3. `recoveryWarningAcknowledged` starts `false` whenever the confirmation flow is newly entered.
4. Acknowledgment is **ephemeral**:
   - do not use DataStore/SharedPreferences/files;
   - do not add it to a recovery marker;
   - do not preserve it solely through `rememberSaveable`;
   - Back/leave/re-enter/process recreation must require acknowledgment again.
5. Completing the swipe changes acknowledgment state only.
6. Swipe completion must **never** invoke `createVault`, dispatch Home navigation, or start another authoritative mutation.
7. TalkBack/accessibility exposes an equivalent explicit semantic action that transitions the same acknowledgment state.
8. Final Create Vault remains a separate explicit button action.
9. For `ConfirmPassword`, final Create Vault is enabled only when:
   - no authentication/create operation is in progress;
   - confirmation password matches the password carried by `AuthState.ConfirmPassword` and is otherwise valid under existing rules; and
   - acknowledgment is complete.
10. Existing CreatePassword and Login button behavior must remain unchanged unless a minimal shared enablement refactor is required.
11. Existing `authenticationInProgress` single-flight behavior must remain or be strengthened narrowly; do not replace it with a generic transaction framework.
12. Existing `vaultRepository.createVault(...)` path and password `CharArray` cleanup remain authoritative.
13. Before explicit Create starts, abandoning the flow mutates no vault state.
14. After explicit Create starts, Back/navigation is not a second create/cancel trigger; existing in-progress behavior remains authoritative.
15. No `CREATE_VAULT_IN_PROGRESS`, no T127 transaction/recovery marker, and no new persistence/storage architecture.
16. No new third-party dependency, gesture framework, recovery feature, or password policy.

## Accessibility guidance

Do not make physical dragging the only operable path.

The acknowledgment UI should expose meaningful semantics, including an intentional action label equivalent to the approved swipe action and a state description that distinguishes not-yet-acknowledged vs acknowledged state.

A narrow Material 3 swipe component may be wrapped with Compose semantics/custom accessibility action so TalkBack can perform the same state transition without synthesizing a drag gesture.

The semantic action must only acknowledge; it must not create the vault.

## Testing expectations

Add focused automated coverage at the narrowest practical seam. Prefer isolated tests over bootstrapping the entire application when possible.

Required automated assertions where practical:

- acknowledgment starts false / Create action is not enabled solely by matching passwords;
- matching confirmation + acknowledgment enables the explicit Create action;
- mismatch keeps Create disabled even after acknowledgment;
- swipe/acknowledgment callback changes only acknowledgment state and does not invoke vault creation;
- accessibility semantics exposes an equivalent intentional acknowledgment action;
- acknowledgment UI/state is shown only for Create Vault confirmation, not Login;
- existing duplicate-create guard/in-progress disablement remains intact.

A small pure enablement helper may be introduced only if it makes unit coverage substantially simpler; keep it local to auth UI and do not build a generic validation framework.

## Prepared artifacts

- Patch: none
- Apply helper: none
- Verify helper: none

Reason: T127 is a focused Compose interaction/lifecycle/accessibility change. Direct Codex implementation against the inspected source is safer than a brittle prepared gesture patch and still requires independent UI/semantics review.

## Execution / review steps

After T127 is explicitly activated:

```powershell
python scripts/governance_preflight.py --implementation T127
git status -sb
git log -5 --oneline --decorate
```

Then:

1. Read `AGENTS.md`, `TASKS.md`, this kit, TSD §9, ADR 0012, and Threat Model T13.
2. Re-read current `AuthScreen.kt` and `ButtonBar.kt`; verify assumptions still match.
3. Implement only T127 within the allowed scope.
4. Review the full diff for accidental persistence/storage/navigation changes.
5. Run `git diff --check`.
6. Run focused tests and the free debug build.
7. Do not start T128.

If the current Material 3 API differs from the expected swipe API shape, adapt only within the approved existing-dependency pattern. Do not upgrade/add dependencies solely to get a preferred component API.

## Build / automated validation

Use the narrowest matching test task based on the tests added, then build:

```powershell
.\gradlew.bat :app:testFreeDebugUnitTest
.\gradlew.bat :app:assembleFreeDebug
```

If instrumentation/Compose UI tests are added and an emulator/device is available in the execution environment, run the focused connected test as well. If not available, report that explicitly rather than pretending it ran.

Do not weaken or delete an existing test merely to make T127 pass.

## Physical / TalkBack validation gate

T127 requires physical-device accessibility smoke validation. This is an intentional exception to the cloud-first workflow.

On the Samsung Galaxy A11 (or another physical Android device), validate at minimum:

1. Start with no vault and enter a Master Password.
2. Confirm the same password.
3. Verify the unrecoverability warning is visible.
4. Verify Create Vault is disabled before acknowledgment.
5. Complete the visual swipe; verify it acknowledges but does **not** create/navigate automatically.
6. Verify explicit Create Vault becomes enabled only when password confirmation is valid.
7. Back out and re-enter confirmation; acknowledgment must be false again.
8. Enable TalkBack and use the semantic acknowledgment action without relying on physical dragging.
9. Verify TalkBack acknowledgment does not auto-create the vault.
10. Tap Create Vault explicitly once and verify normal creation succeeds without duplicate execution.

If Codex Cloud cannot perform this physical validation, it must **not claim T127 fully complete**. It may finish implementation + automated validation and report `READY FOR PHYSICAL VALIDATION`, while keeping T127 active. Final task completion/gate reset occurs only after the required device validation is reported successful.

## Out of scope

- Recovery key/account/server recovery.
- Repeating warning on every unlock.
- Password policy/complexity redesign.
- Master Password change flow (T129).
- LKG/storage foundation (T128).
- Backup/restore/biometric work.
- New transaction/recovery marker for Create Vault.
- Redux/navigation/storage architecture rewrite.
- Custom low-level gesture framework.
- New dependency or Material 3 upgrade solely for this feature.

## Known stale-kit conditions

Refresh this kit before use if any of these become true:

- `AuthScreen.kt` or `ButtonBar.kt` semantics materially change after `ba1e86e...`.
- Create Vault moves away from `AuthState.ConfirmPassword`.
- `vaultRepository.createVault(...)` ownership changes.
- Material 3 dependency/version changes materially.
- T127/ADR 0012/TSD §9/Threat Model T13 requirements change.
- Another implementation task modifies the auth flow before T127 executes.

Documentation-only kit/activation commits after the prepared-against checkpoint do not make the kit stale by themselves when the inspected application source remains unchanged.

## Completion

T127 is complete only after automated validation **and** required physical/TalkBack smoke validation pass.

After successful validation:

- mark only T127 complete;
- create one focused final checkpoint for the validated task state;
- reset `TASKS.md` to `PLANNING_FREEZE / NONE / NONE`;
- report the checkpoint;
- stop;
- do not prepare or start T128 from the implementation run.
