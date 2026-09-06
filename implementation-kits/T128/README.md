# T128 JIT Implementation Kit — Exactly-One Encrypted LKG + Validate-Before-Promote

KIT_STATUS: READY
TASK: T128
BASELINE_BRANCH: prototype/v0.2
BASELINE_COMMIT: f3c909b23bf1fd65f0a30e8af5bf59cf12387a2d
BASELINE_RELATION: T127 closure checkpoint; implementation baseline 7cc4f9468801e418198e319fd7b8350e0184ca9e
TARGET_SCOPE: Phase 13 T128 only
RISK_CLASS: SECURITY_SENSITIVE
MODEL_RECOMMENDATION: GPT-5.6 Sol
REASONING_RECOMMENDATION: HIGH
AUTHORITATIVE_EXECUTION_ENVIRONMENT: Codex Cloud
LOCAL_VALIDATION_ENVIRONMENT: Windows Android SDK; Samsung Galaxy A11 / SM-A115F when physical regression smoke is warranted

## 1. Governance state

This kit is READY for T128 only. READY does **not** activate implementation.

At kit creation time the required control surface remains:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

T128 MUST NOT be implemented until the user gives explicit authorization and governance is deliberately activated for exactly:

- `EXECUTION_STATUS: ACTIVE_IMPLEMENTATION`
- `ACTIVE_TASK: T128`
- `ACTIVE_KIT: implementation-kits/T128/README.md`

One-writer rule applies while Codex Cloud is implementing. GitHub `prototype/v0.2` is authoritative. Local Windows is for Android SDK/build and physical-device validation when required.

## 2. Authoritative intent

T128 establishes the narrow data-safety foundation approved for Phase 13:

1. maintain at most one internal encrypted Last-Known-Good (LKG) KDBX artifact, and exactly one once a previous committed valid active state exists to preserve;
2. serialize a candidate vault to an app-private temporary encrypted KDBX;
3. validate that candidate by decoding/opening it with the intended credentials **before** changing the active vault or existing LKG;
4. after validation, promote the candidate while rotating the prior valid active vault into the single LKG slot;
5. preserve the previous valid active vault and previous LKG deterministically if validation or promotion fails;
6. allow an active-corrupt + valid-LKG condition to be distinguishable for a later explicit restore offer, without silently replacing the active vault.

This task is the foundation only. Full restore orchestration is T129.

## 3. Preconditions before implementation

Before editing implementation files, Codex MUST:

1. fetch authoritative `prototype/v0.2`;
2. confirm the branch relation to `BASELINE_COMMIT` and report any unexpected divergence;
3. read `TASKS.md`, `docs/GOVERNANCE_STATUS.md`, this kit, `TSD.md`, and `docs/adr/0005-data-safety-kdbx-lkg-saf.md`;
4. verify governance has been explicitly activated for T128;
5. run `python scripts/governance_preflight.py` and stop on failure;
6. verify a clean working tree before implementation;
7. enforce the one-writer rule.

If governance is still `PLANNING_FREEZE / NONE / NONE`, STOP. Do not implement T128.

## 4. Scope — IN

Implementation may cover only the minimum required to make the T128 persistence invariants true:

- refactor the narrow persistence path in `KotpassVaultRepository` so candidate validation occurs before promotion;
- keep candidate, active, and LKG artifacts encrypted KDBX files in app-private storage;
- use one deterministic LKG location/identity; no timestamped/history accumulation;
- on successful mutation, make the immediately previous committed valid active vault the LKG and the validated candidate the new active vault;
- on a fresh vault creation where no prior active state exists, do not invent a synthetic historical LKG; the first later successful mutation may establish the first LKG;
- guarantee candidate-validation failure leaves the current active vault and current LKG untouched;
- guarantee a promotion/file-operation failure does not leave a destructive partial state: the previous valid active and previous valid LKG must remain/restored as the authoritative pair;
- keep enough internal distinction for tests to prove an invalid/corrupt active vault can coexist with a valid LKG without automatic replacement;
- preserve current credential/KDBX behavior and reuse intended credentials only for in-process KDBX validation; do not log, persist, or broaden credential lifetime;
- add deterministic fault-injection only if required to test post-validation promotion failure without relying on flaky OS/file-permission behavior.

