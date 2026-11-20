package ir.amiraligrr.damdast.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val context: Context,
    private val fallbackLanguage: AppLanguage,
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val language = stringPreferencesKey("language")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val adsDebugDisabled = booleanPreferencesKey("ads_debug_disabled")
        val rewardedAccessUntil = longPreferencesKey("rewarded_access_until")
    }

    val settings: Flow<AppSettings> = context.damdastDataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[Keys.theme]
                ?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.DARK,
            language = prefs[Keys.language]
                ?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: fallbackLanguage,
            onboardingCompleted = prefs[Keys.onboardingCompleted] ?: false,
            adsDebugDisabled = prefs[Keys.adsDebugDisabled] ?: false,
            rewardedAccessUntil = prefs[Keys.rewardedAccessUntil] ?: 0L,
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.damdastDataStore.edit { it[Keys.theme] = theme.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.damdastDataStore.edit { it[Keys.language] = language.name }
    }

    suspend fun completeOnboarding() {
        context.damdastDataStore.edit { it[Keys.onboardingCompleted] = true }
    }

    suspend fun restartOnboarding() {
        context.damdastDataStore.edit { it[Keys.onboardingCompleted] = false }
    }

    suspend fun setAdsDebugDisabled(disabled: Boolean) {
        context.damdastDataStore.edit { it[Keys.adsDebugDisabled] = disabled }
    }

    suspend fun grantRewardedAccess(durationMillis: Long) {
        context.damdastDataStore.edit {
            it[Keys.rewardedAccessUntil] = System.currentTimeMillis() + durationMillis
        }
    }
}
