package ir.amiraligrr.damdast.ads

import android.app.Activity
import android.util.Log
import ir.amiraligrr.damdast.BuildConfig
import ir.amiraligrr.damdast.ads.AdPlacementConfig.ACTIVE_INTERSTITIAL
import ir.amiraligrr.damdast.ads.AdPlacementConfig.INTERSTITIAL_ENABLED
import ir.amiraligrr.damdast.ads.AdPlacementConfig.NATIVE_BANNER
import ir.amiraligrr.damdast.ads.AdPlacementConfig.NATIVE_ENABLED
import ir.amiraligrr.damdast.ads.AdPlacementConfig.REWARDED_VIDEO
import ir.amiraligrr.damdast.ads.AdPlacementConfig.STANDARD_BANNER
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class TapsellAdsManager(
    private val appContext: android.content.Context,
    private val frequencyController: AdFrequencyController = AdFrequencyController(appContext),
) : AdsManager {
    private val _state = MutableStateFlow(
        if (!BuildConfig.ADS_CONFIGURED || BuildConfig.ADS_DISABLED_BY_CONFIG) {
            AdsInitState.DISABLED
        } else {
            AdsInitState.INITIALIZING
        },
    )
    override val state: StateFlow<AdsInitState> = _state.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val initialized = AtomicBoolean(false)
    private val interstitialRequesting = AtomicBoolean(false)
    @Volatile
    private var enabled: Boolean = BuildConfig.ADS_CONFIGURED && !BuildConfig.ADS_DISABLED_BY_CONFIG

    override fun initialize() {
        if (_state.value == AdsInitState.DISABLED) return
        if (!initialized.compareAndSet(false, true)) return
        scope.launch {
            runCatching {
                Tapsell.setInitializationListener {
                    _state.value = AdsInitState.READY
                }
            }.onFailure {
                _state.value = AdsInitState.UNAVAILABLE
            }
            delay(10_000)
            if (_state.value == AdsInitState.INITIALIZING) {
                _state.value = AdsInitState.UNAVAILABLE
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private fun ready(): Boolean =
        enabled && _state.value == AdsInitState.READY && BuildConfig.ADS_CONFIGURED

    override fun requestBanner(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    ) {
        if (!ready()) {
            onFailed("Ads not ready")
            return
        }
        runCatching {
            Tapsell.requestBannerAd(
                STANDARD_BANNER,
                BannerSize.BANNER_320_50,
                activity,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) = onLoaded(adId)
                    override fun onFailure(message: String) = onFailed(message)
                },
            )
        }.onFailure { onFailed(it.message.orEmpty()) }
    }

    override fun showBanner(
        adId: String,
        container: BannerContainer,
        activity: Activity,
        onImpression: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        runCatching {
            Tapsell.showBannerAd(
                adId,
                container,
                activity,
                object : AdStateListener.Banner {
                    override fun onAdImpression() = onImpression()
                    override fun onAdClicked() = Unit
                    override fun onAdFailed(message: String) = onFailed(message)
                },
            )
        }.onFailure { onFailed(it.message.orEmpty()) }
    }

    override fun destroyBanner(adId: String) {
        runCatching { Tapsell.destroyBannerAd(adId) }
    }

    override fun requestNative(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    ) {
        if (!ready() || !NATIVE_ENABLED) {
            onFailed("Native ads unavailable")
            return
        }
        runCatching {
            Tapsell.requestNativeAd(
                NATIVE_BANNER,
                activity,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) = onLoaded(adId)
                    override fun onFailure(message: String) = onFailed(message)
                },
            )
        }.onFailure { onFailed(it.message.orEmpty()) }
    }

    override fun showNative(
        adId: String,
        slot: NativeAdSlot,
        activity: Activity,
        onImpression: () -> Unit,
        onClicked: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        runCatching {
            Tapsell.showNativeAd(
                adId,
                slot.nativeAdView,
                activity,
                object : AdStateListener.Native {
                    override fun onAdImpression() = onImpression()
                    override fun onAdClicked() = onClicked()
                    override fun onAdClosed(completionState: AdShowCompletionState) = Unit
                    override fun onAdFailed(message: String) = onFailed(message)
                },
            )
        }.onFailure { onFailed(it.message.orEmpty()) }
    }

    override fun destroyNative(adId: String) {
        runCatching { Tapsell.destroyNativeAd(adId) }
    }

    override fun requestRewarded(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    ) {
        if (!ready()) {
            onFailed("Rewarded ads unavailable")
            return
        }
        runCatching {
            Tapsell.requestRewardedAd(
                REWARDED_VIDEO,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) = onLoaded(adId)
                    override fun onFailure(message: String) = onFailed(message)
                },
            )
        }.onFailure { onFailed(it.message.orEmpty()) }
    }

    override fun showRewarded(
        adId: String,
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        var rewardGranted = false
        runCatching {
            Tapsell.showRewardedAd(
                adId,
                activity,
                object : AdStateListener.Rewarded {
                    override fun onAdImpression() = Unit
                    override fun onAdClicked() = Unit
                    override fun onAdClosed(completionState: AdShowCompletionState) = onClosed()
                    override fun onRewarded() {
                        rewardGranted = true
                        onRewarded()
                    }

                    override fun onAdFailed(message: String) = onFailed(message)
                },
            )
        }.onFailure {
            if (!rewardGranted) onFailed(it.message.orEmpty())
        }
    }

    override fun onToolCompleted(activity: Activity) {
        scope.launch {
            frequencyController.noteToolCompletion()
            maybeShowInterstitial(activity)
        }
    }

    override fun maybeShowInterstitial(activity: Activity) {
        if (!INTERSTITIAL_ENABLED || !ready()) return
        if (!interstitialRequesting.compareAndSet(false, true)) return
        scope.launch {
            if (!frequencyController.canShow()) {
                interstitialRequesting.set(false)
                return@launch
            }
            runCatching {
                Tapsell.requestInterstitialAd(
                    ACTIVE_INTERSTITIAL,
                    object : RequestResultListener {
                        override fun onSuccess(adId: String) {
                            if (activity.isFinishing || activity.isDestroyed) {
                                interstitialRequesting.set(false)
                                return
                            }
                            Tapsell.showInterstitialAd(
                                adId,
                                activity,
                                object : AdStateListener.Interstitial {
                                    override fun onAdImpression() {
                                        scope.launch { frequencyController.recordShown() }
                                    }

                                    override fun onAdClicked() = Unit
                                    override fun onAdClosed(completionState: AdShowCompletionState) {
                                        interstitialRequesting.set(false)
                                    }

                                    override fun onAdFailed(message: String) {
                                        interstitialRequesting.set(false)
                                        logFailure(message)
                                    }
                                },
                            )
                        }

                        override fun onFailure(message: String) {
                            interstitialRequesting.set(false)
                            logFailure(message)
                        }
                    },
                )
            }.onFailure {
                interstitialRequesting.set(false)
                logFailure(it.message.orEmpty())
            }
        }
    }

    private fun logFailure(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("DamdastAds", "Ad request did not fill: ${message.take(160)}")
        }
    }
}
