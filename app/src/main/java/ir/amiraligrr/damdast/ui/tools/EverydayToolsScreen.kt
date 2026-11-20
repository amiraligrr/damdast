package ir.amiraligrr.damdast.ui.tools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddToHomeScreen
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.DamdastViewModel
import ir.amiraligrr.damdast.data.NoteItem
import ir.amiraligrr.damdast.data.TaskItem
import ir.amiraligrr.damdast.tools.ToolCatalog
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.EmptyState
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolIcon
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.SecurityUtils
import ir.amiraligrr.damdast.util.ShortcutHelper
import ir.amiraligrr.damdast.util.copyToClipboard
import ir.amiraligrr.damdast.util.formatDateTime
import ir.amiraligrr.damdast.util.formatDecimal
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.localizeDigits
import ir.amiraligrr.damdast.util.normalizeDigits
import ir.amiraligrr.damdast.util.shareText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID

@Composable
fun EverydayToolsScreen(
    tool: ToolDefinition,
    viewModel: DamdastViewModel,
    notes: List<NoteItem>,
    tasks: List<TaskItem>,
    onBack: () -> Unit,
) {
    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        when (tool.id) {
            "password_generator" -> PasswordTool()
            "random_number" -> RandomNumberTool()
            "random_selector" -> RandomSelectorTool()
            "dice" -> DiceTool()
            "coin_flip" -> CoinTool()
            "notes" -> NotesTool(notes, viewModel)
            "tasks" -> TasksTool(tasks, viewModel)
            "shortcuts" -> ShortcutsTool()
        }
    }
}

