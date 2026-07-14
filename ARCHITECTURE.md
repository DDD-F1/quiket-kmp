# Quiket KMP Architecture

## Architecture Baseline

Quiket KMP는 독립 앱으로 설계한다. 기존 Android 앱은 아키텍처 기준이 아니라 QA 단계에서 화면과 동작을 비교하는 reference다.

기준 구조:

```text
composeApp/                Android app, iOS framework, platform entry points
app-shell/                 root flow, navigation, onboarding, review placeholder
core/
  auth/                    auth/session data, domain, use cases, coordinator
  designsystem/            shared theme, components, fonts
  legal/                   shared terms and privacy policy content
  network/                 Ktor, JSON, API configuration, envelope/error handling
  platform/                shared expect/actual platform utilities
feature/
  auth/                    login, signup, password reset, OAuth UI
  history/                 history data/domain/presentation
  home/                    home and exam schedule
  mypage/                  account, settings, inquiry
  quiz/                    quiz create, play, and result
  subject/                 subject, lecture, upload, material check
iosApp/
```

- `:composeApp`: Android application and the single KMP framework exported to iOS. It owns platform entry points, manifests, signing, and SDK bridges.
- `:app-shell`: shared application composition root. It owns `QuiketApp`, root routes, bottom navigation, onboarding, and the review placeholder.
- `:core:*`: capabilities used by at least two features. Core modules must not depend on feature or app modules.
- `:feature:*`: product capability modules. Each feature keeps its own data/domain/presentation layers and resources.
- `iosApp`: Swift/iOS host shell, app entry, signing, Info.plist, iOS app configuration.

The feature/core split is an explicit architecture decision approved on 2026-07-11. Migration details and acceptance gates live in `docs/multi-module-migration.md`.

## Feature Package Shape

Feature modules are organized by product capability. Their internal package shape remains layered.

```text
<feature>/
  data/
    remote/
    repository/
    mapper/
    dto/
  domain/
    model/
    repository/
    usecase/
  presentation/
    <Feature>Route.kt
    <Feature>Screen.kt
    <Feature>State.kt
    <Feature>StateHolder.kt
```

- `data`: Ktor API calls, DTOs, remote data source, repository implementation.
- `domain`: repository interface, domain model, meaningful use case, policy mapper.
- `presentation`: Route, Screen, immutable state, event/side effect, StateHolder.
- Small static UI does not need empty layers.
- Network calls, session handling, storage access, upload polling, quiz creation policy, and cross-screen business rules must use this boundary.
- `AuthClient`, `SubjectClient`, and other API client classes are transport details hidden behind remote data sources.

Dependency direction:

```text
RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen
```

## Dependency Injection

Compose Multiplatform uses Koin 4.x and currently pins `4.1.1`. The shared toolchain currently uses Kotlin `2.4.0`, Compose Multiplatform `1.10.3`, and Navigation 3 `1.1.1`; dependency upgrades are reviewed separately from feature work.

- Each core/feature module exports its own Koin `Module` from `commonMain`.
- `:app-shell` aggregates core and feature modules without starting Koin.
- `:composeApp` starts Koin and adds Android/iOS platform modules.
- Android `MainActivity` and iOS `MainViewController` initialize Koin at app startup.
- Koin provides `HttpClient`, `Json`, base URL, API clients, remote data sources, repositories, use cases, and StateHolders.
- Composables must not create or directly depend on API clients.
- Screen-level StateHolders use `factory` by default.
- App-wide session/auth coordinators, shared network objects, and stable platform services use `single`.

## Network And Session

- Ktor `HttpClient`, kotlinx serialization `Json`, base URL, and envelope parsing belong to the network/data boundary.
- Feature clients decode the standard response envelope through `HttpResponse.requireApiSuccess` or `HttpResponse.decodeApiData`. They must not duplicate body/status/envelope parsing.
- A successful HTTP status with `success=false` is an API failure. Preserve HTTP status and server `code` in `ApiException` when available.
- Authenticated requests go through `AuthenticatedCallRunner`.
- Token refresh, retry, and refresh failure cleanup are centralized.
- Individual screens, routes, and feature clients must not duplicate refresh-token handling.
- Release traffic must use HTTPS. Build configuration must keep development, staging, and production endpoints explicit and must reject an insecure production base URL.
- Access and refresh tokens must use platform-protected storage. A secure-storage failure must not silently downgrade to plaintext persistence.

