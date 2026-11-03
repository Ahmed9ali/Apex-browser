package com.example.automation

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.example.data.local.ExtractionRuleEntity
import com.example.engine.BuiltInShowcasePages
import kotlin.coroutines.resume
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONTokener

data class ExtractedRecord(
    val id: String,
    val tabId: String,
    val tabTitle: String,
    val primaryField: String,
    val secondaryValue: String,
    val linkUrl: String,
    val matchedSelector: String
)

data class TabSnapshotTarget(
    val tabId: String,
    val title: String,
    val url: String
)

class RetrieverEngine {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * Translates either a natural-language extraction prompt or a raw CSS selector
     * into a reusable local-first [ExtractionRuleEntity].
     */
    fun buildRuleFromInput(input: String): ExtractionRuleEntity {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        val looksLikeRawCss = trimmed.startsWith(".") ||
            trimmed.startsWith("#") ||
            trimmed.contains("[") ||
            trimmed.contains(">") ||
            trimmed in setOf("a", "h1", "h2", "h3", "p", "tr", "li", "article")

        return when {
            looksLikeRawCss -> ExtractionRuleEntity(
                name = "CSS: ${trimmed.take(28)}",
                promptDescription = "Custom CSS selector rule ($trimmed)",
                itemSelector = trimmed,
                titleSubSelector = "h1, h2, h3, a, :scope",
                valueSubSelector = ".price, span, p, :scope",
                linkSubSelector = "a, :scope"
            )

            lower.contains("product") || lower.contains("price") -> ExtractionRuleEntity(
                name = "Products & Prices",
                promptDescription = trimmed,
                itemSelector = ".product-card, [data-product], article, li",
                titleSubSelector = ".product-title, h2, h3, a",
                valueSubSelector = ".price, [data-price], strong, .score",
                linkSubSelector = "a"
            )

            lower.contains("link") || lower.contains("url") -> ExtractionRuleEntity(
                name = "Page Links & Anchors",
                promptDescription = trimmed,
                itemSelector = "a[href]",
                titleSubSelector = ":scope",
                valueSubSelector = ":scope",
                linkSubSelector = ":scope"
            )

            else -> ExtractionRuleEntity(
                name = trimmed.take(30).ifBlank { "Structured Content" },
                promptDescription = trimmed.ifBlank { "Extract headlines, values, and links" },
                itemSelector = "article, .story-card, .product-card, section, tr, li",
                titleSubSelector = "h1, h2, h3, .product-title, a",
                valueSubSelector = ".price, .score, p, span",
                linkSubSelector = "a"
            )
        }
    }

    suspend fun extractFromSingleTab(
        target: TabSnapshotTarget,
        webView: WebView?,
        rule: ExtractionRuleEntity
    ): List<ExtractedRecord> {
        if (webView != null && target.url != "apex://home") {
            val liveRecords = runDomExtractionJs(target, webView, rule)
            if (liveRecords.isNotEmpty()) {
                return liveRecords
            }
        }
        return fallbackStaticExtraction(target, rule)
    }

    /**
     * Runs the given extraction workflow across ALL open tabs in parallel!
     */
    suspend fun extractAcrossTabsInParallel(
        tabs: List<TabSnapshotTarget>,
        webViewLookup: (String) -> WebView?,
        rule: ExtractionRuleEntity
    ): List<ExtractedRecord> = coroutineScope {
        tabs.map { tab ->
            async {
                extractFromSingleTab(
                    target = tab,
                    webView = webViewLookup(tab.tabId),
                    rule = rule
                )
            }
        }.awaitAll().flatten()
    }

