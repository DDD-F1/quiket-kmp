# API Contract Snapshot

Last synchronized: 2026-07-10
OpenAPI version: `3.0.3`
Quiket API version: `1.1.0`

`openapi.yaml` is the version-controlled snapshot of the latest backend contract shared with this repository. The backend's Notion board and deployment state remain authoritative; update this file whenever the backend shares a newer contract.

## Contract Rules

- API base path, request fields, response envelopes, and status branches must follow `openapi.yaml`.
- Authenticated device-login endpoints require `X-Device-Id` and `X-Device-Name` headers.
- Successful and failed responses use the `ApiResponse` envelope. Treat `success`, HTTP status, and `code` together when routing an auth flow.
- Do not log or persist OAuth identity tokens, authorization codes, refresh tokens, Apple private keys, or backend secrets.

## OAuth Contract

| Provider | Endpoints | App status |
| --- | --- | --- |
| Kakao | `POST /auth/oauth/kakao/login`, `/link`, `/nickname` | Implemented. iOS SDK bridge and shared account-link/nickname routes exist. |
| Apple | `POST /auth/oauth/apple/login`, `/link`, `/nickname` | iOS client, system authorization bridge, UI, and shared nickname/account-link routes are implemented. Real-device and backend-flow QA remain. |

## Apple Login

### `POST /auth/oauth/apple/login`

Send `X-Device-Id`, `X-Device-Name`, and this JSON body:

| Field | Required | Notes |
| --- | --- | --- |
| `identityToken` | Yes | JWT returned by `ASAuthorizationAppleIDCredential`. |
| `authorizationCode` | No | Send whenever Apple returns it. The backend exchanges it for a revoke-capable refresh token. |
| `fullName` | No | Apple supplies the name only at first authorization. Combine the available name components and omit when unavailable. |
| `agreedToTerms` | Conditionally | Must be `true` when the server is allowed to create a new Apple account immediately. |

Handle the response branches as follows:

| HTTP | Code | Required app action |
| --- | --- | --- |
| `200` | `AUTH_APPLE_LOGIN_SUCCESS` | Save Quiket access/refresh tokens and enter the app. |
| `201` | `AUTH_APPLE_SIGNUP_SUCCESS` | Save tokens and enter the app. |
| `202` | `AUTH_NICKNAME_REQUIRED` | Keep `signupToken` and show the Apple nickname route. |
| `409` | `AUTH_OAUTH_ACCOUNT_LINK_REQUIRED` | Keep `linkToken` and returned email, then show the existing-account link route. |
| `400` | `COMMON_VALIDATION_ERROR` | Show the terms-consent error; do not retry with the same input. |
| `401` | `AUTH_APPLE_INVALID_TOKEN` | Treat the Apple credential as invalid and require a fresh SDK authorization. |
| `503` | `AUTH_APPLE_CONFIG_ERROR` or `AUTH_APPLE_KEY_FETCH_FAILED` | Surface a retryable service error. |

### `POST /auth/oauth/apple/nickname`

Send `signupToken` and `nickname` with the device headers. A `200` response is the final login completion and must save the returned Quiket tokens.

### `POST /auth/oauth/apple/link`

Send `linkToken`, local `email`, and local `password` with the device headers. Unlike the Kakao link request, the Apple contract does not include `agreedToLink`. A `200` response is the final login completion and must save the returned Quiket tokens.

## Implementation Boundary

Apple SDK authorization belongs to the iOS host/platform boundary. `iosApp/iosApp/ContentView.swift` converts `ASAuthorizationAppleIDCredential` into immutable contract values, and the shared KMP auth layer follows `RemoteDataSource -> Repository -> UseCase -> StateHolder -> Route -> Screen`. The app preserves the temporary signup/link token only until the matching completion request succeeds or the user leaves that flow.

The iOS target declares the Sign in with Apple entitlement in `iosApp/iosApp/Quiket.entitlements`. Enable the same capability for `com.f1.quiket` in Apple Developer before creating a distribution profile. Next QA must cover `200/201/202/409/400/401/503`, first authorization, repeat login, nickname completion, and account linking on a real device.
