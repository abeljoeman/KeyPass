#!/usr/bin/env python3
"""Fail closed when Codex is asked to implement without an approved task and kit."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


def read_gate(path: Path) -> tuple[str, str, str, str]:
    text = path.read_text(encoding="utf-8")
    status_match = re.search(r"^\*\*EXECUTION_STATUS:\s*([^*\r\n]+)\*\*", text, re.MULTILINE)
    task_match = re.search(r"^\*\*ACTIVE_TASK:\s*([^*\r\n]+)\*\*", text, re.MULTILINE)
    kit_match = re.search(r"^\*\*ACTIVE_KIT:\s*([^*\r\n]+)\*\*", text, re.MULTILINE)
    status = status_match.group(1).strip() if status_match else "MISSING"
    task = task_match.group(1).strip() if task_match else "MISSING"
    kit = kit_match.group(1).strip() if kit_match else "MISSING"
    return status, task, kit, text


def resolve_repo_file(repo_root: Path, relative_value: str) -> Path | None:
    candidate_rel = Path(relative_value)
    if candidate_rel.is_absolute() or ".." in candidate_rel.parts:
        return None
    candidate = (repo_root / candidate_rel).resolve()
    if candidate != repo_root and repo_root not in candidate.parents:
        return None
    return candidate


def read_kit_identity(path: Path) -> tuple[str, str]:
    text = path.read_text(encoding="utf-8")
    task_match = re.search(r"^TASK_ID:\s*(T\d{3,})\s*$", text, re.MULTILINE | re.IGNORECASE)
    status_match = re.search(r"^KIT_STATUS:\s*([A-Z_]+)\s*$", text, re.MULTILINE | re.IGNORECASE)
    task = task_match.group(1).upper() if task_match else "MISSING"
    status = status_match.group(1).upper() if status_match else "MISSING"
    return task, status


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--implementation", metavar="TXXX", help="Exact task ID requested for implementation")
    parser.add_argument("--planning", action="store_true", help="Validate planning mode only; application implementation remains forbidden")
    args = parser.parse_args()

    repo_root = Path(__file__).resolve().parents[1]
    tasks_path = repo_root / "TASKS.md"
    if not tasks_path.is_file():
        print("GOVERNANCE BLOCKER: TASKS.md is missing.", file=sys.stderr)
        return 2

    status, active_task, active_kit, text = read_gate(tasks_path)

    if args.planning:
        print(
            "Planning preflight OK: "
            f"EXECUTION_STATUS={status}, ACTIVE_TASK={active_task}, ACTIVE_KIT={active_kit}"
        )
        if status == "ACTIVE_IMPLEMENTATION":
            print("WARNING: an implementation task is active; planning changes must not silently alter its scope or kit.")
        return 0

    if not args.implementation:
        print("GOVERNANCE BLOCKER: implementation mode requires --implementation Txxx.", file=sys.stderr)
        return 2

    requested = args.implementation.upper()
    if not re.fullmatch(r"T\d{3,}", requested):
        print(f"GOVERNANCE BLOCKER: invalid task id {requested!r}.", file=sys.stderr)
        return 2

    if status != "ACTIVE_IMPLEMENTATION":
        print(
            f"GOVERNANCE BLOCKER: EXECUTION_STATUS is {status!r}, not 'ACTIVE_IMPLEMENTATION'.",
            file=sys.stderr,
        )
        return 3

    if active_task != requested:
        print(
            f"GOVERNANCE BLOCKER: requested {requested}, but ACTIVE_TASK is {active_task!r}.",
            file=sys.stderr,
        )
        return 3

    task_definition = re.search(rf"\b{re.escape(requested)}\b", text)
    if not task_definition:
        print(f"GOVERNANCE BLOCKER: {requested} has no task definition in TASKS.md.", file=sys.stderr)
        return 3

    if active_kit in {"NONE", "MISSING", ""}:
        print("GOVERNANCE BLOCKER: ACTIVE_KIT is not set for the active implementation task.", file=sys.stderr)
        return 3

    kit_path = resolve_repo_file(repo_root, active_kit)
    if kit_path is None:
        print(f"GOVERNANCE BLOCKER: ACTIVE_KIT path is not a safe repository-relative path: {active_kit!r}.", file=sys.stderr)
        return 3

    if not kit_path.is_file():
        print(f"GOVERNANCE BLOCKER: ACTIVE_KIT manifest does not exist: {active_kit}.", file=sys.stderr)
        return 3

    kit_task, kit_status = read_kit_identity(kit_path)
    if kit_task != requested:
        print(
            f"GOVERNANCE BLOCKER: kit TASK_ID is {kit_task!r}, expected {requested!r}.",
            file=sys.stderr,
        )
        return 3

    if kit_status != "READY":
        print(
            f"GOVERNANCE BLOCKER: kit KIT_STATUS is {kit_status!r}, expected 'READY'.",
            file=sys.stderr,
        )
        return 3

    print(
        f"Implementation preflight OK: {requested} is active and kit {active_kit} is READY."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
