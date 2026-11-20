package ir.amiraligrr.damdast.util

import ir.amiraligrr.damdast.data.AppLanguage
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = buildString(length) {
    for (char in this@toPersianDigits) {
        if (char in '0'..'9') append(persianDigits[char - '0']) else append(char)
    }
}

fun String.normalizeDigits(): String = buildString(length) {
    for (char in this@normalizeDigits) {
        when (char) {
            in '۰'..'۹' -> append('0' + (char - '۰'))
            in '٠'..'٩' -> append('0' + (char - '٠'))
            '٬', ',' -> append(',')
            '٫' -> append('.')
            else -> append(char)
        }
    }
}

fun localizeDigits(value: String, language: AppLanguage): String =
    if (language == AppLanguage.PERSIAN) value.toPersianDigits() else value

fun formatInt(value: Long, language: AppLanguage, grouping: Boolean = true): String {
    val raw = if (grouping) {
        "%,d".format(Locale.US, value)
    } else {
        value.toString()
    }
    return localizeDigits(raw, language)
}

fun formatInt(value: Int, language: AppLanguage, grouping: Boolean = true): String =
    formatInt(value.toLong(), language, grouping)

fun formatDecimal(
    value: Double,
    language: AppLanguage,
    maxDecimals: Int = 6,
    grouping: Boolean = true,
): String {
    if (!value.isFinite()) return value.toString()
    val rounded = BigDecimal.valueOf(value)
        .setScale(maxDecimals, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    var plain = rounded.toPlainString()
    val negative = plain.startsWith("-")
    if (negative) plain = plain.removePrefix("-")
    val parts = plain.split('.', limit = 2)
    var integerPart = parts[0]
    if (grouping) integerPart = integerPart.reversed().chunked(3).joinToString(",").reversed()
    val result = buildString {
        if (negative) append('-')
        append(integerPart)
        if (parts.size == 2) append('.').append(parts[1])
    }
    return if (language == AppLanguage.PERSIAN) {
        result.replace(",", "٬").replace(".", "٫").toPersianDigits()
    } else {
        result
    }
}

fun formatBytes(bytes: Long, language: AppLanguage): String {
    if (bytes < 0) return "—"
    if (bytes < 1024) return "${formatInt(bytes, language)} B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = -1
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return "${formatDecimal(value, language, maxDecimals = 1)} ${units[unit]}"
}

fun calendarDayKey(timestamp: Long): Int {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calendar.get(Calendar.YEAR) * 1000 +
        calendar.get(Calendar.DAY_OF_YEAR)
}

fun formatClock(
    millis: Long,
    language: AppLanguage,
    includeMillis: Boolean = false,
): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val base = if (hours > 0) {
        "%02d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    } else {
        "%02d:%02d".format(Locale.US, minutes, seconds)
    }
    val withMillis = if (includeMillis) {
        base + "." + "%03d".format(Locale.US, millis % 1000)
    } else {
        base
    }
    return localizeDigits(withMillis, language)
}

fun formatDateNumeric(
    timestamp: Long,
    language: AppLanguage,
): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return if (language == AppLanguage.PERSIAN) {
        val jalali = PersianCalendar.fromGregorian(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
        localizeDigits(
            "%04d/%02d/%02d".format(Locale.US, jalali.year, jalali.month, jalali.day),
            language,
        )
    } else {
        "%04d/%02d/%02d".format(
            Locale.US,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }
}

fun formatDateTime(
    timestamp: Long,
    language: AppLanguage,
    includeTime: Boolean = true,
): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val date = formatDateNumeric(timestamp, language)
    if (!includeTime) return date
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val time = localizeDigits("%02d:%02d".format(Locale.US, hour, minute), language)
    return "$date · $time"
}

fun formatRelativeTime(
    timestamp: Long,
    language: AppLanguage,
    justNow: String,
    minutesAgo: (Long) -> String,
    hoursAgo: (Long) -> String,
    yesterday: String,
): String {
    if (timestamp <= 0) return ""
    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0)
    return when {
        diff < 60_000 -> justNow
        diff < 3_600_000 -> minutesAgo(diff / 60_000)
        calendarDayKey(timestamp) == calendarDayKey(now) -> hoursAgo((diff / 3_600_000).coerceAtLeast(1))
        calendarDayKey(timestamp) == calendarDayKey(now - 86_400_000L) -> yesterday
        else -> formatDateNumeric(timestamp, language)
    }
}

fun weekdayName(timestamp: Long, language: AppLanguage): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return if (language == AppLanguage.PERSIAN) {
        val names = arrayOf(
            "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه",
            "پنجشنبه", "جمعه", "شنبه",
        )
        names[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    } else {
        java.text.SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.time)
    }
}
