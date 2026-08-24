# 0002 — Reuse KeyPass as Application Reference/Base

**Status:** Accepted  
**Date:** 2026-08-24

## Context and Problem Statement

Building the complete Android shell from scratch would duplicate existing open-source work. KeyPass already provides Android password-manager UI, navigation, password generation, authentication-related components, and other useful application plumbing.

## Considered Options

- Build a new Android app from scratch.
- Fork/adapt KeyPass.
- Fork a larger mature password manager such as KeePassDX.

## Decision Outcome

Use **KeyPass as the primary application reference/base**, then remove features outside prototype scope.

Security-sensitive implementation is reviewed before reuse.

## Consequences

Good:

- Faster prototype.
- Existing Android/Compose structure.
- Less boilerplate.
- Reuse-first principle is satisfied.

Bad:

- Inherited complexity must be removed carefully.
- Some existing security/storage code must not be reused blindly.
- Upstream structure may not perfectly match the final product.
