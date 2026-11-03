package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.viewmodel.BrowserViewModel

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferencesState.collectAsStateWithLifecycle()
    val privacyStats by viewModel.privacyStatsState.collectAsStateWithLifecycle()
    val blockedLogs by viewModel.blockedLogsState.collectAsStateWithLifecycle()
    val extractionRules by viewModel.extractionRulesState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarksState.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistoryState.collectAsStateWithLifecycle()

    val reducedMotion = LocalReducedMotion.current
    val snackbarHostState = remember { SnackbarHostState() }
    val viewBookmarksLabel = stringResource(R.string.action_view_bookmarks)

    val isWideScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    val activeTab = uiState.activeTab
    val canNavigateBack = !activeTab.isHomePage || activeTab.canGoBack
    val isCurrentWebPageBookmarked = remember(activeTab.url, bookmarks) {
        !activeTab.isHomePage && bookmarks.any { it.url.equals(activeTab.url, ignoreCase = true) }
    }

    LaunchedEffect(uiState.snackbarNotice) {
        val notice = uiState.snackbarNotice ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = notice,
            actionLabel = viewBookmarksLabel,
            duration = SnackbarDuration.Short
        )
        viewModel.consumeSnackbarNotice()
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.setBookmarksScreenOpen(true)
        }
    }

    BackHandler(
        enabled = canNavigateBack &&
            !uiState.isTabSwitcherOpen &&
            !uiState.isBookmarksScreenOpen &&
            !uiState.isSearchScreenOpen &&
            !uiState.isAutomationOpen
    ) {
        viewModel.navigateBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!uiState.isSearchScreenOpen) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CompactTopToolbar(
                        visible = uiState.isToolbarVisible || activeTab.isHomePage,
                        activeTab = activeTab,
                        searchEngine = preferences.searchEngine,
                        shieldsEnabled = preferences.shieldsEnabled,
                        onAddressPillClick = {
                            viewModel.openSearchScreen(prefillCurrentUrl = !activeTab.isHomePage)
                        },
                        onReloadClick = viewModel::reloadActiveTab,
                        onShieldClick = { viewModel.setShieldSheetOpen(true) }
                    )

                    if (isWideScreen) {
                        ExpressiveHorizontalTabStrip(
                            tabs = uiState.tabs,
                            activeTabId = uiState.activeTabId,
                            onSelectTab = viewModel::selectTab,
                            onCloseTab = viewModel::closeTab,
                            onNewTab = { viewModel.createNewTab() }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeTab.isHomePage &&
                !uiState.isTabSwitcherOpen &&
                !uiState.isBookmarksScreenOpen &&
                !uiState.isSearchScreenOpen &&
                !uiState.isAutomationOpen
            ) {
                FloatingActionButton(
                    onClick = { viewModel.createNewTab() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("home_new_tab_fab")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.fab_new_tab)
                    )
                }
            }
        },
        bottomBar = {
            BraveBottomNavigationBar(
                isHomeActive = activeTab.isHomePage,
                isBookmarksScreenOpen = uiState.isBookmarksScreenOpen,
                isCurrentWebPageBookmarked = isCurrentWebPageBookmarked,
                isTabSwitcherOpen = uiState.isTabSwitcherOpen,
                isSearchScreenOpen = uiState.isSearchScreenOpen,
                openTabCount = uiState.tabs.size,
                desktopViewportEnabled = preferences.desktopViewportEnabled,
                onHomeClick = viewModel::onBottomHomePressed,
                onBookmarksClick = viewModel::onBottomBookmarksPressed,
                onSearchClick = {
                    if (uiState.isSearchScreenOpen) {
                        viewModel.closeSearchScreen()
                    } else {
                        viewModel.openSearchScreen(prefillCurrentUrl = false)
                    }
                },
                onTabsClick = { viewModel.setTabSwitcherOpen(!uiState.isTabSwitcherOpen) },
                onOpenAutomation = { module -> viewModel.setAutomationOpen(true, module) },
                onNewTabClick = { viewModel.createNewTab() },
                onOpenBookmarksScreenExplicit = { viewModel.setBookmarksScreenOpen(true) },
                onOpenShieldSheet = { viewModel.setShieldSheetOpen(true) },
                onToggleDesktopMode = viewModel::toggleDesktopViewport,
                onCycleSeedPalette = viewModel::cycleSeedTheme,
                onResetStats = viewModel::resetPrivacyStats
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Main Browser Content Area with Shared-Axis Tab Switching
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val forwardSwitch = uiState.activeTabIndex >= uiState.previousTabIndex
                    AnimatedContent(
                        targetState = activeTab,
                        contentKey = { it.id },
                        transitionSpec = {
                            ApexMotion.sharedAxisTabTransition(
                                forward = forwardSwitch,
                                reducedMotion = reducedMotion
                            )
                        },
                        label = "SharedAxisTabContent"
                    ) { currentTab ->
                        if (currentTab.isHomePage) {
                            StartPagePrivacyHub(
                                privacyStats = privacyStats,
                                preferences = preferences,
                                bookmarks = bookmarks,
                                onOpenSearchScreen = {
                                    viewModel.openSearchScreen(prefillCurrentUrl = false)
                                },
                                onToggleShields = viewModel::toggleShields,
                                onToggleHttps = viewModel::toggleHttpsUpgrade,
                                onTogglePreload = viewModel::toggleAggressivePreload,
                                onToggleScriptGuard = viewModel::toggleScriptGuard,
                                onSelectDestination = { url ->
                                    viewModel.submitNavigation(url, currentTab.id)
                                },
                                onOpenExtension = { ext ->
                                    viewModel.setAutomationOpen(true, ext)
                                }
                            )
                        } else {
                            WebViewViewport(
                                tab = currentTab,
                                preferences = preferences,
                                privacyManager = viewModel.privacyManager,
                                performanceOptimizer = viewModel.performanceOptimizer,
                                existingWebView = viewModel.getWebView(currentTab.id),
                                onRegisterWebView = viewModel::registerWebView,
                                onPageStarted = viewModel::onWebViewPageStarted,
                                onProgressChanged = viewModel::onWebViewProgress,
                                onPageFinished = viewModel::onWebViewPageFinished,
                                onSubresourceBlocked = viewModel::onSubresourceBlocked,
                                onHttpsUpgradeTriggered = { _ ->
                                    viewModel.submitNavigation(currentTab.url, currentTab.id)
                                },
                                onScrollDelta = viewModel::onWebViewScrollDelta
                            )
                        }
                    }
                }

                // Supporting Side Pane for Automation Extensions on Expanded Tablets/Desktop
                val (enterContainer, exitContainer) = ApexMotion.containerTransformSpec(reducedMotion)
                AnimatedVisibility(
                    visible = isExpandedScreen && uiState.isAutomationOpen,
                    enter = enterContainer,
                    exit = exitContainer
                ) {
                    AutomationPanel(
                        activeModule = uiState.activeExtension,
                        nanoMessages = uiState.nanoMessages,
                        isNanoRunning = uiState.isNanoRunning,
                        extractionRules = extractionRules,
                        selectedRuleId = uiState.selectedRuleId,
                        retrieverPromptInput = uiState.retrieverPromptInput,
                        extractedRecords = uiState.extractedRecords,
                        isRetrieverRunning = uiState.isRetrieverRunning,
                        retrieverStatusBanner = uiState.retrieverStatusBanner,
                        retrieverDisplayMode = uiState.retrieverDisplayMode,
                        openTabsCount = uiState.tabs.size,
                        isSideSheet = true,
                        onSelectModule = viewModel::selectExtensionModule,
                        onRunNanoPrompt = viewModel::runNanoTask,
                        onRetrieverPromptChange = viewModel::onRetrieverPromptChanged,
                        onSelectRule = viewModel::selectExtractionRule,
                        onSaveRule = viewModel::saveCurrentRetrieverRule,
                        onDeleteRule = viewModel::deleteRetrieverRule,
                        onCaptureCurrentPage = viewModel::captureCurrentPageWithRetriever,
                        onRunParallelAllTabs = viewModel::runParallelMultiTabExtraction,
                        onToggleDisplayMode = viewModel::setRetrieverDisplayMode,
                        onDismiss = { viewModel.setAutomationOpen(false) },
                        modifier = Modifier
                            .width(410.dp)
                            .padding(8.dp)
                    )
                }
            }

            // Container-Transform Overlay for Automation Extensions on Compact & Medium screens
            val (enterOverlay, exitOverlay) = ApexMotion.containerTransformSpec(reducedMotion)
            AnimatedVisibility(
                visible = !isExpandedScreen && uiState.isAutomationOpen,
                enter = enterOverlay,
                exit = exitOverlay,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.setAutomationOpen(false)
                        }
                ) {
                    AutomationPanel(
                        activeModule = uiState.activeExtension,
                        nanoMessages = uiState.nanoMessages,
                        isNanoRunning = uiState.isNanoRunning,
                        extractionRules = extractionRules,
                        selectedRuleId = uiState.selectedRuleId,
                        retrieverPromptInput = uiState.retrieverPromptInput,
                        extractedRecords = uiState.extractedRecords,
                        isRetrieverRunning = uiState.isRetrieverRunning,
                        retrieverStatusBanner = uiState.retrieverStatusBanner,
                        retrieverDisplayMode = uiState.retrieverDisplayMode,
                        openTabsCount = uiState.tabs.size,
                        isSideSheet = false,
                        onSelectModule = viewModel::selectExtensionModule,
                        onRunNanoPrompt = viewModel::runNanoTask,
                        onRetrieverPromptChange = viewModel::onRetrieverPromptChanged,
                        onSelectRule = viewModel::selectExtractionRule,
                        onSaveRule = viewModel::saveCurrentRetrieverRule,
                        onDeleteRule = viewModel::deleteRetrieverRule,
                        onCaptureCurrentPage = viewModel::captureCurrentPageWithRetriever,
                        onRunParallelAllTabs = viewModel::runParallelMultiTabExtraction,
                        onToggleDisplayMode = viewModel::setRetrieverDisplayMode,
                        onDismiss = { viewModel.setAutomationOpen(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.88f)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* consume tap inside panel */ }
                    )
                }
            }

            // Dedicated Search Screen with Search-Engine Suggestions
            AnimatedVisibility(
                visible = uiState.isSearchScreenOpen,
                enter = if (reducedMotion) fadeIn() else enterOverlay,
                exit = if (reducedMotion) fadeOut() else exitOverlay,
                modifier = Modifier.fillMaxSize()
            ) {
                SearchScreenOverlay(
                    query = uiState.searchQuery,
                    selectedEngine = preferences.searchEngine,
                    suggestions = uiState.searchSuggestions,
                    isLoadingSuggestions = uiState.isLoadingSuggestions,
                    recentHistory = searchHistory,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSelectEngine = viewModel::selectSearchEngine,
                    onSubmitSearch = { submitted -> viewModel.submitNavigation(submitted) },
                    onFillQuery = viewModel::onSearchQueryChanged,
                    onDeleteHistoryItem = viewModel::deleteSearchHistoryItem,
                    onClearAllHistory = viewModel::clearAllSearchHistory,
                    onDismiss = viewModel::closeSearchScreen
                )
            }

            // Bookmarks Screen Overlay
            AnimatedVisibility(
                visible = uiState.isBookmarksScreenOpen,
                enter = if (reducedMotion) fadeIn() else enterOverlay,
                exit = if (reducedMotion) fadeOut() else exitOverlay,
                modifier = Modifier.fillMaxSize()
            ) {
                BookmarksScreenOverlay(
                    bookmarks = bookmarks,
                    onSelectBookmark = { url -> viewModel.submitNavigation(url) },
                    onDeleteBookmark = viewModel::deleteBookmark,
                    onDismiss = { viewModel.setBookmarksScreenOpen(false) }
                )
            }

            // Draggable Grid Tab Switcher Overlay
            AnimatedVisibility(
                visible = uiState.isTabSwitcherOpen,
                enter = if (reducedMotion) fadeIn() else enterOverlay,
                exit = if (reducedMotion) fadeOut() else exitOverlay,
                modifier = Modifier.fillMaxSize()
            ) {
                DraggableTabGridOverlay(
                    gridEntries = uiState.gridEntries,
                    totalOpenTabs = uiState.tabs.size,
                    activeTabId = uiState.activeTabId,
                    expandedGroupId = uiState.expandedGroupId,
                    onSelectTab = viewModel::selectTab,
                    onCloseTab = viewModel::closeTab,
                    onCloseGroup = viewModel::closeGroup,
                    onMergeEntries = viewModel::mergeGridEntriesIntoGroup,
                    onOpenGroup = viewModel::setExpandedGroup,
                    onAddTabToGroup = { grpId -> viewModel.createNewTab(groupId = grpId) },
                    onUngroupTab = viewModel::ungroupTab,
                    onNewTab = { viewModel.createNewTab() },
                    onResetAllTabs = viewModel::resetAllTabs,
                    onDismiss = { viewModel.setTabSwitcherOpen(false) }
                )
            }
        }
    }

    if (uiState.isShieldSheetOpen) {
        ShieldAndPreloadModalSheet(
            activeTab = activeTab,
            privacyStats = privacyStats,
            preferences = preferences,
            preloadCandidates = uiState.preloadCandidates,
            blockedLogs = blockedLogs,
            onToggleShields = viewModel::toggleShields,
            onToggleHttps = viewModel::toggleHttpsUpgrade,
            onTogglePreload = viewModel::toggleAggressivePreload,
            onToggleScriptGuard = viewModel::toggleScriptGuard,
            onNavigateCandidate = { url -> viewModel.submitNavigation(url) },
            onDismiss = { viewModel.setShieldSheetOpen(false) }
        )
    }
}
