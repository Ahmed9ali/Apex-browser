package com.example.ui.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.SearchEngineOption
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.ExtensionModule

@Composable
fun CompactTopToolbar(
    visible: Boolean,
    activeTab: BrowserTab,
    searchEngine: SearchEngineOption,
    shieldsEnabled: Boolean,
    onAddressPillClick: () -> Unit,
    onReloadClick: () -> Unit,
    onShieldClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalReducedMotion.current

    val enterTransition = if (reducedMotion) {
        fadeIn()
    } else {
        expandVertically(animationSpec = ApexMotion.spatialSpring()) +
            fadeIn(animationSpec = ApexMotion.effectsSpring())
    }
    val exitTransition = if (reducedMotion) {
        fadeOut()
    } else {
        shrinkVertically(animationSpec = ApexMotion.spatialSpring()) +
            fadeOut(animationSpec = ApexMotion.effectsSpring())
    }

    AnimatedVisibility(
        visible = visible,
        enter = enterTransition,
        exit = exitTransition,
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val pillShape = rememberMorphingCornerShape(
                        targetRadius = if (activeTab.isHomePage) 28.dp else 18.dp,
                        label = "AddressBarShapeMorph"
                    )

                    Surface(
                        shape = pillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(pillShape)
                            .clickable(onClick = onAddressPillClick)
                            .testTag("address_bar_container")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = if (activeTab.isHomePage) {
                                    Icons.Filled.Search
                                } else {
                                    Icons.Filled.Lock
                                },
                                contentDescription = stringResource(R.string.cd_https_secure),
                                tint = if (activeTab.wasUpgradedToHttps) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (activeTab.isHomePage) {
                                    stringResource(R.string.search_screen_hint, searchEngine.displayName)
                                } else {
                                    activeTab.url
                                },
                                style = if (activeTab.isHomePage) {
                                    MaterialTheme.typography.bodyMedium
                                } else {
                                    EmphasizedTypography.addressBarActive
                                },
                                color = if (activeTab.isHomePage) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (!activeTab.isHomePage) {
                                IconButton(
                                    onClick = onReloadClick,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("reload_page_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = stringResource(R.string.cd_reload_page),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    val shieldContainer by animateColorAsState(
                        targetValue = if (shieldsEnabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = ApexMotion.effectsSpec(),
                        label = "ShieldContainerColor"
                    )

                    Surface(
                        shape = rememberMorphingCornerShape(
                            targetRadius = if (shieldsEnabled) 18.dp else 12.dp,
                            label = "ShieldCornerMorph"
                        ),
                        color = shieldContainer,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable(onClick = onShieldClick)
                            .testTag("privacy_shield_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(48.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (shieldsEnabled && activeTab.blockedCountOnPage > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiary
                                        ) {
                                            Text(
                                                text = activeTab.blockedCountOnPage.toString(),
                                                style = EmphasizedTypography.badgeCounter
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (shieldsEnabled) Icons.Filled.Security else Icons.Outlined.Shield,
                                    contentDescription = stringResource(R.string.cd_privacy_shield),
                                    tint = if (shieldsEnabled) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                if (activeTab.isLoading) {
                    LinearProgressIndicator(
                        progress = { activeTab.progress.coerceIn(0.08f, 1f) },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .testTag("page_loading_indicator")
                    )
                }
            }
        }
    }
}

/**
 * Ergonomic Bottom NavigationBar featuring:
 * Home • Bookmarks (opens screen on Home, bookmarks page on Web) • Search (opens Search Screen) • Tabs
 * plus the M3 Expressive Split Button for AI Extensions & Main Menu.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BraveBottomNavigationBar(
    isHomeActive: Boolean,
    isBookmarksScreenOpen: Boolean,
    isCurrentWebPageBookmarked: Boolean,
    isTabSwitcherOpen: Boolean,
    isSearchScreenOpen: Boolean,
    openTabCount: Int,
    desktopViewportEnabled: Boolean,
    onHomeClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTabsClick: () -> Unit,
    onOpenAutomation: (ExtensionModule) -> Unit,
    onNewTabClick: () -> Unit,
    onOpenBookmarksScreenExplicit: () -> Unit,
    onOpenShieldSheet: () -> Unit,
    onToggleDesktopMode: (Boolean) -> Unit,
    onCycleSeedPalette: () -> Unit,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_navigation_bar")
    ) {
        // 1. Home
        val homeSelected = isHomeActive && !isBookmarksScreenOpen && !isTabSwitcherOpen && !isSearchScreenOpen
        NavigationBarItem(
            selected = homeSelected,
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = if (homeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.nav_home)
                )
            },
            label = { Text(stringResource(R.string.nav_home), maxLines = 1) },
            modifier = Modifier.testTag("nav_home_button")
        )

        // 2. Bookmarks
        NavigationBarItem(
            selected = isBookmarksScreenOpen || (!isHomeActive && isCurrentWebPageBookmarked && !isSearchScreenOpen),
            onClick = onBookmarksClick,
            icon = {
                val bookmarkIcon = when {
                    isBookmarksScreenOpen -> Icons.Filled.Bookmarks
                    isHomeActive -> Icons.Outlined.Bookmarks
                    isCurrentWebPageBookmarked -> Icons.Filled.Bookmark
                    else -> Icons.Outlined.BookmarkAdd
                }
                Icon(
                    imageVector = bookmarkIcon,
                    contentDescription = stringResource(R.string.nav_bookmarks)
                )
            },
            label = { Text(stringResource(R.string.nav_bookmarks), maxLines = 1) },
            modifier = Modifier.testTag("nav_bookmarks_button")
        )

        // 3. Search (Dedicated Search Screen with live search-engine suggestions)
        NavigationBarItem(
            selected = isSearchScreenOpen,
            onClick = onSearchClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.nav_search)
                )
            },
            label = { Text(stringResource(R.string.nav_search), maxLines = 1) },
            modifier = Modifier.testTag("nav_search_button")
        )

        // 4. Tabs
        NavigationBarItem(
            selected = isTabSwitcherOpen,
            onClick = onTabsClick,
            icon = {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = openTabCount.toString(),
                                style = EmphasizedTypography.badgeCounter
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Layers,
                        contentDescription = stringResource(R.string.nav_tabs)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_tabs), maxLines = 1) },
            modifier = Modifier.testTag("nav_tabs_button")
        )

        // Material 3 Expressive Split Button for AI Extensions + Main Browser Menu
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 8.dp)
                .minimumInteractiveComponentSize()
        ) {
            val chevronRotation by animateFloatAsState(
                targetValue = if (menuExpanded) 180f else 0f,
                animationSpec = ApexMotion.spatialSpec(),
                label = "SplitButtonChevronRotation"
            )

            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = { onOpenAutomation(ExtensionModule.NANO) },
                        modifier = Modifier.testTag("split_menu_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = stringResource(R.string.automation_panel_title),
                            modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize)
                        )
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(
                        checked = menuExpanded,
                        onCheckedChange = { menuExpanded = it },
                        modifier = Modifier.testTag("split_menu_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.cd_main_menu_more),
                            modifier = Modifier
                                .size(SplitButtonDefaults.TrailingIconSize)
                                .rotate(chevronRotation)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ext_nano_title)) },
                            leadingIcon = {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenAutomation(ExtensionModule.NANO)
                            },
                            modifier = Modifier.testTag("menu_open_nano")
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ext_retriever_title)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.TableChart, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenAutomation(ExtensionModule.RETRIEVER)
                            },
                            modifier = Modifier.testTag("menu_open_retriever")
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_new_tab)) },
                            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onNewTabClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_open_bookmarks)) },
                            leadingIcon = { Icon(Icons.Filled.BookmarkAdd, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenBookmarksScreenExplicit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_privacy_sheet)) },
                            leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenShieldSheet()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (desktopViewportEnabled) "Desktop Viewport (On)"
                                    else stringResource(R.string.menu_desktop_mode)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.DesktopWindows, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleDesktopMode(!desktopViewportEnabled)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_cycle_seed)) },
                            leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onCycleSeedPalette()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_clear_data)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.CleaningServices, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onResetStats()
                            }
                        )
                    }
                }
            )
        }
    }
}
