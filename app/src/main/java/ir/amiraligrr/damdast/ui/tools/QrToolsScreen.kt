package ir.amiraligrr.damdast.ui.tools

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.DamdastViewModel
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.EmptyState
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.CodeGenerator
import ir.amiraligrr.damdast.util.ImageUtils
import ir.amiraligrr.damdast.util.copyToClipboard
import ir.amiraligrr.damdast.util.formatDateTime
import ir.amiraligrr.damdast.util.openUrl
import ir.amiraligrr.damdast.util.shareFile
import ir.amiraligrr.damdast.util.shareText
import ir.amiraligrr.damdast.util.sharedCacheFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

private data class ScanResultData(val value: String, val format: String)

@Composable
fun QrToolsScreen(tool: ToolDefinition, viewModel: DamdastViewModel, onBack: () -> Unit) {
    when (tool.id) {
        "qr_scanner", "barcode_scanner" -> ScannerTool(
            requestCamera = true,
            galleryOnly = false,
            viewModel = viewModel,
            onBack = onBack,
        )

        "scan_gallery" -> ScannerTool(
            requestCamera = false,
            galleryOnly = true,
            viewModel = viewModel,
            onBack = onBack,
        )

        "scan_history" -> ScanHistoryTool(viewModel = viewModel, onBack = onBack)
        else -> GeneratorTool(tool = tool, onBack = onBack)
    }
}

@Composable
private fun ScannerTool(
    requestCamera: Boolean,
    galleryOnly: Boolean,
    viewModel: DamdastViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var result by remember { mutableStateOf<ScanResultData?>(null) }
    var decoding by remember { mutableStateOf(false) }
    var decodeError by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf(false) }
    var torch by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        BarcodeAnalyzer { value, format ->
            if (result == null) {
                result = ScanResultData(value, format)
                viewModel.addScan(value, format)
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted = it }

    fun decodeGallery(uri: Uri) {
        scope.launch {
            decoding = true
            decodeError = false
            val bitmap = withContext(Dispatchers.IO) { ImageUtils.loadBitmap(context, uri, 2048) }
            if (bitmap == null) {
                decodeError = true
                decoding = false
                return@launch
            }
            val found = withContext(Dispatchers.Default) { decodeBitmap(bitmap) }
            bitmap.recycle()
            if (found == null) {
                decodeError = true
            } else {
                result = found
                viewModel.addScan(found.value, found.format)
            }
            decoding = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) decodeGallery(uri)
    }

    LaunchedEffect(requestCamera) {
        if (requestCamera && !granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(galleryOnly) {
        if (galleryOnly && result == null) {
            galleryLauncher.launch("image/*")
        }
    }

    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    if (requestCamera && granted) {
        LaunchedEffect(previewView, granted) {
            runCatching {
                val provider = context.cameraProvider()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }
                provider.unbindAll()
                cameraProvider = provider
                val bound = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
                camera = bound
                cameraError = false
            }.onFailure { cameraError = true }
        }
        LaunchedEffect(torch, camera) {
            camera?.cameraControl?.enableTorch(torch)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { cameraProvider?.unbindAll() }
            executor.shutdown()
            analyzer.unlock()
        }
    }

    ToolPage(
        title = stringResource(if (galleryOnly) R.string.tool_scan_gallery else R.string.tool_qr_scanner),
        onBack = onBack,
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (requestCamera && granted) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                )
                Canvas(Modifier.fillMaxSize()) {
                    val side = minOf(size.width, size.height) * 0.68f
                    val left = (size.width - side) / 2
                    val top = (size.height - side) / 2
                    drawRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(side, side),
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }
                Text(
                    text = stringResource(R.string.scan_instruction),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RoundCameraAction(
                        icon = if (torch) Icons.Outlined.FlashOff else Icons.Outlined.FlashOn,
                        description = stringResource(
                            if (torch) R.string.scan_torch_off else R.string.scan_torch_on,
                        ),
                        onClick = { torch = !torch },
                    )
                    RoundCameraAction(
                        icon = Icons.Outlined.PhotoLibrary,
                        description = stringResource(R.string.scan_gallery),
                        onClick = { galleryLauncher.launch("image/*") },
                    )
                }
                if (cameraError) {
                    ScannerError(
                        title = stringResource(R.string.scan_camera_error),
                        onGallery = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                    )
                }
            } else if (galleryOnly || !requestCamera) {
                GalleryScanPanel(
                    decoding = decoding,
                    error = decodeError,
                    hasResult = result != null,
                    onPick = { galleryLauncher.launch("image/*") },
                    onBack = onBack,
                )
            } else {
                CameraPermissionPanel(
                    granted = granted,
                    onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onOpenSettings = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}"),
                            ),
                        )
                    },
                    onGallery = { galleryLauncher.launch("image/*") },
                )
            }

            if (decoding) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.scan_decoding),
                            color = Color.White,
                        )
                    }
                }
            }

            result?.let { scan ->
                ScanResultCard(
                    result = scan,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(14.dp),
                    onCopy = {
                        copyToClipboard(context, scan.value)
                        scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                    },
                    onShare = { shareText(context, scan.value) },
                    onOpen = {
                        if (!openUrl(context, scan.value)) {
                            scope.launch {
                                snackbar?.showSnackbar(context.getString(R.string.scan_no_link))
                            }
                        }
                    },
                    onAgain = {
                        result = null
                        decodeError = false
                        analyzer.unlock()
                        if (galleryOnly) galleryLauncher.launch("image/*")
                    },
                    openEnabled = scan.value.startsWith("http://") || scan.value.startsWith("https://"),
                )
            }

            if (decodeError && result == null) {
                ErrorBanner(
                    text = stringResource(R.string.scan_no_code_found),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun RoundCameraAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f)),
    ) {
        Icon(icon, contentDescription = description, tint = Color.White)
    }
}

