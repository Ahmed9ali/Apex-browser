package com.example.data.repository

import com.example.data.local.ApexDao
import com.example.data.local.BlockedDomainLogEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.ExtractionRuleEntity
import com.example.data.local.PrivacyStatSummaryEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.preferences.BrowserPreferences
import com.example.data.preferences.SearchEngineOption
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.local.OfflinePageEntity
import com.example.data.local.TopVisitEntity
import com.example.engine.OfflineStorageUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class BrowserRepository(
    private val dao: ApexDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val sharedPrefs: SharedPreferences? = null
) {

    val preferencesFlow: Flow<BrowserPreferences> = preferencesRepository.preferencesFlow

    val privacyStatsFlow: Flow<PrivacyStatSummaryEntity> = dao.observePrivacyStats()
        .map { it ?: PrivacyStatSummaryEntity() }

    val recentBlockedLogsFlow: Flow<List<BlockedDomainLogEntity>> = dao.observeRecentBlockedLogs()

    val extractionRulesFlow: Flow<List<ExtractionRuleEntity>> = dao.observeExtractionRules()

    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.observeBookmarks()

    val searchHistoryFlow: Flow<List<SearchHistoryEntity>> = dao.observeSearchHistory()

    val offlinePagesFlow: Flow<List<OfflinePageEntity>> = dao.observeOfflinePages()

    val offlinePagesCountFlow: Flow<Int> = dao.observeOfflinePagesCount()

    val topVisitsFlow: Flow<List<TopVisitEntity>> = dao.observeTopVisits()

    /**
     * Fresh-install initialization: creates only a zeroed-out telemetry row if absent,
     * and seeds initial quick shortcuts for top visits or restores from SharedPreferences.
     */
    suspend fun ensureFreshInitialized() {
        if (dao.getPrivacyStatsOnce() == null) {
            dao.upsertPrivacyStats(PrivacyStatSummaryEntity())
        }

        // Restore bookmarks from SharedPreferences if DB is clean
        sharedPrefs?.getString("saved_bookmarks_json", null)?.let { jsonStr ->
            if (dao.getBookmarksOnce().isEmpty() && jsonStr.isNotBlank()) {
                runCatching {
                    val array = JSONArray(jsonStr)
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        dao.upsertBookmark(
                            BookmarkEntity(
                                url = item.optString("url"),
                                title = item.optString("title"),
                                createdAt = item.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }
        }

        // Top visits setup and SharedPreferences restore
        if (dao.getTopVisitsOnce().isEmpty()) {
            val restoredJson = sharedPrefs?.getString("saved_top_visits_json", null)
            var restored = false
            if (!restoredJson.isNullOrBlank()) {
                runCatching {
                    val array = JSONArray(restoredJson)
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        dao.upsertTopVisit(
                            TopVisitEntity(
                                title = item.optString("title"),
                                url = item.optString("url"),
                                createdAt = item.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                    restored = true
                }
            }
            if (!restored) {
                dao.upsertTopVisit(TopVisitEntity(title = "Google", url = "https://www.google.com"))
                dao.upsertTopVisit(TopVisitEntity(title = "YouTube", url = "https://www.youtube.com"))
                dao.upsertTopVisit(TopVisitEntity(title = "Wikipedia", url = "https://www.wikipedia.org"))
                dao.upsertTopVisit(TopVisitEntity(title = "GitHub", url = "https://www.github.com"))
                dao.upsertTopVisit(TopVisitEntity(title = "Reddit", url = "https://www.reddit.com"))
                syncTopVisitsToSharedPrefs()
            }
        }
    }

    private suspend fun syncBookmarksToSharedPrefs() {
        sharedPrefs?.let { sp ->
            runCatching {
                val current = dao.getBookmarksOnce()
                val array = JSONArray()
                for (b in current) {
                    val obj = JSONObject()
                    obj.put("id", b.id)
                    obj.put("url", b.url)
                    obj.put("title", b.title)
                    obj.put("createdAt", b.createdAt)
                    array.put(obj)
                }
                sp.edit().putString("saved_bookmarks_json", array.toString()).apply()
            }
        }
    }

    private suspend fun syncTopVisitsToSharedPrefs() {
        sharedPrefs?.let { sp ->
            runCatching {
                val current = dao.getTopVisitsOnce()
                val array = JSONArray()
                for (v in current) {
                    val obj = JSONObject()
                    obj.put("id", v.id)
                    obj.put("title", v.title)
                    obj.put("url", v.url)
                    obj.put("createdAt", v.createdAt)
                    array.put(obj)
                }
                sp.edit().putString("saved_top_visits_json", array.toString()).apply()
            }
        }
    }

    suspend fun addTopVisit(title: String, url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) return
        val finalUrl = if (!trimmedUrl.startsWith("http://", ignoreCase = true) &&
            !trimmedUrl.startsWith("https://", ignoreCase = true) &&
            !trimmedUrl.startsWith("apex://", ignoreCase = true)
        ) {
            "https://$trimmedUrl"
        } else {
            trimmedUrl
        }
        val cleanTitle = title.trim().ifBlank {
            finalUrl.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        }
        dao.upsertTopVisit(
            TopVisitEntity(
                title = cleanTitle,
                url = finalUrl,
                createdAt = System.currentTimeMillis()
            )
        )
        syncTopVisitsToSharedPrefs()
    }

    suspend fun deleteTopVisit(id: Long) {
        dao.deleteTopVisitById(id)
        syncTopVisitsToSharedPrefs()
    }

    suspend fun saveBookmark(url: String, title: String) {
        dao.upsertBookmark(
            BookmarkEntity(
                url = url,
                title = title.ifBlank { url },
                createdAt = System.currentTimeMillis()
            )
        )
        syncBookmarksToSharedPrefs()
    }

    suspend fun deleteBookmark(id: Long) {
        dao.deleteBookmarkById(id)
        syncBookmarksToSharedPrefs()
    }

    suspend fun recordSearchQuery(query: String, engineName: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("apex://", ignoreCase = true)) return
        dao.upsertSearchHistory(
            SearchHistoryEntity(
                query = trimmed,
                searchEngineName = engineName,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSearchHistoryItem(id: Long) {
        dao.deleteSearchHistoryById(id)
    }

    suspend fun clearAllSearchHistory() {
        dao.clearSearchHistory()
    }

    suspend fun recordBlockedItems(
        pageHost: String,
        trackersDelta: Int,
        adsDelta: Int,
        bytesDelta: Long,
        timeMsDelta: Long,
        sampleDomain: String?,
        sampleCategory: String?
    ) {
        val current = dao.getPrivacyStatsOnce() ?: PrivacyStatSummaryEntity()
        dao.upsertPrivacyStats(
            current.copy(
                trackersBlocked = current.trackersBlocked + trackersDelta,
                adsBlocked = current.adsBlocked + adsDelta,
                bytesSaved = current.bytesSaved + bytesDelta,
                timeSavedMs = current.timeSavedMs + timeMsDelta,
                updatedAt = System.currentTimeMillis()
            )
        )
        if (!sampleDomain.isNullOrBlank()) {
            dao.insertBlockedLog(
                BlockedDomainLogEntity(
                    pageHost = pageHost.ifBlank { "web" },
                    blockedDomain = sampleDomain,
                    category = sampleCategory ?: "Tracker",
                    savedBytes = bytesDelta.coerceAtLeast(8_192L)
                )
            )
        }
    }

    suspend fun recordHttpsUpgrade() {
        val current = dao.getPrivacyStatsOnce() ?: PrivacyStatSummaryEntity()
        dao.upsertPrivacyStats(
            current.copy(
                httpsUpgrades = current.httpsUpgrades + 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetTelemetryStats() {
        dao.upsertPrivacyStats(PrivacyStatSummaryEntity())
        dao.clearBlockedLogs()
        dao.clearSearchHistory()
    }

    suspend fun saveExtractionRule(rule: ExtractionRuleEntity): Long {
        return dao.upsertExtractionRule(rule)
    }

    suspend fun updateRuleRunCount(id: Long, count: Int) {
        dao.updateRuleRunCount(id, count)
    }

    suspend fun deleteExtractionRule(id: Long) {
        dao.deleteExtractionRule(id)
    }

    suspend fun setShieldsEnabled(enabled: Boolean) =
        preferencesRepository.setShieldsEnabled(enabled)

    suspend fun setHttpsUpgradeEnabled(enabled: Boolean) =
        preferencesRepository.setHttpsUpgradeEnabled(enabled)

    suspend fun setAggressivePreloadEnabled(enabled: Boolean) =
        preferencesRepository.setAggressivePreloadEnabled(enabled)

    suspend fun setScriptGuardEnabled(enabled: Boolean) =
        preferencesRepository.setScriptGuardEnabled(enabled)

    suspend fun setDesktopViewportEnabled(enabled: Boolean) =
        preferencesRepository.setDesktopViewportEnabled(enabled)

    suspend fun setSearchEngine(engine: SearchEngineOption) =
        preferencesRepository.setSearchEngine(engine)

    suspend fun setDynamicColorEnabled(enabled: Boolean) =
        preferencesRepository.setDynamicColorEnabled(enabled)

    suspend fun setSeedPaletteOrdinal(ordinal: Int) =
        preferencesRepository.setSeedPaletteOrdinal(ordinal)

    suspend fun cycleSeedPalette() =
        preferencesRepository.cycleSeedPalette()

    suspend fun saveOfflinePage(url: String, title: String, htmlContent: String): Long {
        if (url.isBlank() || htmlContent.isBlank()) return -1L
        val sizeBytes = htmlContent.toByteArray(Charsets.UTF_8).size.toLong()
        val snippet = OfflineStorageUtil.extractSnippet(htmlContent)
        val entity = OfflinePageEntity(
            url = url,
            title = title.ifBlank { url },
            htmlContent = htmlContent,
            textSnippet = snippet,
            sizeBytes = sizeBytes,
            savedAt = System.currentTimeMillis()
        )
        return dao.upsertOfflinePage(entity)
    }

    suspend fun getOfflinePageByUrl(url: String): OfflinePageEntity? {
        return dao.getOfflinePageByUrl(url)
    }

    suspend fun getOfflinePageById(id: Long): OfflinePageEntity? {
        return dao.getOfflinePageById(id)
    }

    suspend fun deleteOfflinePage(id: Long) {
        dao.deleteOfflinePageById(id)
    }

    suspend fun deleteOfflinePageByUrl(url: String) {
        dao.deleteOfflinePageByUrl(url)
    }

    suspend fun clearOfflinePages() {
        dao.clearOfflinePages()
    }

    suspend fun setAutoSaveOfflinePagesEnabled(enabled: Boolean) {
        preferencesRepository.setAutoSaveOfflinePagesEnabled(enabled)
    }
}
