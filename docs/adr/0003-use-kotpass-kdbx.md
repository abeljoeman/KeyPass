# 0003 — Use Kotpass and KDBX for Persisted Vault Storage

**Status:** Accepted  
**Date:** 2026-08-24

## Context and Problem Statement

The application needs encrypted local credential persistence. Creating a custom encrypted database/container would create unnecessary security-critical code and future compatibility burden.

## Considered Options

- Room database with custom field encryption
- SQLCipher
- Custom encrypted JSON/file format
- Kotpass + KeePass KDBX

## Decision Outcome

Use **Kotpass + KDBX** as the persisted vault mechanism.

The app will implement only a thin mapping/repository layer around the library.

## Consequences

Good:

- Avoids inventing an encrypted format.
- Reduces custom security-critical code.
- Uses an established KeePass-compatible format.
- Keeps backend/database infrastructure unnecessary.

Bad:

- Application model must be mapped to KDBX entries.
- The project depends on Kotpass behavior and compatibility.
- KDBX concepts may expose more capability than the prototype requires.
