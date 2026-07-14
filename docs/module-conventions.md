# Compose Multiplatform Module Conventions

Use this before adding source sets, Gradle wiring, navigation entries, platform services, or resources for the Compose Multiplatform app.

## Module Baseline

- `:composeApp` is the Android application and iOS framework host.
- `:app-shell` owns shared root navigation and application composition.
- `:core:*` owns capabilities reused by at least two features.
- `:feature:*` owns product capability data/domain/presentation and feature resources.
- `iosApp` remains the Swift/iOS host shell.
- Put platform implementations in the owning module's `androidMain`/`iosMain`; keep Swift host concerns in `iosApp`.

## Feature Package Shape

기능 모듈 내부는 다음 package slice를 따른다.

```text
feature/<module>/src/commonMain/kotlin/com/f1/quiket/composeapp/<product-area>/
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

- 모든 작은 기능에 빈 패키지를 만들지는 않는다.
- 멀티모듈 1차 이관에서는 기존 `com.f1.quiket.composeapp.*` 패키지를 유지한다. package rename은 동작 이관과 분리한다.
- Gradle module name and package name do not have to be identical. Use product vocabulary already established by the codebase, such as `feature:auth` owning the `login` package.
- 네트워크 호출, 저장소 접근, 세션, polling, quiz policy처럼 테스트 가능한 흐름이 생기면 이 구조를 따른다.
- 루트 feature package의 큰 파일은 compatibility shell로만 둔다. 새 코드와 구조 개선 코드는 위 구조로 정리한다.
- 여러 기능에서 공유하는 capability만 적절한 `core:*` 모듈로 승격한다. 포괄적인 `util` 또는 `common` 모듈을 기본 해법으로 만들지 않는다.

## DI Wiring

- 각 모듈은 자신의 Koin module만 노출한다.
- `:app-shell`은 core/feature module을 집계한다.
- `:composeApp`은 Koin 시작과 platform module 제공을 담당한다.
- `HttpClient`, `Json`, base URL은 network singleton으로 제공한다.
- Remote data source, repository, use case, StateHolder는 Koin module에서 생성자 주입으로 연결한다.
- Route에서 API client를 직접 생성하지 않는다.
- Koin module declarations may be public for aggregation, but implementation types stay `internal` unless a downstream module requires a deliberate contract.

## Navigation

- Compose Multiplatform root and cross-feature navigation stays in `:app-shell` and uses KMP Navigation 3 `1.1.1`.
- Use `rememberNavBackStack`, `NavDisplay`, and `@Serializable` sealed `NavKey` route models registered in the shared saved-state configuration.
- Do not use Android Navigation 2 `NavController`, Android-only navigation artifacts, or Android lifecycle navigation owners in `commonMain`.
- Root product flow remains `Splash -> Onboarding/Login -> Main`.
- Compose Multiplatform route behavior should match Android `dev` during QA unless an accepted platform difference is documented.
- Keep route payloads small: stable IDs, enum wire values, and minimal display values only. Do not pass OAuth credentials, passwords, full domain objects, file handles, or platform objects.
- Saveable route restoration and deterministic back behavior require automated serialization/stack tests plus Android/iOS runtime QA.
- Persistent business drafts are separate from navigation state. Sensitive authentication drafts intentionally reset rather than being serialized.
- The final iOS host supports `iosArm64` and `iosSimulatorArm64`; `iosX64` is not a final app target because Navigation 3 `1.1.1` does not publish that variant.

## Resources And Assets

- Feature resources belong in `feature/<name>/src/commonMain/composeResources`.
- Shared theme, fonts, and truly shared UI resources belong in `core/designsystem/src/commonMain/composeResources`.
- Splash, onboarding, and root navigation resources belong in `app-shell/src/commonMain/composeResources`.
- Android host resources belong in `composeApp/src/androidMain/res`.
- Compose Multiplatform files such as Lottie JSON should use the owning module's `composeResources/files`.
- Resource names follow `ic_*`, `img_*`, `illust_*`, `bg_*`, and `anim_*`.
- Avoid duplicating the same image inside Compose Multiplatform packages; move shared resources to common composeResources.

## Gradle Wiring

- Keep module dependencies aligned with `composeApp -> app-shell -> feature -> core`; the Android host may depend directly on `core:auth` only to initialize session storage before Koin.
- Use version-catalog plugin aliases and source-set dependencies before adding custom build logic.
- Add only the dependencies required by the target module/source set.
- Use `implementation` by default. Use `api` only when a type from that dependency intentionally appears in the module's public ABI.
- Do not add a generic `core:common` or empty feature module.
- Feature-to-feature dependencies require an explicit product contract, a cycle check, and documentation in `ARCHITECTURE.md`. Prefer a narrow contract when the downstream feature does not need the upstream implementation surface.
- Verify the changed module and both final targets using `docs/validation.md`.

## Current Ownership Decisions

- `:core:auth` owns authentication, session, token refresh, and device identity. It does not own my-page screens or legal copy.
- `:core:legal` owns immutable terms and privacy-policy content shared by auth and my-page flows. It has no feature or app dependency.
- `:feature:mypage` owns account/settings/inquiry presentation and the in-app legal presentation.
- `:app-shell` owns the current review-tab placeholder. Quiz result and retry behavior belong to `:feature:quiz`; do not register an unused review API graph for a placeholder screen.
- `:feature:subject` owns file selection and upload contracts. `:feature:quiz` may consume the approved subject/part domain contract but should not rely on subject implementation details.
