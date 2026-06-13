# Anchor

Anchor is an offline-first Android MVP for saving personal song and photo anchors. It is built with Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, and app-private photo storage.

## MVP Scope

- Save shared songs and selected/shared photos.
- Keep all app data on device; the app manifest must not include `android.permission.INTERNET`.
- Use `Room` for resources, tags, and tag links.
- Use `DataStore` for theme, onboarding, and app language.
- Copy photos into `filesDir/photos/` and thumbnails into `cacheDir/anchor-thumbnails/`.
- Provide Home, Help now, Library, Timeline, Detail, Tag manager, Settings, and Onboarding flows.

## Verification

Run these after implementation changes:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug :app:detekt :app:ktlintCheck
```

Run `.\gradlew.bat :app:connectedDebugAndroidTest` only when a device or emulator is available.

The user-owned `lc` and `sc` wrappers are intentionally not run by agents.

## Privacy And Compliance

- Privacy policy draft: [docs/privacy-policy.md](docs/privacy-policy.md)
- Play release checklist: [docs/play-release-checklist.md](docs/play-release-checklist.md)

Current MVP claim: no network permission, no analytics SDK, no crash SDK, no ads SDK, no billing SDK, and no off-device sync.

## Release Signing

Release signing uses the `ANCHOR` environment variable prefix:

```powershell
$env:ANCHOR_KEYSTORE_PATH="C:\path\to\keystore.jks"
$env:ANCHOR_KEYSTORE_PASSWORD="password"
$env:ANCHOR_KEY_ALIAS="alias"
$env:ANCHOR_KEY_PASSWORD="password"
```

## License

MIT
