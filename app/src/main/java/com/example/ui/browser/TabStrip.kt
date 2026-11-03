package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.TabGridEntry

/**
 * Horizontal Tab Strip for tablets & landscape widths using expressive corner morphing
 * and distinct tonal surfaces for active vs inactive tabs.
 */
@Composable
fun ExpressiveHorizontalTabStrip(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("horizontal_tab_strip")
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tabs, key = { it.id }) { tab ->
                val isSelected = tab.id == activeTabId
                val morphShape = rememberMorphingCornerShape(
                    targetRadius = if (isSelected) 24.dp else 12.dp,
                    label = "TabStripMorph_${tab.id}"
                )
                val tabSurfaceColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    animationSpec = ApexMotion.effectsSpec(),
                    label = "TabStripColor_${tab.id}"
                )
                val tabContentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = ApexMotion.effectsSpec(),
                    label = "TabStripContentColor_${tab.id}"
                )

                Surface(
                    shape = morphShape,
                    color = tabSurfaceColor,
                    tonalElevation = if (isSelected) 4.dp else 1.dp,
                    border = if (isSelected) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .height(42.dp)
                        .clickable { onSelectTab(tab.id) }
                        .testTag("tab_chip_${tab.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 14.dp, end = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (tab.isHomePage) Icons.Outlined.Home else Icons.Filled.Public,
                            contentDescription = null,
                            tint = tabContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.title,
                            style = if (isSelected) {
                                EmphasizedTypography.activeTabTitle
                            } else {
                                MaterialTheme.typography.labelLarge
                            },
                            color = tabContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onCloseTab(tab.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_close_tab),
                                tint = tabContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = rememberMorphingCornerShape(targetRadius = 20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onNewTab)
                        .testTag("tab_strip_new_tab_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.fab_new_tab),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draggable Grid Tab Switcher with M3 Expressive group-merge transitions.
 * - Dragging any tab (via its drag handle or long-press) onto another tab or group merges them!
 * - Inside each Group Card's 2x2 preview:
 *   • If the group has <= 4 tabs (2, 3, or 4 tabs), up to all 4 tabs stay shown directly!
 *   • If the group has > 4 tabs, 3 tabs are shown and the 4th slot displays the remaining `+N` count!
 */
@Composable
fun DraggableTabGridOverlay(
    gridEntries: List<TabGridEntry>,
    totalOpenTabs: Int,
    activeTabId: String,
    expandedGroupId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseGroup: (String) -> Unit,
    onMergeEntries: (String, String) -> Unit,
    onOpenGroup: (String?) -> Unit,
    onAddTabToGroup: (String) -> Unit,
    onUngroupTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onResetAllTabs: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (expandedGroupId != null) {
            onOpenGroup(null)
        } else {
            onDismiss()
        }
    }

    val itemBounds = remember { mutableStateMapOf<String, Rect>() }
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dragTranslation by remember { mutableStateOf(Offset.Zero) }
    var hoveredTargetKey by remember { mutableStateOf<String?>(null) }

    fun updateHoverTarget(key: String, deltaTotal: Offset) {
        val originRect = itemBounds[key] ?: return
        val currentCenter = originRect.center + deltaTotal
        val hitKey = itemBounds.entries.firstOrNull { (otherKey, rect) ->
            otherKey != key && rect.contains(currentCenter)
        }?.key
        hoveredTargetKey = hitKey
    }

    fun finishDrag() {
        val from = draggedKey
        val to = hoveredTargetKey
        if (from != null && to != null && from != to) {
            onMergeEntries(from, to)
        }
        draggedKey = null
        dragTranslation = Offset.Zero
        hoveredTargetKey = null
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
            .testTag("draggable_tab_grid_switcher")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.tabs_sheet_title),
                            style = EmphasizedTypography.headlineHero,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalOpenTabs open • ${stringResource(R.string.tabs_drag_subtitle)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onResetAllTabs,
                            modifier = Modifier.testTag("reset_tabs_button")
                        ) {
                            Text(stringResource(R.string.tabs_close_all))
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("close_tab_switcher_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_close_tab)
                            )
                        }
                    }
                }

                // 2-Column (Adaptive) Draggable Grid of Tabs & Tab Groups
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 112.dp, top = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("tabs_lazy_grid")
                ) {
                    items(
                        items = gridEntries,
                        key = { it.gridKey }
                    ) { entry ->
                        val isBeingDragged = draggedKey == entry.gridKey
                        val isDropTarget = hoveredTargetKey == entry.gridKey

                        val cardScale by animateFloatAsState(
                            targetValue = when {
                                isBeingDragged -> 1.06f
                                isDropTarget -> 1.04f
                                else -> 1f
                            },
                            animationSpec = ApexMotion.spatialSpec(),
                            label = "GridCardScale_${entry.gridKey}"
                        )

                        val animatedModifier = if (isBeingDragged) {
                            Modifier.zIndex(25f)
                        } else {
                            Modifier
                                .zIndex(if (isDropTarget) 5f else 0f)
                                .animateItem(placementSpec = ApexMotion.spatialSpring())
                        }

                        Box(
                            modifier = animatedModifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    if (!isBeingDragged) {
                                        itemBounds[entry.gridKey] = coords.boundsInRoot()
                                    }
                                }
                                .graphicsLayer {
                                    scaleX = cardScale
                                    scaleY = cardScale
                                    if (isBeingDragged) {
                                        translationX = dragTranslation.x
                                        translationY = dragTranslation.y
                                        alpha = 0.94f
                                    }
                                }
                                .pointerInput(entry.gridKey) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedKey = entry.gridKey
                                            dragTranslation = Offset.Zero
                                            hoveredTargetKey = null
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragTranslation += dragAmount
                                            updateHoverTarget(entry.gridKey, dragTranslation)
                                        },
                                        onDragEnd = { finishDrag() },
                                        onDragCancel = { finishDrag() }
                                    )
                                }
                        ) {
                            when (entry) {
                                is TabGridEntry.SingleTab -> SingleTabGridCard(
                                    tab = entry.tab,
                                    isSelected = entry.tab.id == activeTabId,
                                    isDropTarget = isDropTarget,
                                    onClick = { onSelectTab(entry.tab.id) },
                                    onClose = { onCloseTab(entry.tab.id) },
                                    onImmediateDragStart = {
                                        draggedKey = entry.gridKey
                                        dragTranslation = Offset.Zero
                                        hoveredTargetKey = null
                                    },
                                    onImmediateDrag = { delta ->
                                        dragTranslation += delta
                                        updateHoverTarget(entry.gridKey, dragTranslation)
                                    },
                                    onImmediateDragEnd = { finishDrag() }
                                )

                                is TabGridEntry.GroupTab -> GroupTabGridCard(
                                    entry = entry,
                                    activeTabId = activeTabId,
                                    isDropTarget = isDropTarget,
                                    onOpenGroup = { onOpenGroup(entry.group.id) },
                                    onCloseGroup = { onCloseGroup(entry.group.id) },
                                    onImmediateDragStart = {
                                        draggedKey = entry.gridKey
                                        dragTranslation = Offset.Zero
                                        hoveredTargetKey = null
                                    },
                                    onImmediateDrag = { delta ->
                                        dragTranslation += delta
                                        updateHoverTarget(entry.gridKey, dragTranslation)
                                    },
                                    onImmediateDragEnd = { finishDrag() }
                                )
                            }
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = onNewTab,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.fab_new_tab)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.fab_new_tab),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("switcher_new_tab_fab")
            )

            // Expanded Tab Group Detail Modal
            val expandedGroupEntry = gridEntries
                .filterIsInstance<TabGridEntry.GroupTab>()
                .firstOrNull { it.group.id == expandedGroupId }

            AnimatedVisibility(
                visible = expandedGroupEntry != null,
                enter = fadeIn(ApexMotion.effectsSpring()) +
                    scaleIn(initialScale = 0.90f, animationSpec = ApexMotion.spatialSpring()),
                exit = fadeOut(ApexMotion.effectsSpring()) +
                    scaleOut(targetScale = 0.90f, animationSpec = ApexMotion.spatialSpring()),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(40f)
            ) {
                if (expandedGroupEntry != null) {
                    ExpandedGroupModal(
                        groupEntry = expandedGroupEntry,
                        activeTabId = activeTabId,
                        onSelectTab = onSelectTab,
                        onAddTabToGroup = { onAddTabToGroup(expandedGroupEntry.group.id) },
                        onUngroupTab = onUngroupTab,
                        onCloseTab = onCloseTab,
                        onDismiss = { onOpenGroup(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleTabGridCard(
    tab: BrowserTab,
    isSelected: Boolean,
    isDropTarget: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onImmediateDragStart: () -> Unit,
    onImmediateDrag: (Offset) -> Unit,
    onImmediateDragEnd: () -> Unit
) {
    val cardShape = rememberMorphingCornerShape(
        targetRadius = when {
            isDropTarget -> 30.dp
            isSelected -> 26.dp
            else -> 18.dp
        },
        label = "SingleGridCardShape_${tab.id}"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> MaterialTheme.colorScheme.tertiaryContainer
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "SingleGridCardBg_${tab.id}"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> MaterialTheme.colorScheme.onTertiaryContainer
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "SingleGridCardFg_${tab.id}"
    )

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(
            width = if (isDropTarget || isSelected) 2.dp else 1.dp,
            color = when {
                isDropTarget -> MaterialTheme.colorScheme.tertiary
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(198.dp)
            .clickable(onClick = onClick)
            .testTag("grid_tab_card_${tab.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Card Top Bar: Drag Handle + Title + Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .pointerInput(tab.id) {
                            detectDragGestures(
                                onDragStart = { onImmediateDragStart() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onImmediateDrag(dragAmount)
                                },
                                onDragEnd = { onImmediateDragEnd() },
                                onDragCancel = { onImmediateDragEnd() }
                            )
                        }
                        .testTag("drag_handle_tab_${tab.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragIndicator,
                        contentDescription = stringResource(R.string.cd_drag_tab),
                        tint = contentColor.copy(alpha = 0.72f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = tab.title,
                    style = EmphasizedTypography.activeTabTitle,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("close_tab_button_${tab.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_tab),
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Center Preview Surface
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    if (isDropTarget) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = stringResource(R.string.tab_group_drop_hint),
                                style = EmphasizedTypography.badgeCounter,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (tab.isHomePage) Icons.Outlined.Home else Icons.Filled.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (tab.isHomePage) "Privacy Start Hub" else tab.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Footer Status Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSelected) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = EmphasizedTypography.badgeCounter,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Hold/drag to group",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }

                if (tab.blockedCountOnPage > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${tab.blockedCountOnPage}",
                            style = EmphasizedTypography.badgeCounter,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Group Tab Card displaying a 2x2 mini-tab grid with the exact requested rule:
 * - If `tabs.size > 4`: 3 tabs are shown in slots 1–3, and the 4th slot displays `+N` remaining count!
 * - Else (`tabs.size <= 4`): up to all 4 tabs stay shown directly in slots 1–4!
 */
@Composable
private fun GroupTabGridCard(
    entry: TabGridEntry.GroupTab,
    activeTabId: String,
    isDropTarget: Boolean,
    onOpenGroup: () -> Unit,
    onCloseGroup: () -> Unit,
    onImmediateDragStart: () -> Unit,
    onImmediateDrag: (Offset) -> Unit,
    onImmediateDragEnd: () -> Unit
) {
    val containsActive = entry.tabs.any { it.id == activeTabId }
    val totalTabs = entry.tabs.size
    val hasMoreThanFour = totalTabs > 4
    val overflowRemainingCount = totalTabs - 3 // Shown in 4th slot when > 4 tabs

    val cardShape = rememberMorphingCornerShape(
        targetRadius = if (isDropTarget) 32.dp else 26.dp,
        label = "GroupGridCardShape_${entry.group.id}"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> MaterialTheme.colorScheme.tertiaryContainer
            containsActive -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "GroupGridCardBg_${entry.group.id}"
    )

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isDropTarget || containsActive) 2.dp else 1.5.dp,
            color = when {
                isDropTarget -> MaterialTheme.colorScheme.tertiary
                containsActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(198.dp)
            .clickable(onClick = onOpenGroup)
            .testTag("grid_group_card_${entry.group.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Group Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .pointerInput(entry.group.id) {
                            detectDragGestures(
                                onDragStart = { onImmediateDragStart() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onImmediateDrag(dragAmount)
                                },
                                onDragEnd = { onImmediateDragEnd() },
                                onDragCancel = { onImmediateDragEnd() }
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragIndicator,
                        contentDescription = stringResource(R.string.cd_drag_tab),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.FolderCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${entry.group.title} ($totalTabs)",
                    style = EmphasizedTypography.activeTabTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onCloseGroup,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_tab),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2x2 Mini-Tab Preview Grid:
            // - Slots 0, 1, 2: first 3 tabs
            // - Slot 3 (4th slot): if totalTabs > 4 -> "+{totalTabs - 3}" count badge; else -> 4th tab stays!
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GroupMiniSlotTile(
                        tab = entry.tabs.getOrNull(0),
                        overflowCount = null,
                        isActiveTab = entry.tabs.getOrNull(0)?.id == activeTabId,
                        modifier = Modifier.weight(1f)
                    )
                    GroupMiniSlotTile(
                        tab = entry.tabs.getOrNull(1),
                        overflowCount = null,
                        isActiveTab = entry.tabs.getOrNull(1)?.id == activeTabId,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GroupMiniSlotTile(
                        tab = entry.tabs.getOrNull(2),
                        overflowCount = null,
                        isActiveTab = entry.tabs.getOrNull(2)?.id == activeTabId,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasMoreThanFour) {
                        GroupMiniSlotTile(
                            tab = null,
                            overflowCount = overflowRemainingCount,
                            isActiveTab = false,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("group_overflow_count_slot_${entry.group.id}")
                        )
                    } else {
                        GroupMiniSlotTile(
                            tab = entry.tabs.getOrNull(3),
                            overflowCount = null,
                            isActiveTab = entry.tabs.getOrNull(3)?.id == activeTabId,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("group_fourth_tab_slot_${entry.group.id}")
                        )
                    }
                }
            }

            if (isDropTarget) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.tab_group_drop_hint),
                    style = EmphasizedTypography.badgeCounter,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun GroupMiniSlotTile(
    tab: BrowserTab?,
    overflowCount: Int?,
    isActiveTab: Boolean,
    modifier: Modifier = Modifier
) {
    when {
        overflowCount != null -> {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+$overflowCount",
                        style = EmphasizedTypography.activeTabTitle,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        tab != null -> {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isActiveTab) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                border = if (isActiveTab) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                },
                modifier = modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (tab.isHomePage) Icons.Outlined.Home else Icons.Filled.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        else -> {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.45f),
                modifier = modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ExpandedGroupModal(
    groupEntry: TabGridEntry.GroupTab,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onAddTabToGroup: () -> Unit,
    onUngroupTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.50f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume */ }
                .testTag("expanded_group_modal")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = groupEntry.group.title,
                            style = EmphasizedTypography.headlineHero,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${groupEntry.tabs.size} tabs in group",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onAddTabToGroup,
                            modifier = Modifier.testTag("group_add_tab_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tab")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close_tab))
                        }
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(groupEntry.tabs, key = { it.id }) { tab ->
                        val isSelected = tab.id == activeTabId
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTab(tab.id) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (tab.isHomePage) Icons.Outlined.Home else Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tab.title,
                                        style = EmphasizedTypography.activeTabTitle,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tab.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { onUngroupTab(tab.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CallSplit,
                                        contentDescription = stringResource(R.string.tab_group_ungroup),
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCloseTab(tab.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.cd_close_tab),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
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
