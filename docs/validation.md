# Validation Guide

This document is the single source of truth for local validation commands. Run the smallest meaningful gate first, then expand validation according to the changed surface and release risk.

## Validation Order

1. Run tests or compilation for the module that owns the change.
2. Run tests for directly affected downstream modules.
3. Compile the final Android and iOS targets for shared-code changes.
4. Run emulator, simulator, or real-device QA for runtime-sensitive changes.
5. Run release packaging only when release configuration and signing inputs are available.

A successful compile does not replace runtime QA. A `NO-SOURCE` test result means that the module has no tests for that target; it is not evidence that behavior was tested.

## Changed Module

Use the owning module path instead of running a fixed list of unrelated tests.

```bash
./gradlew :<module>:testDebugUnitTest
```

Examples:

```bash
./gradlew :core:auth:testDebugUnitTest
./gradlew :feature:quiz:testDebugUnitTest
./gradlew :app-shell:testDebugUnitTest
```

For shared Navigation 3 route or back-stack changes, run the app-shell multiplatform tests so Android and iOS serialization stay aligned:

```bash
./gradlew :app-shell:allTests
```

For shared API envelope/error changes and suspend-result/cancellation changes, run their multiplatform contract tests:

```bash
./gradlew :core:network:allTests
./gradlew :core:platform:allTests
```

For a module without Android unit tests, run the closest available common or target compilation and record the missing coverage.

For a local Android sweep of every checked-in automated suite:

```bash
./gradlew \
  :core:auth:testDebugUnitTest \
  :core:network:testDebugUnitTest \
  :core:platform:testDebugUnitTest \
  :feature:auth:testDebugUnitTest \
  :feature:history:testDebugUnitTest \
  :feature:home:testDebugUnitTest \
  :feature:mypage:testDebugUnitTest \
  :feature:quiz:testDebugUnitTest \
  :feature:subject:testDebugUnitTest \
  :app-shell:testDebugUnitTest
```

## Shared KMP Final Gates

Run these after changes to `app-shell`, `core/*`, `feature/*`, shared resources, Koin wiring, or shared platform contracts:

```bash
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Run Android lint when Android manifests, resources, dependencies, SDK configuration, or release behavior changes:

```bash
./gradlew :composeApp:lintDebug
```

## Continuous Integration

`.github/workflows/ci.yml` runs on pull requests, pushes to `main`, and manual dispatch.

- `Android And Shared Tests` runs the complete Android test sweep, common metadata compilation, final Android compilation, and `lintDebug` on `ubuntu-latest`.
- `iOS Shared Tests` runs each tested module on `iosSimulatorArm64` and compiles the final iOS framework on the ARM64 `macos-15` runner.
- JUnit, HTML test, and Android lint reports are uploaded for 14 days even when a job fails.
- CI uses a non-secret placeholder Kakao key for compile-time configuration. It does not contain OAuth, backend, or signing secrets.

The first remote workflow run must still be observed after the workflow is pushed. A locally valid YAML file is not proof that repository Actions permissions, quotas, and runner availability are configured correctly.

## iOS Host

For Swift, Xcode project, entitlement, plist, CocoaPods/SPM, or iOS host changes, use a generic simulator destination so the command is portable across machines:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -destination 'generic/platform=iOS Simulator' \
  build
```

Use `xcrun simctl list devices available` when a named simulator is required for launch or screenshot QA. Do not commit machine-specific simulator UUIDs as current setup instructions.

## Runtime QA

Use the Android `dev` app only as a visual and behavioral reference. Validate the KMP app itself on both platforms when the changed flow is platform-sensitive.

- Authentication: provider success, cancellation, first signup, repeat login, nickname, account link, token expiry, background/foreground return.
- Navigation: process recreation or relaunch, back behavior, draft restoration, deep link when applicable.
- Upload: supported type, size boundary, cancellation, retry, backgrounding, processing failure.
- Quiz: create, play modes, timer, submit, retry, result, session expiry.
- Accessibility-sensitive UI: large text, screen reader semantics, focus order, touch targets, contrast.

Kakao and Apple OAuth require real-device checks before public release. Simulator-only verification is insufficient for the final release gate.

## Release Packaging

Release inputs are documented in `docs/environment-setup.md`. Release approval requires valid signing inputs and an HTTPS production API. The current project still needs an automated build failure for a non-HTTPS production base URL; until that exists, verify it explicitly before packaging.

Android:

```bash
./gradlew :composeApp:bundleRelease
```

iOS archive:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath "$HOME/Downloads/Quiket/Quiket.xcarchive" \
  archive \
  -allowProvisioningUpdates
```

Keep generated `.aab`, `.ipa`, `.xcarchive`, and export directories outside the repository.

## Documentation And Skill Changes

```bash
git diff --check
```

Also verify that every new relative Markdown link resolves from its containing document and that commands use current Gradle module names from `settings.gradle.kts`.
