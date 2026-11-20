package ir.amiraligrr.damdast.ui.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.text.PDFTextStripper
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.DamdastViewModel
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.ErrorPane
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.ImageUtils
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.normalizeDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.ceil

@Composable
fun PdfToolsScreen(
    tool: ToolDefinition,
    viewModel: DamdastViewModel,
    onBack: () -> Unit,
) {
    if (tool.id == "pdf_viewer") {
        PdfViewerTool(viewModel = viewModel, onBack = onBack)
    } else {
        PdfOperationTool(tool = tool, onBack = onBack)
    }
}

private class PdfViewerController(context: android.content.Context, uri: Uri) {
    private val descriptor: ParcelFileDescriptor =
        context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("Cannot open file")
    private val renderer = PdfRenderer(descriptor)
    val pageCount: Int get() = renderer.pageCount

    @Synchronized
    fun render(index: Int, targetWidth: Int = 1080): Bitmap {
        val page = renderer.openPage(index)
        try {
            val height = (targetWidth.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bitmap
        } finally {
            page.close()
        }
    }

    fun close() {
        runCatching { renderer.close() }
        runCatching { descriptor.close() }
    }
}

@Composable
private fun PdfViewerTool(viewModel: DamdastViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()
    var uri by remember { mutableStateOf<Uri?>(null) }
    var controller by remember { mutableStateOf<PdfViewerController?>(null) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var pageCount by rememberSaveable { mutableIntStateOf(0) }
    var requestedPage by rememberSaveable { mutableIntStateOf(1) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Int>>(emptyList()) }
    var searchDone by remember { mutableStateOf(false) }
    val bookmarks by viewModel.pdfBookmarks.collectAsStateWithLifecycle()
    val key = uri?.toString().orEmpty()
    val bookmarked = key.isNotBlank() && (page + 1) in bookmarks[key].orEmpty()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selected ->
        if (selected != null) {
            uri = selected
            page = 0
            requestedPage = 1
            error = false
        }
    }
    LaunchedEffect(uri) {
        controller?.close()
        controller = null
        bitmap?.recycle()
        bitmap = null
        val selected = uri ?: return@LaunchedEffect
        loading = true
        error = false
        runCatching {
            withContext(Dispatchers.IO) {
                val created = PdfViewerController(context, selected)
                val count = created.pageCount
                val rendered = created.render(0)
                Triple(created, count, rendered)
            }
        }.onSuccess { (created, count, rendered) ->
            controller = created
            pageCount = count
            bitmap = rendered
            loading = false
        }.onFailure {
            error = true
            loading = false
        }
    }
    LaunchedEffect(controller, page) {
        val active = controller ?: return@LaunchedEffect
        if (pageCount == 0) return@LaunchedEffect
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { active.render(page) }
        }.onSuccess {
            bitmap?.recycle()
            bitmap = it
            loading = false
        }.onFailure {
            loading = false
            error = true
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            controller?.close()
            bitmap?.recycle()
        }
    }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            searchDone = false
            return@LaunchedEffect
        }
        delay(350)
        val selected = uri ?: return@LaunchedEffect
        searching = true
        searchDone = false
        val results = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(selected)?.use { input ->
                    PDDocument.load(input).use { document ->
                        val text = PDFTextStripper().getText(document)
                        text.split('\u000C').mapIndexedNotNull { index, pageText ->
                            if (pageText.contains(searchQuery, ignoreCase = true)) index + 1 else null
                        }
                    }
                } ?: emptyList()
            }.getOrDefault(emptyList())
        }
        searchResults = results
        searching = false
        searchDone = true
    }

    ToolPage(
        title = stringResource(R.string.tool_pdf_viewer),
        onBack = onBack,
        actions = {
            if (uri != null && pageCount > 0) {
                IconButton(
                    onClick = {
                        if (key.isNotBlank()) viewModel.togglePdfBookmark(key, page + 1)
                    },
                ) {
                    Icon(
                        imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (bookmarked) R.string.pdf_bookmark_remove else R.string.pdf_bookmark_add,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DamdastButton(
                    text = stringResource(R.string.pdf_choose),
                    onClick = { picker.launch("application/pdf") },
                    style = DamdastButtonStyle.Outline,
                    modifier = Modifier.weight(1f),
                )
                if (uri != null) {
                    Text(
                        text = stringResource(
                            R.string.pdf_page_of,
                            formatInt(page + 1, language),
                            formatInt(pageCount, language),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
            if (uri != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.pdf_search_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = pdfFieldColors(),
                )
                if (searching) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.common_loading), style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (searchResults.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.pdf_search_results),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        searchResults.forEach { resultPage ->
                            FilterChip(
                                selected = page + 1 == resultPage,
                                onClick = {
                                    if (resultPage in 1..pageCount) {
                                        page = resultPage - 1
                                    } else {
                                        error = true
                                    }
                                },
                                label = {
                                    Text(
                                        formatInt(resultPage, language),
                                    )
                                },
                            )
                        }
                    }
                } else if (searchDone && searchQuery.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.pdf_no_results),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            when {
                error -> ErrorPane(
                    title = stringResource(R.string.error_invalid_pdf),
                    body = stringResource(R.string.pdf_open_error),
                    retryLabel = stringResource(R.string.pdf_choose),
                    onRetry = { picker.launch("application/pdf") },
                )

                loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                bitmap != null -> Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .background(androidx.compose.ui.graphics.Color.White),
                )

                else -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
            if (uri != null && pageCount > 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (page > 0) page-- }) {
                        Icon(
                            Icons.Outlined.ChevronLeft,
                            contentDescription = stringResource(R.string.pdf_previous_page),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(
                            R.string.pdf_page_of,
                            formatInt(page + 1, language),
                            formatInt(pageCount, language),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { if (page < pageCount - 1) page++ }) {
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = stringResource(R.string.pdf_next_page),
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PdfOperationTool(tool: ToolDefinition, onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var uris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var working by remember { mutableStateOf(false) }
    var pageRange by rememberSaveable { mutableStateOf("1-3") }
    var angle by rememberSaveable { mutableIntStateOf(90) }
    var watermark by rememberSaveable { mutableStateOf("") }

    val outputMime = if (tool.id == "pdf_to_images") "application/zip" else "application/pdf"
    val outputName = when (tool.id) {
        "pdf_merge" -> "damdast-merged.pdf"
        "pdf_extract" -> "damdast-pages.pdf"
        "pdf_rotate" -> "damdast-rotated.pdf"
        "pdf_watermark" -> "damdast-watermarked.pdf"
        "pdf_numbers" -> "damdast-numbered.pdf"
        "images_to_pdf" -> "damdast-images.pdf"
        else -> "damdast-pages.zip"
    }

    fun failure(message: String = context.getString(R.string.pdf_failed)) {
        scope.launch { snackbar?.showSnackbar(message) }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(outputMime),
    ) { target ->
        if (target != null) {
            scope.launch {
                working = true
                val success = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(target)?.use { output ->
                            processPdfOperation(
                                context = context,
                                toolId = tool.id,
                                inputs = uris,
                                pageRange = pageRange,
                                angle = angle,
                                watermark = watermark,
                                output = output,
                            )
                        } != null
                    }.getOrDefault(false)
                }
                working = false
                snackbar?.showSnackbar(
                    context.getString(if (success) R.string.pdf_done else R.string.pdf_failed),
                )
            }
        }
    }
    val singlePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) uris = listOf(it)
    }
    val multiPdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        if (it.isNotEmpty()) uris = it.take(10)
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        if (it.isNotEmpty()) uris = it.take(20)
    }

    val canApply = when (tool.id) {
        "pdf_merge" -> uris.size >= 2
        "images_to_pdf" -> uris.isNotEmpty()
        "pdf_watermark" -> uris.isNotEmpty() && watermark.isNotBlank()
        else -> uris.isNotEmpty()
    }
    val language = LocalAppLanguage.current

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
            when (tool.id) {
                "pdf_merge" -> {
                    DamdastButton(
                        text = stringResource(R.string.pdf_choose_multiple),
                        onClick = { multiPdfPicker.launch("application/pdf") },
                        style = DamdastButtonStyle.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (uris.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(
                                R.string.pdf_selected_count,
                                formatInt(uris.size, language),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                "images_to_pdf" -> {
                    DamdastButton(
                        text = stringResource(R.string.common_pick_image),
                        onClick = { imagePicker.launch("image/*") },
                        style = DamdastButtonStyle.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (uris.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.pdf_selected_count, formatInt(uris.size, language)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    DamdastButton(
                        text = stringResource(R.string.pdf_choose),
                        onClick = { singlePicker.launch("application/pdf") },
                        style = DamdastButtonStyle.Outline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (tool.id == "pdf_extract") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pageRange,
                    onValueChange = { pageRange = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pdf_page_range)) },
                    placeholder = { Text(stringResource(R.string.pdf_page_range_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = pdfFieldColors(),
                )
            }
            if (tool.id == "pdf_rotate") {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.pdf_angle),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(90, 180, 270).forEach { value ->
                        FilterChip(
                            selected = angle == value,
                            onClick = { angle = value },
                            label = { Text("$value°") },
                        )
                    }
                }
            }
            if (tool.id == "pdf_watermark") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = watermark,
                    onValueChange = { watermark = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pdf_watermark_text)) },
                    placeholder = { Text(stringResource(R.string.pdf_watermark_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = pdfFieldColors(),
                )
            }
            if (tool.id == "pdf_to_images") {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.pdf_lossy_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            DamdastButton(
                text = stringResource(R.string.pdf_save),
                onClick = {
                    if (canApply) saveLauncher.launch(outputName) else failure()
                },
                enabled = canApply && !working,
                loading = working,
                modifier = Modifier.fillMaxWidth(),
            )
            if (working) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.pdf_working), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun processPdfOperation(
    context: android.content.Context,
    toolId: String,
    inputs: List<Uri>,
    pageRange: String,
    angle: Int,
    watermark: String,
    output: OutputStream,
) {
    when (toolId) {
        "images_to_pdf" -> imagesToPdf(context, inputs, output)
        "pdf_merge" -> mergePdfs(context, inputs, output)
        "pdf_extract" -> extractPdfPages(context, inputs.first(), pageRange, output)
        "pdf_rotate" -> rotatePdf(context, inputs.first(), angle, output)
        "pdf_watermark" -> watermarkPdf(context, inputs.first(), watermark, output)
        "pdf_numbers" -> numberPdfPages(context, inputs.first(), output)
        "pdf_to_images" -> pdfToImages(context, inputs.first(), output)
        else -> error("Unsupported PDF operation")
    }
}

private fun mergePdfs(context: android.content.Context, uris: List<Uri>, output: OutputStream) {
    val documents = uris.mapNotNull { uri ->
        context.contentResolver.openInputStream(uri)?.use { PDDocument.load(it) }
    }
    try {
        PDDocument().use { target ->
            documents.forEach { document ->
                for (page in document.pages) target.importPage(page)
            }
            target.save(output)
        }
    } finally {
        documents.forEach { runCatching { it.close() } }
    }
}

private fun extractPdfPages(
    context: android.content.Context,
    uri: Uri,
    range: String,
    output: OutputStream,
) {
    val document = context.contentResolver.openInputStream(uri)?.use { PDDocument.load(it) }
        ?: error("Cannot open PDF")
    document.use {
        val pages = parsePageRange(range, it.numberOfPages)
        require(pages.isNotEmpty()) { "No pages selected" }
        PDDocument().use { target ->
            pages.forEach { pageNumber -> target.importPage(it.getPage(pageNumber - 1)) }
            target.save(output)
        }
    }
}

private fun rotatePdf(context: android.content.Context, uri: Uri, angle: Int, output: OutputStream) {
    val document = context.contentResolver.openInputStream(uri)?.use { PDDocument.load(it) }
        ?: error("Cannot open PDF")
    document.use {
        for (page in it.pages) {
            page.rotation = (page.rotation + angle) % 360
        }
        it.save(output)
    }
}

private fun watermarkPdf(
    context: android.content.Context,
    uri: Uri,
    text: String,
    output: OutputStream,
) {
    val document = context.contentResolver.openInputStream(uri)?.use { PDDocument.load(it) }
        ?: error("Cannot open PDF")
    document.use {
        val font = PDType1Font.HELVETICA_BOLD
        for (page in it.pages) {
            PDPageContentStream(it, page, PDPageContentStream.AppendMode.APPEND, true, true).use { stream ->
                stream.setNonStrokingColor(140, 140, 165)
                stream.beginText()
                stream.setFont(font, 38f)
                val width = page.mediaBox.width
                val height = page.mediaBox.height
                val textWidth = font.getStringWidth(text) / 1000f * 38f
                stream.newLineAtOffset((width - textWidth) / 2f, height / 2f)
                stream.showText(text)
                stream.endText()
            }
        }
        it.save(output)
    }
}

private fun numberPdfPages(context: android.content.Context, uri: Uri, output: OutputStream) {
    val document = context.contentResolver.openInputStream(uri)?.use { PDDocument.load(it) }
        ?: error("Cannot open PDF")
    document.use {
        val font = PDType1Font.HELVETICA
        it.pages.forEachIndexed { index, page ->
            PDPageContentStream(it, page, PDPageContentStream.AppendMode.APPEND, true, true).use { stream ->
                stream.setNonStrokingColor(80, 80, 80)
                stream.beginText()
                stream.setFont(font, 10f)
                val value = "${index + 1} / ${it.numberOfPages}"
                val width = font.getStringWidth(value) / 1000f * 10f
                stream.newLineAtOffset((page.mediaBox.width - width) / 2f, 18f)
                stream.showText(value)
                stream.endText()
            }
        }
        it.save(output)
    }
}

private fun imagesToPdf(context: android.content.Context, uris: List<Uri>, output: OutputStream) {
    val document = PdfDocument()
    try {
        val pageWidth = 595
        val pageHeight = 842
        uris.forEachIndexed { index, uri ->
            val bitmap = loadBitmapBlocking(context, uri) ?: return@forEachIndexed
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)
            val scale = minOf(pageWidth.toFloat() / bitmap.width, pageHeight.toFloat() / bitmap.height)
            val width = bitmap.width * scale
            val height = bitmap.height * scale
            val left = (pageWidth - width) / 2f
            val top = (pageHeight - height) / 2f
            canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + width, top + height), null)
            document.finishPage(page)
            bitmap.recycle()
        }
        document.writeTo(output)
    } finally {
        document.close()
    }
}

private fun pdfToImages(context: android.content.Context, uri: Uri, output: OutputStream) {
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: error("Cannot open PDF")
    val renderer = PdfRenderer(descriptor)
    try {
        val zip = ZipOutputStream(output)
        for (index in 0 until renderer.pageCount) {
            val page = renderer.openPage(index)
            val width = 1400
            val height = (width.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            page.close()
            zip.putNextEntry(ZipEntry("page-%03d.png".format(index + 1)))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, zip)
            bitmap.recycle()
            zip.closeEntry()
        }
        zip.finish()
    } finally {
        renderer.close()
        descriptor.close()
    }
}

private fun loadBitmapBlocking(context: android.content.Context, uri: Uri): Bitmap? =
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) }
    }.getOrNull()

private fun parsePageRange(input: String, pageCount: Int): List<Int> {
    val normalized = input.normalizeDigits()
    val pages = linkedSetOf<Int>()
    normalized.split(',').forEach { part ->
        val trimmed = part.trim()
        if (trimmed.isEmpty()) return@forEach
        if (trimmed.contains('-')) {
            val pieces = trimmed.split('-', limit = 2)
            val start = pieces[0].toIntOrNull() ?: return@forEach
            val end = pieces[1].toIntOrNull() ?: return@forEach
            val boundedStart = maxOf(1, start)
            val boundedEnd = minOf(pageCount, end)
            if (boundedStart <= boundedEnd) {
                for (page in boundedStart..boundedEnd) pages.add(page)
            }
        } else {
            val page = trimmed.toIntOrNull() ?: return@forEach
            if (page in 1..pageCount) pages.add(page)
        }
    }
    return pages.toList()
}

@Composable
private fun pdfFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
)
