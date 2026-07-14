# Quiket KMP Rules

## Product Direction

- Quiket KMP는 독립 앱으로 설계한다.
- 기존 Android 앱은 QA 단계에서 화면과 동작을 비교하는 reference로만 사용한다.
- Compose Multiplatform 앱의 기준 구조는 `:composeApp` host, `:app-shell`, `:core:*`, `:feature:*`, `iosApp` host다.
- 2026-07-11 승인된 멀티모듈 결정은 `ARCHITECTURE.md`와 `docs/multi-module-migration.md`를 따른다.
- 모듈은 빌드 속도만을 위해 나누지 않는다. 제품 capability, 공통 기반, 플랫폼 host라는 소유 경계가 있을 때만 만든다.

## KMP/Compose Multiplatform Source Rules

- Compose Multiplatform 신규 구현은 소유 모듈의 `src/commonMain`에 둔다.
- `commonMain`은 Android framework, Hilt, Retrofit, Android Navigation 2 `NavController`, Context에 직접 의존하지 않는다. 공통 내비게이션에는 승인된 KMP Navigation 3 API만 사용한다.
- 플랫폼 의존 기능은 `androidMain`, `iosMain`, `iosApp` 또는 명시적인 platform service로 분리한다.
- 네트워크 호출, 세션, 저장소 접근, 업로드 polling, 퀴즈 생성 정책, 화면 간 재사용 정책은 Composable에 두지 않는다.
- 이런 로직은 `RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen` 의존 방향을 따른다.
- 단순 정적 UI, 순수 표시 컴포넌트, 한 화면에만 닫힌 일시 UI 상태에는 레이어를 과하게 만들지 않는다.

## Dependency Injection

- Compose Multiplatform DI는 Koin 4.x를 사용하며 현재 채택 버전은 `4.1.1`이다. Kotlin `2.4.0`, Compose Multiplatform `1.10.3`, Navigation 3 `1.1.1` 조합을 현재 공통 UI 기준으로 사용한다.
- 각 core/feature 모듈은 자신의 Koin module만 제공한다.
- `:app-shell`은 core/feature module 목록을 조립하고, `:composeApp`이 platform module과 함께 Koin을 시작한다.
- Android `MainActivity`와 iOS `MainViewController`에서 앱 시작 시 Koin을 초기화한다.
- `HttpClient`, `Json`, base URL, remote data source, repository, use case, StateHolder는 Koin에서 생성한다.
- Composable, Route, StateHolder 안에서 `AuthClient()`, `SubjectClient()` 같은 API client를 직접 생성하지 않는다.
- 화면 단위 StateHolder는 기본적으로 `factory`, 앱 전역 session/auth coordinator는 필요한 경우에만 `single`로 둔다.

## Layer Rules

- `data/remote`: Ktor 호출, request/response DTO, 서버 envelope parsing에 가까운 adapter.
- `data/repository`: remote/local data source를 domain contract에 맞게 조립한다.
- `domain/repository`: presentation이 의존하는 repository interface.
- `domain/model`: 화면 DTO가 아니라 기능 의미를 가진 model.
- `domain/usecase`: 세션, 권한, quiz option policy, upload polling, mapper처럼 테스트 가치가 있는 정책.
- `presentation`: Route, Screen, immutable state, event/side effect, StateHolder.
- API client는 presentation에서 직접 쓰지 않는다. API client는 remote data source 내부 구현 세부사항으로 둔다.
- 새 호출부의 의존 방향은 repository/usecase 쪽으로 잡는다.
- 표준 API envelope는 `core:network`의 `HttpResponse.requireApiSuccess` 또는 `HttpResponse.decodeApiData`로 해석한다. feature client마다 body/status/success parsing을 다시 만들지 않는다.
- HTTP 2xx라도 envelope의 `success=false`면 실패이며, 가능한 경우 `ApiException`에 HTTP status와 서버 code를 보존한다.

## Compose And State

- Compose 화면의 Route/Screen 분리와 state ownership은 `docs/compose-screen-conventions.md`를 따른다.
- 화면 상태는 가능하면 하나의 state object로 관리한다.
- 사용자 입력과 화면 액션은 명확한 event/callback 또는 intent로 받는다.
- 네비게이션, 스낵바 같은 일회성 이벤트는 durable state와 분리한다.
- Composable에는 네트워크 호출, 저장소 접근, 비즈니스 정책을 직접 두지 않는다.
- Route는 state collection과 navigation binding을 담당하고, Screen은 state와 callback만 받는다.
- StateHolder는 commonMain의 plain Kotlin state owner이며 Android lifecycle에 직접 의존하지 않는다.
- suspend 작업을 `Result`로 바꿀 때는 `runSuspendCatching`을 사용해 `CancellationException`을 재전파한다. StateHolder에서 plain `runCatching`으로 coroutine 취소를 오류 상태로 삼키지 않는다.

## Navigation

