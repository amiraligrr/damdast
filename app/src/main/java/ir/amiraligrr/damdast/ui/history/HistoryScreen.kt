package ir.amiraligrr.damdast.ui.history

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.RecentTool
import ir.amiraligrr.damdast.data.ScanRecord
import ir.amiraligrr.damdast.tools.ToolCatalog
import ir.amiraligrr.damdast.ui.components.EmptyState
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolIcon
import ir.amiraligrr.damdast.ui.components.ToolRow
import ir.amiraligrr.damdast.ui.components.relativeTimeText
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.copyToClipboard
import ir.amiraligrr.damdast.util.formatDateTime
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.openUrl
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    recent: List<RecentTool>,
    scans: List<ScanRecord>,
    favorites: Set<String>,
    onOpenTool: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeleteScan: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val recentTools = remember(recent) {
        recent.mapNotNull { record -> ToolCatalog.byId[record.toolId]?.let { it to record } }
    }
    fun message(text: String) {
        scope.launch { snackbar?.showSnackbar(text) }
    }

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
        item(key = "header") {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.history_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.history_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (recent.isNotEmpty() || scans.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Outlined.DeleteSweep,
                                contentDescription = stringResource(R.string.history_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (recent.isEmpty() && scans.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Outlined.History,
                    title = stringResource(R.string.history_empty_title),
                    body = stringResource(R.string.history_empty_body),
                )
            }
        }

        if (scans.isNotEmpty()) {
            item(key = "scans_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.history_scans),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.scans_count, formatInt(scans.size, language)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(scans, key = { "scan_${it.id}" }) { scan ->
                ScanCard(
                    scan = scan,
                    language = language,
                    onCopy = {
                        copyToClipboard(context, scan.value)
                        message(context.getString(R.string.scan_copied))
                    },
                    onOpen = {
                        if (scan.value.startsWith("http://") || scan.value.startsWith("https://")) {
                            if (!openUrl(context, scan.value)) {
                                message(context.getString(R.string.link_open_failed))
                            }
                        } else {
                            message(context.getString(R.string.scan_no_link))
                        }
                    },
                    onDelete = { onDeleteScan(scan.id) },
                )
            }
        } else if (recent.isNotEmpty()) {
            item(key = "scans_empty") {
                Text(
                    text = stringResource(R.string.history_scans_empty),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        if (recentTools.isNotEmpty()) {
            item(key = "recent_header") {
                Text(
                    text = stringResource(R.string.history_recent_tools),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                )
            }
            items(recentTools, key = { "recent_${it.first.id}" }) { (tool, record) ->
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

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = { Text(stringResource(R.string.history_clear_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                        message(context.getString(R.string.history_cleared))
                    },
                ) {
                    Text(
                        text = stringResource(R.string.history_clear),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun ScanCard(
    scan: ScanRecord,
    language: ir.amiraligrr.damdast.data.AppLanguage,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ToolIcon(icon = Icons.Outlined.QrCode2, accent = MaterialTheme.colorScheme.primary, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = scan.value.ifBlank { stringResource(R.string.scan_value_empty) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${scan.format} · ${formatDateTime(scan.createdAt, language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.common_copy),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onOpen) {
                    Icon(
                        Icons.Outlined.OpenInNew,
                        contentDescription = stringResource(R.string.history_open_scan),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.history_delete_scan),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
