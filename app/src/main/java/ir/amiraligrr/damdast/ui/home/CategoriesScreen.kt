package ir.amiraligrr.damdast.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.tools.ToolCatalog
import ir.amiraligrr.damdast.tools.ToolCategory
import ir.amiraligrr.damdast.ui.components.ToolIcon
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.components.ToolRow
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.formatInt

@Composable
fun CategoriesScreen(
    favorites: Set<String>,
    onOpenTool: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 104.dp + bottomInset,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "categories_header") {
            Column {
                Text(
                    text = stringResource(R.string.categories_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.categories_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                CategoriesSearchPill(onClick = onOpenSearch)
                Spacer(Modifier.height(6.dp))
            }
        }
        ToolCatalog.categoriesWithTools().forEach { category ->
            val tools = ToolCatalog.byCategory(category)
            item(key = "category_${category.name}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    ToolIcon(icon = category.icon, accent = category.accent, size = 32.dp)
                    Text(
                        text = stringResource(category.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(
                            R.string.category_tool_count,
                            formatInt(tools.size, language),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(tools, key = { it.id }) { tool ->
                ToolRow(
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
        item(key = "categories_end") {
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun CategoryDetailScreen(
    category: ToolCategory,
    favorites: Set<String>,
    onOpenTool: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit,
) {
    val tools = remember(category) { ToolCatalog.byCategory(category) }
    ToolPage(title = stringResource(category.titleRes), onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(tools, key = { it.id }) { tool ->
                ToolRow(
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

@Composable
private fun CategoriesSearchPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
