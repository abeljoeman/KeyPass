# T130 Pre-Kit — Forgot Master Password / Resumable Destructive Reset

TASK_ID: T130
PREKIT_STATUS: DRAFT_NOT_READY
EXECUTABLE: NO
OBSERVED_AGAINST: bc607a40ed545bdf1a3855663765b77f66fc9445
FINAL_PREPARED_AGAINST: TBD_AFTER_T129
BASELINE_BRANCH: prototype/v0.2
TARGET_SCOPE: Phase 13 T130 only
RISK_CLASS: SECURITY_SENSITIVE
MODEL_RECOMMENDATION: GPT-5.6 Sol
REASONING_RECOMMENDATION: HIGH
AUTHORITATIVE_EXECUTION_ENVIRONMENT: Codex on local Windows repository G:\Projects\KeyPass
AUTHORITATIVE_REMOTE: GitHub abeljoeman/KeyPass branch prototype/v0.2
PHYSICAL_VALIDATION_DEVICE: Samsung Galaxy A11 / SM-A115F

## 1. Status and purpose

This file is a **planning pre-kit**, not an executable Implementation Kit.

It exists only to preserve verified T130 research while T129 is still pending. It MUST NOT be referenced as `ACTIVE_KIT`, MUST NOT be treated as `KIT_STATUS: READY`, and MUST NOT authorize application-code work.

T130 can become executable only after T129 is implemented, validated, owner-reviewed, closed, and the final T129 checkpoint is known. At that point this document must be revalidated against the new repository state and replaced/promoted by:

- `implementation-kits/T130/README.md`
- `KIT_STATUS: READY`
- `PREPARED_AGAINST: <final accepted T129 checkpoint>`

The final READY kit must account for every T129 artifact/API/startup/finalization change relevant to destructive reset. Do not mechanically rename this file without that review.

At draft time the required execution gate remains:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

T129 itself has a READY kit but is not activated at this checkpoint. T130 remains NOT ACTIVE.

## 2. Authoritative sources

Before finalizing this pre-kit into a READY kit, re-read and obey in authority order:

- `ENGINEERING_PRINCIPLES.md`
- `PRD.md` Phase 13, especially P13-FR-030..034
- `TSD.md` §10 and §16
- `docs/adr/0008-resumable-destructive-reset.md`
- `docs/THREAT_MODEL.md`, especially T22/T23 and related credential/recovery threats
- `TASKS.md` T130
- the final accepted T129 implementation and closure checkpoint
- the eventual T130 READY kit

If T129 changes any assumption recorded below, update the final T130 kit rather than forcing this draft onto stale source.

## 3. Approved T130 objective

Implement an honest forgotten-Master-Password path from the locked/login state that allows the user to intentionally delete RAHSA-managed local vault/security state and start a new vault, without implying that the inaccessible old vault can be recovered.

The operation must:

1. be reachable only from the locked/login flow;
2. show the persisted password hint when available while clearly stating that RAHSA cannot decrypt/recover the vault without the correct Master Password;
3. require the exact confirmation text `DELETE` before the final `Reset RAHSA` action can be enabled;
4. perform no deletion before the user explicitly starts the final Reset action;
5. durably create a minimal app-private non-secret `RESET_IN_PROGRESS` marker, or equivalent narrowly scoped marker, **before the first destructive mutation**;
6. after that marker exists, converge idempotently toward complete reset rather than attempting rollback;
7. remove only RAHSA-managed security/vault state tied to the old vault;
8. leave user-managed external backups and unrelated preferences untouched;
9. resume cleanup on startup when the reset marker exists, before normal vault routing/use;
10. route to normal Create New Vault only after all required cleanup succeeds and the reset marker is removed.

T130 is destructive reset, not password recovery.

## 4. Security invariants

The final implementation must prove all of these invariants.

### 4.1 No deletion before explicit final action

- Opening `Forgot Master Password?` does not mutate vault/security state.
- Viewing the hint does not mutate vault/security state.
- Back/navigation away before Reset starts does not mutate vault/security state.
- Home/background/recreation/process death/force-close before Reset starts does not mutate vault/security state.
- A partial or incorrect confirmation token does not enable or start reset.
- The typed `DELETE` draft is not required to persist across recreation; re-entry starts unconfirmed.

### 4.2 Exact confirmation

The destructive action is enabled only when the user has entered the exact approved token:

`DELETE`

Do not silently broaden accepted variants, auto-fill it, or persist it as confirmation state.

### 4.3 Marker-before-mutation ordering