@Composable
private fun CameraPermissionPanel(
    granted: Boolean,
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
    onGallery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(
                if (granted) R.string.scan_camera_error else R.string.scan_camera_permission_title,
            ),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (granted) R.string.scan_camera_error else R.string.scan_camera_permission_body,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        DamdastButton(
            text = stringResource(R.string.scan_grant_camera),
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        DamdastButton(
            text = stringResource(R.string.scan_gallery),
            onClick = onGallery,
            style = DamdastButtonStyle.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        DamdastButton(
            text = stringResource(R.string.scan_open_settings),
            onClick = onOpenSettings,
            style = DamdastButtonStyle.Ghost,
        )
    }
}

@Composable
private fun GalleryScanPanel(
    decoding: Boolean,
    error: Boolean,
    hasResult: Boolean,
    onPick: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.tool_scan_gallery_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (error) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.scan_no_code_found),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Spacer(Modifier.height(18.dp))
        DamdastButton(
            text = stringResource(R.string.common_pick_image),
            onClick = onPick,
            enabled = !decoding,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ScannerError(title: String, onGallery: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            DamdastButton(
                text = stringResource(R.string.scan_gallery),
                onClick = onGallery,
                style = DamdastButtonStyle.Tonal,
            )
        }
    }
}

@Composable
private fun ErrorBanner(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
    )
}

@Composable
private fun ScanResultCard(
    result: ScanResultData,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onAgain: () -> Unit,
    openEnabled: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.scan_result),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = result.value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = result.format,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.common_copy),
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.common_share),
                    )
                }
                if (openEnabled) {
                    IconButton(onClick = onOpen) {
                        Icon(
                            Icons.Outlined.OpenInNew,
                            contentDescription = stringResource(R.string.history_open_scan),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onAgain) {
                    Text(stringResource(R.string.scan_again))
                }
            }
        }
    }
}

