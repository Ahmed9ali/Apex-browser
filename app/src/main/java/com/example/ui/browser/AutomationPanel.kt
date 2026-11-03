package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.automation.AgentRole
import com.example.automation.ExtractedRecord
import com.example.automation.NanoChatMessage
import com.example.automation.NanoReasoningStep
import com.example.automation.StepStatus
import com.example.data.local.ExtractionRuleEntity
import com.example.ui.theme.ApexMotion
import com.example.ui.theme.EmphasizedTypography
import com.example.ui.theme.rememberMorphingCornerShape
import com.example.ui.viewmodel.ExtensionModule
import com.example.ui.viewmodel.RetrieverDisplayMode

@Composable
fun AutomationPanel(
    activeModule: ExtensionModule,
    nanoMessages: List<NanoChatMessage>,
    isNanoRunning: Boolean,
    extractionRules: List<ExtractionRuleEntity>,
    selectedRuleId: Long?,
    retrieverPromptInput: String,
    extractedRecords: List<ExtractedRecord>,
    isRetrieverRunning: Boolean,
    retrieverStatusBanner: String,
    retrieverDisplayMode: RetrieverDisplayMode,
    openTabsCount: Int,
    isSideSheet: Boolean,
    onSelectModule: (ExtensionModule) -> Unit,
    onRunNanoPrompt: (String) -> Unit,
    onRetrieverPromptChange: (String) -> Unit,
    onSelectRule: (Long) -> Unit,
    onSaveRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    onCaptureCurrentPage: () -> Unit,
    onRunParallelAllTabs: () -> Unit,
    onToggleDisplayMode: (RetrieverDisplayMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    Surface(
        shape = if (isSideSheet) {
            MaterialTheme.shapes.large
        } else {
            MaterialTheme.shapes.extraLarge
        },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxHeight()
            .testTag("automation_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Header & Module Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.automation_panel_title),
                            style = EmphasizedTypography.activeTabTitle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Local DOM Multi-Agent & Scraper",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_automation_panel_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_tab)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expressive Morphing Segmented Selector: Nano vs Retriever
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ExtensionSelectorPill(
                    selected = activeModule == ExtensionModule.NANO,
                    title = stringResource(R.string.ext_nano_title),
                    subtitle = "Planner • Navigator • Validator",
                    onClick = { onSelectModule(ExtensionModule.NANO) },
                    tag = "tab_nano_extension",
                    modifier = Modifier.weight(1f)
                )
                ExtensionSelectorPill(
                    selected = activeModule == ExtensionModule.RETRIEVER,
                    title = stringResource(R.string.ext_retriever_title),
                    subtitle = "Local-First DOM Scraper",
                    onClick = { onSelectModule(ExtensionModule.RETRIEVER) },
                    tag = "tab_retriever_extension",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            when (activeModule) {
                ExtensionModule.NANO -> NanoMultiAgentSidebar(
                    messages = nanoMessages,
                    isRunning = isNanoRunning,
                    onSubmitTask = onRunNanoPrompt,
                    modifier = Modifier.weight(1f)
                )

                ExtensionModule.RETRIEVER -> RetrieverScraperWorkspace(
                    rules = extractionRules,
                    selectedRuleId = selectedRuleId,
                    promptInput = retrieverPromptInput,
                    records = extractedRecords,
                    isRunning = isRetrieverRunning,
                    statusBanner = retrieverStatusBanner,
                    displayMode = retrieverDisplayMode,
                    openTabsCount = openTabsCount,
                    onPromptChange = onRetrieverPromptChange,
                    onSelectRule = onSelectRule,
                    onSaveRule = onSaveRule,
                    onDeleteRule = onDeleteRule,
                    onCapturePage = onCaptureCurrentPage,
                    onRunParallelTabs = onRunParallelAllTabs,
                    onToggleDisplayMode = onToggleDisplayMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExtensionSelectorPill(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    val shape = rememberMorphingCornerShape(
        targetRadius = if (selected) 22.dp else 12.dp,
        label = "ExtensionPill_$title"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "ExtensionPillBg_$title"
    )
    val fgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = ApexMotion.effectsSpec(),
        label = "ExtensionPillFg_$title"
    )

    Surface(
        shape = shape,
        color = bgColor,
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = title,
                style = EmphasizedTypography.activeTabTitle,
                color = fgColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = fgColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NanoMultiAgentSidebar(
    messages: List<NanoChatMessage>,
    isRunning: Boolean,
    onSubmitTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draftPrompt by rememberSaveable { mutableStateOf("") }

    val quickPrompts = remember {
        listOf(
            "Find the top 3 AI news articles today and summarize them",
            "Click On-Device Agents filter and highlight results",
            "Scroll and summarize all hardware products & prices"
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Quick Prompt Suggestions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            quickPrompts.forEachIndexed { idx, preset ->
                FilterChip(
                    selected = false,
                    enabled = !isRunning,
                    onClick = { onSubmitTask(preset) },
                    label = {
                        Text(
                            text = preset,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.testTag("nano_preset_chip_$idx")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("nano_messages_list")
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.isUser) {
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    NanoAgentResponseCard(message = msg)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = draftPrompt,
                onValueChange = { draftPrompt = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.nano_input_hint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .weight(1f)
                    .testTag("nano_task_input")
            )

            Button(
                enabled = !isRunning && draftPrompt.isNotBlank(),
                onClick = {
                    val toRun = draftPrompt
                    draftPrompt = ""
                    onSubmitTask(toRun)
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("nano_run_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.nano_run_action),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NanoAgentResponseCard(message: NanoChatMessage) {
    var logExpanded by rememberSaveable(message.id) { mutableStateOf(true) }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            if (message.steps.isNotEmpty()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { logExpanded = !logExpanded }
                        .testTag("nano_reasoning_toggle_${message.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (message.isRunning) {
                                    Icons.Filled.Sync
                                } else {
                                    Icons.Filled.CheckCircle
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Agent Reasoning Trace (${message.steps.size} steps)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (logExpanded) {
                                Icons.Filled.ExpandLess
                            } else {
                                Icons.Filled.ExpandMore
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(visible = logExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        message.steps.forEach { step ->
                            ReasoningStepRow(step = step)
                        }
                    }
                }
            }

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ReasoningStepRow(step: NanoReasoningStep) {
    val roleContainer = when (step.role) {
        AgentRole.PLANNER -> MaterialTheme.colorScheme.secondaryContainer
        AgentRole.NAVIGATOR -> MaterialTheme.colorScheme.primaryContainer
        AgentRole.VALIDATOR -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val roleContent = when (step.role) {
        AgentRole.PLANNER -> MaterialTheme.colorScheme.onSecondaryContainer
        AgentRole.NAVIGATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        AgentRole.VALIDATOR -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(shape = MaterialTheme.shapes.extraSmall, color = roleContainer) {
                    Text(
                        text = step.role.badge,
                        style = EmphasizedTypography.badgeCounter,
                        color = roleContent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = if (step.status == StepStatus.RUNNING) "RUNNING" else "DONE",
                    style = EmphasizedTypography.badgeCounter,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = step.actionTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = step.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!step.jsSnippet.isNullOrBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = step.jsSnippet,
                        style = EmphasizedTypography.monoCode,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RetrieverScraperWorkspace(
    rules: List<ExtractionRuleEntity>,
    selectedRuleId: Long?,
    promptInput: String,
    records: List<ExtractedRecord>,
    isRunning: Boolean,
    statusBanner: String,
    displayMode: RetrieverDisplayMode,
    openTabsCount: Int,
    onPromptChange: (String) -> Unit,
    onSelectRule: (Long) -> Unit,
    onSaveRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    onCapturePage: () -> Unit,
    onRunParallelTabs: () -> Unit,
    onToggleDisplayMode: (RetrieverDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Saved Room Extraction Rules Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            rules.forEach { rule ->
                val isSelected = rule.id == selectedRuleId
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectRule(rule.id) },
                    label = {
                        Text(
                            text = if (rule.lastRunCount > 0) {
                                "${rule.name} (${rule.lastRunCount})"
                            } else {
                                rule.name
                            }
                        )
                    },
                    trailingIcon = if (isSelected && rules.size > 1) {
                        {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete rule",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteRule(rule.id) }
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.testTag("retriever_rule_chip_${rule.id}")
                )
            }
        }

        // Extraction Rule / CSS Selector Input + Save Rule Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = onPromptChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.retriever_rule_hint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .testTag("retriever_rule_input")
            )
            FilledTonalButton(
                onClick = onSaveRule,
                enabled = promptInput.isNotBlank(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                modifier = Modifier.testTag("retriever_save_rule_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.BookmarkAdd,
                    contentDescription = stringResource(R.string.retriever_save_rule),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Capture Current Page & Multi-Tab Parallel Extraction Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                enabled = !isRunning,
                onClick = onCapturePage,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("retriever_capture_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.retriever_capture_tab), maxLines = 1)
            }

            FilledTonalButton(
                enabled = !isRunning,
                onClick = onRunParallelTabs,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("retriever_parallel_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("All $openTabsCount Tabs", maxLines = 1)
            }
        }

        // Status & View Mode Toggle (Table vs Cards)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = statusBanner,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { onToggleDisplayMode(RetrieverDisplayMode.TABLE) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TableChart,
                        contentDescription = "Table view",
                        tint = if (displayMode == RetrieverDisplayMode.TABLE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                IconButton(
                    onClick = { onToggleDisplayMode(RetrieverDisplayMode.CARDS) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ViewAgenda,
                        contentDescription = "Card list view",
                        tint = if (displayMode == RetrieverDisplayMode.CARDS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        // Extracted Structured Data Output
        if (records.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TableChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "No DOM Records Captured Yet",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Capture Page' for the current tab or 'All $openTabsCount Tabs' to run multi-tab parallel scraping completely on-device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("retriever_results_list")
            ) {
                if (displayMode == RetrieverDisplayMode.TABLE) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Tab",
                                    style = EmphasizedTypography.badgeCounter,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(0.24f)
                                )
                                Text(
                                    text = "Extracted Item",
                                    style = EmphasizedTypography.badgeCounter,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(0.46f)
                                )
                                Text(
                                    text = "Value / Meta",
                                    style = EmphasizedTypography.badgeCounter,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(0.30f)
                                )
                            }
                        }
                    }

                    items(records, key = { it.id }) { rec ->
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = rec.tabTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(0.24f)
                                        .padding(end = 6.dp)
                                )
                                Text(
                                    text = rec.primaryField,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(0.46f)
                                        .padding(end = 6.dp)
                                )
                                Text(
                                    text = rec.secondaryValue,
                                    style = EmphasizedTypography.monoCode,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(0.30f)
                                )
                            }
                        }
                    }
                } else {
                    items(records, key = { it.id }) { rec ->
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = rec.tabTitle,
                                        style = EmphasizedTypography.badgeCounter,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = rec.matchedSelector,
                                        style = EmphasizedTypography.monoCode,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = rec.primaryField,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = rec.secondaryValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
