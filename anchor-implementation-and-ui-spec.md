# Anchor (working title) - Implementation & UI Spec

**Type:** Android app, Kotlin + Jetpack Compose
**Concept:** A personal emotional resource library. Users collect songs and photos that are tied to specific feelings and bodily states, tag them with what they help with, and retrieve them quickly in the moment of need. Functions as both a "hope box" toolkit and a lightweight mood diary that builds itself as a side effect of saving resources.
**Origin:** Idea from a psychophysical physiotherapist, describing what her clients need when they are alone between sessions.
**Positioning:** Personal collection and journaling tool. NOT marketed as therapy, treatment, or a medical device. This keeps it outside EU MDR scope and simplifies Play Store review.

---

## 1. Product Principles

1. **Privacy absolute.** Everything stays on device. No account, no cloud, no analytics by default (opt-in privacy-first analytics like Aptabase/TelemetryDeck can be considered later, consistent with the rest of the Finnvek portfolio). "Nothing ever leaves your phone" is a core selling point for a mental-wellbeing adjacent app.
2. **Fast in crisis, gentle in calm.** Two usage modes drive the entire UI: (a) "I feel bad right now, give me my tools" must work in under 5 seconds with zero typing; (b) "I want to save this song/photo and reflect" can be slower and richer.
3. **Capture must be near-zero friction.** Saving a song happens through the Android share sheet from Spotify/YouTube Music. Saving a photo happens through the system photo picker or share sheet. If capture takes more than ~15 seconds, users will stop doing it.
4. **The diary is a byproduct, not a chore.** Every saved resource with its tags and optional note IS a diary entry. A timeline view emerges for free. No "you must journal daily" pressure, no streaks, no guilt mechanics.
5. **No content of our own.** The app ships empty. All value comes from the user's own songs and photos. This avoids licensing entirely and makes the collection deeply personal, which is the whole point.

---

## 2. Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin | |
| UI | Jetpack Compose, Material 3 | Compose BOM 2025.12.00+ (Compose 1.10 / Material3 1.4 stable). Material 3 Expressive components are in material3 1.5.0 alphas and are planned to go stable later in 2026; design for Expressive, but every component below has a stable M3 fallback. Decide alpha vs stable at implementation time based on what is stable that week. |
| Theming | Material You dynamic color + custom fallback scheme | Dynamic color on Android 12+, custom calm palette below as fallback and as an optional fixed theme |
| Database | Room | Single DB: resources, tags, diary notes |
| Preferences | DataStore | Theme choice, app lock setting, onboarding state |
| Images | Coil 3 | Loading thumbnails and full images |
| Photo access | Android Photo Picker (`PickVisualMedia`) | No READ_MEDIA permission needed. App copies picked images into app-private storage so the collection is independent of the gallery |
| Music links | ACTION_SEND share target + deep links out | No audio is ever stored or played by the app itself |
| App lock | BiometricPrompt (optional, user-enabled) | Class 2/3 biometrics with device credential fallback |
| Widget | Glance | One "I need help now" widget, phase 2 |
| Min SDK | 26 (Android 8.0) | Photo Picker is backported via Google Play services to 4.4+; dynamic color gracefully degrades below 12 |
| Target SDK | Current Play requirement at release time | Verify at implementation; do not hardcode now |
| Architecture | Single module, MVVM, Compose Navigation | Same shape as KnitTools, keep it boring |

No INTERNET permission in the MVP. This is verifiable by users and reviewers and is the strongest possible privacy statement.

---

## 3. Data Model

```
Resource
  id: Long (PK)
  type: enum { SONG, PHOTO }            // extensible: VIDEO, QUOTE, AUDIO_NOTE later
  createdAt: Instant
  title: String                          // song title or user caption
  subtitle: String?                      // artist name for songs
  uri: String                            // deep link (songs) or internal file path (photos)
  sourceApp: String?                     // "spotify", "youtube_music", "other"
  note: String?                          // free-text diary note, optional
  feelingThen: List<TagRef>              // how I felt when this happened / when I saved it
  helpsWith: List<TagRef>                // what this resource helps me with
  bodyFeel: String?                      // optional free text: where/how it felt in the body
  favorite: Boolean
  timesUsed: Int                         // incremented when opened from "Help now" flow
  lastUsedAt: Instant?

Tag
  id: Long (PK)
  label: String
  emoji: String?
  category: enum { FEELING, NEED }
  isPreset: Boolean                      // presets can be hidden but not deleted
  sortOrder: Int
```

**Preset tags (editable, localizable, FI + EN at launch):**

- FEELING (how it felt): calm, safe, joyful, energized, melancholy, anxious, angry, restless, numb, sad
- NEED (helps me to): calm down, release anger, feel safe, get energy, ground myself, feel less alone, fall asleep, ride out a wave

