# Anchor MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement task-by-task. Do not run `lc` or `sc`; user runs those wrappers.

**Goal:** Muuttaa nykyinen Android Kotlin Starter Anchorin Phase 1 MVP:ksi: henkilökohtainen, offline-first laulu- ja kuvakirjasto, jossa on nopea “Help now” -polku, capture-flow’t, kirjasto, timeline ja perusasetukset.

**Locked decisions:** Toteutetaan ensin MVP, ei Phase 2. Sovelluksen työnimi on `Anchor`, paketti `com.finnvek.anchor`. Capture näyttää NEED-tagit ensin; FEELING on valinnainen laajennettava osio.

**Verified baseline:** Nykyinen `:app:assembleDebug` onnistuu, mutta app on vielä starter (`MyApp`, `com.finnvek.template`, `Text("Hello")`).

## 1. Foundation And Architecture

- Uudelleennimeä projekti: `rootProject.name = "Anchor"`, `applicationId` ja `namespace` muotoon `com.finnvek.anchor`, paketit `app/src/main/java/com/finnvek/anchor/...`.
- Aseta SDK:t: `minSdk = 26`, `compileSdk = 36`, `targetSdk = 35`. Target 35 täyttää nykyisen Google Play -vaatimuksen, mutta release-portilla se tarkistetaan uudelleen ennen uploadia.
- Päivitä vain vakaat runtime-riippuvuudet: Compose BOM `2026.05.01`, Navigation Compose `2.9.8`, Activity Compose `1.13.0`, DataStore `1.2.1`; pidä Room `2.8.4`, Lifecycle `2.10.0`, Hilt `2.59.2`. Lisää Coil 3 `3.5.0` ja AppCompat `1.7.1` kielivalintaan. Älä ota Material3 `1.5.0-alpha*` käyttöön MVP:ssä; käytä vakaata Material3 `1.4.0`-linjaa.
- Lisää testiriippuvuudet: Compose UI test JUnit4, Compose UI test manifest, AndroidX Test Core tarvittaessa, ja pidä Room testing.
- Manifestiin ei lisätä `INTERNET`-permissionia. Lisää `ShareCaptureActivity` exported share targetiksi `ACTION_SEND` / `text/plain` ja `image/*`, koska Androidin share target vaatii manifest intent-filterit ja käyttäjän pitää saada vahvistaa/muokata sisältö ennen käyttöä.
- Pidä MVP:ssä `android:allowBackup="false"` privacy-lupauksen vuoksi. Speksin dynaaminen “backup sallittu vain app lock off” siirretään Phase 2:een, koska staattiset backup XML -säännöt eivät turvallisesti reagoi app lock -asetukseen; Android backup -käyttäytyminen on muutenkin target- ja OS-versioriippuvaista.
- Päivitä `AGENTS.md` arkkitehtuurimuutoksen jälkeen: nykyinen paketti, single-module MVVM, Room/DataStore/PhotoStorage, “no internet permission”, testikomennot. Luo `memory/MEMORY.md` repo-dokumentiksi vain jos repo ottaa sellaisen käyttöön tässä muutoksessa; älä muokkaa globaalia Codex-muistia ilman erillistä pyyntöä.

## 2. Data, Domain, And Storage

- Luo domain-tyypit: `ResourceType { SONG, PHOTO }`, `TagCategory { FEELING, NEED }`, `TagRole { FEELING_THEN, HELPS_WITH }`, `ThemeMode { SYSTEM, LIGHT, DARK, CALM }`, `LibraryContentType { ALL, SONGS, PHOTOS }`, `LibraryViewMode { LIBRARY, TIMELINE }`.
- Room schema v1:
  - `resources`: `id`, `type`, `created_at_ms`, `title`, `subtitle`, `uri`, `source_app`, `note`, `body_feel`, `favorite`, `times_used`, `last_used_at_ms`, `thumbnail_path`.
  - `tags`: `id`, `preset_key`, `custom_label`, `emoji`, `category`, `is_preset`, `is_hidden`, `sort_order`.
  - `resource_tag_refs`: `resource_id`, `tag_id`, `role`; primary key `(resource_id, tag_id, role)`, cascade delete from resources/tags.
