# Compose Multiplatform Architecture Baseline

Last updated: 2026-07-01

## Branch Model

- `current KMP branch`: KMP/Compose Multiplatform app branch
- Android `dev`: QA parity reference only

## Architecture Baseline

- Compose Multiplatform is not feature-by-feature Gradle multi-module.
- Compose Multiplatform product code lives in `:composeApp`, split by feature packages under `commonMain`.
- `iosApp` is the iOS host shell.
- Compose Multiplatform uses Koin 4.x, currently `4.1.1` because Koin `4.2.0` iOS klibs require Kotlin Native ABI `2.3.0`.
- Compose Multiplatform API-backed flows follow `RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen`.
- Feature Gradle modules are not part of the Compose Multiplatform baseline. Add them only after an explicit architecture decision.
- Large root feature files are compatibility shells. New or moved logic belongs in `presentation/domain/data`.

## Compose Multiplatform Parity Rule

Use Android `dev` only as the visual and behavioral QA reference. Compose Multiplatform architecture is defined by its own KMP/Compose Multiplatform package and dependency rules.

## Compose Multiplatform Structure Rule

- Use Koin for shared object creation.
- Do not create API clients directly from Composables.
- Put session refresh behind a shared authenticated call runner.
- Prefer screen-level StateHolder for durable state and business events.
- Keep platform APIs behind expect/actual or platform service interfaces.

## Implemented Architecture Baseline

- Koin app bootstrap is wired from Android and iOS hosts.
- Shared `HttpClient`, `Json`, base URL, API clients, remote data sources, repositories, use cases, and auth state holder are provided by Koin.
- Auth/login/signup/password reset/Kakao nickname and account-link routes call `AuthStateHolder` instead of `AuthClient`.
- Home, history, my page dashboard, account settings, notification settings, inquiry, subject create/detail/edit, upload, material check, quiz create/start/play/result routes call use cases instead of API clients.
- Quiz create request policy is moved behind `BuildQuizCreateRequestUseCase`.
- Review tab remains a placeholder and intentionally has no API dependency until the feature UI is implemented.

## QA Parity Baseline

Detailed evidence lives in `docs/kmp-migration-parity.md`. High-level QA parity baseline:

- Onboarding/Login/Home: aligned enough for current QA
- Bottom tabs: Home, History, Review placeholder, My page aligned to Android baseline
- Subject create/detail/manage: Android parity work mostly complete
- Upload text/PDF/image: core flows and picker behavior checked
- Material check and lecture view: Android parity work complete
- Quiz create/start/play/result/review detail: major parity issues addressed
- Session refresh: Compose Multiplatform refresh serialization added
- API spec changes: latest known result/upload/review path and response updates reflected or confirmed

## Runtime Verification Requirements

- Kakao login real-device verification
- Lottie animation final real-device verification
- Push/notification token registration, if product scope requires it
- QA data cleanup on the shared backend

## Validation Baseline

For Compose Multiplatform changes, prefer:

```bash
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:compileDebugKotlinAndroid
```

For iOS host/runtime work:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro' build
```
