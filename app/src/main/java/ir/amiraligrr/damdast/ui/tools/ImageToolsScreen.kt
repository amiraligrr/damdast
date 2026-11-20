package ir.amiraligrr.damdast.ui.tools

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.EmptyState
import ir.amiraligrr.damdast.ui.components.LoadingPane
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.ImageOperations
import ir.amiraligrr.damdast.util.ImageUtils
import ir.amiraligrr.damdast.util.formatBytes
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.shareFile
import ir.amiraligrr.damdast.util.sharedCacheFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ImageToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
    if (tool.id == "image_collage") {
        CollageTool(onBack = onBack)
        return
    }
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var uri by remember { mutableStateOf<Uri?>(null) }
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var applying by remember { mutableStateOf(false) }
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var quality by rememberSaveable { mutableFloatStateOf(85f) }
    var formatIndex by rememberSaveable { mutableIntStateOf(0) }
    var cropLeft by rememberSaveable { mutableFloatStateOf(0f) }
    var cropTop by rememberSaveable { mutableFloatStateOf(0f) }
    var cropRight by rememberSaveable { mutableFloatStateOf(0f) }
    var cropBottom by rememberSaveable { mutableFloatStateOf(0f) }
    var resizeWidth by rememberSaveable { mutableStateOf("") }
    var resizeHeight by rememberSaveable { mutableStateOf("") }
    var keepAspect by rememberSaveable { mutableStateOf(true) }
    var brightness by rememberSaveable { mutableFloatStateOf(0f) }
    var contrast by rememberSaveable { mutableFloatStateOf(0f) }
    var saturation by rememberSaveable { mutableFloatStateOf(1f) }
    var filterIndex by rememberSaveable { mutableIntStateOf(0) }
    var textValue by rememberSaveable { mutableStateOf("") }
    var textSize by rememberSaveable { mutableFloatStateOf(0.35f) }
    var textColorIndex by rememberSaveable { mutableIntStateOf(0) }
    var textPosition by rememberSaveable { mutableIntStateOf(2) }
    var shapeCircle by rememberSaveable { mutableStateOf(false) }
    var shapeSize by rememberSaveable { mutableFloatStateOf(0.5f) }
    var shapeColorIndex by rememberSaveable { mutableIntStateOf(0) }

    val palette = listOf(
        android.graphics.Color.WHITE,
        android.graphics.Color.BLACK,
        android.graphics.Color.parseColor("#7C5CFF"),
        android.graphics.Color.parseColor("#22D3EE"),
        android.graphics.Color.parseColor("#FB7185"),
        android.graphics.Color.parseColor("#FBBF24"),
    )
    val saveMime = when {
        tool.id == "image_compress" -> "image/jpeg"
        tool.id == "image_convert" -> listOf("image/png", "image/jpeg", "image/webp")[formatIndex]
        else -> "image/png"
    }
    val saveFormat = when {
        saveMime == "image/jpeg" -> Bitmap.CompressFormat.JPEG
        saveMime == "image/webp" -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.PNG
    }
    val saveExtension = when (saveFormat) {
        Bitmap.CompressFormat.JPEG -> "jpg"
        Bitmap.CompressFormat.WEBP -> "webp"
        else -> "png"
    }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(saveMime),
    ) { target ->
        val image = pendingBitmap
        pendingBitmap = null
        if (target != null && image != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(target)?.use { output ->
                            image.compress(saveFormat, quality.toInt(), output)
                        } != null
                    }.getOrDefault(false)
                }
                snackbar?.showSnackbar(
                    context.getString(if (ok) R.string.image_saved else R.string.image_save_failed),
                )
            }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selected ->
        uri = selected
    }
    LaunchedEffect(uri) {
        val selected = uri ?: return@LaunchedEffect
        loading = true
        loadFailed = false
        val loaded = withContext(Dispatchers.IO) { ImageUtils.loadBitmap(context, selected, 2048) }
        if (loaded == null) {
            loading = false
            loadFailed = true
            return@LaunchedEffect
        }
        original?.recycle()
        bitmap?.recycle()
        original = loaded
        bitmap = loaded.copy(Bitmap.Config.ARGB_8888, true)
        resizeWidth = loaded.width.toString()
        resizeHeight = loaded.height.toString()
        loading = false
    }
    LaunchedEffect(original) {
        val image = original ?: return@LaunchedEffect
        if (resizeWidth.isBlank()) {
            resizeWidth = image.width.toString()
            resizeHeight = image.height.toString()
        }
    }
    val current = bitmap

    fun applyOperation(operation: (Bitmap) -> Bitmap) {
        val image = current ?: return
        scope.launch {
            applying = true
            val result = withContext(Dispatchers.Default) {
                runCatching { operation(image) }.getOrNull()
            }
            if (result != null) {
                bitmap = result
                snackbar?.showSnackbar(context.getString(R.string.image_applied))
            } else {
                snackbar?.showSnackbar(context.getString(R.string.error_generic))
            }
            applying = false
        }
    }

    fun saveCurrent() {
        val image = current ?: return
        pendingBitmap = image
        saveLauncher.launch("damdast-${tool.id}.$saveExtension")
    }

    fun shareCurrent() {
        val image = current ?: return
        scope.launch {
            val file = sharedCacheFile(context, "damdast-${tool.id}.$saveExtension")
            val ok = withContext(Dispatchers.IO) {
                ImageUtils.saveBitmap(image, file, saveFormat, quality.toInt())
            }
            if (ok) shareFile(context, file, saveMime)
            else snackbar?.showSnackbar(context.getString(R.string.image_save_failed))
        }
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
            Spacer(Modifier.height(10.dp))
            if (tool.id != "image_exif") {
                ImagePreview(
                    bitmap = current,
                    cropMode = tool.id == "image_crop",
                    cropLeft = cropLeft,
                    cropTop = cropTop,
                    cropRight = cropRight,
                    cropBottom = cropBottom,
                    zoomable = tool.id == "image_viewer",
                    loading = loading,
                    loadFailed = loadFailed,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DamdastButton(
                    text = stringResource(if (tool.id == "image_exif") R.string.image_pick_image else if (current == null) R.string.image_pick_image else R.string.common_pick_image),
                    onClick = { picker.launch("image/*") },
                    style = if (current == null) DamdastButtonStyle.Primary else DamdastButtonStyle.Outline,
                    modifier = Modifier.weight(1f),
                )
                if (current != null && tool.id != "image_exif") {
                    DamdastButton(
                        text = stringResource(R.string.image_reset),
                        onClick = {
                            original?.let { bitmap = it.copy(Bitmap.Config.ARGB_8888, true) }
                        },
                        style = DamdastButtonStyle.Ghost,
                    )
                }
            }
            if (applying) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.common_loading), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (current == null && !loading) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.image_no_image),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            current?.let {
                Spacer(Modifier.height(16.dp))
                when (tool.id) {
                    "image_viewer" -> Unit
                    "image_exif" -> ExifSection(uri, it, language)
                    "image_crop" -> {
                        CropControls(
                            left = cropLeft, top = cropTop, right = cropRight, bottom = cropBottom,
                            onLeft = { cropLeft = it }, onTop = { cropTop = it },
                            onRight = { cropRight = it }, onBottom = { cropBottom = it },
                        ) {
                            if (cropLeft + cropRight >= 0.95f || cropTop + cropBottom >= 0.95f) {
                                scope.launch { snackbar?.showSnackbar(context.getString(R.string.image_crop_invalid)) }
                            } else {
                                applyOperation {
                                    ImageOperations.crop(it, cropLeft, cropTop, cropRight, cropBottom)
                                }
                            }
                        }
                    }

                    "image_resize" -> {
                        ResizeControls(
                            width = resizeWidth,
                            height = resizeHeight,
                            keepAspect = keepAspect,
                            onWidth = { value ->
                                resizeWidth = value.filter(Char::isDigit).take(4)
                                if (keepAspect) {
                                    val w = resizeWidth.toIntOrNull()
                                    val currentBitmap = current
                                    if (w != null && currentBitmap != null) {
                                        resizeHeight = ((w.toFloat() / currentBitmap.width) * currentBitmap.height)
                                            .toInt().toString()
                                    }
                                }
                            },
                            onHeight = { value ->
                                resizeHeight = value.filter(Char::isDigit).take(4)
                                if (keepAspect) {
                                    val h = resizeHeight.toIntOrNull()
                                    val currentBitmap = current
                                    if (h != null && currentBitmap != null) {
                                        resizeWidth = ((h.toFloat() / currentBitmap.height) * currentBitmap.width)
                                            .toInt().toString()
                                    }
                                }
                            },
                            onKeepAspect = { keepAspect = it },
                        ) {
                            val w = resizeWidth.toIntOrNull()
                            val h = resizeHeight.toIntOrNull()
                            if (w == null || h == null || w !in 16..8000 || h !in 16..8000) {
                                scope.launch { snackbar?.showSnackbar(context.getString(R.string.image_size_invalid)) }
                            } else {
                                applyOperation { ImageOperations.resize(it, w, h) }
                            }
                        }
                    }

                    "image_transform" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DamdastButton(
                                text = stringResource(R.string.image_rotate_left),
                                onClick = { applyOperation { ImageOperations.rotate(it, -90f) } },
                                style = DamdastButtonStyle.Outline,
                                modifier = Modifier.weight(1f),
                            )
                            DamdastButton(
                                text = stringResource(R.string.image_rotate_right),
                                onClick = { applyOperation { ImageOperations.rotate(it, 90f) } },
                                style = DamdastButtonStyle.Outline,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DamdastButton(
                                text = stringResource(R.string.image_flip_horizontal),
                                onClick = { applyOperation { ImageOperations.flip(it, true, false) } },
                                style = DamdastButtonStyle.Outline,
                                modifier = Modifier.weight(1f),
                            )
                            DamdastButton(
                                text = stringResource(R.string.image_flip_vertical),
                                onClick = { applyOperation { ImageOperations.flip(it, false, true) } },
                                style = DamdastButtonStyle.Outline,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    "image_adjust" -> {
                        AdjustControls(
                            brightness = brightness,
                            contrast = contrast,
                            saturation = saturation,
                            onBrightness = { brightness = it },
                            onContrast = { contrast = it },
                            onSaturation = { saturation = it },
                        ) {
                            applyOperation { ImageOperations.adjust(it, brightness, contrast, saturation) }
                        }
                    }

                    "image_filters" -> {
                        FiltersControls(
                            selected = filterIndex,
                            onSelect = { filterIndex = it },
                        ) {
                            applyOperation { ImageOperations.filter(it, filterIndex) }
                        }
                    }

                    "image_text" -> {
                        TextShapeControls(
                            text = textValue,
                            textSize = textSize,
                            textColorIndex = textColorIndex,
                            textPosition = textPosition,
                            shapeCircle = shapeCircle,
                            shapeSize = shapeSize,
                            shapeColorIndex = shapeColorIndex,
                            palette = palette,
                            onText = { textValue = it },
                            onTextSize = { textSize = it },
                            onTextColor = { textColorIndex = it },
                            onTextPosition = { textPosition = it },
                            onCircle = { shapeCircle = it },
                            onShapeSize = { shapeSize = it },
                            onShapeColor = { shapeColorIndex = it },
                            onAddText = {
                                if (textValue.isNotBlank()) {
                                    applyOperation {
                                        ImageOperations.addText(
                                            it, textValue, palette[textColorIndex], textSize, textPosition,
                                        )
                                    }
                                }
                            },
                            onAddShape = {
                                applyOperation {
                                    ImageOperations.addShape(
                                        it, shapeCircle, palette[shapeColorIndex], shapeSize,
                                    )
                                }
                            },
                        )
                    }

                    "image_compress" -> {
                        Text(
                            text = stringResource(R.string.image_quality) + " · ${quality.toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Slider(
                            value = quality,
                            onValueChange = { quality = it },
                            valueRange = 10f..100f,
                        )
                        Text(
                            text = stringResource(R.string.tool_image_compress_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    "image_convert" -> {
                        Text(
                            text = stringResource(R.string.image_format),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("PNG", "JPEG", "WebP").forEachIndexed { index, label ->
                                FilterChip(
                                    selected = formatIndex == index,
                                    onClick = { formatIndex = index },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                }
                if (tool.id != "image_exif" && tool.id != "image_viewer") {
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DamdastButton(
                            text = stringResource(R.string.image_save),
                            onClick = ::saveCurrent,
                            icon = Icons.Outlined.Save,
                            modifier = Modifier.weight(1f),
                        )
                        DamdastButton(
                            text = stringResource(R.string.image_share),
                            onClick = ::shareCurrent,
                            icon = Icons.Outlined.Share,
                            style = DamdastButtonStyle.Outline,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (tool.id == "image_viewer") {
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DamdastButton(
                            text = stringResource(R.string.image_save),
                            onClick = ::saveCurrent,
                            modifier = Modifier.weight(1f),
                        )
                        DamdastButton(
                            text = stringResource(R.string.image_share),
                            onClick = ::shareCurrent,
                            style = DamdastButtonStyle.Outline,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ImagePreview(
    bitmap: Bitmap?,
    cropMode: Boolean,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    zoomable: Boolean,
    loading: Boolean,
    loadFailed: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp, max = 430.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                loading -> CircularProgressIndicator()
                loadFailed -> Text(
                    text = stringResource(R.string.image_loading_failed),
                    color = MaterialTheme.colorScheme.error,
                )

                bitmap == null -> EmptyState(
                    icon = Icons.Outlined.Image,
                    title = stringResource(R.string.image_pick_image),
                    body = stringResource(R.string.image_no_image),
                )

                else -> {
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        offset += panChange
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (zoomable) Modifier.transformable(transformState) else Modifier),
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y,
                                ),
                        )
                        if (cropMode) {
                            Canvas(Modifier.fillMaxSize()) {
                                val left = size.width * cropLeft
                                val top = size.height * cropTop
                                val right = size.width * (1f - cropRight)
                                val bottom = size.height * (1f - cropBottom)
                                drawRect(
                                    color = Color.White.copy(alpha = 0.75f),
                                    topLeft = Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(
                                        (right - left).coerceAtLeast(0f),
                                        (bottom - top).coerceAtLeast(0f),
                                    ),
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CropControls(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    onLeft: (Float) -> Unit,
    onTop: (Float) -> Unit,
    onRight: (Float) -> Unit,
    onBottom: (Float) -> Unit,
    onApply: () -> Unit,
) {
    listOf(
        Triple(R.string.image_crop_left, left, onLeft),
        Triple(R.string.image_crop_top, top, onTop),
        Triple(R.string.image_crop_right, right, onRight),
        Triple(R.string.image_crop_bottom, bottom, onBottom),
    ).forEach { (label, value, setter) ->
        Text(
            text = stringResource(label) + " · ${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(value = value, onValueChange = setter, valueRange = 0f..0.45f)
    }
    DamdastButton(
        text = stringResource(R.string.image_apply_crop),
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResizeControls(
    width: String,
    height: String,
    keepAspect: Boolean,
    onWidth: (String) -> Unit,
    onHeight: (String) -> Unit,
    onKeepAspect: (Boolean) -> Unit,
    onApply: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ImageNumberField(width, onWidth, stringResource(R.string.image_width), Modifier.weight(1f))
        ImageNumberField(height, onHeight, stringResource(R.string.image_height), Modifier.weight(1f))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onKeepAspect(!keepAspect) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = keepAspect, onCheckedChange = onKeepAspect)
        Text(stringResource(R.string.image_keep_aspect))
    }
    DamdastButton(
        text = stringResource(R.string.image_apply_resize),
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ImageNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.small,
        colors = imageFieldColors(),
    )
}

@Composable
private fun AdjustControls(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightness: (Float) -> Unit,
    onContrast: (Float) -> Unit,
    onSaturation: (Float) -> Unit,
    onApply: () -> Unit,
) {
    Text(
        text = stringResource(R.string.image_brightness) + " · ${brightness.toInt()}",
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(brightness, onBrightness, valueRange = -100f..100f)
    Text(
        text = stringResource(R.string.image_contrast) + " · ${contrast.toInt()}",
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(contrast, onContrast, valueRange = -100f..100f)
    Text(
        text = stringResource(R.string.image_saturation) + " · ${(saturation * 100).toInt()}%",
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(saturation, onSaturation, valueRange = 0f..2f)
    DamdastButton(
        text = stringResource(R.string.image_apply_adjust),
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FiltersControls(selected: Int, onSelect: (Int) -> Unit, onApply: () -> Unit) {
    val labels = listOf(
        R.string.image_filter_original,
        R.string.image_filter_mono,
        R.string.image_filter_sepia,
        R.string.image_filter_warm,
        R.string.image_filter_cool,
        R.string.image_filter_invert,
        R.string.image_filter_vintage,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(stringResource(label)) },
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    DamdastButton(
        text = stringResource(R.string.image_apply_filter),
        onClick = onApply,
        enabled = selected != 0,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TextShapeControls(
    text: String,
    textSize: Float,
    textColorIndex: Int,
    textPosition: Int,
    shapeCircle: Boolean,
    shapeSize: Float,
    shapeColorIndex: Int,
    palette: List<Int>,
    onText: (String) -> Unit,
    onTextSize: (Float) -> Unit,
    onTextColor: (Int) -> Unit,
    onTextPosition: (Int) -> Unit,
    onCircle: (Boolean) -> Unit,
    onShapeSize: (Float) -> Unit,
    onShapeColor: (Int) -> Unit,
    onAddText: () -> Unit,
    onAddShape: () -> Unit,
) {
    OutlinedTextField(
        value = text,
        onValueChange = onText,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.image_text_label)) },
        placeholder = { Text(stringResource(R.string.image_text_hint)) },
        shape = MaterialTheme.shapes.small,
        colors = imageFieldColors(),
    )
    Text(
        text = stringResource(R.string.image_text_size) + " · ${(textSize * 100).toInt()}%",
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(textSize, onTextSize, valueRange = 0.1f..1f)
    ColorSwatches(palette, textColorIndex, onTextColor)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(R.string.image_crop_top, R.string.common_default, R.string.image_crop_bottom)
            .forEachIndexed { index, _ ->
                FilterChip(
                    selected = textPosition == index,
                    onClick = { onTextPosition(index) },
                    label = {
                        Text(
                            when (index) {
                                0 -> "↑"
                                2 -> "↓"
                                else -> "•"
                            },
                        )
                    },
                )
            }
    }
    DamdastButton(
        text = stringResource(R.string.image_apply_text),
        onClick = onAddText,
        enabled = text.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    Text(text = stringResource(R.string.image_shape_type), style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !shapeCircle,
            onClick = { onCircle(false) },
            label = { Text(stringResource(R.string.image_shape_rectangle)) },
        )
        FilterChip(
            selected = shapeCircle,
            onClick = { onCircle(true) },
            label = { Text(stringResource(R.string.image_shape_circle)) },
        )
    }
    Text(
        text = stringResource(R.string.image_shape_size) + " · ${(shapeSize * 100).toInt()}%",
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(shapeSize, onShapeSize, valueRange = 0.05f..1f)
    ColorSwatches(palette, shapeColorIndex, onShapeColor)
    DamdastButton(
        text = stringResource(R.string.image_apply_shape),
        onClick = onAddShape,
        style = DamdastButtonStyle.Outline,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ColorSwatches(palette: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        palette.forEachIndexed { index, color ->
            val composeColor = Color(color)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(composeColor)
                    .then(
                        if (selected == index) {
                            Modifier.padding(2.dp)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected == index) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExifSection(uri: Uri?, bitmap: Bitmap, language: ir.amiraligrr.damdast.data.AppLanguage) {
    val context = LocalContext.current
    val labels = listOf(
        stringResource(R.string.image_exif_dimensions),
        stringResource(R.string.image_exif_file),
        stringResource(R.string.image_exif_size),
        stringResource(R.string.image_exif_type),
        stringResource(R.string.image_exif_make),
        stringResource(R.string.image_exif_model),
        stringResource(R.string.image_exif_date),
        stringResource(R.string.image_exif_aperture),
        stringResource(R.string.image_exif_exposure),
        stringResource(R.string.image_exif_iso),
        stringResource(R.string.image_exif_focal),
        stringResource(R.string.image_exif_gps),
    )
    var rows by remember(uri) { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(uri) {
        val selected = uri ?: return@LaunchedEffect
        rows = withContext(Dispatchers.IO) {
            val result = mutableListOf<Pair<String, String>>()
            result.add(labels[0] to "${bitmap.width} × ${bitmap.height}")
            runCatching {
                context.contentResolver.query(selected, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex >= 0) result.add(labels[1] to cursor.getString(nameIndex))
                        if (sizeIndex >= 0) result.add(labels[2] to formatBytes(cursor.getLong(sizeIndex), language))
                    }
                }
                context.contentResolver.getType(selected)?.let { result.add(labels[3] to it) }
                context.contentResolver.openInputStream(selected)?.use { stream ->
                    val exif = ExifInterface(stream)
                    fun add(label: String, tag: String) {
                        exif.getAttribute(tag)?.takeIf { it.isNotBlank() }?.let { result.add(label to it) }
                    }
                    add(labels[4], ExifInterface.TAG_MAKE)
                    add(labels[5], ExifInterface.TAG_MODEL)
                    add(labels[6], ExifInterface.TAG_DATETIME_ORIGINAL)
                    add(labels[7], ExifInterface.TAG_F_NUMBER)
                    add(labels[8], ExifInterface.TAG_EXPOSURE_TIME)
                    add(labels[9], ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                    add(labels[10], ExifInterface.TAG_FOCAL_LENGTH)
                    val latLong = exif.latLong
                    if (latLong != null) {
                        result.add(labels[11] to "${"%.5f".format(latLong[0])}, ${"%.5f".format(latLong[1])}")
                    }
                }
            }
            result
        }
    }
    KeyValueCard(rows)
}

@Composable
private fun KeyValueCard(rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            if (rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.common_loading),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
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
private fun CollageTool(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var collage by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png"),
    ) { uri ->
        val image = pendingBitmap
        pendingBitmap = null
        if (uri != null && image != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            image.compress(Bitmap.CompressFormat.PNG, 100, output)
                        } != null
                    }.getOrDefault(false)
                }
                snackbar?.showSnackbar(
                    context.getString(if (ok) R.string.image_saved else R.string.image_save_failed),
                )
            }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        scope.launch {
            loading = true
            val loaded = withContext(Dispatchers.IO) {
                uris.take(4).mapNotNull { uri -> ImageUtils.loadBitmap(context, uri, 1400) }
            }
            bitmaps.forEach { it.recycle() }
            bitmaps = loaded
            collage = null
            loading = false
        }
    }
    ToolPage(title = stringResource(R.string.tool_image_collage), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.image_collage_select),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            DamdastButton(
                text = stringResource(R.string.common_pick_image),
                onClick = { picker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.image_collage_count,
                    formatInt(bitmaps.size, LocalAppLanguage.current),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            DamdastButton(
                text = stringResource(R.string.image_collage_generate),
                onClick = {
                    if (bitmaps.size >= 2) {
                        scope.launch {
                            loading = true
                            collage = withContext(Dispatchers.Default) {
                                ImageOperations.collage(bitmaps)
                            }
                            loading = false
                        }
                    }
                },
                enabled = bitmaps.size in 2..4,
                modifier = Modifier.fillMaxWidth(),
            )
            if (loading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            collage?.let { image ->
                Spacer(Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .padding(10.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamdastButton(
                        text = stringResource(R.string.image_save),
                        onClick = {
                            pendingBitmap = image
                            saveLauncher.launch("damdast-collage.png")
                        },
                        modifier = Modifier.weight(1f),
                    )
                    DamdastButton(
                        text = stringResource(R.string.image_share),
                        onClick = {
                            scope.launch {
                                val file = sharedCacheFile(context, "damdast-collage.png")
                                val ok = withContext(Dispatchers.IO) {
                                    ImageUtils.saveBitmap(image, file, Bitmap.CompressFormat.PNG)
                                }
                                if (ok) shareFile(context, file, "image/png")
                            }
                        },
                        style = DamdastButtonStyle.Outline,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(6.dp))
                DamdastButton(
                    text = stringResource(R.string.image_collage_clear),
                    onClick = {
                        bitmaps.forEach { it.recycle() }
                        bitmaps = emptyList()
                        collage = null
                    },
                    style = DamdastButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun imageFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
)