- Preset-tagit tallennetaan `preset_key`-avaimilla, ei raakateksteinä, jotta FI/EN-lokalisointi toimii. `custom_label` yliajaa presetin tekstin vain jos käyttäjä muokkaa labelia. Presetit voi piilottaa, ei poistaa.
- Seedaa presetit ensimmäisessä käynnistyksessä idempotentisti `TagSeeder`-luokalla. FEELING: calm, safe, joyful, energized, melancholy, anxious, angry, restless, numb, sad. NEED: calm down, release anger, feel safe, get energy, ground myself, feel less alone, fall asleep, ride out a wave.
- Repository-rajapinnat:
  - `ResourceRepository`: observe home/recent/library/detail/help-results, save capture, update content/tags, favorite, increment usage, delete.
  - `TagRepository`: observe visible tags, create custom tag, update custom label/emoji/order, hide preset, delete custom.
  - `SettingsRepository`: observe/update theme, onboarding done, selected app locale.
- `PhotoStorage` kopioi valitun tai jaetun kuvan app-private-hakemistoon `filesDir/photos/`, luo thumbnailin `cacheDir/anchor-thumbnails/`, palauttaa suhteelliset polut, ja poistaa molemmat resource-deletessä. Photo Picker on oikea MVP-valinta, koska se antaa pääsyn vain käyttäjän valitsemaan mediaan eikä koko mediakirjastoon.
- `ShareIntentParser` tukee Spotify-, YouTube Music- ja generic URL -tekstiä. Jos parse epäonnistuu, titleksi asetetaan raw shared text ja kentät jäävät muokattaviksi.

## 3. UI And Feature Tasks

- Design system:
  - Korvaa starter-teema `AnchorTheme`-teemalla: dynamic color Android 12+, Calm fixed fallback, light/dark, keskitetyt `AnchorSpacing`, `AnchorShapes`, `AnchorMotion`.
  - Kaikki mitat, muodot ja värit tulevat tokenien kautta; ei inline-väri- tai spacing-arvoja.
  - Motion: kriisipolku käyttää vain välittömiä/fade-siirtymiä; muut polut voivat käyttää M3-container-siirtymiä.
- Navigation shell:
  - `MainActivity` hostaa `AnchorAppRoot`.
  - Bottom navigation: Home, Library, You.
  - Reitit: onboarding, home, library, timeline mode, resource detail, help need picker, help results, photo pager, settings, tag manager, internal photo capture.
  - `ShareCaptureActivity` hostaa vain capture-sheetin ja sulkee itsensä save/cancel jälkeen, jotta käyttäjä palaa musiikki- tai galleria-appiin.
- Capture:
  - Yhteinen `CaptureScreen` lauluille ja kuville.
  - Save sallitaan ilman tageja.
  - NEED-chipit näkyvät ensin; FEELING on “Add how it felt” -laajennus.
  - “+ more” näyttää kaikki tagit ja inline custom tag -luonnin.
  - Photo capture käyttää `PickVisualMedia`; Activity Compose fallback hoitaa vanhemmat laitteet dokumentoidusti.
- Home:
  - Ensimmäinen focusable elementti on vähintään 96dp korkea “What do you need right now?” -kortti.
  - Näytä recent resources ja hiljainen viikon stat-rivi: saves count + top tag counts.
  - Global empty state kertoo kaksi capture-tapaa ilman syyllistävää tekstiä.
- Help now:
  - Need picker on full-screen, close X, suuret 64–80dp kortit, ei tekstisyöttöä.
  - NEED-sorttaus: ensin tagit, joilla on eniten `times_used`-käyttöjä niihin linkittyneissä resursseissa, sitten `sort_order`.
  - Results: resurssit järjestyksessä favorite desc, times_used desc, last_used_at desc, created_at desc.
  - Resurssin avaaminen tästä polusta kutsuu `incrementUsage(resourceId, now)`.
  - Laulu avataan ulkoisella intentillä; jos se epäonnistuu, snackbar näyttää “Couldn’t open this” + “Search instead”.
- Library, Timeline, Detail:
  - Library: All/Songs/Photos segmented control, search title/subtitle/note/bodyFeel, filter sheet feeling/need/favorite, mixed list.
  - Timeline: sama data päiväryhmittäin uusimmasta vanhimpaan, note ja tagit näkyvissä.
  - Detail: kuva tai song card, tagien editointi, note/bodyFeel editointi, favorite, usage count, delete confirmation.
- You / Settings:
  - Theme: System, Light, Dark, Calm.
  - Language: System, Finnish, English; toteutus AppCompatDelegate + `values/strings.xml` ja `values-fi/strings.xml`.
  - Manage tags: piilota preset, muokkaa custom label/emoji/order, poista custom.
  - Privacy/About: “No internet permission. Your collection stays on this device.” Data Safety -teksti pidetään linjassa Playn vaatimuksen kanssa, koska myös closed/open/production trackit tarvitsevat Data Safety -lomakkeen.
  - Ei näytetä app lock-, export-, widget- tai billing-kontrolleja MVP:ssä.

