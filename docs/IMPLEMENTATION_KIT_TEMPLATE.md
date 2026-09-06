# RAHSA Implementation Kit — Txxx

TASK_ID: Txxx
KIT_STATUS: READY
PREPARED_AGAINST: <checkpoint commit SHA>
RISK_CLASS: LOW | NORMAL | SECURITY_SENSITIVE
MODEL_RECOMMENDATION: <model>
REASONING_RECOMMENDATION: <effort>

## Objective

Reference the exact Txxx in `TASKS.md` and summarize only the task-owned change.

## Source of truth

- `PRD.md`: <section>
- `TSD.md`: <section>
- ADR: <path/section if applicable>
- `docs/THREAT_MODEL.md`: <section if applicable>

## Prepared against

- Branch: <branch>
- Checkpoint: <sha + subject>
- Expected working tree: clean

## Expected / allowed files

- `<path>`

Any additional file requires task-scope justification.

## Reuse findings

1. Retained RAHSA: <finding>
2. Upstream KeyPass: <finding>
3. Android/AndroidX/Jetpack: <finding>
4. OSS dependency: <finding or none>
5. Local implementation: <why needed if applicable>

## Security / invariants

- <constraint>

## Out of scope

- <explicit boundary>

## Prepared artifacts

- Patch: none | `Txxx.patch`
- Apply helper: none | `apply-Txxx.ps1`
- Verify helper: none | `verify-Txxx.ps1`

## Apply / review

```powershell
python scripts/governance_preflight.py --implementation Txxx
# optional: git apply --check implementation-kits/Txxx/Txxx.patch
# optional: .\implementation-kits\Txxx\apply-Txxx.ps1
git diff --check
git diff
```

Do not force a stale patch. If current source invalidates a security/product/architecture assumption, stop and return for planning/kit refresh.

## Build / test

```powershell
<targeted commands>
```

## Smoke / device validation

- <scenario or not required>

## Known stale-kit conditions

- Any prior task changes a file/semantic assumption this kit relies on.
- HEAD/application source differs materially from the prepared-against checkpoint.
- The approved Txxx or its source-of-truth documents change.

## Completion

After validation, complete only Txxx, create one focused checkpoint, reset `TASKS.md` to `PLANNING_FREEZE / NONE / NONE`, report the checkpoint, and stop.