The NEED category is the heart of the app. The physiotherapist's examples map directly: "a song that discharges my aggression" = helps with *release anger*; "a nature photo where I noticed I felt peaceful" = feeling *calm*, helps with *ground myself / calm down*.

Photos are copied to `filesDir/photos/` with a downscaled cached thumbnail in `cacheDir`. Original gallery file is never modified. Backups: include the Room DB and photos dir in Android Auto Backup only if app lock is off; if app lock is on, exclude everything via backup rules (document this tradeoff in Settings).

---

## 4. Core Flows

### 4.1 Capture: Song (share target)

1. User hears a song in Spotify / YouTube Music / anywhere, taps Share, picks Anchor.
2. App receives `ACTION_SEND` with text (URL + usually "Song - Artist" in EXTRA_TEXT/SUBJECT). Parse title/artist heuristically per source; if parsing fails, show editable fields prefilled with raw text.
3. A single bottom-sheet style capture screen opens (not the full app): parsed song card on top, then "How do you feel?" chip row (FEELING tags), then "This helps me..." chip row (NEED tags), then optional one-line note field, Save button.
4. Save and close, returning the user to their music app. Total interaction: two chip taps + Save, under 10 seconds.

Deep link out: store the received URL as-is. Opening a Spotify URL re-opens Spotify, a YouTube Music URL opens YT Music. No API integration, no SDKs, no tokens.

### 4.2 Capture: Photo

Two entry points:
- Share sheet from the gallery app (same capture sheet as songs, with image preview instead of song card).
- Inside the app: "Add photo" launches the system Photo Picker, then the same capture sheet.

On save: copy image into app storage, generate thumbnail, store tags/note.

### 4.3 Retrieve: "Help now" (the crisis path)

Entry: big primary action on Home, plus app shortcut (long-press icon), plus widget in phase 2.

1. Screen asks one question: **"What do you need right now?"** with the NEED tags as large tappable cards (min height 64dp, one column on small screens). No text input anywhere on this path.
2. Tapping a need shows the user's matching resources, favorites and most-used first. Songs show a play button (opens source app), photos open full-screen immediately with a swipeable pager.
3. Each open from this path increments `timesUsed`, so the list self-organizes toward what actually works.
4. Empty state per need: "Nothing saved for this yet. When something helps, share it here so it's ready next time." Calm tone, no exclamation marks.

### 4.4 Browse: Library and Timeline

- **Library:** grid/list of all resources, filter chips by type (Songs / Photos), by FEELING and by NEED. Search by title/artist/note text.
- **Timeline (diary view):** the same data grouped by day, newest first. Each entry shows resource + tags + note. This satisfies the "päiväkirjamaisesti" half of the idea with zero extra user effort. Optional: allow standalone text-only entries later (phase 2), but MVP keeps diary = saved resources only.

### 4.5 Resource detail

Full view of one resource: large image or song card, all tags (editable), note (editable), body-feel text, favorite toggle, "used X times", delete (with confirmation). For songs: "Open in Spotify/YouTube Music" primary button.

---

## 5. UI / UX Specification

### 5.1 Style direction

**Material Design 3, Expressive flavor.** Android app, so platform conventions apply (per design skill decision flow). Modern Material here means: dynamic color (Material You), tonal surfaces instead of borders, large shape language, expressive motion on the calm paths, and restrained motion on the crisis path. The crisis path deliberately uses LESS expressiveness: bigger targets, fewer elements, no animation that delays interaction.

Light and dark theme both supported, following system. Many users will reach for this app at night; dark theme is not an afterthought, design it first.

### 5.2 Color

**Primary approach: dynamic color** (`dynamicLightColorScheme` / `dynamicDarkColorScheme` on Android 12+). The app then feels personal automatically, which fits the product.

**Fallback / fixed "Calm" scheme** (also offered as a manual choice in Settings for users who find their wallpaper-derived palette agitating):

| Token | Light | Dark |
|---|---|---|
| primary | #4A6B5D (muted sage green) | #A8C5B4 |
| onPrimary | #FFFFFF | #15291F |
| primaryContainer | #CDE8D9 | #334B40 |
| secondary | #5C6B7A (calm blue-gray) | #B8C5D4 |
| tertiary | #8A6E5C (warm sand) | #D9BBA8 |
| surface | #F7F9F6 | #121514 |
| surfaceContainer | #ECEFEA | #1B1F1D |
| error | standard M3 error roles | standard M3 error roles |

Rationale: green/blue-gray/sand reads as nature and ground, matching the photo use case (nature photos, calm states) without being a clinical "mental health teal" cliche. All pairs must pass WCAG AA; run the palette through the Material theme builder at implementation time rather than trusting these hex values blindly.

