# T129 JIT Implementation Kit — Real Master Password Re-key + Crash-Consistent Finalization

TASK_ID: T129
KIT_STATUS: READY
PREPARED_AGAINST: a06995000d46d309f8b9cd2f3dd03752fd10b87f
BASELINE_BRANCH: prototype/v0.2
TARGET_SCOPE: Phase 13 T129 only
RISK_CLASS: SECURITY_SENSITIVE
MODEL_RECOMMENDATION: GPT-5.6 Sol
REASONING_RECOMMENDATION: HIGH
AUTHORITATIVE_EXECUTION_ENVIRONMENT: Codex on local Windows repository G:\Projects\KeyPass
AUTHORITATIVE_REMOTE: GitHub abeljoeman/KeyPass branch prototype/v0.2
PHYSICAL_VALIDATION_DEVICE: Samsung Galaxy A11 / SM-A115F

## 1. Governance state

This kit is READY for T129 only. READY does **not** activate implementation.

At preparation time the execution gate remains:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

T129 MUST NOT be implemented until the owner explicitly authorizes activation and governance is deliberately changed to exactly:

- `EXECUTION_STATUS: ACTIVE_IMPLEMENTATION`
- `ACTIVE_TASK: T129`
- `ACTIVE_KIT: implementation-kits/T129/README.md`

Until then, Codex may inspect/read but must not edit application implementation for T129.

Once activated, **Codex running against the local Windows checkout is the single writer** for T129 implementation, automated validation, Android build, and physical smoke validation. Do not run a concurrent Codex Cloud implementation or overlapping manual edits.

GitHub `prototype/v0.2` remains authoritative remote history. The local writer must synchronize from remote before implementation and push only owner-authorized focused checkpoints.

### Owner-review hold

For T129, after implementation/tests/build/device validation succeed, Codex MUST create a focused implementation checkpoint and hand it back for owner/ChatGPT review **without closing governance**. Leave:

- `EXECUTION_STATUS: ACTIVE_IMPLEMENTATION`
- `ACTIVE_TASK: T129`
- `ACTIVE_KIT: implementation-kits/T129/README.md`

until owner review explicitly accepts the implementation. Do not mark T129 COMPLETE or reset the gate before that acceptance. This task-specific hold prevents premature closure of a security-sensitive checkpoint.

## 2. Sources of truth

Read and obey, in authority order:

- `ENGINEERING_PRINCIPLES.md`
- `PRD.md` Phase 13, especially P13-FR-020..028
- `TSD.md` §10, §11, §16
- `docs/adr/0005-data-safety-kdbx-lkg-saf.md`
- `docs/adr/0007-master-password-change-crash-consistency.md`
- `docs/THREAT_MODEL.md`, especially T14/T21 and related credential/recovery threats
- `TASKS.md` T129
- this kit

If this kit conflicts with a higher-level source, STOP and follow the higher-level source; report the mismatch rather than silently adapting security semantics.

## 3. Authoritative objective

Implement a **real KDBX Master Password change**, not a settings-only password edit.

The required operation is:

1. user enters current Master Password, new Master Password, confirmation, and optional prefilled/editable hint;
2. warning clearly states the old Master Password will stop working and RAHSA cannot recover/open the vault if the new password is forgotten;
3. user performs the existing intentional swipe/TalkBack acknowledgment; acknowledgment alone never starts re-key;
4. user separately presses the final `Change Master Password` action;
5. repository re-authenticates the supplied current password against the **on-disk active KDBX**, even though the session is already unlocked;
6. repository derives a re-keyed database using Kotpass `0.13.0` `KeePassDatabase.modifyCredentials`; no custom KDBX cryptography;
7. candidate is encoded to an app-private encrypted temporary KDBX and successfully decoded with the **new** credentials;
8. the verified old active KDBX is preserved as the canonical single LKG using the accepted T128/ADR 0005 ordering;
9. only then is the verified candidate promoted to active and the promoted active verified again;
10. successful verified promotion is the authoritative commit boundary: before commit old password/hint/security metadata remain authoritative; after commit new active/new password remain authoritative and are not rolled back merely because UI/finalization is interrupted;
11. post-commit finalization applies the retained/edited hint, invalidates prior biometric quick-unlock state only when such state was active/present, cleans narrow temporary/finalization state, and reconciles the live session when the process survives;
12. a post-commit restart completes only the required non-secret finalization idempotently before normal use.

