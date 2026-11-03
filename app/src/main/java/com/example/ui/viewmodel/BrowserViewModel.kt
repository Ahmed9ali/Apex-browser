package com.example.ui.viewmodel

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.automation.ExtractedRecord
import com.example.automation.NanoChatMessage
import com.example.automation.NanoMultiAgentEngine
import com.example.automation.RetrieverEngine
import com.example.automation.TabSnapshotTarget
import com.example.data.local.ApexDatabase
import com.example.data.local.BlockedDomainLogEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.ExtractionRuleEntity
import com.example.data.local.PrivacyStatSummaryEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.preferences.BrowserPreferences
import com.example.data.preferences.SearchEngineOption
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.BrowserRepository
import com.example.engine.PerformanceOptimizer
import com.example.engine.PreloadCandidate
import com.example.engine.PrivacyManager
import com.example.engine.SearchSuggestionEngine
import com.example.engine.SearchSuggestionItem
import com.example.ui.theme.SeedPalettePreset
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TabGroupInfo(
    val id: String = "grp_${UUID.randomUUID()}",
    val title: String
)

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Home",
    val url: String = "apex://home",
    val groupId: String? = null,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val blockedCountOnPage: Int = 0,
    val isHttps: Boolean = true,
    val wasUpgradedToHttps: Boolean = false
) {
    val isHomePage: Boolean
        get() = url.isBlank() || url.equals("apex://home", ignoreCase = true)
}

sealed interface TabGridEntry {
    val gridKey: String

    data class SingleTab(val tab: BrowserTab) : TabGridEntry {
        override val gridKey: String = "single_${tab.id}"
    }

    data class GroupTab(
        val group: TabGroupInfo,
        val tabs: List<BrowserTab>
    ) : TabGridEntry {
        override val gridKey: String = "group_${group.id}"
    }
}

enum class ExtensionModule {
    NANO,
    RETRIEVER
}

enum class RetrieverDisplayMode {
    TABLE,
    CARDS
}

data class BrowserUiState(
    // Fresh launch: strictly 1 clean Home tab — no pre-opened tabs
    val tabs: List<BrowserTab> = listOf(
        BrowserTab(id = "tab_home", title = "Home", url = "apex://home")
    ),
    val tabGroups: List<TabGroupInfo> = emptyList(),
    val expandedGroupId: String? = null,
    val activeTabId: String = "tab_home",
    val previousTabIndex: Int = 0,
    val isToolbarVisible: Boolean = true,
    val isSearchScreenOpen: Boolean = false,
    val searchQuery: String = "",
    val searchSuggestions: List<SearchSuggestionItem> = emptyList(),
    val isLoadingSuggestions: Boolean = false,
    val isTabSwitcherOpen: Boolean = false,
    val isBookmarksScreenOpen: Boolean = false,
    val isSettingsScreenOpen: Boolean = false,
    val isAutomationOpen: Boolean = false,
    val activeExtension: ExtensionModule = ExtensionModule.NANO,
    val snackbarNotice: String? = null,
    val preloadCandidates: List<PreloadCandidate> = emptyList(),
    val nanoMessages: List<NanoChatMessage> = listOf(
        NanoChatMessage(
            id = "welcome_nano",
            isUser = false,
            text = "Nano Multi-Agent ready. PlannerAgent, NavigatorAgent, and ValidatorAgent run locally over the active WebView DOM."
        )
    ),
    val isNanoRunning: Boolean = false,
    val selectedRuleId: Long? = null,
    val retrieverPromptInput: String = "",
    val extractedRecords: List<ExtractedRecord> = emptyList(),
    val isRetrieverRunning: Boolean = false,
    val retrieverStatusBanner: String = "Ready for single-page or multi-tab parallel capture",
    val retrieverDisplayMode: RetrieverDisplayMode = RetrieverDisplayMode.TABLE
) {
    val activeTab: BrowserTab
        get() = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()

    val activeTabIndex: Int
        get() = tabs.indexOfFirst { it.id == activeTabId }.coerceAtLeast(0)

    val gridEntries: List<TabGridEntry>
        get() {
            val emittedGroups = mutableSetOf<String>()
            val result = mutableListOf<TabGridEntry>()
            val groupLookup = tabGroups.associateBy { it.id }

            for (tab in tabs) {
                val gId = tab.groupId
                if (gId == null) {
                    result.add(TabGridEntry.SingleTab(tab))
                } else if (emittedGroups.add(gId)) {
                    val memberTabs = tabs.filter { it.groupId == gId }
                    if (memberTabs.size <= 1) {
                        result.add(TabGridEntry.SingleTab(tab))
                    } else {
                        val info = groupLookup[gId] ?: TabGroupInfo(id = gId, title = "Tab Group")
                        result.add(TabGridEntry.GroupTab(group = info, tabs = memberTabs))
                    }
                }
            }
            return result
        }
}

