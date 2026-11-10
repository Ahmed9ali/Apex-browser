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
    val searchEngine: SearchEngineOption = SearchEngineOption.DUCKDUCKGO,
    val autoSaveOfflinePagesEnabled: Boolean = true
) {
    val seedPreset: SeedPalettePreset
        get() = SeedPalettePreset.entries.getOrElse(seedPaletteOrdinal) {
            SeedPalettePreset.ELECTRIC_COBALT
        }

    val searchEngineTemplate: String
        get() = searchEngine.searchUrlTemplate
}

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val sharedPrefs: android.content.SharedPreferences? = null
) {

    constructor(context: Context) : this(
        dataStore = context.applicationContext.apexDataStore,
        sharedPrefs = context.applicationContext.getSharedPreferences(
            "apex_browser_user_preferences",
            Context.MODE_PRIVATE
        )
    )

    private object Keys {
        val SHIELDS_ENABLED = booleanPreferencesKey("shields_enabled")
        val HTTPS_UPGRADE = booleanPreferencesKey("https_upgrade_enabled")
        val AGGRESSIVE_PRELOAD = booleanPreferencesKey("aggressive_preload_enabled")
        val SCRIPT_GUARD = booleanPreferencesKey("script_guard_enabled")
        val DESKTOP_VIEWPORT = booleanPreferencesKey("desktop_viewport_enabled")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val SEED_ORDINAL = intPreferencesKey("seed_palette_ordinal")
        val SEARCH_ENGINE_ID = stringPreferencesKey("search_engine_id")
        val AUTO_SAVE_OFFLINE = booleanPreferencesKey("auto_save_offline_enabled")
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
            val shields = prefs[Keys.SHIELDS_ENABLED]
                ?: sharedPrefs?.getBoolean("shields_enabled", true)
                ?: true
            val https = prefs[Keys.HTTPS_UPGRADE]
                ?: sharedPrefs?.getBoolean("https_upgrade_enabled", true)
                ?: true
            val preload = prefs[Keys.AGGRESSIVE_PRELOAD]
                ?: sharedPrefs?.getBoolean("aggressive_preload_enabled", true)
                ?: true
            val scriptGuard = prefs[Keys.SCRIPT_GUARD]
                ?: sharedPrefs?.getBoolean("script_guard_enabled", false)
                ?: false
            val desktop = prefs[Keys.DESKTOP_VIEWPORT]
                ?: sharedPrefs?.getBoolean("desktop_viewport_enabled", false)
                ?: false
            val dynamicColor = prefs[Keys.DYNAMIC_COLOR]
                ?: sharedPrefs?.getBoolean("dynamic_color_enabled", true)
                ?: true
            val seedOrdinal = prefs[Keys.SEED_ORDINAL]
                ?: sharedPrefs?.getInt("seed_palette_ordinal", 0)
                ?: 0
            val engineId = prefs[Keys.SEARCH_ENGINE_ID]
                ?: sharedPrefs?.getString("search_engine_id", SearchEngineOption.DUCKDUCKGO.id)
            val autoSave = prefs[Keys.AUTO_SAVE_OFFLINE]
                ?: sharedPrefs?.getBoolean("auto_save_offline_enabled", true)
                ?: true

            BrowserPreferences(
                shieldsEnabled = shields,
                httpsUpgradeEnabled = https,
                aggressivePreloadEnabled = preload,
                scriptGuardEnabled = scriptGuard,
                desktopViewportEnabled = desktop,
                dynamicColorEnabled = dynamicColor,
                seedPaletteOrdinal = seedOrdinal,
                searchEngine = SearchEngineOption.fromId(engineId),
                autoSaveOfflinePagesEnabled = autoSave
            )
        }

    suspend fun setAutoSaveOfflinePagesEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_SAVE_OFFLINE] = enabled }
        sharedPrefs?.edit()?.putBoolean("auto_save_offline_enabled", enabled)?.apply()
    }

    suspend fun setShieldsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SHIELDS_ENABLED] = enabled }
        sharedPrefs?.edit()?.putBoolean("shields_enabled", enabled)?.apply()
    }

    suspend fun setHttpsUpgradeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HTTPS_UPGRADE] = enabled }
        sharedPrefs?.edit()?.putBoolean("https_upgrade_enabled", enabled)?.apply()
    }

    suspend fun setAggressivePreloadEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AGGRESSIVE_PRELOAD] = enabled }
        sharedPrefs?.edit()?.putBoolean("aggressive_preload_enabled", enabled)?.apply()
    }

    suspend fun setScriptGuardEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SCRIPT_GUARD] = enabled }
        sharedPrefs?.edit()?.putBoolean("script_guard_enabled", enabled)?.apply()
    }

    suspend fun setDesktopViewportEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DESKTOP_VIEWPORT] = enabled }
        sharedPrefs?.edit()?.putBoolean("desktop_viewport_enabled", enabled)?.apply()
    }

    suspend fun setSearchEngine(engine: SearchEngineOption) {
        dataStore.edit { it[Keys.SEARCH_ENGINE_ID] = engine.id }
        sharedPrefs?.edit()?.putString("search_engine_id", engine.id)?.apply()
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
        sharedPrefs?.edit()?.putBoolean("dynamic_color_enabled", enabled)?.apply()
    }

    suspend fun setSeedPaletteOrdinal(ordinal: Int) {
        val bounded = ordinal.coerceIn(0, SeedPalettePreset.entries.lastIndex)
        dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = false
            prefs[Keys.SEED_ORDINAL] = bounded
        }
        sharedPrefs?.edit()
            ?.putBoolean("dynamic_color_enabled", false)
            ?.putInt("seed_palette_ordinal", bounded)
            ?.apply()
    }

    suspend fun cycleSeedPalette() {
        dataStore.edit { prefs ->
            val currentDynamic = prefs[Keys.DYNAMIC_COLOR] ?: true
            val currentSeed = prefs[Keys.SEED_ORDINAL] ?: 0
            val (newDynamic, newOrdinal) = if (currentDynamic) {
                false to 0
            } else {
                val next = currentSeed + 1
                if (next >= SeedPalettePreset.entries.size) {
                    true to 0
                } else {
                    false to next
                }
            }
            prefs[Keys.DYNAMIC_COLOR] = newDynamic
            prefs[Keys.SEED_ORDINAL] = newOrdinal
            sharedPrefs?.edit()
                ?.putBoolean("dynamic_color_enabled", newDynamic)
                ?.putInt("seed_palette_ordinal", newOrdinal)
                ?.apply()
        }
    }
}
