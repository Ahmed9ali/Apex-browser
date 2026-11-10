package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Https
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.WebAssetOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.BrowserPreferences
import com.example.data.preferences.SearchEngineOption
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.SeedPalettePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenOverlay(
    preferences: BrowserPreferences,
    onSelectSearchEngine: (SearchEngineOption) -> Unit,
    onToggleShields: (Boolean) -> Unit,
    onToggleHttpsUpgrade: (Boolean) -> Unit,
    onToggleScriptGuard: (Boolean) -> Unit,
    onToggleSmartPreload: (Boolean) -> Unit,
    onToggleDesktopViewport: (Boolean) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onSelectSeedPalette: (SeedPalettePreset) -> Unit,
    onClearBrowsingData: () -> Unit,
    onToggleAutoSaveOfflinePages: (Boolean) -> Unit = {},
    onClearOfflinePages: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val engines = SearchEngineOption.entries

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = EmphasizedTypography.headlineHero
                    )
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag("settings_screen")
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .testTag("settings_scroll_list")
            ) {
                // 1. Default Search Engine — Inside Cards
                item(key = "section_search_engine") {
                    MaterialGroupSectionHeader(
                        icon = Icons.Outlined.TravelExplore,
                        title = stringResource(R.string.settings_search_engine_section),
                        subtitle = stringResource(R.string.settings_search_engine_subtitle)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_search_engine_card")
                    ) {
                        engines.forEach { engine ->
                            val selected = engine == preferences.searchEngine
                            OutlinedCard(
                                onClick = { onSelectSearchEngine(engine) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_engine_option_${engine.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                        },
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            SearchEngineLogo(
                                                engine = engine,
                                                size = 26.dp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = engine.displayName,
                                                style = EmphasizedTypography.activeTabTitle,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ) {
                                                Text(
                                                    text = engine.shortBadge,
                                                    style = EmphasizedTypography.badgeCounter,
                                                    color = if (selected) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = engine.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    RadioButton(
                                        selected = selected,
                                        onClick = null
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Privacy & Security — Material 3 Grouped List
                item(key = "section_privacy") {
                    MaterialGroupSectionHeader(
                        icon = Icons.Outlined.Security,
                        title = stringResource(R.string.settings_privacy_section)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(GroupedListSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormattedSettingsSwitchItem(
                            index = 0,
                            totalCount = 3,
                            icon = Icons.Outlined.Security,
                            title = stringResource(R.string.settings_shields_title),
                            description = stringResource(R.string.settings_shields_desc),
                            checked = preferences.shieldsEnabled,
                            onCheckedChange = onToggleShields,
                            tag = "settings_switch_shields"
                        )
                        FormattedSettingsSwitchItem(
                            index = 1,
                            totalCount = 3,
                            icon = Icons.Outlined.Https,
                            title = stringResource(R.string.settings_https_title),
                            description = stringResource(R.string.settings_https_desc),
                            checked = preferences.httpsUpgradeEnabled,
                            onCheckedChange = onToggleHttpsUpgrade,
                            tag = "settings_switch_https"
                        )
                        FormattedSettingsSwitchItem(
                            index = 2,
                            totalCount = 3,
                            icon = Icons.Outlined.WebAssetOff,
                            title = stringResource(R.string.settings_script_guard_title),
                            description = stringResource(R.string.settings_script_guard_desc),
                            checked = preferences.scriptGuardEnabled,
                            onCheckedChange = onToggleScriptGuard,
                            tag = "settings_switch_script_guard"
                        )
                    }
                }

                // 3. Browsing & Performance — Material 3 Grouped List
                item(key = "section_browsing") {
                    MaterialGroupSectionHeader(
                        icon = Icons.Outlined.Speed,
                        title = stringResource(R.string.settings_browsing_section)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(GroupedListSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormattedSettingsSwitchItem(
                            index = 0,
                            totalCount = 3,
                            icon = Icons.Outlined.Speed,
                            title = stringResource(R.string.settings_preload_title),
                            description = stringResource(R.string.settings_preload_desc),
                            checked = preferences.aggressivePreloadEnabled,
                            onCheckedChange = onToggleSmartPreload,
                            tag = "settings_switch_preload"
                        )
                        FormattedSettingsSwitchItem(
                            index = 1,
                            totalCount = 3,
                            icon = Icons.Outlined.DesktopWindows,
                            title = stringResource(R.string.settings_desktop_title),
                            description = stringResource(R.string.settings_desktop_desc),
                            checked = preferences.desktopViewportEnabled,
                            onCheckedChange = onToggleDesktopViewport,
                            tag = "settings_switch_desktop"
                        )
                        FormattedSettingsSwitchItem(
                            index = 2,
                            totalCount = 3,
                            icon = Icons.Outlined.CloudDownload,
                            title = stringResource(R.string.settings_offline_cache_title),
                            description = stringResource(R.string.settings_offline_cache_desc),
                            checked = preferences.autoSaveOfflinePagesEnabled,
                            onCheckedChange = onToggleAutoSaveOfflinePages,
                            tag = "settings_switch_offline_cache"
                        )
                    }
                }

                // 4. Appearance & Material 3 Theme — Material 3 Grouped List
                item(key = "section_appearance") {
                    MaterialGroupSectionHeader(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.settings_appearance_section)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(GroupedListSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormattedSettingsSwitchItem(
                            index = 0,
                            totalCount = 2,
                            icon = Icons.Outlined.Palette,
                            title = stringResource(R.string.settings_dynamic_color_title),
                            description = stringResource(R.string.settings_dynamic_color_desc),
                            checked = preferences.dynamicColorEnabled,
                            onCheckedChange = onToggleDynamicColor,
                            tag = "settings_switch_dynamic_color"
                        )

                        val paletteItemShape = groupedListItemShape(index = 1, totalCount = 2)
                        Surface(
                            shape = paletteItemShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(paletteItemShape)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    SettingsIconBadge(icon = Icons.Outlined.ColorLens)
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_seed_palette_label),
                                            style = EmphasizedTypography.activeTabTitle,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (preferences.dynamicColorEnabled) {
                                                "Tap any palette below to switch from wallpaper colors"
                                            } else {
                                                "Active palette: ${preferences.seedPreset.label}"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    SeedPalettePreset.entries.forEach { preset ->
                                        val isSelected = !preferences.dynamicColorEnabled &&
                                            preferences.seedPreset == preset
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onSelectSeedPalette(preset) },
                                            label = { Text(preset.label) },
                                            leadingIcon = {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(preset.seedColor)
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.surface,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.testTag("settings_palette_${preset.name}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Browsing Data Management — Single-Item Material 3 Grouped List
                item(key = "section_browsing_data") {
                    MaterialGroupSectionHeader(
                        icon = Icons.Outlined.CleaningServices,
                        title = stringResource(R.string.settings_data_section)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(GroupedListSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val shape0 = groupedListItemShape(index = 0, totalCount = 2)
                        Surface(
                            shape = shape0,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape0)
                                .clickable(
                                    role = Role.Button,
                                    onClick = onClearBrowsingData
                                )
                                .testTag("settings_clear_data_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 68.dp)
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.CleaningServices,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_clear_data_button),
                                        style = EmphasizedTypography.activeTabTitle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Removes saved search queries and resets local counters",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val shape1 = groupedListItemShape(index = 1, totalCount = 2)
                        Surface(
                            shape = shape1,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape1)
                                .clickable(
                                    role = Role.Button,
                                    onClick = onClearOfflinePages
                                )
                                .testTag("settings_clear_offline_pages_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 68.dp)
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.CloudOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.clear_offline_pages),
                                        style = EmphasizedTypography.activeTabTitle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Removes all saved copies of visited webpages from local storage",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    highlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FormattedSettingsSwitchItem(
    index: Int,
    totalCount: Int,
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    MaterialGroupListItem(
        index = index,
        totalCount = totalCount,
        title = title,
        subtitle = description,
        role = Role.Switch,
        onClick = { onCheckedChange(!checked) },
        leadingContent = {
            SettingsIconBadge(icon = icon, highlighted = checked)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null
            )
        },
        modifier = Modifier.testTag(tag)
    )
}
