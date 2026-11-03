package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.SeedPalettePreset
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.apexDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "apex_browser_prefs_v3"
)

enum class SearchEngineOption(
    val id: String,
    val displayName: String,
    val shortBadge: String,
    val description: String,
    val searchUrlTemplate: String
) {
    DUCKDUCKGO(
        id = "duckduckgo",
        displayName = "DuckDuckGo",
        shortBadge = "DDG",
        description = "Private web search without user profiling",
        searchUrlTemplate = "https://duckduckgo.com/?q=%s"
    ),
    GOOGLE(
        id = "google",
        displayName = "Google",
        shortBadge = "G",
        description = "Comprehensive global web index and instant answers",
        searchUrlTemplate = "https://www.google.com/search?q=%s"
    ),
    BRAVE(
        id = "brave",
        displayName = "Brave Search",
        shortBadge = "BRV",
        description = "Independent search index with built-in privacy",
        searchUrlTemplate = "https://search.brave.com/search?q=%s"
    ),
    BING(
        id = "bing",
        displayName = "Bing",
        shortBadge = "BNG",
        description = "Web search, news, and rich media results",
        searchUrlTemplate = "https://www.bing.com/search?q=%s"
    ),
    WIKIPEDIA(
        id = "wikipedia",
        displayName = "Wikipedia",
        shortBadge = "WIKI",
        description = "Direct encyclopedia article lookup and summaries",
        searchUrlTemplate = "https://en.wikipedia.org/w/index.php?search=%s"
    );

    companion object {
        fun fromId(id: String?): SearchEngineOption {
            return entries.firstOrNull { it.id == id } ?: DUCKDUCKGO
        }
    }
}

data class BrowserPreferences(
    val shieldsEnabled: Boolean = true,
    val httpsUpgradeEnabled: Boolean = true,
    val aggressivePreloadEnabled: Boolean = true,
    val scriptGuardEnabled: Boolean = false,
    val desktopViewportEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = true,
    val seedPaletteOrdinal: Int = 0,
    val searchEngine: SearchEngineOption = SearchEngineOption.DUCKDUCKGO
) {
    val seedPreset: SeedPalettePreset
        get() = SeedPalettePreset.entries.getOrElse(seedPaletteOrdinal) {
            SeedPalettePreset.ELECTRIC_COBALT
        }

    val searchEngineTemplate: String
        get() = searchEngine.searchUrlTemplate
}

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.apexDataStore)

    private object Keys {
        val SHIELDS_ENABLED = booleanPreferencesKey("shields_enabled")
        val HTTPS_UPGRADE = booleanPreferencesKey("https_upgrade_enabled")
        val AGGRESSIVE_PRELOAD = booleanPreferencesKey("aggressive_preload_enabled")
        val SCRIPT_GUARD = booleanPreferencesKey("script_guard_enabled")
        val DESKTOP_VIEWPORT = booleanPreferencesKey("desktop_viewport_enabled")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val SEED_ORDINAL = intPreferencesKey("seed_palette_ordinal")
        val SEARCH_ENGINE_ID = stringPreferencesKey("search_engine_id")
    }

    val preferencesFlow: Flow<BrowserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            BrowserPreferences(
                shieldsEnabled = prefs[Keys.SHIELDS_ENABLED] ?: true,
                httpsUpgradeEnabled = prefs[Keys.HTTPS_UPGRADE] ?: true,
                aggressivePreloadEnabled = prefs[Keys.AGGRESSIVE_PRELOAD] ?: true,
                scriptGuardEnabled = prefs[Keys.SCRIPT_GUARD] ?: false,
                desktopViewportEnabled = prefs[Keys.DESKTOP_VIEWPORT] ?: false,
                dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: true,
                seedPaletteOrdinal = prefs[Keys.SEED_ORDINAL] ?: 0,
                searchEngine = SearchEngineOption.fromId(prefs[Keys.SEARCH_ENGINE_ID])
            )
        }

    suspend fun setShieldsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SHIELDS_ENABLED] = enabled }
    }

    suspend fun setHttpsUpgradeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HTTPS_UPGRADE] = enabled }
    }

    suspend fun setAggressivePreloadEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AGGRESSIVE_PRELOAD] = enabled }
    }

    suspend fun setScriptGuardEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SCRIPT_GUARD] = enabled }
    }

    suspend fun setDesktopViewportEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DESKTOP_VIEWPORT] = enabled }
    }

    suspend fun setSearchEngine(engine: SearchEngineOption) {
        dataStore.edit { it[Keys.SEARCH_ENGINE_ID] = engine.id }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setSeedPaletteOrdinal(ordinal: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = false
            prefs[Keys.SEED_ORDINAL] = ordinal.coerceIn(0, SeedPalettePreset.entries.lastIndex)
        }
    }

    suspend fun cycleSeedPalette() {
        dataStore.edit { prefs ->
            val currentDynamic = prefs[Keys.DYNAMIC_COLOR] ?: true
            val currentSeed = prefs[Keys.SEED_ORDINAL] ?: 0
            if (currentDynamic) {
                prefs[Keys.DYNAMIC_COLOR] = false
                prefs[Keys.SEED_ORDINAL] = 0
            } else {
                val next = currentSeed + 1
                if (next >= SeedPalettePreset.entries.size) {
                    prefs[Keys.DYNAMIC_COLOR] = true
                    prefs[Keys.SEED_ORDINAL] = 0
                } else {
                    prefs[Keys.SEED_ORDINAL] = next
                }
            }
        }
    }
}
