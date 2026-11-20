package ir.amiraligrr.damdast.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.tools.ToolCatalog
import ir.amiraligrr.damdast.ui.components.EmptyState
import ir.amiraligrr.damdast.ui.components.ToolRow

@Composable
fun FavoritesScreen(
    favorites: Set<String>,
    onOpenTool: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onExplore: () -> Unit,
) {
    val favoriteTools = remember(favorites) {
        ToolCatalog.all.filter { it.id in favorites }
    }
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
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        item(key = "title") {
            Column {
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.favorites_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        if (favoriteTools.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = stringResource(R.string.favorites_empty_title),
                    body = stringResource(R.string.favorites_empty_body),
                    actionLabel = stringResource(R.string.home_explore),
                    onAction = onExplore,
                )
            }
        } else {
            items(favoriteTools, key = { it.id }) { tool ->
                ToolRow(
                    title = stringResource(tool.nameRes),
                    description = stringResource(tool.descriptionRes),
                    icon = tool.icon,
                    accent = tool.accent,
                    favorite = true,
                    onClick = { onOpenTool(tool.id) },
                    onToggleFavorite = { onToggleFavorite(tool.id) },
                    premium = tool.premium,
                )
            }
        }
    }
}
