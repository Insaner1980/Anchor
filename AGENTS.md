# Anchor Project Instructions

<claude-mem-context>
# Memory Context

# [Android-kotlin-starter] recent context, 2026-06-10 6:52pm GMT+3

No previous sessions found.
</claude-mem-context>

## Current Architecture

- App name: `Anchor`
- Package, namespace, and applicationId: `com.finnvek.anchor`
- Android app module: single module `:app`
- Architecture direction: single-module MVVM with Hilt DI, Room local data, DataStore settings, and app-private `PhotoStorage`
- Privacy boundary: the merged manifest must not include `android.permission.INTERNET`
- Backup policy: keep `android:allowBackup="false"` for the MVP privacy promise
- Share entry: `ShareCaptureActivity` is the exported `ACTION_SEND` target for `text/plain` and `image/*`

## Verification

- Do not run `lc` or `sc`; the user runs those wrappers when needed.
- Normal implementation checks:
  - `.\gradlew.bat :app:assembleDebug`
  - `.\gradlew.bat :app:testDebugUnitTest`
  - `.\gradlew.bat :app:lintDebug :app:detekt :app:ktlintCheck`
- Run `.\gradlew.bat :app:connectedDebugAndroidTest` only when a device or emulator is available.

## Notes For Future Changes

- Keep design tokens centralized when building the Anchor UI.
- Keep repository boundaries explicit: resources, tags, settings, and photo file storage each have one source of truth.
- Update this file when modules, package ownership, data flow, or storage responsibilities change.