## 5. Scope — OUT

Do NOT implement or modify:

- T129 restore orchestration, restore button/action, restore confirmation, or actual LKG-to-active recovery flow;
- T130 reset-to-fresh-vault behavior;
- T131 SAF export;
- T132 SAF import/migration;
- T133 closeout work;
- any UI/navigation changes not strictly required by T128;
- T127 swipe acknowledgment visuals, warning-message appearance, or its already validated behavior/security semantics;
- silent or automatic LKG restoration;
- backup history, multiple retained LKG versions, timestamped backup accumulation, or user-browsable backup management;
- KDBX crypto/KDF policy changes, vault schema/model migrations, credential-policy changes, or new dependency introduction;
- opportunistic refactors outside the vault-persistence seam.

## 6. Allowed files

Primary implementation files:

- `app/src/main/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepository.kt`
- `app/src/test/java/com/yogeshpaliyal/keypass/vault/KotpassVaultRepositoryTest.kt`

Conditionally allowed only when necessary for deterministic, focused fault testing:

- one small internal production helper under `app/src/main/java/com/yogeshpaliyal/keypass/vault/` that abstracts only the filesystem operations needed by T128;
- the directly corresponding test file under `app/src/test/java/com/yogeshpaliyal/keypass/vault/`.

Do **not** change `VaultRepository.kt`, UI/view-model files, Gradle/dependency files, KDBX configuration, navigation, or unrelated tests without stopping and requesting an explicit scope decision.

Closure/evidence documentation may be updated only after implementation and validation under the normal task-completion workflow; this kit does not pre-authorize broad documentation changes.

## 7. Required implementation invariants

### 7.1 Candidate validation precedes mutation of active/LKG

The candidate must first be encoded to a temporary encrypted KDBX and then successfully decoded/opened with the intended credentials. Until that validation succeeds:

- active must not be replaced, moved, truncated, or deleted;
- existing LKG must not be replaced, moved, truncated, or deleted.

### 7.2 Exactly-one LKG lifecycle

Once a previous committed valid active state exists:

- the successful next promotion leaves one active vault and one LKG;
- the LKG is the immediately previous committed valid active state;
- later successful promotions replace the single LKG rather than accumulate history;
- stale temporary/transaction artifacts must not become extra retained backups.

### 7.3 Deterministic failure behavior

If candidate validation fails, active and LKG remain unchanged.

If file promotion fails after candidate validation, the operation must resolve back to the prior valid authoritative state rather than silently accept an uncertain partial state. The implementation must be deterministic and unit-testable.

If the filesystem/platform cannot support the required invariant using the current design, STOP and report the exact transaction gap instead of weakening the invariant.

### 7.4 Recovery detection is not recovery orchestration

T128 may establish only the minimum internal distinction necessary to prove:

- active invalid/corrupt;
- the single LKG is valid with the intended credentials;
- no automatic replacement occurs.

If surfacing a future restore offer requires a new public `VaultRepository` API, UI state contract, navigation change, or restore command, STOP and request a scope decision. Those concerns belong to T129 unless separately authorized.

### 7.5 Credential handling

Candidate and LKG validation must use the intended vault credentials without:

- writing plaintext credentials to disk;
- logging credentials;
- introducing a new persistent credential cache;
- extending credential lifetime beyond what is necessary for the current repository operation.

Wrong credentials must not be transformed into an oracle that silently exposes whether a valid LKG exists.

## 8. Required tests

Add focused automated coverage proving at minimum:

