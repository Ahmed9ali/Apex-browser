package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApexDao {

    @Query("SELECT * FROM privacy_stats WHERE id = 1")
    fun observePrivacyStats(): Flow<PrivacyStatSummaryEntity?>

    @Query("SELECT * FROM privacy_stats WHERE id = 1")
    suspend fun getPrivacyStatsOnce(): PrivacyStatSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrivacyStats(stats: PrivacyStatSummaryEntity)

    @Query("SELECT * FROM blocked_domain_logs ORDER BY timestamp DESC LIMIT 40")
    fun observeRecentBlockedLogs(): Flow<List<BlockedDomainLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedLog(log: BlockedDomainLogEntity)

    @Query("DELETE FROM blocked_domain_logs")
    suspend fun clearBlockedLogs()

    @Query("SELECT * FROM extraction_rules ORDER BY createdAt DESC")
    fun observeExtractionRules(): Flow<List<ExtractionRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExtractionRule(rule: ExtractionRuleEntity): Long

    @Query("UPDATE extraction_rules SET lastRunCount = :count WHERE id = :id")
    suspend fun updateRuleRunCount(id: Long, count: Int)

    @Query("DELETE FROM extraction_rules WHERE id = :id")
    suspend fun deleteExtractionRule(id: Long)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    // Search History (starts empty on fresh install)
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 30")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchHistory(entry: SearchHistoryEntity): Long

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistoryById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
