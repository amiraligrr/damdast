package ir.amiraligrr.damdast.data

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.amiraligrr.damdast.DamdastApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DamdastViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DamdastApp
    private val settingsRepository = app.settingsRepository
    private val userDataRepository = app.userDataRepository

    val settings: StateFlow<AppSettings?> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val favorites: StateFlow<Set<String>> = userDataRepository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val recentTools: StateFlow<List<RecentTool>> = userDataRepository.recentTools.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val scanHistory: StateFlow<List<ScanRecord>> = userDataRepository.scanHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val notes: StateFlow<List<NoteItem>> = userDataRepository.notes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val tasks: StateFlow<List<TaskItem>> = userDataRepository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val pdfBookmarks: StateFlow<Map<String, Set<Int>>> = userDataRepository.pdfBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.tag),
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.completeOnboarding() }
    }

    fun restartOnboarding() {
        viewModelScope.launch { settingsRepository.restartOnboarding() }
    }

    fun setAdsDebugDisabled(disabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAdsDebugDisabled(disabled) }
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch { userDataRepository.toggleFavorite(toolId) }
    }

    fun recordToolUse(toolId: String) {
        viewModelScope.launch { userDataRepository.recordToolUse(toolId) }
    }

    fun addScan(value: String, format: String) {
        viewModelScope.launch { userDataRepository.addScan(value, format) }
    }

    fun deleteScan(id: String) {
        viewModelScope.launch { userDataRepository.deleteScan(id) }
    }

    fun clearScanHistory() {
        viewModelScope.launch { userDataRepository.clearScanHistory() }
    }

    fun clearHistory() {
        viewModelScope.launch { userDataRepository.clearHistory() }
    }

    fun saveNote(note: NoteItem) {
        viewModelScope.launch { userDataRepository.saveNote(note) }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { userDataRepository.deleteNote(id) }
    }

    fun saveTask(task: TaskItem) {
        viewModelScope.launch { userDataRepository.saveTask(task) }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch { userDataRepository.toggleTask(id) }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch { userDataRepository.deleteTask(id) }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch { userDataRepository.clearCompletedTasks() }
    }

    fun togglePdfBookmark(uri: String, page: Int) {
        viewModelScope.launch { userDataRepository.togglePdfBookmark(uri, page) }
    }

    fun grantRewardedAccess(durationMillis: Long = 30 * 60 * 1000L) {
        viewModelScope.launch { settingsRepository.grantRewardedAccess(durationMillis) }
    }

    fun setAdsDebugDisabledNow(disabled: Boolean) {
        setAdsDebugDisabled(disabled)
    }
}