    private suspend fun runDomExtractionJs(
        target: TabSnapshotTarget,
        webView: WebView,
        rule: ExtractionRuleEntity
    ): List<ExtractedRecord> {
        val itemSel = rule.itemSelector.replace("'", "\\'")
        val titleSel = rule.titleSubSelector.replace("'", "\\'")
        val valSel = rule.valueSubSelector.replace("'", "\\'")
        val linkSel = rule.linkSubSelector.replace("'", "\\'")

        val script = """
            (function() {
                try {
                    const pick = (root, sel) => {
                        if (!sel || sel === ':scope') return root;
                        return root.querySelector(sel) || root;
                    };
                    let nodes = Array.from(document.querySelectorAll('$itemSel'));
                    if (nodes.length === 0) {
                        nodes = Array.from(document.querySelectorAll('article, h1, h2, h3, a[href]'));
                    }
                    const results = [];
                    nodes.slice(0, 24).forEach((node, idx) => {
                        node.style.outline = '1.5px dashed rgba(52, 211, 153, 0.75)';
                        const tEl = pick(node, '$titleSel');
                        const vEl = pick(node, '$valSel');
                        const lEl = node.tagName === 'A' ? node : node.querySelector('$linkSel');
                        const primary = (tEl ? (tEl.innerText || '') : (node.innerText || '')).trim().replace(/\s+/g, ' ');
                        let secondary = (vEl && vEl !== tEl ? (vEl.innerText || '') : '').trim().replace(/\s+/g, ' ');
                        const href = lEl && lEl.href ? lEl.href : (node.href || '');
                        if (!secondary && href) secondary = href;
                        if (primary.length > 1) {
                            results.push({
                                p: primary.slice(0, 110),
                                s: secondary.slice(0, 90),
                                u: href.slice(0, 140),
                                tag: node.tagName.toLowerCase()
                            });
                        }
                    });
                    return JSON.stringify(results);
                } catch (e) {
                    return "[]";
                }
            })();
        """.trimIndent()

        val raw = suspendCancellableCoroutine<String> { cont ->
            mainHandler.post {
                if (!cont.isActive) return@post
                runCatching {
                    webView.evaluateJavascript(script) { res ->
                        if (cont.isActive) cont.resume(res ?: "[]")
                    }
                }.onFailure {
                    if (cont.isActive) cont.resume("[]")
                }
            }
        }

        return parseExtractedJson(raw, target, rule.itemSelector)
    }

    private fun parseExtractedJson(
        raw: String,
        target: TabSnapshotTarget,
        selector: String
    ): List<ExtractedRecord> {
        if (raw.isBlank() || raw == "null") return emptyList()
        return runCatching {
            val unquoted = if (raw.startsWith("\"") && raw.endsWith("\"")) {
                JSONTokener(raw).nextValue().toString()
            } else {
                raw
            }
            val arr = JSONArray(unquoted)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val primary = obj.optString("p", "").trim()
                    if (primary.isNotEmpty()) {
                        add(
                            ExtractedRecord(
                                id = "${target.tabId}_$i",
                                tabId = target.tabId,
                                tabTitle = target.title.ifBlank { "Tab" },
                                primaryField = primary,
                                secondaryValue = obj.optString("s", "—").ifBlank { "—" },
                                linkUrl = obj.optString("u", ""),
                                matchedSelector = obj.optString("tag", selector)
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Parses built-in HTML or Speed Dial metadata when a background tab's WebView
     * has not been inflated yet or when the user captures the Start Page directly.
     */
    private fun fallbackStaticExtraction(
        target: TabSnapshotTarget,
        rule: ExtractionRuleEntity
    ): List<ExtractedRecord> {
        if (target.url == "apex://home" || target.url.isBlank()) {
            return BuiltInShowcasePages.speedDialItems.mapIndexed { idx, item ->
                ExtractedRecord(
                    id = "${target.tabId}_sd_$idx",
                    tabId = target.tabId,
                    tabTitle = "Apex Start Page",
                    primaryField = item.title,
                    secondaryValue = "${item.badge} • ${item.subtitle}",
                    linkUrl = item.url,
                    matchedSelector = "speed-dial"
                )
            }
        }

        val builtIn = BuiltInShowcasePages.resolveHtml(target.url)?.second
        if (builtIn != null) {
            val headingRegex = Regex("<h2[^>]*>(.*?)</h2>", RegexOption.DOT_MATCHES_ALL)
            val priceOrMetaRegex = Regex("<(?:div|span) class=\"(?:price|score)\">(.*?)</(?:div|span)>")
            val titles = headingRegex.findAll(builtIn)
                .map { it.groupValues[1].replace(Regex("<[^>]+>"), "").replace("&amp;", "&").trim() }
                .toList()
            val values = priceOrMetaRegex.findAll(builtIn)
                .map { it.groupValues[1].replace(Regex("<[^>]+>"), "").trim() }
                .toList()

            return titles.mapIndexed { idx, title ->
                ExtractedRecord(
                    id = "${target.tabId}_static_$idx",
                    tabId = target.tabId,
                    tabTitle = target.title,
                    primaryField = title,
                    secondaryValue = values.getOrElse(idx) { "Extracted via ${rule.name}" },
                    linkUrl = target.url,
                    matchedSelector = rule.itemSelector
                )
            }
        }

        return listOf(
            ExtractedRecord(
                id = "${target.tabId}_meta_0",
                tabId = target.tabId,
                tabTitle = target.title,
                primaryField = target.title.ifBlank { target.url },
                secondaryValue = "Ready — focus tab to hydrate live DOM",
                linkUrl = target.url,
                matchedSelector = rule.itemSelector
            )
        )
    }
}
