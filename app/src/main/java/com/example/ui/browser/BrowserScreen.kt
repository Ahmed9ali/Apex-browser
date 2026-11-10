package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.engine.OfflineStorageUtil
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
    val bookmarks by viewModel.bookmarksState.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistoryState.collectAsStateWithLifecycle()
    val offlinePages by viewModel.offlinePagesState.collectAsStateWithLifecycle()
    val offlinePagesCount by viewModel.offlinePagesCountState.collectAsStateWithLifecycle()
    val topVisits by viewModel.topVisitsState.collectAsStateWithLifecycle()

    val reducedMotion = LocalReducedMotion.current
    val snackbarHostState = remember { SnackbarHostState() }
    val viewBookmarksLabel = stringResource(R.string.action_view_bookmarks)

    val isWideScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val activeTab = uiState.activeTab
    val canNavigateBack = !activeTab.isHomePage || activeTab.canGoBack
    val isCurrentWebPageBookmarked = remember(activeTab.url, bookmarks) {
        !activeTab.isHomePage && bookmarks.any { it.url.equals(activeTab.url, ignoreCase = true) }
    }

    LaunchedEffect(uiState.snackbarNotice) {
        val notice = uiState.snackbarNotice ?: return@LaunchedEffect
        val isBookmarkNotice = notice.startsWith("Bookmarked")
        val result = snackbarHostState.showSnackbar(
            message = notice,
            actionLabel = if (isBookmarkNotice) viewBookmarksLabel else null,
            duration = SnackbarDuration.Short
        )
        viewModel.consumeSnackbarNotice()
        if (isBookmarkNotice && result == SnackbarResult.ActionPerformed) {
            viewModel.setBookmarksScreenOpen(true)
        }
    }

    BackHandler(
        enabled = canNavigateBack &&
            !uiState.isTabSwitcherOpen &&
            !uiState.isBookmarksScreenOpen &&
            !uiState.isOfflinePagesScreenOpen &&
            !uiState.isSettingsScreenOpen &&
            !uiState.isSearchScreenOpen &&
            !uiState.isMenuSheetOpen
    ) {
        viewModel.navigateBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!uiState.isSearchScreenOpen &&
                !uiState.isSettingsScreenOpen &&
                !uiState.isBookmarksScreenOpen &&
                !uiState.isOfflinePagesScreenOpen &&
                !uiState.isTabSwitcherOpen
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CompactTopToolbar(
                        visible = uiState.isToolbarVisible || activeTab.isHomePage,
                        activeTab = activeTab,
                        onAddressPillClick = {
                            viewModel.openSearchScreen(prefillCurrentUrl = !activeTab.isHomePage)
                        },
                        onReloadClick = viewModel::reloadActiveTab,
                        searchEngine = preferences.searchEngine
                    )

                    if (activeTab.isOfflineCopy) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("offline_copy_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudDone,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Offline Mode • Saved ${OfflineStorageUtil.formatSavedTimestamp(activeTab.offlineSavedAt ?: 0L)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.reloadOnline(activeTab.id) },
                                    modifier = Modifier.testTag("offline_banner_reload_online")
                                ) {
                                    Text(
                                        text = stringResource(R.string.offline_banner_try_online),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

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
                !uiState.isSettingsScreenOpen &&
                !uiState.isSearchScreenOpen &&
                !uiState.isMenuSheetOpen
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
            if (!uiState.isTabSwitcherOpen &&
                !uiState.isSearchScreenOpen &&
                !uiState.isSettingsScreenOpen &&
                !uiState.isBookmarksScreenOpen &&
                !uiState.isOfflinePagesScreenOpen
            ) {
                val activeGroup = uiState.activeTabGroup
                val showGroupStrip = activeGroup != null

                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = showGroupStrip,
                        enter = if (reducedMotion) {
                            fadeIn()
                        } else {
                            expandVertically(animationSpec = ApexMotion.spatialSpring()) +
                                fadeIn(animationSpec = ApexMotion.effectsSpring())
                        },
                        exit = if (reducedMotion) {
                            fadeOut()
                        } else {
                            shrinkVertically(animationSpec = ApexMotion.spatialSpring()) +
                                fadeOut(animationSpec = ApexMotion.effectsSpring())
                        }
                    ) {
                        if (activeGroup != null) {
                            BottomGroupTabStrip(
                                group = activeGroup,
                                groupTabs = uiState.activeGroupTabs,
                                activeTabId = uiState.activeTabId,
                                onSelectTab = viewModel::selectTab,
                                onCloseTab = viewModel::closeTab,
                                onAddTabToGroup = {
                                    viewModel.createNewTab(
                                        groupId = activeGroup.id,
                                        stayInSwitcher = false
                                    )
                                },
                                onEditGroup = { viewModel.openGroupEditor(activeGroup.id) }
                            )
                        }
                    }

                    BraveBottomNavigationBar(
                        isHomeActive = activeTab.isHomePage,
                        isBookmarksScreenOpen = uiState.isBookmarksScreenOpen,
                        isSettingsScreenOpen = uiState.isSettingsScreenOpen,
                        isMenuSheetOpen = uiState.isMenuSheetOpen,
                        isCurrentWebPageBookmarked = isCurrentWebPageBookmarked,
                        isTabSwitcherOpen = uiState.isTabSwitcherOpen,
                        isSearchScreenOpen = uiState.isSearchScreenOpen,
                        openTabCount = uiState.tabs.size,
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
                        onMenuClick = viewModel::toggleMenuSheet
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val (enterOverlay, exitOverlay) = ApexMotion.containerTransformSpec(reducedMotion)

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
                            StartPageHome(
                                preferences = preferences,
                                topVisits = topVisits,
                                onOpenSearchScreen = {
                                    viewModel.openSearchScreen(prefillCurrentUrl = false)
                                },
                                onSelectDestination = { url ->
                                    viewModel.submitNavigation(url, currentTab.id)
                                },
                                onAddTopVisit = viewModel::addTopVisit,
                                onDeleteTopVisit = viewModel::deleteTopVisit,
                                offlinePagesCount = offlinePagesCount,
                                onOpenOfflinePagesScreen = {
                                    viewModel.setOfflinePagesScreenOpen(true)
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
                                onScrollDelta = viewModel::onWebViewScrollDelta,
                                onPageLoadFailed = { tabId, failedUrl ->
                                    viewModel.onPageLoadFailed(tabId, failedUrl)
                                }
                            )
                        }
                    }
                }
            }

            // Dedicated Search Screen
            AnimatedVisibility(
                visible = uiState.isSearchScreenOpen,
                enter = fadeIn(tween(140)),
                exit = fadeOut(tween(120)),
                modifier = Modifier.fillMaxSize()
            ) {
                SearchScreenOverlay(
                    query = uiState.searchQuery,
                    selectedEngine = preferences.searchEngine,
                    suggestions = uiState.searchSuggestions,
                    isLoadingSuggestions = uiState.isLoadingSuggestions,
                    recentHistory = searchHistory,
                    activeTabUrl = if (!activeTab.isHomePage) activeTab.url else null,
                    activeTabTitle = if (!activeTab.isHomePage) activeTab.title else null,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onSubmitSearch = { submitted -> viewModel.submitNavigation(submitted) },
                    onFillQuery = viewModel::onSearchQueryChanged,
                    onDeleteHistoryItem = viewModel::deleteSearchHistoryItem,
                    onClearAllHistory = viewModel::clearAllSearchHistory,
                    onDismiss = viewModel::closeSearchScreen
                )
            }

            // Settings Screen Overlay (Opened from Hamburger Menu Bottom Sheet)
            AnimatedVisibility(
                visible = uiState.isSettingsScreenOpen,
                enter = if (reducedMotion) fadeIn() else enterOverlay,
                exit = if (reducedMotion) fadeOut() else exitOverlay,
                modifier = Modifier.fillMaxSize()
            ) {
                SettingsScreenOverlay(
                    preferences = preferences,
                    onSelectSearchEngine = viewModel::selectSearchEngine,
                    onToggleShields = viewModel::toggleShields,
                    onToggleHttpsUpgrade = viewModel::toggleHttpsUpgrade,
                    onToggleScriptGuard = viewModel::toggleScriptGuard,
                    onToggleSmartPreload = viewModel::toggleAggressivePreload,
                    onToggleDesktopViewport = viewModel::toggleDesktopViewport,
                    onToggleDynamicColor = viewModel::toggleDynamicColor,
                    onSelectSeedPalette = viewModel::selectSeedPalette,
                    onClearBrowsingData = viewModel::resetPrivacyStats,
                    onToggleAutoSaveOfflinePages = viewModel::setAutoSaveOfflinePagesEnabled,
                    onClearOfflinePages = viewModel::clearAllOfflinePages,
                    onDismiss = { viewModel.setSettingsScreenOpen(false) }
                )
            }

            // Offline Pages Screen Overlay
            AnimatedVisibility(
                visible = uiState.isOfflinePagesScreenOpen,
                enter = if (reducedMotion) fadeIn() else enterOverlay,
                exit = if (reducedMotion) fadeOut() else exitOverlay,
                modifier = Modifier.fillMaxSize()
            ) {
                OfflinePagesScreenOverlay(
                    offlinePages = offlinePages,
                    onSelectOfflinePage = { url -> viewModel.loadOfflinePage(url) },
                    onDeleteOfflinePage = { id -> viewModel.deleteOfflinePage(id) },
                    onClearAllOfflinePages = viewModel::clearAllOfflinePages,
                    onDismiss = { viewModel.setOfflinePagesScreenOpen(false) }
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

            // Draggable Grid Tab Switcher Overlay (No Bottom Nav — Centered Pills)
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
                    onEditGroup = viewModel::openGroupEditor,
                    onMergeEntries = viewModel::mergeGridEntriesIntoGroup,
                    onMoveGroupedTabToTarget = viewModel::moveGroupedTabToGridTarget,
                    onOpenGroup = viewModel::setExpandedGroup,
                    onAddTabToGroup = { grpId -> viewModel.createNewTab(groupId = grpId) },
                    onUngroupTab = viewModel::ungroupTab,
                    onCycleTabColor = viewModel::cycleTabColor,
                    onNewTab = { viewModel.createNewTab() },
                    onCreateGroup = viewModel::createNewTabGroupFromSwitcher,
                    onResetAllTabs = viewModel::resetAllTabs,
                    onDismiss = { viewModel.setTabSwitcherOpen(false) }
                )
            }

            // Hamburger Menu ModalBottomSheet (contains Settings & Quick Browser Actions)
            if (uiState.isMenuSheetOpen) {
                BrowserMenuBottomSheet(
                    isHomeActive = activeTab.isHomePage,
                    desktopViewportEnabled = preferences.desktopViewportEnabled,
                    savedOfflinePagesCount = offlinePagesCount,
                    onOpenSettings = { viewModel.setSettingsScreenOpen(true) },
                    onNewTabClick = { viewModel.createNewTab() },
                    onOpenBookmarksClick = { viewModel.setBookmarksScreenOpen(true) },
                    onSaveOfflineClick = viewModel::saveCurrentPageForOffline,
                    onOpenOfflinePagesClick = { viewModel.setOfflinePagesScreenOpen(true) },
                    onReloadPageClick = viewModel::reloadActiveTab,
                    onToggleDesktopMode = viewModel::toggleDesktopViewport,
                    onClearBrowsingData = viewModel::resetPrivacyStats,
                    onDismiss = { viewModel.setMenuSheetOpen(false) }
                )
            }

            // Group Rename & Tonal Color Picker Dialog
            val groupToEdit = uiState.editingGroup
            if (groupToEdit != null) {
                GroupRenameColorDialog(
                    group = groupToEdit,
                    onSave = { newTitle, selectedColor ->
                        viewModel.updateGroupTitleAndColor(
                            groupId = groupToEdit.id,
                            newTitle = newTitle,
                            colorOption = selectedColor
                        )
                    },
                    onUngroupAll = { viewModel.ungroupAllInGroup(groupToEdit.id) },
                    onDismiss = viewModel::dismissGroupEditor
                )
            }
        }
    }
}