The durable reset marker must exist successfully before any old-vault artifact, setting, biometric state, or session state is destructively cleared as part of the committed reset operation.

If durable marker creation fails, destructive cleanup must not begin.

The marker is non-secret. It must not contain:

- current/old/new Master Password;
- decrypted KDBX data;
- credential material;
- backup password/key material;
- external backup locations when not strictly necessary;
- vault contents or metadata copied from the vault.

### 4.4 Post-marker desired state is reset completion

Once the marker exists and Reset has started:

- Back/Home/navigation is not a transaction cancel;
- process death/crash/force-close does not restore the old vault;
- restart must resume cleanup;
- cleanup is idempotent;
- already-missing task-owned artifacts are treated as already cleaned when safe;
- partial failure retains the reset marker and fails closed;
- the app must not expose normal old-vault use or Create New Vault as though reset were complete until required cleanup has actually completed.

### 4.5 Destructive boundary is narrow

Delete/clear only state owned by RAHSA and tied to the old vault, including as applicable after T129 revalidation:

- active internal KDBX;
- exactly one internal LKG KDBX;
- RAHSA-owned candidate/temp/rollback/recovery artifacts;
- any stale task-owned post-commit/finalization artifact that the final T129 design establishes as tied to the old vault and which the finalized T130 design explicitly accounts for;
- password hint;
- legacy/vault-tied credential convenience state still retained by the app;
- biometric quick-unlock wrapped credential state when present;
- related Android Keystore alias when present;
- in-memory vault/session state.

Do not perform a blanket app-data/settings reset merely for convenience.

### 4.6 Preserve external/user-owned data and unrelated preferences

T130 MUST NOT delete or modify:

- KDBX backups/files selected or exported by the user through SAF or other user-owned external storage;
- unrelated UI preferences such as theme/appearance;
- unrelated app preferences that are not credentials/security state tied to the old vault;
- future cloud/account state outside this approved local reset boundary.

### 4.7 No recovery bypass

T130 must not:

- recover or decrypt the old vault without the Master Password;
- use LKG as a password-recovery bypass;
- silently restore LKG;
- present biometric as forgotten-password recovery;
- present external backup as automatic recovery;
- add security questions, server recovery, cloud recovery, escrow, or recovery keys.

### 4.8 No secure-erasure claim

Use normal app-managed deletion. Do not introduce or claim forensic secure wipe, multi-pass overwrite, or guaranteed physical erasure on flash storage.

## 5. Current observed implementation seams at pre-kit baseline

These observations are valid only against `OBSERVED_AGAINST` and MUST be checked again after T129.

### 5.1 Login/authentication UI

Current login/create/confirm behavior lives primarily in:

- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/AuthScreen.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/PasswordInputField.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/ButtonBar.kt`

T130 will need a narrow `Forgot Master Password?` entry from the login state and a destructive-reset confirmation surface. Do not turn this into a broad authentication redesign.

### 5.2 Navigation/state

Current navigation is Redux/screen-state based and routes through:

- `app/src/main/java/com/yogeshpaliyal/keypass/ui/nav/DashboardComposeActivity.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/states/ScreenState.kt`
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/states/AuthState.kt`

A small dedicated reset state/screen or equally narrow reuse is acceptable. Do not introduce a second navigation architecture.

### 5.3 Vault artifacts established by T128

At this observed baseline, `KotpassVaultRepository` manages:

- active `vault.kdbx`;
- deterministic LKG `vault.kdbx.lkg`;
- candidate artifacts named from the active vault with `.candidate.` in the file name;
- rollback artifacts named from the active vault with `.rollback.` in the file name.

T130 should prefer a narrow repository/file-operations-owned cleanup API rather than duplicating filename knowledge throughout UI code.

**Revalidation requirement:** T129 may add or rename task-owned candidate/finalization/marker artifacts and may extend repository APIs. The final T130 kit must inspect the accepted T129 diff before freezing cleanup ownership/names.

### 5.4 Settings state

Current retained `UserSettings` includes legacy/vault-related fields such as:

- `dbPassword`;
- `passwordHint`;
- `isBiometricEnable`;
- biometric login timing/convenience fields;
- other unrelated settings.

Current settings utilities expose narrow setters including `setPasswordHint`, `setBiometricEnable`, and legacy `setDatabasePassword`.

T130 must clear only old-vault credential/security convenience state required by the approved reset semantics. It must not replace all settings with a fresh `UserSettings()` unless a higher-level requirement explicitly changes this decision.

The legacy `dbPassword` field must never be interpreted as a legitimate Master Password recovery source.

