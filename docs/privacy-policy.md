# Anchor Privacy Policy Draft

Last updated: 2026-06-13

Anchor is a personal offline-first app for saving songs, photos, notes, and tags that help you find useful anchors later.

## Data Stored By The App

Anchor stores the following data on your device:

- Saved song links and editable song titles/subtitles.
- Saved photo copies selected or shared by you.
- Notes, body-feel text, favorite state, usage counts, and timestamps.
- Built-in and custom tags.
- App settings such as theme, onboarding status, and selected app language.

## No Network Transfer

The MVP app does not request `android.permission.INTERNET`. It does not include analytics, ads, crash reporting, cloud sync, account login, or remote backup code.

## Local Photo Storage

Photos selected or shared by you are copied into the app-private files directory. Anchor also creates app-private thumbnail cache files. Deleting a photo resource in Anchor is intended to remove both the saved copy and its thumbnail.

## Sharing And External Apps

Anchor can receive shared text or images from other apps through Android share intents. When you open a saved song, Android may hand the saved link to another app or browser on your device. That external app handles the link under its own privacy policy.

## Data Safety Draft Position

Based on the current MVP code, Anchor is intended to declare no app-collected or shared user data in Google Play Data Safety only while all of these remain true:

- No `INTERNET` permission.
- No analytics, ads, crash reporting, or remote logging SDK.
- No account, sync, export upload, or cloud backup feature.
- No third-party network library that transmits user data.

If any of those change, this policy and the Google Play Data Safety form must be updated before release.

## Contact

Add the developer contact email before publishing.
