# Compose Multiplatform Architecture Baseline

Last updated: 2026-07-14

## Branch Model

- `current KMP branch`: KMP/Compose Multiplatform app branch
- Android `dev`: QA parity reference only

## Architecture Baseline

- Compose Multiplatform uses an App Shell + Core + Feature modular monolith.
- `:composeApp` is the Android/iOS host, while shared product code lives in `:app-shell`, `:core:*`, and `:feature:*`.
- `:core:legal` owns shared immutable terms/privacy content; authentication and my-page presentation consume it without a feature-to-feature dependency.
- `iosApp` is the iOS host shell.
- Compose Multiplatform uses Kotlin `2.4.0`, Compose Multiplatform `1.10.3`, Koin `4.1.1`, and Navigation 3 `1.1.1`.
- Compose Multiplatform API-backed flows follow `RemoteDataSource -> Repository -> UseCase -> StateHolder/Route -> Screen`.
- The multi-module architecture decision and initial source migration completed on 2026-07-11; verification gates are tracked in `docs/multi-module-migration.md`.
- Android emulator and iOS simulator runtime verification for login, home, subject, lecture, quiz, history, and my page completed on 2026-07-13 and was repeated after the Navigation 3/upload changes on 2026-07-14.
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
- Review tab UI remains an `:app-shell` placeholder. The unused review API client, models, and DI registration were removed from `:feature:quiz`.
- Standard API envelopes are decoded through `core:network` response helpers, with MockEngine contract coverage for success, envelope failure, HTTP 400/401 metadata, malformed responses, missing data, and malformed data.
- Suspending StateHolder failure conversion uses cancellation-safe `runSuspendCatching`; common tests verify success, ordinary failure, and `CancellationException` identity-preserving rethrow.
- `MainScreen`, subject detail/part detail, quiz create, and text lecture upload were decomposed into route, content/step, component, option, and dialog-oriented files without adding feature modules or changing state ownership.
- Targeted Gradle dependencies were narrowed from `api` to `implementation`; `api` remains only where a dependency type intentionally appears in public ABI.
- Backend OpenAPI `3.0.3` / Quiket API `1.1.0` snapshot is tracked in `docs/openapi.yaml`; the app-facing OAuth contract is summarized in `docs/api-contract.md`.
- Kakao Android SDK initialization, OAuth callback activity, browser-account fallback, and shared server login flow are implemented. Release uses `com.f1.quiket`; debug uses `com.f1.quiket.composeapp` for side-by-side QA.
- Apple OAuth KMP client, iOS Sign in with Apple bridge, Apple UI, and shared nickname/account-link routes are implemented. Real-device and backend-flow QA remain.
- Root auth/onboarding and nested main flows now use KMP Navigation 3 with serializable `NavKey` destinations and a saveable `NavBackStack` instead of screen-switching `remember` flags.
- Navigation route serialization, stack replacement/pop behavior, and quiz launch-config mapping have shared regression tests that run on Android and iOS Simulator targets.
- Retained Navigation 3 entries read observable home/history/my-page state from their StateHolder at composition time, so asynchronous loads replace loading UI. Android process-kill QA restored the saved account-settings destination and preserved back order.
- Subject PDF/image selection now copies provider content into app-owned temporary files without materializing the original file as a `ByteArray`. Upload uses reopenable `kotlinx-io` sources with Ktor streaming multipart, previews use bounded thumbnails, and temporary files are released on rejection, replacement, removal, route disposal, and successful completion.
- Android runtime QA completed an image upload through server analysis and material confirmation; Android and iOS runtime QA also verified picker cancellation/removal and route-disposal cleanup with empty upload cache directories afterward.
- The Xcode target excludes simulator `x86_64` because the final framework intentionally supports only `iosArm64` and `iosSimulatorArm64`. Generic iOS Simulator Release builds therefore request a configured Kotlin target.
- The automated suite now contains 20 test source files and 63 test cases covering auth input policy, home exam presentation, history HTTP contracts, my-page StateHolder rollback/validation, quiz domain policy, shared API/user-facing errors, subject state and streaming upload contracts, Navigation 3, cancellation, and Koin wiring.
- `.github/workflows/ci.yml` now runs for pull requests and `main` with Android/shared tests, final Android/common compilation and lint, plus iOS Simulator tests and final iOS compilation. The first remote run remains to be observed after push.

