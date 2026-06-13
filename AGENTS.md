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
- UI entry flow: `MainActivity` hosts `AnchorAppRoot`, which owns the Navigation Compose shell and Home/Library/You bottom navigation
- Theme flow: `AnchorAppViewModel` reads `SettingsRepository.themeMode`; `MainActivity` applies it to `AnchorTheme`
- Navigation source of truth: MVP route strings live in `ui/navigation/AnchorRoute.kt`
- Settings UI: `ui/settings/SettingsRoute` is the You/settings destination and writes through `SettingsViewModel`
- Design tokens: `AnchorTheme` provides centralized color schemes, `AnchorSpacing`, `AnchorShapes`, and `AnchorMotion`
- Local data source of truth: `AnchorDatabase` owns `resources`, `tags`, and `resource_tag_refs` through `ResourceDao` and `TagDao`
- Settings source of truth: `DataStoreSettingsRepository` owns theme, onboarding, and app-locale preferences in `anchor_settings`
- Photo storage source of truth: `PhotoStorage` copies selected/shared images to `filesDir/photos/`, writes thumbnails to `cacheDir/anchor-thumbnails/`, and owns cleanup by stored relative paths
- Capture flow: `ShareCaptureActivity` converts `ACTION_SEND` text/images into `CaptureInitialDraft` and hosts `CaptureRoute`; `CaptureViewModel` saves song/photo captures through `ResourceRepository`
- Home/Help flow: `HomeRoute` opens `HelpNeedPickerRoute`; `HelpResultsViewModel` increments usage before opening songs through `ExternalSongOpener` or photos through `PhotoPagerRoute`
- Library flow: `LibraryRoute` owns content-type, search, favorite, and timeline view state through `LibraryViewModel`
- Detail/tag flow: `ResourceDetailRoute` edits content, favorite, and delete through `ResourceRepository`; `TagManagerRoute` manages custom/preset tags through `TagRepository`
- Onboarding flow: `AnchorAppRoot` starts at `OnboardingRoute` until `SettingsRepository.onboardingDone` is true
- Repository implementations: `DataResourceRepository` and `DataTagRepository` are the Hilt-bound concrete data layer for MVP UI flows
- Locale flow: `App` observes the stored locale and applies it via `AppCompatDelegate`; `MainActivity` must remain an `AppCompatActivity`
- Startup data flow: `App` launches `TagSeeder.seedIfNeeded()` with the Hilt-provided application scope to install missing preset tags idempotently
- Repository contracts: resource, tag, and settings access flow through the interfaces in `repository/`; concrete implementations should preserve those boundaries
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
