package ir.amiraligrr.damdast.ads

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import ir.amiraligrr.damdast.data.damdastDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdFrequencyController(private val context: Context) {
    private object Keys {
        val day = stringPreferencesKey("ad_interstitial_day")
        val count = intPreferencesKey("ad_interstitial_count")
        val lastShown = longPreferencesKey("ad_interstitial_last")
        val sessions = intPreferencesKey("ad_tool_sessions")
    }

    private val mutex = Mutex()
    private val minimumIntervalMillis = 5 * 60 * 1000L
    private val maximumPerDay = 3
    private val minimumSessions = 2

    suspend fun noteToolCompletion() {
        mutex.withLock {
            runCatching {
                context.damdastDataStore.edit { prefs ->
                    prefs[Keys.sessions] = (prefs[Keys.sessions] ?: 0) + 1
                }
            }
        }
    }

    suspend fun canShow(): Boolean = mutex.withLock {
        runCatching {
            val prefs = context.damdastDataStore.data.first()
            val today = todayKey()
            val count = if (prefs[Keys.day] == today) prefs[Keys.count] ?: 0 else 0
            val last = prefs[Keys.lastShown] ?: 0L
            val sessions = prefs[Keys.sessions] ?: 0
            val now = System.currentTimeMillis()
            count < maximumPerDay &&
                now - last >= minimumIntervalMillis &&
                sessions >= minimumSessions
        }.getOrDefault(false)
    }

    suspend fun recordShown() {
        mutex.withLock {
            runCatching {
                context.damdastDataStore.edit { prefs ->
                    val today = todayKey()
                    val count = if (prefs[Keys.day] == today) prefs[Keys.count] ?: 0 else 0
                    prefs[Keys.day] = today
                    prefs[Keys.count] = count + 1
                    prefs[Keys.lastShown] = System.currentTimeMillis()
                    prefs[Keys.sessions] = 0
                }
            }
        }
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