On success, the live session remains unlocked if the process survives, the old Master Password fails to open active, and the new Master Password opens active.

## 4. Critical compatibility note with T128

The T128 persistence foundation currently validates ordinary credential mutations where the existing active and candidate use the same credentials. T129 is the first operation where:

- **old active/LKG uses the old/current credentials**; and
- **candidate/new active uses the new credentials**.

Therefore T129 is explicitly allowed to make the smallest necessary extension to the T128 safe-promotion seam so old-active verification uses current credentials while candidate/promoted-active verification uses new credentials.

Do not weaken T128 ordering. For re-key, the required storage sequence remains:

```text
verify current password against active
→ create re-keyed candidate with new credentials
→ encode candidate separately
→ decode/validate candidate with new credentials
→ verify old active with current credentials
→ old verified active → canonical LKG
→ candidate → active
→ decode/verify promoted active with new credentials
→ commit accepted
```

Regression tests must prove ordinary same-credential CRUD still follows T128 semantics.

## 5. Reuse findings already verified

### 5.1 Kotpass

Kotpass `0.13.0` exposes `KeePassDatabase.modifyCredentials { ... }`. Use this existing library path and the repository's existing `Credentials` / `EncryptedValue` primitives. Do not implement KDBX encryption, KDF, header mutation, or credential serialization locally.

### 5.2 T128 safe promotion

Reuse `KotpassVaultRepository` + `VaultFileOperations` and the accepted one-LKG ordering from final T128 checkpoint `34e6b19053aa0d2f3a7ed7381add175fb2d9e4b4`.

Do not create a second transaction mechanism for re-key.

### 5.3 Recovery acknowledgment

Reuse `ui/auth/components/RecoveryAcknowledgment.kt` from T127 for swipe and TalkBack semantics. T129 may parameterize it minimally only if necessary for T129 text/state reuse, but must not change T127 behavior, appearance, warning semantics, or accessibility acceptance.

### 5.4 Hint persistence

Existing user settings provide `passwordHint` and `setPasswordHint(...)`. Reuse that storage. Do not create a second hint store.

### 5.5 Legacy password setting is forbidden

`UserSettings` still contains legacy `dbPassword: String?`, and `SharedPreferenceUtils.kt` still exposes `setDatabasePassword(...)`. **T129 MUST NOT read, write, synchronize, repurpose, or persist the new/current Master Password through these legacy settings.** The KDBX credential is authoritative.

Do not delete/migrate this legacy field in T129 unless a higher-level approved requirement explicitly requires it; cleanup is separate technical debt.

### 5.6 Advisory strength estimator dependency review

The preferred estimator has been reviewed for this JIT kit:

- library/project: zxcvbn4j (`nulab/zxcvbn4j`)
- Maven coordinate: `com.nulab-inc:zxcvbn:1.9.0`
- reviewed release: `1.9.0` (2025-09-26)
- license: MIT
- intended use: local/offline **advisory-only** password strength feedback
- Android: upstream documentation states Android support
- security review: no package-specific advisory for the Maven artifact was identified during JIT review; unrelated npm-package advisories must not be conflated with this Java artifact

T129 may add this dependency if current Gradle/Android resolution succeeds. It must never become a validation gate or complexity policy: a weak score does not block Change Master Password when the approved non-empty + confirmation rules pass.

If `1.9.0` cannot resolve/build cleanly in the repository, or inspection reveals a material license/security/Android compatibility concern, STOP and report it. Do not invent a home-grown strength algorithm, silently choose another dependency, or turn strength into an enforcement rule.

## 6. Scope — IN

Implementation may cover only the minimum necessary for T129:

- one narrow `VaultRepository` operation for real Master Password change / re-key;
- Kotpass implementation using current-password on-disk reauthentication + `modifyCredentials`;
- T128 safe-promotion seam extension for distinct old vs new credentials;
- deterministic repository failure injection/tests for current verification, candidate validation, promotion, and rollback;
- one Change Master Password screen/state reachable from Settings while vault is unlocked;
- current/new/confirmation password fields, optional prefilled/editable hint, advisory strength, consequence warning, acknowledgment, explicit final action, error/progress/success state;
- logic-layer single-flight protection plus UI disablement while operation runs;
- reuse of current Auto-Lock/session behavior without redesign;
- smallest app-private non-secret re-key finalization marker/helper required for post-commit crash consistency;
- smallest startup hook necessary to replay pending **post-commit** finalization before normal use;
- post-commit hint application;
- conditional invalidation of existing biometric-enabled/quick-unlock state when present, without implementing T133;
- minimal success notification when previously active biometric state was invalidated; no biometric message when none existed;
- zxcvbn4j dependency wiring for advisory-only strength if compatible;
- corresponding focused repository/UI/finalization tests and resource strings.

