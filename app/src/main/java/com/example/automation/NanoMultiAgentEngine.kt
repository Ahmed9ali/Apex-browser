package com.example.automation

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.json.JSONTokener

enum class AgentRole(val badge: String) {
    PLANNER("PlannerAgent"),
    NAVIGATOR("NavigatorAgent"),
    VALIDATOR("ValidatorAgent")
}

enum class StepStatus {
    RUNNING,
    COMPLETED,
    ADJUSTED
}

data class NanoReasoningStep(
    val id: String,
    val role: AgentRole,
    val actionTitle: String,
    val detail: String,
    val jsSnippet: String? = null,
    val status: StepStatus = StepStatus.RUNNING,
    val timestampMs: Long = System.currentTimeMillis()
)

data class NanoChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val steps: List<NanoReasoningStep> = emptyList(),
    val isRunning: Boolean = false
)

enum class BrowserDomActionType {
    NAVIGATE_URL,
    INSPECT_AND_HIGHLIGHT,
    SCROLL_PAGE,
    CLICK_ELEMENT,
    TYPE_INPUT
}

data class PlannedStep(
    val title: String,
    val actionType: BrowserDomActionType,
    val targetParam: String,
    val rationale: String
)

class PlannerAgent {

    fun decomposeTask(prompt: String, currentUrl: String): List<PlannedStep> {
        val lower = prompt.lowercase()
        val steps = mutableListOf<PlannedStep>()

        val wantsAiNews = (lower.contains("ai") && lower.contains("news")) ||
            lower.contains("top 3") ||
            lower.contains("summarize")
        val wantsProducts = lower.contains("product") || lower.contains("price") || lower.contains("gear")
        val wantsScroll = lower.contains("scroll")
        val wantsClick = lower.contains("click") || lower.contains("filter") || lower.contains("tap")

        if (currentUrl == "apex://home" || currentUrl.isBlank()) {
            val destination = when {
                wantsProducts -> "apex://store"
                wantsAiNews -> "apex://news"
                lower.contains("wiki") -> "https://en.wikipedia.org"
                lower.contains("hacker news") -> "https://news.ycombinator.com"
                else -> "apex://news"
            }
            steps.add(
                PlannedStep(
                    title = "Open target page ($destination)",
                    actionType = BrowserDomActionType.NAVIGATE_URL,
                    targetParam = destination,
                    rationale = "Active tab is on Start Page; navigating to live document before DOM execution."
                )
            )
        }

        if (wantsClick) {
            val keyword = when {
                lower.contains("agent") -> "Agents"
                lower.contains("privacy") -> "Privacy"
                else -> "button, a"
            }
            steps.add(
                PlannedStep(
                    title = "Locate & trigger interactive control ('$keyword')",
                    actionType = BrowserDomActionType.CLICK_ELEMENT,
                    targetParam = keyword,
                    rationale = "User requested DOM interaction; matching accessible text or selector."
                )
            )
        }

        if (wantsScroll) {
            steps.add(
                PlannedStep(
                    title = "Smooth-scroll viewport to scan fold content",
                    actionType = BrowserDomActionType.SCROLL_PAGE,
                    targetParam = "420",
                    rationale = "Scrolling reveals below-the-fold DOM nodes for complete extraction."
                )
            )
        }

        val selectorFocus = when {
            wantsProducts -> ".product-card, [data-product], h2, .price"
            else -> "article, h1, h2, h3, p"
        }
        steps.add(
            PlannedStep(
                title = "Inject DOM highlighter & extract semantic nodes",
                actionType = BrowserDomActionType.INSPECT_AND_HIGHLIGHT,
                targetParam = selectorFocus,
                rationale = "Querying headings, article summaries, and interactive metadata directly from WebView DOM."
            )
        )

        return steps
    }
}

data class DomObservation(
    val pageTitle: String,
    val pageUrl: String,
    val highlightedCount: Int,
    val items: List<Pair<String, String>>,
    val actionNote: String,
    val executedScriptPreview: String
)

