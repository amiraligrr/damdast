package ir.amiraligrr.damdast.ui.tools

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.tools.ToolDefinition
import ir.amiraligrr.damdast.ui.components.DamdastButton
import ir.amiraligrr.damdast.ui.components.DamdastButtonStyle
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.DateMath
import ir.amiraligrr.damdast.util.GregorianDate
import ir.amiraligrr.damdast.util.JalaliDate
import ir.amiraligrr.damdast.util.PersianCalendar
import ir.amiraligrr.damdast.util.formatClock
import ir.amiraligrr.damdast.util.formatDateNumeric
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.localizeDigits
import ir.amiraligrr.damdast.util.normalizeDigits
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DateToolsScreen(tool: ToolDefinition, onBack: () -> Unit) {
    ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
        when (tool.id) {
            "stopwatch" -> StopwatchTool()
            "timer" -> TimerTool()
            "countdown" -> CountdownTool()
            "world_clock" -> WorldClockTool()
            "date_calculator" -> DateCalculatorTool()
            "jalali_converter" -> JalaliConverterTool()
        }
    }
}

@Composable
private fun StopwatchTool() {
    val language = LocalAppLanguage.current
    var running by rememberSaveable { mutableStateOf(false) }
    var accumulated by rememberSaveable { mutableLongStateOf(0L) }
    var startedAt by rememberSaveable { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var laps by rememberSaveable { mutableStateOf(listOf<Long>()) }
    val elapsed = accumulated + if (running) (now - startedAt).coerceAtLeast(0) else 0

    LaunchedEffect(running) {
        while (running) {
            now = SystemClock.elapsedRealtime()
            delay(31)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        BigTimeCard(
            time = formatClock(elapsed, language, includeMillis = true),
            label = stringResource(R.string.time_elapsed),
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DamdastButton(
                text = stringResource(
                    when {
                        running -> R.string.time_pause
                        elapsed > 0 -> R.string.time_resume
                        else -> R.string.time_start
                    },
                ),
                onClick = {
                    if (running) {
                        accumulated = elapsed
                        running = false
                    } else {
                        startedAt = SystemClock.elapsedRealtime()
                        running = true
                    }
                },
                modifier = Modifier.weight(1f),
            )
            DamdastButton(
                text = stringResource(R.string.time_lap),
                onClick = { laps = listOf(elapsed) + laps },
                style = DamdastButtonStyle.Outline,
                enabled = elapsed > 0,
                modifier = Modifier.weight(1f),
            )
            DamdastButton(
                text = stringResource(R.string.time_reset),
                onClick = {
                    running = false
                    accumulated = 0L
                    startedAt = 0L
                    laps = emptyList()
                },
                style = DamdastButtonStyle.Ghost,
                enabled = elapsed > 0,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.time_laps),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        if (laps.isEmpty()) {
            Text(
                text = stringResource(R.string.time_no_laps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            laps.forEachIndexed { index, lap ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.time_lap_number,
                            formatInt(laps.size - index, language),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatClock(lap, language, includeMillis = true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TimerTool() {
    val language = LocalAppLanguage.current
    var hours by rememberSaveable { mutableStateOf("0") }
    var minutes by rememberSaveable { mutableStateOf("5") }
    var seconds by rememberSaveable { mutableStateOf("0") }
    var remaining by rememberSaveable { mutableLongStateOf(0L) }
    var running by rememberSaveable { mutableStateOf(false) }
    var endAt by rememberSaveable { mutableLongStateOf(0L) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running) {
            val left = endAt - SystemClock.elapsedRealtime()
            if (left <= 0) {
                remaining = 0
                running = false
                finished = true
                runCatching {
                    val tone = ToneGenerator(AudioManager.STREAM_ALARM, 80)
                    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 900)
                    delay(1000)
                    tone.release()
                }
            } else {
                remaining = left
                delay(50)
            }
        }
    }

    fun durationFromFields(): Long {
        val h = hours.normalizeDigits().toLongOrNull() ?: 0L
        val m = minutes.normalizeDigits().toLongOrNull() ?: 0L
        val s = seconds.normalizeDigits().toLongOrNull() ?: 0L
        return ((h * 3600) + (m * 60) + s).coerceIn(0, 24 * 3600) * 1000
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        BigTimeCard(
            time = if (remaining > 0 || running) {
                formatClock(remaining, language)
            } else {
                formatClock(durationFromFields(), language)
            },
            label = if (finished) stringResource(R.string.time_finished) else {
                stringResource(R.string.time_remaining)
            },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(1, 5, 10, 15).forEach { preset ->
                FilterChip(
                    selected = false,
                    onClick = {
                        hours = "0"
                        minutes = (preset % 60).toString()
                        seconds = "0"
                        remaining = preset * 60_000L
                        finished = false
                    },
                    label = {
                        Text(
                            stringResource(
                                when (preset) {
                                    1 -> R.string.time_preset_1
                                    5 -> R.string.time_preset_5
                                    10 -> R.string.time_preset_10
                                    else -> R.string.time_preset_15
                                },
                            ),
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DurationField(hours, { hours = it.filter(Char::isDigit).take(2) }, stringResource(R.string.time_hours_label), Modifier.weight(1f))
            DurationField(minutes, { minutes = it.filter(Char::isDigit).take(2) }, stringResource(R.string.time_minutes_label), Modifier.weight(1f))
            DurationField(seconds, { seconds = it.filter(Char::isDigit).take(2) }, stringResource(R.string.time_seconds_label), Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DamdastButton(
                text = stringResource(if (running) R.string.time_pause else R.string.time_start),
                onClick = {
                    if (running) {
                        running = false
                    } else {
                        val duration = if (remaining > 0) remaining else durationFromFields()
                        if (duration <= 0) {
                            error = true
                        } else {
                            error = false
                            finished = false
                            remaining = duration
                            endAt = SystemClock.elapsedRealtime() + duration
                            running = true
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )
            DamdastButton(
                text = stringResource(R.string.time_reset),
                onClick = {
                    running = false
                    remaining = 0
                    finished = false
                    error = false
                },
                style = DamdastButtonStyle.Outline,
            )
        }
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_invalid_input),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.text_processed_locally),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DurationField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun BigTimeCard(time: String, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.displayLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CountdownTool() {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    var target by rememberSaveable {
        mutableLongStateOf(System.currentTimeMillis() + 86_400_000L)
    }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val remaining = (target - now).coerceAtLeast(0)
    val days = remaining / 86_400_000
    val hours = (remaining % 86_400_000) / 3_600_000
    val minutes = (remaining % 3_600_000) / 60_000
    val seconds = (remaining % 60_000) / 1000
    val calendar = remember(target) { Calendar.getInstance().apply { timeInMillis = target } }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.time_target),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${formatDateNumeric(target, language)} · " +
                        localizeDigits(
                            "%02d:%02d".format(Locale.US, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)),
                            language,
                        ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(18.dp))
                if (remaining == 0L) {
                    Text(
                        text = stringResource(R.string.time_finished),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CountdownUnit(formatInt(days, language), stringResource(R.string.time_days), Modifier.weight(1f))
                        CountdownUnit(formatInt(hours, language, grouping = false), stringResource(R.string.time_hours), Modifier.weight(1f))
                        CountdownUnit(formatInt(minutes, language, grouping = false), stringResource(R.string.time_minutes), Modifier.weight(1f))
                        CountdownUnit(formatInt(seconds, language, grouping = false), stringResource(R.string.time_seconds), Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DamdastButton(
                text = stringResource(R.string.time_choose_date),
                onClick = {
                    val current = Calendar.getInstance().apply { timeInMillis = target }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val updated = Calendar.getInstance().apply {
                                timeInMillis = target
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                            target = updated.timeInMillis
                        },
                        current.get(Calendar.YEAR),
                        current.get(Calendar.MONTH),
                        current.get(Calendar.DAY_OF_MONTH),
                    ).show()
                },
                style = DamdastButtonStyle.Outline,
                modifier = Modifier.weight(1f),
            )
            DamdastButton(
                text = stringResource(R.string.time_choose_time),
                onClick = {
                    val current = Calendar.getInstance().apply { timeInMillis = target }
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val updated = Calendar.getInstance().apply {
                                timeInMillis = target
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                            }
                            target = updated.timeInMillis
                        },
                        current.get(Calendar.HOUR_OF_DAY),
                        current.get(Calendar.MINUTE),
                        true,
                    ).show()
                },
                style = DamdastButtonStyle.Outline,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        DamdastButton(
            text = stringResource(R.string.time_now),
            onClick = { target = System.currentTimeMillis() + 86_400_000L },
            style = DamdastButtonStyle.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun CountdownUnit(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val worldCities = listOf(
    "Asia/Tehran", "UTC", "Europe/London", "Europe/Paris", "Europe/Berlin",
    "Europe/Istanbul", "Asia/Dubai", "Asia/Kolkata", "Asia/Tokyo", "Asia/Shanghai",
    "Asia/Singapore", "Australia/Sydney", "America/New_York", "America/Chicago",
    "America/Denver", "America/Los_Angeles", "America/Sao_Paulo", "Africa/Cairo",
    "Africa/Johannesburg", "Pacific/Auckland",
)

@Composable
private fun WorldClockTool() {
    val language = LocalAppLanguage.current
    var zones by rememberSaveable {
        mutableStateOf(listOf("Asia/Tehran", "UTC", "Europe/London", "America/New_York", "Asia/Tokyo"))
    }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showAdd by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        zones.forEach { zoneId ->
            val zone = TimeZone.getTimeZone(zoneId)
            val timeFormat = remember(zoneId) {
                SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = zone }
            }
            val zoneCalendar = remember(zoneId, now) {
                Calendar.getInstance(zone).apply { timeInMillis = now }
            }
            val zoneDate = if (language == AppLanguage.PERSIAN) {
                val jalali = PersianCalendar.fromGregorian(
                    zoneCalendar.get(Calendar.YEAR),
                    zoneCalendar.get(Calendar.MONTH) + 1,
                    zoneCalendar.get(Calendar.DAY_OF_MONTH),
                )
                "%04d/%02d/%02d".format(Locale.US, jalali.year, jalali.month, jalali.day)
            } else {
                "%04d/%02d/%02d".format(
                    Locale.US,
                    zoneCalendar.get(Calendar.YEAR),
                    zoneCalendar.get(Calendar.MONTH) + 1,
                    zoneCalendar.get(Calendar.DAY_OF_MONTH),
                )
            }
            val offsetMinutes = zone.getOffset(now) / 60_000
            val sign = if (offsetMinutes >= 0) "+" else "-"
            val absOffset = kotlin.math.abs(offsetMinutes)
            val offset = "%s%02d:%02d".format(Locale.US, sign, absOffset / 60, absOffset % 60)
            val cityName = zoneId.substringAfterLast('/').replace('_', ' ')
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = zoneId + " · " + stringResource(R.string.time_utc_offset, offset),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = localizeDigits(timeFormat.format(Date(now)), language),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = localizeDigits(zoneDate, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (zones.size > 1) {
                    IconButton(onClick = { zones = zones - zoneId }) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.time_remove_city),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
        }
        DamdastButton(
            text = stringResource(R.string.time_add_city),
            onClick = { showAdd = true },
            icon = Icons.Outlined.Add,
            style = DamdastButtonStyle.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.time_select_city)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    items(worldCities.filterNot { it in zones }) { zone ->
                        Text(
                            text = zone.substringAfterLast('/').replace('_', ' '),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    zones = zones + zone
                                    showAdd = false
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdd = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun DateCalculatorTool() {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val today = remember { DateMath.nowGregorian() }
    var start by remember { mutableStateOf(today) }
    var end by remember { mutableStateOf(DateMath.addDays(today.year, today.month, today.day, 30)) }
    var deltaDays by rememberSaveable { mutableStateOf("7") }
    val difference = remember(start, end) {
        DateMath.difference(start.year, start.month, start.day, end.year, end.month, end.day)
    }
    val delta = deltaDays.normalizeDigits().trim().toIntOrNull() ?: 0
    val resultDate = DateMath.addDays(start.year, start.month, start.day, delta)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        DateSelector(
            label = stringResource(R.string.time_start_date),
            date = start,
            language = language,
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day -> start = GregorianDate(year, month + 1, day) },
                    start.year,
                    start.month - 1,
                    start.day,
                ).show()
            },
        )
        Spacer(Modifier.height(8.dp))
        DateSelector(
            label = stringResource(R.string.time_end_date),
            date = end,
            language = language,
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day -> end = GregorianDate(year, month + 1, day) },
                    end.year,
                    end.month - 1,
                    end.day,
                ).show()
            },
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.time_date_difference),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        KeyValueRows(
            rows = listOf(
                stringResource(R.string.time_total_days) to stringResource(
                    R.string.time_days_value,
                    formatInt(difference.totalDays, language),
                ),
                stringResource(R.string.time_weeks_days) to stringResource(
                    R.string.time_days_weeks_value,
                    formatInt(difference.weeks, language),
                    formatInt(difference.leftoverDays, language),
                ),
                stringResource(R.string.time_years_months_days) to stringResource(
                    R.string.time_ymd_value,
                    formatInt(difference.years, language),
                    formatInt(difference.months, language),
                    formatInt(difference.days, language),
                ),
            ),
        )
        if (!difference.startBeforeEnd) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.time_reverse_range),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.time_add_subtract),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = deltaDays,
            onValueChange = { deltaDays = it.filter { char -> char.isDigit() || char == '-' }.take(6) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.time_days_delta)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = dateFieldColors(),
        )
        Spacer(Modifier.height(10.dp))
        KeyValueRows(
            rows = listOf(
                stringResource(R.string.time_result_date) to formatGregorian(resultDate, language),
            ),
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DateSelector(
    label: String,
    date: GregorianDate,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 13.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatGregorian(date, language),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun JalaliConverterTool() {
    val language = LocalAppLanguage.current
    val todayGregorian = remember { DateMath.nowGregorian() }
    val todayJalali = remember {
        PersianCalendar.fromGregorian(todayGregorian.year, todayGregorian.month, todayGregorian.day)
    }
    var gy by rememberSaveable { mutableStateOf(todayGregorian.year.toString()) }
    var gm by rememberSaveable { mutableStateOf(todayGregorian.month.toString()) }
    var gd by rememberSaveable { mutableStateOf(todayGregorian.day.toString()) }
    var jy by rememberSaveable { mutableStateOf(todayJalali.year.toString()) }
    var jm by rememberSaveable { mutableStateOf(todayJalali.month.toString()) }
    var jd by rememberSaveable { mutableStateOf(todayJalali.day.toString()) }
    var gregorianResult by rememberSaveable { mutableStateOf<String?>(null) }
    var jalaliResult by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        DamdastButton(
            text = stringResource(R.string.time_today),
            onClick = {
                gy = todayGregorian.year.toString()
                gm = todayGregorian.month.toString()
                gd = todayGregorian.day.toString()
                jy = todayJalali.year.toString()
                jm = todayJalali.month.toString()
                jd = todayJalali.day.toString()
                gregorianResult = null
                jalaliResult = null
                error = false
            },
            style = DamdastButtonStyle.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        ConversionCard(
            title = stringResource(R.string.time_gregorian_to_persian),
            year = gy,
            month = gm,
            day = gd,
            onYear = { gy = it.filter(Char::isDigit).take(4) },
            onMonth = { gm = it.filter(Char::isDigit).take(2) },
            onDay = { gd = it.filter(Char::isDigit).take(2) },
        ) {
            val y = gy.normalizeDigits().toIntOrNull()
            val m = gm.normalizeDigits().toIntOrNull()
            val d = gd.normalizeDigits().toIntOrNull()
            if (y == null || m == null || d == null || !PersianCalendar.isValidGregorian(y, m, d)) {
                error = true
                jalaliResult = null
            } else {
                error = false
                val jalali = PersianCalendar.fromGregorian(y, m, d)
                jalaliResult = formatJalali(jalali, language)
                jy = jalali.year.toString()
                jm = jalali.month.toString()
                jd = jalali.day.toString()
            }
        }
        if (jalaliResult != null) {
            Spacer(Modifier.height(8.dp))
            ResultLine(stringResource(R.string.time_jalali_result, jalaliResult.orEmpty()))
        }
        Spacer(Modifier.height(16.dp))
        ConversionCard(
            title = stringResource(R.string.time_persian_to_gregorian),
            year = jy,
            month = jm,
            day = jd,
            onYear = { jy = it.filter(Char::isDigit).take(4) },
            onMonth = { jm = it.filter(Char::isDigit).take(2) },
            onDay = { jd = it.filter(Char::isDigit).take(2) },
        ) {
            val y = jy.normalizeDigits().toIntOrNull()
            val m = jm.normalizeDigits().toIntOrNull()
            val d = jd.normalizeDigits().toIntOrNull()
            if (y == null || m == null || d == null || !PersianCalendar.isValid(y, m, d)) {
                error = true
                gregorianResult = null
            } else {
                error = false
                val gregorian = PersianCalendar.toGregorian(y, m, d)
                gregorianResult = localizeDigits(
                    "%04d/%02d/%02d".format(Locale.US, gregorian.year, gregorian.month, gregorian.day),
                    language,
                )
                gy = gregorian.year.toString()
                gm = gregorian.month.toString()
                gd = gregorian.day.toString()
            }
        }
        if (gregorianResult != null) {
            Spacer(Modifier.height(8.dp))
            ResultLine(stringResource(R.string.time_gregorian_result, gregorianResult.orEmpty()))
        }
        if (error) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.time_invalid_date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ConversionCard(
    title: String,
    year: String,
    month: String,
    day: String,
    onYear: (String) -> Unit,
    onMonth: (String) -> Unit,
    onDay: (String) -> Unit,
    onConvert: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallDateField(year, onYear, stringResource(R.string.time_year), Modifier.weight(1.2f))
                SmallDateField(month, onMonth, stringResource(R.string.time_month), Modifier.weight(1f))
                SmallDateField(day, onDay, stringResource(R.string.time_day), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            DamdastButton(
                text = stringResource(R.string.time_convert),
                onClick = onConvert,
                style = DamdastButtonStyle.Tonal,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SmallDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.small,
        colors = dateFieldColors(),
    )
}

@Composable
private fun ResultLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun KeyValueRows(rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun dateFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
)

private fun formatGregorian(date: GregorianDate, language: AppLanguage): String {
    val raw = "%04d/%02d/%02d".format(Locale.US, date.year, date.month, date.day)
    if (language == AppLanguage.PERSIAN) {
        val jalali = PersianCalendar.fromGregorian(date.year, date.month, date.day)
        return localizeDigits(
            "%04d/%02d/%02d".format(Locale.US, jalali.year, jalali.month, jalali.day),
            language,
        )
    }
    return raw
}

private fun formatJalali(date: JalaliDate, language: AppLanguage): String =
    localizeDigits("%04d/%02d/%02d".format(Locale.US, date.year, date.month, date.day), language)