class BrowserViewModel(
    private val repository: BrowserRepository,
    val privacyManager: PrivacyManager = PrivacyManager(),
    val performanceOptimizer: PerformanceOptimizer = PerformanceOptimizer(),
    private val suggestionEngine: SearchSuggestionEngine = SearchSuggestionEngine(),
    private val nanoEngine: NanoMultiAgentEngine = NanoMultiAgentEngine(),
    private val retrieverEngine: RetrieverEngine = RetrieverEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val preferencesState: StateFlow<BrowserPreferences> = repository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BrowserPreferences()
        )

    val privacyStatsState: StateFlow<PrivacyStatSummaryEntity> = repository.privacyStatsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PrivacyStatSummaryEntity()
        )

    val blockedLogsState: StateFlow<List<BlockedDomainLogEntity>> = repository.recentBlockedLogsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val extractionRulesState: StateFlow<List<ExtractionRuleEntity>> = repository.extractionRulesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val bookmarksState: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val searchHistoryState: StateFlow<List<SearchHistoryEntity>> = repository.searchHistoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val webViewRegistry = mutableMapOf<String, WebView>()
    private var suggestionJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureFreshInitialized()
        }
    }

    fun registerWebView(tabId: String, webView: WebView) {
        webViewRegistry[tabId] = webView
    }

    fun getWebView(tabId: String): WebView? = webViewRegistry[tabId]

    fun consumeSnackbarNotice() {
        _uiState.update { it.copy(snackbarNotice = null) }
    }

    // --- Dedicated Search Screen & Search-Engine Suggestions ---

    fun openSearchScreen(prefillCurrentUrl: Boolean = false) {
        val currentTab = _uiState.value.activeTab
        val initialQuery = if (prefillCurrentUrl && !currentTab.isHomePage) {
            currentTab.url
        } else {
            ""
        }
        _uiState.update {
            it.copy(
                isSearchScreenOpen = true,
                isBookmarksScreenOpen = false,
                isSettingsScreenOpen = false,
                isTabSwitcherOpen = false,
                isAutomationOpen = false,
                isToolbarVisible = true,
                searchQuery = initialQuery,
                searchSuggestions = emptyList(),
                isLoadingSuggestions = initialQuery.isNotBlank()
            )
        }
        if (initialQuery.isNotBlank()) {
            triggerSuggestionsFetch(initialQuery, preferencesState.value.searchEngine)
        }
    }

    fun closeSearchScreen() {
        suggestionJob?.cancel()
        _uiState.update {
            it.copy(
                isSearchScreenOpen = false,
                isLoadingSuggestions = false
            )
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update {
            it.copy(
                searchQuery = newQuery,
                searchSuggestions = if (newQuery.isBlank()) emptyList() else it.searchSuggestions,
                isLoadingSuggestions = newQuery.isNotBlank()
            )
        }
        triggerSuggestionsFetch(newQuery, preferencesState.value.searchEngine)
    }

    fun selectSearchEngine(engine: SearchEngineOption) {
        viewModelScope.launch {
            repository.setSearchEngine(engine)
            val currentQ = _uiState.value.searchQuery
            if (currentQ.isNotBlank()) {
                triggerSuggestionsFetch(currentQ, engine)
            }
        }
    }

    private fun triggerSuggestionsFetch(query: String, engine: SearchEngineOption) {
        suggestionJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(searchSuggestions = emptyList(), isLoadingSuggestions = false)
            }
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(160)
            val results = suggestionEngine.fetchSuggestions(trimmed, engine)
            _uiState.update { state ->
                if (state.searchQuery.trim() == trimmed) {
                    state.copy(
                        searchSuggestions = results,
                        isLoadingSuggestions = false
                    )
                } else {
                    state
                }
            }
        }
    }

    fun deleteSearchHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteSearchHistoryItem(id) }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch { repository.clearAllSearchHistory() }
    }

    fun submitNavigation(rawInput: String, targetTabId: String = _uiState.value.activeTabId) {
        val trimmedInput = rawInput.trim()
        if (trimmedInput.isEmpty()) return

        val prefs = preferencesState.value
        val normalized = privacyManager.normalizeAndUpgradeUrl(
            rawInput = trimmedInput,
            httpsUpgradeEnabled = prefs.httpsUpgradeEnabled,
            searchEngineTemplate = prefs.searchEngineTemplate
        )

        if (!normalized.isBuiltInScheme) {
            viewModelScope.launch {
                repository.recordSearchQuery(
                    query = trimmedInput,
                    engineName = prefs.searchEngine.displayName
                )
            }
        }

        if (normalized.wasUpgradedToHttps) {
            viewModelScope.launch { repository.recordHttpsUpgrade() }
        }

        suggestionJob?.cancel()

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == targetTabId) {
                    tab.copy(
                        url = normalized.finalUrl,
                        title = if (normalized.finalUrl == "apex://home") {
                            "Home"
                        } else {
                            deriveShortTitle(normalized.finalUrl)
                        },
                        blockedCountOnPage = 0,
                        isHttps = normalized.finalUrl.startsWith("https://") || normalized.isBuiltInScheme,
                        wasUpgradedToHttps = normalized.wasUpgradedToHttps,
                        isLoading = normalized.finalUrl != "apex://home"
                    )
                } else {
                    tab
                }
            }
            state.copy(
                tabs = updatedTabs,
                isSearchScreenOpen = false,
                isLoadingSuggestions = false,
                isBookmarksScreenOpen = false,
                isSettingsScreenOpen = false,
                isTabSwitcherOpen = false,
                isToolbarVisible = true
            )
        }
    }

    fun onWebViewPageStarted(tabId: String, url: String) {
        if (url.startsWith("data:") || url.isBlank()) return
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) {
                        val effectiveUrl = mapBaseUrlBackToScheme(url, tab.url)
                        tab.copy(
                            url = effectiveUrl,
                            isLoading = true,
                            isHttps = effectiveUrl.startsWith("https://") || effectiveUrl.startsWith("apex://")
                        )
                    } else {
                        tab
                    }
                }
            )
        }
    }

    fun onWebViewProgress(tabId: String, progress: Int) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) {
                        tab.copy(
                            progress = (progress / 100f).coerceIn(0f, 1f),
                            isLoading = progress < 100
                        )
                    } else {
                        tab
                    }
                }
            )
        }
    }

    fun onWebViewPageFinished(
        tabId: String,
        title: String?,
        url: String?,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) {
                        val effectiveUrl = if (!url.isNullOrBlank()) {
                            mapBaseUrlBackToScheme(url, tab.url)
                        } else {
                            tab.url
                        }
                        tab.copy(
                            title = title?.takeIf { it.isNotBlank() } ?: deriveShortTitle(effectiveUrl),
                            url = effectiveUrl,
                            isLoading = false,
                            progress = 1f,
                            canGoBack = canGoBack,
                            canGoForward = canGoForward
                        )
                    } else {
                        tab
                    }
                }
            )
        }

        val wv = webViewRegistry[tabId]
        if (wv != null && tabId == _uiState.value.activeTabId) {
            performanceOptimizer.preloadLikelyNextPages(
                webView = wv,
                enabled = preferencesState.value.aggressivePreloadEnabled
            ) { candidates ->
                _uiState.update { it.copy(preloadCandidates = candidates) }
            }
        }
    }

    fun onSubresourceBlocked(
        tabId: String,
        pageHost: String,
        blockedDomain: String,
        category: String,
        bytesSaved: Long,
        timeSavedMs: Long
    ) {
        incrementTabBlockedCount(tabId, 1)
        viewModelScope.launch {
            repository.recordBlockedItems(
                pageHost = pageHost,
                trackersDelta = if (category == "Tracker") 1 else 0,
                adsDelta = if (category == "Ad Network") 1 else 0,
                bytesDelta = bytesSaved,
                timeMsDelta = timeSavedMs,
                sampleDomain = blockedDomain,
                sampleCategory = category
            )
        }
    }

    private fun incrementTabBlockedCount(tabId: String, delta: Int) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) {
                        tab.copy(blockedCountOnPage = tab.blockedCountOnPage + delta)
                    } else {
                        tab
                    }
                }
            )
        }
    }

    fun onWebViewScrollDelta(dy: Int) {
        if (_uiState.value.activeTab.isHomePage || _uiState.value.isSearchScreenOpen) {
            if (!_uiState.value.isToolbarVisible) {
                _uiState.update { it.copy(isToolbarVisible = true) }
            }
            return
        }
        if (dy > 16 && _uiState.value.isToolbarVisible) {
            _uiState.update { it.copy(isToolbarVisible = false) }
        } else if (dy < -12 && !_uiState.value.isToolbarVisible) {
            _uiState.update { it.copy(isToolbarVisible = true) }
        }
    }

    // --- Tab & Draggable Tab Group Management ---

    fun selectTab(tabId: String) {
        _uiState.update { state ->
            val prevIdx = state.activeTabIndex
            val targetTab = state.tabs.firstOrNull { it.id == tabId } ?: return@update state
            state.copy(
                previousTabIndex = prevIdx,
                activeTabId = tabId,
                expandedGroupId = null,
                isTabSwitcherOpen = false,
                isBookmarksScreenOpen = false,
                isSearchScreenOpen = false,
                isToolbarVisible = true
            )
        }
    }

    fun createNewTab(
        initialUrl: String = "apex://home",
        groupId: String? = null,
        stayInSwitcher: Boolean = groupId != null
    ) {
        val newTab = BrowserTab(
            title = if (initialUrl == "apex://home") "Home" else deriveShortTitle(initialUrl),
            url = initialUrl,
            groupId = groupId
        )
        _uiState.update { state ->
            state.copy(
                previousTabIndex = state.activeTabIndex,
                tabs = state.tabs + newTab,
                activeTabId = if (!stayInSwitcher) newTab.id else state.activeTabId,
                isTabSwitcherOpen = stayInSwitcher,
                isBookmarksScreenOpen = false,
                isSettingsScreenOpen = false,
                isSearchScreenOpen = false,
                isToolbarVisible = true
            )
        }
        if (initialUrl != "apex://home") {
            submitNavigation(initialUrl, newTab.id)
        }
    }

    fun createNewTabGroupFromSwitcher() {
        _uiState.update { state ->
            val newGroupId = "grp_${UUID.randomUUID()}"
            val groupNumber = state.tabGroups.size + 1
            val newGroup = TabGroupInfo(id = newGroupId, title = "Group $groupNumber")

            val ungroupedTabs = state.tabs.filter { it.groupId == null }
            val updatedTabs = if (ungroupedTabs.size >= 2) {
                val idsToGroup = ungroupedTabs.takeLast(2).map { it.id }.toSet()
                state.tabs.map { tab ->
                    if (tab.id in idsToGroup) tab.copy(groupId = newGroupId) else tab
                }
            } else if (ungroupedTabs.size == 1) {
                val existingUngroupedId = ungroupedTabs.single().id
                val companionTab = BrowserTab(title = "Home", url = "apex://home", groupId = newGroupId)
                state.tabs.map { tab ->
                    if (tab.id == existingUngroupedId) tab.copy(groupId = newGroupId) else tab
                } + companionTab
            } else {
                val t1 = BrowserTab(title = "Home", url = "apex://home", groupId = newGroupId)
                val t2 = BrowserTab(title = "Home", url = "apex://home", groupId = newGroupId)
                state.tabs + t1 + t2
            }

            val (cleanTabs, cleanGroups) = normalizeGroups(updatedTabs, state.tabGroups + newGroup)
            state.copy(
                tabs = cleanTabs,
                tabGroups = cleanGroups,
                isTabSwitcherOpen = true
            )
        }
    }

    fun mergeGridEntriesIntoGroup(draggedKey: String, targetKey: String) {
        if (draggedKey == targetKey) return

        _uiState.update { state ->
            val draggedIsGroup = draggedKey.startsWith("group_")
            val targetIsGroup = targetKey.startsWith("group_")

            val draggedId = draggedKey.removePrefix("single_").removePrefix("group_")
            val targetId = targetKey.removePrefix("single_").removePrefix("group_")

            when {
                !draggedIsGroup && !targetIsGroup -> {
                    val newGroupId = "grp_${UUID.randomUUID()}"
                    val groupNumber = state.tabGroups.size + 1
                    val newGroup = TabGroupInfo(id = newGroupId, title = "Group $groupNumber")

                    val updatedTabs = state.tabs.map { tab ->
                        if (tab.id == targetId || tab.id == draggedId) {
                            tab.copy(groupId = newGroupId)
                        } else {
                            tab
                        }
                    }
                    normalizeGroups(
                        tabs = updatedTabs,
                        groups = state.tabGroups + newGroup
                    ).let { (cleanTabs, cleanGroups) ->
                        state.copy(tabs = cleanTabs, tabGroups = cleanGroups)
                    }
                }

                !draggedIsGroup && targetIsGroup -> {
                    val updatedTabs = state.tabs.map { tab ->
                        if (tab.id == draggedId) tab.copy(groupId = targetId) else tab
                    }
                    normalizeGroups(updatedTabs, state.tabGroups).let { (cleanTabs, cleanGroups) ->
                        state.copy(tabs = cleanTabs, tabGroups = cleanGroups)
                    }
                }

                draggedIsGroup && !targetIsGroup -> {
                    val updatedTabs = state.tabs.map { tab ->
                        if (tab.id == targetId) tab.copy(groupId = draggedId) else tab
                    }
                    normalizeGroups(updatedTabs, state.tabGroups).let { (cleanTabs, cleanGroups) ->
                        state.copy(tabs = cleanTabs, tabGroups = cleanGroups)
                    }
                }

                else -> {
                    val updatedTabs = state.tabs.map { tab ->
                        if (tab.groupId == draggedId) tab.copy(groupId = targetId) else tab
                    }
                    normalizeGroups(updatedTabs, state.tabGroups).let { (cleanTabs, cleanGroups) ->
                        state.copy(
                            tabs = cleanTabs,
                            tabGroups = cleanGroups,
                            expandedGroupId = if (state.expandedGroupId == draggedId) targetId else state.expandedGroupId
                        )
                    }
                }
            }
        }
    }

    fun ungroupTab(tabId: String) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == tabId) tab.copy(groupId = null) else tab
            }
            val (cleanTabs, cleanGroups) = normalizeGroups(updatedTabs, state.tabGroups)
            val validExpanded = state.expandedGroupId?.takeIf { expId ->
                cleanGroups.any { it.id == expId }
            }
            state.copy(
                tabs = cleanTabs,
                tabGroups = cleanGroups,
                expandedGroupId = validExpanded
            )
        }
    }

    fun setExpandedGroup(groupId: String?) {
        _uiState.update { it.copy(expandedGroupId = groupId) }
    }

    fun closeGroup(groupId: String) {
        val groupTabIds = _uiState.value.tabs.filter { it.groupId == groupId }.map { it.id }.toSet()
        groupTabIds.forEach { id -> webViewRegistry.remove(id)?.destroy() }

        _uiState.update { state ->
            val remaining = state.tabs.filterNot { it.id in groupTabIds }
            if (remaining.isEmpty()) {
                val fresh = BrowserTab(title = "Home", url = "apex://home")
                state.copy(
                    tabs = listOf(fresh),
                    tabGroups = emptyList(),
                    expandedGroupId = null,
                    activeTabId = fresh.id
                )
            } else {
                val (cleanTabs, cleanGroups) = normalizeGroups(remaining, state.tabGroups)
                val nextActive = if (state.activeTabId in groupTabIds) {
                    cleanTabs.last().id
                } else {
                    state.activeTabId
                }
                state.copy(
                    tabs = cleanTabs,
                    tabGroups = cleanGroups,
                    expandedGroupId = if (state.expandedGroupId == groupId) null else state.expandedGroupId,
                    activeTabId = nextActive
                )
            }
        }
    }

    fun closeTab(tabId: String) {
        webViewRegistry.remove(tabId)?.destroy()
        _uiState.update { state ->
            val remaining = state.tabs.filterNot { it.id == tabId }
            if (remaining.isEmpty()) {
                val fresh = BrowserTab(title = "Home", url = "apex://home")
                state.copy(
                    tabs = listOf(fresh),
                    tabGroups = emptyList(),
                    expandedGroupId = null,
                    activeTabId = fresh.id
                )
            } else {
                val (cleanTabs, cleanGroups) = normalizeGroups(remaining, state.tabGroups)
                val nextActiveId = if (state.activeTabId == tabId) {
                    cleanTabs.last().id
                } else {
                    state.activeTabId
                }
                val validExpanded = state.expandedGroupId?.takeIf { expId ->
                    cleanGroups.any { it.id == expId }
                }
                state.copy(
                    tabs = cleanTabs,
                    tabGroups = cleanGroups,
                    expandedGroupId = validExpanded,
                    activeTabId = nextActiveId
                )
            }
        }
    }

    private fun normalizeGroups(
        tabs: List<BrowserTab>,
        groups: List<TabGroupInfo>
    ): Pair<List<BrowserTab>, List<TabGroupInfo>> {
        val countsByGroup = tabs.mapNotNull { it.groupId }.groupingBy { it }.eachCount()
        val validGroupIds = countsByGroup.filterValues { it >= 2 }.keys
        val cleanedTabs = tabs.map { tab ->
            if (tab.groupId != null && tab.groupId !in validGroupIds) {
                tab.copy(groupId = null)
            } else {
                tab
            }
        }
        val cleanedGroups = groups.filter { it.id in validGroupIds }
        return cleanedTabs to cleanedGroups
    }

    fun resetAllTabs() {
        webViewRegistry.values.forEach { it.destroy() }
        webViewRegistry.clear()
        val fresh = BrowserTab(title = "Home", url = "apex://home")
        _uiState.update {
            it.copy(
                tabs = listOf(fresh),
                tabGroups = emptyList(),
                expandedGroupId = null,
                activeTabId = fresh.id,
                isTabSwitcherOpen = false
            )
        }
    }

    // --- Context-Aware Bottom Bar Controls (Home, Bookmarks, Search, Tabs) ---

    fun navigateBack() {
        val active = _uiState.value.activeTab
        val wv = webViewRegistry[active.id]
        if (!active.isHomePage && wv != null && wv.canGoBack()) {
            wv.goBack()
        } else if (!active.isHomePage) {
            _uiState.update { state ->
                state.copy(
                    tabs = state.tabs.map { tab ->
                        if (tab.id == active.id) {
                            tab.copy(url = "apex://home", title = "Home", isLoading = false, blockedCountOnPage = 0)
                        } else {
                            tab
                        }
                    },
                    isToolbarVisible = true
                )
            }
        }
    }

    fun onBottomHomePressed() {
        val currentState = _uiState.value
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == currentState.activeTabId && !tab.isHomePage) {
                        tab.copy(
                            url = "apex://home",
                            title = "Home",
                            isLoading = false,
                            blockedCountOnPage = 0
                        )
                    } else {
                        tab
                    }
                },
                isBookmarksScreenOpen = false,
                isSettingsScreenOpen = false,
                isSearchScreenOpen = false,
                isTabSwitcherOpen = false,
                isAutomationOpen = false,
                isToolbarVisible = true
            )
        }
    }

    fun onBottomBookmarksPressed() {
        val state = _uiState.value
        if (state.isBookmarksScreenOpen) {
            _uiState.update { it.copy(isBookmarksScreenOpen = false) }
            return
        }
        if (state.activeTab.isHomePage || state.isSettingsScreenOpen) {
            _uiState.update {
                it.copy(
                    isBookmarksScreenOpen = true,
                    isSettingsScreenOpen = false,
                    isSearchScreenOpen = false,
                    isTabSwitcherOpen = false,
                    isAutomationOpen = false,
                    isToolbarVisible = true
                )
            }
        } else {
            val active = state.activeTab
            viewModelScope.launch {
                repository.saveBookmark(url = active.url, title = active.title)
                _uiState.update {
                    it.copy(
                        isTabSwitcherOpen = false,
                        isSettingsScreenOpen = false,
                        isSearchScreenOpen = false,
                        snackbarNotice = "Bookmarked '${active.title}'"
                    )
                }
            }
        }
    }

    fun setBookmarksScreenOpen(open: Boolean) {
        _uiState.update {
            it.copy(
                isBookmarksScreenOpen = open,
                isSettingsScreenOpen = if (open) false else it.isSettingsScreenOpen,
                isSearchScreenOpen = if (open) false else it.isSearchScreenOpen,
                isTabSwitcherOpen = if (open) false else it.isTabSwitcherOpen,
                isToolbarVisible = true
            )
        }
    }

    fun setSettingsScreenOpen(open: Boolean) {
        _uiState.update {
            it.copy(
                isSettingsScreenOpen = open,
                isBookmarksScreenOpen = if (open) false else it.isBookmarksScreenOpen,
                isSearchScreenOpen = if (open) false else it.isSearchScreenOpen,
                isTabSwitcherOpen = if (open) false else it.isTabSwitcherOpen,
                isAutomationOpen = if (open) false else it.isAutomationOpen,
                isToolbarVisible = true
            )
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { repository.deleteBookmark(id) }
    }

    fun reloadActiveTab() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) return
        val builtIn = com.example.engine.BuiltInShowcasePages.resolveHtml(active.url)
        val wv = webViewRegistry[active.id]
        if (wv != null) {
            if (builtIn != null) {
                wv.loadDataWithBaseURL(builtIn.first, builtIn.second, "text/html", "UTF-8", null)
            } else {
                wv.reload()
            }
        }
    }

    fun setTabSwitcherOpen(open: Boolean) {
        _uiState.update {
            it.copy(
                isTabSwitcherOpen = open,
                expandedGroupId = if (!open) null else it.expandedGroupId,
                isBookmarksScreenOpen = if (open) false else it.isBookmarksScreenOpen,
                isSettingsScreenOpen = if (open) false else it.isSettingsScreenOpen,
                isSearchScreenOpen = if (open) false else it.isSearchScreenOpen,
                isToolbarVisible = true
            )
        }
    }

    fun setAutomationOpen(open: Boolean, module: ExtensionModule = _uiState.value.activeExtension) {
        _uiState.update {
            it.copy(
                isAutomationOpen = open,
                activeExtension = module,
                isSettingsScreenOpen = if (open) false else it.isSettingsScreenOpen,
                isSearchScreenOpen = if (open) false else it.isSearchScreenOpen,
                isToolbarVisible = true
            )
        }
    }

    fun selectExtensionModule(module: ExtensionModule) {
        _uiState.update { it.copy(activeExtension = module) }
    }

    // --- Preferences & Settings Controls ---

    fun toggleShields(enabled: Boolean) {
        viewModelScope.launch { repository.setShieldsEnabled(enabled) }
    }

    fun toggleHttpsUpgrade(enabled: Boolean) {
        viewModelScope.launch { repository.setHttpsUpgradeEnabled(enabled) }
    }

    fun toggleAggressivePreload(enabled: Boolean) {
        viewModelScope.launch { repository.setAggressivePreloadEnabled(enabled) }
    }

    fun toggleScriptGuard(enabled: Boolean) {
        viewModelScope.launch { repository.setScriptGuardEnabled(enabled) }
    }

    fun toggleDesktopViewport(enabled: Boolean) {
        viewModelScope.launch { repository.setDesktopViewportEnabled(enabled) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColorEnabled(enabled) }
    }

    fun selectSeedPalette(preset: SeedPalettePreset) {
        viewModelScope.launch { repository.setSeedPaletteOrdinal(preset.ordinal) }
    }

    fun cycleSeedTheme() {
        viewModelScope.launch { repository.cycleSeedPalette() }
    }

    fun resetPrivacyStats() {
        viewModelScope.launch {
            repository.resetTelemetryStats()
            _uiState.update {
                it.copy(snackbarNotice = "Search history and browsing data cleared")
            }
        }
    }

    // --- Extension 1: Nano Multi-Agent Automation ---

    fun runNanoTask(userPrompt: String) {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isEmpty() || _uiState.value.isNanoRunning) return

        val userMsg = NanoChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = cleanPrompt
        )
        val agentMsgId = "a_${System.currentTimeMillis()}"
        val placeholderAgentMsg = NanoChatMessage(
            id = agentMsgId,
            isUser = false,
            text = "Executing multi-agent DOM workflow…",
            steps = emptyList(),
            isRunning = true
        )

        _uiState.update {
            it.copy(
                nanoMessages = it.nanoMessages + userMsg + placeholderAgentMsg,
                isNanoRunning = true
            )
        }

        viewModelScope.launch {
            val startTabUrl = _uiState.value.activeTab.url
            val outcome = nanoEngine.runTask(
                prompt = cleanPrompt,
                currentUrl = startTabUrl,
                webViewProvider = { webViewRegistry[_uiState.value.activeTabId] },
                onNavigateUrl = { destUrl ->
                    submitNavigation(destUrl, _uiState.value.activeTabId)
                },
                onStepUpdate = { liveSteps ->
                    _uiState.update { state ->
                        state.copy(
                            nanoMessages = state.nanoMessages.map { msg ->
                                if (msg.id == agentMsgId) msg.copy(steps = liveSteps) else msg
                            }
                        )
                    }
                }
            )

            _uiState.update { state ->
                state.copy(
                    isNanoRunning = false,
                    nanoMessages = state.nanoMessages.map { msg ->
                        if (msg.id == agentMsgId) {
                            msg.copy(
                                text = outcome.finalSummary,
                                isRunning = false
                            )
                        } else {
                            msg
                        }
                    }
                )
            }
        }
    }

    // --- Extension 2: Retriever Local-First Web Scraper ---

    fun onRetrieverPromptChanged(value: String) {
        _uiState.update { it.copy(retrieverPromptInput = value) }
    }

    fun selectExtractionRule(ruleId: Long) {
        val rule = extractionRulesState.value.firstOrNull { it.id == ruleId }
        _uiState.update {
            it.copy(
                selectedRuleId = ruleId,
                retrieverPromptInput = rule?.promptDescription ?: it.retrieverPromptInput
            )
        }
    }

    fun setRetrieverDisplayMode(mode: RetrieverDisplayMode) {
        _uiState.update { it.copy(retrieverDisplayMode = mode) }
    }

    fun saveCurrentRetrieverRule() {
        val input = _uiState.value.retrieverPromptInput.trim()
        if (input.isEmpty()) return
        viewModelScope.launch {
            val built = retrieverEngine.buildRuleFromInput(input)
            val newId = repository.saveExtractionRule(built)
            _uiState.update {
                it.copy(
                    selectedRuleId = newId,
                    retrieverStatusBanner = "Saved rule '${built.name}' locally"
                )
            }
        }
    }

    fun deleteRetrieverRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteExtractionRule(ruleId)
            if (_uiState.value.selectedRuleId == ruleId) {
                _uiState.update { it.copy(selectedRuleId = null) }
            }
        }
    }

    fun captureCurrentPageWithRetriever() {
        val state = _uiState.value
        if (state.isRetrieverRunning) return
        val rule = resolveActiveRule(state)
        val activeTab = state.activeTab

        _uiState.update {
            it.copy(
                isRetrieverRunning = true,
                retrieverStatusBanner = "Capturing DOM from '${activeTab.title}'…"
            )
        }

        viewModelScope.launch {
            val target = TabSnapshotTarget(
                tabId = activeTab.id,
                title = activeTab.title,
                url = activeTab.url
            )
            val records = retrieverEngine.extractFromSingleTab(
                target = target,
                webView = webViewRegistry[activeTab.id],
                rule = rule
            )
            if (rule.id != 0L) {
                repository.updateRuleRunCount(rule.id, records.size)
            }
            _uiState.update {
                it.copy(
                    isRetrieverRunning = false,
                    extractedRecords = records,
                    retrieverStatusBanner = "Captured ${records.size} structured rows from '${activeTab.title}'"
                )
            }
        }
    }

    fun runParallelMultiTabExtraction() {
        val state = _uiState.value
        if (state.isRetrieverRunning) return
        val rule = resolveActiveRule(state)
        val targets = state.tabs.map {
            TabSnapshotTarget(tabId = it.id, title = it.title, url = it.url)
        }

        _uiState.update {
            it.copy(
                isRetrieverRunning = true,
                retrieverStatusBanner = "Running parallel DOM extraction across ${targets.size} open tabs…"
            )
        }

        viewModelScope.launch {
            val records = retrieverEngine.extractAcrossTabsInParallel(
                tabs = targets,
                webViewLookup = { id -> webViewRegistry[id] },
                rule = rule
            )
            if (rule.id != 0L) {
                repository.updateRuleRunCount(rule.id, records.size)
            }
            _uiState.update {
                it.copy(
                    isRetrieverRunning = false,
                    extractedRecords = records,
                    retrieverStatusBanner = "Parallel run complete: ${records.size} rows across ${targets.size} tabs"
                )
            }
        }
    }

    private fun resolveActiveRule(state: BrowserUiState): ExtractionRuleEntity {
        val rules = extractionRulesState.value
        val byId = state.selectedRuleId?.let { id -> rules.firstOrNull { it.id == id } }
        if (byId != null && (state.retrieverPromptInput.isBlank() || state.retrieverPromptInput == byId.promptDescription)) {
            return byId
        }
        if (state.retrieverPromptInput.isNotBlank()) {
            return retrieverEngine.buildRuleFromInput(state.retrieverPromptInput)
        }
        return rules.firstOrNull() ?: retrieverEngine.buildRuleFromInput("Extract headlines, prices, and links")
    }

    private fun mapBaseUrlBackToScheme(loadedUrl: String, currentTabUrl: String): String {
        return when {
            loadedUrl.startsWith("https://news.apex.browser") -> "apex://news"
            loadedUrl.startsWith("https://store.apex.browser") -> "apex://store"
            currentTabUrl.startsWith("apex://") && loadedUrl == "about:blank" -> currentTabUrl
            else -> loadedUrl
        }
    }

    private fun deriveShortTitle(url: String): String {
        return when (url.lowercase()) {
            "apex://home" -> "Home"
            "apex://news" -> "AI News Pulse"
            "apex://store" -> "Apex Gear Store"
            else -> url.removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .substringBefore('/')
                .ifBlank { "Web Tab" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        suggestionJob?.cancel()
        webViewRegistry.values.forEach { it.destroy() }
        webViewRegistry.clear()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = ApexDatabase.getInstance(appContext)
                    val prefsRepo = UserPreferencesRepository(appContext)
                    val repo = BrowserRepository(database.apexDao(), prefsRepo)
                    return BrowserViewModel(repository = repo) as T
                }
            }
        }
    }
}
