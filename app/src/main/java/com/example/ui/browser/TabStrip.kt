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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.theme.resolveTonalColors
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.TabGridEntry
import kotlin.math.roundToInt

private enum class TabSwitcherFilter {
    ALL_TABS,
    GROUPS_ONLY
}

/**
 * Horizontal Tab Strip for tablets & landscape widths using expressive corner morphing.
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
 * Full-screen Tab & Group Switcher (no Bottom Navigation Bar):
 * - Material 3 CenterAlignedTopAppBar featuring a centered `Tabs | Groups` segmented pill.
 * - Centered Floating Action Pill (`+ Add Tab` | `Group`) anchored at BottomCenter.
 * - Draggable 2-column grid supporting drag-and-drop group creation and 2x2 group card previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableTabGridOverlay(
    gridEntries: List<TabGridEntry>,
    totalOpenTabs: Int,
    activeTabId: String,
    expandedGroupId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseGroup: (String) -> Unit,
    onEditGroup: (String) -> Unit,
    onMergeEntries: (String, String) -> Unit,
    onMoveGroupedTabToTarget: (String, String?) -> Unit,
    onOpenGroup: (String?) -> Unit,
    onAddTabToGroup: (String) -> Unit,
    onUngroupTab: (String) -> Unit,
    onCycleTabColor: (String) -> Unit,
    onNewTab: () -> Unit,
    onCreateGroup: () -> Unit,
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

    var switcherFilter by rememberSaveable { mutableStateOf(TabSwitcherFilter.ALL_TABS) }
    val groupEntries = remember(gridEntries) {
        gridEntries.filterIsInstance<TabGridEntry.GroupTab>()
    }
    val visibleEntries = remember(gridEntries, groupEntries, switcherFilter) {
        when (switcherFilter) {
            TabSwitcherFilter.ALL_TABS -> gridEntries
            TabSwitcherFilter.GROUPS_ONLY -> groupEntries
        }
    }

    val itemBounds = remember { mutableStateMapOf<String, Rect>() }
    var rootPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var ungroupDropBannerBounds by remember { mutableStateOf<Rect?>(null) }

    // Whole-card dragging (single tab or group card)
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dragTranslation by remember { mutableStateOf(Offset.Zero) }
    var hoveredTargetKey by remember { mutableStateOf<String?>(null) }

    // Dragging an individual member tab out of a GroupTabGridCard
    var draggedMemberTab by remember { mutableStateOf<BrowserTab?>(null) }
    var draggedMemberStartRect by remember { mutableStateOf<Rect?>(null) }
    var draggedMemberOffset by remember { mutableStateOf(Offset.Zero) }

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

    fun updateMemberHoverTarget(memberTab: BrowserTab, startRect: Rect, deltaTotal: Offset) {
        val currentCenter = startRect.center + deltaTotal
        val sourceGroupKey = memberTab.groupId?.let { "group_$it" }
        val inUngroupBanner = ungroupDropBannerBounds?.contains(currentCenter) == true
        if (inUngroupBanner) {
            hoveredTargetKey = null
            return
        }
        val hitOtherKey = itemBounds.entries.firstOrNull { (otherKey, rect) ->
            otherKey != sourceGroupKey && rect.contains(currentCenter)
        }?.key
        hoveredTargetKey = hitOtherKey
    }

    fun finishMemberDrag() {
        val member = draggedMemberTab
        val startRect = draggedMemberStartRect
        if (member != null && startRect != null) {
            val dropCenter = startRect.center + draggedMemberOffset
            val sourceGroupKey = member.groupId?.let { "group_$it" }
            val sourceGroupRect = sourceGroupKey?.let { itemBounds[it] }
            val inUngroupBanner = ungroupDropBannerBounds?.contains(dropCenter) == true

            if (inUngroupBanner) {
                onMoveGroupedTabToTarget(member.id, null)
            } else if (hoveredTargetKey != null && hoveredTargetKey != sourceGroupKey) {
                onMoveGroupedTabToTarget(member.id, hoveredTargetKey)
            } else if (sourceGroupRect == null || !sourceGroupRect.contains(dropCenter)) {
                // Dropped outside the source group card -> remove from group!
                onMoveGroupedTabToTarget(member.id, null)
            }
        }
        draggedMemberTab = null
        draggedMemberStartRect = null
        draggedMemberOffset = Offset.Zero
        hoveredTargetKey = null
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_tab_switcher_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
                title = {
                    CenteredTabsAndGroupsModePill(
                        selectedFilter = switcherFilter,
                        totalTabs = totalOpenTabs,
                        totalGroups = groupEntries.size,
                        onSelectFilter = { switcherFilter = it }
                    )
                },
                actions = {
                    TextButton(
                        onClick = onResetAllTabs,
                        modifier = Modifier.testTag("reset_tabs_button")
                    ) {
                        Text(stringResource(R.string.tabs_close_all))
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("draggable_tab_grid_switcher")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onGloballyPositioned { coords ->
                    rootPositionInRoot = coords.positionInRoot()
                }
        ) {
            if (visibleEntries.isEmpty() && switcherFilter == TabSwitcherFilter.GROUPS_ONLY) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 460.dp)
                            .testTag("empty_tab_groups_state")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.FolderCopy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.tabs_groups_empty_title),
                                style = EmphasizedTypography.activeTabTitle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.tabs_groups_empty_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (draggedMemberTab != null) 66.dp else 8.dp,
                        bottom = 120.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("tabs_lazy_grid")
                ) {
                    items(
                        items = visibleEntries,
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
                                    onCycleTabColor = { onCycleTabColor(entry.tab.id) },
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
                                    draggedMemberTabId = draggedMemberTab?.id,
                                    onOpenGroup = { onOpenGroup(entry.group.id) },
                                    onEditGroup = { onEditGroup(entry.group.id) },
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
                                    onImmediateDragEnd = { finishDrag() },
                                    onMemberTabDragStart = { memberTab, rect ->
                                        draggedMemberTab = memberTab
                                        draggedMemberStartRect = rect
                                        draggedMemberOffset = Offset.Zero
                                        hoveredTargetKey = null
                                    },
                                    onMemberTabDrag = { delta ->
                                        draggedMemberOffset += delta
                                        val member = draggedMemberTab
                                        val start = draggedMemberStartRect
                                        if (member != null && start != null) {
                                            updateMemberHoverTarget(member, start, draggedMemberOffset)
                                        }
                                    },
                                    onMemberTabDragEnd = { finishMemberDrag() }
                                )
                            }
                        }
                    }
                }
            }

            // Drag-to-Remove-From-Group Banner when dragging a mini-tab out of a Group Card
            AnimatedVisibility(
                visible = draggedMemberTab != null,
                enter = fadeIn(ApexMotion.effectsSpring()) + scaleIn(initialScale = 0.92f, animationSpec = ApexMotion.spatialSpring()),
                exit = fadeOut(ApexMotion.effectsSpring()) + scaleOut(targetScale = 0.92f, animationSpec = ApexMotion.spatialSpring()),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .zIndex(50f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .onGloballyPositioned { coords ->
                            ungroupDropBannerBounds = coords.boundsInRoot()
                        }
                        .testTag("grid_ungroup_drop_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CallSplit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tab_group_drop_ungroup_hint),
                            style = EmphasizedTypography.activeTabTitle,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Floating Ghost Pill while dragging a grouped tab out of its Group Card
            val activeDraggedMember = draggedMemberTab
            val activeMemberStartRect = draggedMemberStartRect
            if (activeDraggedMember != null && activeMemberStartRect != null) {
                val ghostTopLeft = activeMemberStartRect.topLeft - rootPositionInRoot + draggedMemberOffset
                val ghostPalette = activeDraggedMember.tabColor?.resolveTonalColors()
                Surface(
                    shape = CircleShape,
                    color = ghostPalette?.container ?: MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(
                        2.dp,
                        ghostPalette?.accent ?: MaterialTheme.colorScheme.primary
                    ),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .zIndex(65f)
                        .graphicsLayer {
                            translationX = ghostTopLeft.x
                            translationY = ghostTopLeft.y
                            scaleX = 1.06f
                            scaleY = 1.06f
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (activeDraggedMember.isHomePage) {
                                Icons.Outlined.Home
                            } else {
                                Icons.Filled.Public
                            },
                            contentDescription = null,
                            tint = ghostPalette?.accent ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = activeDraggedMember.title,
                            style = EmphasizedTypography.activeTabTitle,
                            color = ghostPalette?.onContainer ?: MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                    }
                }
            }

            // Centered Floating Action Pill (+ Add Tab | Group) at Bottom Center (No Bottom Nav)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 20.dp)
                    .testTag("tabs_center_action_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .height(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNewTab)
                            .testTag("switcher_new_tab_fab")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.tabs_action_add_tab),
                                style = EmphasizedTypography.activeTabTitle,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .height(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                onCreateGroup()
                            }
                            .testTag("switcher_new_group_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreateNewFolder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = stringResource(R.string.tabs_action_new_group),
                                style = EmphasizedTypography.activeTabTitle,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

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
                        onEditGroup = { onEditGroup(expandedGroupEntry.group.id) },
                        onAddTabToGroup = { onAddTabToGroup(expandedGroupEntry.group.id) },
                        onUngroupTab = onUngroupTab,
                        onCycleTabColor = onCycleTabColor,
                        onCloseTab = onCloseTab,
                        onDismiss = { onOpenGroup(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredTabsAndGroupsModePill(
    selectedFilter: TabSwitcherFilter,
    totalTabs: Int,
    totalGroups: Int,
    onSelectFilter: (TabSwitcherFilter) -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .height(42.dp)
            .testTag("tabs_groups_center_mode_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            val tabsSelected = selectedFilter == TabSwitcherFilter.ALL_TABS
            val tabsBg by animateColorAsState(
                targetValue = if (tabsSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                animationSpec = ApexMotion.effectsSpec(),
                label = "TabsPillSegmentBg"
            )
            val tabsFg by animateColorAsState(
                targetValue = if (tabsSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = ApexMotion.effectsSpec(),
                label = "TabsPillSegmentFg"
            )

            Surface(
                shape = CircleShape,
                color = tabsBg,
                modifier = Modifier
                    .height(34.dp)
                    .clip(CircleShape)
                    .clickable { onSelectFilter(TabSwitcherFilter.ALL_TABS) }
                    .testTag("mode_pill_tabs_segment")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Layers,
                        contentDescription = null,
                        tint = tabsFg,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${stringResource(R.string.tabs_pill_tabs)} ($totalTabs)",
                        style = MaterialTheme.typography.labelLarge,
                        color = tabsFg
                    )
                }
            }

            val groupsSelected = selectedFilter == TabSwitcherFilter.GROUPS_ONLY
            val groupsBg by animateColorAsState(
                targetValue = if (groupsSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                animationSpec = ApexMotion.effectsSpec(),
                label = "GroupsPillSegmentBg"
            )
            val groupsFg by animateColorAsState(
                targetValue = if (groupsSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = ApexMotion.effectsSpec(),
                label = "GroupsPillSegmentFg"
            )

            Surface(
                shape = CircleShape,
                color = groupsBg,
                modifier = Modifier
                    .height(34.dp)
                    .clip(CircleShape)
                    .clickable { onSelectFilter(TabSwitcherFilter.GROUPS_ONLY) }
                    .testTag("mode_pill_groups_segment")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderCopy,
                        contentDescription = null,
                        tint = groupsFg,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${stringResource(R.string.tabs_pill_groups)} ($totalGroups)",
                        style = MaterialTheme.typography.labelLarge,
                        color = groupsFg
                    )
                }
            }
        }
    }
}