## QA Parity Baseline

Detailed evidence lives in `docs/kmp-migration-parity.md`. High-level QA parity baseline:

- Onboarding/Login/Home: aligned enough for current QA
- Bottom tabs: Home, History, Review placeholder, My page aligned to Android baseline
- Subject create/detail/manage: Android parity work mostly complete
- Upload text/PDF/image: core flows and picker behavior checked
- Material check and lecture view: Android parity work complete
- Quiz create/start/play/result/review detail: major parity issues addressed
- Session refresh: Compose Multiplatform refresh serialization added
- API spec changes: OpenAPI `1.1.0` snapshot synchronized; Android Kakao SDK and Apple OAuth client implementations added
- Login surface: Android exposes Kakao only; iOS exposes Kakao and Apple. Email login and member signup remain hidden in the final source after temporary QA access was removed.

## Runtime Verification Requirements

- Kakao login real-device verification
- Apple login real-device verification, including first authorization, repeat login, nickname setup, and account linking
- Lottie animation final real-device verification
- Push/notification token registration, if product scope requires it
- QA data cleanup on the shared backend

## Public Release Blockers

- The current production API base URL still uses cleartext HTTP, and both platform hosts allow insecure traffic. Public release requires HTTPS and removal of those exceptions.
- Android session tokens currently use plaintext preferences. iOS keychain failure currently falls back to `NSUserDefaults`; both paths require secure, fail-closed persistence.
- Kakao and Apple login requests currently default terms consent to `true` without a visible OAuth terms step. The consent contract and UI flow must be corrected before new-account release traffic.
- In-app legal copy says uploaded content may be used for AI model training, while the current product policy says it is not used for separate model training. Legal text, backend behavior, and store privacy disclosures must be reconciled.
- The submitted App Review login instructions must match the login path visible in that exact build. Hidden email credentials are not a valid review path.

## Quality Gaps

- Automated coverage now reaches auth, home, history, my page, quiz domain policy, subject state and multipart streaming, API contracts, Navigation 3, cancellation, and Koin. Upload polling/retry, picker and thumbnail platform bridges, quiz play/submit StateHolders, runtime process restoration, secure platform session persistence, and broader feature-client contracts still need regression coverage.
- CI is checked in but has not yet completed a remote GitHub Actions run. Branch protection should require both CI jobs only after that first run is green.
- Navigation 3 stable required the Kotlin `2.4.0` toolchain. The current legacy KMP plus Android Gradle plugin wiring still builds but emits migration warnings; move shared Android targets to the new KMP Android plugin and keep the Android application host split before adopting a toolchain that removes legacy compatibility.
- Compose Multiplatform `1.10.3` and Navigation 3 `1.1.1` currently emit duplicate KLIB `unique_name` warnings for relocated Compose/lifecycle/saved-state dependencies during some iOS metadata tasks. Shared tests, final iOS compilation, Xcode linking, and simulator launch pass; recheck and remove this warning debt during the next coordinated Compose Multiplatform upgrade.
- Future large-screen cleanup should continue inside the owning module using route/section/component/dialog files; additional Gradle modules are not the default response to file size.
- Accessibility follow-up is required for persistent field labels, modal focus, selected-state semantics, touch targets, large text, and state color contrast.

## Validation Baseline

Use `docs/validation.md` as the current command source. Validate the changed module first, then affected modules and both final targets. Use `docs/environment-setup.md` for local keys, signing, OAuth registration, and artifact locations.
