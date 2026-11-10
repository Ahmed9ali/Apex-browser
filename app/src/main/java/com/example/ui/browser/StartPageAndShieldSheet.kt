package com.example.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TopVisitEntity
import com.example.data.preferences.BrowserPreferences
import com.example.ui.theme.EmphasizedTypography
import java.net.URI

@Composable
fun StartPageHome(
    preferences: BrowserPreferences,
    topVisits: List<TopVisitEntity>,
    onOpenSearchScreen: () -> Unit,
    onSelectDestination: (String) -> Unit,
    onAddTopVisit: (String, String) -> Unit,
    onDeleteTopVisit: (Long) -> Unit,
    offlinePagesCount: Int = 0,
    onOpenOfflinePagesScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var visitPendingDelete by remember { mutableStateOf<TopVisitEntity?>(null) }

    val curvedSearchShape = RoundedCornerShape(16.dp)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("start_page_hub")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
        ) {
            // Recent Top Visits Horizontal Row with + Add Button on the right
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_top_visits_section")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.home_top_visits_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_top_visits_row")
                    ) {
                        // Top visits shortcuts first
                        items(
                            items = topVisits,
                            key = { it.id }
                        ) { visit ->
                            TopVisitTile(
                                topVisit = visit,
                                onClick = { onSelectDestination(visit.url) },
                                onDelete = { visitPendingDelete = visit }
                            )
                        }

                        // Add button tile placed at the right side
                        item(key = "add_shortcut_tile") {
                            AddTopVisitTile(onClick = { showAddDialog = true })
                        }
                    }
                }
            }

            // Quick Offline Pages Shortcut (if user has saved pages)
            if (offlinePagesCount > 0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenOfflinePagesScreen)
                            .testTag("home_offline_pages_shortcut")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudDone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.menu_offline_pages),
                                    style = EmphasizedTypography.activeTabTitle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$offlinePagesCount saved ${if (offlinePagesCount == 1) "page" else "pages"} available offline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onOpenOfflinePagesScreen) {
                                Text(stringResource(R.string.home_bookmarks_manage))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Shortcut Dialog
    if (showAddDialog) {
        AddTopVisitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, url ->
                onAddTopVisit(name, url)
                showAddDialog = false
            }
        )
    }

    // Delete Shortcut Confirmation Dialog
    visitPendingDelete?.let { visit ->
        AlertDialog(
            onDismissRequest = { visitPendingDelete = null },
            title = { Text(stringResource(R.string.cd_remove_top_visit)) },
            text = { Text("Remove \"${visit.title}\" from your top visits?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTopVisit(visit.id)
                        visitPendingDelete = null
                    }
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { visitPendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AddTopVisitTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .width(68.dp)
            .testTag("home_add_top_visit_tile")
    ) {
        Surface(
            shape = tileShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .size(56.dp)
                .clip(tileShape)
                .clickable(onClick = onClick)
                .testTag("home_add_top_visit_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.home_add_top_visit),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.home_add_top_visit),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TopVisitTile(
    topVisit: TopVisitEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(16.dp)

    val hostLabel = remember(topVisit.url) {
        runCatching {
            URI(topVisit.url).host?.removePrefix("www.")?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: topVisit.url
    }

    val monogram = remember(hostLabel, topVisit.title) {
        topVisit.title.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            ?: hostLabel.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            ?: "W"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .width(68.dp)
            .testTag("home_top_visit_tile_${topVisit.id}")
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                shape = tileShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .size(56.dp)
                    .clip(tileShape)
                    .clickable(onClick = onClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = monogram,
                                style = EmphasizedTypography.activeTabTitle,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Quick delete trigger button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDelete)
                    .testTag("home_remove_top_visit_${topVisit.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_remove_top_visit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = topVisit.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AddTopVisitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_top_visit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.dialog_top_visit_name_label)) },
                    placeholder = { Text(stringResource(R.string.dialog_top_visit_name_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_top_visit_name_input")
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.dialog_top_visit_url_label)) },
                    placeholder = { Text(stringResource(R.string.dialog_top_visit_url_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_top_visit_url_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedUrl = url.trim()
                    val cleanUrl = if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                        "https://$trimmedUrl"
                    } else {
                        trimmedUrl
                    }
                    val cleanTitle = title.trim().ifEmpty {
                        runCatching { URI(cleanUrl).host?.removePrefix("www.") }.getOrNull() ?: cleanUrl
                    }
                    onConfirm(cleanTitle, cleanUrl)
                },
                enabled = url.isNotBlank(),
                modifier = Modifier.testTag("dialog_top_visit_confirm_button")
            ) {
                Text(stringResource(R.string.dialog_add_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_top_visit_cancel_button")
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