### 5.5 Biometric/Keystore seam

Current retained code includes:

- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/components/BiometricPrompt.kt`
- `common/src/main/java/com/yogeshpaliyal/common/utils/CryptoManager.kt`
- biometric-related settings fields/utilities.

T130 cleanup must remove/disable vault-tied biometric wrapped credential state and related alias when present, but MUST NOT implement T133 Secure Biometric Quick Unlock.

**Revalidation requirement:** T129 is approved to invalidate existing biometric quick-unlock state after successful re-key and may establish a narrower helper/API. The final T130 kit should reuse the accepted T129 cleanup seam when appropriate rather than creating a competing implementation.

### 5.6 Startup/resume seam

At this observed baseline, `MyApplication.kt` does not contain reset-resume orchestration. `DashboardComposeActivity.kt` currently provides the app's Compose/root vault wiring and startup work.

The final T130 kit must inspect the accepted T129 implementation first, because T129 may introduce a post-commit finalization/startup coordinator that can be reused or may establish the correct narrow startup seam.

Do not build a generic transaction/workflow engine. T130 needs only an idempotent reset marker/coordinator.

## 6. Provisional implementation shape

Subject to post-T129 revalidation, the preferred minimal structure is:

1. a login-only entry action from `AuthScreen`;
2. one focused destructive-reset screen/state with warning, hint, `DELETE` confirmation, and explicit `Reset RAHSA` action;
3. one narrow reset coordinator/service responsible for:
   - durable marker creation;
   - task-owned cleanup ordering;
   - idempotent resume;
   - marker removal only after complete success;
4. vault/file-operations API that owns deletion of RAHSA-managed internal vault artifacts rather than making UI code understand raw filenames;
5. narrow settings/biometric cleanup helpers that remove only vault-tied state;
6. one startup check that finishes a pending reset before normal vault routing/use;
7. focused deterministic tests, including fault injection around cleanup stages.

The coordinator may be a new small production helper if this is clearer and safer than forcing reset semantics into an unrelated UI class. It must remain T130-specific.

## 7. Provisional allowed files

This allowlist is intentionally provisional and must be finalized after T129. The final READY kit should reduce or adjust it based on the accepted source tree.

Likely UI/navigation files:

- `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/AuthScreen.kt`
- one new focused reset UI file under `app/src/main/java/com/yogeshpaliyal/keypass/ui/auth/` or a dedicated narrowly named reset package;
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/states/AuthState.kt` and/or `ScreenState.kt` only as required for one reset route;
- `app/src/main/java/com/yogeshpaliyal/keypass/ui/nav/DashboardComposeActivity.kt` only for narrow routing/startup integration if that remains the correct seam after T129;
- `app/src/main/res/values/strings.xml`.

Likely vault/reset files:

- `app/src/main/java/com/yogeshpaliyal/keypass/vault/VaultRepository.kt` if a narrow reset cleanup contract is appropriate;
- `app/src/main/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepository.kt`;
- `app/src/main/java/com/yogeshpaliyal/keypass/vault/VaultFileOperations.kt`;
- one new focused T130 reset coordinator/marker helper under an appropriate app-private package;
- `app/src/main/java/com/yogeshpaliyal/keypass/MyApplication.kt` only if post-T129 review proves it is the correct narrow startup hook.

Likely settings/security files only when needed:

- `common/src/main/java/com/yogeshpaliyal/common/utils/SharedPreferenceUtils.kt` or the exact accepted T129 successor/helper;
- `common/src/main/java/com/yogeshpaliyal/common/utils/CryptoManager.kt` or the exact accepted T129 biometric invalidation helper;
- avoid changing `UserSettings.kt` schema unless post-T129 inspection proves a small non-secret reset marker belongs there and higher-level architecture permits it; app-private file marker is generally preferable to broad settings/schema changes unless the accepted design says otherwise.

Tests:

- focused repository/reset coordinator unit tests under `app/src/test/...`;
- focused Compose/instrumentation tests under `app/src/androidTest/...` only for UI/accessibility/process/lifecycle cases that cannot be proven reliably as local unit tests;
- existing regression tests that require narrow updates because of the new route/API.

No Gradle/dependency change is expected for T130. If one appears necessary, stop for planning review unless the final READY kit explicitly authorizes it.

## 8. Explicit out of scope

Do NOT implement under T130:

