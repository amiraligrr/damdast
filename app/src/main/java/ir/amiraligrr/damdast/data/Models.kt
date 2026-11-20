package ir.amiraligrr.damdast.data

enum class AppTheme {
    LIGHT,
    DARK,
}

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    PERSIAN("fa"),
}

data class AppSettings(
    val theme: AppTheme = AppTheme.DARK,
    val language: AppLanguage = AppLanguage.PERSIAN,
    val onboardingCompleted: Boolean = false,
    val adsDebugDisabled: Boolean = false,
    val rewardedAccessUntil: Long = 0L,
)

data class RecentTool(
    val toolId: String,
    val lastUsed: Long,
    val useCount: Int,
)

data class ScanRecord(
    val id: String,
    val value: String,
    val format: String,
    val createdAt: Long,
)

data class NoteItem(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: Long,
)

data class TaskItem(
    val id: String,
    val text: String,
    val done: Boolean,
    val createdAt: Long,
)
