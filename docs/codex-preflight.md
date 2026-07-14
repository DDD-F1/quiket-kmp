# Codex Preflight

Use this before implementing or modifying code in this repository.

## Always Check

- Read the user request and the newest conversation context first.
- Check `git status --short --untracked-files=all` before editing.
- Do not revert or overwrite user changes.
- Prefer existing project patterns over new abstractions.
- Keep changes scoped to the requested feature, bug, or document update.
- Do not push unless the user explicitly asks for a push.

## Identify The Target Surface

- KMP/Compose Multiplatform app: `composeApp`, `app-shell`, `core/*`, `feature/*`, `iosApp`
- Compose Multiplatform branch: usually `current KMP branch`
- Android `dev`: QA parity reference only, not the Compose Multiplatform architecture source
- Non-Compose Multiplatform Android-only code: touch only when the user explicitly asks for Android-only work

If a task mentions iOS, Compose Multiplatform, KMP, shared UI, commonMain, composeResources, platform actuals, or Android/iOS parity, identify the owning module from `docs/module-conventions.md`. Treat `composeApp` as the platform host, not the default owner of shared product code.

## Read Before Editing

- For architecture direction, read `ARCHITECTURE.md`.
- For project-wide rules, read `RULES.md`.
- For Compose/Compose Multiplatform screen work, read `docs/compose-screen-conventions.md`.
- For modules, source sets, navigation wiring, resources, or Gradle wiring, read `docs/module-conventions.md`.
- For current Compose Multiplatform completion/parity state, read `docs/kmp-current-status.md`.
- For detailed QA evidence, inspect `docs/kmp-migration-parity.md` only when needed; it is intentionally long.
- For build, test, runtime, and release gates, read `docs/validation.md`.
- For local keys, signing, OAuth registration, or release setup, read `docs/environment-setup.md`.

## Validation

`docs/validation.md` is the single source of truth for commands. Run the changed module first, then affected downstream modules, then Android/iOS final targets when shared code is involved.

- Do not run a fixed trio of unrelated module tests for every change.
- Do not treat `NO-SOURCE` as test coverage.
- Do not treat compilation as a substitute for emulator, simulator, or real-device QA.
- Use generic Xcode destinations for portable compile checks; select a named simulator only for launch or screenshot work.
- Report build warnings only when they are new, relevant, or actionable.

For documentation or skill-only changes, run `git diff --check` and verify relative links and module names.

## Release Safety

- Never commit signing files, OAuth private keys, access/refresh tokens, or local configuration.
- Production traffic must use HTTPS; cleartext exceptions are not acceptable release configuration.
- OAuth terms consent must come from an explicit user action, never a hard-coded default.
- App Review credentials or demo instructions must match the login UI visible in the submitted build.
