package ir.amiraligrr.damdast

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.data.AppSettings
import ir.amiraligrr.damdast.data.DamdastViewModel
import ir.amiraligrr.damdast.ui.MainShell
import ir.amiraligrr.damdast.ui.components.BrandMark
import ir.amiraligrr.damdast.ui.onboarding.OnboardingFlow
import ir.amiraligrr.damdast.ui.theme.Brand
import ir.amiraligrr.damdast.ui.theme.DamdastTheme
import ir.amiraligrr.damdast.ui.theme.LocalDarkTheme
import ir.amiraligrr.damdast.ui.theme.rememberReduceMotion

@Composable
fun DamdastRoot(
    viewModel: DamdastViewModel,
    shortcutTarget: String?,
    onShortcutConsumed: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val reduceMotion = rememberReduceMotion()
    val current = settings

    LaunchedEffect(current?.language) {
        val language = current?.language ?: return@LaunchedEffect
        val stored = AppCompatDelegate.getApplicationLocales()
        if (stored.isEmpty || stored.toLanguageTags().substringBefore(',').substringBefore('-') != language.tag) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.tag),
            )
        }
    }

    DamdastTheme(
        theme = current?.theme ?: ir.amiraligrr.damdast.data.AppTheme.DARK,
        language = current?.language ?: AppLanguage.PERSIAN,
        reduceMotion = reduceMotion,
    ) {
        SystemBars()
        when {
            current == null -> BrandSplash()
            !current.onboardingCompleted -> OnboardingFlow(
                settings = current,
                onThemeSelected = viewModel::setTheme,
                onLanguageSelected = viewModel::setLanguage,
                onComplete = viewModel::completeOnboarding,
            )

            else -> MainShell(
                viewModel = viewModel,
                settings = current,
                shortcutTarget = shortcutTarget,
                onShortcutConsumed = onShortcutConsumed,
            )
        }
    }
}

@Composable
private fun SystemBars() {
    val view = LocalView.current
    val dark = LocalDarkTheme.current
    val context = LocalContext.current
    SideEffect {
        val window = context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

@Composable
private fun BrandSplash() {
    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splash-scale",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BrandMark(
                modifier = Modifier.scale(scale),
                size = 84.dp,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.brand_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = Brand.Purple,
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
