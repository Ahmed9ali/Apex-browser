package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_stats")
data class PrivacyStatSummaryEntity(
    @PrimaryKey val id: Int = 1,
    val trackersBlocked: Int = 0,
    val adsBlocked: Int = 0,
    val httpsUpgrades: Int = 0,
    val bytesSaved: Long = 0L,
    val timeSavedMs: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_domain_logs")
data class BlockedDomainLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pageHost: String,
    val blockedDomain: String,
    val category: String,
    val savedBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "extraction_rules")
data class ExtractionRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val promptDescription: String,
    val itemSelector: String,
    val titleSubSelector: String,
    val valueSubSelector: String,
    val linkSubSelector: String,
    val lastRunCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["url"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "search_history",
    indices = [Index(value = ["query"], unique = true)]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val searchEngineName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "offline_pages",
    indices = [Index(value = ["url"], unique = true)]
)
data class OfflinePageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val htmlContent: String,
    val textSnippet: String = "",
    val sizeBytes: Long = 0L,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "top_visits",
    indices = [Index(value = ["url"], unique = true)]
)
data class TopVisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)