## 7. Scope — OUT

Do NOT implement or broaden into:

- T130 Forgot Master Password / destructive reset;
- T131 external KDBX backup;
- T132 external KDBX restore;
- T133 Biometric Quick Unlock enable/disable/unlock flow or new Keystore architecture;
- recovery key/account/server recovery;
- password history, expiry, mandatory complexity, minimum-strength policy, reuse detection, breach lookup, or network password analysis;
- external-backup prerequisite;
- custom KDBX crypto/KDF/header implementation;
- KDBX schema migration;
- generic transaction/workflow/recovery framework;
- new navigation framework/state architecture;
- T127 cosmetic refinement or changed acknowledgment semantics;
- persistence of password drafts/acknowledgment;
- plaintext Master Password in DataStore/preferences/files/logs/marker/analytics;
- use of legacy `dbPassword` / `setDatabasePassword` as an authentication or persistence mechanism;
- silent LKG restore or any user-facing LKG recovery flow;
- opportunistic refactors unrelated to T129.

## 8. Expected / allowed files

### Primary repository/storage

- `app/src/main/java/com/yogeshpaliyal/keypass/vault/VaultRepository.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepository.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/vault/VaultFileOperations.kt` only if the existing deterministic seam needs a narrow T129-compatible extension
- `app/src/test/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepositoryTest.kt`

### Primary UI/navigation/settings wiring

- one new screen under `app/src/main/java/com/yogeshpaliyal/keypass/ui/` dedicated to Change Master Password, preferably `changeMasterPassword/ChangeMasterPasswordScreen.kt`
- one corresponding narrow Redux `ScreenState` under `app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/states/`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/settings/MySettingsFragment.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/nav/DashboardComposeActivity.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/nav/NavigationModel.kt` only if the established screen-state wiring requires it
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/RecoveryAcknowledgment.kt` only for minimal backward-compatible reuse parameterization; no T127 semantic/cosmetic change
- `app/src/main/res/values/strings.xml`
- directly corresponding existing locale resource files only when repository policy/build requires synchronized strings

### Finalization/settings

- `common/src/main/java/com/yogeshpaliyal/common/utils/SharedPreferenceUtils.kt` only for narrow existing-setting operations required by finalization; do not use `setDatabasePassword`
- `common/src/main/java/com/yogeshpaliyal/common/data/UserSettings.kt` only if a **non-secret** T129 finalization field is proven necessary; adding password/draft/ack fields is forbidden
- at most one small T129-specific app-private finalization marker/coordinator file under an existing appropriate app package
- at most one minimal startup integration point, expected to be `DashboardComposeActivity.kt` unless current inspection proves another existing startup hook is more correct
- directly corresponding unit/instrumentation test(s)

### Dependency wiring, only if zxcvbn4j is adopted

- `buildSrc/src/main/kotlin/Dependencies.kt`
- `app/build.gradle.kts`

### Kit correction

- `implementation-kits/T129/README.md` only for an authorized in-scope correction as defined below

Do not interpret this list as permission to touch every file. Change the smallest necessary set. If implementation needs a materially broader public API, new framework, new security storage design, or files owned by T130–T133, STOP.

## 9. Finalization-marker constraints

A finalization marker is allowed only because ADR 0007 explicitly permits a minimal app-private non-secret recovery/finalization marker.

The design MUST satisfy all of the following:

- no current/new/old Master Password, password-derived secret, decrypted credential/vault data, or acknowledgment draft;
- no generic transaction engine;
- enough non-secret state to distinguish whether post-commit finalization is legitimately required before applying hint/biometric cleanup after restart;
- replay is idempotent;
- pre-commit failure or abandoned UI cannot cause new hint/biometric metadata to become authoritative;
- after verified promotion/commit, process death must not cause speculative rollback of the new active vault;
- startup with a legitimately pending post-commit finalization completes only non-secret cleanup/finalization before normal use, then removes/settles the marker;
- marker/temp cleanup failure must not re-key again;
- the marker may retain only non-secret finalization payload strictly necessary by ADR 0007 (for example the intended password hint if the implementation treats the user-visible hint as non-secret metadata); it must not retain any password input.

