package ir.amiraligrr.damdast.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

data class DateDifference(
    val startBeforeEnd: Boolean,
    val totalDays: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val weeks: Long,
    val leftoverDays: Long,
)

object DateMath {
    private const val DAY_MILLIS = 86_400_000L

    fun epochDay(year: Int, month: Int, day: Int): Long {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
            clear()
            isLenient = false
            set(year, month - 1, day)
        }
        return calendar.timeInMillis / DAY_MILLIS
    }

    fun daysBetween(
        startYear: Int,
        startMonth: Int,
        startDay: Int,
        endYear: Int,
        endMonth: Int,
        endDay: Int,
    ): Long = epochDay(endYear, endMonth, endDay) - epochDay(startYear, startMonth, startDay)

    fun addDays(year: Int, month: Int, day: Int, delta: Int): GregorianDate {
        val calendar = GregorianCalendar().apply {
            clear()
            set(year, month - 1, day)
            add(Calendar.DAY_OF_MONTH, delta)
        }
        return GregorianDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun difference(
        startYear: Int,
        startMonth: Int,
        startDay: Int,
        endYear: Int,
        endMonth: Int,
        endDay: Int,
    ): DateDifference {
        val startKey = startYear * 10_000 + startMonth * 100 + startDay
        val endKey = endYear * 10_000 + endMonth * 100 + endDay
        val startBeforeEnd = startKey <= endKey
        val start = if (startBeforeEnd) {
            GregorianCalendar(startYear, startMonth - 1, startDay)
        } else {
            GregorianCalendar(endYear, endMonth - 1, endDay)
        }
        val end = if (startBeforeEnd) {
            GregorianCalendar(endYear, endMonth - 1, endDay)
        } else {
            GregorianCalendar(startYear, startMonth - 1, startDay)
        }
        val totalDays = daysBetween(
            start.get(Calendar.YEAR),
            start.get(Calendar.MONTH) + 1,
            start.get(Calendar.DAY_OF_MONTH),
            end.get(Calendar.YEAR),
            end.get(Calendar.MONTH) + 1,
            end.get(Calendar.DAY_OF_MONTH),
        )
        var cursor = start.clone() as GregorianCalendar
        var years = 0
        while (years < 500) {
            val next = cursor.clone() as GregorianCalendar
            next.add(Calendar.YEAR, 1)
            if (next.after(end)) break
            years++
            cursor = next
        }
        var months = 0
        while (months < 12) {
            val next = cursor.clone() as GregorianCalendar
            next.add(Calendar.MONTH, 1)
            if (next.after(end)) break
            months++
            cursor = next
        }
        var days = 0
        while (days < 31) {
            val next = cursor.clone() as GregorianCalendar
            next.add(Calendar.DAY_OF_MONTH, 1)
            if (next.after(end)) break
            days++
            cursor = next
        }
        return DateDifference(
            startBeforeEnd = startBeforeEnd,
            totalDays = totalDays,
            years = years,
            months = months,
            days = days,
            weeks = totalDays / 7,
            leftoverDays = totalDays % 7,
        )
    }

    fun nowGregorian(): GregorianDate {
        val calendar = Calendar.getInstance()
        return GregorianDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun formatLocal(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(timestamp))

    fun formatUtc(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))

    fun formatIsoUtc(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))

    fun parseTimestamp(input: String): Long? {
        val text = input.trim().normalizeDigits()
        if (text.isEmpty()) return null
        if (text.matches(Regex("-?\\d+"))) {
            val value = text.toLongOrNull() ?: return null
            return if (kotlin.math.abs(value) < 100_000_000_000L) value * 1000L else value
        }
        val normalized = text.replace('T', ' ').removeSuffix("Z")
        val patterns = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy",
        )
        for (pattern in patterns) {
            val format = SimpleDateFormat(pattern, Locale.US).apply {
                isLenient = false
                timeZone = TimeZone.getDefault()
            }
            val parsed = runCatching { format.parse(normalized) }.getOrNull()
            if (parsed != null) return parsed.time
        }
        return null
    }
}

data class JwtParts(
    val header: String,
    val payload: String,
    val signature: String,
)

object JwtUtils {
    fun decode(token: String): JwtParts {
        val parts = token.trim().split('.')
        require(parts.size == 3) { "A JWT must have three parts" }
        val header = TextTransforms.base64Decode(parts[0])
        val payload = TextTransforms.base64Decode(parts[1])
        val headerPretty = runCatching { JsonUtils.pretty(header) }.getOrDefault(header)
        val payloadPretty = runCatching { JsonUtils.pretty(payload) }.getOrDefault(payload)
        return JwtParts(headerPretty, payloadPretty, parts[2])
    }
}