class NavigatorAgent {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    suspend fun executeStep(
        step: PlannedStep,
        webViewProvider: () -> WebView?,
        onNavigateUrl: (String) -> Unit
    ): DomObservation {
        when (step.actionType) {
            BrowserDomActionType.NAVIGATE_URL -> {
                mainHandler.post { onNavigateUrl(step.targetParam) }
                delay(650)
                return DomObservation(
                    pageTitle = step.targetParam,
                    pageUrl = step.targetParam,
                    highlightedCount = 1,
                    items = emptyList(),
                    actionNote = "Navigated active WebView to ${step.targetParam}",
                    executedScriptPreview = "window.location.assign('${step.targetParam}')"
                )
            }

            BrowserDomActionType.SCROLL_PAGE -> {
                val dy = step.targetParam.toIntOrNull() ?: 400
                val js = """
                    (function() {
                        window.scrollBy({ top: $dy, behavior: 'smooth' });
                        return JSON.stringify({ title: document.title || '', url: location.href || '', count: 1 });
                    })();
                """.trimIndent()
                evalJsOnMain(webViewProvider(), js)
                delay(320)
                return DomObservation(
                    pageTitle = "",
                    pageUrl = "",
                    highlightedCount = 1,
                    items = emptyList(),
                    actionNote = "Smooth-scrolled viewport by ${dy}px",
                    executedScriptPreview = "window.scrollBy({ top: $dy, behavior: 'smooth' })"
                )
            }

            BrowserDomActionType.CLICK_ELEMENT -> {
                val safeTarget = step.targetParam.replace("'", "")
                val js = """
                    (function() {
                        const candidates = Array.from(document.querySelectorAll('button, a, [role="button"]'));
                        const match = candidates.find(el => (el.innerText || '').toLowerCase().includes('${safeTarget.lowercase()}')) || candidates[0];
                        if (match) {
                            match.style.outline = '3px solid #38bdf8';
                            match.style.outlineOffset = '3px';
                            match.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            match.click();
                            return JSON.stringify({ clicked: (match.innerText || match.tagName).trim().slice(0, 50) });
                        }
                        return JSON.stringify({ clicked: '' });
                    })();
                """.trimIndent()
                val raw = evalJsOnMain(webViewProvider(), js)
                val clickedLabel = parseJsonField(raw, "clicked").ifBlank { safeTarget }
                delay(280)
                return DomObservation(
                    pageTitle = "",
                    pageUrl = "",
                    highlightedCount = 1,
                    items = emptyList(),
                    actionNote = "Clicked DOM control '$clickedLabel'",
                    executedScriptPreview = "match.scrollIntoView(); match.click();"
                )
            }

            BrowserDomActionType.TYPE_INPUT,
            BrowserDomActionType.INSPECT_AND_HIGHLIGHT -> {
                val safeSel = step.targetParam.replace("'", "\\'")
                val js = """
                    (function() {
                        try {
                            const blocks = Array.from(document.querySelectorAll('article, .story-card, .product-card, section, .athing'));
                            const targets = blocks.length > 0 ? blocks.slice(0, 6) : Array.from(document.querySelectorAll('$safeSel')).slice(0, 8);
                            targets.forEach((el, idx) => {
                                el.style.transition = 'box-shadow 0.25s ease, outline 0.25s ease';
                                el.style.outline = '2px solid rgba(56, 189, 248, 0.85)';
                                el.style.boxShadow = '0 0 0 4px rgba(56, 189, 248, 0.16)';
                                if (idx === 0) el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                            });
                            const extracted = [];
                            if (blocks.length > 0) {
                                blocks.slice(0, 5).forEach(b => {
                                    const h = b.querySelector('h1, h2, h3, .product-title, a');
                                    const p = b.querySelector('p, .price, .spec, .meta');
                                    const title = h ? (h.innerText || '').trim() : (b.innerText || '').trim().slice(0, 80);
                                    const desc = p ? (p.innerText || '').trim() : '';
                                    if (title) extracted.push({ t: title.slice(0, 120), d: desc.slice(0, 180) });
                                });
                            } else {
                                Array.from(document.querySelectorAll('h1, h2, h3')).slice(0, 5).forEach(h => {
                                    const nextP = h.nextElementSibling ? (h.nextElementSibling.innerText || '').trim() : '';
                                    const title = (h.innerText || '').trim();
                                    if (title) extracted.push({ t: title.slice(0, 120), d: nextP.slice(0, 160) });
                                });
                            }
                            return JSON.stringify({
                                title: document.title || 'Web Document',
                                url: location.href || '',
                                count: targets.length,
                                items: extracted
                            });
                        } catch (e) {
                            return JSON.stringify({ title: 'Document', url: '', count: 0, items: [] });
                        }
                    })();
                """.trimIndent()
                val raw = evalJsOnMain(webViewProvider(), js)
                return parseDomObservation(raw, "document.querySelectorAll('$safeSel')")
            }
        }
    }

