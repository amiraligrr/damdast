package ir.amiraligrr.damdast.ui.tools

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.CodeFormatter
import ir.amiraligrr.damdast.util.CsvUtils
import ir.amiraligrr.damdast.util.JsonUtils
import ir.amiraligrr.damdast.util.formatBytes
import ir.amiraligrr.damdast.util.formatDateTime
import ir.amiraligrr.damdast.util.formatInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Composable
fun FileToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
    when (tool.id) {
        "zip_create" -> ZipCreateTool(onBack)
        "zip_extract" -> ZipExtractTool(onBack)
        else -> FileReaderTool(tool, onBack)
    }
}

private fun mimeFor(toolId: String): String = when (toolId) {
    "txt_viewer" -> "text/*"
    "csv_viewer" -> "text/*"
    "json_viewer" -> "application/json"
    "xml_viewer" -> "text/*"
    "markdown_viewer" -> "text/*"
    else -> "*/*"
}

@Composable
private fun FileReaderTool(tool: ToolDefinition, onBack: () -> Unit) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var size by remember { mutableStateOf(0L) }
    var mime by remember { mutableStateOf("") }
    var modified by remember { mutableStateOf(0L) }
    var text by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selected ->
        uri = selected
    }
    LaunchedEffect(uri) {
        val selected = uri ?: return@LaunchedEffect
        loading = true
        error = null
        text = ""
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.query(selected, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex >= 0) name = cursor.getString(nameIndex)
                        if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
                    }
                }
                mime = context.contentResolver.getType(selected).orEmpty()
                modified = runCatching { DocumentFile.fromSingleUri(context, selected)?.lastModified() ?: 0L }
                    .getOrDefault(0L)
                if (tool.id != "file_info") {
                    require(size <= 5L * 1024 * 1024) { "too_large" }
                    context.contentResolver.openInputStream(selected)?.use { input ->
                        text = input.readBytes().toString(Charsets.UTF_8)
                    }
                }
            }.onFailure {
                error = if (it.message == "too_large") {
                    context.getString(R.string.file_too_large)
                } else {
                    context.getString(R.string.error_unreadable_file)
                }
            }
        }
        loading = false
    }
    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(tool.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            DamdastButton(
                text = stringResource(R.string.file_choose),
                onClick = { picker.launch(mimeFor(tool.id)) },
                style = DamdastButtonStyle.Outline,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            when {
                loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.file_reading))
                }

                error != null -> Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )

                uri == null -> Text(
                    text = stringResource(R.string.image_no_image),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                tool.id == "file_info" -> {
                    KeyValues(
                        listOf(
                            stringResource(R.string.file_name) to name.ifBlank { "—" },
                            stringResource(R.string.file_size) to formatBytes(size, language),
                            stringResource(R.string.file_type) to mime.ifBlank { "—" },
                            stringResource(R.string.file_modified) to if (modified > 0) {
                                formatDateTime(modified, language)
                            } else {
                                "—"
                            },
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.file_info_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                tool.id == "csv_viewer" -> CsvPreview(text)
                tool.id == "json_viewer" -> {
                    val pretty = remember(text) { runCatching { JsonUtils.pretty(text) } }
                    if (pretty.isFailure) {
                        Text(
                            text = stringResource(R.string.dev_invalid_json),
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        TextCard(pretty.getOrThrow(), monospace = true)
                    }
                }

                tool.id == "xml_viewer" -> TextCard(
                    remember(text) { CodeFormatter.markup(text) },
                    monospace = true,
                )

                tool.id == "markdown_viewer" -> MarkdownPreview(text)
                else -> TextCard(text)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TextCard(text: String, monospace: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        SelectionContainer {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun CsvPreview(text: String) {
    val language = LocalAppLanguage.current
    val rows = remember(text) { CsvUtils.parse(text, maxRows = 100) }
    if (rows.isEmpty()) {
        Text(
            text = stringResource(R.string.error_unreadable_file),
            color = MaterialTheme.colorScheme.error,
        )
        return
    }
    val limited = rows.take(100)
    Text(
        text = stringResource(R.string.file_preview_limited, formatInt(limited.size, language)),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(6.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
        ) {
            Column {
                limited.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (index == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else {
                                    Color.Transparent
                                },
                            ),
                    ) {
                        row.take(24).forEach { cell ->
                            Text(
                                text = cell.ifBlank { " " },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .width(130.dp)
                                    .padding(6.dp),
                            )
                        }
                    }
                    if (index == 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownPreview(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            var inCode = false
            text.lines().forEach { line ->
                when {
                    line.trimStart().startsWith("```") -> inCode = !inCode
                    inCode -> Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )

                    line.startsWith("# ") -> Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                    )

                    line.startsWith("## ") -> Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                    )

                    line.startsWith("### ") -> Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    line.startsWith("- ") || line.startsWith("* ") -> Row {
                        Text("•", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(line.drop(2), style = MaterialTheme.typography.bodyMedium)
                    }

                    line.isBlank() -> Spacer(Modifier.height(6.dp))
                    else -> Text(line, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.file_markdown_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun KeyValues(rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            rows.forEach { (label, value) ->
                Row(Modifier.padding(vertical = 5.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(100.dp),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ZipCreateTool(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var uris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pending by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        if (it.isNotEmpty()) uris = it
    }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { target ->
        if (target != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(target)?.use { output ->
                            ZipOutputStream(output).use { zip ->
                                pending.forEachIndexed { index, uri ->
                                    val name = queryName(context, uri) ?: "file-$index"
                                    val unique = if (pending.count { queryName(context, it) == name } > 1) {
                                        "${index + 1}-$name"
                                    } else {
                                        name
                                    }
                                    zip.putNextEntry(ZipEntry(unique))
                                    context.contentResolver.openInputStream(uri)?.use { it.copyTo(zip) }
                                    zip.closeEntry()
                                }
                            }
                        } != null
                    }.getOrDefault(false)
                }
                snackbar?.showSnackbar(
                    context.getString(if (ok) R.string.file_zip_done else R.string.file_zip_failed),
                )
            }
        }
    }
    ToolPage(title = stringResource(R.string.tool_zip_create), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            DamdastButton(
                text = stringResource(R.string.file_choose_files),
                onClick = { picker.launch("*/*") },
                icon = Icons.Outlined.FolderZip,
                modifier = Modifier.fillMaxWidth(),
            )
            if (uris.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.file_selected_count,
                        formatInt(uris.size, LocalAppLanguage.current),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(14.dp))
            DamdastButton(
                text = stringResource(R.string.file_zip_create),
                onClick = {
                    pending = uris
                    saveLauncher.launch("damdast.zip")
                },
                enabled = uris.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ZipExtractTool(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var zipUri by remember { mutableStateOf<Uri?>(null) }
    var extracting by remember { mutableStateOf(false) }
    val zipPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        zipUri = it
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { tree ->
        val zip = zipUri
        if (tree != null && zip != null) {
            scope.launch {
                extracting = true
                val count = withContext(Dispatchers.IO) {
                    runCatching {
                        runCatching {
                            context.contentResolver.takePersistableUriPermission(
                                tree,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                            )
                        }
                        val root = DocumentFile.fromTreeUri(context, tree) ?: return@withContext -1
                        val targetRoot = root.createDirectory("Damdast Extract") ?: root
                        var extracted = 0
                        context.contentResolver.openInputStream(zip)?.use { raw ->
                            ZipInputStream(raw).use { zipStream ->
                                var entry: ZipEntry? = zipStream.nextEntry
                                while (entry != null) {
                                    val path = sanitizeZipPath(entry.name)
                                    if (path.isNotEmpty() && !entry.isDirectory) {
                                        val parts = path.split('/')
                                        var parent = targetRoot
                                        for (part in parts.dropLast(1)) {
                                            parent = parent.findFile(part) ?: parent.createDirectory(part) ?: parent
                                        }
                                        val fileName = parts.last()
                                        val file = parent.findFile(fileName) ?: parent.createFile(
                                            mimeForName(fileName),
                                            fileName,
                                        )
                                        if (file != null) {
                                            context.contentResolver.openOutputStream(file.uri)?.use { output ->
                                                zipStream.copyTo(output)
                                            }
                                            extracted++
                                        }
                                    }
                                    zipStream.closeEntry()
                                    entry = zipStream.nextEntry
                                }
                            }
                        }
                        extracted
                    }.getOrDefault(-1)
                }
                extracting = false
                snackbar?.showSnackbar(
                    if (count >= 0) {
                        context.getString(R.string.file_zip_extract_done, count.toString())
                    } else {
                        context.getString(R.string.file_zip_failed)
                    },
                )
            }
        }
    }
    ToolPage(title = stringResource(R.string.tool_zip_extract), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            DamdastButton(
                text = stringResource(R.string.file_choose_zip),
                onClick = { zipPicker.launch("*/*") },
                icon = Icons.Outlined.Unarchive,
                modifier = Modifier.fillMaxWidth(),
            )
            if (zipUri != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = queryName(context, zipUri!!) ?: stringResource(R.string.file_choose_zip),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(14.dp))
            DamdastButton(
                text = stringResource(R.string.file_choose_folder),
                onClick = { folderPicker.launch(null) },
                enabled = zipUri != null && !extracting,
                loading = extracting,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.file_info_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun queryName(context: android.content.Context, uri: Uri): String? =
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else null
        }
    }.getOrNull()

private fun sanitizeZipPath(name: String): String =
    name.replace('\\', '/')
        .split('/')
        .filter { it.isNotBlank() && it != "." && it != ".." }
        .joinToString("/")

private fun mimeForName(name: String): String {
    val extension = name.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}
