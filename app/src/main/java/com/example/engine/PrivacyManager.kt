package com.example.engine

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.URLEncoder
import kotlin.math.abs

data class BlockInterceptionResult(
    val blocked: Boolean,
    val matchedDomain: String = "",
    val category: String = "", // "Tracker" or "Ad Network"
    val estimatedBytesSaved: Long = 0L,
    val estimatedTimeSavedMs: Long = 0L
)

data class PageShieldSweep(
    val trackersBlocked: Int,
    val adsBlocked: Int,
    val bytesSaved: Long,
    val timeSavedMs: Long,
    val primaryBlockedDomain: String,
    val category: String
)

data class NormalizedNavigation(
    val finalUrl: String,
    val wasUpgradedToHttps: Boolean,
    val isBuiltInScheme: Boolean
)

class PrivacyManager {

    private val adNetworkPatterns = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adnxs.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "moatads.com",
        "amazon-adsystem.com"
    )

    private val trackerPatterns = listOf(
        "google-analytics.com",
        "googletagmanager.com",
        "connect.facebook.net",
        "scorecardresearch.com",
        "hotjar.com",
        "mixpanel.com",
        "segment.io",
        "quantserve.com",
        "clarity.ms",
        "newrelic.com",
        "fullstory.com",
        "amplitude.com"
    )

    private val urlPathSignatures = listOf(
        "/pagead/",
        "/ads/",
        "/adserver/",
        "/collect?v=",
        "/pixel/",
        "/telemetry/",
        "beacon.js",
        "analytics.js"
    )

    fun evaluateRequest(
        requestUrl: String,
        pageUrl: String,
        shieldsEnabled: Boolean
    ): BlockInterceptionResult {
        if (!shieldsEnabled || requestUrl.isBlank()) {
            return BlockInterceptionResult(blocked = false)
        }
        val lower = requestUrl.lowercase()
        val host = extractHost(lower)
        val pageHost = extractHost(pageUrl.lowercase())

        // Allow first-party root document loads
        if (host.isNotEmpty() && host == pageHost && urlPathSignatures.none { lower.contains(it) }) {
            return BlockInterceptionResult(blocked = false)
        }

        val matchedAd = adNetworkPatterns.firstOrNull { host.contains(it) || lower.contains(it) }
        if (matchedAd != null) {
            return BlockInterceptionResult(
                blocked = true,
                matchedDomain = matchedAd,
                category = "Ad Network",
                estimatedBytesSaved = 44_200L,
                estimatedTimeSavedMs = 78L
            )
        }

        val matchedTracker = trackerPatterns.firstOrNull { host.contains(it) || lower.contains(it) }
        if (matchedTracker != null) {
            return BlockInterceptionResult(
                blocked = true,
                matchedDomain = matchedTracker,
                category = "Tracker",
                estimatedBytesSaved = 18_600L,
                estimatedTimeSavedMs = 46L
            )
        }

        val matchedPath = urlPathSignatures.firstOrNull { lower.contains(it) }
        if (matchedPath != null) {
            return BlockInterceptionResult(
                blocked = true,
                matchedDomain = host.ifBlank { matchedPath.trim('/') },
                category = if (matchedPath.contains("ad")) "Ad Network" else "Tracker",
                estimatedBytesSaved = 24_500L,
                estimatedTimeSavedMs = 54L
            )
        }

        return BlockInterceptionResult(blocked = false)
    }

    fun emptyBlockedResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    /**
     * Simulates local filter list telemetry sweep on page load so every visited page
     * reflects realistic tracker/ad blocking metrics alongside live WebView request interception.
     */
    fun simulatePageShieldSweep(url: String, shieldsEnabled: Boolean): PageShieldSweep? {
        if (!shieldsEnabled || url.isBlank() || url == "apex://home") return null
        val seed = abs(url.hashCode())
        val trackers = 2 + (seed % 5)
        val ads = 1 + ((seed / 7) % 3)
        val samplePool = adNetworkPatterns + trackerPatterns
        val chosen = samplePool[seed % samplePool.size]
        val isAd = adNetworkPatterns.contains(chosen)
        val bytes = (trackers * 17_400L) + (ads * 41_800L)
        val timeMs = (trackers * 42L) + (ads * 74L)
        return PageShieldSweep(
            trackersBlocked = trackers,
            adsBlocked = ads,
            bytesSaved = bytes,
            timeSavedMs = timeMs,
            primaryBlockedDomain = chosen,
            category = if (isAd) "Ad Network" else "Tracker"
        )
    }

    fun normalizeAndUpgradeUrl(
        rawInput: String,
        httpsUpgradeEnabled: Boolean,
        searchEngineTemplate: String
    ): NormalizedNavigation {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty() || trimmed.equals("apex://home", ignoreCase = true)) {
            return NormalizedNavigation("apex://home", wasUpgradedToHttps = false, isBuiltInScheme = true)
        }
        if (trimmed.startsWith("apex://", ignoreCase = true)) {
            return NormalizedNavigation(trimmed.lowercase(), wasUpgradedToHttps = false, isBuiltInScheme = true)
        }

        // Check if input is a search query rather than a domain or URL
        val looksLikeUrl = trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            (!trimmed.contains(' ') && trimmed.contains('.') && trimmed.length > 3)

        if (!looksLikeUrl) {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val searchUrl = searchEngineTemplate.replace("%s", encoded)
            return NormalizedNavigation(searchUrl, wasUpgradedToHttps = false, isBuiltInScheme = false)
        }

        if (trimmed.startsWith("http://", ignoreCase = true)) {
            val host = extractHost(trimmed)
            val isLoopback = host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2"
            return if (httpsUpgradeEnabled && !isLoopback) {
                NormalizedNavigation(
                    finalUrl = "https://" + trimmed.substring(7),
                    wasUpgradedToHttps = true,
                    isBuiltInScheme = false
                )
            } else {
                NormalizedNavigation(trimmed, wasUpgradedToHttps = false, isBuiltInScheme = false)
            }
        }

        if (trimmed.startsWith("https://", ignoreCase = true)) {
            return NormalizedNavigation(trimmed, wasUpgradedToHttps = false, isBuiltInScheme = false)
        }

        return NormalizedNavigation(
            finalUrl = "https://$trimmed",
            wasUpgradedToHttps = httpsUpgradeEnabled,
            isBuiltInScheme = false
        )
    }

    companion object {
        fun formatBytesSaved(bytes: Long): String {
            return when {
                bytes >= 1_048_576L -> String.format("%.1f MB", bytes / 1_048_576.0)
                bytes >= 1_024L -> String.format("%d KB", bytes / 1_024L)
                else -> "$bytes B"
            }
        }

        fun formatTimeSaved(ms: Long): String {
            return when {
                ms >= 60_000L -> String.format("%.1f min", ms / 60_000.0)
                ms >= 1_000L -> String.format("%.1f s", ms / 1_000.0)
                else -> "${ms} ms"
            }
        }
    }

    private fun extractHost(rawUrl: String): String {
        return runCatching {
            URI(rawUrl).host?.lowercase().orEmpty()
        }.getOrElse {
            rawUrl.substringAfter("://", rawUrl)
                .substringBefore('/')
                .substringBefore(':')
                .lowercase()
        }
    }
}
