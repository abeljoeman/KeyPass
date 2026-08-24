# Engineering Principles — Android Password Manager Prototype

**Version:** 1.0  
**Ratified:** 2026-08-24

These principles govern implementation decisions for the prototype.

If another document conflicts with this file, this file takes precedence unless explicitly amended.

## I. Reuse First

Before implementing a new component, check in this order:

1. Is a suitable implementation already present in the retained KeyPass code?
2. Does Android/Jetpack provide the capability?
3. Does a mature, license-compatible open-source library provide it?
4. Only then implement it locally.

Reuse must not bypass code review.

## II. No Custom Cryptography

Do not invent:

- Cryptographic algorithms
- Encryption modes
- Key derivation algorithms
- Encrypted file/container formats
- Authentication/tagging schemes

Use established platform APIs and established libraries/formats.

For persisted credentials, the prototype uses KDBX through Kotpass.

## III. Prototype Scope Is Binding

Only implement functionality described in `PRD.md`.

Do not add "useful" adjacent features without an explicit scope change.

Especially avoid:

- Cloud sync
- Accounts
- TOTP
- Passkeys
- Autofill
- Browser extensions
- Analytics
- Backend APIs

## IV. Local First

Core prototype behavior must work without a network.

Do not add network dependencies unless the product requirement explicitly changes.

Prefer no Android INTERNET permission.

## V. Security Over Convenience

The prototype may be visually incomplete, but it may not knowingly:

- Store passwords in plaintext.
- Persist the master password in plaintext.
- Log secret values.
- Disable vault integrity checks.
- Silently continue after vault-decryption failure.
- Replace a corrupted vault without user-visible failure.

## VI. Simplicity / YAGNI

Prefer the smallest structure that satisfies the requirement.

Avoid:

- Premature multi-module architecture
- Layers with no current responsibility
- Generic frameworks created for one use
- New dependencies for trivial helpers
- Large refactors unrelated to the active task
- Abstractions added "for future scalability"

## VII. Explicit Dependencies

Every new third-party dependency must have:

- Purpose
- License
- Source repository
- Why existing code/platform API is insufficient

Security-sensitive dependencies require additional review.

## VIII. Test Important Boundaries

Tests should focus on the highest-risk behavior:

- Vault create/open/write
- Wrong password
- Credential mapping
- CRUD persistence
- Lock/unlock
- Corrupted vault handling
- Sensitive-data leakage

Do not chase coverage percentage for its own sake.

## IX. Small AI-Agent Tasks

Codex/DeepSeek tasks should:

- Reference one task ID from `TASKS.md`.
- Reference `PRD.md` and `TSD.md`.
- Avoid modifying unrelated files.
- Include acceptance criteria.
- End with a build/test command.

Do not prompt an agent to "build the whole password manager."

## X. Documentation Is Part of the Codebase

Architecture and product decisions belong in Markdown and version control.

When a significant decision changes:

1. Update or supersede the relevant ADR.
2. Update `TSD.md` if architecture changes.
3. Update `PRD.md` if product scope changes.
4. Regenerate/revise `TASKS.md` accordingly.

## Governance

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
     TASKS.md
        ↓
       Code
```

A code change that violates a higher-level document is not accepted merely because it builds.