- T129 Change Master Password behavior beyond reusing its accepted cleanup/startup seams;
- T131 external KDBX backup/export;
- T132 external restore/import;
- T133 biometric quick unlock;
- recovery questions;
- cloud/server/account recovery;
- recovery key/escrow;
- old-password bypass;
- automatic restore from LKG;
- automatic import from external backup;
- deletion of external/user-owned backup files;
- secure overwrite/multi-pass wipe claims;
- generic transaction/workflow framework;
- broad settings reset;
- broad navigation rewrite;
- unrelated T127 cosmetic changes;
- backup history/versioning;
- KDF/crypto policy changes;
- schema migration unrelated to the narrow reset marker/state;
- new external dependencies unless separately owner-approved.

## 9. Required deterministic automated evidence for the final kit

The final READY kit should require focused tests proving at minimum:

### Confirmation and pre-start behavior

1. Opening reset flow performs no deletion.
2. Missing/partial/wrong confirmation token keeps final Reset disabled and performs no deletion.
3. Exact `DELETE` enables the final action according to the approved UI state.
4. Back/navigation away before final Reset performs no deletion.
5. Recreation/re-entry before final Reset does not retain destructive confirmation as completed.

### Marker ordering and committed reset

6. Marker persistence succeeds before the first destructive cleanup operation. This ordering must be deterministic in tests, not inferred only from end state.
7. Marker-write failure produces zero destructive cleanup.
8. Once marker exists, reset does not roll back already removed state.

### Cleanup scope

9. Successful reset removes active KDBX.
10. Successful reset removes exactly-one LKG artifact.
11. Successful reset removes RAHSA-owned candidate/temp/rollback/recovery leftovers defined by the final post-T129 source.
12. Successful reset clears password hint.
13. Successful reset clears legacy/vault-tied credential convenience state that remains in the accepted post-T129 code.
14. Successful reset removes/disables biometric wrapped state and related Keystore alias when present.
15. Successful reset clears/locks in-memory vault/session state.
16. A user-owned external KDBX/backup fixture outside app-private managed storage is untouched.
17. Unrelated preference fixtures remain unchanged.

### Failure/resume/idempotency

18. Failure after marker creation retains the marker and does not route to Create New Vault as completed.
19. Startup with marker resumes pending cleanup before normal vault use.
20. Restart/resume tolerates already-missing files/settings/alias safely.
21. Repeating cleanup after partial completion is idempotent.
22. Marker is removed only after all required cleanup succeeds.
23. Create New Vault routing occurs only after marker removal/complete cleanup.
24. No LKG auto-restore/recovery path is triggered during reset.

### Regression

25. Normal login with correct Master Password still works when no reset is in progress.
26. Wrong-password login remains a normal authentication failure, not a reset/recovery trigger.
27. Existing Create Vault behavior remains unchanged outside the explicit reset transition.
28. Existing T128 safe-promotion/LKG behavior remains intact outside reset cleanup.
29. T129 re-key behavior remains intact when no reset is in progress.

## 10. Fault-injection expectations

The final implementation should expose only the narrowest deterministic seam necessary to simulate failures at meaningful reset stages, for example:

- marker creation;
- active-vault deletion;
- LKG/managed-temp deletion;
- settings cleanup;
- biometric/Keystore cleanup;
- marker removal.

Do not rely on flaky device filesystem tricks or physically killing the process at precisely timed moments as the only proof of crash consistency. Physical/process smoke complements deterministic automated tests; it does not replace them.

Do not create a generic fault-injection framework.

## 11. Finalization gate after T129

Before converting this pre-kit to READY, ChatGPT/owner preparation must:

1. verify the final accepted/pushed T129 checkpoint on `prototype/v0.2`;
2. verify T129 is COMPLETE and governance has returned to `PLANNING_FREEZE / NONE / NONE`;
3. compare `OBSERVED_AGAINST` to the final T129 checkpoint and inspect every changed file relevant to:
   - repository APIs;
   - vault artifact names/ownership;
   - candidate/LKG handling;
   - re-key finalization marker;
   - startup reconciliation;
   - settings/hint cleanup;
   - biometric invalidation/Keystore helper;
   - navigation/auth flow;
4. refresh the final T130 allowed-file list to the smallest accurate set;
5. update tests/commands to actual post-T129 seams;
6. resolve whether any T129 finalization marker can coexist with reset and define deterministic precedence/order without inventing new product behavior;
7. create `implementation-kits/T130/README.md` with standard manifest fields including `KIT_STATUS: READY` and the final T129 checkpoint as `PREPARED_AGAINST`;
8. keep T130 NOT ACTIVE until separate explicit owner activation.

A stale assumption in this pre-kit is a reason to revise the final kit, not permission to broaden T130.

