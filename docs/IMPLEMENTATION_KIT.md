# RAHSA Implementation Kit Standard

**Status:** Current execution standard  
**Updated:** 2026-09-06

An **Implementation Kit** is a just-in-time execution package prepared for exactly one approved RAHSA task (`Txxx`). Its purpose is to reduce repeated repository exploration and code-generation cost while preserving independent Codex review, security verification, build/test validation, and focused checkpoints.

An Implementation Kit is an execution aid. It is **not** a product, architecture, security, or task authority.

## 1. Authority

If anything in a kit conflicts with a higher-level source of truth, the kit loses.

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

A prepared patch that builds is still unacceptable if it violates the approved task or any higher-level document.

## 2. When a kit may be prepared

A kit may be prepared only after:

1. the product/security decisions required by the capability are approved;
2. required PRD/TSD/ADR/Threat Model updates are complete;
3. the target `Txxx` has a complete, owner-approved task definition;
4. the latest task checkpoint/HEAD is known.

Preparing a kit does **not** authorize application-code execution. The repository remains blocked until `TASKS.md` activates the exact Txxx and references its kit.

## 3. Just-in-time rule

Prepare the kit for the **next task only**, against the latest verified checkpoint.

Do not pre-generate implementation patches for an entire phase by default. Later-task patches become stale easily after earlier task commits and create false confidence.

After Codex completes Txxx and returns a checkpoint, prepare the next task's kit against that new checkpoint.

## 4. Standard location

Each active task uses:

```text
implementation-kits/
└── Txxx/
    ├── README.md             # required manifest
    ├── Txxx.patch            # optional
    ├── apply-Txxx.ps1        # optional
    └── verify-Txxx.ps1       # optional
```

Only `README.md` is mandatory. A kit without a patch is valid when direct Codex implementation is safer or more efficient.

## 5. Required manifest

The first lines of `implementation-kits/Txxx/README.md` must contain:

```text
TASK_ID: Txxx
KIT_STATUS: READY
PREPARED_AGAINST: <checkpoint commit SHA>
RISK_CLASS: LOW | NORMAL | SECURITY_SENSITIVE
MODEL_RECOMMENDATION: <model>
REASONING_RECOMMENDATION: <effort>
```

The manifest must then state, concisely:

- objective and exact task reference;
- source-of-truth documents;
- prepared-against checkpoint/branch;
- expected/allowed files;
- reuse findings already verified;
- security constraints and explicit out-of-scope boundaries;
- patch/script inventory, if any;
- exact apply/review steps;
- build/test commands;
- smoke/device validation when required;
- known assumptions and stale-kit conditions;
- rollback/fallback notes when relevant.

Do not duplicate long PRD/TSD/ADR text. Link to exact repository sections instead.

## 6. Patch rules

A `.patch` is optional and is most useful for deterministic, narrow changes.

When a patch exists:

- it must be prepared against the checkpoint recorded in the manifest;
- Codex must inspect the affected current files before trusting it;
- run `git apply --check <patch>` before applying when practical;
- never force a patch through conflicts;
- review the full resulting diff after application;
- a stale patch may be adapted only within the exact approved task and only when the adaptation does not introduce a new product/security/architecture decision;
- otherwise stop and return the task for kit refresh/planning review.

The patch must not include unrelated formatting, broad refactors, generated secrets, credentials, keystores, production vaults, or release-signing material.

## 7. PowerShell helper rules

A task-specific `.ps1` is optional and should be used for deterministic mechanical work or repeatable validation.

Helpers must:

- use explicit known paths rather than broad wildcards;
- fail fast on unexpected state;
- preserve source encoding and line endings where relevant;
- avoid storing or printing secrets;
- avoid network access unless the approved task explicitly requires it;
- avoid `git add .`, broad staging, automatic commit, automatic push, or silent task completion;
- not bypass build/test/governance checks;
- be idempotent where practical, or explicitly reject a second run when not safe;
- make the resulting diff inspectable by Codex.

Recommended PowerShell safety defaults where appropriate:

```powershell
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
```

## 8. Codex responsibility

Even with a complete kit, Codex remains responsible for:

1. reading `AGENTS.md` and current governance;
2. verifying branch/HEAD/working tree;
3. reading the exact active Txxx and kit manifest;
4. running governance preflight;
5. validating the kit against current source;
6. reviewing all applied changes;
7. running required build/tests;
8. running smoke/device validation when required;
9. fixing only narrow task-owned issues;
10. marking only the active task complete after validation;
11. creating one focused checkpoint;
12. resetting the execution gate and stopping.

A kit reduces exploration. It does not transfer correctness responsibility away from Codex.

## 9. Cost-aware model selection with kits

Kit completeness may reduce exploration/coding cost, but it does **not** reduce the task's inherent security risk.

Typical starting points:

- deterministic low-risk task with full patch/script → Luna + low;
- low-risk task needing limited adaptation → Luna + low/medium;
- normal focused implementation/review → Terra + medium;
- security-sensitive vault/master-password/authentication/Keystore/recovery semantics → retain the security-appropriate model/reasoning even if a patch is supplied; consider Sol when justified;
- difficult concurrency/lifecycle/architecture problem or failed cheaper attempt → escalate with explicit reason.

Never select a cheaper model solely because a patch exists when independent security reasoning is still required.

## 10. Task activation

When the next task and kit are ready for Codex, `TASKS.md` must expose exactly:

```text
EXECUTION_STATUS: ACTIVE_IMPLEMENTATION
ACTIVE_TASK: Txxx
ACTIVE_KIT: implementation-kits/Txxx/README.md
```

Codex then runs:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

The preflight must fail closed if the task or kit reference is missing/mismatched.

## 11. End-of-task gate

After Txxx passes its required validation and is committed, the same checkpoint must return `TASKS.md` to:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

This prevents Codex from automatically starting the next task.

The next task requires a new just-in-time kit and explicit activation.

## 12. What should be stored in a kit

Store only information that materially reduces exploration or prevents likely mistakes.

Good kit content:

- exact file paths;
- verified reuse findings;
- a small patch for mechanical changes;
- a deterministic helper script;
- concise implementation notes;
- targeted test/build commands;
- task-specific failure cases.

Avoid large narrative duplication, speculative architecture, or broad repository dumps. The optimization goal is **lowest expected total cost while preserving correctness and security**.
