# Damdast — دم‌دست

**Made by Amiraligrr. For everyone, everywhere.**

Damdast is a local-first, offline-first Android toolbox. It combines QR/barcode tools,
PDF viewing and manipulation, image editing, text utilities, calculators, file viewers,
developer tools, date/time tools, color tools, and everyday utilities in one coherent
app. There is no account system and no Damdast backend. Files are processed on the
device unless the user explicitly shares them.

## Status

- Kotlin + Jetpack Compose application.
- 87 working tools in 10 categories. Every tool card opens a functional screen.
- Three-step first-launch onboarding: introduction → theme → language.
- Full English and Persian (RTL) interfaces, Persian digits and Jalali dates.
- Light and dark themes persisted with DataStore.
- Tapsell advertising integration: standard banner, native banner, interstitial
  (frequency capped) and rewarded video for optional advanced tools. Live SDK
  initialization was attempted on an emulator but did not complete (see below).
- Debug and release APKs build successfully.
- 19 JVM unit tests pass.

### Tool catalog (87)

| Category | Count | Included |
|---|---:|---|
| QR & Barcode | 8 | Camera scanner, gallery scan, QR generator, Wi-Fi QR, contact QR, barcode scanner/generator, scan history |
| PDF | 8 | Viewer with search/bookmarks, merge, extract, rotate, watermark¹, page numbers, images→PDF, PDF→images |
| Image | 11 | Viewer, crop, resize, rotate/flip, tone adjustment, filters, text/shapes, compress, convert, EXIF, collage¹ |
| Text | 12 | Counter, case, duplicate removal, sorting, reverse, whitespace, Base64, URL, HTML entities, compare, extraction, Lorem Ipsum |
| Calculator & Converters | 6 | Standard/scientific calculator, percentage, discount, profit, units, number bases |
| File | 8 | TXT/CSV/JSON/XML/Markdown viewers, file information, ZIP create/extract |
| Developer | 15 | JSON format/validate/minify, XML/HTML/CSS/JS formatters, UUID, hash, timestamp, regex, URL parser, JWT decoder, Unix permissions, color converter |
| Date & Time | 6 | Stopwatch, timer, countdown, world clock, date calculator, Persian/Gregorian conversion |
| Color & Design | 5 | Color picker, gradient, palette, image colors, font preview |
| Everyday | 8 | Password generator, random number, random selector, dice, coin flip, notes, tasks, home-screen shortcuts |

¹ Requires a rewarded-video unlock (30 minutes). Core tools remain free.

### Not implemented / intentionally limited

- PDF **text editing** is not supported. PDF annotations, highlights, freehand drawings,
  signatures, split-archive, reorder, and lossy compression are not in this release.
- Image collage gated by rewarded access; image crop uses percentage trims rather than
  a freeform drag handle.
- Batch rename is not included. Single-file rename is not included.
- Pre-roll and native-video ad formats are disabled: there is no video playback feature
  in this release.
- Release APK is unsigned unless local signing properties are provided.

### Runtime verification in this environment

An API 37 emulator smoke test was performed:

- App launched without a fatal crash.
- Three-step onboarding displayed in English (device language).
- Light theme selection applied immediately and survived a force-stop/relaunch.
- Persian selection recreated the UI in RTL with Vazirmatn and Persian digits
  without losing the onboarding step.
- Home showed Jalali date `۱۴۰۵/۰۸/۲۹`, RTL navigation and the full bottom bar.
- A launcher shortcut opened the calculator tool.
- Tapsell initialization was attempted, but the SDK logged
  `lateinit property mediationApplicationId has not been initialized` and could not
  fetch config on the emulator. The app remained usable. The integration compiles and
  fails safely, but live ad callbacks are **not** verified. Check that the supplied key
  is a Mediation app ID and verify placements in the Tapsell dashboard.
- The emulator displayed a system 16 KB page-size compatibility warning caused by
  third-party native libraries (DataStore/CameraX/Tapsell transitive deps), not by a
  Damdast crash. It is an Android 15/16 packaging follow-up.

## Onboarding

1. **Introduction** — brand visual, tool overview, tagline, privacy note. Uses the
   device language if it is English or Persian, otherwise Persian.
2. **Theme** — light/dark mini UI previews, immediate application, persisted.
3. **Language** — Persian/English, applied immediately to the whole UI with correct
   RTL/LTR. Language and theme are persisted across restarts. Onboarding is shown only
   on first launch unless restarted from Settings.

## Build

Requirements: JDK 17, Android SDK 36, network access for the first dependency sync.

```bash
cp local.properties.example local.properties
# Edit local.properties: sdk.dir and tapsell.app.key
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew testDebugUnitTest
```

APK outputs:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

See [docs/BUILD_AND_RELEASE.md](docs/BUILD_AND_RELEASE.md) for signing and release notes.

## Tapsell configuration

The real Tapsell app key is **never committed**. It is read from the ignored
`local.properties` file and injected through the `TapsellMediationAppKey` manifest
placeholder. Without a key, ads are disabled and the app remains fully usable.

```properties
# local.properties (ignored)
sdk.dir=/path/to/Android/Sdk
tapsell.app.key=YOUR_KEY_HERE
damdast.ads.disabled=false
```

Placement IDs and architecture are documented in
[docs/ADVERTISING.md](docs/ADVERTISING.md). Two supplied placements labelled as
instant banners have ambiguous formats; only candidate A is used, candidate B is
reserved and disabled.

## Privacy

- No account, no analytics SDK, no custom backend.
- Files and local data remain on the device.
- Android system pickers are used for file/image access.
- Camera is requested only for scanning.
- Privacy policy is available in Settings.
- Only the Tapsell SDK performs network requests (ads) and may use advertising IDs
  subject to its own policy.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Advertising](docs/ADVERTISING.md)
- [Build and release](docs/BUILD_AND_RELEASE.md)

## License and attribution

Application code and design © 2026 Amiraligrr. Fonts are under the SIL Open Font
License 1.1 (see `app/src/main/assets/fonts/`). Third-party libraries retain their
own licenses; see Settings → Open-source licenses.
