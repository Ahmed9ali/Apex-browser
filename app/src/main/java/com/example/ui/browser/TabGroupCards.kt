package com.example.ui.browser

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.preferences.SearchEngineOption
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.TabGroupTonalColors
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.theme.resolveTonalColors
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.TabGridEntry
import kotlin.math.abs

@Composable
internal fun SingleTabGridCard(
    tab: BrowserTab,
    isSelected: Boolean,
    isDropTarget: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onCycleTabColor: () -> Unit,
    onImmediateDragStart: () -> Unit,
    onImmediateDrag: (Offset) -> Unit,
    onImmediateDragEnd: () -> Unit
) {
    val customTabPalette = tab.tabColor?.resolveTonalColors()

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
            isSelected && customTabPalette != null -> customTabPalette.container
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            customTabPalette != null -> customTabPalette.subtleSurface
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "SingleGridCardBg_${tab.id}"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> MaterialTheme.colorScheme.onTertiaryContainer
            isSelected && customTabPalette != null -> customTabPalette.onContainer
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "SingleGridCardFg_${tab.id}"
    )

    val borderColor = when {
        isDropTarget -> MaterialTheme.colorScheme.tertiary
        customTabPalette != null && isSelected -> customTabPalette.accent
        isSelected -> Color(0xFFFF5500) // Signature Brave highlight orange on edges
        customTabPalette != null -> customTabPalette.border.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }

    val borderWidth = when {
        isDropTarget -> 3.dp
        isSelected -> 2.5.dp // Distinct, prominent edge highlight like Brave
        else -> 1.dp
    }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(
            width = borderWidth,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .clickable(onClick = onClick)
            .testTag("grid_tab_card_${tab.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header: Drag handle, site icon, title, close button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
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
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Favicon / Engine logo
                if (tab.isHomePage) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = null,
                        tint = customTabPalette?.accent ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (tab.url.contains("google", ignoreCase = true)) {
                    SearchEngineLogo(SearchEngineOption.GOOGLE, size = 16.dp)
                } else if (tab.url.contains("duckduckgo", ignoreCase = true)) {
                    SearchEngineLogo(SearchEngineOption.DUCKDUCKGO, size = 16.dp)
                } else if (tab.url.contains("brave", ignoreCase = true)) {
                    SearchEngineLogo(SearchEngineOption.BRAVE, size = 16.dp)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = customTabPalette?.accent ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = tab.title,
                    style = EmphasizedTypography.activeTabTitle,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
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

            // Web Activity & Preview Canvas
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (isDropTarget) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = stringResource(R.string.tab_group_drop_hint),
                                style = EmphasizedTypography.badgeCounter,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else if (tab.isHomePage) {
                    // Home Start Page Mockup
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                SearchEngineLogo(SearchEngineOption.DUCKDUCKGO, size = 12.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Search or type URL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Start Page",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (tab.previewThumbnail != null) {
                    // Actual captured Web Page Snapshot with web status overlay
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = tab.previewThumbnail.asImageBitmap(),
                            contentDescription = tab.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Top domain badge
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = if (tab.isHttps) Icons.Filled.Lock else Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = if (tab.isHttps) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tab.url.removePrefix("https://").removePrefix("http://"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    // Rich Web Activity View with domain, HTTPS, shields, and activity status
                    val cleanHost = remember(tab.url) {
                        tab.url.removePrefix("https://").removePrefix("http://").substringBefore("/")
                    }
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cleanHost.ifBlank { tab.url },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Simulated web content wireframe lines
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))
                            )
                        }

                        // Web Activity Indicator Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (tab.isLoading) {
                                LinearProgressIndicator(
                                    progress = { tab.progress.coerceIn(0.1f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (tab.blockedCountOnPage > 0) {
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (tab.blockedCountOnPage > 0) {
                                                        MaterialTheme.colorScheme.tertiary
                                                    } else {
                                                        MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (tab.blockedCountOnPage > 0) {
                                                "Shield: ${tab.blockedCountOnPage} blocked"
                                            } else {
                                                cleanHost.ifBlank { "Web Page" }
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom bar: Activity badge + Tab color swatch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (tab.blockedCountOnPage > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "${tab.blockedCountOnPage} BLOCKED",
                            style = EmphasizedTypography.badgeCounter,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Drag to group",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }

                // Quick Tab Color Swatch Button
                Surface(
                    shape = CircleShape,
                    color = customTabPalette?.container ?: MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(
                        width = 1.dp,
                        color = customTabPalette?.accent ?: MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = onCycleTabColor
                        )
                        .testTag("cycle_tab_color_${tab.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = stringResource(R.string.cd_cycle_tab_color),
                            tint = customTabPalette?.accent ?: contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Group Tab Card displaying a 2x2 mini-tab grid:
 * - Tinted with the group's chosen [TabGroupColorOption].
 * - Includes an Edit button to rename the group & change its color.
 * - If `tabs.size > 4`: 3 tabs are shown in slots 1–3, and the 4th slot displays `+N` remaining count.
 * - Else (`tabs.size <= 4`): up to all 4 tabs stay shown directly in slots 1–4.
 * - Individual mini-tab slots support long-press dragging out of the group to ungroup/move!
 */
@Composable
internal fun GroupTabGridCard(
    entry: TabGridEntry.GroupTab,
    activeTabId: String,
    isDropTarget: Boolean,
    draggedMemberTabId: String?,
    onOpenGroup: () -> Unit,
    onEditGroup: () -> Unit,
    onCloseGroup: () -> Unit,
    onImmediateDragStart: () -> Unit,
    onImmediateDrag: (Offset) -> Unit,
    onImmediateDragEnd: () -> Unit,
    onMemberTabDragStart: (BrowserTab, Rect) -> Unit,
    onMemberTabDrag: (Offset) -> Unit,
    onMemberTabDragEnd: () -> Unit
) {
    val groupPalette = entry.group.colorOption.resolveTonalColors()
    val containsActive = entry.tabs.any { it.id == activeTabId }
    val totalTabs = entry.tabs.size
    val hasMoreThanFour = totalTabs > 4
    val overflowRemainingCount = totalTabs - 3

    val cardShape = rememberMorphingCornerShape(
        targetRadius = if (isDropTarget) 32.dp else 26.dp,
        label = "GroupGridCardShape_${entry.group.id}"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> MaterialTheme.colorScheme.tertiaryContainer
            containsActive -> groupPalette.container
            else -> groupPalette.subtleSurface
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
                containsActive -> groupPalette.accent
                else -> groupPalette.border
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .clickable(onClick = onOpenGroup)
            .testTag("grid_group_card_${entry.group.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
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
                        tint = groupPalette.onContainer.copy(alpha = 0.72f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(groupPalette.accent)
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
                    onClick = onEditGroup,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("edit_group_button_${entry.group.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.cd_edit_group),
                        tint = groupPalette.accent,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onCloseGroup,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("close_group_button_${entry.group.id}")
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
                    val tab0 = entry.tabs.getOrNull(0)
                    GroupMiniSlotTile(
                        tab = tab0,
                        groupPalette = groupPalette,
                        overflowCount = null,
                        isActiveTab = tab0?.id == activeTabId,
                        isBeingDraggedOut = tab0 != null && tab0.id == draggedMemberTabId,
                        onOpenGroup = onOpenGroup,
                        onDragOutStart = onMemberTabDragStart,
                        onDragOutMove = onMemberTabDrag,
                        onDragOutEnd = onMemberTabDragEnd,
                        modifier = Modifier.weight(1f)
                    )
                    val tab1 = entry.tabs.getOrNull(1)
                    GroupMiniSlotTile(
                        tab = tab1,
                        groupPalette = groupPalette,
                        overflowCount = null,
                        isActiveTab = tab1?.id == activeTabId,
                        isBeingDraggedOut = tab1 != null && tab1.id == draggedMemberTabId,
                        onOpenGroup = onOpenGroup,
                        onDragOutStart = onMemberTabDragStart,
                        onDragOutMove = onMemberTabDrag,
                        onDragOutEnd = onMemberTabDragEnd,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val tab2 = entry.tabs.getOrNull(2)
                    GroupMiniSlotTile(
                        tab = tab2,
                        groupPalette = groupPalette,
                        overflowCount = null,
                        isActiveTab = tab2?.id == activeTabId,
                        isBeingDraggedOut = tab2 != null && tab2.id == draggedMemberTabId,
                        onOpenGroup = onOpenGroup,
                        onDragOutStart = onMemberTabDragStart,
                        onDragOutMove = onMemberTabDrag,
                        onDragOutEnd = onMemberTabDragEnd,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasMoreThanFour) {
                        GroupMiniSlotTile(
                            tab = null,
                            groupPalette = groupPalette,
                            overflowCount = overflowRemainingCount,
                            isActiveTab = false,
                            isBeingDraggedOut = false,
                            onOpenGroup = onOpenGroup,
                            onDragOutStart = onMemberTabDragStart,
                            onDragOutMove = onMemberTabDrag,
                            onDragOutEnd = onMemberTabDragEnd,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("group_overflow_count_slot_${entry.group.id}")
                        )
                    } else {
                        val tab3 = entry.tabs.getOrNull(3)
                        GroupMiniSlotTile(
                            tab = tab3,
                            groupPalette = groupPalette,
                            overflowCount = null,
                            isActiveTab = tab3?.id == activeTabId,
                            isBeingDraggedOut = tab3 != null && tab3.id == draggedMemberTabId,
                            onOpenGroup = onOpenGroup,
                            onDragOutStart = onMemberTabDragStart,
                            onDragOutMove = onMemberTabDrag,
                            onDragOutEnd = onMemberTabDragEnd,
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
    groupPalette: TabGroupTonalColors,
    overflowCount: Int?,
    isActiveTab: Boolean,
    isBeingDraggedOut: Boolean,
    onOpenGroup: () -> Unit,
    onDragOutStart: (BrowserTab, Rect) -> Unit,
    onDragOutMove: (Offset) -> Unit,
    onDragOutEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        overflowCount != null -> {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = groupPalette.accent,
                modifier = modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+$overflowCount",
                        style = EmphasizedTypography.activeTabTitle,
                        color = groupPalette.onAccent
                    )
                }
            }
        }

        tab != null -> {
            var slotBounds by remember { mutableStateOf<Rect?>(null) }
            val tabPalette = tab.tabColor?.resolveTonalColors() ?: groupPalette

            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isActiveTab) {
                    tabPalette.container
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                border = BorderStroke(
                    width = if (isActiveTab) 1.5.dp else 0.8.dp,
                    color = if (isActiveTab) {
                        tabPalette.accent
                    } else {
                        tabPalette.border.copy(alpha = 0.45f)
                    }
                ),
                modifier = modifier
                    .fillMaxSize()
                    .alpha(if (isBeingDraggedOut) 0.32f else 1f)
                    .onGloballyPositioned { coords ->
                        slotBounds = coords.boundsInRoot()
                    }
                    .clickable(onClick = onOpenGroup)
                    .pointerInput(tab.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                val rect = slotBounds
                                if (rect != null) {
                                    onDragOutStart(tab, rect)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragOutMove(dragAmount)
                            },
                            onDragEnd = { onDragOutEnd() },
                            onDragCancel = { onDragOutEnd() }
                        )
                    }
                    .testTag("group_mini_tab_${tab.id}")
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
                        tint = tabPalette.accent,
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
internal fun ExpandedGroupModal(
    groupEntry: TabGridEntry.GroupTab,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onEditGroup: () -> Unit,
    onAddTabToGroup: () -> Unit,
    onUngroupTab: (String) -> Unit,
    onCycleTabColor: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val groupPalette = groupEntry.group.colorOption.resolveTonalColors()

    var modalBounds by remember { mutableStateOf<Rect?>(null) }
    var dropZoneBounds by remember { mutableStateOf<Rect?>(null) }
    val rowBoundsMap = remember { mutableStateMapOf<String, Rect>() }

    var draggedTabId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val isHoveringUngroupZone = remember(draggedTabId, dragOffset, dropZoneBounds, modalBounds) {
        val currentId = draggedTabId ?: return@remember false
        val startRect = rowBoundsMap[currentId] ?: return@remember false
        val center = startRect.center + dragOffset
        val inDropZone = dropZoneBounds?.contains(center) == true
        val outsideModal = modalBounds?.let { !it.contains(center) } == true
        val draggedFarHorizontally = abs(dragOffset.x) > 200f
        inDropZone || outsideModal || draggedFarHorizontally
    }

    fun finishModalRowDrag() {
        val id = draggedTabId
        if (id != null && isHoveringUngroupZone) {
            onUngroupTab(id)
        }
        draggedTabId = null
        dragOffset = Offset.Zero
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.52f))
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
            border = BorderStroke(2.dp, groupPalette.accent),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 540.dp)
                .padding(20.dp)
                .onGloballyPositioned { coords ->
                    modalBounds = coords.boundsInRoot()
                }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onEditGroup)
                            .padding(vertical = 4.dp)
                            .testTag("expanded_group_rename_header")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = groupPalette.container,
                            border = BorderStroke(1.5.dp, groupPalette.accent),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.FolderCopy,
                                    contentDescription = null,
                                    tint = groupPalette.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = groupEntry.group.title,
                                    style = EmphasizedTypography.headlineHero,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.cd_edit_group),
                                    tint = groupPalette.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${groupEntry.tabs.size} tabs • ${groupEntry.group.colorOption.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

                // Drag-to-Remove-From-Group Drop Zone
                val dropZoneBg by animateColorAsState(
                    targetValue = if (isHoveringUngroupZone) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                    },
                    animationSpec = ApexMotion.effectsSpec(),
                    label = "ModalUngroupDropZoneBg"
                )
                val dropZoneBorder = if (isHoveringUngroupZone) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = dropZoneBg,
                    border = BorderStroke(
                        width = if (isHoveringUngroupZone) 2.dp else 1.dp,
                        color = dropZoneBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            dropZoneBounds = coords.boundsInRoot()
                        }
                        .testTag("expanded_group_ungroup_drop_zone")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CallSplit,
                            contentDescription = null,
                            tint = if (isHoveringUngroupZone) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHoveringUngroupZone) {
                                stringResource(R.string.tab_group_drop_ungroup_hint)
                            } else {
                                stringResource(R.string.tab_group_drag_out_subtitle)
                            },
                            style = if (isHoveringUngroupZone) {
                                EmphasizedTypography.activeTabTitle
                            } else {
                                MaterialTheme.typography.labelMedium
                            },
                            color = if (isHoveringUngroupZone) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(GroupedListSpacing),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    itemsIndexed(
                        items = groupEntry.tabs,
                        key = { _, tab -> tab.id }
                    ) { index, tab ->
                        val isSelected = tab.id == activeTabId
                        val isRowDragged = draggedTabId == tab.id
                        val tabPalette = (tab.tabColor ?: groupEntry.group.colorOption).resolveTonalColors()
                        val itemShape = groupedListItemShape(
                            index = index,
                            totalCount = groupEntry.tabs.size,
                            outerRadius = 22.dp,
                            innerRadius = 5.dp
                        )

                        Surface(
                            shape = itemShape,
                            color = if (isSelected) {
                                tabPalette.container
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = if (isSelected || isRowDragged) {
                                BorderStroke(1.5.dp, tabPalette.accent)
                            } else {
                                null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(if (isRowDragged) 30f else 0f)
                                .onGloballyPositioned { coords ->
                                    if (!isRowDragged) {
                                        rowBoundsMap[tab.id] = coords.boundsInRoot()
                                    }
                                }
                                .graphicsLayer {
                                    if (isRowDragged) {
                                        translationX = dragOffset.x
                                        translationY = dragOffset.y
                                        scaleX = 1.03f
                                        scaleY = 1.03f
                                        alpha = 0.94f
                                    }
                                }
                                .clip(itemShape)
                                .clickable { onSelectTab(tab.id) }
                                .pointerInput(tab.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedTabId = tab.id
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                        },
                                        onDragEnd = { finishModalRowDrag() },
                                        onDragCancel = { finishModalRowDrag() }
                                    )
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .pointerInput(tab.id) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    draggedTabId = tab.id
                                                    dragOffset = Offset.Zero
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffset += dragAmount
                                                },
                                                onDragEnd = { finishModalRowDrag() },
                                                onDragCancel = { finishModalRowDrag() }
                                            )
                                        }
                                        .testTag("modal_drag_handle_${tab.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DragIndicator,
                                        contentDescription = stringResource(R.string.cd_drag_tab),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) {
                                        tabPalette.accent
                                    } else {
                                        tabPalette.container
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onCycleTabColor(tab.id) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (tab.isHomePage) Icons.Outlined.Home else Icons.Filled.Public,
                                            contentDescription = stringResource(R.string.cd_cycle_tab_color),
                                            tint = if (isSelected) {
                                                tabPalette.onAccent
                                            } else {
                                                tabPalette.accent
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
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
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CallSplit,
                                        contentDescription = stringResource(R.string.tab_group_ungroup),
                                        tint = tabPalette.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCloseTab(tab.id) },
                                    modifier = Modifier.size(38.dp)
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
