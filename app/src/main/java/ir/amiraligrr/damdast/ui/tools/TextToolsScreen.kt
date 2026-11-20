package ir.amiraligrr.damdast.ui.tools

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.TextTransforms
import ir.amiraligrr.damdast.util.copyToClipboard
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.shareText
import ir.amiraligrr.damdast.util.toast
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun TextToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHostState.current
    fun message(text: String) {
        if (snackbar != null) {
            scope.launch { snackbar.showSnackbar(text) }
        } else {
            toast(context, text)
        }
    }

    val onCopy: (String) -> Unit = { value ->
        copyToClipboard(context, value)
        message(context.getString(R.string.common_copied))
    }
    val onShare: (String) -> Unit = { value ->
        if (!shareText(context, value)) message(context.getString(R.string.error_share_failed))
    }
    val onError: (String) -> Unit = { message(it) }

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
            Spacer(Modifier.height(18.dp))
            when (tool.id) {
                "text_counter" -> CounterTool()
                "case_converter" -> CaseTool(onCopy, onShare, onError)
                "remove_duplicates" -> DedupeTool(onCopy, onShare, onError)
                "sort_lines" -> SortTool(onCopy, onShare, onError)
                "reverse_text" -> ReverseTool(onCopy, onShare, onError)
                "whitespace" -> WhitespaceTool(onCopy, onShare, onError)
                "base64" -> EncodeDecodeTool(mode = "base64", onCopy, onShare, onError)
                "url_encoder" -> EncodeDecodeTool(mode = "url", onCopy, onShare, onError)
                "html_entities" -> EncodeDecodeTool(mode = "html", onCopy, onShare, onError)
                "text_compare" -> CompareTool()
                "text_extract" -> ExtractTool(onCopy, onShare)
                "lorem_ipsum" -> LoremTool(onCopy, onShare)
            }
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.text_processed_locally),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ToolTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    modifier: Modifier = Modifier,
    minLines: Int = 7,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            maxLines = 16,
            placeholder = {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            },
            shape = MaterialTheme.shapes.medium,
            colors = textFieldColors(),
        )
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
private fun ResultCard(
    text: String,
    label: String = stringResource(R.string.text_output_label),
    error: String? = null,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onUseAsInput: (() -> Unit)? = null,
    maxHeight: Int = 330,
) {
    val hasText = text.isNotBlank()
    Column(Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (error != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onCopy(text) },
                    enabled = hasText,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.common_copy),
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = { onShare(text) },
                    enabled = hasText,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.common_share),
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (onUseAsInput != null) {
                    IconButton(
                        onClick = onUseAsInput,
                        enabled = hasText,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Outlined.SwapVert,
                            contentDescription = stringResource(R.string.text_swap),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp, max = maxHeight.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
            ) {
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (hasText) {
                    SelectionContainer {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.text_output_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun CheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CounterTool() {
    val language = LocalAppLanguage.current
    var input by rememberSaveable { mutableStateOf("") }
    val stats = remember(input) { TextTransforms.stats(input) }
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
        minLines = 9,
    )
    Spacer(Modifier.height(14.dp))
    if (input.isEmpty()) {
        Text(
            text = stringResource(R.string.text_stats_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(8.dp))
    val statsItems = listOf(
        formatInt(stats.words, language) to stringResource(R.string.text_words_label),
        formatInt(stats.characters, language) to stringResource(R.string.text_characters_label),
        formatInt(stats.charactersNoSpaces, language) to stringResource(R.string.text_characters_no_spaces_label),
        formatInt(stats.lines, language) to stringResource(R.string.text_lines_label),
        formatInt(stats.paragraphs, language) to stringResource(R.string.text_paragraphs_label),
        formatInt(stats.sentences, language) to stringResource(R.string.text_sentences_label),
    )
    statsItems.chunked(2).forEach { pair ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            pair.forEach { (value, label) ->
                StatTile(value = value, label = label, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.shapes.small,
            )
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.text_reading_time_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.text_reading_minutes, formatInt(stats.readingMinutes, language)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(horizontal = 13.dp, vertical = 12.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CaseTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onError: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    val options = listOf(
        stringResource(R.string.text_case_upper),
        stringResource(R.string.text_case_lower),
        stringResource(R.string.text_case_title),
        stringResource(R.string.text_case_sentence),
    )
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
    )
    Spacer(Modifier.height(12.dp))
    ChipRow(options = options, selectedIndex = mode, onSelect = { mode = it })
    Spacer(Modifier.height(12.dp))
    DamdastButton(
        text = stringResource(R.string.text_convert),
        enabled = input.isNotBlank(),
        onClick = {
            output = when (mode) {
                0 -> TextTransforms.upper(input)
                1 -> TextTransforms.lower(input)
                2 -> TextTransforms.titleCase(input)
                else -> TextTransforms.sentenceCase(input)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(
        text = output,
        onCopy = onCopy,
        onShare = onShare,
        onUseAsInput = if (output.isNotBlank()) {
            { input = output }
        } else {
            null
        },
    )
}

@Composable
private fun DedupeTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onError: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    var trim by rememberSaveable { mutableStateOf(true) }
    var ignoreCase by rememberSaveable { mutableStateOf(false) }
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
    )
    Spacer(Modifier.height(10.dp))
    CheckRow(trim, { trim = it }, stringResource(R.string.text_dedupe_trim))
    CheckRow(ignoreCase, { ignoreCase = it }, stringResource(R.string.text_dedupe_ignore_case))
    Spacer(Modifier.height(10.dp))
    DamdastButton(
        text = stringResource(R.string.text_clean),
        enabled = input.isNotBlank(),
        onClick = {
            output = TextTransforms.removeDuplicateLines(input, trim, ignoreCase)
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(output, onCopy = onCopy, onShare = onShare)
}

@Composable
private fun SortTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onError: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var ignoreCase by rememberSaveable { mutableStateOf(true) }
    var removeDuplicates by rememberSaveable { mutableStateOf(false) }
    var removeEmpty by rememberSaveable { mutableStateOf(true) }
    val options = listOf(
        stringResource(R.string.text_sort_az),
        stringResource(R.string.text_sort_za),
        stringResource(R.string.text_sort_numeric),
        stringResource(R.string.text_sort_length),
    )
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
    )
    Spacer(Modifier.height(12.dp))
    ChipRow(options = options, selectedIndex = mode, onSelect = { mode = it })
    Spacer(Modifier.height(6.dp))
    CheckRow(ignoreCase, { ignoreCase = it }, stringResource(R.string.text_sort_ignore_case))
    CheckRow(removeDuplicates, { removeDuplicates = it }, stringResource(R.string.text_sort_remove_duplicates))
    CheckRow(removeEmpty, { removeEmpty = it }, stringResource(R.string.text_sort_remove_empty))
    Spacer(Modifier.height(10.dp))
    DamdastButton(
        text = stringResource(R.string.common_apply),
        enabled = input.isNotBlank(),
        onClick = {
            output = TextTransforms.sortLines(
                text = input,
                mode = TextTransforms.SortMode.entries[mode],
                ignoreCase = ignoreCase,
                removeDuplicates = removeDuplicates,
                removeEmpty = removeEmpty,
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(output, onCopy = onCopy, onShare = onShare)
}

@Composable
private fun ReverseTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onError: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
    )
    Spacer(Modifier.height(12.dp))
    ChipRow(
        options = listOf(
            stringResource(R.string.text_reverse_characters),
            stringResource(R.string.text_reverse_lines),
        ),
        selectedIndex = mode,
        onSelect = { mode = it },
    )
    Spacer(Modifier.height(12.dp))
    DamdastButton(
        text = stringResource(R.string.text_convert),
        enabled = input.isNotBlank(),
        onClick = {
            output = if (mode == 0) TextTransforms.reverse(input) else TextTransforms.reverseLines(input)
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(output, onCopy = onCopy, onShare = onShare)
}

@Composable
private fun WhitespaceTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onError: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
    )
    Spacer(Modifier.height(12.dp))
    DamdastButton(
        text = stringResource(R.string.text_clean),
        enabled = input.isNotBlank(),
        onClick = { output = TextTransforms.cleanWhitespace(input) },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(output, onCopy = onCopy, onShare = onShare)
}

@Composable
private fun EncodeDecodeTool(
    mode: String,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onError: (String) -> Unit,
) {
    var input by rememberSaveable(mode) { mutableStateOf("") }
    var output by rememberSaveable(mode) { mutableStateOf("") }
    var error by rememberSaveable(mode) { mutableStateOf<String?>(null) }
    var encode by rememberSaveable(mode) { mutableStateOf(true) }
    val invalidBase64 = stringResource(R.string.text_base64_invalid)
    val invalidUrl = stringResource(R.string.text_url_invalid)
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
    )
    Spacer(Modifier.height(12.dp))
    ChipRow(
        options = listOf(
            stringResource(R.string.text_mode_encode),
            stringResource(R.string.text_mode_decode),
        ),
        selectedIndex = if (encode) 0 else 1,
        onSelect = { encode = it == 0 },
    )
    Spacer(Modifier.height(12.dp))
    DamdastButton(
        text = if (encode) stringResource(R.string.text_mode_encode) else stringResource(R.string.text_mode_decode),
        enabled = input.isNotBlank(),
        onClick = {
            error = null
            output = runCatching {
                when (mode) {
                    "base64" -> if (encode) {
                        TextTransforms.base64Encode(input)
                    } else {
                        TextTransforms.base64Decode(input)
                    }

                    "url" -> if (encode) TextTransforms.urlEncode(input) else TextTransforms.urlDecode(input)
                    else -> if (encode) TextTransforms.htmlEncode(input) else TextTransforms.htmlDecode(input)
                }
            }.onFailure {
                error = if (mode == "base64") invalidBase64 else invalidUrl
            }.getOrDefault("")
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(
        text = output,
        error = error,
        onCopy = onCopy,
        onShare = onShare,
    )
}

@Composable
private fun CompareTool() {
    var left by rememberSaveable { mutableStateOf("") }
    var right by rememberSaveable { mutableStateOf("") }
    val diff = remember(left, right) {
        if (left.isEmpty() && right.isEmpty()) emptyList() else TextTransforms.diffLines(left, right)
    }
    ToolTextField(
        value = left,
        onValueChange = { left = it },
        label = stringResource(R.string.text_compare_left),
        hint = stringResource(R.string.text_input_hint),
        minLines = 5,
    )
    Spacer(Modifier.height(12.dp))
    ToolTextField(
        value = right,
        onValueChange = { right = it },
        label = stringResource(R.string.text_compare_right),
        hint = stringResource(R.string.text_input_hint),
        minLines = 5,
    )
    Spacer(Modifier.height(14.dp))
    if (diff.isNotEmpty()) {
        val hasDifferences = diff.any { it.kind != TextTransforms.DiffKind.SAME }
        Text(
            text = stringResource(
                if (hasDifferences) R.string.text_compare_different else R.string.text_compare_identical,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = if (hasDifferences) MaterialTheme.colorScheme.primary else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(Modifier.height(8.dp))
        if (hasDifferences) {
            DiffLegend()
            Spacer(Modifier.height(8.dp))
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
            ) {
                diff.forEach { line ->
                    val background = when (line.kind) {
                        TextTransforms.DiffKind.ADDED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        TextTransforms.DiffKind.REMOVED -> MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                        TextTransforms.DiffKind.SAME -> Color.Transparent
                    }
                    val prefix = when (line.kind) {
                        TextTransforms.DiffKind.ADDED -> "+"
                        TextTransforms.DiffKind.REMOVED -> "−"
                        TextTransforms.DiffKind.SAME -> " "
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(background)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                        )
                        Spacer(Modifier.width(7.dp))
                        SelectionContainer {
                            Text(
                                text = line.text.ifEmpty { " " },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiffLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.text_diff_removed),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.text_diff_added),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExtractTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var unique by rememberSaveable { mutableStateOf(true) }
    val results = remember(input, type, unique) {
        val raw = when (type) {
            0 -> TextTransforms.extractUrls(input)
            1 -> TextTransforms.extractEmails(input)
            else -> TextTransforms.extractNumbers(input)
        }
        if (unique) raw.distinct() else raw
    }
    val language = LocalAppLanguage.current
    ToolTextField(
        value = input,
        onValueChange = { input = it },
        label = stringResource(R.string.text_input_label),
        hint = stringResource(R.string.text_input_hint),
        minLines = 6,
    )
    Spacer(Modifier.height(12.dp))
    ChipRow(
        options = listOf(
            stringResource(R.string.text_extract_urls),
            stringResource(R.string.text_extract_emails),
            stringResource(R.string.text_extract_numbers),
        ),
        selectedIndex = type,
        onSelect = { type = it },
    )
    Spacer(Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { unique = !unique }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(checked = unique, onCheckedChange = { unique = it })
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.text_extract_unique),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.text_found_count, formatInt(results.size, language)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (results.isNotEmpty()) {
            androidx.compose.material3.TextButton(onClick = { onCopy(results.joinToString("\n")) }) {
                Text(stringResource(R.string.text_output_copy_all))
            }
            IconButton(onClick = { onShare(results.joinToString("\n")) }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.common_share),
                )
            }
        }
    }
    if (results.isEmpty()) {
        Text(
            text = stringResource(R.string.text_output_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
            ) {
                results.forEach { value ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionContainer(Modifier.weight(1f)) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(
                            onClick = { onCopy(value) },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = stringResource(R.string.common_copy),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoremTool(
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) {
    var paragraphs by rememberSaveable { mutableIntStateOf(3) }
    var sentences by rememberSaveable { mutableIntStateOf(4) }
    var output by rememberSaveable { mutableStateOf("") }
    Text(
        text = stringResource(R.string.text_lorem_paragraphs),
        style = MaterialTheme.typography.labelLarge,
    )
    Slider(
        value = paragraphs.toFloat(),
        onValueChange = { paragraphs = it.toInt().coerceIn(1, 10) },
        valueRange = 1f..10f,
        steps = 8,
    )
    Text(
        text = stringResource(R.string.text_lorem_sentences),
        style = MaterialTheme.typography.labelLarge,
    )
    Slider(
        value = sentences.toFloat(),
        onValueChange = { sentences = it.toInt().coerceIn(1, 12) },
        valueRange = 1f..12f,
        steps = 10,
    )
    Spacer(Modifier.height(8.dp))
    DamdastButton(
        text = stringResource(R.string.text_generate),
        onClick = {
            output = TextTransforms.loremIpsum(paragraphs, sentences, Random.Default)
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    ResultCard(output, onCopy = onCopy, onShare = onShare, maxHeight = 420)
    Spacer(Modifier.height(8.dp))
    DamdastButton(
        text = stringResource(R.string.text_clear_input),
        onClick = { output = "" },
        style = DamdastButtonStyle.Ghost,
        modifier = Modifier.fillMaxWidth(),
    )
}
