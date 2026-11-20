package ir.amiraligrr.damdast.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class UserDataRepository(private val context: Context) {

    private object Keys {
        val favorites = stringSetPreferencesKey("favorites")
        val recent = stringPreferencesKey("recent_tools")
        val scans = stringPreferencesKey("scan_history")
        val notes = stringPreferencesKey("notes")
        val tasks = stringPreferencesKey("tasks")
        val bookmarks = stringPreferencesKey("pdf_bookmarks")
    }

    val favorites: Flow<Set<String>> = context.damdastDataStore.data.map { prefs ->
        prefs[Keys.favorites].orEmpty()
    }

    val recentTools: Flow<List<RecentTool>> = context.damdastDataStore.data.map { prefs ->
        decodeRecent(prefs[Keys.recent])
    }

    val scanHistory: Flow<List<ScanRecord>> = context.damdastDataStore.data.map { prefs ->
        decodeScans(prefs[Keys.scans])
    }

    val notes: Flow<List<NoteItem>> = context.damdastDataStore.data.map { prefs ->
        decodeNotes(prefs[Keys.notes])
    }

    val tasks: Flow<List<TaskItem>> = context.damdastDataStore.data.map { prefs ->
        decodeTasks(prefs[Keys.tasks])
    }

    val pdfBookmarks: Flow<Map<String, Set<Int>>> = context.damdastDataStore.data.map { prefs ->
        decodeBookmarks(prefs[Keys.bookmarks])
    }

    suspend fun toggleFavorite(toolId: String) {
        context.damdastDataStore.edit { prefs ->
            val current = prefs[Keys.favorites].orEmpty()
            prefs[Keys.favorites] = if (toolId in current) current - toolId else current + toolId
        }
    }

    suspend fun recordToolUse(toolId: String, now: Long = System.currentTimeMillis()) {
        context.damdastDataStore.edit { prefs ->
            val current = decodeRecent(prefs[Keys.recent]).toMutableList()
            val existing = current.indexOfFirst { it.toolId == toolId }
            if (existing >= 0) {
                val old = current.removeAt(existing)
                current.add(
                    0,
                    old.copy(lastUsed = now, useCount = old.useCount + 1),
                )
            } else {
                current.add(0, RecentTool(toolId, now, 1))
            }
            prefs[Keys.recent] = encodeRecent(current.take(50))
        }
    }

    suspend fun addScan(value: String, format: String) {
        if (value.isBlank()) return
        val now = System.currentTimeMillis()
        context.damdastDataStore.edit { prefs ->
            val current = decodeScans(prefs[Keys.scans]).toMutableList()
            val newest = current.firstOrNull()
            if (newest != null && newest.value == value && now - newest.createdAt < 5_000) {
                prefs[Keys.scans] = encodeScans(current.take(100))
                return@edit
            }
            current.add(
                0,
                ScanRecord(
                    id = java.util.UUID.randomUUID().toString(),
                    value = value,
                    format = format,
                    createdAt = now,
                ),
            )
            prefs[Keys.scans] = encodeScans(current.take(100))
        }
    }

    suspend fun deleteScan(id: String) {
        context.damdastDataStore.edit { prefs ->
            val current = decodeScans(prefs[Keys.scans]).filterNot { it.id == id }
            prefs[Keys.scans] = encodeScans(current)
        }
    }

    suspend fun clearScanHistory() {
        context.damdastDataStore.edit { prefs -> prefs[Keys.scans] = "[]" }
    }

    suspend fun clearHistory() {
        context.damdastDataStore.edit { prefs ->
            prefs[Keys.recent] = "[]"
            prefs[Keys.scans] = "[]"
        }
    }

    suspend fun saveNote(note: NoteItem) {
        context.damdastDataStore.edit { prefs ->
            val current = decodeNotes(prefs[Keys.notes]).filterNot { it.id == note.id }
            val updated = (current + note).sortedByDescending { it.updatedAt }
            prefs[Keys.notes] = encodeNotes(updated)
        }
    }

    suspend fun deleteNote(id: String) {
        context.damdastDataStore.edit { prefs ->
            prefs[Keys.notes] = encodeNotes(decodeNotes(prefs[Keys.notes]).filterNot { it.id == id })
        }
    }

    suspend fun saveTask(task: TaskItem) {
        context.damdastDataStore.edit { prefs ->
            val current = decodeTasks(prefs[Keys.tasks]).filterNot { it.id == task.id }
            prefs[Keys.tasks] = encodeTasks(current + task)
        }
    }

    suspend fun toggleTask(id: String) {
        context.damdastDataStore.edit { prefs ->
            val updated = decodeTasks(prefs[Keys.tasks]).map {
                if (it.id == id) it.copy(done = !it.done) else it
            }
            prefs[Keys.tasks] = encodeTasks(updated)
        }
    }

    suspend fun deleteTask(id: String) {
        context.damdastDataStore.edit { prefs ->
            prefs[Keys.tasks] = encodeTasks(decodeTasks(prefs[Keys.tasks]).filterNot { it.id == id })
        }
    }

    suspend fun clearCompletedTasks() {
        context.damdastDataStore.edit { prefs ->
            prefs[Keys.tasks] = encodeTasks(decodeTasks(prefs[Keys.tasks]).filterNot { it.done })
        }
    }

    suspend fun togglePdfBookmark(uri: String, page: Int) {
        context.damdastDataStore.edit { prefs ->
            val map = decodeBookmarks(prefs[Keys.bookmarks]).toMutableMap()
            val pages = map[uri].orEmpty().toMutableSet()
            if (!pages.add(page)) pages.remove(page)
            if (pages.isEmpty()) map.remove(uri) else map[uri] = pages
            prefs[Keys.bookmarks] = encodeBookmarks(map)
        }
    }

    // --- JSON encoding -----------------------------------------------------

    private fun decodeRecent(raw: String?): List<RecentTool> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("toolId")
                    if (id.isBlank()) continue
                    add(
                        RecentTool(
                            toolId = id,
                            lastUsed = obj.optLong("lastUsed"),
                            useCount = obj.optInt("useCount", 1),
                        ),
                    )
                }
            }.sortedByDescending { it.lastUsed }
        }.getOrDefault(emptyList())
    }

    private fun encodeRecent(items: List<RecentTool>): String {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("toolId", it.toolId)
                    .put("lastUsed", it.lastUsed)
                    .put("useCount", it.useCount),
            )
        }
        return array.toString()
    }

    private fun decodeScans(raw: String?): List<ScanRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val value = obj.optString("value")
                    if (value.isBlank()) continue
                    add(
                        ScanRecord(
                            id = obj.optString("id", i.toString()),
                            value = value,
                            format = obj.optString("format"),
                            createdAt = obj.optLong("createdAt"),
                        ),
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    private fun encodeScans(items: List<ScanRecord>): String {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("value", it.value)
                    .put("format", it.format)
                    .put("createdAt", it.createdAt),
            )
        }
        return array.toString()
    }

    private fun decodeNotes(raw: String?): List<NoteItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    if (id.isBlank()) continue
                    add(
                        NoteItem(
                            id = id,
                            title = obj.optString("title"),
                            body = obj.optString("body"),
                            updatedAt = obj.optLong("updatedAt"),
                        ),
                    )
                }
            }.sortedByDescending { it.updatedAt }
        }.getOrDefault(emptyList())
    }

    private fun encodeNotes(items: List<NoteItem>): String {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("body", it.body)
                    .put("updatedAt", it.updatedAt),
            )
        }
        return array.toString()
    }

    private fun decodeTasks(raw: String?): List<TaskItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    if (id.isBlank()) continue
                    add(
                        TaskItem(
                            id = id,
                            text = obj.optString("text"),
                            done = obj.optBoolean("done"),
                            createdAt = obj.optLong("createdAt"),
                        ),
                    )
                }
            }.sortedBy { it.createdAt }
        }.getOrDefault(emptyList())
    }

    private fun encodeTasks(items: List<TaskItem>): String {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("text", it.text)
                    .put("done", it.done)
                    .put("createdAt", it.createdAt),
            )
        }
        return array.toString()
    }

    private fun decodeBookmarks(raw: String?): Map<String, Set<Int>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    val array = obj.optJSONArray(key) ?: return@forEach
                    val pages = buildSet {
                        for (i in 0 until array.length()) add(array.optInt(i))
                    }.filter { it > 0 }.toSet()
                    if (pages.isNotEmpty()) put(key, pages)
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun encodeBookmarks(map: Map<String, Set<Int>>): String {
        val obj = JSONObject()
        map.forEach { (key, pages) ->
            obj.put(key, JSONArray(pages.sorted()))
        }
        return obj.toString()
    }
}
