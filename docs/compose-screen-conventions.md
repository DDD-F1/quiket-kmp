# Compose Screen Conventions

Use this before creating or changing Jetpack Compose or Compose Multiplatform screens.

## Route And Screen

- Split feature entry points into `Route` and `Screen` by default.
- `Route` owns state loading, state collection, side effects, and navigation callbacks.
- `Screen` owns rendering and receives state plus event callbacks as parameters.
- Do not call Android `NavController` directly from `Screen`.
- In Compose Multiplatform, avoid Android-only framework objects in `commonMain` screens.
- In Compose Multiplatform, Route should obtain StateHolder/use cases through Koin instead of constructing API clients.
- `Screen` must not create API client, repository, use case, or platform service instances.

## State Ownership

- Prefer stateless `Screen` APIs for business state and durable UI state.
- Keep transient UI-only state inside `Screen` when it has no business meaning.
- Examples allowed inside `Screen`: pager state, expanded menus, selected local tab, animation state, focus state, text draft before submit.
- Move state to route/state holder/ViewModel when it must survive screen return, repository sync, platform recreation, or business rules.
- If state affects navigation, persistence, network/database calls, or cross-screen behavior, do not keep it only in `Screen`.

## Compose Multiplatform StateHolder Pattern

- StateHolder is a plain Kotlin state owner in `commonMain`.
- StateHolder receives use cases or repositories through constructor injection.
- StateHolder owns durable screen state, loading/error transitions, and business events.
- Route wires StateHolder state to Screen parameters and handles navigation/one-off side effects.
- Screen receives immutable state and callbacks only.
- Prefer Koin `factory` for screen-level StateHolders.
- Use Koin `single` only for app-wide coordinators such as session/auth state.
- Do not introduce Android ViewModel, SavedStateHandle, or lifecycle dependencies in `commonMain`.
- Suspending StateHolder operations must preserve structured concurrency. Use `runSuspendCatching` for result conversion so `CancellationException` is rethrown instead of becoming UI failure state.

## Compose Multiplatform Screen Rules

- Put shared screen UI in the owning `app-shell` or `feature/*` module's `src/commonMain`.
- Keep `composeApp` focused on platform entry points, SDK bridges, manifests, and Koin startup; it is not the default owner of product screens.
- Keep platform APIs behind callbacks, expect/actual, or platform services.
- Treat safe area, navigation bar, keyboard, modal, file picker, and browser behavior as platform-sensitive.
- Verify iOS layout for top safe area, bottom tab area, text wrapping, and button hit areas.
- Put feature resources in the owning feature, app-shell resources in `app-shell`, and truly shared theme/component resources in `core:designsystem`.

## QA Parity

- Build Compose Multiplatform screens from the Compose Multiplatform architecture and design system first.
- During QA, compare Compose Multiplatform against Android `dev` for visible structure, default selections, button enabled state, error messages, and navigation results.
- If Compose Multiplatform intentionally differs from Android because of platform constraints, document the accepted difference in `docs/kmp-current-status.md` or `docs/kmp-migration-parity.md`.

## MVI/UDF

- Screen state should be represented by a clear immutable state model where practical.
- User actions should enter the state owner as explicit event functions or intents.
- One-off events such as navigation and snackbars should be separated from durable screen state.
- Composables must not contain business logic.
- Network calls, token refresh, repository calls, and domain policy mapping belong outside Composables.

## UI Implementation

- Prefer existing design system colors, typography, spacing, and components.
- Put Compose Multiplatform resources in the owning module's `src/commonMain/composeResources`.
- Keep previews/screens easy to render without Android framework objects.
- Use clear callback names such as `onBackClick`, `onPrimaryClick`, `onComplete`, and `onSkip`.

## Responsive Layout And Accessibility

- Avoid fixed full-screen heights and large fixed top padding for login, form, and action-heavy screens. Prefer content-driven sizing, safe-area handling, and scrolling where content can grow.
- Verify small phones, large text, long Korean strings, keyboard-open state, and both Android/iOS safe areas.
- Give text fields a persistent accessible label after input and associate validation errors with the field semantics.
- Use `selectable`/`toggleable` or explicit `selected`/`checked` semantics for custom selection controls.
- Use `Dialog`, `ModalBottomSheet`, or equivalent modal semantics when background content must not remain accessible.
- Keep interactive targets at least 48dp on Android-oriented shared UI and at least 44pt for iOS usability.
- Check stateful foreground/background color pairs for readable contrast; a design token is not automatically valid in every state.

## Navigation And Restoration

- Root and cross-feature navigation belongs to `app-shell` and uses KMP Navigation 3. Feature screens expose navigation callbacks and do not manipulate the app back stack directly.
- Model destinations as serializable `NavKey` values with minimal stable arguments; do not pass secrets, full domain objects, or platform handles.
- State required after process recreation, app relaunch, or return from a platform flow must not live only in plain `remember`.
- Use a serializable route model for saveable back-stack restoration and a separate persistent owner for business drafts that must survive a fresh app launch.
- Sensitive signup and OAuth credentials are not navigation state and must reset to a safe authentication entry point instead of being persisted in routes.
- Navigation, snackbar, and other one-off effects must be separated from durable state and must not replay after restoration.

## File Size And Decomposition

- A Gradle module is not a substitute for screen-level decomposition.
- Split large screens by state owner, route, major section, reusable component, and dialog/overlay when the file becomes difficult to review or test.
- Keep business decisions in StateHolder/use cases; extracting a visual component must not create a second source of state truth.
- Keep the route entry file focused on dependency/state/navigation wiring. Section and component files may use module-internal APIs but should not widen public feature contracts.

## Practical Rule

Default to stateless screens, but do not force every pixel of UI state upward. Lift state only when ownership, persistence, testing, or business behavior needs it.
