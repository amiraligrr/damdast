package ir.amiraligrr.damdast.ui.tools

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.LoadingPane
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.ArgbColor
import ir.amiraligrr.damdast.util.ColorUtils
import ir.amiraligrr.damdast.util.ImageUtils
import ir.amiraligrr.damdast.util.copyToClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ColorToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        when (tool.id) {
            "color_picker" -> ColorPickerTool()
            "gradient_generator" -> GradientTool()
            "palette_generator" -> PaletteTool()
            "image_colors" -> ImageColorsTool()
            "font_preview" -> FontPreviewTool()
        }
    }
}

@Composable
private fun ColorPickerTool() {
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var hue by rememberSaveable { mutableFloatStateOf(258f) }
    var saturation by rememberSaveable { mutableFloatStateOf(0.72f) }
    var value by rememberSaveable { mutableFloatStateOf(1f) }
    var copyValue by remember { mutableStateOf("") }
    val color = ColorUtils.fromHsv(hue, saturation, value)
    fun copyHex() {
        copyToClipboard(context, color.hex())
        scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Color(color.red, color.green, color.blue))
                .clickable { copyHex() },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = color.hex(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (Color(color.red, color.green, color.blue).luminance() > 0.5f) {
                        Color.Black
                    } else {
                        Color.White
                    },
                )
                Text(
                    text = stringResource(R.string.color_copy_value),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (Color(color.red, color.green, color.blue).luminance() > 0.5f) {
                        Color.Black.copy(alpha = 0.6f)
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        ColorSlider(
            label = stringResource(R.string.color_hue),
            value = hue,
            valueRange = 0f..360f,
            display = "${hue.toInt()}°",
            onValueChange = { hue = it },
        )
        ColorSlider(
            label = stringResource(R.string.color_saturation),
            value = saturation,
            valueRange = 0f..1f,
            display = "${(saturation * 100).toInt()}%",
            onValueChange = { saturation = it },
        )
        ColorSlider(
            label = stringResource(R.string.color_brightness),
            value = value,
            valueRange = 0f..1f,
            display = "${(value * 100).toInt()}%",
            onValueChange = { value = it },
        )
        Spacer(Modifier.height(10.dp))
        ColorCodeCard(
            rows = listOf(
                "HEX" to color.hex(),
                "RGB" to color.rgbString(),
                "HSL" to ColorUtils.hslString(color),
                "HSV" to ColorUtils.hsvString(color),
            ),
            language = language,
        )
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(100.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = display,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun GradientTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var start by rememberSaveable { mutableStateOf("#7C5CFF") }
    var end by rememberSaveable { mutableStateOf("#22D3EE") }
    var angle by rememberSaveable { mutableFloatStateOf(135f) }
    val startColor = runCatching { ColorUtils.parse(start) }.getOrNull()
    val endColor = runCatching { ColorUtils.parse(end) }.getOrNull()
    val valid = startColor != null && endColor != null
    val radians = Math.toRadians(angle.toDouble())
    val endOffset = Offset((cos(radians) * 1000).toFloat(), (sin(radians) * 1000).toFloat())
    val startOffset = Offset((-cos(radians) * 1000).toFloat(), (-sin(radians) * 1000).toFloat())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                    if (valid) {
                        Brush.linearGradient(
                            listOf(
                                Color(startColor!!.red, startColor.green, startColor.blue),
                                Color(endColor!!.red, endColor.green, endColor.blue),
                            ),
                            start = startOffset,
                            end = endOffset,
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    },
                ),
        )
        Spacer(Modifier.height(16.dp))
        HexField(start, { start = it }, stringResource(R.string.color_start_color), startColor != null)
        Spacer(Modifier.height(8.dp))
        HexField(end, { end = it }, stringResource(R.string.color_end_color), endColor != null)
        if (!valid) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.color_invalid_hex),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(12.dp))
        ColorSlider(
            label = stringResource(R.string.color_angle),
            value = angle,
            valueRange = 0f..360f,
            display = "${angle.toInt()}°",
            onValueChange = { angle = it },
        )
        Spacer(Modifier.height(10.dp))
        val css = "background: linear-gradient(${angle.toInt()}deg, ${startColor?.hex() ?: start}, ${endColor?.hex() ?: end});"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.color_css),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            copyToClipboard(context, css)
                            scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                        },
                        enabled = valid,
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.common_copy),
                        )
                    }
                }
                SelectionContainer {
                    Text(
                        text = css,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun HexField(value: String, onValueChange: (String) -> Unit, label: String, valid: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = !valid,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun PaletteTool() {
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var hue by rememberSaveable { mutableFloatStateOf(258f) }
    var saturation by rememberSaveable { mutableFloatStateOf(0.7f) }
    var lightness by rememberSaveable { mutableFloatStateOf(0.55f) }
    val base = remember(hue, saturation, lightness) {
        ColorUtils.hslToColor(hue, saturation, lightness)
    }
    val palette = remember(base) { ColorUtils.palette(base) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.color_base_color),
            style = MaterialTheme.typography.labelLarge,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(Color(base.red, base.green, base.blue)),
        )
        Spacer(Modifier.height(10.dp))
        ColorSlider(
            label = stringResource(R.string.color_hue),
            value = hue,
            valueRange = 0f..360f,
            display = "${hue.toInt()}°",
            onValueChange = { hue = it },
        )
        ColorSlider(
            label = stringResource(R.string.color_saturation),
            value = saturation,
            valueRange = 0f..1f,
            display = "${(saturation * 100).toInt()}%",
            onValueChange = { saturation = it },
        )
        ColorSlider(
            label = stringResource(R.string.color_lightness),
            value = lightness,
            valueRange = 0f..1f,
            display = "${(lightness * 100).toInt()}%",
            onValueChange = { lightness = it },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.color_palette),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        palette.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { color ->
                    val composeColor = Color(color.red, color.green, color.blue)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .background(composeColor)
                            .clickable {
                                copyToClipboard(context, color.hex())
                                scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                            }
                            .padding(14.dp)
                            .height(82.dp),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text(
                            text = color.hex(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (composeColor.luminance() > 0.5f) Color.Black else Color.White,
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
        ColorCodeCard(
            rows = listOf(
                "HEX" to base.hex(),
                "RGB" to base.rgbString(),
                "HSL" to ColorUtils.hslString(base),
            ),
            language = language,
        )
    }
}

@Composable
private fun ImageColorsTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var colors by remember { mutableStateOf<List<ArgbColor>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<android.net.Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selected = uri
    }
    LaunchedEffect(selected) {
        val uri = selected ?: return@LaunchedEffect
        loading = true
        error = false
        val bitmap = withContext(Dispatchers.IO) { ImageUtils.loadBitmap(context, uri, 512) }
        if (bitmap == null) {
            error = true
            colors = emptyList()
        } else {
            colors = withContext(Dispatchers.Default) { ImageUtils.dominantColors(bitmap) }
            bitmap.recycle()
        }
        loading = false
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        DamdastButton(
            text = stringResource(R.string.common_pick_image),
            onClick = { launcher.launch("image/*") },
            icon = Icons.Outlined.ImageSearch,
            style = DamdastButtonStyle.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        when {
            loading -> LoadingPane(label = stringResource(R.string.color_extracting))
            error -> Text(
                text = stringResource(R.string.error_unreadable_file),
                color = MaterialTheme.colorScheme.error,
            )

            colors.isEmpty() -> Text(
                text = stringResource(R.string.color_extract_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> {
                Text(
                    text = stringResource(R.string.color_extracted),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                colors.forEach { color ->
                    val composeColor = Color(color.red, color.green, color.blue)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(composeColor)
                            .clickable {
                                copyToClipboard(context, color.hex())
                                scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                            }
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = color.hex(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (composeColor.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = color.rgbString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (composeColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.65f) else {
                                Color.White.copy(alpha = 0.75f)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FontPreviewTool() {
    var sample by rememberSaveable { mutableStateOf("") }
    var familyIndex by rememberSaveable { mutableIntStateOf(0) }
    var size by rememberSaveable { mutableFloatStateOf(24f) }
    val defaultSample = stringResource(R.string.color_font_sample_text)
    val text = sample.ifBlank { defaultSample }
    val families = listOf(
        stringResource(R.string.color_font_sans) to FontFamily.SansSerif,
        stringResource(R.string.color_font_serif) to FontFamily.Serif,
        stringResource(R.string.color_font_mono) to FontFamily.Monospace,
        stringResource(R.string.color_font_cursive) to FontFamily.Cursive,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = sample,
            onValueChange = { sample = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            label = { Text(stringResource(R.string.color_font_sample)) },
            placeholder = { Text(defaultSample) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.color_font_family),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            families.forEachIndexed { index, (label, _) ->
                FilterChip(
                    selected = familyIndex == index,
                    onClick = { familyIndex = index },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.color_font_size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(72.dp),
            )
            Slider(
                value = size,
                onValueChange = { size = it },
                valueRange = 12f..48f,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${size.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.End,
            )
        }
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = size.sp,
                        fontFamily = families[familyIndex].second,
                        lineHeight = (size * 1.55f).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = size.sp,
                        fontFamily = families[familyIndex].second,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (size * 1.55f).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ColorCodeCard(rows: List<Pair<String, String>>, language: ir.amiraligrr.damdast.data.AppLanguage) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(60.dp),
                    )
                    SelectionContainer(Modifier.weight(1f)) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(
                        onClick = {
                            copyToClipboard(context, value)
                            scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                        },
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.common_copy),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}
