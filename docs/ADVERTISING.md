# Advertising

Damdast uses the Tapsell Mediation Android SDK for its advertising-first business
model. There are no subscriptions or in-app purchases. The app is fully usable when
Tapsell initialization fails, no ad fills, or the device is offline.

## SDK and compatibility

- Dependency: `ir.tapsell:tapsell:1.3.0` (current stable at implementation time)
- Legacy placement adapter: `ir.tapsell.mediation.adapter:legacy:1.3.0`
- The SDK auto-initializes through its ContentProvider; Damdast registers
  `Tapsell.setInitializationListener` and tracks initialization state.
- The AAR classes were inspected before coding to verify method signatures; methods
  used here match the 1.3.0 API (`requestBannerAd`, `showBannerAd`,
  `requestNativeAd`, `showNativeAd`, `requestInterstitialAd`, `showInterstitialAd`,
  `requestRewardedAd`, `showRewardedAd`, and destructors).
- Requirements checked: compileSdk 36, minSdk 24, `INTERNET`,
  `ACCESS_NETWORK_STATE`, `com.google.android.gms.permission.AD_ID`, and the
  `TapsellMediationAppKey` manifest placeholder.

## App key (not committed)

The real key is supplied through the ignored `local.properties`:

```properties
tapsell.app.key=YOUR_KEY_HERE
```

`app/build.gradle.kts` reads it and adds the Tapsell manifest placeholder. It is not
logged, documented, or committed. `local.properties.example` contains only an empty
placeholder. If the key is absent or `damdast.ads.disabled=true`, the app uses
`NoOpAdsManager` and all ads are hidden.

## Placement mapping

| Role | ID | Status | Where |
|---|---|---|---|
| Standard banner | `6aff4d7eaeabeb750feb8f25` | Active | Home content banner slot |
| Native banner | `6aff4de46cbc4169d6c3855e` | Active | Home sponsored card |
| Rewarded video | `6aff4e03728e2256843b0c91` | Active | Premium tool gate and Settings support |
| Instant banner / interstitial candidate A | `6aff4dae6cbc4169d6c3855d` | Active as interstitial | After a safe tool→home transition |
| Instant banner / interstitial candidate B | `6aff4e1e728e2256843b0c92` | **Disabled / reserved** | Not requested |
| Video pre-roll | `6aff4e3c4f21b435f886825d` | **Disabled** | No video playback feature |
| Native video | `6aff4e5b728e2256843b0c93` | **Disabled** | Not requested |

### Unresolved format ambiguity

The two "instant banner" entries have different IDs and similar labels. The actual
dashboard format cannot be verified from the repository or SDK alone. Only candidate
A is used, and only through `requestInterstitialAd`. If the dashboard shows A is not
an interstitial, change `AdPlacementConfig.ACTIVE_INTERSTITIAL` to the verified ID or
disable `INTERSTITIAL_ENABLED`. Do not assume the two IDs are interchangeable.
Candidate B remains reserved so a request can never be sent to an unverified format.

Pre-roll and native video are disabled by config. No fake video feature was added to
justify them.

## Strategy and rules implemented

- **Banner:** home only; fixed 50 dp banner area; hidden on failure/no fill; never
  overlaps content or a camera/editing workspace.
- **Native:** clearly labelled "Sponsored" and visually separated by a card/border;
  required Tapsell native views are registered with `NativeAdView.Builder`
  (`withTitle`, `withDescription`, `withSponsored`, `withCtaButton`, `withLogo`).
- **Interstitial:** only after returning home from a *non-media* tool (text,
  calculators, developer, date, color, everyday). Camera, PDF, image and file tools
  never trigger one. Frequency cap:
  - at least 2 tool sessions between ads,
  - at least 5 minutes between ads,
  - at most 3 per day.
  The count/last-shown/session counters are persisted in DataStore. Failed ad
  requests do not block navigation and do not count as impressions.
- **Rewarded:** only after an explicit button tap and explanation. Access is granted
  only in the SDK `onRewarded()` callback, never on open or close. Reward duration is
  30 minutes, persisted in DataStore. If the ad closes early or fails, no access is
  granted.
- **Pre-roll / native video:** disabled, as there is no real video playback feature.

## Architecture

- `AdPlacementConfig` — IDs and feature flags.
- `AdsManager` — interface + `NoOpAdsManager`.
- `TapsellAdsManager` — Tapsell implementation, one initialization, init timeout,
  lifecycle-aware request/show/destroy methods, failure mapping.
- `AdFrequencyController` — DataStore-backed caps.
- `AdComponents.kt` — Compose wrappers (`BannerAdSlot`, `NativeAdCard`,
  `RewardedUnlockDialog`, `PremiumToolGate`).

Ads are requested asynchronously and only after `AdsInitState.READY`. Request
callbacks update Compose state; no UI thread blocking. Banner/native resources are
destroyed when their composables leave composition. Interstitial requests are
guarded by an atomic flag to avoid duplicates.

## Debug controls

- `damdast.ads.disabled=true` in `local.properties` disables ads at build time.
- In debug builds, Settings → Developer → “Disable ads in this debug build” toggles
  ads at runtime. The switch does not appear in release builds.
- The app never shows "no ad" errors to the user for banners/native/interstitial;
  rewarded failures do show a clear message and award nothing.

## Testing status

The integration compiles in debug and release. A live initialization attempt on an
API 37 emulator produced `lateinit property mediationApplicationId has not been
initialized` from `TapsellInternals` and repeated `ConfigFetchTask` failures. The app
did not crash and ads were simply unavailable. This indicates that the supplied key
or account configuration may not be a valid **Mediation** app ID (it may be a legacy
key), or that the SDK could not bootstrap on that emulator. This must be resolved with
the Tapsell dashboard/key owner before claiming working ads. No test device/dashboard
access was available. Required follow-up testing:

1. Valid Mediation app key and each placement verified in the Tapsell dashboard;
   reproduce or resolve the `mediationApplicationId` initialization error.
2. Initialization callback and no-network/timeout behavior on a real device.
3. Banner show/impression/click destruction across navigation.
4. Native rendering and click tracking.
5. Interstitial frequency persistence and no-fill behavior.
6. Rewarded callback with completed, skipped and failed videos.
7. Candidate A/B format verification.

No app was published to Cafe Bazaar, Myket, Google Play, or any other store.