A non-secret encrypted-file fingerprint/hash or phase identifier may be used if required to distinguish the authoritative storage state without storing credentials. Do not add a general journal.

If crash-consistent post-commit finalization cannot be implemented narrowly without secret persistence or a generic transaction framework, STOP and return for owner architecture review.

## 10. Biometric invalidation boundary

Current source retains legacy `UserSettings.isBiometricEnable` / `setBiometricEnable(...)`, while T133 secure Biometric Quick Unlock is not implemented yet.

T129 must:

- inspect current source before coding to identify any actually active biometric/wrapped-state/Keystore artifacts;
- if prior biometric quick-unlock state is genuinely active/present, invalidate/disable only that existing state after commit and notify the user once;
- if only the legacy enable flag exists, clear it after commit as appropriate without inventing T133 secure-state structures;
- if no biometric state was active, create no state and show no irrelevant biometric invalidation message;
- never implement biometric enrollment, prompt, wrapping, Keystore key creation, auto-prompt, or unlock fallback here.

## 11. UI and lifecycle invariants

Before the explicit final action starts:

- current/new/confirmation passwords remain ephemeral;
- acknowledgment starts false every fresh entry;
- Back/navigation away, recreation, process death, force-close, or abandoned background flow must not persist password drafts or acknowledgment;
- reopening starts all password fields empty and acknowledgment false;
- current persisted hint may be prefilled again;
- no authoritative vault/hint/biometric mutation occurs.

Final action enablement requires only:

- current password non-empty;
- new password non-empty;
- confirmation matches new password;
- consequence acknowledgment true;
- operation not already in progress.

Strength is advisory and MUST NOT affect enablement.

Swipe/TalkBack acknowledgment only changes acknowledgment state. It never calls re-key.

Once the explicit operation starts:

- Back/Home/navigation is not a transaction cancel;
- operation is single-flight;
- normal existing Auto-Lock behavior remains unchanged;
- UI completion failure after commit cannot make old password authoritative again.

## 12. Secret-handling constraints

- Never log current/new/confirmation password, `Credentials`, decrypted vault contents, or marker payload that might expose user secrets.
- Never write current/new password to DataStore, SharedPreferences, files, SavedStateHandle, Redux state persisted across process recreation, marker, analytics, clipboard, or `UserSettings.dbPassword`.
- Keep credentials in-process only as long as needed for verification/re-key/promotion; clear mutable password buffers/byte arrays as practical.
- Do not introduce persistent password caching.
- zxcvbn evaluation is local only; no network/API call sends password text off-device.
- Error behavior must not expose an LKG recovery oracle.

## 13. Repository tests required

Add deterministic automated coverage proving at minimum:

1. **Wrong current password** — reauthentication fails before candidate/promotion; active + existing LKG remain byte-for-byte unchanged; hint and biometric/finalization state remain unchanged.
2. **Successful real re-key** — active opens with new password and rejects old password.
3. **Credential-role proof** — resulting canonical LKG opens with the **old** password and active opens with the **new** password.
4. **Candidate validation failure** — injected failure leaves old active/LKG authoritative and no new password is accepted.
5. **Pre-promotion failure** — old active/password/hint/biometric remain authoritative.
6. **Promotion failure after old active is LKG** — T128 rollback restores old active and prior LKG deterministically; old password remains valid; new password is not accepted.
7. **Promoted-active verification failure** — rollback follows the accepted T128 safety semantics and does not leave ambiguous credential authority.
8. **Exactly-one LKG** — repeated prior normal mutation + re-key does not accumulate backup history.
9. **Same-credential regression** — existing credential create/update/delete persistence still passes T128 ordering/rollback behavior.
10. **Single-flight** — duplicate concurrent/rapid re-key invocation cannot execute two authoritative changes.
11. **Live-session success** — after successful re-key in a surviving process, repository remains usable/unlocked with the re-keyed in-memory database.
12. **No legacy password persistence** — re-key does not populate/update `UserSettings.dbPassword` or another plaintext password setting.

Use the existing `VaultFileOperations` seam for deterministic promotion failures rather than physical filesystem tricks.

## 14. Finalization / crash-boundary tests required

