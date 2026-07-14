# QuiketApp Agent Instructions

Use these instructions for AI coding work in this repository.

## Preflight

- Before implementing or modifying code, read `docs/codex-preflight.md`.
- Before creating or changing Compose screens, read `docs/compose-screen-conventions.md`.
- Before adding modules, navigation entries, Gradle wiring, or resources, read `docs/module-conventions.md`.
- Use `docs/validation.md` as the single source of truth for validation commands.
- Before configuring another machine or creating release artifacts, read `docs/environment-setup.md`.
- For KMP/Compose Multiplatform migration status, read `docs/kmp-current-status.md`; use `docs/kmp-migration-parity.md` only when detailed QA evidence is needed.
- Treat `README.md`, `ARCHITECTURE.md`, and `RULES.md` as the project-level source of truth.
- If these documents conflict, prefer the most specific document for the task and call out the conflict.

## Repository Shape

- This repository is the KMP/Compose Multiplatform app.
- `composeApp` is the Android application and iOS framework host.
- Shared product code lives in `app-shell`, `core/*`, and `feature/*` KMP modules.
- `iosApp` is the Swift/iOS host shell.
- The legacy Android app lives in the separate Android repository and is only a QA parity reference when needed.
- Follow the approved App Shell + Core + Feature modular-monolith boundaries in `ARCHITECTURE.md` and `docs/module-conventions.md`.

## Project Guardrails

- Do not revert or overwrite user changes.
- Keep edits scoped to the requested feature or bug.
- Prefer existing local patterns over new abstractions.
- Use `rg` or `rg --files` for search.
- Use `apply_patch` for manual file edits.
- Run the smallest meaningful Gradle validation for the changed surface.
- Do not treat `NO-SOURCE` as behavioral test coverage or compile success as runtime QA.
- Do not push unless the user explicitly asks for a push.

## Subagent Routing

- Use installed subagents proactively when the user explicitly asks for delegation, subagents, or parallel agent work.
- Prefer the most specific domain subagent over broader generalists.
- Use `kotlin-specialist` for Android native code, Kotlin, Jetpack Compose, Gradle-adjacent Android implementation details, and native modules.
- Use `mobile-developer` when behavior crosses app lifecycle, permissions, foreground/background transitions, deep links, push, or device-specific UX.
- Use `reviewer` for PR-style correctness, regression, security, and missing-test review.
- Use `test-automator` for automated tests, test harness improvements, and targeted regression coverage.
