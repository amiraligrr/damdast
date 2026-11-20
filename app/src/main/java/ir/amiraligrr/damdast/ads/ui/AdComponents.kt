package ir.amiraligrr.damdast.ads.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.ads.AdsInitState
import ir.amiraligrr.damdast.ads.AdsManager
import ir.amiraligrr.damdast.ads.NativeAdSlot
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import ir.tapsell.mediation.ad.views.ntv.NativeAdView
import ir.tapsell.mediation.ad.views.ntv.NativeAdViewContainer

@Composable
fun BannerAdSlot(
    adsManager: AdsManager,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val state by adsManager.state.collectAsState()
    val container = remember(activity) { BannerContainer(activity) }
    var adId by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var requesting by remember { mutableStateOf(false) }
    val canLoad = enabled && state == AdsInitState.READY

    LaunchedEffect(canLoad) {
        if (!canLoad) {
            if (!enabled) {
                adId?.let(adsManager::destroyBanner)
                adId = null
                loaded = false
            }
            return@LaunchedEffect
        }
        if (adId == null && !requesting && !failed) {
            requesting = true
            adsManager.requestBanner(
                activity = activity,
                onLoaded = { id ->
                    requesting = false
                    adId = id
                    adsManager.showBanner(
                        adId = id,
                        container = container,
                        activity = activity,
                        onImpression = { loaded = true },
                        onFailed = {
                            failed = true
                            loaded = false
                        },
                    )
                },
                onFailed = {
                    requesting = false
                    failed = true
                },
            )
        }
    }
    DisposableEffect(adId) {
        onDispose { adId?.let(adsManager::destroyBanner) }
    }

    AnimatedVisibility(
        visible = enabled && !failed && (loaded || requesting),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.common_advertisement),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(
                    factory = { container },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!loaded) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
fun NativeAdCard(
    adsManager: AdsManager,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val state by adsManager.state.collectAsState()
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val titleColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val accentColor = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimary = MaterialTheme.colorScheme.onPrimary.toArgb()
    val sponsoredHint = context.getString(R.string.native_sponsored_label)
    val slot = remember(activity, dark, titleColor, bodyColor, accentColor, onPrimary, sponsoredHint) {
        createNativeAdSlot(
            activity = activity,
            dark = dark,
            titleColor = titleColor,
            bodyColor = bodyColor,
            accentColor = accentColor,
            onPrimary = onPrimary,
            sponsoredHint = sponsoredHint,
        )
    }
    var adId by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var requesting by remember { mutableStateOf(false) }
    val canLoad = enabled && state == AdsInitState.READY

    LaunchedEffect(canLoad) {
        if (!canLoad) return@LaunchedEffect
        if (adId == null && !requesting && !failed) {
            requesting = true
            adsManager.requestNative(
                activity = activity,
                onLoaded = { id ->
                    requesting = false
                    adId = id
                    loaded = true
                    adsManager.showNative(
                        adId = id,
                        slot = slot,
                        activity = activity,
                        onFailed = {
                            failed = true
                            loaded = false
                        },
                    )
                },
                onFailed = {
                    requesting = false
                    failed = true
                },
            )
        }
    }
    DisposableEffect(adId) {
        onDispose { adId?.let(adsManager::destroyNative) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (loaded) Modifier else Modifier.height(0.dp)),
    ) {
        if (loaded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.native_sponsored_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.common_advertisement),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            AndroidView(
                factory = { slot.container },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun RewardedUnlockDialog(
    adsManager: AdsManager,
    onDismiss: () -> Unit,
    onRewarded: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    var loading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var rewardEarned by remember { mutableStateOf(false) }
    val errorClosed = stringResource(R.string.rewarded_closed_early)
    val errorFailed = stringResource(R.string.rewarded_failed)

    fun startAd() {
        loading = true
        showError = null
        rewardEarned = false
        adsManager.requestRewarded(
            activity = activity,
            onLoaded = { adId ->
                loading = false
                adsManager.showRewarded(
                    adId = adId,
                    activity = activity,
                    onRewarded = {
                        rewardEarned = true
                        onRewarded()
                    },
                    onClosed = {
                        if (rewardEarned) {
                            onDismiss()
                        } else {
                            loading = false
                            showError = errorClosed
                        }
                    },
                    onFailed = {
                        loading = false
                        showError = errorFailed
                    },
                )
            },
            onFailed = {
                loading = false
                showError = errorFailed
            },
        )
    }

    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.rewarded_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.rewarded_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (loading) {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.rewarded_loading),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                showError?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(18.dp))
                DamdastButton(
                    text = stringResource(R.string.rewarded_dialog_watch),
                    onClick = ::startAd,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                DamdastButton(
                    text = stringResource(R.string.rewarded_dialog_not_now),
                    onClick = onDismiss,
                    style = DamdastButtonStyle.Ghost,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun PremiumToolGate(
    tool: ToolDefinition,
    adsManager: AdsManager,
    adsEnabled: Boolean,
    onRewarded: () -> Unit,
    onBack: () -> Unit,
) {
    val state by adsManager.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.premium_gate_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(tool.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.premium_gate_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            when {
                !adsEnabled || state == AdsInitState.DISABLED -> Text(
                    text = stringResource(R.string.ads_not_configured),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> DamdastButton(
                    text = stringResource(R.string.premium_gate_unlock),
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    if (showDialog) {
        RewardedUnlockDialog(
            adsManager = adsManager,
            onDismiss = { showDialog = false },
            onRewarded = {
                onRewarded()
                showDialog = false
            },
        )
    }
}

private fun createNativeAdSlot(
    activity: Activity,
    dark: Boolean,
    titleColor: Int,
    bodyColor: Int,
    accentColor: Int,
    onPrimary: Int,
    sponsoredHint: String,
): NativeAdSlot {
    val density = activity.resources.displayMetrics.density
    fun dp(value: Int): Int = (value * density).toInt()
    val container = NativeAdViewContainer(activity).apply {
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }
    val row = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(12), dp(10), dp(12), dp(10))
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
    val logo = ImageView(activity).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply {
            marginEnd = dp(10)
        }
    }
    val texts = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }
    val sponsored = TextView(activity).apply {
        text = sponsoredHint
        setTextColor(bodyColor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
        maxLines = 1
    }
    val title = TextView(activity).apply {
        setTextColor(titleColor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        setTypeface(typeface, Typeface.BOLD)
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    val description = TextView(activity).apply {
        setTextColor(bodyColor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    texts.addView(sponsored)
    texts.addView(title)
    texts.addView(description)
    val cta = Button(activity).apply {
        setTextColor(onPrimary)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        isAllCaps = false
        minHeight = 0
        minimumHeight = 0
        setPadding(dp(12), dp(4), dp(12), dp(4))
        backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(36),
        )
    }
    row.addView(logo)
    row.addView(texts)
    row.addView(cta)
    container.addView(row)
    val nativeAdView = NativeAdView.Builder(container)
        .withLogo(logo)
        .withTitle(title)
        .withDescription(description)
        .withSponsored(sponsored)
        .withCtaButton(cta)
        .build()
    return NativeAdSlot(container, nativeAdView)
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
