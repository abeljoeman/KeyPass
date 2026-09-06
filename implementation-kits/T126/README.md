# RAHSA Implementation Kit — T126

TASK_ID: T126
KIT_STATUS: READY
PREPARED_AGAINST: 0509b2323e0481792f28e050398c906a539087a6
RISK_CLASS: LOW
MODEL_RECOMMENDATION: Luna
REASONING_RECOMMENDATION: low

## Objective

Implement only `T126 — Harden Password Generator randomness` from `TASKS.md`: replace non-security/default Kotlin random selection in `PasswordGenerator` with direct `java.security.SecureRandom` bounded selection from one combined allowed-character alphabet, preserving existing generator UX/configuration and adding targeted unit coverage.

## Source of truth

- `PRD.md`: Phase 13 Password Generator security-hardening requirements (`P13-FR-001..004`).
- `TSD.md`: §8 `Password Generator hardening — T126 / ADR 0011`.
- ADR: `docs/adr/0011-password-generator-secure-random.md`.
- `docs/THREAT_MODEL.md`: T28 and the Phase 13 generator validation gate.
- `TASKS.md`: T126 definition and acceptance criteria.

## Prepared against

- Branch: `prototype/v0.2`
- Checkpoint: `0509b2323e0481792f28e050398c906a539087a6` — `docs: approve phase 13 technical package`
- Expected working tree: clean

## Expected / allowed files

Implementation-owned files:

- `common/src/main/java/com/yogeshpaliyal/common/utils/PasswordGenerator.kt`
- `app/src/test/java/com/yogeshpaliyal/keypass/security/PasswordGeneratorTest.kt`

Governance-only completion file:

- `TASKS.md` — only to mark T126 complete/reset `PLANNING_FREEZE / NONE / NONE` after validation.

Any additional implementation file requires task-scope justification. Do not modify Gradle/dependencies for this task; existing `app` unit-test infrastructure already has JUnit 4 and depends on `:common`.

## Reuse findings

1. Retained RAHSA: current `PasswordGenerator.kt` already owns all password category configuration and is the only production implementation that needs hardening.
2. Upstream KeyPass: no additional upstream mechanism is required; retain the existing public generator/config entry points.
3. Android/Java platform: use `java.security.SecureRandom` directly; no Android-specific crypto wrapper is needed.
4. OSS dependency: none. Do not add one.
5. Local implementation: a narrow combined-alphabet build plus `SecureRandom.nextInt(bound)` character selection is sufficient.

## Security / invariants

- Every generated secret character is selected using `SecureRandom.nextInt(allowedAlphabet.size)` or an equivalent bounded call on `java.security.SecureRandom`.
- Build one allowed alphabet from currently enabled categories: uppercase, lowercase, numbers, configured symbols, and blank space.
- Do not use Kotlin `random()` / default randomness for production secret generation.
- Do not use category-first random selection.
- Enabled categories define allowed characters only; T126 does not guarantee at least one character from each selected category.
- Preserve requested length and existing `PasswordConfig` behavior/entry points.
- When no usable characters are enabled, preserve safe non-crashing empty-output behavior; do not invent a new UI policy in this task.
- Do not log generated passwords or RNG state.
- No dependency, UX, navigation, persistence, vault, or password-policy changes.

## Out of scope

- Passphrase generation.
- Password strength UI or policy.
- Mandatory presence of each enabled category.
- Generator UI redesign.
- Generic RNG/crypto abstraction or dependency injection framework.
- Third-party randomness libraries.
- Changes to Master Password, KDBX, backup/restore, biometric, or vault persistence.

## Prepared artifacts

- Patch: `implementation-kits/T126/T126.patch`
- Apply helper: none
- Verify helper: none

The patch is intentionally narrow and deterministic. Codex must inspect current source and run `git apply --check` before applying. If the patch is stale, Codex may implement/adapt the same approved behavior directly only if no new product/security/architecture decision is needed.

## Apply / review

```powershell
python scripts/governance_preflight.py --implementation T126

git status -sb
git apply --check implementation-kits/T126/T126.patch
git apply implementation-kits/T126/T126.patch

git diff --check
git diff -- common/src/main/java/com/yogeshpaliyal/common/utils/PasswordGenerator.kt app/src/test/java/com/yogeshpaliyal/keypass/security/PasswordGeneratorTest.kt
```

Codex must independently verify that no production `.random()` remains in `PasswordGenerator.generatePassword()` and that the resulting implementation selects directly from the combined alphabet.

## Build / test

Run targeted unit tests first:

```powershell
.\gradlew.bat :app:testFreeDebugUnitTest --tests "com.yogeshpaliyal.keypass.security.PasswordGeneratorTest"
```

Then build the approved smoke variant:

```powershell
.\gradlew.bat :app:assembleFreeDebug
```

Finally:

```powershell
git diff --check
git status -sb
```

## Smoke / device validation

Physical-device validation is not required for T126 because there is no UI/platform-device behavior change. A lightweight manual generator smoke is optional after the build, but unit tests + `assembleFreeDebug` are the required validation.

## Known stale-kit conditions

- HEAD/application source differs materially from prepared checkpoint `0509b232...` before this kit is applied.
- `PasswordGenerator.kt` or `PasswordConfig` semantics are changed by another task before T126 executes.
- T126, ADR 0011, TSD §8, or the Threat Model generator requirement changes.
- Existing app unit-test/JUnit infrastructure is removed or materially changed.

If a stale condition changes a security/product assumption, stop and return for kit refresh rather than broadening scope.

## Rollback / fallback

Before commit, normal `git restore` of the two implementation-owned files is sufficient. No data migration, persistent state, dependency, or schema rollback exists for T126.

## Completion

After targeted tests and `:app:assembleFreeDebug` pass, complete only T126, create one focused checkpoint, reset `TASKS.md` to:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

Report the checkpoint and stop. Do not begin T127; its JIT kit must be prepared separately against the new checkpoint.
