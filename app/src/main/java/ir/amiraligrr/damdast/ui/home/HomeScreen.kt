package ir.amiraligrr.damdast.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.data.RecentTool
import ir.amiraligrr.damdast.tools.ToolCatalog
import ir.amiraligrr.damdast.tools.ToolCategory
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.BrandMark
import ir.amiraligrr.damdast.ui.components.SectionHeader
import ir.amiraligrr.damdast.ui.components.ToolCardWide
import ir.amiraligrr.damdast.ui.components.ToolIcon
import ir.amiraligrr.damdast.ui.components.ToolRow
import ir.amiraligrr.damdast.ui.components.relativeTimeText
import ir.amiraligrr.damdast.ui.theme.Brand
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.ui.theme.LocalReduceMotion
import ir.amiraligrr.damdast.ui.theme.Motion
import ir.amiraligrr.damdast.util.formatDateNumeric
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.weekdayName
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    favorites: Set<String>,
    recent: List<RecentTool>,
    onOpenTool: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenCategory: (ToolCategory) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    nativeAdSlot: (@Composable () -> Unit)? = null,
    bannerSlot: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val favoriteTools = remember(favorites) {
        ToolCatalog.all.filter { it.id in favorites }
    }
    val recentTools = remember(recent) {
        recent.mapNotNull { record -> ToolCatalog.byId[record.toolId]?.let { it to record } }
    }
    val recommended = remember {
        ToolCatalog.recommended.ifEmpty { ToolCatalog.all.take(4) }
    }
    val categories = remember { ToolCatalog.categoriesWithTools() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 10.dp,
            bottom = 104.dp + bottomInset,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") {
            HomeHero(
                language = language,
                onSearch = onOpenSearch,
                onSettings = onOpenSettings,
            )
        }

        item(key = "favorites_header") {
            RevealSection(index = 1) {
                SectionHeader(
                    title = stringResource(R.string.home_favorites),
                    actionLabel = if (favoriteTools.isNotEmpty()) stringResource(R.string.home_see_all) else null,
                    onAction = if (favoriteTools.isNotEmpty()) onOpenFavorites else null,
                )
            }
        }
        item(key = "favorites_content") {
            RevealSection(index = 2) {
                if (favoriteTools.isEmpty()) {
                    HomeHintCard(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = stringResource(R.string.home_empty_favorites_title),
                        body = stringResource(R.string.home_empty_favorites_body),
                        actionLabel = stringResource(R.string.home_explore),
                        onAction = onOpenCategories,
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp),
                    ) {
                        items(favoriteTools, key = { it.id }) { tool ->
                            ToolCardWide(
                                title = stringResource(tool.nameRes),
                                description = stringResource(tool.descriptionRes),
                                icon = tool.icon,
                                accent = tool.accent,
                                favorite = tool.id in favorites,
                                onClick = { onOpenTool(tool.id) },
                                onToggleFavorite = { onToggleFavorite(tool.id) },
                                premium = tool.premium,
                            )
                        }
                    }
                }
            }
        }

        if (recommended.isNotEmpty()) {
            item(key = "recommended_header") {
                RevealSection(index = 3) {
                    SectionHeader(
                        title = stringResource(R.string.home_recommended),
                        actionLabel = stringResource(R.string.home_see_all),
                        onAction = onOpenCategories,
                    )
                }
            }
            item(key = "recommended_content") {
                RevealSection(index = 4) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp),
                    ) {
                        items(recommended, key = { it.id }) { tool ->
                            ToolCardWide(
                                title = stringResource(tool.nameRes),
                                description = stringResource(tool.descriptionRes),
                                icon = tool.icon,
                                accent = tool.accent,
                                favorite = tool.id in favorites,
                                onClick = { onOpenTool(tool.id) },
                                onToggleFavorite = { onToggleFavorite(tool.id) },
                                premium = tool.premium,
                            )
                        }
                    }
                }
            }
        }

        item(key = "categories_header") {
            RevealSection(index = 5) {
                SectionHeader(
                    title = stringResource(R.string.home_categories),
                    actionLabel = stringResource(R.string.home_see_all),
                    onAction = onOpenCategories,
                )
            }
        }
        item(key = "categories_content") {
            RevealSection(index = 6) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.chunked(2).forEach { rowCategories ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowCategories.forEach { category ->
                                CategoryCard(
                                    category = category,
                                    count = ToolCatalog.byCategory(category).size,
                                    language = language,
                                    onClick = { onOpenCategory(category) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (nativeAdSlot != null) {
            item(key = "native_ad") {
                nativeAdSlot()
            }
        }

        item(key = "recent_header") {
            RevealSection(index = 7) {
                SectionHeader(
                    title = stringResource(R.string.home_recent),
                    actionLabel = if (recentTools.isNotEmpty()) stringResource(R.string.home_see_all) else null,
                    onAction = if (recentTools.isNotEmpty()) onOpenHistory else null,
                )
            }
        }
        item(key = "recent_content") {
            RevealSection(index = 8) {
                if (recentTools.isEmpty()) {
                    HomeHintCard(
                        icon = Icons.Outlined.History,
                        title = stringResource(R.string.home_empty_recent_title),
                        body = stringResource(R.string.home_empty_recent_body),
                        actionLabel = stringResource(R.string.home_explore),
                        onAction = onOpenCategories,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        recentTools.take(3).forEach { (tool, record) ->
                            ToolRow(
                                title = stringResource(tool.nameRes),
                                description = relativeTimeText(record.lastUsed),
                                icon = tool.icon,
                                accent = tool.accent,
                                favorite = tool.id in favorites,
                                onClick = { onOpenTool(tool.id) },
                                onToggleFavorite = { onToggleFavorite(tool.id) },
                                premium = tool.premium,
                            )
                        }
                    }
                }
            }
        }

        if (bannerSlot != null) {
            item(key = "banner_ad") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    bannerSlot()
                }
            }
        }

        item(key = "footer") {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RevealSection(index: Int, content: @Composable () -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (reduceMotion) {
            visible = true
        } else {
            delay((index * 35L).coerceAtMost(240L))
            visible = true
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.Medium)) + slideInVertically(
            animationSpec = tween(Motion.Medium),
            initialOffsetY = { it / 12 },
        ),
    ) {
        content()
    }
}

@Composable
private fun HomeHero(
    language: AppLanguage,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
) {
    val now = System.currentTimeMillis()
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = stringResource(
        when {
            hour < 12 -> R.string.home_greeting_morning
            hour < 18 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        },
    )
    val date = "${weekdayName(now, language)} · ${formatDateNumeric(now, language)}"
    val searchHint = stringResource(R.string.home_search_hint)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        Brand.PurpleDeep,
                        Color(0xFF2C167D),
                    ),
                ),
            ),
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.width * 0.34f,
                center = Offset(size.width * 0.98f, size.height * 0.06f),
            )
            drawCircle(
                color = Brand.Cyan.copy(alpha = 0.10f),
                radius = size.width * 0.22f,
                center = Offset(size.width * 0.08f, size.height * 0.9f),
            )
            val step = 30.dp.toPx()
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
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = 34.dp)
                Spacer(Modifier.width(9.dp))
                Text(
                    text = stringResource(R.string.brand_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.13f)),
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.nav_settings),
                        tint = Color.White,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.13f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.88f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = greeting,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.75f),
            )
            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = onSearch,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = searchHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: ToolCategory,
    count: Int,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(104.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToolIcon(icon = category.icon, accent = category.accent, size = 34.dp)
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(15.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(category.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.category_tool_count,
                        formatInt(count, language),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HomeHintCard(
    icon: ImageVector,
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ToolIcon(icon = icon, accent = MaterialTheme.colorScheme.primary, size = 38.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            )
        }
    }
}
