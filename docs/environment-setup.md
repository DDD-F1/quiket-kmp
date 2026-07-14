# Environment And Release Setup

This document describes the local inputs required to build Quiket without committing credentials or signing material.

## Prerequisites

- JDK 17
- Android SDK matching `compileSdk` in `composeApp/build.gradle.kts`
- Xcode with an installed iOS Simulator runtime for iOS work
- A valid Apple Development team for device builds and archives
- Access to the Kakao application configuration used by Quiket

Use `docs/validation.md` for build and verification commands.

The final KMP framework targets iPhone devices (`iosArm64`) and Apple Silicon simulators (`iosSimulatorArm64`). Intel simulator `iosX64` is not a final app target because Navigation 3 `1.1.1` does not publish an `iosX64` variant. The Xcode target therefore excludes `x86_64` for simulator SDKs so generic simulator builds request only configured Kotlin targets.

## Android Local Configuration

Store machine-local Android and release values in the ignored root `local.properties` file. Do not commit real values.

```properties
sdk.dir=/absolute/path/to/Android/sdk
kakao.native.app.key=<kakao-native-app-key>

# Required only for Android release packaging
storeFile=/absolute/path/to/release-key.jks
storePassword=<store-password>
keyAlias=<key-alias>
keyPassword=<key-password>
```

The same values may be supplied temporarily through environment variables:

```text
KAKAO_NATIVE_APP_KEY
STORE_FILE
STORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
```

Release packaging intentionally fails when the Kakao key or Android signing values are missing. Debug builds use the application ID `com.f1.quiket.composeapp`; release builds use `com.f1.quiket`.

## iOS Local Configuration

Create the ignored file `iosApp/Configuration/Local.xcconfig`:

```xcconfig
TEAM_ID=<apple-development-team-id>
BUNDLE_ID=com.f1.quiket
KAKAO_NATIVE_APP_KEY=<kakao-native-app-key>
```

`iosApp/Configuration/Config.xcconfig` contains non-secret defaults and conditionally includes this local file. Keep the Kakao URL scheme synchronized with the native app key through the existing plist configuration.

The Apple Developer identifier, Xcode bundle identifier, Kakao Developers iOS bundle identifier, and App Store Connect bundle identifier must all be `com.f1.quiket` for release.

## Kakao Platform Registration

Register every application ID or bundle ID used for OAuth testing in Kakao Developers:

- Android release package: `com.f1.quiket`
- Android debug package: `com.f1.quiket.composeapp`
- iOS release bundle: `com.f1.quiket`
- iOS URL scheme: `kakao${KAKAO_NATIVE_APP_KEY}` with the variable replaced by the actual key

Do not place the Kakao key directly in Kotlin, Swift, plist, or a committed Gradle file. The native app key identifies the Kakao application, but configuration management still belongs in local or CI inputs.

## Apple Login Backend Secrets

The following values belong to backend secret management, not the mobile repository or app bundle:

```text
APPLE_OAUTH_CLIENT_ID
TEAM_ID
KEY_ID
PRIVATE_KEY
```

`APPLE_OAUTH_CLIENT_ID` is the Apple Service ID or bundle identifier expected by the backend contract. `PRIVATE_KEY` means the full contents of the matching `.p8` key, including the BEGIN/END lines and line breaks. Send it only through the team's approved secret channel. Never commit a `.p8` file or copy its contents into app configuration.

An Apple Auth Key is associated with an Apple Developer team and enabled services, not with one local Xcode project. Confirm the `KEY_ID`, team, and Sign in with Apple capability instead of choosing a key only by its filename.

## Release Security Gates

Public release is blocked until all of these are true:

- The production API uses HTTPS and Android/iOS cleartext exceptions are removed.
- Access and refresh tokens use platform-protected storage.
- OAuth terms consent comes from an explicit user action and is not hard-coded to `true`.
- Kakao login passes on a real Android device and Apple login passes on a real iPhone.
- App Review receives a login path that is actually visible and usable in the submitted build.
- Privacy disclosures, in-app legal text, backend behavior, and store privacy answers describe the same data practices.

## Artifact Location

Keep release artifacts under a user-owned directory such as `$HOME/Downloads/Quiket/`, not under the repository or `/tmp`. Do not commit `.aab`, `.ipa`, `.xcarchive`, provisioning profiles, or exported signing material.
