# Engineering Principles — RAHSA

**Version:** 1.1  
**Updated:** 2026-09-06

These principles govern engineering decisions for RAHSA. They supersede prototype-era governance where the two conflict.

## I. Product terminology

- **RAHSA** is the current product/application name.
- **KeyPass** means the upstream open-source project or a legacy technical identifier that still contains `keypass`.
- New product requirements, roadmap items, tasks, and user-facing documentation MUST use RAHSA unless they are explicitly discussing upstream KeyPass or historical code identifiers.

## II. Reuse First

Before implementing a new capability, check in this order:

1. Suitable implementation already retained in RAHSA.
2. Suitable implementation/pattern in upstream KeyPass.
3. Android / AndroidX / Jetpack platform capability.
4. Mature, actively maintained, license-compatible open-source library or implementation.
5. Only then implement the minimum required code locally.

Reuse is not blind copying. Security-sensitive inherited or third-party code MUST be reviewed before use.

## III. No Custom Cryptography

Do not invent cryptographic algorithms, encryption modes, key-derivation schemes, encrypted containers, recovery formats, or authentication/tagging schemes.

Use established platform APIs and established libraries/formats. Persisted credential data remains KDBX-backed through Kotpass unless an explicitly approved architecture decision changes that.

## IV. Approved Scope Controls Implementation

The current approved `PRD.md` controls product scope.

Prototype-era non-goals were boundaries for those historical phases; they are **not permanent product bans**. A previously excluded feature may enter implementation only after explicit owner approval and the required PRD/TSD/ADR/threat-model updates are complete.

An idea, roadmap candidate, inherited code path, or unchecked historical item is not implementation approval.

## V. Preserve the v0.2 Baseline

`v0.2-prototype` is the stable RAHSA baseline.

Existing correct behavior is presumed preserved unless an approved requirement explicitly changes it. In particular, preserve:

- KDBX/Kotpass as the persisted source of truth.
- `VaultRepository` as the vault boundary unless an approved TSD/ADR changes it.
- Fail-closed vault behavior.
- Regression-safe CRUD, search, generator, lock, clipboard, and corrupt-vault protections.
- Local-first operation.

Do not perform unrelated architecture migrations or rewrites while adding a feature.

## VI. Local First

Core RAHSA behavior should work without a network. Do not add network dependencies or Android `INTERNET` permission without an explicitly approved requirement and documented justification.

## VII. Security Over Convenience

RAHSA may not knowingly:

- Store credential values in plaintext persistence.
- Persist a master password in plaintext.
- Log secrets.
- Bypass vault integrity/decryption checks.
- Continue as unlocked after authentication/decryption failure.
- Replace a corrupt or unreadable vault silently.
- Treat biometric success as vault unlock unless the vault is actually unlocked through the approved security design.

## VIII. Simplicity / YAGNI

Prefer the smallest structure that satisfies an approved requirement. Avoid premature layers, generic frameworks for one use, new dependencies for trivial helpers, broad refactors unrelated to the active task, and abstractions added only for hypothetical future scale.

## IX. Explicit Open-Source Dependencies

Every new third-party dependency MUST record:

- Purpose.
- Source repository/project.
- License.
- Why retained RAHSA code, upstream KeyPass, or Android/Jetpack is insufficient.
- Security/privacy impact where relevant.

Security-sensitive dependencies require additional review.

## X. Test Important Boundaries

Prioritize tests around security and persistence boundaries: vault create/open/re-key/write, wrong credentials, credential mapping, CRUD persistence, lock/unlock, lifecycle races, corrupted vaults, recovery/failure paths, biometric security boundaries, and sensitive-data leakage.

Do not chase coverage percentage for its own sake.

## XI. Planning and Implementation Are Separate

Product/security planning is completed before Codex implementation.

Planning flow:

```text
Discussion/research
→ PRD
→ TSD / ADR / Threat Model as needed
→ TASKS.md
→ owner approval
```

Implementation flow:

```text
approved active Txxx
→ Codex executes exactly one task
→ build/test/validation
→ focused checkpoint
```

Codex MUST NOT invent product scope or architecture during implementation. If an active task requires contradicting a higher-level decision, stop and return the conflict to planning.

## XII. Cost-Effective AI Development

Use the lowest-cost model and lowest reasoning effort reasonably likely to complete the task correctly and safely. Do not default to the strongest model.

- Inspection, status, grep, smoke tests, mechanical work: prefer Luna + low.
- Focused normal implementation: prefer Terra + medium.
- Security-sensitive vault/master-password/authentication semantics, difficult lifecycle races, or architecture-critical problems: consider Sol with a justified reasoning level.
- `xhigh`/`max` require an explicit reason.

Correctness and security take precedence over token savings, but higher cost requires a concrete expected benefit.

## XIII. Public Release Work Is Parked

Play Store/public-release preparation, trademark clearance, package/applicationId migration, release signing/store identity, listing identity, and final release compliance work are PARKED until the owner explicitly resumes that workstream.

## XIV. Documentation Is Part of the Codebase

Significant decisions MUST be reflected in version-controlled source-of-truth documents before implementation.

Authority order:

```text
ENGINEERING_PRINCIPLES.md
        ↓
      PRD.md
        ↓
      TSD.md
        ↓
      ADRs
        ↓
docs/THREAT_MODEL.md
        ↓
     TASKS.md
        ↓
       Code
```

`AGENTS.md`, `docs/WORKFLOW.md`, and `docs/GOVERNANCE_STATUS.md` govern execution mechanics/status and MUST NOT override product/security decisions above.

A code change that violates a higher-level document is not accepted merely because it builds.
