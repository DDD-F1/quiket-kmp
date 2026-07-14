# Testing Strategy

This document defines how Quiket protects behavior in the KMP modular monolith. Test count is a health signal, not the goal by itself; each test should protect a product rule, module contract, state transition, or platform boundary.

## Test Layers

1. **Domain and policy tests**
   - Validate pure mappers, value normalization, request construction, validation rules, sorting, and filtering.
   - Keep these deterministic and independent of Compose, Koin, clocks, and the network whenever possible.
2. **StateHolder tests**
   - Exercise success, failure, optimistic update rollback, validation, cancellation, and unauthorized-session behavior through fake repositories.
   - Use `kotlinx.coroutines.test.runTest`; never use arbitrary sleeps.
3. **API contract tests**
   - Use Ktor `MockEngine` to assert request path, query, authorization, envelope decoding, error metadata, malformed payload handling, and unknown enum fallback.
   - Do not call the production backend from automated tests.
4. **Dependency graph tests**
   - Verify Koin can resolve the real shared graph and catch missing module wiring before runtime.
5. **Runtime QA**
   - Use emulator, simulator, and real devices for OAuth, file pickers, process recreation, secure storage, animation, safe areas, and other platform-owned behavior.
   - Runtime QA complements automated tests; it does not replace them.

## Ownership Rules

- Tests live in the module that owns the behavior under `src/commonTest` unless the behavior is platform-specific.
- A bug fix should include a regression test at the lowest layer that reproduces the bug.
- A new use case or policy needs boundary and invalid-input coverage.
- A new authenticated API client needs at least success mapping and unauthorized/error mapping coverage.
- A StateHolder with rollback or retry behavior needs both success and failure-path coverage.
- Shared test fakes stay local to a feature until duplication is large enough to justify a dedicated test-fixture module.

## Current Baseline

As of 2026-07-14, the suite contains 20 test source files and 63 `@Test` cases. It protects:

- authentication response parsing, token refresh, and signup input rules;
- API envelope/error metadata and user-facing network messages;
- cancellation-safe suspend result conversion;
- Navigation 3 route serialization, back-stack behavior, and Koin wiring;
- home exam normalization, de-duplication, expiry filtering, and ordering;
- history request/authentication and response mapping;
- my-page notification rollback and inquiry validation/submission;
- quiz request construction, OX answer normalization, defaults, and launch configuration;
- subject detail and part edit state transitions;
- upload cache-file reopening and cleanup plus streaming multipart headers and payloads.

The next high-value gaps are upload polling/retry, picker and thumbnail platform bridges, quiz play/submit StateHolders, process restoration, secure session persistence, and broader feature-client contract tests.

## CI Gates

`.github/workflows/ci.yml` runs on pull requests, pushes to `main`, and manual dispatch.

- **Android And Shared Tests** runs all modules with checked-in tests, common metadata compilation, final Android compilation, and Android lint on `ubuntu-latest`.
- **iOS Shared Tests** runs the same shared suites on the `iosSimulatorArm64` targets and compiles the final iOS framework on `macos-15`.
- Both jobs upload JUnit/HTML reports for 14 days, including failed runs.
- CI uses a non-secret placeholder Kakao key only to satisfy compile-time manifest configuration. OAuth and signing secrets are not required or stored in the workflow.

## Coverage Measurement

A single percentage is not yet a release gate because Android JVM coverage does not represent iOS/platform paths accurately. After the legacy KMP/Android Gradle plugin migration is complete, add a version-compatible Kover report, record the baseline, and gate regressions rather than selecting an arbitrary target first.

Until then, review coverage by product risk and keep the checked-in test inventory and CI green. Local commands and final gates are defined in `docs/validation.md`.