## 12. Expected local execution validation after future activation

Once a final T130 READY kit exists and the owner explicitly activates T130, local Codex should be required to run at minimum:

```powershell
cd G:\Projects\KeyPass
git status -sb
python scripts/governance_preflight.py --implementation T130
```

Then focused reset/repository/settings/security tests during development, followed by repository-wide validation appropriate to the accepted post-T129 tree, expected to include:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleFreeDebug
python scripts/governance_preflight.py --implementation T130
git diff --check
```

Run established lint/static checks if applicable and required by the current repository workflow.

No build-only acceptance: deterministic destructive-reset/failure tests are mandatory.

## 13. Physical-device smoke expectation after future activation

Use only a disposable test vault on Samsung Galaxy A11 / SM-A115F. Never use a valuable production vault for destructive smoke.

Minimum device scenarios should include:

1. install/run the exact validated `freeDebug` build/commit;
2. create/open a disposable vault and establish representative local data;
3. lock to Login;
4. open `Forgot Master Password?` and verify warning/hint behavior;
5. verify Back before Reset leaves the vault intact;
6. re-enter, type a wrong token, verify Reset remains unavailable/no deletion;
7. type exact `DELETE`, explicitly start Reset, and verify completion routes to Create New Vault only after cleanup;
8. verify old local vault cannot be opened because app-managed old-vault state is gone;
9. verify unrelated settings remain intact where practical;
10. if a disposable user-owned external backup exists, verify reset does not delete it;
11. perform a restart/relaunch scenario relevant to pending-marker resume if this can be done safely and deterministically without destructive device hacks;
12. record device model, build/commit SHA, scenarios, and results.

Biometric-specific physical cleanup should be exercised only if the accepted post-T129 code has existing biometric state that can be safely configured for the disposable vault. Do not implement T133 just to satisfy T130 smoke.

## 14. Stop conditions for final preparation or execution

STOP and return to owner/planning if any of the following occurs:

- T129 has not yet been accepted/closed when someone attempts to mark T130 READY;
- the final T129 checkpoint introduces artifacts or credential/finalization semantics not accounted for by the T130 design;
- reset requires implementing T131, T132, or T133 behavior;
- the implementation would delete external/user-owned files;
- the implementation would blanket-reset unrelated settings;
- the implementation proposes secure-wipe claims or overwrite machinery;
- a generic transaction/workflow framework appears necessary;
- a new dependency is needed without explicit approval;
- crypto/KDF policy or KDBX schema changes are proposed;
- the reset marker would need to contain a secret;
- partial cleanup can expose normal vault/Create Vault use while the reset marker remains;
- startup precedence with a T129 finalization marker is ambiguous and cannot be resolved from approved architecture;
- deterministic cleanup-order/failure tests cannot be implemented without broad architecture changes;
- T127 cosmetics/semantics or unrelated UI behavior would be changed.

## 15. Owner-review hold recommendation

Because T130 is deliberately destructive and security-sensitive, the final READY kit should use the same owner-review hold pattern as T129 unless the owner explicitly chooses otherwise:

- Codex implements/tests/builds/device-smokes T130;
- Codex pushes one focused implementation checkpoint;
- governance remains `ACTIVE_IMPLEMENTATION / T130 / implementation-kits/T130/README.md`;
- owner/ChatGPT reviews scope, destructive-boundary evidence, failure tests, and device results;
- only after explicit acceptance is T130 marked COMPLETE and governance reset to `PLANNING_FREEZE / NONE / NONE`.

Codex must never self-authorize T131.

## 16. Promotion checklist for this pre-kit

This pre-kit is ready for later revalidation when all boxes below can be satisfied:

- [ ] T129 accepted and closed.
- [ ] Final T129 checkpoint SHA recorded.
- [ ] Post-T129 branch HEAD verified.
- [ ] T129 changed files inspected for reset impact.
- [ ] Active/LKG/candidate/rollback/finalization artifact ownership refreshed.
- [ ] Startup/finalization precedence resolved.
- [ ] Biometric invalidation/Keystore seam refreshed.
- [ ] Hint/settings cleanup seam refreshed.
- [ ] Provisional allowed-file list narrowed and made authoritative.
- [ ] Focused deterministic test plan updated to actual post-T129 APIs.
- [ ] Final local validation commands verified.
- [ ] `implementation-kits/T130/README.md` created with `KIT_STATUS: READY`.
- [ ] T130 remains NOT ACTIVE until separate explicit owner activation.

Until then this file remains `DRAFT_NOT_READY` and `EXECUTABLE: NO`.