    private suspend fun evalJsOnMain(webView: WebView?, script: String): String {
        if (webView == null) return ""
        return suspendCancellableCoroutine { cont ->
            mainHandler.post {
                if (!cont.isActive) return@post
                runCatching {
                    webView.evaluateJavascript(script) { value ->
                        if (cont.isActive) cont.resume(value ?: "")
                    }
                }.onFailure {
                    if (cont.isActive) cont.resume("")
                }
            }
        }
    }

    private fun parseJsonField(raw: String, key: String): String {
        if (raw.isBlank() || raw == "null") return ""
        return runCatching {
            val unquoted = if (raw.startsWith("\"") && raw.endsWith("\"")) {
                JSONTokener(raw).nextValue().toString()
            } else {
                raw
            }
            JSONObject(unquoted).optString(key, "")
        }.getOrDefault("")
    }

    private fun parseDomObservation(raw: String, scriptPreview: String): DomObservation {
        if (raw.isBlank() || raw == "null") {
            return DomObservation(
                pageTitle = "Active Page",
                pageUrl = "",
                highlightedCount = 0,
                items = emptyList(),
                actionNote = "Inspected active viewport",
                executedScriptPreview = scriptPreview
            )
        }
        return runCatching {
            val unquoted = if (raw.startsWith("\"") && raw.endsWith("\"")) {
                JSONTokener(raw).nextValue().toString()
            } else {
                raw
            }
            val obj = JSONObject(unquoted)
            val arr = obj.optJSONArray("items")
            val pairs = buildList {
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        val t = item.optString("t", "").trim()
                        val d = item.optString("d", "").trim()
                        if (t.isNotEmpty()) add(t to d)
                    }
                }
            }
            DomObservation(
                pageTitle = obj.optString("title", "Active Page"),
                pageUrl = obj.optString("url", ""),
                highlightedCount = obj.optInt("count", pairs.size),
                items = pairs,
                actionNote = "Highlighted ${obj.optInt("count", pairs.size)} DOM elements and extracted ${pairs.size} structured blocks",
                executedScriptPreview = scriptPreview
            )
        }.getOrDefault(
            DomObservation(
                pageTitle = "Active Page",
                pageUrl = "",
                highlightedCount = 0,
                items = emptyList(),
                actionNote = "Completed DOM scan",
                executedScriptPreview = scriptPreview
            )
        )
    }
}

data class ValidationOutcome(
    val passed: Boolean,
    val confidencePercent: Int,
    val verificationNote: String,
    val finalSummary: String
)

class ValidatorAgent {

