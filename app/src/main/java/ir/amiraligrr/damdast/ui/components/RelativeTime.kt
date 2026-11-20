package ir.amiraligrr.damdast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.ui.theme.LocalAppLanguage
import ir.amiraligrr.damdast.util.calendarDayKey
import ir.amiraligrr.damdast.util.formatDateNumeric
import ir.amiraligrr.damdast.util.formatInt
import ir.amiraligrr.damdast.util.formatRelativeTime

@Composable
fun relativeTimeText(timestamp: Long): String {
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    return formatRelativeTime(
        timestamp = timestamp,
        language = language,
        justNow = context.getString(R.string.common_just_now),
        minutesAgo = { minutes ->
            context.getString(R.string.time_minutes_ago, formatInt(minutes, language))
        },
        hoursAgo = { hours ->
            context.getString(R.string.time_hours_ago, formatInt(hours, language))
        },
        yesterday = context.getString(R.string.common_yesterday),
    )
}

@Composable
fun shortDateText(timestamp: Long): String {
    val language = LocalAppLanguage.current
    val days = calendarDayKey(timestamp) - calendarDayKey(System.currentTimeMillis())
    return when (days) {
        0 -> stringResource(R.string.common_today)
        -1 -> stringResource(R.string.common_yesterday)
        else -> formatDateNumeric(timestamp, language)
    }
}
