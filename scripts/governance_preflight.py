#!/usr/bin/env python3
"""Fail closed when Codex is asked to implement without an approved active task."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


def read_gate(path: Path) -> tuple[str, str, str]:
    text = path.read_text(encoding="utf-8")
    status_match = re.search(r"^\*\*EXECUTION_STATUS:\s*([^*\r\n]+)\*\*", text, re.MULTILINE)
    task_match = re.search(r"^\*\*ACTIVE_TASK:\s*([^*\r\n]+)\*\*", text, re.MULTILINE)
    status = status_match.group(1).strip() if status_match else "MISSING"
    task = task_match.group(1).strip() if task_match else "MISSING"
    return status, task, text


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

    status, active_task, text = read_gate(tasks_path)

    if args.planning:
        print(f"Planning preflight OK: EXECUTION_STATUS={status}, ACTIVE_TASK={active_task}")
        if status == "ACTIVE_IMPLEMENTATION":
            print("WARNING: an implementation task is active; planning changes must not silently alter its scope.")
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

    print(f"Implementation preflight OK: {requested} is the approved active task.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
