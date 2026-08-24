# 0001 — Use Android Native Kotlin and Jetpack Compose

**Status:** Accepted  
**Date:** 2026-08-24

## Context and Problem Statement

The prototype targets Android first. Existing candidate code and libraries are Android/Kotlin based, and the current goal is maximum reuse with minimum new infrastructure.

## Considered Options

- Android native with Kotlin + Jetpack Compose
- Flutter
- React Native

## Decision Outcome

Use **Android native with Kotlin + Jetpack Compose**.

## Consequences

Good:

- Maximum reuse of KeyPass code.
- Direct access to Android platform APIs.
- No cross-platform bridge for security-sensitive Android behavior.
- Smaller prototype migration effort.

Bad:

- iOS would require a separate implementation later.