@Composable
private fun PasswordTool() {
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var length by rememberSaveable { mutableIntStateOf(20) }
    var upper by rememberSaveable { mutableStateOf(true) }
    var lower by rememberSaveable { mutableStateOf(true) }
    var digits by rememberSaveable { mutableStateOf(true) }
    var symbols by rememberSaveable { mutableStateOf(true) }
    var excludeAmbiguous by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var entropy by rememberSaveable { mutableStateOf(0.0) }
    var error by rememberSaveable { mutableStateOf(false) }
    val canGenerate = upper || lower || digits || symbols

    fun generate() {
        runCatching {
            SecurityUtils.generatePassword(
                length = length,
                useUpper = upper,
                useLower = lower,
                useDigits = digits,
                useSymbols = symbols,
                excludeAmbiguous = excludeAmbiguous,
            )
        }.onSuccess {
            password = it.value
            entropy = it.entropyBits
            error = false
        }.onFailure {
            error = true
        }
    }
    LaunchedEffect(Unit) { if (password.isEmpty()) generate() }
    val strengthLabel = when {
        entropy < 40 -> stringResource(R.string.everyday_strength_weak)
        entropy < 60 -> stringResource(R.string.everyday_strength_medium)
        entropy < 80 -> stringResource(R.string.everyday_strength_strong)
        else -> stringResource(R.string.everyday_strength_very_strong)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(16.dp)) {
                SelectionContainer {
                    Text(
                        text = password.ifEmpty { "…" },
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (entropy / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(6.dp))
                Row {
                    Text(
                        text = strengthLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            R.string.everyday_entropy_bits,
                            formatDecimal(entropy, language, 0),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            copyToClipboard(context, password)
                            scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                        },
                        enabled = password.isNotEmpty(),
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.common_copy),
                        )
                    }
                    IconButton(
                        onClick = { shareText(context, password) },
                        enabled = password.isNotEmpty(),
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.common_share),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.everyday_password_length) + " · " +
                formatInt(length, language),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = length.toFloat(),
            onValueChange = { length = it.toInt().coerceIn(8, 64) },
            valueRange = 8f..64f,
            steps = 55,
        )
        CheckOption(upper, { upper = it }, stringResource(R.string.everyday_uppercase))
        CheckOption(lower, { lower = it }, stringResource(R.string.everyday_lowercase))
        CheckOption(digits, { digits = it }, stringResource(R.string.everyday_digits))
        CheckOption(symbols, { symbols = it }, stringResource(R.string.everyday_symbols))
        CheckOption(excludeAmbiguous, { excludeAmbiguous = it }, stringResource(R.string.everyday_exclude_ambiguous))
        Spacer(Modifier.height(10.dp))
        DamdastButton(
            text = stringResource(R.string.everyday_generate_password),
            onClick = ::generate,
            enabled = canGenerate,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_invalid_input),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.everyday_password_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CheckOption(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RandomNumberTool() {
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var min by rememberSaveable { mutableStateOf("1") }
    var max by rememberSaveable { mutableStateOf("100") }
    var count by rememberSaveable { mutableIntStateOf(5) }
    var unique by rememberSaveable { mutableStateOf(true) }
    var results by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var error by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(min, { min = it }, stringResource(R.string.everyday_min), Modifier.weight(1f))
            NumberField(max, { max = it }, stringResource(R.string.everyday_max), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.everyday_count) + " · " + formatInt(count, language),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = count.toFloat(),
            onValueChange = { count = it.toInt().coerceIn(1, 100) },
            valueRange = 1f..100f,
            steps = 98,
        )
        CheckOption(unique, { unique = it }, stringResource(R.string.everyday_unique_values))
        Spacer(Modifier.height(8.dp))
        DamdastButton(
            text = stringResource(R.string.everyday_generate),
            onClick = {
                val low = min.normalizeDigits().toIntOrNull()
                val high = max.normalizeDigits().toIntOrNull()
                if (low == null || high == null) {
                    error = true
                } else {
                    runCatching { SecurityUtils.randomInts(low, high, count, unique) }
                        .onSuccess {
                            results = it
                            error = false
                        }
                        .onFailure { error = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_invalid_input),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(14.dp))
        if (results.isNotEmpty()) {
            ResultGrid(results, language)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DamdastButton(
                    text = stringResource(R.string.common_copy),
                    onClick = {
                        copyToClipboard(context, results.joinToString(", "))
                        scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                    },
                    style = DamdastButtonStyle.Tonal,
                    modifier = Modifier.weight(1f),
                )
                DamdastButton(
                    text = stringResource(R.string.common_share),
                    onClick = { shareText(context, results.joinToString(", ")) },
                    style = DamdastButtonStyle.Outline,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ResultGrid(values: List<Int>, language: ir.amiraligrr.damdast.data.AppLanguage) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { value ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = formatInt(value, language),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RandomSelectorTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableIntStateOf(1) }
    var unique by rememberSaveable { mutableStateOf(true) }
    var results by rememberSaveable { mutableStateOf(listOf<String>()) }
    var error by rememberSaveable { mutableStateOf(false) }
    val items = remember(input) { input.lines().map { it.trim() }.filter { it.isNotEmpty() } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp),
            label = { Text(stringResource(R.string.everyday_items)) },
            shape = MaterialTheme.shapes.medium,
            colors = everydayFieldColors(),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.everyday_count) + " · " + formatInt(count, LocalAppLanguage.current),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = count.toFloat(),
            onValueChange = { count = it.toInt().coerceIn(1, 20) },
            valueRange = 1f..20f,
            steps = 18,
        )
        CheckOption(unique, { unique = it }, stringResource(R.string.everyday_unique_values))
        DamdastButton(
            text = stringResource(R.string.everyday_pick),
            onClick = {
                runCatching { SecurityUtils.pick(items = items, count = count, unique = unique) }
                    .onSuccess {
                        results = it
                        error = false
                    }
                    .onFailure { error = true }
            },
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_invalid_input),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(14.dp))
        if (results.isNotEmpty()) {
            results.forEach { value ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 7.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DamdastButton(
                    text = stringResource(R.string.common_copy),
                    onClick = {
                        copyToClipboard(context, results.joinToString("\n"))
                        scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
                    },
                    style = DamdastButtonStyle.Tonal,
                    modifier = Modifier.weight(1f),
                )
                DamdastButton(
                    text = stringResource(R.string.common_share),
                    onClick = { shareText(context, results.joinToString("\n")) },
                    style = DamdastButtonStyle.Outline,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DiceTool() {
    val language = LocalAppLanguage.current
    var sides by rememberSaveable { mutableIntStateOf(6) }
    var count by rememberSaveable { mutableIntStateOf(2) }
    var values by rememberSaveable { mutableStateOf(listOf<Int>()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(stringResource(R.string.everyday_dice_sides), style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(4, 6, 8, 10, 12, 20).forEach { side ->
                FilterChip(
                    selected = sides == side,
                    onClick = { sides = side },
                    label = { Text("d$side") },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.everyday_dice_count) + " · " + formatInt(count, language),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = count.toFloat(),
            onValueChange = { count = it.toInt().coerceIn(1, 6) },
            valueRange = 1f..6f,
            steps = 4,
        )
        DamdastButton(
            text = stringResource(R.string.everyday_roll),
            onClick = { values = SecurityUtils.rollDice(sides, count) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        if (values.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                values.forEach { value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(92.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = formatInt(value, language),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinTool() {
    val language = LocalAppLanguage.current
    val random = remember { SecureRandom() }
    val scope = rememberCoroutineScope()
    var heads by rememberSaveable { mutableIntStateOf(0) }
    var tails by rememberSaveable { mutableIntStateOf(0) }
    var result by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var flipping by rememberSaveable { mutableStateOf(false) }
    var turn by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = turn,
        animationSpec = tween(550),
        label = "coin-rotation",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(180.dp)
                .rotate(rotation)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when {
                    flipping -> "…"
                    result == true -> stringResource(R.string.everyday_heads)
                    result == false -> stringResource(R.string.everyday_tails)
                    else -> "?"
                },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(20.dp))
        DamdastButton(
            text = stringResource(if (flipping) R.string.everyday_flipping else R.string.everyday_coin_flip),
            onClick = {
                if (!flipping) {
                    scope.launch {
                        flipping = true
                        turn += 1080f
                        delay(560)
                        val headsResult = random.nextBoolean()
                        result = headsResult
                        if (headsResult) heads++ else tails++
                        flipping = false
                    }
                }
            },
            enabled = !flipping,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(
                R.string.everyday_coin_stats,
                formatInt(heads, language),
                formatInt(tails, language),
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotesTool(notes: List<NoteItem>, viewModel: DamdastViewModel) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<NoteItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val language = LocalAppLanguage.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        DamdastButton(
            text = stringResource(R.string.everyday_new_note),
            onClick = {
                editing = null
                showEditor = true
            },
            icon = Icons.Outlined.Add,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        if (notes.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.everyday_no_notes),
                body = stringResource(R.string.everyday_no_notes_body),
            )
        } else {
            notes.forEach { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            editing = note
                            showEditor = true
                        },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = note.title.ifBlank { stringResource(R.string.everyday_note_title) },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(
                                onClick = {
                                    viewModel.deleteNote(note.id)
                                    scope.launch {
                                        snackbar?.showSnackbar(context.getString(R.string.everyday_note_deleted))
                                    }
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.everyday_delete_note),
                                    modifier = Modifier.size(17.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (note.body.isNotBlank()) {
                            Text(
                                text = note.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formatDateTime(note.updatedAt, language),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
    if (showEditor) {
        NoteEditorDialog(
            note = editing,
            onDismiss = { showEditor = false },
            onSave = { note ->
                viewModel.saveNote(note)
                showEditor = false
                scope.launch { snackbar?.showSnackbar(context.getString(R.string.everyday_note_saved)) }
            },
        )
    }
}

@Composable
private fun NoteEditorDialog(
    note: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit,
) {
    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by rememberSaveable(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(
                        if (note == null) R.string.everyday_new_note else R.string.everyday_edit_note,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.everyday_note_title)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp),
                    label = { Text(stringResource(R.string.everyday_note_body)) },
                    shape = MaterialTheme.shapes.small,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamdastButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = onDismiss,
                        style = DamdastButtonStyle.Ghost,
                        modifier = Modifier.weight(1f),
                    )
                    DamdastButton(
                        text = stringResource(R.string.common_save),
                        onClick = {
                            if (title.isNotBlank() || body.isNotBlank()) {
                                onSave(
                                    NoteItem(
                                        id = note?.id ?: UUID.randomUUID().toString(),
                                        title = title.trim(),
                                        body = body.trim(),
                                        updatedAt = System.currentTimeMillis(),
                                    ),
                                )
                            }
                        },
                        enabled = title.isNotBlank() || body.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TasksTool(tasks: List<TaskItem>, viewModel: DamdastViewModel) {
    val language = LocalAppLanguage.current
    var input by rememberSaveable { mutableStateOf("") }
    val remaining = tasks.count { !it.done }
    fun addTask() {
        if (input.isNotBlank()) {
            viewModel.saveTask(
                TaskItem(
                    id = UUID.randomUUID().toString(),
                    text = input.trim(),
                    done = false,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            input = ""
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.everyday_task_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addTask() }),
                colors = everydayFieldColors(),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { addTask() },
                enabled = input.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.everyday_add_task),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.everyday_tasks_remaining,
                    formatInt(remaining, language),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (tasks.any { it.done }) {
                TextButton(onClick = { viewModel.clearCompletedTasks() }) {
                    Text(stringResource(R.string.everyday_clear_completed))
                }
            }
        }
        if (tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Checklist,
                title = stringResource(R.string.everyday_no_tasks),
                body = stringResource(R.string.everyday_no_tasks_body),
            )
        } else {
            tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = task.done,
                        onCheckedChange = { viewModel.toggleTask(task.id) },
                    )
                    Text(
                        text = task.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.done) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (task.done) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { viewModel.deleteTask(task.id) },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.everyday_delete_task),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutsTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val language = LocalAppLanguage.current
    val shortcutIds = listOf("qr_scanner", "calculator", "notes", "tasks")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.everyday_shortcuts_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        shortcutIds.mapNotNull { ToolCatalog.byId[it] }.forEach { definition ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolIcon(icon = definition.icon, accent = definition.accent, size = 38.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(definition.nameRes),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(
                            when (definition.id) {
                                "qr_scanner" -> R.string.everyday_shortcut_qr_desc
                                "calculator" -> R.string.everyday_shortcut_calculator_desc
                                "notes" -> R.string.everyday_shortcut_notes_desc
                                else -> R.string.everyday_shortcut_tasks_desc
                            },
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = {
                        val ok = ShortcutHelper.requestPinnedShortcut(
                            context,
                            definition.id,
                            context.getString(definition.nameRes),
                        )
                        scope.launch {
                            snackbar?.showSnackbar(
                                context.getString(
                                    if (ok) {
                                        R.string.everyday_shortcut_requested
                                    } else {
                                        R.string.everyday_shortcut_unsupported
                                    },
                                ),
                            )
                        }
                    },
                ) {
                    Icon(
                        Icons.Outlined.AddToHomeScreen,
                        contentDescription = stringResource(R.string.everyday_pin_shortcut),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() || char == '-' }.take(10)) },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.small,
        colors = everydayFieldColors(),
    )
}

@Composable
private fun everydayFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
)
