package ir.amiraligrr.damdast.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.data.AppSettings
import ir.amiraligrr.damdast.data.AppTheme
import ir.amiraligrr.damdast.ui.components.AnimatedSelectionScale
import ir.amiraligrr.damdast.ui.components.BrandMark
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.GlowBackdrop
import ir.amiraligrr.damdast.ui.theme.Brand
import ir.amiraligrr.damdast.ui.theme.DarkColors
import ir.amiraligrr.damdast.ui.theme.LightColors
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.ui.theme.LocalReduceMotion
import ir.amiraligrr.damdast.ui.theme.Motion
import ir.amiraligrr.damdast.ui.theme.damdastTypography
import ir.amiraligrr.damdast.util.localizeDigits

@Composable
fun OnboardingFlow(
    settings: AppSettings,
    onThemeSelected: (AppTheme) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onComplete: () -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    val reduceMotion = LocalReduceMotion.current
    BackHandler(enabled = step > 1) { step-- }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        GlowBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandMark(size = 36.dp, showWordmark = true)
                Spacer(Modifier.weight(1f))
                StepDots(current = step, total = 3)
            }
            AnimatedContent(
                targetState = step,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    if (reduceMotion) {
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else {
                        (
                            fadeIn(tween(Motion.Medium, easing = FastOutSlowInEasing)) +
                                slideInVertically(
                                    tween(Motion.Medium, easing = FastOutSlowInEasing),
                                    initialOffsetY = { it / 18 },
                                )
                            ) togetherWith (
                            fadeOut(tween(Motion.Fast)) +
                                slideOutVertically(
                                    tween(Motion.Fast),
                                    targetOffsetY = { -it / 22 },
                                )
                            )
                    }
                },
                label = "onboarding-step",
            ) { currentStep ->
                when (currentStep) {
                    1 -> IntroStep(
                        onContinue = { step = 2 },
                    )

                    2 -> ThemeStep(
                        selected = settings.theme,
                        onSelect = onThemeSelected,
                        onContinue = { step = 3 },
                    )

                    else -> LanguageStep(
                        selected = settings.language,
                        onSelect = onLanguageSelected,
                        onContinue = onComplete,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val active = index + 1 == current
            val width by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (active) 22.dp else 7.dp,
                animationSpec = tween(Motion.Medium),
                label = "step-dot-width",
            )
            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 8.dp),
        content = content,
    )
}

@Composable
private fun IntroStep(onContinue: () -> Unit) {
    OnboardingPage {
        HeroIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(258.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.onboarding_intro_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_intro_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        FeatureGrid()
        Spacer(Modifier.height(18.dp))
        TaglineCard()
        Spacer(Modifier.height(18.dp))
        DamdastButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FeatureGrid() {
    val features = listOf(
        Icons.Outlined.QrCode2 to stringResource(R.string.onboarding_feature_qr),
        Icons.Outlined.PictureAsPdf to stringResource(R.string.onboarding_feature_pdf),
        Icons.Outlined.Image to stringResource(R.string.onboarding_feature_image),
        Icons.Outlined.TextFields to stringResource(R.string.onboarding_feature_text),
        Icons.Outlined.Calculate to stringResource(R.string.onboarding_feature_calc),
        Icons.Outlined.Lock to stringResource(R.string.onboarding_feature_everyday),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        features.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (icon, text) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TaglineCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ),
                ),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Column {
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.onboarding_privacy_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HeroIllustration(modifier: Modifier = Modifier) {
    val reduceMotion = LocalReduceMotion.current
    val transition = rememberInfiniteTransition(label = "hero")
    val float by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero-float",
    )
    val float2 by transition.animateFloat(
        initialValue = 5f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero-float-2",
    )
    val angle by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero-angle",
    )
    val motion = if (reduceMotion) 0f else 1f

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                MaterialTheme.shapes.extraLarge,
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Brand.Purple.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.62f, size.height * 0.42f),
                    radius = size.width * 0.48f,
                ),
                radius = size.width * 0.48f,
                center = Offset(size.width * 0.62f, size.height * 0.42f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Brand.Cyan.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.18f, size.height * 0.72f),
                    radius = size.width * 0.35f,
                ),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.18f, size.height * 0.72f),
            )
            val step = 26.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.035f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f,
                )
                x += step
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (float * motion).dp)
                .size(104.dp)
                .rotate(angle * 0.4f * motion)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Brand.Purple, Brand.PurpleDeep, Color(0xFF3B1FB8)),
                    ),
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.25f),
                    RoundedCornerShape(30.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            BrandMark(size = 66.dp)
        }

        FloatingToolChip(
            icon = Icons.Outlined.QrCode2,
            color = Brand.PurpleSoft,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = (42 + float * motion).dp),
            rotate = -6f,
        )
        FloatingToolChip(
            icon = Icons.Outlined.PictureAsPdf,
            color = Brand.Rose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-18).dp, y = (32 + float2 * motion).dp),
            rotate = 7f,
        )
        FloatingToolChip(
            icon = Icons.Outlined.Calculate,
            color = Brand.Amber,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 34.dp, y = (-38 + float2 * motion).dp),
            rotate = -4f,
        )
        FloatingToolChip(
            icon = Icons.Outlined.Code,
            color = Brand.Cyan,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-28).dp, y = (-46 + float * motion).dp),
            rotate = 5f,
        )
        FloatingToolChip(
            icon = Icons.Outlined.Image,
            color = Brand.Mint,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 12.dp, y = 2.dp),
            rotate = -3f,
            small = true,
        )
        FloatingToolChip(
            icon = Icons.Outlined.TextFields,
            color = Brand.PurpleSoft,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 6.dp),
            rotate = 4f,
            small = true,
        )
    }
}