@Composable
private fun GeneratorTool(tool: ToolDefinition, onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val mode = when (tool.id) {
        "wifi_qr" -> "wifi"
        "contact_qr" -> "contact"
        "barcode_generator" -> "barcode"
        else -> "qr"
    }
    var qrType by rememberSaveable { mutableIntStateOf(0) }
    var content by rememberSaveable { mutableStateOf("") }
    var ssid by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var security by rememberSaveable { mutableIntStateOf(1) }
    var hidden by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var organization by rememberSaveable { mutableStateOf("") }
    var barcodeFormat by rememberSaveable { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generatedContent by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
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
                    context.getString(if (ok) R.string.gen_saved else R.string.gen_save_failed),
                )
            }
        }
    }
    val securityLabels = listOf(
        stringResource(R.string.gen_security_none),
        stringResource(R.string.gen_security_wpa),
        stringResource(R.string.gen_security_wep),
    )
    val barcodeFormats = listOf(
        "CODE_128" to BarcodeFormat.CODE_128,
        "EAN_13" to BarcodeFormat.EAN_13,
        "CODE_39" to BarcodeFormat.CODE_39,
        "UPC_A" to BarcodeFormat.UPC_A,
    )
    val emptyMessage = stringResource(R.string.gen_empty)
    val invalidEan = stringResource(R.string.gen_invalid_ean)

    fun payload(): String? = when (mode) {
        "wifi" -> if (ssid.isBlank()) null else CodeGenerator.wifiPayload(
            ssid = ssid,
            password = password,
            security = when (security) {
                0 -> "None"
                2 -> "WEP"
                else -> "WPA"
            },
            hidden = hidden,
        )

        "contact" -> if (name.isBlank()) null else CodeGenerator.contactPayload(
            name = name,
            phone = phone,
            email = email,
            organization = organization,
        )

        else -> content.ifBlank { null }
    }

    fun generate() {
        val value = payload()
        if (value.isNullOrBlank()) {
            error = emptyMessage
            return
        }
        runCatching {
            if (mode == "barcode") {
                CodeGenerator.barcode(value, barcodeFormats[barcodeFormat].second)
            } else {
                CodeGenerator.qr(value)
            }
        }.onSuccess {
            bitmap = it
            generatedContent = value
            error = null
        }.onFailure {
            error = if (mode == "barcode" && barcodeFormats[barcodeFormat].second == BarcodeFormat.EAN_13) {
                invalidEan
            } else {
                it.message ?: context.getString(R.string.error_invalid_input)
            }
        }
    }

    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            when (mode) {
                "wifi" -> {
                    GeneratorField(ssid, { ssid = it }, stringResource(R.string.gen_wifi_ssid))
                    Spacer(Modifier.height(8.dp))
                    GeneratorField(password, { password = it }, stringResource(R.string.gen_wifi_password))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.gen_wifi_security),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        securityLabels.forEachIndexed { index, label ->
                            FilterChip(
                                selected = security == index,
                                onClick = { security = index },
                                label = { Text(label) },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hidden = !hidden },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(checked = hidden, onCheckedChange = { hidden = it })
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.gen_wifi_hidden))
                    }
                }

                "contact" -> {
                    GeneratorField(name, { name = it }, stringResource(R.string.gen_contact_name))
                    Spacer(Modifier.height(8.dp))
                    GeneratorField(
                        phone,
                        { phone = it },
                        stringResource(R.string.gen_contact_phone),
                        KeyboardType.Phone,
                    )
                    Spacer(Modifier.height(8.dp))
                    GeneratorField(
                        email,
                        { email = it },
                        stringResource(R.string.gen_contact_email),
                        KeyboardType.Email,
                    )
                    Spacer(Modifier.height(8.dp))
                    GeneratorField(
                        organization,
                        { organization = it },
                        stringResource(R.string.gen_contact_org),
                    )
                }

                "barcode" -> {
                    GeneratorField(content, { content = it }, stringResource(R.string.gen_content))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.gen_barcode_format),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        barcodeFormats.forEachIndexed { index, (label, _) ->
                            FilterChip(
                                selected = barcodeFormat == index,
                                onClick = { barcodeFormat = index },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = qrType == 0,
                            onClick = { qrType = 0 },
                            label = { Text(stringResource(R.string.gen_text)) },
                        )
                        FilterChip(
                            selected = qrType == 1,
                            onClick = { qrType = 1 },
                            label = { Text(stringResource(R.string.gen_url)) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    GeneratorField(
                        content,
                        { content = it },
                        stringResource(R.string.gen_content),
                        if (qrType == 1) KeyboardType.Uri else KeyboardType.Text,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            DamdastButton(
                text = stringResource(R.string.gen_generate),
                onClick = ::generate,
                modifier = Modifier.fillMaxWidth(),
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(16.dp))
            bitmap?.let { image ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = stringResource(R.string.gen_ready),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .padding(14.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamdastButton(
                        text = stringResource(R.string.gen_save),
                        onClick = {
                            pendingBitmap = image
                            saveLauncher.launch("damdast-code.png")
                        },
                        style = DamdastButtonStyle.Tonal,
                        modifier = Modifier.weight(1f),
                    )
                    DamdastButton(
                        text = stringResource(R.string.gen_share),
                        onClick = {
                            scope.launch {
                                val file = sharedCacheFile(context, "damdast-code.png")
                                val saved = withContext(Dispatchers.IO) {
                                    ImageUtils.saveBitmap(image, file, Bitmap.CompressFormat.PNG)
                                }
                                if (saved) {
                                    shareFile(context, file, "image/png")
                                }
                            }
                        },
                        style = DamdastButtonStyle.Outline,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                DamdastButton(
                    text = stringResource(R.string.gen_copy_content),
                    onClick = {
                        copyToClipboard(context, generatedContent)
                        scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                    },
                    style = DamdastButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.gen_qr_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GeneratorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun ScanHistoryTool(viewModel: DamdastViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val scans by viewModel.scanHistory.collectAsStateWithLifecycle()
    var showClear by remember { mutableStateOf(false) }
    ToolPage(title = stringResource(R.string.tool_scan_history), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.history_scans),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (scans.isNotEmpty()) {
                    TextButton(onClick = { showClear = true }) {
                        Text(stringResource(R.string.common_clear))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (scans.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.QrCode2,
                    title = stringResource(R.string.scan_history_empty),
                    body = stringResource(R.string.scan_history_empty_body),
                )
            } else {
                scans.forEach { scan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(Modifier.padding(13.dp)) {
                            Text(
                                text = scan.value,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = scan.format + " · " + formatDateTime(scan.createdAt, language),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    copyToClipboard(context, scan.value)
                                    scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                                }) {
                                    Icon(
                                        Icons.Outlined.ContentCopy,
                                        contentDescription = stringResource(R.string.common_copy),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                if (scan.value.startsWith("http")) {
                                    IconButton(onClick = {
                                        if (!openUrl(context, scan.value)) {
                                            scope.launch { snackbar?.showSnackbar(context.getString(R.string.link_open_failed)) }
                                        }
                                    }) {
                                        Icon(
                                            Icons.Outlined.OpenInNew,
                                            contentDescription = stringResource(R.string.history_open_scan),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteScan(scan.id) }) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = stringResource(R.string.history_delete_scan),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            title = { Text(stringResource(R.string.scan_clear_confirm)) },
            text = { Text(stringResource(R.string.scan_clear_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearScanHistory()
                    showClear = false
                }) {
                    Text(stringResource(R.string.common_clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClear = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

private class BarcodeAnalyzer(
    private val onResult: (String, String) -> Unit,
) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.DATA_MATRIX,
                    BarcodeFormat.AZTEC,
                    BarcodeFormat.PDF_417,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.ITF,
                    BarcodeFormat.CODABAR,
                ),
            ),
        )
    }
    private val locked = AtomicBoolean(false)
    private var lastScan = 0L

    fun unlock() {
        locked.set(false)
    }

    override fun analyze(image: ImageProxy) {
        try {
            if (locked.get()) return
            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastScan < 280) return
            lastScan = now
            val plane = image.planes.firstOrNull() ?: return
            val width = image.width
            val height = image.height
            val data = ByteArray(width * height)
            val buffer = plane.buffer
            val rowStride = plane.rowStride
            for (row in 0 until height) {
                val position = row * rowStride
                if (position + width > buffer.limit()) break
                buffer.position(position)
                buffer.get(data, row * width, width)
            }
            val decoded = decodeFrame(data, width, height, image.imageInfo.rotationDegrees)
            if (decoded != null) {
                locked.set(true)
                Handler(Looper.getMainLooper()).post {
                    onResult(decoded.first, decoded.second)
                }
            }
        } catch (_: Exception) {
            // Camera frames are noisy and failures are expected.
        } finally {
            reader.reset()
            image.close()
        }
    }

    private fun decodeFrame(data: ByteArray, width: Int, height: Int, rotation: Int): Pair<String, String>? {
        val rotations = when (rotation) {
            90 -> listOf(90, 0)
            180 -> listOf(180, 0)
            270 -> listOf(270, 0)
            else -> listOf(0)
        }
        for (degrees in rotations) {
            val (pixels, w, h) = rotate(data, width, height, degrees)
            val source = PlanarYUVLuminanceSource(pixels, w, h, 0, 0, w, h, false)
            try {
                val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
                return result.text to result.barcodeFormat.name
            } catch (_: NotFoundException) {
                reader.reset()
            }
        }
        return null
    }

    private fun rotate(data: ByteArray, width: Int, height: Int, degrees: Int): Triple<ByteArray, Int, Int> {
        if (degrees == 0) return Triple(data, width, height)
        val output = ByteArray(width * height)
        if (degrees == 180) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    output[(height - 1 - y) * width + (width - 1 - x)] = data[y * width + x]
                }
            }
            return Triple(output, width, height)
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val source = data[y * width + x]
                if (degrees == 90) {
                    val nx = height - 1 - y
                    val ny = x
                    output[ny * height + nx] = source
                } else {
                    val nx = y
                    val ny = width - 1 - x
                    output[ny * height + nx] = source
                }
            }
        }
        return Triple(output, height, width)
    }
}

private fun decodeBitmap(bitmap: Bitmap): ScanResultData? {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.DATA_MATRIX,
                    BarcodeFormat.AZTEC,
                    BarcodeFormat.PDF_417,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                ),
                DecodeHintType.TRY_HARDER to true,
            ),
        )
    }
    return runCatching {
        val source = RGBLuminanceSource(width, height, pixels)
        val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
        ScanResultData(result.text, result.barcodeFormat.name)
    }.getOrNull()
}

private suspend fun android.content.Context.cameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            { continuation.resume(future.get()) },
            ContextCompat.getMainExecutor(this),
        )
    }
