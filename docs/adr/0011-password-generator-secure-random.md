# ADR 0011 — Password Generator Uses SecureRandom over a Combined Allowed Alphabet

**Status:** Accepted — owner-approved Phase 13 architecture  
**Date:** 2026-09-06

## Context

RAHSA's current password generator uses Kotlin default `.random()` selection. Password generation is a security-sensitive operation even though the feature is intentionally low-priority and its existing UX should remain unchanged.

The owner approved a minimal hardening: use `java.security.SecureRandom` directly and select each generated character from one combined alphabet built from the currently enabled character categories.

## Decision

1. Production password generation uses `java.security.SecureRandom` as the cryptographically secure random source.
2. Build one allowed-character alphabet from the existing enabled categories (uppercase, lowercase, numbers, symbols, and any already-supported category semantics).
3. For each output position, choose one index using secure bounded selection (`SecureRandom.nextInt(alphabet.size)` or equivalent) and append that character.
4. Do not use a category-first random choice followed by a second per-category character choice.
5. Do not add a requirement that every enabled category must appear at least once; existing category switches define the allowed alphabet, not a new mandatory-composition policy.
6. Preserve existing requested length, configuration persistence, generator entry points, and UI behavior.
7. Do not add passphrase generation, password-policy enforcement, strength UI, new dependencies, or a generic cryptography/randomness framework.
8. Keep implementation small and directly testable. If a narrow seam is needed for deterministic tests, it must not weaken the production `SecureRandom` default or become a general-purpose abstraction.

## Consequences

### Positive

- Secret generation uses a CSPRNG appropriate for security-sensitive randomness.
- Combined-alphabet selection avoids category-first probability distortion across differently sized categories.
- No dependency, UX, or architecture expansion is required.
- Scope remains consistent with the owner's principle: low usage, low effort, security hygiene.

### Costs / limitations

- This does not guarantee at least one character from every enabled category.
- It does not add password-strength education, passphrases, or policy enforcement.
- Tests must validate allowed-character and length behavior without asserting deterministic production output.

## Rejected alternatives

- Keep Kotlin default `.random()` — not appropriate for secret generation.
- Category-first selection — creates unequal per-character probabilities when category sizes differ and is unnecessary for the approved behavior.
- Guarantee every enabled category appears — changes generator semantics and adds an unapproved complexity/composition policy.
- Add a third-party randomness library — unnecessary; Java `SecureRandom` is sufficient.
- Redesign generator UI or add passphrases — out of Phase 13 scope.
