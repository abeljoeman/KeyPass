# KeyPass Agent Instructions

## Cost-Aware Codex Model & Reasoning Policy

Cost awareness is mandatory for every Codex task.

Always choose the lowest-cost model and lowest reasoning effort that is reasonably likely to complete the task correctly. Do not default to the strongest model or highest reasoning effort merely because they are available.

Before every Codex implementation, review, debugging, inspection, or test task:

1. Classify the task by complexity, security sensitivity, uncertainty, and expected exploration.
2. Choose the cheapest model likely to succeed.
3. Choose the lowest reasoning effort likely to succeed.
4. State the selected model, reasoning effort, and a brief cost-effectiveness reason.
5. Escalate only when the cheaper choice is inadequate, risk materially requires it, or an earlier attempt failed.

### Model selection

- `gpt-5.6-luna`
  - Preferred for smoke tests, repository inspection, grep/search, mechanical edits, formatting, straightforward tests, and narrow low-risk work.

- `gpt-5.6-terra`
  - Default for normal implementation requiring coding judgment with clear scope and moderate complexity.

- `gpt-5.6-sol`
  - Reserve for security-sensitive work, vault/master-password semantics, concurrency or lifecycle races, architecture decisions, difficult multi-file debugging, or cases where cheaper models are insufficient.

Do not use `gpt-5.6-sol` automatically for ordinary implementation.

### Reasoning effort

When supported by the installed Codex version:

- `none`: truly trivial deterministic work needing essentially no planning.
- `low`: smoke tests, inspections, searches, narrow mechanical work.
- `medium`: normal implementation/debugging with clear scope.
- `high`: security-sensitive logic, concurrency, lifecycle/state interactions, or difficult debugging.
- `xhigh`: only when `high` is plausibly insufficient.
- `max`: exceptional long-horizon critical investigation only.

Never increase reasoning effort merely because a higher option exists.

### Practical starting points

- Read-only smoke test/status/simple inspection:
  `gpt-5.6-luna` + `low`

- Mechanical edit or very small test:
  `gpt-5.6-luna` + `low` or `medium`

- Focused feature implementation:
  `gpt-5.6-terra` + `medium`

- Moderate multi-file refactor:
  `gpt-5.6-terra` + `medium` or `high`

- Security-sensitive vault/master-password/lock-state task:
  evaluate `gpt-5.6-sol`, but start with the lowest sufficient effort rather than assuming `high`

- Complex race, architecture problem, or failed cheaper attempt:
  `gpt-5.6-sol` + `high`

- `xhigh` or `max`:
  require an explicit reason before use

### Cost-control rules

- Keep each Codex prompt narrowly scoped.
- Read only files needed for the current decision.
- Avoid repeated broad repository scans when verified context is still valid.
- Prefer targeted tests before expensive broad validation when risk permits.
- Run broader validation when required by security risk or acceptance criteria.
- Prefer one well-scoped Codex run over multiple speculative runs.
- Do not use multi-agent / Ultra-style execution by default.
- Reuse verified context when safe, but re-check security-critical assumptions.
- A higher-cost model must have a concrete expected benefit.

The goal is not minimum token usage at any cost.

The goal is:

**lowest expected total cost while preserving correctness, security, and sufficient verification.**
