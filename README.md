# Quiket

Quiket은 학습 자료 업로드, AI 퀴즈 생성, 문제 풀이, 기록, 오답 복습을 제공하는 Android/iOS 학습 앱이다.

이 저장소는 Kotlin Multiplatform과 Compose Multiplatform으로 구현한 제품 앱이다. 기존 Android 앱은 제품 화면과 동작을 비교하는 QA reference일 뿐, 이 저장소의 아키텍처 기준은 아니다.

## 기술 스택

- Kotlin 2.4.0, Kotlin Multiplatform, Compose Multiplatform 1.10.3
- Navigation 3 KMP 1.1.1
- Koin 4.1.1
- Ktor Client, kotlinx serialization, coroutines
- Android SDK 36, iOS Swift host
- Kakao Login, Sign in with Apple

## 구조 요약

KMP/Compose Multiplatform 앱은 App Shell + Core + Feature 모듈러 모놀리스 구조를 사용한다. `:composeApp`은 Android 앱과 iOS framework host이고, 공유 제품 코드는 `:app-shell`, `:core:*`, `:feature:*`에 둔다. 기능 모듈 내부는 필요에 따라 `presentation/domain/data`로 구분한다.

```text
composeApp/                Android application, iOS framework host, SDK bridges
app-shell/                 root flow, navigation, onboarding, module aggregation
core/
  auth/                    authentication and session capability
  designsystem/            shared theme/components/resources
  legal/                   shared terms and privacy policy content
  network/                 Ktor/JSON/API support
  platform/                shared expect/actual utilities
feature/
  auth/                    login, signup, password reset, OAuth UI
  history/                 learning history
  home/                    home and exam schedule
  mypage/                  account, settings, inquiry, legal presentation
  quiz/                    quiz create, play, result
  subject/                 subject, lecture, upload, material check
iosApp/                    Swift host, entitlements, plist, signing
```

의존 방향은 다음을 기준으로 한다.

```text
composeApp -> app-shell -> feature:* -> core:*
RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen
```

## 설계 원칙

- DI: 각 core/feature 모듈이 Koin module을 제공하고 `:app-shell`이 집계하며 `:composeApp`이 시작
- Network: singleton `HttpClient`, `Json`, base URL, envelope parser, authenticated call runner
- Data: remote data source, DTO, repository implementation
- Domain: repository interface, domain model, meaningful use case
- Presentation: Route, Screen, immutable state, StateHolder
- KMP/Compose Multiplatform 앱은 독립 앱으로 설계한다.
- 단순 UI에는 과한 레이어를 만들지 않는다.
- 네트워크, 세션, 저장소, 업로드 polling, 퀴즈 생성 정책은 presentation에서 분리한다.
- 여러 기능에서 실제 중복이 생긴 경우에만 공통 레이어나 helper로 승격한다.
- Android와 iOS는 공유 제품 코드와 동일한 domain contract를 사용하고, SDK·파일 picker·secure storage 같은 플랫폼 경계만 분리한다.

## 시작하기

1. JDK, Android SDK, Xcode 등 필수 도구를 준비한다.
2. Kakao 키와 signing 값을 로컬 설정에 추가한다.
3. 변경 모듈을 먼저 검증한 뒤 Android/iOS 최종 타깃을 컴파일한다.

실제 값과 파일 위치는 [환경 및 릴리즈 설정](./docs/environment-setup.md), 명령은 [검증 가이드](./docs/validation.md)를 따른다. 비밀값과 서명 파일은 저장소에 커밋하지 않는다.

## 작업 기준 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md): Compose Multiplatform 구조와 책임 경계
- [RULES.md](./RULES.md): 구현 규칙, 리소스, 검증 기준
- [docs/codex-preflight.md](./docs/codex-preflight.md): 작업 전 범위·규칙·안전 확인
- [docs/compose-screen-conventions.md](./docs/compose-screen-conventions.md): Compose/Compose Multiplatform 화면 작성 규칙
- [docs/module-conventions.md](./docs/module-conventions.md): Compose Multiplatform source set/resource/DI 규칙
- [docs/testing-strategy.md](./docs/testing-strategy.md): 테스트 계층, 모듈 소유권, CI 기준, 남은 공백
- [docs/validation.md](./docs/validation.md): 변경 범위별 테스트, 컴파일, 런타임, 릴리즈 검증
- [docs/environment-setup.md](./docs/environment-setup.md): 로컬 키, signing, OAuth, 릴리즈 환경 설정
- [docs/openapi.yaml](./docs/openapi.yaml): 백엔드 OpenAPI `3.0.3` / Quiket API `1.1.0` 동기화 사본
- [docs/api-contract.md](./docs/api-contract.md): 앱 OAuth 계약과 구현 상태
- [docs/kmp-current-status.md](./docs/kmp-current-status.md): Compose Multiplatform 기준 구조와 완료 상태 요약
- [docs/kmp-migration-parity.md](./docs/kmp-migration-parity.md): Android/Compose Multiplatform 대조 QA 이력
- [docs/multi-module-migration.md](./docs/multi-module-migration.md): 멀티모듈 결정과 이관 기록

## 현재 상태

멀티모듈 이관과 Android/iOS 기본 플로우 QA는 완료됐지만, 공개 릴리즈 전 HTTPS 전환, 플랫폼 보안 저장소, OAuth 약관 동의 정합성, Kakao/Apple 실기기 검증이 필요하다. 완료 상태와 알려진 위험은 [현재 상태](./docs/kmp-current-status.md)를 기준으로 확인한다.

## 검증

고정된 테스트 목록을 반복하지 않는다. 변경 모듈의 테스트부터 실행하고, 공유 코드 변경이면 Android와 iOS 최종 타깃을 모두 컴파일한다. 전체 기준과 런타임 QA 매트릭스는 [docs/validation.md](./docs/validation.md)에 있다.
