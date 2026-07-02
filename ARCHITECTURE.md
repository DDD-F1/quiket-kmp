# Quiket KMP Architecture

## Architecture Baseline

Quiket KMP는 독립 앱으로 설계한다. 기존 Android 앱은 아키텍처 기준이 아니라 QA 단계에서 화면과 동작을 비교하는 reference다.

기준 구조:

```text
composeApp/
  src/commonMain/
    kotlin/com/f1/quiket/composeapp/
      auth/
        data/
        domain/
        presentation/
      designsystem/
      history/
      home/
      login/
      main/
      mypage/
      network/
      onboarding/
      quiz/
      result/
      review/
      subject/
      util/
    composeResources/
  src/androidMain/
  src/iosMain/
iosApp/
```

- `:composeApp`: KMP shared module. Shared UI, state, domain, data, network, resources live here.
- `iosApp`: Swift/iOS host shell, app entry, signing, Info.plist, iOS app configuration.
- `androidMain`: Android actual implementations and Android platform adapters.
- `iosMain`: iOS actual implementations and iOS platform adapters.

Feature Gradle modules are not part of the Compose Multiplatform baseline. Add them only after an explicit architecture decision.

## Feature Package Shape

Feature packages are organized by product capability inside a single KMP module.

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

Compose Multiplatform uses Koin 4.x. The current repo uses `4.1.1` because Koin `4.2.0` iOS klibs require Kotlin Native ABI `2.3.0`, while this project currently builds with Kotlin `2.2.21`.

- `commonMain/di`: `initKoin`, common module, network module, feature module wiring.
- `androidMain/di`: Android platform module.
- `iosMain/di`: iOS platform module.
- Android `MainActivity` and iOS `MainViewController` initialize Koin at app startup.
- Koin provides `HttpClient`, `Json`, base URL, API clients, remote data sources, repositories, use cases, and StateHolders.
- Composables must not create or directly depend on API clients.
- Screen-level StateHolders use `factory` by default.
- App-wide session/auth coordinators, shared network objects, and stable platform services use `single`.

## Network And Session

- Ktor `HttpClient`, kotlinx serialization `Json`, base URL, and envelope parsing belong to the network/data boundary.
- Authenticated requests go through `AuthenticatedCallRunner`.
- Token refresh, retry, and refresh failure cleanup are centralized.
- Individual screens, routes, and feature clients must not duplicate refresh-token handling.

## Presentation Pattern

- Route obtains StateHolder/use cases through Koin and wires state, effects, and navigation.
- Screen receives immutable state and callbacks only.
- StateHolder is a plain Kotlin state owner in `commonMain`.
- StateHolder uses constructor injection.
- Android ViewModel, SavedStateHandle, Android lifecycle, Hilt, Retrofit, AndroidX Navigation, and Context do not belong in `commonMain`.

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

## Build Logic

`build-logic` included build provides Android/KMP convention plugins. New Gradle settings should reuse existing convention plugins before adding manual setup.