Add focused tests proving:

1. pre-commit failure does not apply edited hint, clear biometric state, or leave a marker that later finalizes as if commit happened;
2. successful commit followed by interruption before normal UI completion leaves the new active/new password authoritative;
3. restart with a legitimate pending post-commit marker completes hint application + conditional biometric invalidation + cleanup idempotently;
4. marker replay twice is harmless and does not re-key again;
5. marker contains no Master Password/decrypted-vault data;
6. an edited hint becomes persisted only after commit/finalization;
7. unchanged/retained hint remains correct after success;
8. when biometric state was active, success disables/invalidate existing state and surfaces the required informational result;
9. when no biometric state was active, success does not fabricate state/message;
10. stale/invalid marker state fails closed and does not roll back or silently change the active KDBX.

If deterministic process-death semantics require a small coordinator test rather than full process instrumentation, use both focused unit tests and the practical instrumentation/lifecycle coverage available in the repo.

## 15. UI / accessibility tests required

Cover, where practical with current test infrastructure:

- Settings exposes Change Master Password only in unlocked application context;
- current password required;
- new password required;
- confirmation mismatch blocks final action;
- advisory strength renders without becoming an enforcement gate;
- warning states both consequences: old password stops working and RAHSA cannot recover/open if new password forgotten;
- final action disabled until acknowledgment + normal field validation pass;
- swipe completion alone does not invoke re-key;
- explicit final action invokes at most once;
- TalkBack/accessibility exposes equivalent intentional acknowledgment action/state;
- Back/navigation/recreation resets current/new/confirmation fields + acknowledgment;
- returning to the flow may prefill the currently persisted hint again;
- in-progress operation cannot be cancelled merely by Back/Home navigation;
- success/error state does not leak password content.

Do not alter T127 test expectations except for backward-compatible shared-component reuse if absolutely necessary.

## 16. Local execution preconditions

After owner activation, before editing implementation files, local Codex MUST:

1. work from `G:\Projects\KeyPass`;
2. `git fetch` and synchronize `prototype/v0.2` with authoritative `origin/prototype/v0.2`;
3. confirm current remote history is a descendant of `PREPARED_AGAINST` and contains the T129 READY kit + activation checkpoint;
4. confirm working tree is clean;
5. read `AGENTS.md`, `TASKS.md`, `docs/GOVERNANCE_STATUS.md`, this kit, `TSD.md` §10/§11/§16, ADR 0005, ADR 0007, and relevant Threat Model entries;
6. verify exactly `ACTIVE_IMPLEMENTATION / T129 / implementation-kits/T129/README.md`;
7. run `python scripts/governance_preflight.py --implementation T129` and STOP on failure;
8. confirm no other writer is modifying the branch/worktree;
9. inspect current T128 persistence implementation before changing its credential handling;
10. inspect current biometric state implementation before assuming anything beyond the legacy settings field;
11. confirm Android SDK/JDK/Gradle environment works;
12. for physical validation, confirm Samsung SM-A115F is connected/authorized via `adb devices` before claiming device results.

If governance remains `PLANNING_FREEZE / NONE / NONE`, STOP. Do not implement T129.

## 17. Codex authority to correct this kit

During **activated T129 execution**, Codex may correct this README only when it discovers:

- stale/wrong repository path, class name, Gradle task, command, or dependency coordinate;
- a factual mismatch between the kit and current source;
- an impossible/flaky test instruction that can be replaced by a deterministic equivalent without weakening acceptance;
- a too-narrow allowed-file assumption where one directly related file is proven necessary for the same approved T129 behavior;
- a narrower crash-finalization mechanism that conforms more precisely to ADR 0007 without changing product/security semantics.

For each correction Codex MUST:

1. edit the kit explicitly rather than silently violate it;
2. explain what was wrong and why the correction remains T129;
3. keep the smallest possible scope;
4. include the kit diff in the handoff;
5. rerun `python scripts/governance_preflight.py --implementation T129` before continuing.

Codex MUST STOP/request owner decision if a correction would:

- add or weaken a product/security requirement;
- require T130–T133 behavior;
- redesign Auto-Lock/session policy;
- introduce custom crypto/KDF/schema migration;
- require secret-bearing recovery state;
- require a generic transaction/workflow framework;
- change T127 behavior/cosmetics;
- replace advisory strength with mandatory password policy;
- choose a different third-party dependency after a material zxcvbn4j problem;
- materially broaden public repository APIs/navigation architecture;
- self-authorize or activate another task.

