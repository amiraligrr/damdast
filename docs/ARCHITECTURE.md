# Architecture

## Stack

- Kotlin 2.1.20, Jetpack Compose (BOM 2025.02.00), Material 3
- AGP 8.9.3, Gradle 8.13, compileSdk/targetSdk 36, minSdk 24, JDK 17
- DataStore Preferences for settings and small structured local datasets
- CameraX 1.4.1 + ZXing core for on-device scanning and code generation
- PDFBox-Android 2.0.27.0 for PDF editing and text extraction
- Android `PdfRenderer`/`PdfDocument` for viewing and rendering
- Tapsell 1.3.0 + legacy adapter for ads
- Vazirmatn (Persian) and Manrope (English) fonts, bundled with OFL licenses

No Room database is used. History, favorites, notes, tasks, scan history and PDF
bookmarks are small structured lists that fit comfortably in DataStore JSON. This
keeps the app lightweight and avoids a database for a few hundred records. Writes
are asynchronous through `suspend` repository methods.

## Packages

```
ir.amiraligrr.damdast
├── DamdastApp.kt            Application, repositories, ads manager, PDFBox init, locale
├── DamdastRoot.kt           settings gate, onboarding vs main shell, system bars
├── MainActivity.kt          edge-to-edge, shortcut intent, ViewModel
├── ads/
│   ├── AdPlacementConfig.kt Tapsell placement map and enabled flags
│   ├── AdsManager.kt        interface, state model, NoOp implementation
│   ├── AdFrequencyController.kt  DataStore-backed interstitial caps
│   ├── TapsellAdsManager.kt Tapsell 1.3.0 implementation
│   └── ui/AdComponents.kt   banner, native card, rewarded dialog, premium gate
├── data/
│   ├── Models.kt            settings/user data models
│   ├── SettingsRepository.kt
│   ├── UserDataRepository.kt
│   ├── DamdastViewModel.kt  app-scoped state and commands
│   └── Store.kt             single DataStore instance
├── tools/ToolCatalog.kt     87 tool definitions, categories, search metadata
├── ui/
│   ├── components/          design system and shared UI
│   ├── onboarding/          3-step flow + animated brand illustration
│   ├── home/                home, categories, favorites
│   ├── history/, search/, settings/
│   ├── tools/               shared tool screens by domain
│   └── theme/               color, type, shape, motion
└── util/                    pure logic (tested) + Android helpers
```

## Navigation

`MainShell` owns a Navigation Compose graph:

- Bottom tabs: `home`, `categories`, `favorites`, `history`, `settings`
- Detail routes: `search`, `category/{category}`, `tool/{toolId}`, `about`,
  `privacy`, `licenses`
- The bottom bar is hidden on detail/tool routes, which use `ToolPage`.
- Tool use is recorded in DataStore; the tool-to-home transition can trigger a
  frequency-capped interstitial only for non-media categories.

## State and persistence

- `DamdastViewModel` exposes settings, favorites, recent tools, scans, notes, tasks
  and PDF bookmarks as `StateFlow` values.
- `SettingsRepository` stores theme, language, onboarding flag, rewarded access
  expiry and the debug ad toggle.
- `UserDataRepository` stores favorites as a string set and bounded JSON lists
  (recent tools ≤ 50, scans ≤ 100). Notes/tasks are upserted by id.
- No main-thread disk/network work: repository writes run in coroutines; scanning,
  PDF, ZIP, image and file decoding use `Dispatchers.IO`/`Dispatchers.Default`.

## Localization and RTL

- English is the default resource set; Persian is in `values-fa`.
- `AppCompatDelegate.setApplicationLocales` applies language immediately and
  persists it via AppCompat's auto-store service.
- Compose additionally provides `LocalLayoutDirection` based on the selected
  language.
- `Formatters.kt` normalizes Persian/Arabic digits on input and localizes numbers,
  dates and times on output. `PersianCalendar` uses the jalaali-js algorithm.
- Vazirmatn is used for Persian; Manrope for English.

## Design system

`ui/theme` defines deep-navy/purple dark surfaces, off-white/white light surfaces,
rounded shapes, typography, motion constants and reduced-motion detection.
Shared components include buttons, cards, tool tiles, search fields, empty/loading/
error panes, top bars, bottom navigation, dialogs and destructive confirmations.

## Scanning and generation

- `QrToolsScreen` requests camera permission only on scanner screens.
- CameraX `ImageAnalysis` feeds a ZXing `MultiFormatReader`; frames are throttled
  and the analyzer locks after a result. The analyzer supports Y-plane rotation.
- Gallery scanning uses the system photo picker and `RGBLuminanceSource`.
- Generators use ZXing writers and save/share via SAF and FileProvider.
- Scans are stored in DataStore, capped at 100.

## PDF

- Viewer: `PdfRenderer` renders one page at a time; PDFBox extracts text for
  search; bookmarks are stored per document URI.
- Editors: PDFBox for merge/extract/rotate/watermark/page numbers; Android
  `PdfRenderer` for PDF→PNG ZIP; Android `PdfDocument` for images→PDF.
- Text editing and annotations are deliberately not claimed.

## Images

- Loaded via `ImageDecoder` (API 28+) or sampled `BitmapFactory`, capped at 2048 px.
- Operations are pure bitmap transforms (crop, resize, rotate, flip, ColorMatrix
  adjustments/filters, centered text/shapes, collage). Save uses SAF; share uses
  FileProvider.
- EXIF is read through AndroidX ExifInterface.

## Files

- Viewers use SAF `GetContent`, read at most 5 MB and decode off the main thread.
- ZIP create/extract use `java.util.zip` and SAF; paths are sanitized to prevent
  directory traversal and extraction happens only in a user-selected folder.

## Testing

`app/src/test` contains 19 JVM tests for calculator, Persian calendar round-trips,
text transforms/Base64, password/random generation, JSON/code formatting, colors,
Unix permissions, unit/base conversion, URLs/JWT/CSV and date math.

Runtime/device tests still required: onboarding recreation, RTL, camera permission
denial, PDF/ZIP SAF flows, activity recreation, no-internet startup and live Tapsell
callbacks.
