# 0004 — No Backend or Cloud Sync for Prototype

**Status:** Accepted  
**Date:** 2026-08-24

## Context and Problem Statement

The first milestone is to validate the local password-manager experience. Backend, account, and sync functionality would add authentication, API, infrastructure, conflict-resolution, monitoring, and additional security scope.

## Considered Options

- Backend from the beginning
- Optional cloud backup
- Fully local prototype

## Decision Outcome

Build a **fully local prototype with no backend and no cloud sync**.

## Consequences

Good:

- Much smaller implementation scope.
- No server operating cost.
- Smaller attack surface.
- Faster validation of the core UX.

Bad:

- No multi-device access.
- No remote recovery.
- No cloud backup in prototype.
