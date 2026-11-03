package com.example.ui.browser

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.TabGridEntry

@Composable
internal fun SingleTabGridCard(
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
            .height(194.dp)
            .clickable(onClick = onClick)
            .testTag("grid_tab_card_${tab.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
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
                            text = if (tab.isHomePage) "Home" else tab.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

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
internal fun GroupTabGridCard(
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
    val overflowRemainingCount = totalTabs - 3

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
            .height(194.dp)
            .clickable(onClick = onOpenGroup)
            .testTag("grid_group_card_${entry.group.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
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
internal fun ExpandedGroupModal(
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
                        val itemShape = groupedListItemShape(
                            index = index,
                            totalCount = groupEntry.tabs.size,
                            outerRadius = 22.dp,
                            innerRadius = 5.dp
                        )
                        Surface(
                            shape = itemShape,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(itemShape)
                                .clickable { onSelectTab(tab.id) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (tab.isHomePage) Icons.Outlined.Home else Icons.Filled.Public,
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.primary
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
                                        tint = MaterialTheme.colorScheme.secondary,
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
