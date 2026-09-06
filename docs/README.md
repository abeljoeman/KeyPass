# RAHSA Documentation Index

Use this index to distinguish current authority, planning state, and historical material.

## Start here

1. `docs/GOVERNANCE_STATUS.md` — current workstream, execution gate, accepted Phase 13 architecture state.
2. `TASKS.md` — machine-readable active-task/kit gate and T126–T133 task definitions.
3. `PRD.md` — approved v0.2 baseline + approved Phase 13 product amendment.
4. `TSD.md` — approved v0.2 technical baseline + owner-approved consolidated Phase 13 technical design.
5. `docs/THREAT_MODEL.md` — owner-approved consolidated Phase 13 security boundaries and implementation validation gates.

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

- `PRD.md` — Phase 13 product requirements approved by owner.
- `TSD.md` — consolidated Phase 13 technical design approved by owner.
- `docs/THREAT_MODEL.md` — consolidated Phase 13 threat model approved by owner; implementation/device validation gates remain unchecked until execution.
- `TASKS.md` — T126–T133 approved task definitions; **no task active**.

Accepted Phase 13 ADRs:

- `docs/adr/0005-data-safety-kdbx-lkg-saf.md`
- `docs/adr/0006-biometric-quick-unlock-keystore.md`
- `docs/adr/0007-master-password-change-crash-consistency.md`
- `docs/adr/0008-resumable-destructive-reset.md`
- `docs/adr/0009-manual-kdbx-backup-via-saf.md`
- `docs/adr/0010-safe-kdbx-restore-finalization.md`
- `docs/adr/0011-password-generator-secure-random.md`
- `docs/adr/0012-create-vault-recovery-acknowledgment.md`

All ADRs above are **Accepted — owner-approved Phase 13 architecture**.

`docs/ROADMAP.md` and `docs/FEATURES.md` remain the current sequencing/status inventory.

## Current execution gate

No implementation is authorized while `TASKS.md` shows:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

The consolidated Phase 13 technical package is approved. The next workflow step is JIT preparation for **T126 only**. T126 is not activated until its repository-local Implementation Kit is READY and the execution gate is explicitly changed.

## Execution / workflow documents

- `AGENTS.md` — Codex repository instructions.
- `docs/WORKFLOW.md` — planning/preparation/execution process.
- `docs/IMPLEMENTATION_KIT.md` — JIT kit standard.
- `docs/IMPLEMENTATION_KIT_TEMPLATE.md` — kit manifest template.
- `implementation-kits/README.md` — kit directory rules.

## Historical / supporting documents

Prototype-era completed task checklists and rollout notes remain available in Git history and at `v0.2-prototype`. They are evidence/reference, not current execution instructions when superseded by the documents above.

Public/Play Store release documents remain parked until the owner explicitly resumes that workstream.
