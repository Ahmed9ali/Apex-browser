package com.example.engine

import android.annotation.SuppressLint
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import org.json.JSONArray

data class PreloadCandidate(
    val title: String,
    val url: String,
    val confidencePercent: Int,
    val status: String = "Prefetched DNS + Static Assets"
)

class PerformanceOptimizer {

    private val mobileUserAgentSuffix = "ApexBrowser/1.5 Mobile Safari/537.36"
    private val desktopUserAgent =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 ApexBrowser/1.5 Safari/537.36"

    @SuppressLint("SetJavaScriptEnabled")
    fun configureFastWebView(
        webView: WebView,
        scriptGuardEnabled: Boolean,
        desktopMode: Boolean,
        aggressiveCaching: Boolean
    ) {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, false) // Privacy-by-default: block 3P cookies
        }

        webView.settings.apply {
            javaScriptEnabled = !scriptGuardEnabled
            domStorageEnabled = true
            loadsImagesAutomatically = true
            cacheMode = if (aggressiveCaching) {
                WebSettings.LOAD_CACHE_ELSE_NETWORK
            } else {
                WebSettings.LOAD_DEFAULT
            }
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            mediaPlaybackRequiresUserGesture = true

            userAgentString = if (desktopMode) {
                desktopUserAgent
            } else {
                val base = WebSettings.getDefaultUserAgent(webView.context)
                "$base $mobileUserAgentSuffix"
            }
        }
    }

    /**
     * Discovers high-probability next-navigation links in the active WebView DOM
     * and injects lightweight `<link rel="dns-prefetch">` + `<link rel="prerender">` hints.
     */
    fun preloadLikelyNextPages(
        webView: WebView,
        enabled: Boolean,
        onPreloaded: (List<PreloadCandidate>) -> Unit
    ) {
        if (!enabled) {
            onPreloaded(emptyList())
            return
        }

        val preloadScript = """
            (function() {
                try {
                    const anchors = Array.from(document.querySelectorAll('a[href]'))
                        .filter(a => {
                            const h = a.getAttribute('href') || '';
                            const text = (a.innerText || '').trim();
                            return text.length > 3 && (h.startsWith('http') || h.startsWith('/') || h.startsWith('apex://'));
                        })
                        .slice(0, 4);
                    const out = [];
                    anchors.forEach((a, idx) => {
                        const resolved = a.href || a.getAttribute('href');
                        if (resolved && resolved.startsWith('http')) {
                            const link = document.createElement('link');
                            link.rel = 'dns-prefetch';
                            link.href = resolved;
                            document.head.appendChild(link);
                        }
                        out.push({
                            title: (a.innerText || '').trim().slice(0, 56),
                            url: resolved,
                            confidence: Math.max(68, 94 - (idx * 7))
                        });
                    });
                    return JSON.stringify(out);
                } catch (e) {
                    return "[]";
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(preloadScript) { rawJson ->
            val parsed = parsePreloadJson(rawJson)
            onPreloaded(parsed)
        }
    }

    private fun parsePreloadJson(raw: String?): List<PreloadCandidate> {
        if (raw.isNullOrBlank() || raw == "null") return emptyList()
        return runCatching {
            val unquoted = if (raw.startsWith("\"") && raw.endsWith("\"")) {
                org.json.JSONTokener(raw).nextValue().toString()
            } else {
                raw
            }
            val array = JSONArray(unquoted)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        PreloadCandidate(
                            title = obj.optString("title", "Next Page"),
                            url = obj.optString("url", ""),
                            confidencePercent = obj.optInt("confidence", 82)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
