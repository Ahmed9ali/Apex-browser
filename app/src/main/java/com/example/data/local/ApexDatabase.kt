package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PrivacyStatSummaryEntity::class,
        BlockedDomainLogEntity::class,
        ExtractionRuleEntity::class,
        BookmarkEntity::class,
        SearchHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ApexDatabase : RoomDatabase() {

    abstract fun apexDao(): ApexDao

    companion object {
        @Volatile
        private var INSTANCE: ApexDatabase? = null

        fun getInstance(context: Context): ApexDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ApexDatabase::class.java,
                    "apex_browser_fresh.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
