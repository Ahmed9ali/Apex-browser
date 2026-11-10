package com.example.ui.browser

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.preferences.BrowserPreferences
import com.example.engine.BuiltInShowcasePages
import com.example.engine.PerformanceOptimizer
import com.example.engine.PrivacyManager
import com.example.ui.viewmodel.BrowserTab

private const val LAST_LOADED_URL_TAG = -19048102

@Composable
fun WebViewViewport(
    tab: BrowserTab,
    preferences: BrowserPreferences,
    privacyManager: PrivacyManager,
    performanceOptimizer: PerformanceOptimizer,
    existingWebView: WebView?,
    onRegisterWebView: (String, WebView) -> Unit,
    onPageStarted: (String, String) -> Unit,
    onProgressChanged: (String, Int) -> Unit,
    onPageFinished: (String, String?, String?, Boolean, Boolean) -> Unit,
    onSubresourceBlocked: (String, String, String, String, Long, Long) -> Unit,
    onHttpsUpgradeTriggered: (String) -> Unit,
    onScrollDelta: (Int) -> Unit,
    onPageLoadFailed: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember(tab.id) {
        existingWebView ?: WebView(context).apply {
            performanceOptimizer.configureFastWebView(
                webView = this,
                scriptGuardEnabled = preferences.scriptGuardEnabled,
                desktopMode = preferences.desktopViewportEnabled,
                aggressiveCaching = preferences.aggressivePreloadEnabled
            )
            setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                val dy = scrollY - oldScrollY
                onScrollDelta(dy)
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onProgressChanged(tab.id, newProgress)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val reqUrl = request?.url?.toString().orEmpty()
                    if (reqUrl.isBlank()) return false

                    if (reqUrl.startsWith("http://", ignoreCase = true) && preferences.httpsUpgradeEnabled) {
                        val upgraded = "https://" + reqUrl.substring(7)
                        onHttpsUpgradeTriggered(upgraded)
                        view?.loadUrl(upgraded)
                        return true
                    }
                    return false
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val reqUrl = request?.url?.toString().orEmpty()
                    if (request?.isForMainFrame == true) {
                        return super.shouldInterceptRequest(view, request)
                    }
                    val result = privacyManager.evaluateRequest(
                        requestUrl = reqUrl,
                        pageUrl = tab.url,
                        shieldsEnabled = preferences.shieldsEnabled
                    )
                    if (result.blocked) {
                        onSubresourceBlocked(
                            tab.id,
                            tab.title,
                            result.matchedDomain,
                            result.category,
                            result.estimatedBytesSaved,
                            result.estimatedTimeSavedMs
                        )
                        return privacyManager.emptyBlockedResponse()
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (!url.isNullOrBlank()) {
                        onPageStarted(tab.id, url)
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onPageFinished(
                        tab.id,
                        view?.title,
                        url,
                        view?.canGoBack() ?: false,
                        view?.canGoForward() ?: false
                    )
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        val reqUrl = request.url?.toString().orEmpty()
                        onPageLoadFailed(tab.id, reqUrl)
                    }
                }
            }
            onRegisterWebView(tab.id, this)
        }
    }

    LaunchedEffect(
        preferences.scriptGuardEnabled,
        preferences.desktopViewportEnabled,
        preferences.aggressivePreloadEnabled
    ) {
        performanceOptimizer.configureFastWebView(
            webView = webView,
            scriptGuardEnabled = preferences.scriptGuardEnabled,
            desktopMode = preferences.desktopViewportEnabled,
            aggressiveCaching = preferences.aggressivePreloadEnabled
        )
    }

    LaunchedEffect(tab.url, tab.isOfflineCopy, tab.offlineHtmlContent) {
        val lastTarget = webView.getTag(LAST_LOADED_URL_TAG) as? String
        val targetKey = "${tab.url}#offline=${tab.isOfflineCopy}"
        if (lastTarget != targetKey && !tab.isHomePage) {
            webView.setTag(LAST_LOADED_URL_TAG, targetKey)
            if (tab.isOfflineCopy && !tab.offlineHtmlContent.isNullOrBlank()) {
                webView.loadDataWithBaseURL(
                    tab.url,
                    tab.offlineHtmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            } else {
                val builtIn = BuiltInShowcasePages.resolveHtml(tab.url)
                if (builtIn != null) {
                    webView.loadDataWithBaseURL(
                        builtIn.first,
                        builtIn.second,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else {
                    webView.loadUrl(tab.url)
                }
            }
        }
    }

    AndroidView(
        factory = { webView },
        update = { view ->
            onRegisterWebView(tab.id, view)
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("browser_webview_${tab.id}")
    )
}
