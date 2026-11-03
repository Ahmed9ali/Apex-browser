package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SearchHistoryEntity
import com.example.data.preferences.SearchEngineOption
import com.example.engine.SearchSuggestionItem
import com.example.engine.SuggestionKind
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.rememberMorphingCornerShape

@Composable
fun SearchScreenOverlay(
    query: String,
    selectedEngine: SearchEngineOption,
    suggestions: List<SearchSuggestionItem>,
    isLoadingSuggestions: Boolean,
    recentHistory: List<SearchHistoryEntity>,
    onQueryChange: (String) -> Unit,
    onSelectEngine: (SearchEngineOption) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onFillQuery: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("dedicated_search_screen")
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 780.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Top Expressive Search Input Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("search_screen_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_close_search)
                                )
                            }

                            Surface(
                                shape = rememberMorphingCornerShape(
                                    targetRadius = if (query.isEmpty()) 26.dp else 16.dp,
                                    label = "SearchBoxMorph"
                                ),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp)
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = selectedEngine.shortBadge,
                                            style = EmphasizedTypography.badgeCounter,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (query.isEmpty()) {
                                            Text(
                                                text = stringResource(
                                                    R.string.search_screen_hint,
                                                    selectedEngine.displayName
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        BasicTextField(
                                            value = query,
                                            onValueChange = onQueryChange,
                                            singleLine = true,
                                            textStyle = EmphasizedTypography.addressBarActive.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Uri,
                                                imeAction = ImeAction.Search
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onSearch = {
                                                    if (query.isNotBlank()) {
                                                        focusManager.clearFocus()
                                                        onSubmitSearch(query)
                                                    }
                                                }
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester)
                                                .testTag("search_screen_input")
                                        )
                                    }

                                    if (query.isNotEmpty()) {
                                        IconButton(
                                            onClick = { onQueryChange("") },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("search_screen_clear_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = stringResource(R.string.cd_clear_input),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Search Engine Selector Chips
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                                .testTag("search_engine_selector_row")
                        ) {
                            SearchEngineOption.entries.forEach { engine ->
                                val selected = engine == selectedEngine
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectEngine(engine) },
                                    label = { Text(engine.displayName) },
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
                                    modifier = Modifier.testTag("engine_chip_${engine.id}")
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isLoadingSuggestions,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            LinearProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                            )
                        }
                    }
                }

                // Body: Live Engine Suggestions (when typing) OR Recent History / Clean Fresh State
                if (query.isNotBlank()) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("search_suggestions_list")
                    ) {
                        item {
                            Text(
                                text = stringResource(
                                    R.string.search_suggestions_header,
                                    selectedEngine.displayName
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }

                        items(suggestions, key = { it.id }) { suggestion ->
                            SuggestionRowCard(
                                item = suggestion,
                                typedPrefix = query.trim(),
                                onSelect = {
                                    focusManager.clearFocus()
                                    onSubmitSearch(suggestion.submitValue)
                                },
                                onFill = { onFillQuery(suggestion.fillText) }
                            )
                        }
                    }
                } else {
                    // Empty Query: Show User's Genuine Recent Searches (empty on fresh install!)
                    if (recentHistory.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp)
                        ) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp)
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.TravelExplore,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.search_fresh_empty_title),
                                        style = EmphasizedTypography.activeTabTitle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.search_fresh_empty_subtitle,
                                            selectedEngine.displayName
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("recent_searches_list")
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.search_recent_header),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    TextButton(
                                        onClick = onClearAllHistory,
                                        modifier = Modifier.testTag("clear_search_history_button")
                                    ) {
                                        Text(stringResource(R.string.search_clear_history))
                                    }
                                }
                                HorizontalDivider()
                            }

                            items(recentHistory, key = { it.id }) { historyItem ->
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(placementSpec = ApexMotion.spatialSpring())
                                        .clickable {
                                            focusManager.clearFocus()
                                            onSubmitSearch(historyItem.query)
                                        }
                                        .testTag("history_item_${historyItem.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = historyItem.query,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = historyItem.searchEngineName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { onFillQuery(historyItem.query) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.NorthWest,
                                                contentDescription = stringResource(R.string.cd_fill_suggestion),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteHistoryItem(historyItem.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = stringResource(R.string.cd_remove_history_item),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRowCard(
    item: SearchSuggestionItem,
    typedPrefix: String,
    onSelect: () -> Unit,
    onFill: () -> Unit
) {
    val isUrl = item.kind == SuggestionKind.DIRECT_URL

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isUrl) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("suggestion_row_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isUrl) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isUrl) Icons.Filled.Public else Icons.Filled.Search,
                        contentDescription = null,
                        tint = if (isUrl) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val annotatedTitle = buildAnnotatedString {
                    val lowerTitle = item.title.lowercase()
                    val lowerPrefix = typedPrefix.lowercase()
                    if (lowerPrefix.isNotEmpty() && lowerTitle.startsWith(lowerPrefix)) {
                        append(item.title.substring(0, typedPrefix.length))
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append(item.title.substring(typedPrefix.length))
                        }
                    } else {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(item.title)
                        }
                    }
                }

                Text(
                    text = annotatedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUrl) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUrl) {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onFill,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("fill_suggestion_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.NorthWest,
                    contentDescription = stringResource(R.string.cd_fill_suggestion),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
