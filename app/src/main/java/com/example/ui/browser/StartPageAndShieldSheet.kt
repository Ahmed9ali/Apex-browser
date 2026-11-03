package com.example.ui.browser

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BlockedDomainLogEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.PrivacyStatSummaryEntity
import com.example.data.preferences.BrowserPreferences
import com.example.engine.PreloadCandidate
import com.example.engine.PrivacyManager
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.ExtensionModule

@Composable
fun StartPagePrivacyHub(
    privacyStats: PrivacyStatSummaryEntity,
    preferences: BrowserPreferences,
    bookmarks: List<BookmarkEntity>,
    onOpenSearchScreen: () -> Unit,
    onToggleShields: (Boolean) -> Unit,
    onToggleHttps: (Boolean) -> Unit,
    onTogglePreload: (Boolean) -> Unit,
    onToggleScriptGuard: (Boolean) -> Unit,
    onSelectDestination: (String) -> Unit,
    onOpenExtension: (ExtensionModule) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBlocked = privacyStats.trackersBlocked + privacyStats.adsBlocked
    val animatedBlocked by animateIntAsState(
        targetValue = totalBlocked,
        animationSpec = ApexMotion.spatialSpec(),
        label = "AnimatedBlockedStat"
    )
    val animatedHttps by animateIntAsState(
        targetValue = privacyStats.httpsUpgrades,
        animationSpec = ApexMotion.spatialSpec(),
        label = "AnimatedHttpsStat"
    )

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("start_page_hub")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
        ) {
            // Hero Banner Card
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_apex_hero_1793695901815),
                            contentDescription = stringResource(R.string.privacy_dashboard_title),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.90f)
                                        )
                                    )
                                )
                        )
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (preferences.shieldsEnabled) {
                                            "Apex Shields Active • ${preferences.searchEngine.displayName}"
                                        } else {
                                            "Shields Paused"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Apex Browser",
                                        style = EmphasizedTypography.headlineHero,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Fast, private browsing with zero preloaded clutter",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { onOpenExtension(ExtensionModule.NANO) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("hero_nano_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Nano")
                                    }
                                    FilledTonalButton(
                                        onClick = { onOpenExtension(ExtensionModule.RETRIEVER) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("hero_retriever_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.TableChart,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Retriever")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Prominent Home Search Pill launching Dedicated Search Screen
            item {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(onClick = onOpenSearchScreen)
                        .testTag("home_search_launch_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(
                                R.string.search_screen_hint,
                                preferences.searchEngine.displayName
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = preferences.searchEngine.shortBadge,
                                style = EmphasizedTypography.badgeCounter,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Quick Toggles FilterChip Row
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("privacy_quick_toggles")
                ) {
                    ExpressiveToggleChip(
                        selected = preferences.shieldsEnabled,
                        label = stringResource(R.string.chip_shields_up),
                        onClick = { onToggleShields(!preferences.shieldsEnabled) },
                        tag = "chip_toggle_shields"
                    )
                    ExpressiveToggleChip(
                        selected = preferences.httpsUpgradeEnabled,
                        label = stringResource(R.string.chip_https_only),
                        onClick = { onToggleHttps(!preferences.httpsUpgradeEnabled) },
                        tag = "chip_toggle_https"
                    )
                    ExpressiveToggleChip(
                        selected = preferences.aggressivePreloadEnabled,
                        label = stringResource(R.string.chip_smart_preload),
                        onClick = { onTogglePreload(!preferences.aggressivePreloadEnabled) },
                        tag = "chip_toggle_preload"
                    )
                    ExpressiveToggleChip(
                        selected = preferences.scriptGuardEnabled,
                        label = stringResource(R.string.chip_script_guard),
                        onClick = { onToggleScriptGuard(!preferences.scriptGuardEnabled) },
                        tag = "chip_toggle_scripts"
                    )
                }
            }

            // Privacy Dashboard Card (starts at 0 on fresh install)
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("privacy_dashboard_card")
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.privacy_dashboard_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.privacy_dashboard_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "${privacyStats.trackersBlocked}T / ${privacyStats.adsBlocked}A",
                                    style = EmphasizedTypography.badgeCounter,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TelemetryStatTile(
                                value = animatedBlocked.toString(),
                                label = stringResource(R.string.stat_trackers_blocked),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryStatTile(
                                value = animatedHttps.toString(),
                                label = stringResource(R.string.stat_https_upgrades),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TelemetryStatTile(
                                value = PrivacyManager.formatBytesSaved(privacyStats.bytesSaved),
                                label = stringResource(R.string.stat_data_saved),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryStatTile(
                                value = PrivacyManager.formatTimeSaved(privacyStats.timeSavedMs),
                                label = stringResource(R.string.stat_time_saved),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Genuine User Bookmarks Section (clean empty hint on fresh install)
            item {
                Text(
                    text = stringResource(R.string.home_bookmarks_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (bookmarks.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.home_fresh_bookmarks_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(bookmarks.take(6), key = { it.id }) { bm ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onSelectDestination(bm.url) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bm.title,
                                    style = EmphasizedTypography.activeTabTitle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = bm.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveToggleChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag(tag)
    )
}

@Composable
private fun TelemetryStatTile(
    value: String,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = value,
                style = EmphasizedTypography.displayStat,
                color = contentColor,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldAndPreloadModalSheet(
    activeTab: BrowserTab,
    privacyStats: PrivacyStatSummaryEntity,
    preferences: BrowserPreferences,
    preloadCandidates: List<PreloadCandidate>,
    blockedLogs: List<BlockedDomainLogEntity>,
    onToggleShields: (Boolean) -> Unit,
    onToggleHttps: (Boolean) -> Unit,
    onTogglePreload: (Boolean) -> Unit,
    onToggleScriptGuard: (Boolean) -> Unit,
    onNavigateCandidate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.testTag("shield_inspector_sheet")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apex Shield & Speed Engine",
                            style = EmphasizedTypography.headlineHero,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${activeTab.title} • ${activeTab.blockedCountOnPage} items blocked on page",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (activeTab.isHttps) "HTTPS" else "HTTP",
                                style = EmphasizedTypography.badgeCounter,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    ExpressiveToggleChip(
                        selected = preferences.shieldsEnabled,
                        label = "Ad/Tracker Shield",
                        onClick = { onToggleShields(!preferences.shieldsEnabled) },
                        tag = "sheet_toggle_shields"
                    )
                    ExpressiveToggleChip(
                        selected = preferences.httpsUpgradeEnabled,
                        label = "HTTPS Upgrade",
                        onClick = { onToggleHttps(!preferences.httpsUpgradeEnabled) },
                        tag = "sheet_toggle_https"
                    )
                    ExpressiveToggleChip(
                        selected = preferences.aggressivePreloadEnabled,
                        label = "Preload Optimizer",
                        onClick = { onTogglePreload(!preferences.aggressivePreloadEnabled) },
                        tag = "sheet_toggle_preload"
                    )
                    ExpressiveToggleChip(
                        selected = preferences.scriptGuardEnabled,
                        label = "Script Blocker",
                        onClick = { onToggleScriptGuard(!preferences.scriptGuardEnabled) },
                        tag = "sheet_toggle_scripts"
                    )
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "${privacyStats.trackersBlocked + privacyStats.adsBlocked}",
                                style = EmphasizedTypography.headlineHero,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total Blocked",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            Text(
                                text = PrivacyManager.formatBytesSaved(privacyStats.bytesSaved),
                                style = EmphasizedTypography.headlineHero,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Bandwidth Saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            Text(
                                text = PrivacyManager.formatTimeSaved(privacyStats.timeSavedMs),
                                style = EmphasizedTypography.headlineHero,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Load Time Saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (preloadCandidates.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "PerformanceOptimizer • Preloaded Next Links",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(preloadCandidates.take(3)) { candidate ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onNavigateCandidate(candidate.url)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = candidate.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = candidate.status,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${candidate.confidencePercent}% likely",
                                style = EmphasizedTypography.badgeCounter,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (blockedLogs.isNotEmpty()) {
                item {
                    HorizontalDivider()
                    Text(
                        text = "Recent Intercepted Domains",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(blockedLogs.take(8), key = { it.id }) { log ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.blockedDomain,
                                style = EmphasizedTypography.monoCode,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "on ${log.pageHost} • ${log.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "-${PrivacyManager.formatBytesSaved(log.savedBytes)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
