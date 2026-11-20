# Build and release

## Toolchain

| Item | Value |
|---|---|
| JDK | 17 |
| Gradle | 8.13 (wrapper committed) |
| Android Gradle Plugin | 8.9.3 |
| Kotlin | 2.1.20 |
| compileSdk / targetSdk | 36 / 36 |
| minSdk | 24 |
| Application ID | `ir.amiraligrr.damdast` |
| Version | 1.0.0 (1) |

The Google Maven mirror `https://maven.aliyun.com/repository/google` is listed before
`google()` in `settings.gradle.kts` because the primary Google endpoint was not
reachable from the build environment. Maven Central supplies Tapsell and PDFBox.

## Local configuration

Create `local.properties` from `local.properties.example`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
tapsell.app.key=YOUR_TAPSELL_KEY
damdast.ads.disabled=false
```

`local.properties` is git-ignored and must never be committed. The app key is read in
`app/build.gradle.kts` and injected as the `TapsellMediationAppKey` manifest
placeholder. Never paste the real key into source, documentation, logs or commits.

Release signing is optional and read from the same ignored file:

```properties
release.storeFile=../release.keystore
release.storePassword=...
release.keyAlias=...
release.keyPassword=...
```

Without these properties, `assembleRelease` produces an unsigned APK. Do not commit
keystores or signing credentials.

## Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew testDebugUnitTest
./gradlew clean
```

Verified in this environment:

- `:app:assembleDebug` — success
  - `app/build/outputs/apk/debug/app-debug.apk` (≈36 MB)
- `:app:assembleRelease` — success (unsigned)
  - `app/build/outputs/apk/release/app-release-unsigned.apk` (≈27 MB)
- `:app:testDebugUnitTest` — success, 19 tests

The SDK XML warning about version 4 from installed platform tools is informational.

## Manual test matrix

Run on at least one API 24–27 device and one API 34+ device:

1. Clean install → onboarding introduction in device language/fallback Persian.
2. Select light/dark → verify immediate colors, status/nav bars, restart persistence.
3. Select فارسی → immediate RTL, Vazirmatn, Persian digits; switch to English → LTR.
4. Complete onboarding → home; kill/relaunch → home, not onboarding.
5. Restart onboarding from Settings → sequence appears; favorites/notes retained.
6. Search, categories, favorites, history, clear history.
7. QR camera permission allow/deny; torch; gallery scan; generated QR/barcode
   save/share/open.
8. PDF viewer search/bookmark; merge/extract/rotate/watermark/page numbers;
   images→PDF; PDF→images; encrypted/corrupt PDF errors.
9. Image crop/resize/rotate/filters/text/shape/save/share/EXIF/collage unlock.
10. File viewers, CSV quoted values, ZIP create/extract into a chosen folder.
11. Activity rotation/recreation on onboarding, tool and PDF screens.
12. Airplane mode: local tools work; ads fail silently; rewarded shows failure and
    grants nothing. (Emulator startup with the current key logged a Tapsell
    `mediationApplicationId` bootstrap error; resolve key/account configuration.)
13. Debug ads toggle hides banner/native/interstitial/rewarded access paths.
14. Tapsell live test for each enabled placement and all callbacks.
15. No-fill/blocked network behavior for every ad format.

## Release checklist

- [ ] Verify versionCode/versionName.
- [ ] Configure signing in ignored local.properties.
- [ ] `./gradlew testDebugUnitTest`
- [ ] `./gradlew :app:assembleRelease`
- [ ] Install signed APK and run manual test matrix.
- [ ] Verify Tapsell placements on the dashboard; resolve instant-banner A/B format.
- [ ] Check privacy policy text and Play/Store data-safety disclosures.
- [ ] Confirm no `local.properties`, keystore, API key or build output is staged.
- [ ] Publish only with explicit owner authorization.

## Git delivery

The repository is `https://github.com/amiraligrr/damdast` (default branch `main`).
Commit normally and push with a fast-forward; never force-push or rewrite history.

```bash
git status --short
git add <intended files>
git commit -m "Build Damdast Android toolbox"
git push origin main
```
