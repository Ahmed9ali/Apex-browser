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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserRepository(
    private val dao: ApexDao,
    private val preferencesRepository: UserPreferencesRepository
) {

    val preferencesFlow: Flow<BrowserPreferences> = preferencesRepository.preferencesFlow

    val privacyStatsFlow: Flow<PrivacyStatSummaryEntity> = dao.observePrivacyStats()
        .map { it ?: PrivacyStatSummaryEntity() }

    val recentBlockedLogsFlow: Flow<List<BlockedDomainLogEntity>> = dao.observeRecentBlockedLogs()

    val extractionRulesFlow: Flow<List<ExtractionRuleEntity>> = dao.observeExtractionRules()

    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.observeBookmarks()

    val searchHistoryFlow: Flow<List<SearchHistoryEntity>> = dao.observeSearchHistory()

    /**
     * Fresh-install initialization: creates only a zeroed-out telemetry row if absent.
     * No pre-populated tabs, bookmarks, search history, or blocked logs.
     */
    suspend fun ensureFreshInitialized() {
        if (dao.getPrivacyStatsOnce() == null) {
            dao.upsertPrivacyStats(PrivacyStatSummaryEntity())
        }
    }

    suspend fun saveBookmark(url: String, title: String) {
        dao.upsertBookmark(
            BookmarkEntity(
                url = url,
                title = title.ifBlank { url },
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBookmark(id: Long) {
        dao.deleteBookmarkById(id)
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

    suspend fun cycleSeedPalette() =
        preferencesRepository.cycleSeedPalette()
}