Semantic accent for the "Help now" action: use `primaryContainer` at rest, never red. Red implies emergency services; this is a self-help tool.

### 5.3 Typography & shape

- Material 3 type scale, default Roboto. No custom font in MVP.
- Capture sheet and Help-now cards use `titleLarge` for tag labels; this app is used by people in distress, small text is hostile.
- Shape: large end of M3 scale. Cards 16dp, capture bottom sheet 28dp top corners, Help-now need cards 20dp. Photos in grids 12dp.
- Spacing on the 8dp grid. Help-now path: minimum 16dp gaps, generous padding, max ~5 interactive elements visible at once.

### 5.4 Navigation structure

Bottom navigation bar, 3 destinations (M3 NavigationBar):

```
┌──────────────────────────────────┐
│            Top app bar           │
│                                  │
│           (screen content)       │
│                                  │
│                                  │
│  ┌────────────────────────────┐  │
│  │   [Home]  [Library] [You]  │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

- **Home:** Help-now entry + recent activity
- **Library:** all resources + filters + search; Timeline as a tab or segmented toggle inside Library
- **You:** settings, tags management, app lock, theme, about/privacy

No FAB fighting with the bottom bar on Home; the Help-now card IS the primary action. Library gets a FAB ("Add") that opens a small menu: Add photo / How to save songs (a one-screen illustrated reminder that songs are saved by sharing from a music app).

### 5.5 Screens

**Home**

```
┌──────────────────────────────────┐
│  Anchor                    [⚙]   │
│                                  │
│  ┌────────────────────────────┐  │
│  │  What do you need          │  │
│  │  right now?                │  │
│  │            [large card,    │  │
│  │             primaryCont.]  │  │
│  └────────────────────────────┘  │
│                                  │
│  Recently saved                  │
│  ┌─────┐ ┌─────┐ ┌─────┐         │
│  │photo│ │song │ │photo│  ...    │
│  └─────┘ └─────┘ └─────┘         │
│                                  │
│  This week                       │
│  "3 saves · calm × 2 · anger ×1" │
│  (single quiet stat line, no     │
│   charts, no streaks)            │
│                                  │
│  [Home]   [Library]   [You]      │
└──────────────────────────────────┘
```

The hero card is at least 96dp tall, full width, and is the first focusable element. Long-press app icon shortcut "I need help now" deep-links straight past Home into the need picker.

**Help now: need picker**

Full-screen, top app bar with only a close X. Title `headlineSmall`: "What do you need right now?". NEED tags as a single-column list of large cards (64-80dp), each with emoji + label. Most-used needs sort to top. Scrim-free, instant, no entrance animation beyond a fast fade (under 150ms).

**Help now: results**

Top: chosen need as a removable chip (tap to go back). Content: horizontally-paged photo hero section if photos exist (swipe through full-bleed images), then song list below. Song row: album-art placeholder icon, title, artist, trailing "Open" filled tonal button. One tap opens the music app. If only photos exist, go straight to the full-screen photo pager.

**Capture sheet (shared for songs and photos)**

Modal bottom sheet, 28dp top radius:

```
┌────────────────────────────────┐
│  ── (drag handle)              │
│  ┌──────────────────────────┐  │
│  │ ♪ Song title             │  │
│  │   Artist · Spotify       │  │
│  └──────────────────────────┘  │
│  How did you feel?             │
│  (calm)(safe)(joyful)(anxious) │
│  (angry)(restless)(+ more)     │
│                                │
│  This helps me...              │
│  (calm down)(release anger)    │
│  (feel safe)(ground myself)…   │
│                                │
│  [ Add a note (optional)     ] │
│                                │
│  [        Save        ]        │
└────────────────────────────────┘
```

FilterChip components, multi-select, selected state uses `secondaryContainer`. "+ more" expands the full tag set and offers "new tag" inline creation. Save is enabled even with zero tags selected (never block a save; untagged items land in Library and can be tagged later).

**Library**

Segmented buttons: All / Songs / Photos. Filter row of assist chips opening a filter sheet (by feeling, by need, favorites). Photos in a 3-column grid, songs as rows, mixed view as a sectioned list. Toggle to Timeline view (grouped by date with notes visible).

**You / Settings**

Plain M3 settings list: Theme (System / Light / Dark / Calm fixed palette), App lock (biometric toggle + note about backups being disabled when on), Manage tags, Language (FI/EN), Export my data (zip of photos + JSON, saved via SAF, phase 2), Privacy statement ("This app has no internet permission. Nothing leaves your phone."), About.

### 5.6 Motion

- Calm paths (Library, detail, capture): standard M3 Expressive motion, spring-based container transforms, shared element transition from grid thumbnail to full photo.
- Crisis path (Help now): fades and instant transitions only. Nothing bounces at a person having a panic attack.
- Respect "remove animations" / reduced motion system setting globally.

### 5.7 States and edge cases

- **First launch:** 2-screen onboarding max. Screen 1: what this is (your own songs and photos, for the moments you need them). Screen 2: how to save a song (share sheet illustration) + Add first photo button. Skippable.
- **Global empty:** Home hero still works but leads to an empty-state explainer with the two capture methods.
- **Per-need empty:** see 4.3.
- **Broken deep link** (song removed from service, app uninstalled): open attempt fails → snackbar "Couldn't open this in Spotify" with "Search instead" action that fires a web search intent for "title artist". Never delete the entry automatically; the memory of the song still has value.
- **Photo source deleted from gallery:** irrelevant, we own a copy.
- **Very long titles/notes:** 2-line ellipsis in lists, full text in detail.
- **Large libraries:** Paging 3 if needed; realistically a few hundred items, LazyColumn/LazyVerticalGrid with keys is fine.
- **Share parse failure:** capture sheet opens with raw shared text in an editable title field; never reject a share.
- **App lock enabled:** lock screen with biometric prompt on every cold start and after 1 min background. The Help-now app shortcut still requires unlock (privacy beats speed here; biometric is one touch anyway).

### 5.8 Accessibility

- All touch targets ≥ 48dp; Help-now path ≥ 64dp.
- Chips and need cards: emoji is decorative (`contentDescription = null`), label carries semantics.
- Photo entries: optional user caption doubles as contentDescription; fall back to "Photo saved on {date}, tagged {tags}".
- Dynamic type: layouts must survive 200% font scale, test the capture sheet specifically.
- TalkBack order on Help-now: title → needs list, nothing else focusable.

---

## 6. Implementation Plan (phases)

**Phase 1 - MVP (Play closed testing)**
1. Project setup, theme (dynamic + Calm fallback), navigation shell
2. Room schema + repositories
3. Photo capture: picker, copy-to-private, thumbnailing, capture sheet
4. Song capture: share target, source parsing (Spotify, YouTube Music, generic URL), capture sheet reuse
5. Library + filters + search, resource detail with edit/delete
6. Help-now flow end to end, usage counting
7. Timeline view
8. Settings: theme, tags management, FI/EN localization
9. Onboarding, empty states, polish pass

**Phase 2 - post-launch**
- Glance widget ("I need help now" one-tap)
- App lock (biometric) if not squeezed into MVP
- Export (SAF zip: JSON + photos)
- Standalone text-only diary entries
- Possibly: voice memos as a third resource type (record a calm message to yourself; the physiotherapist angle is strong here)

**Out of scope, permanently unless revisited:** accounts, cloud sync, in-app music playback, social/sharing features, AI anything, mood analytics dashboards.

---

## 7. Play Store & Compliance Notes

- **Category:** Health & Fitness or Lifestyle; choose based on competitor placement at submission time.
- **Health apps declaration:** Play requires a Health apps declaration form for wellbeing-adjacent apps; declare honestly as a wellness/self-management tool, not medical.
- **Wording rule everywhere (listing, app, site):** "helps you collect and find the songs and photos that feel good to you." Never "treats", "reduces symptoms", "therapy", "anxiety relief". This keeps it clearly outside medical device claims (EU MDR) and outside Play's stricter medical app policies.
- **Data Safety form:** no data collected, no data shared. With no INTERNET permission this is airtight.
- **Monetization:** one-time IAP consistent with portfolio. Free tier: full functionality with a cap (e.g. 15 saved resources), one-time unlock for unlimited + widget. Decide exact split later; do not cripple the Help-now flow in the free tier, that would be ugly in this product category.
- **Distribution angle:** psychophysical physiotherapists, psychologists, occupational therapists recommending tools to clients. A simple one-page "for professionals" section on the product site, printable client handout (QR code), and the originating physiotherapist as first professional reviewer.

---

## 8. Open Questions (decide before implementation)

1. Final name. "Anchor" is a placeholder; check Play Store collisions and domain availability before attachment forms.
2. Free/paid split (resource cap vs feature gating).
3. Material3 1.5.0 stable status at implementation start: if Expressive is stable, use it; if still alpha, ship on 1.4 stable and adopt Expressive in an update.
4. Should FEELING tagging be optional-by-default in the capture sheet (only NEED shown, FEELING behind "add feeling")? Leaning yes for capture speed; validate with the physiotherapist.
5. Finnish tag label wording: get the physiotherapist's input, her clinical vocabulary ("kehollinen", "maadoittuminen") may be better than direct translations.