@Composable
private fun FloatingToolChip(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    rotate: Float = 0f,
    small: Boolean = false,
) {
    val size: Dp = if (small) 38.dp else 48.dp
    Box(
        modifier = modifier
            .size(size)
            .rotate(rotate)
            .clip(RoundedCornerShape(if (small) 12.dp else 15.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                RoundedCornerShape(if (small) 12.dp else 15.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(if (small) 19.dp else 23.dp),
        )
    }
}

@Composable
private fun ThemeStep(
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingPage {
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_theme_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_theme_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemePreviewCard(
                theme = AppTheme.LIGHT,
                selected = selected == AppTheme.LIGHT,
                onClick = { onSelect(AppTheme.LIGHT) },
                modifier = Modifier.weight(1f),
            )
            ThemePreviewCard(
                theme = AppTheme.DARK,
                selected = selected == AppTheme.DARK,
                onClick = { onSelect(AppTheme.DARK) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        ThemeDescription(
            title = stringResource(R.string.theme_light),
            description = stringResource(R.string.theme_light_desc),
            selected = selected == AppTheme.LIGHT,
            onClick = { onSelect(AppTheme.LIGHT) },
        )
        Spacer(Modifier.height(8.dp))
        ThemeDescription(
            title = stringResource(R.string.theme_dark),
            description = stringResource(R.string.theme_dark_desc),
            selected = selected == AppTheme.DARK,
            onClick = { onSelect(AppTheme.DARK) },
        )
        Spacer(Modifier.height(24.dp))
        DamdastButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ThemePreviewCard(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = if (theme == AppTheme.DARK) DarkColors else LightColors
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(Motion.Fast),
        label = "theme-border",
    )
    AnimatedSelectionScale(selected = selected, modifier = modifier) {
        Box(            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = MaterialTheme.shapes.large,
                )
                .padding(6.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colors.background,
                border = BorderStroke(1.dp, colors.outlineVariant),
            ) {
                MaterialTheme(colorScheme = colors, typography = damdastTypography(LocalAppLanguage.current)) {
                    MiniAppPreview(theme = theme)
                }
            }
            AnimatedVisibility(
                visible = selected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniAppPreview(theme: AppTheme) {
    val colors = if (theme == AppTheme.DARK) DarkColors else LightColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brand.Purple),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.brand_name),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.theme_preview_title),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.theme_preview_subtitle),
            fontSize = 8.sp,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, CircleShape)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.onSurfaceVariant.copy(alpha = 0.45f)),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.theme_preview_search),
                fontSize = 7.sp,
                color = colors.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        MiniToolRow(
            label = stringResource(R.string.theme_preview_qr),
            color = Brand.Purple,
            colors = colors,
        )
        Spacer(Modifier.height(6.dp))
        MiniToolRow(
            label = stringResource(R.string.theme_preview_pdf),
            color = Brand.Rose,
            colors = colors,
        )
        Spacer(Modifier.height(6.dp))
        MiniToolRow(
            label = stringResource(R.string.theme_preview_notes),
            color = Brand.Amber,
            colors = colors,
        )
        Spacer(Modifier.height(11.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(if (index == 0) 13.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) colors.primary else colors.outlineVariant),
                )
            }
        }
    }
}

@Composable
private fun MiniToolRow(label: String, color: Color, colors: androidx.compose.material3.ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
        Text(
            text = label,
            fontSize = 7.sp,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun ThemeDescription(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "theme-desc-border",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .border(1.dp, border, MaterialTheme.shapes.medium)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SelectionDot(selected)
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .border(
                1.5.dp,
                if (selected) MaterialTheme.colorScheme.primary else {
                    MaterialTheme.colorScheme.outline
                },
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun LanguageStep(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingPage {
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_language_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_language_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(22.dp))
        LanguageCard(
            language = AppLanguage.PERSIAN,
            nativeName = stringResource(R.string.language_persian),
            description = stringResource(R.string.language_persian_desc),
            sample = stringResource(R.string.language_sample_persian),
            selected = selected == AppLanguage.PERSIAN,
            onClick = { onSelect(AppLanguage.PERSIAN) },
        )
        Spacer(Modifier.height(10.dp))
        LanguageCard(
            language = AppLanguage.ENGLISH,
            nativeName = stringResource(R.string.language_english),
            description = stringResource(R.string.language_english_desc),
            sample = stringResource(R.string.language_sample_english),
            selected = selected == AppLanguage.ENGLISH,
            onClick = { onSelect(AppLanguage.ENGLISH) },
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.onboarding_language_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
        DamdastButton(
            text = stringResource(R.string.onboarding_get_started),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LanguageCard(
    language: AppLanguage,
    nativeName: String,
    description: String,
    sample: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "language-border",
    )
    val typography = damdastTypography(language)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = border,
                shape = MaterialTheme.shapes.large,
            )
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                },
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (language == AppLanguage.PERSIAN) "ف" else "A",
                style = typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = nativeName,
                style = typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = sample,
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SelectionDot(selected)
    }
}

@Composable
private fun LanguagePreviewBadge(text: String) {
    DamdastButton(text = text, onClick = {}, style = DamdastButtonStyle.Ghost)
}
