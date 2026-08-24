# Documentation Guide

This repository uses a deliberately small documentation set.

## Authority Order

1. `ENGINEERING_PRINCIPLES.md`
2. `PRD.md`
3. `TSD.md`
4. `docs/adr/*.md`
5. `TASKS.md`
6. Implementation code

If documents conflict, resolve the higher-authority document first.

## Files

### `PRD.md`

Defines what the prototype must do and what is out of scope.

### `TSD.md`

Defines how the prototype is implemented.

### `TASKS.md`

Executable implementation backlog for humans and AI coding agents.

### `ENGINEERING_PRINCIPLES.md`

Non-negotiable engineering rules.

### `docs/THREAT_MODEL.md`

Security assets, threats, trust boundaries, and mitigations.

### `docs/TEST_PLAN.md`

Physical-device and prototype release checklist.

### `docs/adr/`

Small Architecture Decision Records for choices that should not be repeatedly re-litigated.

## Recommended AI-Agent Prompt Pattern

```text
Implement TASKS.md task T0XX.

Read and obey:
1. ENGINEERING_PRINCIPLES.md
2. PRD.md
3. TSD.md
4. relevant docs/adr files

Constraints:
- Do not implement adjacent tasks.
- Reuse existing code before creating new code.
- Do not introduce a new dependency without justification.
- Do not invent cryptographic behavior.
- Run the relevant build/tests before finishing.
- Summarize changed files and remaining risks.
```

## Template Sources / Inspiration

This documentation set is adapted from ideas in:

- GitHub Spec Kit — https://github.com/github/spec-kit
- Markdown Architectural Decision Records (MADR) — https://github.com/adr/madr
- OWASP Threat Modeling Playbook — https://github.com/OWASP/threat-modeling-playbook

The files here are intentionally simplified for a prototype.
