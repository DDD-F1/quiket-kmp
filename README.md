# Quiket

Quiket은 학습 자료 업로드, AI 퀴즈 생성, 문제 풀이, 기록, 오답 복습을 제공하는 모바일 앱이다.

이 브랜치의 기준 앱은 KMP/Compose Multiplatform 기반 `composeApp`이다. 기존 Android 앱은 제품 화면과 동작을 검증할 때 비교하는 QA reference로만 사용한다.

## 구조 요약

KMP/Compose Multiplatform 앱의 기준 구조는 `:composeApp` 하나의 Kotlin Multiplatform 공유 모듈과 `iosApp` host 앱이다. 기능은 `composeApp/src/commonMain` 아래 feature package로 나누고, 네트워크/세션/저장소/정책 로직이 있는 기능은 패키지 내부를 `presentation/domain/data`로 구분한다.

feature별 Gradle 멀티모듈은 현재 기준 구조에 포함하지 않는다. 멀티모듈 분리는 별도 아키텍처 결정 없이 추가하지 않는다.

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

## Compose Multiplatform 기준 구조

- DI: Koin 4.x, currently `4.1.1` because Koin `4.2.0` iOS klibs require Kotlin Native ABI `2.3.0`
- Platform DI: `commonMain/di` common module + `androidMain/iosMain` platform module
- Network: singleton `HttpClient`, `Json`, base URL, envelope parser, authenticated call runner
- Data: remote data source, DTO, repository implementation
- Domain: repository interface, domain model, meaningful use case
- Presentation: Route, Screen, immutable state, StateHolder
- UI parity: QA 단계에서 Android `dev` 앱과 화면/동작을 비교

## 아키텍처 방향

- KMP/Compose Multiplatform 앱은 독립 앱으로 설계한다.
- 단순 UI에는 과한 레이어를 만들지 않는다.
- 네트워크, 세션, 저장소, 업로드 polling, 퀴즈 생성 정책은 presentation에서 분리한다.
- API-backed flow는 `RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen` 의존 방향을 따른다.
- 여러 기능에서 실제 중복이 생긴 경우에만 공통 레이어나 helper로 승격한다.

## 작업 기준 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md): Compose Multiplatform 구조와 책임 경계
- [RULES.md](./RULES.md): 구현 규칙, 리소스, 검증 기준
- [docs/codex-preflight.md](./docs/codex-preflight.md): 작업 전 확인 및 검증 명령
- [docs/compose-screen-conventions.md](./docs/compose-screen-conventions.md): Compose/Compose Multiplatform 화면 작성 규칙
- [docs/module-conventions.md](./docs/module-conventions.md): Compose Multiplatform source set/resource/DI 규칙
- [docs/kmp-current-status.md](./docs/kmp-current-status.md): Compose Multiplatform 기준 구조와 완료 상태 요약
- [docs/kmp-migration-parity.md](./docs/kmp-migration-parity.md): Android/Compose Multiplatform 대조 QA 상세 로그

## 기본 검증

Compose Multiplatform 공통 코드 변경 시:

```bash
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:compileDebugKotlinAndroid
```

iOS host 영향이 있으면:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro' build
```
