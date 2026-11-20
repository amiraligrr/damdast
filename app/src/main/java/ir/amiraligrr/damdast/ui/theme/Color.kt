package ir.amiraligrr.damdast.ui.theme

import androidx.compose.ui.graphics.Color

object Brand {
    val Purple = Color(0xFF7C5CFF)
    val PurpleDeep = Color(0xFF5B3DF5)
    val PurpleSoft = Color(0xFFA78BFA)
    val Cyan = Color(0xFF22D3EE)
    val Mint = Color(0xFF34D399)
    val Amber = Color(0xFFFBBF24)
    val Rose = Color(0xFFFB7185)
    val Navy = Color(0xFF070B18)
    val NavySurface = Color(0xFF0D1426)
    val NavyElevated = Color(0xFF151E33)
    val OffWhite = Color(0xFFF6F7FB)
    val Ink = Color(0xFF0B1020)

    val categoryAccents = listOf(
        Color(0xFF7C5CFF),
        Color(0xFFEF6C8B),
        Color(0xFF22B8CF),
        Color(0xFFF59F00),
        Color(0xFF20C997),
        Color(0xFF7048E8),
        Color(0xFF4C6EF5),
        Color(0xFFE8590C),
        Color(0xFFD6336C),
        Color(0xFF12B886),
    )
}

val LightColors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF5B3DF5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E2FF),
    onPrimaryContainer = Color(0xFF190064),
    secondary = Color(0xFF6552C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E3FF),
    onSecondaryContainer = Color(0xFF1B0B57),
    tertiary = Color(0xFF0E7490),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFF7FF),
    onTertiaryContainer = Color(0xFF003542),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF0B1020),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEDEFF7),
    onSurfaceVariant = Color(0xFF5B6478),
    surfaceTint = Color(0xFF5B3DF5),
    inverseSurface = Color(0xFF1D2436),
    inverseOnSurface = Color(0xFFF1F3FA),
    outline = Color(0xFFD5D9E6),
    outlineVariant = Color(0xFFE5E8F1),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0xFF000000),
)

val DarkColors = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFA793FF),
    onPrimary = Color(0xFF1B006B),
    primaryContainer = Color(0xFF3A278F),
    onPrimaryContainer = Color(0xFFE8E1FF),
    secondary = Color(0xFFC9BCFF),
    onSecondary = Color(0xFF2A1B62),
    secondaryContainer = Color(0xFF3E3079),
    onSecondaryContainer = Color(0xFFE9E2FF),
    tertiary = Color(0xFF67E8F9),
    onTertiary = Color(0xFF003641),
    tertiaryContainer = Color(0xFF00505F),
    onTertiaryContainer = Color(0xFFB5F4FF),
    background = Color(0xFF070B18),
    onBackground = Color(0xFFF4F6FF),
    surface = Color(0xFF0D1426),
    onSurface = Color(0xFFECEEF9),
    surfaceVariant = Color(0xFF151E33),
    onSurfaceVariant = Color(0xFFA5AEC4),
    surfaceTint = Color(0xFFA793FF),
    inverseSurface = Color(0xFFECEEF9),
    inverseOnSurface = Color(0xFF151B28),
    outline = Color(0xFF2A3650),
    outlineVariant = Color(0xFF1F2940),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
)
