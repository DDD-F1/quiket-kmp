# Compose Multiplatform Module Conventions

Use this before adding source sets, Gradle wiring, navigation entries, platform services, or resources for the Compose Multiplatform app.

## Module Baseline

- Compose Multiplatform product code lives in `:composeApp`.
- `iosApp` is the iOS host shell.
- Do not create feature Gradle modules unless the team explicitly approves a KMP module split.
- Use feature packages under `composeApp/src/commonMain/kotlin/com/f1/quiket/composeapp`.
- Keep platform-specific implementations in `androidMain`, `iosMain`, or `iosApp`.
- Put Swift/iOS host concerns in `iosApp`, not `commonMain`.

## Feature Package Shape

Compose Multiplatform 기준 구조는 단일 Gradle 모듈과 feature package slice다. 기능 패키지 내부는 다음 구조를 따른다.

```text
composeApp/src/commonMain/kotlin/com/f1/quiket/composeapp/<feature>/
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
- 네트워크 호출, 저장소 접근, 세션, polling, quiz policy처럼 테스트 가능한 흐름이 생기면 이 구조를 따른다.
- 루트 feature package의 큰 파일은 compatibility shell로만 둔다. 새 코드와 구조 개선 코드는 위 구조로 정리한다.
- 여러 기능에서 공유하는 network/session/util만 `network`, `auth`, `util`, `designsystem` 같은 공통 패키지로 승격한다.

## DI Wiring

- `composeApp/src/commonMain/kotlin/com/f1/quiket/composeapp/di`에 common Koin module을 둔다.
- `composeApp/src/androidMain/kotlin/.../di`와 `composeApp/src/iosMain/kotlin/.../di`에 platform module을 둔다.
- `HttpClient`, `Json`, base URL은 network singleton으로 제공한다.
- Remote data source, repository, use case, StateHolder는 Koin module에서 생성자 주입으로 연결한다.
- Route에서 API client를 직접 생성하지 않는다.

## Navigation

- Compose Multiplatform navigation should stay in common state/navigation structures.
- Do not use AndroidX Navigation in `commonMain`.
- Root product flow remains `Splash -> Onboarding/Login -> Main`.
- Compose Multiplatform route behavior should match Android `dev` during QA unless an accepted platform difference is documented.

## Resources And Assets

- Compose Multiplatform shared resources belong in `composeApp/src/commonMain/composeResources`.
- Compose Multiplatform platform-specific Android resources belong in `composeApp/src/androidMain/res`.
- Compose Multiplatform files such as Lottie JSON should use `composeResources/files` when they are shared.
- Resource names follow `ic_*`, `img_*`, `illust_*`, `bg_*`, and `anim_*`.
- Avoid duplicating the same image inside Compose Multiplatform packages; move shared resources to common composeResources.

## Gradle Wiring

- Keep Gradle wiring focused on the KMP app: root project plus `:composeApp`.
- Use version-catalog plugin aliases and source-set dependencies before adding custom build logic.
- Add only the dependencies required by the target module/source set.
- Verify the changed surface with the tasks listed in `docs/codex-preflight.md`.
