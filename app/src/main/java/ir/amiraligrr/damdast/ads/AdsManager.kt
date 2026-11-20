package ir.amiraligrr.damdast.ads

import android.app.Activity
import android.widget.FrameLayout
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import ir.tapsell.mediation.ad.views.ntv.NativeAdView
import kotlinx.coroutines.flow.StateFlow

enum class AdsInitState {
    DISABLED,
    INITIALIZING,
    READY,
    UNAVAILABLE,
}

data class NativeAdSlot(
    val container: FrameLayout,
    val nativeAdView: NativeAdView,
)

interface AdsManager {
    val state: StateFlow<AdsInitState>

    fun initialize()

    fun setEnabled(enabled: Boolean)

    fun requestBanner(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    )

    fun showBanner(
        adId: String,
        container: BannerContainer,
        activity: Activity,
        onImpression: () -> Unit = {},
        onFailed: (String) -> Unit = {},
    )

    fun destroyBanner(adId: String)

    fun requestNative(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    )

    fun showNative(
        adId: String,
        slot: NativeAdSlot,
        activity: Activity,
        onImpression: () -> Unit = {},
        onClicked: () -> Unit = {},
        onFailed: (String) -> Unit = {},
    )

    fun destroyNative(adId: String)

    fun requestRewarded(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    )

    fun showRewarded(
        adId: String,
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit,
    )

    fun onToolCompleted(activity: Activity)
    fun maybeShowInterstitial(activity: Activity)
}

class NoOpAdsManager : AdsManager {
    override val state: StateFlow<AdsInitState> = kotlinx.coroutines.flow.MutableStateFlow(
        AdsInitState.DISABLED,
    )
    override fun initialize() = Unit
    override fun setEnabled(enabled: Boolean) = Unit
    override fun requestBanner(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    ) = onFailed("Ads are disabled")

    override fun showBanner(
        adId: String,
        container: BannerContainer,
        activity: Activity,
        onImpression: () -> Unit,
        onFailed: (String) -> Unit,
    ) = onFailed("Ads are disabled")

    override fun destroyBanner(adId: String) = Unit
    override fun requestNative(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    ) = onFailed("Ads are disabled")

    override fun showNative(
        adId: String,
        slot: NativeAdSlot,
        activity: Activity,
        onImpression: () -> Unit,
        onClicked: () -> Unit,
        onFailed: (String) -> Unit,
    ) = onFailed("Ads are disabled")

    override fun destroyNative(adId: String) = Unit
    override fun requestRewarded(
        activity: Activity,
        onLoaded: (String) -> Unit,
        onFailed: (String) -> Unit,
    ) = onFailed("Ads are disabled")

    override fun showRewarded(
        adId: String,
        activity: Activity,
        onRewarded: () -> Unit,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit,
    ) = onFailed("Ads are disabled")

    override fun onToolCompleted(activity: Activity) = Unit
    override fun maybeShowInterstitial(activity: Activity) = Unit
}