## 18. Required validation commands

Use repository-working variants, at minimum:

```powershell
python scripts/governance_preflight.py --implementation T129
```

Run focused T129 repository/finalization/UI tests during development, then:

```powershell
.\gradlew.bat test --no-parallel --max-workers 1
.\gradlew.bat assembleFreeDebug --no-parallel --max-workers 1
```

Also run:

```powershell
git diff --check
python scripts/governance_preflight.py --implementation T129
```

Use established repository lint/static checks if already part of the local workflow. A successful build alone is not proof of re-key safety; old/new credential reopen and deterministic failure-boundary tests are mandatory.

## 19. Samsung Galaxy A11 physical smoke

On the validated `freeDebug` APK, record device model + commit SHA and test as much as practical without falsifying unavailable automation:

1. launch/open RAHSA and unlock a disposable test vault;
2. open Settings → Change Master Password;
3. verify warning, hint prefill, strength feedback, acknowledgment, and explicit final action;
4. try wrong current password and verify vault remains usable with old password/state;
5. abandon/re-enter before final submit and verify password fields + acknowledgment reset while persisted hint can prefill again;
6. perform one successful real password change;
7. verify surviving session remains usable/unlocked;
8. lock or force-stop/relaunch through established workflow;
9. verify old password fails and new password opens;
10. verify successful hint edit is reflected after success/relaunch;
11. if existing biometric state was active, verify it is disabled/invalidated and user is informed; otherwise verify no irrelevant biometric message appears;
12. confirm no T130 reset, backup/restore, T133 enrollment/unlock, or unexpected recovery UI appears;
13. run TalkBack acknowledgment smoke if practical on the device.

If a physical step cannot be driven or observed reliably, report it as **not validated**, not as a pass. Do not broaden T129 merely to make ADB UI automation easier.

## 20. Stop conditions

STOP implementation and report instead of improvising if any occurs:

- governance is not exactly active for T129;
- remote changed unexpectedly or one-writer ownership is unclear;
- correct re-key would require custom KDBX cryptography/KDF/schema mutation;
- T128 safe-promotion ordering would need to be weakened;
- distinct old/new credential support cannot be added narrowly to the existing safe-promotion seam;
- crash-consistent finalization appears to require password persistence or a generic transaction framework;
- current-password verification cannot be performed against on-disk active KDBX without broad credential caching;
- a public API/UI/navigation redesign materially beyond one narrow T129 operation/screen becomes necessary;
- zxcvbn4j fails material version/license/security/Android compatibility review and no already-approved alternative exists;
- implementation requires T130/T131/T132/T133 behavior;
- implementation would alter T127 semantics/cosmetics;
- implementation would persist Master Password through `dbPassword`, DataStore, marker, SavedState, logs, or files;
- deterministic failure tests would require unsafe/flaky destructive device tricks;
- required tests/preflight/build fail for reasons outside T129 scope;
- a proposed kit correction changes approved product/security behavior.

## 21. Implementation checkpoint handoff

After T129 implementation and all required validation succeed:

1. create one focused **implementation checkpoint**; do not amend/rebase prior accepted T128 history;
2. keep governance ACTIVE for T129 pending owner review; do not self-close T129;
3. do not begin T130;
4. do not push implementation unless the owner has explicitly authorized that push under the local workflow;
5. handoff must include:
   - local implementation commit SHA;
   - files changed, including any kit correction;
   - exact current-password reauthentication design;
   - exact old/new credential handling through T128 safe promotion;
   - commit-point + finalization-marker design and why it is crash-consistent without secrets;
   - dependency version/license/security note if zxcvbn4j added;
   - focused test commands/results mapped to T129 invariants;
   - full unit test result;
   - `assembleFreeDebug` result;
   - Samsung SM-A115F smoke scenarios/results and any explicitly unvalidated steps;
   - final governance preflight result;
   - `git diff --check` result;
   - final clean `git status` / local-vs-remote position;
   - residual risks/limitations;
   - explicit confirmation no T127 cosmetics and no T130–T133 behavior was introduced.

Only after owner/ChatGPT accepts the implementation checkpoint should a separate docs-only closure checkpoint return governance to:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

Stop. T130 remains unavailable until its own JIT kit is prepared and explicitly activated.
