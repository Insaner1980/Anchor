# Anchor Play Release Checklist

Use this before internal, closed, open, or production Play tracks.

## Code Gates

- Run `.\gradlew.bat :app:assembleDebug`.
- Run `.\gradlew.bat :app:testDebugUnitTest`.
- Run `.\gradlew.bat :app:lintDebug :app:detekt :app:ktlintCheck`.
- Confirm the merged manifest does not contain `android.permission.INTERNET`.
- Confirm `android:allowBackup="false"` remains in the manifest.
- Confirm no analytics, ads, crash reporting, billing, sync, or network SDK was added.

## Google Play App Content

- Data Safety form: required in Play Console. The current MVP should only claim no collected/shared data if the code gates above remain true.
- Privacy policy: required for Data Safety. Publish a reviewed version of `docs/privacy-policy.md` and add its URL in Play Console.
- Health Apps declaration: required for all apps on Play tracks. If Anchor is positioned as wellness/self-management, declare the applicable health/wellness features accurately.
- Health Connect: not used in the MVP.

## Store Wording Guardrails

Use personal wellness/self-management wording. Avoid medical intended-purpose wording such as:

- treats
- therapy
- symptom relief
- clinical
- anxiety treatment
- diagnosis

## Manual UAT

- First launch shows onboarding and skip/start persists.
- Share text into Anchor opens capture and returns after save/cancel.
- Share image into Anchor copies the image and returns after save/cancel.
- Deleting a photo resource removes the app-private copy and thumbnail.
- Help now increments usage and opens song links externally.
- 200% font scale remains usable on onboarding, capture, help cards, bottom navigation, and detail actions.