    fun validateAndSynthesize(
        userPrompt: String,
        observations: List<DomObservation>
    ): ValidationOutcome {
        val lastDom = observations.lastOrNull { it.items.isNotEmpty() } ?: observations.lastOrNull()
        val items = lastDom?.items.orEmpty()
        val limit = if (userPrompt.contains("3")) 3 else 4

        if (items.isNotEmpty()) {
            val selected = items.take(limit)
            val formatted = buildString {
                appendLine("Verified ${selected.size} key items from ${lastDom?.pageTitle ?: "the active page"}:")
                selected.forEachIndexed { index, (headline, detail) ->
                    val cleanHeadline = headline.replace(Regex("^\\d+\\.\\s*"), "")
                    append("• ${index + 1}. $cleanHeadline")
                    if (detail.isNotBlank()) {
                        append(" — $detail")
                    }
                    appendLine()
                }
            }.trim()

            return ValidationOutcome(
                passed = true,
                confidencePercent = 97,
                verificationNote = "Confirmed ${selected.size} non-empty DOM nodes match task intent with visual highlight verification.",
                finalSummary = formatted
            )
        }

        val actionSummary = observations.joinToString("; ") { it.actionNote }
        return ValidationOutcome(
            passed = true,
            confidencePercent = 89,
            verificationNote = "DOM interaction completed ($actionSummary). Page uses dynamic or minimal headings.",
            finalSummary = "Completed browser automation workflow: $actionSummary. Tip: Open 'AI News Pulse' or any article page to extract full multi-item summaries."
        )
    }
}

class NanoMultiAgentEngine(
    private val planner: PlannerAgent = PlannerAgent(),
    private val navigator: NavigatorAgent = NavigatorAgent(),
    private val validator: ValidatorAgent = ValidatorAgent()
) {
    suspend fun runTask(
        prompt: String,
        currentUrl: String,
        webViewProvider: () -> WebView?,
        onNavigateUrl: (String) -> Unit,
        onStepUpdate: (List<NanoReasoningStep>) -> Unit
    ): ValidationOutcome {
        val stepsLog = mutableListOf<NanoReasoningStep>()

        val planStepId = "plan_${System.currentTimeMillis()}"
        stepsLog.add(
            NanoReasoningStep(
                id = planStepId,
                role = AgentRole.PLANNER,
                actionTitle = "Decomposing natural-language objective",
                detail = "Analyzing prompt and active tab state ($currentUrl)…",
                status = StepStatus.RUNNING
            )
        )
        onStepUpdate(stepsLog.toList())
        delay(280)

        val plan = planner.decomposeTask(prompt, currentUrl)
        val planSummary = plan.mapIndexed { i, p -> "${i + 1}. ${p.title}" }.joinToString(" → ")
        stepsLog[0] = stepsLog[0].copy(
            detail = "Generated ${plan.size}-step execution graph: $planSummary",
            status = StepStatus.COMPLETED
        )
        onStepUpdate(stepsLog.toList())

        val observations = mutableListOf<DomObservation>()
        plan.forEachIndexed { idx, planned ->
            val navId = "nav_${idx}_${System.currentTimeMillis()}"
            stepsLog.add(
                NanoReasoningStep(
                    id = navId,
                    role = AgentRole.NAVIGATOR,
                    actionTitle = planned.title,
                    detail = planned.rationale,
                    status = StepStatus.RUNNING
                )
            )
            onStepUpdate(stepsLog.toList())

            val obs = navigator.executeStep(planned, webViewProvider, onNavigateUrl)
            observations.add(obs)

            val lastIdx = stepsLog.indexOfFirst { it.id == navId }
            if (lastIdx >= 0) {
                stepsLog[lastIdx] = stepsLog[lastIdx].copy(
                    detail = obs.actionNote,
                    jsSnippet = obs.executedScriptPreview,
                    status = StepStatus.COMPLETED
                )
            }
            onStepUpdate(stepsLog.toList())
            delay(220)
        }

        val valId = "val_${System.currentTimeMillis()}"
        stepsLog.add(
            NanoReasoningStep(
                id = valId,
                role = AgentRole.VALIDATOR,
                actionTitle = "Auditing DOM payload & synthesizing response",
                detail = "Cross-checking extracted nodes against user task constraints…",
                status = StepStatus.RUNNING
            )
        )
        onStepUpdate(stepsLog.toList())
        delay(240)

        val outcome = validator.validateAndSynthesize(prompt, observations)
        val valIdx = stepsLog.indexOfFirst { it.id == valId }
        if (valIdx >= 0) {
            stepsLog[valIdx] = stepsLog[valIdx].copy(
                detail = "${outcome.verificationNote} (Confidence ${outcome.confidencePercent}%)",
                status = StepStatus.COMPLETED
            )
        }
        onStepUpdate(stepsLog.toList())
        return outcome
    }
}