1. **Successful promotion** — from valid active A to valid candidate B, active becomes B and LKG becomes A; both remain decryptable KDBX with the intended credentials.
2. **Repeated promotion / LKG replacement** — A -> B -> C leaves active C and exactly one LKG representing B; A is not retained as another backup.
3. **Fresh creation** — first vault creation succeeds without fabricating a historical LKG; first subsequent successful mutation can establish the prior active as LKG.
4. **Candidate-invalid failure** — validation failure occurs before active/LKG mutation and leaves both previous artifacts unchanged.
5. **Promotion failure after validation** — deterministic injected filesystem failure demonstrates previous active and previous LKG remain/restored valid and no backup accumulation occurs.
6. **Active-corrupt + valid-LKG** — repository behavior does not silently replace the corrupt active; the condition is distinguishable in the narrowest internal way needed for later T129 work.
7. **Active-invalid + invalid/no-LKG** — no automatic repair is attempted and normal failure semantics remain deterministic.
8. **Wrong credentials** — wrong-password behavior is not reclassified as a recovery path merely because an LKG file exists.
9. **Encrypted artifacts** — candidate/LKG artifacts used by the transaction are valid encrypted KDBX artifacts, not plaintext serialized vault data.
10. **Regression** — existing create/open/CRUD, metadata, duplicate-create, and relevant vault repository tests remain green.

Prefer byte-level before/after assertions for “untouched” failure cases when practical, supplemented by successful KDBX decode assertions.

## 9. Required validation and evidence

Codex Cloud implementation handoff must include:

- `python scripts/governance_preflight.py` result before implementation and again before handoff;
- focused T128 unit-test results;
- full repository unit-test result (`./gradlew test` or the repository-equivalent command available in Codex Cloud);
- implementation commit SHA;
- concise changed-file list and diff summary;
- proof that only allowed files changed;
- final `git status` showing the writer workspace clean;
- explicit statement that no T129+ or T127 cosmetic work was included.

Local Windows validation after implementation should include:

- `\.\gradlew.bat test` (or the repository's working Windows equivalent);
- `\.\gradlew.bat assembleFreeDebug`;
- Samsung Galaxy A11 / SM-A115F smoke only as needed to confirm normal create/open/mutate/relaunch behavior was not regressed. Failure-injection invariants should remain automated rather than depending on physical-device manipulation.

Do not count a successful build alone as proof of LKG transaction safety.

## 10. Stop conditions

STOP implementation and report instead of improvising if any of these occurs:

- governance is not explicitly activated for T128;
- authoritative branch changed unexpectedly or one-writer ownership is unclear;
- required change escapes the allowed-file boundary;
- a public `VaultRepository` API/UI/navigation change appears necessary to surface restore behavior;
- implementation would need T129/T130/T131/T132/T133 behavior;
- implementation would alter T127 behavior/security semantics or perform the parked cosmetic refinement;
- a new external dependency, crypto/KDF change, schema migration, or credential-policy change appears necessary;
- the current filesystem strategy cannot guarantee prior active + prior LKG preservation on post-validation promotion failure;
- deterministic failure testing would require unsafe/flaky device tricks instead of a narrow injectable seam;
- credential validation would require plaintext persistence, logging, or a broader credential cache;
- governance preflight or required tests fail for reasons not directly resolved within T128 scope.

## 11. Completion handoff

When T128 implementation is complete, do **not** begin T129.

Handoff must provide:

1. implementation SHA;
2. files changed;
3. exact validation commands/results;
4. mapping from tests to T128 invariants;
5. any residual risks or limitations;
6. physical-device result if performed;
7. confirmation that no T129+ behavior or T127 cosmetic refinement was introduced.

Only after acceptance/closure should governance return to:

- `EXECUTION_STATUS: PLANNING_FREEZE`
- `ACTIVE_TASK: NONE`
- `ACTIVE_KIT: NONE`

The next task remains unavailable until its own kit is READY and the user explicitly authorizes activation.
