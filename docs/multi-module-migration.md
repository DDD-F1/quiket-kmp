# KMP Multi-Module Migration

Decision date: 2026-07-11

## Objective

Move Quiket from a single KMP product module to a feature-based modular monolith while preserving Android/iOS behavior and the existing layer direction.

## Target Modules

```text
:composeApp
:app-shell
:core:auth
:core:designsystem
:core:legal
:core:network
:core:platform
:feature:auth
:feature:history
:feature:home
:feature:mypage
:feature:quiz
:feature:subject
```

Onboarding and the review placeholder stay in `:app-shell` until they gain enough independent behavior to justify a module. Quiz result stays in `:feature:quiz` because it consumes quiz play contracts directly.

## Dependency Graph

```text
composeApp -> app-shell
composeApp -> core:auth (Android session bootstrap only)
app-shell -> feature:auth, feature:history, feature:home, feature:mypage,
             feature:quiz, feature:subject
app-shell -> core:legal
feature:* -> core:auth, core:designsystem, core:legal, core:network, core:platform as needed
feature:quiz -> feature:subject
core:auth -> core:network, core:platform
```

All other feature-to-feature dependencies are forbidden unless this document is updated with the reason and cycle analysis.

## Migration Order

1. Lock current OAuth/session behavior with characterization tests.
2. Create and migrate `core:designsystem`, `core:network`, `core:platform`, and `core:auth`.
3. Migrate `feature:auth`, `feature:home`, `feature:history`, and `feature:mypage`.
4. Migrate `feature:subject` and its platform file picker.
5. Migrate quiz create/play/result into `feature:quiz`.
6. Move root navigation, onboarding, and main tab composition to `:app-shell`.
7. Reduce `:composeApp` to platform hosts, app configuration, SDK bridges, and Koin startup.

## Migration Rules

- Move behavior without redesigning it. Behavioral refactors require separate tests and a separate change.
- Preserve `RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen` inside every feature.
- Keep feature-specific models, helpers, and resources in the owning feature.
- Promote code to core only when at least two features consume it.
- Keep the exported iOS surface as one static `ComposeApp` framework.
- Do not store credentials, OAuth tokens, or signing data in a module build file.

## Verification Gates

The migration used the following historical gates:

```bash
./gradlew :core:auth:testDebugUnitTest
./gradlew :feature:subject:testDebugUnitTest
./gradlew :app-shell:testDebugUnitTest
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

For platform-host changes, also compile the iOS device target or archive without launching a simulator. A module is complete only when its tests live with the module and `:composeApp` no longer compiles the old source copy.

Use `docs/validation.md` for current commands. It replaces the fixed migration list with changed-module tests, affected downstream tests, final Android/iOS compilation, and runtime QA based on risk.

## Completed Shape

The initial source migration completed on 2026-07-11. Existing package names were retained intentionally so the Gradle boundary change could be verified separately from a package rename.

- `:composeApp` common code owns Koin startup only; Android/iOS entry points and SDK bridges remain in platform source sets.
- `:composeApp` declares `:core:auth` in `androidMain` because the Android host must initialize session storage before Koin starts.
- `:app-shell` owns `QuiketApp`, root flow, tabs, onboarding, review placeholder, and module aggregation.
- `:core:legal` was added after the initial migration to own immutable terms/privacy content shared by authentication and my-page presentation.
- Each core/feature module owns its production sources, Compose resources, and available regression tests.

## Runtime Verification

The completed module graph was exercised on both platforms on 2026-07-13. Email login was enabled only for the QA builds and hidden again afterward.

- Android API 36 emulator: login, home, subject detail, lecture part detail, quiz start/play, history, and my page passed against live API data.
- iPhone 16 Pro / iOS 18.5 simulator: the same core flow passed through Maestro with screenshots at each feature boundary.
- Android logcat and iOS runtime inspection showed no app crash, missing Koin definition, or resource-loading failure.
- Kakao and Apple OAuth still require their separate real-device checks because this migration QA used the temporary email path.
