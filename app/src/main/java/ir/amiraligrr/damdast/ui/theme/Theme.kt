package ir.amiraligrr.damdast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.data.AppTheme

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.PERSIAN }
val LocalDarkTheme = staticCompositionLocalOf { true }
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun DamdastTheme(
    theme: AppTheme,
    language: AppLanguage,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }
    val colorScheme = if (dark) DarkColors else LightColors
    val direction = if (language == AppLanguage.PERSIAN) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalDarkTheme provides dark,
        LocalReduceMotion provides reduceMotion,
        LocalLayoutDirection provides direction,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = damdastTypography(language),
            shapes = DamdastShapes,
            content = content,
        )
    }
}

@Composable
fun previewIsDark(): Boolean = isSystemInDarkTheme()
