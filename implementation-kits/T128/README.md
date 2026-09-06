# T128 JIT Implementation Kit — Exactly-One Encrypted LKG + Validate-Before-Promote

KIT_STATUS: READY
TASK_ID: T128
BASELINE_BRANCH: prototype/v0.2
IMPLEMENTATION_BASELINE: f3c909b23bf1fd65f0a30e8af5bf59cf12387a2d
PREVIOUS_KIT_COMMIT: 6ebba5585c91b7fd066143e37fd7c9ade82e0f63
TARGET_SCOPE: Phase 13 T128 only
RISK_CLASS: SECURITY_SENSITIVE
MODEL_RECOMMENDATION: GPT-5.6 Sol
REASONING_RECOMMENDATION: HIGH
AUTHORITATIVE_EXECUTION_ENVIRONMENT: Codex on local Windows repository G:\Projects\KeyPass
AUTHORITATIVE_REMOTE: GitHub abeljoeman/KeyPass branch prototype/v0.2
PHYSICAL_VALIDATION_DEVICE: Samsung Galaxy A11 / SM-A115F

## 1. Governance state

This kit is READY for T128 only. READY does **not** activate implementation.

At kit revision time the required control surface remains:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

T128 MUST NOT be implemented until the owner gives explicit authorization and governance is deliberately activated for exactly:

- `EXECUTION_STATUS: ACTIVE_IMPLEMENTATION`
- `ACTIVE_TASK: T128`
- `ACTIVE_KIT: implementation-kits/T128/README.md`

Until that activation occurs, Codex may inspect/read but must not edit application implementation for T128.

Once T128 is explicitly activated, **Codex running against the local Windows checkout is the single writer** for T128 implementation, focused tests, Android build, and physical smoke validation. Do not run a concurrent Codex Cloud implementation or make overlapping manual edits while the local Codex writer owns the task.

GitHub `prototype/v0.2` remains the authoritative remote history. The local writer must synchronize from the authoritative remote before implementation and push focused checkpoints back to that branch only after required validation.

## 2. Authoritative intent

T128 establishes the narrow data-safety foundation approved for Phase 13 and ADR 0005:

1. maintain at most one internal encrypted Last-Known-Good (LKG) KDBX artifact, and exactly one once a previous committed valid active state exists to preserve;
2. serialize a candidate vault to an app-private temporary encrypted KDBX;
3. validate that candidate by decoding/opening it with the intended credentials **before** changing the active vault or existing LKG;
4. after validation, promote the candidate while rotating the prior valid active vault into the single LKG slot;
5. preserve a usable previous valid state deterministically if validation or promotion fails;
6. never silently restore a corrupt active vault from LKG; any later user-facing recovery/restore workflow must remain explicit and separately scoped.

T128 is the safe-promotion/LKG foundation. It is **not** Change Master Password (T129), destructive reset (T130), SAF backup (T131), SAF restore (T132), or biometric quick unlock (T133).

## 3. Local execution preconditions

Before editing implementation files, Codex MUST:

1. work from `G:\Projects\KeyPass`;
2. fetch authoritative `origin/prototype/v0.2` and confirm the local branch is synchronized with the latest authoritative remote commit containing this READY kit;
3. confirm that latest remote history remains a descendant of `IMPLEMENTATION_BASELINE`; report unexpected divergence;
4. read `TASKS.md`, `docs/GOVERNANCE_STATUS.md`, this kit, `TSD.md`, and `docs/adr/0005-data-safety-kdbx-lkg-saf.md`;
5. verify governance has been explicitly activated for T128;
6. run `python scripts/governance_preflight.py --implementation T128` and stop on failure;
7. verify `git status` is clean before implementation;
8. verify no other writer is modifying the same branch/worktree;
9. confirm the local Android SDK/JDK/Gradle environment required by the repository is usable;
10. when physical smoke validation is reached, verify Samsung SM-A115F is visible to `adb devices` before claiming a device result.

