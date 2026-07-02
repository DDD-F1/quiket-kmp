# Quiket KMP Rules

## Product Direction

- Quiket KMP는 독립 앱으로 설계한다.
- 기존 Android 앱은 QA 단계에서 화면과 동작을 비교하는 reference로만 사용한다.
- Compose Multiplatform 앱의 기준 구조는 `composeApp` 단일 KMP 공유 모듈과 `iosApp` host다.
- Compose Multiplatform은 feature별 Gradle 멀티모듈이 아니다. 기능별 package boundary로 관리한다.
- KMP feature/core Gradle 멀티모듈 분리는 별도 아키텍처 결정 없이 추가하지 않는다.

## KMP/Compose Multiplatform Source Rules

- Compose Multiplatform 신규 구현은 가능한 한 `composeApp/src/commonMain`에 둔다.
- `commonMain`은 Android framework, Hilt, Retrofit, AndroidX Navigation, Context에 직접 의존하지 않는다.
- 플랫폼 의존 기능은 `androidMain`, `iosMain`, `iosApp` 또는 명시적인 platform service로 분리한다.
- 네트워크 호출, 세션, 저장소 접근, 업로드 polling, 퀴즈 생성 정책, 화면 간 재사용 정책은 Composable에 두지 않는다.
- 이런 로직은 `RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen` 의존 방향을 따른다.
- 단순 정적 UI, 순수 표시 컴포넌트, 한 화면에만 닫힌 일시 UI 상태에는 레이어를 과하게 만들지 않는다.

## Dependency Injection

- Compose Multiplatform DI는 Koin 4.x를 사용한다. 현재 채택 버전은 `4.1.1`이다. Koin `4.2.0` iOS klib는 Kotlin Native ABI `2.3.0`으로 빌드되어 현재 Kotlin `2.2.21`에서는 사용할 수 없다.
- `commonMain/di`는 common module, network module, feature module 묶음을 제공한다.
- `androidMain/di`, `iosMain/di`는 platform module을 제공한다.
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

## Compose And State

- Compose 화면의 Route/Screen 분리와 state ownership은 `docs/compose-screen-conventions.md`를 따른다.
- 화면 상태는 가능하면 하나의 state object로 관리한다.
- 사용자 입력과 화면 액션은 명확한 event/callback 또는 intent로 받는다.
- 네비게이션, 스낵바 같은 일회성 이벤트는 durable state와 분리한다.
- Composable에는 네트워크 호출, 저장소 접근, 비즈니스 정책을 직접 두지 않는다.
- Route는 state collection과 navigation binding을 담당하고, Screen은 state와 callback만 받는다.
- StateHolder는 commonMain의 plain Kotlin state owner이며 Android lifecycle에 직접 의존하지 않는다.

## QA Parity Rule

- Android `dev` 앱은 QA reference다.
- Compose Multiplatform 구현 완료 후 Android `dev`와 비교해 화면 구조, 기본 선택값, 버튼 활성 조건, 오류 문구, navigation 결과를 확인한다.
- iOS 고유 제약 때문에 다르게 구현해야 할 때는 결과 화면 기준으로 사용자 위화감이 없는지 확인한다.
- 플랫폼 차이로 허용하는 내용은 `docs/kmp-current-status.md` 또는 `docs/kmp-migration-parity.md`에 남긴다.

## Common Promotion

- 공통 코드는 두 기능 이상에서 실제 중복이 생겼을 때만 승격한다.
- Compose Multiplatform 공통 코드는 `composeApp/src/commonMain`의 `designsystem`, `network`, `util` 또는 명확한 공통 패키지로 승격한다.
- 특정 기능에서만 쓰는 mapper, model, helper는 해당 기능 패키지 안에 둔다.

## Resources And Assets

- Compose Multiplatform 공통 리소스는 `composeApp/src/commonMain/composeResources`에 둔다.
- Compose Multiplatform platform-specific Android 리소스는 `composeApp/src/androidMain/res`에 둔다.
- Compose Multiplatform raw/runtime asset은 `composeResources/files`를 우선 사용한다.
- 리소스 네이밍은 `ic_*`, `img_*`, `illust_*`, `bg_*`, `anim_*`를 따른다.
- 같은 이미지를 Compose Multiplatform 내부에서 중복해서 두지 않는다.

## Testing And Validation

- Compose Multiplatform remote data source, mapper, 상태 변환, platform boundary는 변경 위험에 따라 테스트나 compile 검증을 추가한다.
- UI 실기 QA는 Android dev와 iOS Compose Multiplatform를 나란히 비교한다.
- Git push는 사용자가 명시적으로 요청할 때만 한다.
