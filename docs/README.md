# RAHSA Documentation Index

Use this index to distinguish current authority, planning state, and historical material.

## Start here

1. `docs/GOVERNANCE_STATUS.md` — current workstream, execution gate, Phase 13 planning state.
2. `TASKS.md` — machine-readable active-task/kit gate and draft task definitions.
3. `PRD.md` — approved v0.2 baseline + approved Phase 13 product amendment.
4. `TSD.md` — approved v0.2 technical baseline + Phase 13 technical draft.
5. `docs/THREAT_MODEL.md` — security boundaries and Phase 13 threat extension.

## Source-of-truth hierarchy

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
Implementation Kit
        ↓
       Code
```

## Current Phase 13 planning documents

- `PRD.md` — product requirements approved by owner.
- `TSD.md` — technical design draft; owner review required.
- `docs/adr/0005-data-safety-kdbx-lkg-saf.md` — proposed data-safety/storage decision.
- `docs/adr/0006-biometric-quick-unlock-keystore.md` — proposed biometric/Keystore decision.
- `docs/THREAT_MODEL.md` — Phase 13 threat extension draft.
- `TASKS.md` — T126–T133 draft definitions; **not active**.
- `docs/ROADMAP.md` / `docs/FEATURES.md` — current sequencing/status inventory.

## Execution / workflow documents

- `AGENTS.md` — Codex repository instructions.
- `docs/WORKFLOW.md` — planning/preparation/execution process.
- `docs/IMPLEMENTATION_KIT.md` — JIT kit standard.
- `docs/IMPLEMENTATION_KIT_TEMPLATE.md` — kit manifest template.
- `implementation-kits/README.md` — kit directory rules.

No implementation is authorized while `TASKS.md` shows `PLANNING_FREEZE / NONE / NONE`.

## Historical / supporting documents

Prototype-era completed task checklists and rollout notes remain available in Git history and at `v0.2-prototype`. They are evidence/reference, not current execution instructions when superseded by the documents above.

Public/Play Store release documents remain parked until the owner explicitly resumes that workstream.