If governance is still `PLANNING_FREEZE / NONE / NONE`, STOP. Do not implement T128.

## 4. Scope — IN

Implementation may cover only the minimum required to make the T128 persistence invariants true:

- refactor the narrow persistence path in `KotpassVaultRepository` so candidate validation occurs before promotion;
- keep candidate, active, and LKG artifacts encrypted KDBX files in app-private storage;
- use one deterministic LKG location/identity; no timestamped/history accumulation;
- on successful mutation, make the immediately previous committed valid active vault the LKG and the validated candidate the new active vault;
- on fresh vault creation where no prior active state exists, do not invent a synthetic historical LKG; the first later successful mutation may establish the first LKG;
- guarantee candidate-validation failure leaves current active and current LKG untouched;
- guarantee promotion/file-operation failure does not accept a destructive partial state as success and preserves/restores the prior usable authoritative state deterministically;
- keep enough internal distinction/tests to prove invalid/corrupt active + valid LKG does **not** trigger automatic replacement;
- preserve current credential/KDBX behavior and reuse intended credentials only for in-process KDBX validation; do not log, persist, or broaden credential lifetime;
- add a narrow deterministic filesystem/fault-injection seam only when required to test post-validation failure safely and repeatably;
- make minimal test-only/supporting changes strictly necessary to prove the above invariants.

## 5. Scope — OUT

Do NOT implement or modify behavior belonging to:

- T129 Change Master Password / real KDBX re-key;
- T130 Forgot Master Password / destructive reset;
- T131 manual external KDBX backup via SAF;
- T132 safe external KDBX restore via SAF;
- T133 Biometric Quick Unlock;
- any user-facing LKG restore action, restore confirmation, restore navigation, or automatic recovery flow;
- T127 swipe acknowledgment visuals, warning-message appearance, or its already validated behavior/security semantics;
- silent or automatic LKG restoration;
- backup history, multiple retained LKG versions, timestamped backup accumulation, or user-browsable backup management;
- KDBX crypto/KDF policy changes, vault schema/model migrations, credential-policy changes, or new external dependency introduction;
- opportunistic refactors outside the vault-persistence seam.

## 6. Allowed files

Primary implementation files:

- `app/src/main/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepository.kt`
- `app/src/test/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepositoryTest.kt`

Conditionally allowed only when necessary for deterministic, focused T128 implementation/testing:

- one small internal production helper under `app/src/main/java/com/yogeshpaliyal/keypass/vault/` that abstracts only filesystem operations required by T128;
- the directly corresponding focused test file under `app/src/test/java/com/yogeshpaliyal/keypass/vault/`;
- `implementation-kits/T128/README.md` only for an authorized in-scope kit correction as defined in section 7.

Do **not** change `VaultRepository.kt`, UI/view-model files, navigation, Gradle/dependency files, KDBX crypto configuration, or unrelated tests merely for convenience. If one of those becomes technically necessary, apply the stop/kit-correction rules below rather than silently expanding scope.

Task closure/evidence documents may be updated only after implementation and validation under the normal task-completion workflow.

## 7. Codex authority to correct this kit

During an **activated T128 execution**, Codex is explicitly allowed to correct this kit when it discovers an error, stale path/command, incorrect factual statement, impossible test instruction, or a too-narrow implementation-file assumption **provided the correction remains strictly necessary to achieve the already-approved T128 objective and does not broaden product/security behavior**.

Permitted kit corrections include, for example:

- correcting a repository path, class name, Gradle task, device command, or task-number/reference mistake;
- replacing an impossible/flaky test mechanism with a deterministic equivalent inside T128;
- adding a directly related repository/helper/test file to the allowed-file list when inspection proves it is required for the same T128 persistence invariant;
- clarifying transaction ordering or validation evidence so it conforms more precisely to ADR 0005/TSD without adding a new feature.

For every such correction Codex MUST:

1. edit this kit explicitly rather than silently violating it;
2. state what was wrong and why the correction is still within T128;
3. keep the smallest possible scope change;
4. include the kit diff in the implementation handoff;
5. re-run governance preflight after the correction before continuing implementation.

Codex MUST **STOP and request an owner scope decision** instead of self-correcting the kit if the proposed change would:

- add user-facing behavior not already approved for T128;
- require implementation of T129–T133 behavior;
- change T127 behavior/security semantics or perform its parked cosmetic refinement;
- introduce a new external dependency, crypto/KDF policy, schema migration, credential policy, broad public API, navigation flow, or generic transaction framework;
- weaken an accepted T128/ADR 0005 safety invariant in order to make implementation easier.

This correction authority is not permission to edit `TASKS.md` activation state or to self-authorize another task.

## 8. Required implementation invariants

### 8.1 Candidate validation precedes mutation of active/LKG

The candidate must first be encoded to a temporary encrypted KDBX and then successfully decoded/opened with the intended credentials. Until that validation succeeds:

- active must not be replaced, moved, truncated, or deleted;
- existing LKG must not be replaced, moved, truncated, or deleted.

### 8.2 Exactly-one LKG lifecycle

Once a previous committed valid active state exists:

- the successful next promotion leaves one active vault and one LKG;
- the LKG is the immediately previous committed valid active state;
- later successful promotions replace the single LKG rather than accumulate history;
- stale temporary/transaction artifacts must not become extra retained backups.

### 8.3 Deterministic failure behavior

If candidate validation fails, active and LKG remain unchanged.

If file promotion fails after candidate validation, the operation must resolve deterministically to a usable prior authoritative state rather than silently accept an uncertain partial state as success.

If the current filesystem strategy cannot satisfy the required invariant, STOP and report the exact transaction gap instead of weakening the invariant.

### 8.4 Corrupt active + valid LKG is not automatic recovery

T128 automated tests must be able to establish/prove:

- active is invalid/corrupt;
- the single LKG remains a valid encrypted KDBX with the intended credentials;
- repository behavior does not silently replace active from LKG.

Do not add a restore button/action, public recovery flow, navigation, or SAF restore behavior to satisfy this test. User-facing restore orchestration belongs outside T128.

### 8.5 Credential handling

Candidate/LKG validation must use intended vault credentials without:

- writing plaintext credentials to disk;
- logging credentials;
- introducing a persistent credential cache;
- extending credential lifetime beyond what is necessary for the repository operation.

Wrong credentials must not become an oracle that silently exposes or restores an LKG.

## 9. Required automated tests

Add focused automated coverage proving at minimum:

1. **Successful promotion** — from valid active A to valid candidate B, active becomes B and LKG becomes A; both remain decryptable KDBX with intended credentials.
2. **Repeated promotion / LKG replacement** — A -> B -> C leaves active C and exactly one LKG representing B; A is not retained as another backup.
3. **Fresh creation** — first vault creation succeeds without fabricating a historical LKG; first subsequent successful mutation may establish prior active as LKG.
4. **Candidate-invalid failure** — validation failure occurs before active/LKG mutation and leaves previous artifacts unchanged.
5. **Promotion failure after validation** — deterministic injected filesystem failure proves the prior usable state is preserved/restored and no backup accumulation occurs.
6. **Active-corrupt + valid-LKG** — no silent replacement/auto-restore occurs.
7. **Active-invalid + invalid/no-LKG** — no automatic repair is attempted and failure semantics remain deterministic.
8. **Wrong credentials** — wrong-password behavior is not reclassified as a recovery path merely because an LKG exists.
9. **Encrypted artifacts** — candidate/LKG transaction artifacts are encrypted KDBX, not plaintext serialized vault data.
10. **Regression** — existing create/open/CRUD, metadata, duplicate-create, and relevant repository tests remain green.