## Presentation Pattern

- Route obtains StateHolder/use cases through Koin and wires state, effects, and navigation.
- Screen receives immutable state and callbacks only.
- StateHolder is a plain Kotlin state owner in `commonMain`.
- StateHolder uses constructor injection.
- Suspending StateHolder work must rethrow `CancellationException`. Use `runSuspendCatching` when a `Result` is needed; plain `runCatching` must not swallow coroutine cancellation.
- Android ViewModel, SavedStateHandle, Android lifecycle, Hilt, Retrofit, Android Navigation 2 `NavController`, and Context do not belong in `commonMain`.
- Root and cross-feature navigation is owned by `:app-shell` and uses the KMP Navigation 3 `NavBackStack`, `NavDisplay`, and serializable `NavKey` route models.
- Route models contain only the minimum stable identifiers and display values needed to recreate a destination. Do not put OAuth tokens, authorization codes, passwords, full domain objects, or platform handles in a route.
- `rememberNavBackStack` covers saveable navigation restoration where the host platform provides saved state. Business drafts that must survive a fresh app launch require an explicit persistent owner; sensitive authentication drafts intentionally reset to a safe login entry point.

## Platform Boundaries

Keep platform behavior behind expect/actual or platform service interfaces:

- file picker
- Kakao login
- local storage
- browser/deep link
- permissions
- notification token
- platform metadata
- runtime asset loading when platform-specific behavior is required

The owning capability owns its platform implementation. Session storage and device identity live in `:core:auth`; file picking lives in `:feature:subject`; SDK entry points that require the application host remain in `:composeApp` and are injected into `:app-shell`.

## Module Dependency Rules

Allowed dependency direction:

```text
composeApp -> app-shell -> feature:* -> core:*
composeApp -> core:auth (Android session bootstrap only)
app-shell -> core:legal
feature:quiz -> feature:subject
core:auth -> core:network, core:platform
```

- A core module never depends on a feature or app module.
- A feature never depends on `:composeApp` or `:app-shell`.
- `:composeApp -> :core:auth` is limited to initializing Android session storage before Koin starts.
- `:feature:quiz -> :feature:subject` is the only initial feature-to-feature dependency because quiz creation consumes subject and part domain contracts.
- New feature-to-feature dependencies require an architecture review and must never create a cycle.
- `internal` remains the default. Only route entry points, domain contracts required by an approved downstream module, resource access, and Koin module declarations are public.
- Gradle dependencies use `implementation` by default. `api` is reserved for dependencies intentionally exposed through a module's public ABI.

## Product Ownership Notes

- `:core:auth` owns authentication and session capability, not account or legal presentation.
- `:core:legal` owns immutable terms and privacy-policy content shared by authentication and my-page flows.
- `:feature:mypage` owns my-page screens and in-app legal presentation.
- `:app-shell` owns the review-tab placeholder. Quiz result/retry behavior stays in `:feature:quiz`; unused review network objects must not be eagerly registered for the placeholder.
- Large screens are decomposed inside their owning module before creating additional Gradle modules.

## QA Reference

Android `dev` is the visual and behavioral QA reference only.

Use it to compare:

- screen layout and hierarchy
- default selections
- button enabled state
- navigation result
- loading/error/empty states
- major interaction behavior

If Compose Multiplatform intentionally differs because of platform constraints, document the accepted result in `docs/kmp-current-status.md` or `docs/kmp-migration-parity.md`.

## Gradle Shape

- All shared modules are Kotlin Multiplatform modules with `commonMain`, `androidMain`, `iosMain`, and matching test source sets only when needed.
- `:composeApp` remains the only Android application module and the only framework linked by Xcode.
- Shared modules are linked transitively into the static `ComposeApp` framework; iOS does not embed a framework per feature.
- Use the version catalog and explicit module build files during migration. Introduce convention plugins only after repeated configuration is stable and demonstrably duplicated.