## 4. Implementation Order

1. **Identity + dependency commit:** nimeä projekti ja paketit, päivitä version catalog, lisää Coil/AppCompat/test deps, varmista `:app:assembleDebug`. Commit: `chore: nimeä Anchor-projekti`.
2. **Theme + navigation shell:** rakenna `AnchorTheme`, design tokens, bottom nav ja tyhjät reitit. Commit: `feat: lisää Anchorin teema ja navigaatiorunko`.
3. **Domain + Room:** lisää entityt, DAO:t, converters, database module, seedatut tagit, repositoryt ja DAO-testit. Commit: `feat: lisää paikallinen tietomalli`.
4. **Settings + localization:** DataStore, theme mode, AppCompat-locale, EN/FI-stringit. Commit: `feat: lisää asetukset ja lokalisointi`.
5. **Photo storage:** `PhotoStorage`, picker draft, thumbnailing, delete cleanup, unit/instrumentation testit. Commit: `feat: lisää kuvien tallennus appin sisäiseen tallennukseen`.
6. **Capture:** `ShareCaptureActivity`, intent parser, capture UI, save flow song/photo. Commit: `feat: lisää laulu- ja kuvacapture`.
7. **Home + Help now:** hero, need picker, results, photo pager, external song open, usage counting. Commit: `feat: toteuta help now -polku`.
8. **Library + Timeline:** list/grid, filters, search, grouped timeline. Commit: `feat: lisää kirjasto ja timeline`.
9. **Detail + tag manager:** editointi, favorite, delete, tag management. Commit: `feat: lisää resurssin tiedot ja tagien hallinta`.
10. **Onboarding + empty states + polish:** 2-screen onboarding, all empty states, accessibility semantics, 200% font scale pass. Commit: `feat: viimeistele onboarding ja tyhjät tilat`.
11. **Docs/compliance:** päivitä `README.md`, `AGENTS.md`, privacy policy, Play checklist. Commit: `docs: päivitä Anchorin projektidokumentaatio`.

## 5. Test And Acceptance Plan

- Commands after each major task:
  - `.\gradlew.bat :app:assembleDebug`
  - `.\gradlew.bat :app:testDebugUnitTest`
  - `.\gradlew.bat :app:lintDebug :app:detekt :app:ktlintCheck`
  - `.\gradlew.bat :app:connectedDebugAndroidTest` only when device/emulator is available.
- Unit tests:
  - `ShareIntentParserTest`: Spotify URL, YouTube Music URL, generic URL, raw unparseable text.
  - `TagSeederTest`: idempotent seed, FI/EN preset keys preserved, hidden presets excluded.
  - `ResourceRepositoryTest`: save song/photo, query by need, favorite/usage ordering, timeline grouping, cascade delete.
  - `SettingsRepositoryTest`: theme and language persistence.
- Android/Compose tests:
  - First launch shows onboarding; skip persists.
  - Capture save works with zero tags, one NEED, NEED+FEELING, and custom tag.
  - Help-now increments usage and reorders results.
  - Library search and filters combine correctly.
  - Detail delete removes DB row and private photo files.
- Manual UAT:
  - Share text intent to `ShareCaptureActivity` opens capture and returns after save.
  - Share image intent copies the image; deleting gallery original does not break Anchor copy.
  - No `android.permission.INTERNET` appears in merged manifest.
  - TalkBack order on Help-now: title, need list, close.
  - 200% font scale: capture sheet, need cards, bottom nav, detail buttons remain usable.
- Play/compliance checks:
  - Health declaration must be completed if category is Health & Fitness or wellness-adjacent; Play reviews the submitted health features.
  - Data Safety says no app-collected/shared data only if no analytics, no network SDKs, no crash SDKs, and no INTERNET permission remain true.
  - EU MDR wording stays non-medical: no “treats”, “therapy”, “symptom relief”, “clinical”, or “anxiety treatment”; intended purpose remains personal wellness/self-management, because MDR qualification depends on intended purpose statements.

## Explicit Assumptions

- MVP excludes Glance widget, app lock, export ZIP, standalone text diary, billing/free cap, analytics, voice memos and in-app music playback.
- “Nothing leaves your phone” takes precedence over Auto Backup in MVP, so backup remains disabled until app lock/export policy is designed together.
- Package `com.finnvek.anchor` is accepted now; Play Store name/domain collision checks happen before release, not before coding.
- Material3 Expressive is treated as design direction only; MVP uses stable Material3 APIs and tokenized shapes/motion.