Prefer byte-level before/after assertions for “untouched” failure cases when practical, supplemented by successful KDBX decode assertions.

## 10. Required local validation

The local Codex writer owns the complete T128 validation loop. A remote/Cloud build is not required when local validation is complete.

Run, at minimum, from the Windows checkout using repository-working command variants:

1. `python scripts/governance_preflight.py --implementation T128` before implementation;
2. focused T128 repository/unit tests during development;
3. `.\gradlew.bat test` (or the exact repository-equivalent full unit-test task if this aggregate task is not valid);
4. `.\gradlew.bat assembleFreeDebug`;
5. `python scripts/governance_preflight.py --implementation T128` again before handoff;
6. `git diff --check` and relevant repository lint/static checks if already part of the established workflow;
7. clean final `git status` after the focused implementation checkpoint/commit.

Do not count a successful build alone as proof of LKG transaction safety. Failure-path invariants must be demonstrated by deterministic automated tests.

## 11. Samsung Galaxy A11 physical smoke test

Physical smoke validation is part of the expected T128 handoff because local Codex has access to the Android toolchain/device workflow.

Before claiming the result:

- verify Samsung Galaxy A11 / SM-A115F is connected and authorized via `adb devices`;
- install/run the validated `freeDebug` build using the repository's established local workflow;
- avoid destructive device manipulation as a substitute for deterministic unit failure injection.

At minimum smoke-test normal user-visible behavior potentially affected by the persistence refactor:

1. launch RAHSA normally;
2. create/open a test vault as appropriate for the current device state;
3. perform representative credential mutations (create and at least one edit/delete path if practical without interfering with unrelated acceptance data);
4. navigate away/back or lock/reopen through the existing supported flow as appropriate;
5. relaunch the app and verify the vault opens and the latest successful mutation persists;
6. confirm there is no unexpected recovery/restore UI, no automatic rollback, and no visible regression in the existing T127 create flow;
7. record device model, build/commit SHA, scenarios run, and pass/fail observations.

If the device is unavailable or blocked by an environment problem, do not falsify a pass. Report automated/build results separately and identify the physical smoke as blocked. Do not broaden T128 merely to fix unrelated device/environment issues.

## 12. Stop conditions

STOP implementation and report instead of improvising if any of these occurs:

- governance is not explicitly activated for T128;
- authoritative remote changed unexpectedly or one-writer ownership is unclear;
- required implementation escapes T128 behavior/safety scope;
- a public `VaultRepository` API/UI/navigation/recovery flow appears necessary;
- implementation would need T129/T130/T131/T132/T133 behavior;
- implementation would alter T127 behavior/security semantics or perform its parked cosmetic refinement;
- a new external dependency, crypto/KDF change, schema migration, credential-policy change, or generic transaction framework appears necessary;
- the current filesystem strategy cannot guarantee the required pre-validation and failure-preservation invariants;
- deterministic failure testing would require unsafe/flaky physical-device tricks instead of a narrow injectable seam;
- credential validation would require plaintext persistence, logging, or a broader credential cache;
- governance preflight or required tests fail for reasons that cannot be resolved strictly within T128;
- a proposed kit correction would broaden feature/product/security scope rather than correct execution details.

## 13. Completion handoff

When T128 implementation is complete, do **not** begin T129.

Handoff must provide:

1. implementation commit SHA and pushed authoritative remote SHA;
2. files changed, including any authorized kit correction;
3. exact validation commands and results;
4. mapping from automated tests to T128 invariants;
5. full unit-test and `assembleFreeDebug` result;
6. Samsung SM-A115F smoke scenarios/result, or an explicit blocked reason if physical validation could not be performed;
7. any residual risks or limitations;
8. final clean `git status` and one-writer confirmation;
9. explicit confirmation that no T129+ behavior or T127 cosmetic refinement was introduced.

Only after owner acceptance/closure should governance return to:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

The next task remains unavailable until its own kit is READY and the owner explicitly authorizes activation.