- 루트와 cross-feature 내비게이션은 `:app-shell`이 소유하고 KMP Navigation 3의 `NavBackStack`, `NavDisplay`, 직렬화 가능한 `NavKey`를 사용한다.
- 탭, 과목, 업로드, 퀴즈, 마이페이지 화면 전환을 여러 `remember` Boolean/nullable 플래그로 표현하지 않는다.
- route에는 화면 재구성에 필요한 최소 식별자와 표시값만 넣는다. OAuth token, authorization code, password, 전체 domain object, platform handle은 넣지 않는다.
- saveable back stack은 플랫폼이 제공하는 상태 복원을 담당한다. 완전한 앱 재실행 뒤에도 필요한 business draft는 별도 영속 저장소가 소유해야 한다.
- 민감한 인증 초안은 영속 복원하지 않으며 세션 검증 실패나 새 프로세스 시작 시 안전한 Login/Onboarding 진입점으로 되돌린다.
- 일회성 화면 요청은 고유 request ID 등으로 소비 여부를 구분해 back stack 복원 뒤 다시 실행되지 않게 한다.

## QA Parity Rule

- Android `dev` 앱은 QA reference다.
- Compose Multiplatform 구현 완료 후 Android `dev`와 비교해 화면 구조, 기본 선택값, 버튼 활성 조건, 오류 문구, navigation 결과를 확인한다.
- iOS 고유 제약 때문에 다르게 구현해야 할 때는 결과 화면 기준으로 사용자 위화감이 없는지 확인한다.
- 플랫폼 차이로 허용하는 내용은 `docs/kmp-current-status.md` 또는 `docs/kmp-migration-parity.md`에 남긴다.

## Common Promotion

- 공통 코드는 두 기능 이상에서 실제 중복이 생겼을 때만 승격한다.
- Compose Multiplatform 공통 코드는 실제 사용 범위에 따라 `:core:designsystem`, `:core:network`, `:core:platform`, `:core:auth`, `:core:legal`로 승격한다.
- 특정 기능에서만 쓰는 mapper, model, helper는 해당 기능 패키지 안에 둔다.

## Resources And Assets

- Compose Multiplatform 공통 리소스는 소유 모듈의 `src/commonMain/composeResources`에 둔다.
- 여러 기능에서 실제 재사용되는 theme/font/component resource만 `:core:designsystem`에 둔다.
- Splash/onboarding/root navigation resource는 `:app-shell`, 기능 전용 resource는 해당 `:feature:*`가 소유한다.
- Android application theme, network security config, Kakao callback처럼 host 설정에 필요한 Android 리소스는 `:composeApp/src/androidMain/res`에 둔다.
- Compose Multiplatform raw/runtime asset은 `composeResources/files`를 우선 사용한다.
- 리소스 네이밍은 `ic_*`, `img_*`, `illust_*`, `bg_*`, `anim_*`를 따른다.
- 같은 이미지를 Compose Multiplatform 내부에서 중복해서 두지 않는다.

## Testing And Validation

- Compose Multiplatform remote data source, mapper, 상태 변환, platform boundary는 변경 위험에 따라 테스트나 compile 검증을 추가한다.
- 버그 수정은 재현 가능한 가장 낮은 계층에 회귀 테스트를 추가한다. 새 use case/policy는 경계값을, 인증 API client는 성공과 unauthorized/error mapping을, rollback/retry StateHolder는 성공과 실패 상태를 보호한다.
- 자동화 테스트는 production backend를 호출하지 않는다. HTTP 계약은 Ktor `MockEngine`, 상태 전이는 fake repository, coroutine은 `runTest`를 사용한다.
- UI 실기 QA는 Android dev와 iOS Compose Multiplatform를 나란히 비교한다.
- 변경 모듈 테스트를 먼저 실행하고 공유 코드 변경이면 Android/iOS 최종 target을 모두 컴파일한다. 명령은 `docs/validation.md`만을 기준으로 한다.
- 테스트 계층과 현재 공백은 `docs/testing-strategy.md`를 기준으로 한다.
- `NO-SOURCE`는 테스트 통과가 아니라 해당 target 테스트 부재를 뜻한다.
- Compile gate는 runtime QA를 대체하지 않는다. OAuth, 파일 선택, 안전 영역, 키보드, 백그라운드 복귀처럼 플랫폼 동작이 개입하면 emulator/simulator/real-device 검증을 추가한다.
- Git push는 사용자가 명시적으로 요청할 때만 한다.

## Security And Release

- Production API는 HTTPS만 사용한다. Android cleartext 허용과 iOS ATS 예외를 릴리즈 편의 수단으로 사용하지 않는다.
- Access token과 refresh token은 플랫폼 보호 저장소에 보관하고, 보안 저장 실패 시 평문 저장소로 조용히 downgrade하지 않는다.
- OAuth identity token, authorization code, Apple `.p8` private key, signing password를 로그나 저장소에 남기지 않는다.
- OAuth의 약관 동의 값은 사용자의 명시적 행동에서 와야 하며 기본값 `true`로 만들지 않는다.
- 앱 내부 약관, 개인정보 처리방침, 실제 backend 처리, App Store/Play Console 개인정보 답변은 동일한 데이터 사용을 설명해야 한다.
- 로컬 키와 signing 입력은 `docs/environment-setup.md`, release 검증은 `docs/validation.md`를 따른다.
