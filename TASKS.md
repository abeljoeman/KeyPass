# RAHSA Implementation Tasks

**EXECUTION_STATUS: PLANNING_FREEZE**  
**ACTIVE_TASK: NONE**  
**ACTIVE_KIT: NONE**  
**Stable baseline:** `v0.2-prototype` (`5d6acd3 release: prepare v0.2 prototype`)

## Current execution gate

There is currently **no approved implementation task**.

Codex MUST NOT modify application code while `ACTIVE_TASK: NONE` or `ACTIVE_KIT: NONE`.

The next implementation phase will be authored only after product/security planning is complete and the required PRD/TSD/ADR/Threat Model updates are approved.

## Completed historical work

Tasks T001–T125 for the original prototype, KDBX integration, CRUD, security hardening, UI/UX rollout, accessibility, and physical-device validation are complete in the `v0.2-prototype` baseline.

The full historical checklist remains available in Git history and at the `v0.2-prototype` tag. It is not the current execution backlog.

## Product & Security Expansion — planning only

The following are discussion/research candidates, **not approved implementation tasks**:

- Master-password management/change.
- Forgotten-master-password and recovery policy.
- Data safety / backup / restore policy.
- Biometric quick-unlock.
- Lock/session policy refinements.
- Android Autofill.
- Passphrase generation.
- Other owner-approved features identified during planning.

Do not infer implementation scope from this list.

## Parked public-release backlog

The following previously identified Brand/Public Release items are PARKED and are not active work:

- B002 — trademark/name clearance.
- B004 — Android `applicationId` / package identity strategy.
- B005 — signing/store identity and store-facing release identity.
- B006 — final upstream/open-source attribution presentation for release.

Do not execute these items until the owner explicitly resumes public-release planning and new active tasks are approved.

## Rules for future task definitions

A future implementation task MUST be created only after its upstream decisions are approved.

Multiple task definitions for a phase may be drafted and approved during planning, but only one may be active for Codex execution.

Each `Txxx` must include:

1. Exact objective and narrow scope.
2. References to relevant `PRD.md`, `TSD.md`, ADR, and/or `docs/THREAT_MODEL.md` sections.
3. Acceptance criteria.
4. Build/test/validation expectations.
5. Security notes where applicable.
6. Explicit out-of-scope boundaries.
7. Reuse/dependency expectations where relevant.
8. Expected/allowed files where practical.

## Just-in-time Implementation Kit gate

Before activating the next Txxx, ChatGPT prepares a just-in-time Implementation Kit against the latest verified checkpoint using `docs/IMPLEMENTATION_KIT.md`.

Every active task requires:

```text
implementation-kits/Txxx/README.md
```

The manifest is mandatory. Patch and PowerShell helper files are optional.

Do not pre-generate full implementation patches for all phase tasks by default; prepare the next task's kit after the previous task checkpoint to reduce staleness.

## Activating exactly one task

When owner-approved Txxx and its kit are ready for Codex, change the machine-readable header to:

```text
EXECUTION_STATUS: ACTIVE_IMPLEMENTATION
ACTIVE_TASK: Txxx
ACTIVE_KIT: implementation-kits/Txxx/README.md
```

The active kit is a non-authoritative execution aid. This task definition and higher-level source-of-truth documents remain authoritative.

## Codex preflight

Before implementation Codex must run:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

A failed preflight is a governance blocker, not something to bypass.

Preflight must verify that:

- execution status is active;
- requested Txxx matches `ACTIVE_TASK`;
- `ACTIVE_KIT` is present and points to a repository-local manifest;
- the manifest identifies the same Txxx and is marked `KIT_STATUS: READY`.

## End of each task

After the active Txxx passes its required build/test/validation:

1. mark only that Txxx complete;
2. reset the machine-readable header to:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

3. stage/review only task-owned changes;
4. create one focused checkpoint;
5. report the checkpoint and stop.

Codex MUST NOT automatically activate or start the next task.

The next task requires a new just-in-time kit prepared against the new checkpoint and explicit activation.
