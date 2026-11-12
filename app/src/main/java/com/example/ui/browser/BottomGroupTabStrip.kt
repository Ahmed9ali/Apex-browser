package com.example.ui.browser

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.SearchEngineOption
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.TabGroupColorOption
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.theme.resolveTonalColors
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.TabGroupInfo

/**
 * Persistent Group Tab Strip displayed immediately above the bottom NavigationBar
 * whenever the user is viewing a tab inside a Tab Group on the Home/Browser screen.
 * Displays the group's color & name (tap to rename/recolor), all tabs in the group
 * with per-tab delete (x) buttons, and a quick (+) button to add another tab to the group.
 */
@Composable
fun BottomGroupTabStrip(
    group: TabGroupInfo,
    groupTabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onAddTabToGroup: () -> Unit,
    onEditGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groupPalette = group.colorOption.resolveTonalColors()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_group_tab_strip")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = groupPalette.border.copy(alpha = 0.45f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                // Group Icon Button (Shows group color/folder icon, tap to edit group; no text)
                Surface(
                    shape = CircleShape,
                    color = groupPalette.container,
                    border = BorderStroke(1.5.dp, groupPalette.border),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = onEditGroup
                        )
                        .testTag("bottom_group_strip_edit_pill")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.FolderCopy,
                            contentDescription = stringResource(R.string.cd_edit_group),
                            tint = groupPalette.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(
                    modifier = Modifier.height(22.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // All Tabs in Active Group (Icon-only, active tab edges highlighted like Brave)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(end = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_group_strip_tabs_row")
                ) {
                    items(
                        items = groupTabs,
                        key = { it.id }
                    ) { tab ->
                        val isSelected = tab.id == activeTabId
                        val tabPalette = (tab.tabColor ?: group.colorOption).resolveTonalColors()
                        val chipShape = RoundedCornerShape(10.dp)
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) {
                                tabPalette.container
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            },
                            animationSpec = ApexMotion.effectsSpec(),
                            label = "BottomGroupTabBg_${tab.id}"
                        )

                        Surface(
                            shape = chipShape,
                            color = chipBg,
                            border = if (isSelected) {
                                BorderStroke(2.dp, tabPalette.accent)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .animateItem(placementSpec = ApexMotion.spatialSpring())
                                .clip(chipShape)
                                .clickable { onSelectTab(tab.id) }
                                .testTag("bottom_group_strip_tab_${tab.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (tab.isHomePage) {
                                    Icon(
                                        imageVector = Icons.Outlined.Home,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) tabPalette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                } else if (tab.url.contains("google", ignoreCase = true)) {
                                    SearchEngineLogo(SearchEngineOption.GOOGLE, size = 19.dp)
                                } else if (tab.url.contains("duckduckgo", ignoreCase = true)) {
                                    SearchEngineLogo(SearchEngineOption.DUCKDUCKGO, size = 19.dp)
                                } else if (tab.url.contains("brave", ignoreCase = true)) {
                                    SearchEngineLogo(SearchEngineOption.BRAVE, size = 19.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Public,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) tabPalette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Add New Tab Directly Inside This Group
                Surface(
                    shape = CircleShape,
                    color = groupPalette.container,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = onAddTabToGroup
                        )
                        .testTag("bottom_group_strip_add_tab")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.cd_add_tab_in_group),
                            tint = groupPalette.onContainer,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Group Rename & Color Picker Dialog.
 * Allows renaming any Tab Group, choosing its tonal accent color (applied to the group
 * and its member tabs), or ungrouping all tabs in one tap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupRenameColorDialog(
    group: TabGroupInfo,
    onSave: (newTitle: String, selectedColor: TabGroupColorOption) -> Unit,
    onUngroupAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var draftTitle by rememberSaveable(group.id) { mutableStateOf(group.title) }
    var selectedColor by rememberSaveable(group.id) { mutableStateOf(group.colorOption) }
    val previewPalette = selectedColor.resolveTonalColors()

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.5.dp, previewPalette.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("group_rename_color_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header + Live Colored Group Badge Preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.tab_group_edit_title),
                        style = EmphasizedTypography.headlineHero,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = CircleShape,
                        color = previewPalette.container,
                        border = BorderStroke(1.dp, previewPalette.border)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderCopy,
                                contentDescription = null,
                                tint = previewPalette.accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = draftTitle.trim().ifEmpty { group.title },
                                style = EmphasizedTypography.badgeCounter,
                                color = previewPalette.onContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 110.dp)
                            )
                        }
                    }
                }

                // Group Name Text Field
                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(stringResource(R.string.tab_group_name_label)) },
                    placeholder = { Text(stringResource(R.string.tab_group_name_placeholder)) },
                    trailingIcon = {
                        if (draftTitle.isNotEmpty()) {
                            IconButton(onClick = { draftTitle = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_clear_input)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onSave(draftTitle, selectedColor) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_rename_input")
                )

                // Group & Tab Color Swatch Selector
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.tab_group_color_label),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedColor.label,
                            style = EmphasizedTypography.badgeCounter,
                            color = previewPalette.accent
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        TabGroupColorOption.entries.forEach { option ->
                            val swatchPalette = option.resolveTonalColors()
                            val isChosen = option == selectedColor

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isChosen) swatchPalette.container else swatchPalette.accent
                                    )
                                    .border(
                                        width = if (isChosen) 2.5.dp else 1.dp,
                                        color = if (isChosen) {
                                            swatchPalette.accent
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable(role = Role.RadioButton) {
                                        selectedColor = option
                                    }
                                    .testTag("group_color_swatch_${option.id}")
                            ) {
                                if (isChosen) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(swatchPalette.accent)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = option.label,
                                            tint = swatchPalette.onAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dialog Actions: Ungroup All | Cancel | Save
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onUngroupAll,
                        modifier = Modifier.testTag("group_dialog_ungroup_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CallSplit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.tab_group_ungroup_all))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("group_dialog_cancel_button")
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                        Button(
                            onClick = { onSave(draftTitle, selectedColor) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = previewPalette.accent,
                                contentColor = previewPalette.onAccent
                            ),
                            modifier = Modifier.testTag("group_dialog_save_button")
                        ) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            }
        }
    }
}
