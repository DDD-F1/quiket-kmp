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

- KMP/Compose Multiplatform app: `composeApp`, `iosApp`
- Compose Multiplatform branch: usually `current KMP branch`
- Android `dev`: QA parity reference only, not the Compose Multiplatform architecture source
- Non-Compose Multiplatform Android-only code: touch only when the user explicitly asks for Android-only work

If a task mentions iOS, Compose Multiplatform, KMP, shared UI, commonMain, composeResources, platform actuals, or Android/iOS parity, treat `composeApp` as the primary surface.

## Read Before Editing

- For architecture direction, read `ARCHITECTURE.md`.
- For project-wide rules, read `RULES.md`.
- For Compose/Compose Multiplatform screen work, read `docs/compose-screen-conventions.md`.
- For modules, source sets, navigation wiring, resources, or Gradle wiring, read `docs/module-conventions.md`.
- For current Compose Multiplatform completion/parity state, read `docs/kmp-current-status.md`.
- For detailed QA evidence, inspect `docs/kmp-migration-parity.md` only when needed; it is intentionally long.

## Validation

Run the smallest meaningful validation for the changed surface.

### Compose Multiplatform common code

```bash
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:compileDebugKotlinAndroid
```

### iOS host or runtime-sensitive Compose Multiplatform work

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro' build
```

### Android-only code

```bash
./gradlew :app:assembleDebug
```

For Android feature-only changes, prefer the relevant feature unit test or assemble task when available.

### Documentation or skill-only changes

```bash
git diff --check
```

Report build warnings only when they are new, relevant, or actionable.
