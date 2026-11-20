package ir.amiraligrr.damdast.ui.tools

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.ArgbColor
import ir.amiraligrr.damdast.util.CodeFormatter
import ir.amiraligrr.damdast.util.ColorUtils
import ir.amiraligrr.damdast.util.DateMath
import ir.amiraligrr.damdast.util.HashUtils
import ir.amiraligrr.damdast.util.JsonUtils
import ir.amiraligrr.damdast.util.JwtUtils
import ir.amiraligrr.damdast.util.UnixPermissions
import ir.amiraligrr.damdast.util.UrlUtils
import ir.amiraligrr.damdast.util.copyToClipboard
import ir.amiraligrr.damdast.util.formatDecimal
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.normalizeDigits
import ir.amiraligrr.damdast.util.shareText
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

@Composable
fun DeveloperToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
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
            Spacer(Modifier.height(16.dp))
            when (tool.id) {
                "json_formatter", "json_validator", "json_minifier" -> JsonTool(tool.id)
                "xml_formatter", "html_formatter", "css_formatter", "js_formatter" -> CodeTool(tool.id)
                "uuid_generator" -> UuidTool()
                "hash_calculator" -> HashTool()
                "timestamp_converter" -> TimestampTool()
                "regex_tester" -> RegexTool()
                "url_parser" -> UrlTool()
                "jwt_decoder" -> JwtTool()
                "unix_permissions" -> UnixPermissionsTool()
                "dev_color_converter" -> DeveloperColorTool()
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun JsonTool(toolId: String) {
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var success by rememberSaveable { mutableStateOf(false) }
    val copied = stringResource(R.string.common_copied)
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    fun message(text: String) {
        scope.launch { snackbar?.showSnackbar(text) }
    }
    fun run(action: String) {
        error = null
        success = false
        if (input.isBlank()) {
            error = context.getString(R.string.dev_empty_input)
            return
        }
        runCatching {
            when (action) {
                "validate" -> {
                    val issue = JsonUtils.validate(input)
                    if (issue != null) throw IllegalArgumentException(issue)
                    success = true
                    output = ""
                }

                "minify" -> output = JsonUtils.minify(input)
                else -> output = JsonUtils.pretty(input)
            }
        }.onFailure {
            error = it.message ?: context.getString(R.string.error_invalid_input)
        }
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        label = { Text(stringResource(R.string.dev_input)) },
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DamdastButton(
            text = stringResource(R.string.dev_pretty),
            onClick = { run("format") },
            style = DamdastButtonStyle.Tonal,
            enabled = input.isNotBlank(),
        )
        DamdastButton(
            text = stringResource(R.string.dev_minify),
            onClick = { run("minify") },
            style = DamdastButtonStyle.Outline,
            enabled = input.isNotBlank(),
        )
        DamdastButton(
            text = stringResource(R.string.dev_validate),
            onClick = { run("validate") },
            style = DamdastButtonStyle.Outline,
            enabled = input.isNotBlank(),
        )
    }
    Spacer(Modifier.height(12.dp))
    if (success) {
        StatusCard(
            title = stringResource(R.string.dev_valid_json),
            text = "",
            positive = true,
        )
    } else if (error != null) {
        StatusCard(
            title = stringResource(R.string.dev_invalid_json),
            text = stringResource(R.string.dev_error_detail, error.orEmpty()),
            positive = false,
        )
    }
    if (output.isNotBlank()) {
        OutputCard(
            title = stringResource(R.string.dev_output),
            text = output,
            onCopy = {
                copyToClipboard(context, output)
                message(copied)
            },
            onShare = { shareText(context, output) },
            monospace = true,
        )
    } else if (toolId == "json_formatter" && error == null && !success) {
        Text(
            text = stringResource(R.string.calc_enter_values),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CodeTool(toolId: String) {
    val kind = when (toolId) {
        "xml_formatter" -> 0
        "html_formatter" -> 1
        "css_formatter" -> 2
        else -> 3
    }
    var input by rememberSaveable(kind) { mutableStateOf("") }
    var output by rememberSaveable(kind) { mutableStateOf("") }
    var error by rememberSaveable(kind) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val note = if (kind <= 1) R.string.dev_markup_note else R.string.dev_code_note
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        label = { Text(stringResource(R.string.dev_input)) },
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(note),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(10.dp))
    DamdastButton(
        text = stringResource(R.string.dev_format),
        onClick = {
            error = null
            if (input.isBlank()) {
                error = context.getString(R.string.dev_empty_input)
            } else {
                output = runCatching {
                    when (kind) {
                        0, 1 -> CodeFormatter.markup(input)
                        2 -> CodeFormatter.css(input)
                        else -> CodeFormatter.javascript(input)
                    }
                }.onFailure {
                    error = it.message ?: context.getString(R.string.error_invalid_input)
                }.getOrDefault("")
            }
        },
        enabled = input.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    if (error != null) {
        StatusCard(
            title = stringResource(R.string.common_error),
            text = error.orEmpty(),
            positive = false,
        )
        Spacer(Modifier.height(8.dp))
    }
    if (output.isNotBlank()) {
        OutputCard(
            title = stringResource(R.string.dev_output),
            text = output,
            onCopy = {
                copyToClipboard(context, output)
                scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
            },
            onShare = { shareText(context, output) },
            monospace = true,
        )
    }
}

@Composable
private fun UuidTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var count by rememberSaveable { mutableIntStateOf(5) }
    var uppercase by rememberSaveable { mutableStateOf(false) }
    var values by rememberSaveable { mutableStateOf(listOf<String>()) }
    Text(stringResource(R.string.dev_count), style = MaterialTheme.typography.labelLarge)
    Slider(
        value = count.toFloat(),
        onValueChange = { count = it.toInt().coerceIn(1, 50) },
        valueRange = 1f..50f,
        steps = 48,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uppercase = !uppercase },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(checked = uppercase, onCheckedChange = { uppercase = it })
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.dev_uppercase))
    }
    Spacer(Modifier.height(10.dp))
    DamdastButton(
        text = stringResource(R.string.dev_generate),
        onClick = {
            values = List(count) {
                UUID.randomUUID().toString().let { if (uppercase) it.uppercase() else it }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    if (values.isNotEmpty()) {
        OutputCard(
            title = stringResource(R.string.common_result),
            text = values.joinToString("\n"),
            onCopy = {
                copyToClipboard(context, values.joinToString("\n"))
                scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
            },
            onShare = { shareText(context, values.joinToString("\n")) },
            monospace = true,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dev_uuid_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HashTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    var algorithm by rememberSaveable { mutableIntStateOf(2) }
    val output = remember(input, algorithm) {
        runCatching { HashUtils.hash(input, HashUtils.algorithms[algorithm]) }.getOrNull()
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        label = { Text(stringResource(R.string.dev_input)) },
        shape = MaterialTheme.shapes.medium,
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HashUtils.algorithms.forEachIndexed { index, name ->
            FilterChip(
                selected = algorithm == index,
                onClick = { algorithm = index },
                label = { Text(name) },
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    if (output != null && input.isNotBlank()) {
        OutputCard(
            title = HashUtils.algorithms[algorithm],
            text = output,
            onCopy = {
                copyToClipboard(context, output)
                scope.launch { snackbar?.showSnackbar(context.getString(R.string.common_copied)) }
            },
            onShare = { shareText(context, output) },
            monospace = true,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dev_hash_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimestampTool() {
    val language = LocalAppLanguage.current
    var input by rememberSaveable { mutableStateOf((System.currentTimeMillis() / 1000).toString()) }
    var timestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var error by rememberSaveable { mutableStateOf(false) }
    fun convert() {
        val result = DateMath.parseTimestamp(input)
        if (result == null) {
            error = true
        } else {
            timestamp = result
            error = false
        }
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.dev_timestamp_input)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        shape = MaterialTheme.shapes.medium,
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DamdastButton(
            text = stringResource(R.string.dev_convert),
            onClick = ::convert,
            modifier = Modifier.weight(1f),
        )
        DamdastButton(
            text = stringResource(R.string.dev_now),
            onClick = {
                input = (System.currentTimeMillis() / 1000).toString()
                convert()
            },
            style = DamdastButtonStyle.Tonal,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(12.dp))
    if (error) {
        StatusCard(
            title = stringResource(R.string.common_error),
            text = stringResource(R.string.dev_timestamp_invalid),
            positive = false,
        )
    } else {
        KeyValueCard(
            rows = listOf(
                stringResource(R.string.dev_timestamp_seconds) to (timestamp / 1000).toString(),
                stringResource(R.string.dev_timestamp_millis) to timestamp.toString(),
                stringResource(R.string.dev_timestamp_local) to DateMath.formatLocal(timestamp),
                stringResource(R.string.dev_timestamp_utc) to DateMath.formatUtc(timestamp),
                stringResource(R.string.dev_timestamp_iso) to DateMath.formatIsoUtc(timestamp),
            ),
            language = language,
        )
    }
}

@Composable
private fun RegexTool() {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var patternText by rememberSaveable { mutableStateOf("") }
    var testText by rememberSaveable { mutableStateOf("") }
    var ignoreCase by rememberSaveable { mutableStateOf(true) }
    var multiline by rememberSaveable { mutableStateOf(false) }
    var dotAll by rememberSaveable { mutableStateOf(false) }
    val result = remember(patternText, testText, ignoreCase, multiline, dotAll) {
        if (patternText.isBlank()) return@remember null
        runCatching {
            var flags = 0
            if (ignoreCase) flags = flags or Pattern.CASE_INSENSITIVE
            if (multiline) flags = flags or Pattern.MULTILINE
            if (dotAll) flags = flags or Pattern.DOTALL
            val pattern = Pattern.compile(patternText, flags)
            val matcher = pattern.matcher(testText)
            val matches = mutableListOf<List<String>>()
            while (matcher.find() && matches.size < 100) {
                matches.add(
                    buildList {
                        add(matcher.group())
                        for (index in 1..matcher.groupCount()) add(matcher.group(index).orEmpty())
                    },
                )
            }
            matches
        }
    }
    OutlinedTextField(
        value = patternText,
        onValueChange = { patternText = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.dev_regex_pattern)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = testText,
        onValueChange = { testText = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        label = { Text(stringResource(R.string.dev_regex_text)) },
        shape = MaterialTheme.shapes.medium,
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Checkbox(ignoreCase, { ignoreCase = it })
        Text(
            stringResource(R.string.dev_ignore_case),
            modifier = Modifier
                .clickable { ignoreCase = !ignoreCase }
                .padding(top = 12.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Checkbox(multiline, { multiline = it })
        Text(
            stringResource(R.string.dev_multiline),
            modifier = Modifier
                .clickable { multiline = !multiline }
                .padding(top = 12.dp),
            style = MaterialTheme.typography.labelSmall,
        )
        Checkbox(dotAll, { dotAll = it })
        Text(
            stringResource(R.string.dev_dot_matches_newline),
            modifier = Modifier
                .clickable { dotAll = !dotAll }
                .padding(top = 12.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
    Spacer(Modifier.height(10.dp))
    when {
        result == null -> Unit
        result.isFailure -> StatusCard(
            title = stringResource(R.string.dev_invalid_regex),
            text = (result.exceptionOrNull() as? PatternSyntaxException)?.description.orEmpty(),
            positive = false,
        )

        result.getOrNull().isNullOrEmpty() -> Text(
            text = stringResource(R.string.dev_no_matches),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        else -> {
            Text(
                text = stringResource(
                    R.string.dev_matches,
                ) + " · " + formatInt(result.getOrNull()!!.size, LocalAppLanguage.current),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            result.getOrNull()!!.forEachIndexed { index, groups ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 7.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(11.dp)) {
                        Text(
                            text = stringResource(R.string.dev_match_number, (index + 1).toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = groups.first(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                        )
                        groups.drop(1).forEachIndexed { groupIndex, value ->
                            Text(
                                text = "${stringResource(R.string.dev_group_number, (groupIndex + 1).toString())}: $value",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlTool() {
    var input by rememberSaveable { mutableStateOf("https://example.com/path?name=Damdast&lang=fa#top") }
    val parsed = remember(input) { runCatching { UrlUtils.parse(input) } }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.dev_input)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(12.dp))
    if (parsed.isFailure) {
        StatusCard(
            title = stringResource(R.string.common_error),
            text = stringResource(R.string.error_invalid_input),
            positive = false,
        )
    } else {
        val url = parsed.getOrThrow()
        KeyValueCard(
            rows = listOf(
                stringResource(R.string.dev_scheme) to url.scheme,
                stringResource(R.string.dev_host) to url.host,
                stringResource(R.string.dev_port) to url.port,
                stringResource(R.string.dev_path) to url.path,
                stringResource(R.string.dev_query) to url.query,
                stringResource(R.string.dev_fragment) to url.fragment,
            ),
            language = LocalAppLanguage.current,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.dev_query_parameters),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(4.dp))
        if (url.parameters.isEmpty()) {
            Text(
                text = stringResource(R.string.dev_no_parameters),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            url.parameters.forEach { (name, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(110.dp),
                    )
                    SelectionContainer {
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
}

@Composable
private fun JwtTool() {
    var input by rememberSaveable { mutableStateOf("") }
    val decoded = remember(input) {
        if (input.isBlank()) null else runCatching { JwtUtils.decode(input) }
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        label = { Text(stringResource(R.string.dev_input)) },
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(10.dp))
    if (decoded != null) {
        Text(
            text = stringResource(R.string.dev_jwt_warning),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(10.dp))
        if (decoded.isFailure) {
            StatusCard(
                title = stringResource(R.string.dev_jwt_invalid),
                text = decoded.exceptionOrNull()?.message.orEmpty(),
                positive = false,
            )
        } else {
            val parts = decoded.getOrThrow()
            OutputCard(
                title = stringResource(R.string.dev_jwt_header),
                text = parts.header,
                onCopy = {},
                onShare = {},
                monospace = true,
                showActions = false,
            )
            Spacer(Modifier.height(8.dp))
            OutputCard(
                title = stringResource(R.string.dev_jwt_payload),
                text = parts.payload,
                onCopy = {},
                onShare = {},
                monospace = true,
                showActions = false,
            )
            Spacer(Modifier.height(8.dp))
            OutputCard(
                title = stringResource(R.string.dev_jwt_signature),
                text = parts.signature,
                onCopy = {},
                onShare = {},
                monospace = true,
                showActions = false,
            )
        }
    }
}

@Composable
private fun UnixPermissionsTool() {
    var octal by rememberSaveable { mutableStateOf("644") }
    val rwx = remember(octal) {
        runCatching { UnixPermissions.fromOctal(octal) }.getOrNull()
    }
    OutlinedTextField(
        value = octal,
        onValueChange = {
            octal = it.normalizeDigits().filter { char -> char in '0'..'7' }.take(4)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.dev_octal)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(10.dp))
    if (rwx == null) {
        Text(
            text = stringResource(R.string.dev_invalid_permissions),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    } else {
        Text(
            text = rwx,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        val labels = listOf(
            stringResource(R.string.dev_read),
            stringResource(R.string.dev_write),
            stringResource(R.string.dev_execute),
        )
        val groups = listOf(
            stringResource(R.string.dev_owner),
            stringResource(R.string.dev_group),
            stringResource(R.string.dev_others),
        )
        groups.forEachIndexed { groupIndex, group ->
            Text(
                text = group,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                labels.forEachIndexed { labelIndex, label ->
                    val charIndex = groupIndex * 3 + labelIndex
                    val checked = rwx[charIndex] != '-'
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                val chars = rwx.toCharArray()
                                chars[charIndex] = if (checked) '-' else {
                                    when (labelIndex) {
                                        0 -> 'r'
                                        1 -> 'w'
                                        else -> 'x'
                                    }
                                }
                                val newRwx = String(chars)
                                val base = UnixPermissions.toOctal(newRwx)
                                val special = if (octal.length == 4) octal.first() else '0'
                                octal = if (special == '0') base else "$special$base"
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.dev_special_bits_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeveloperColorTool() {
    var input by rememberSaveable { mutableStateOf("#7C5CFF") }
    val parsed = remember(input) { runCatching { ColorUtils.parse(input) } }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.dev_color_input)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = codeFieldColors(),
    )
    Spacer(Modifier.height(12.dp))
    if (parsed.isFailure) {
        StatusCard(
            title = stringResource(R.string.common_error),
            text = stringResource(R.string.error_invalid_input),
            positive = false,
        )
    } else {
        val color = parsed.getOrThrow()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(color.red, color.green, color.blue)),
            )
            Column {
                Text(
                    text = stringResource(R.string.dev_color_preview),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = color.hex(),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        KeyValueCard(
            rows = listOf(
                "HEX" to color.hex(),
                "RGB" to color.rgbString(),
                "RGBA" to color.rgbaString(),
                "HSL" to ColorUtils.hslString(color),
                "HSV" to ColorUtils.hsvString(color),
                stringResource(R.string.dev_contrast_white) to stringResource(
                    R.string.dev_ratio,
                    formatDecimal(ColorUtils.contrastRatio(color, ArgbColor(255, 255, 255, 255)), LocalAppLanguage.current, 2),
                ),
                stringResource(R.string.dev_contrast_black) to stringResource(
                    R.string.dev_ratio,
                    formatDecimal(ColorUtils.contrastRatio(color, ArgbColor(255, 0, 0, 0)), LocalAppLanguage.current, 2),
                ),
            ),
            language = LocalAppLanguage.current,
            monospaceValues = true,
        )
    }
}

@Composable
private fun codeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
private fun OutputCard(
    title: String,
    text: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    monospace: Boolean = false,
    showActions: Boolean = true,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 13.dp, end = 4.dp, top = 5.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (showActions) {
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.common_copy),
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.common_share),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
        ) {
            SelectionContainer {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                )
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, text: String, positive: Boolean) {
    val color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = color,
            )
            if (text.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun KeyValueCard(
    rows: List<Pair<String, String>>,
    language: ir.amiraligrr.damdast.data.AppLanguage,
    monospaceValues: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(130.dp),
                    )
                    SelectionContainer(Modifier.weight(1f)) {
                        Text(
                            text = value.ifBlank { "—" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = if (monospaceValues) FontFamily.Monospace else FontFamily.Default,
                        )
                    }
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 13.dp),
                    )
                }
            }
        }
    }
}
