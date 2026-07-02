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

## Compose Multiplatform Screen Rules

- Put shared screen UI in `composeApp/src/commonMain`.
- Keep platform APIs behind callbacks, expect/actual, or platform services.
- Treat safe area, navigation bar, keyboard, modal, file picker, and browser behavior as platform-sensitive.
- Verify iOS layout for top safe area, bottom tab area, text wrapping, and button hit areas.
- Use `composeResources` for shared Compose Multiplatform images, fonts, and animation JSON.

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
- Put Compose Multiplatform shared brand resources in `composeApp/src/commonMain/composeResources`.
- Keep previews/screens easy to render without Android framework objects.
- Use clear callback names such as `onBackClick`, `onPrimaryClick`, `onComplete`, and `onSkip`.

## Practical Rule

Default to stateless screens, but do not force every pixel of UI state upward. Lift state only when ownership, persistence, testing, or business behavior needs it.
