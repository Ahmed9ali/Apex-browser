package com.example.engine

import com.example.data.preferences.SearchEngineOption
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SuggestionKind {
    DIRECT_URL,
    ENGINE_SUGGESTION,
    RECENT_HISTORY
}

data class SearchSuggestionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val submitValue: String,
    val fillText: String,
    val engineBadge: String,
    val kind: SuggestionKind
)

class SearchSuggestionEngine {

    suspend fun fetchSuggestions(
        rawQuery: String,
        engine: SearchEngineOption
    ): List<SearchSuggestionItem> = withContext(Dispatchers.IO) {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchSuggestionItem>()

        // 1. Direct URL navigation option if query resembles a domain or URL
        val looksLikeDomainOrUrl = trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            (!trimmed.contains(' ') && trimmed.contains('.') && trimmed.length >= 4)

        if (looksLikeDomainOrUrl) {
            val normalizedUrl = if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
                trimmed
            } else {
                "https://$trimmed"
            }
            results.add(
                SearchSuggestionItem(
                    id = "url_$normalizedUrl",
                    title = normalizedUrl,
                    subtitle = "Open web address directly",
                    submitValue = normalizedUrl,
                    fillText = normalizedUrl,
                    engineBadge = "URL",
                    kind = SuggestionKind.DIRECT_URL
                )
            )
        }

        // 2. Fetch live autocomplete suggestions from the active Search Engine
        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        val endpoint = buildSuggestUrl(engine, encoded)

        val liveItems = runCatching {
            val body = httpGetJson(endpoint)
            parseEngineSuggestions(body, trimmed, engine)
        }.getOrElse {
            // Fallback to DuckDuckGo OpenSearch if Brave API rate-limits
            if (engine == SearchEngineOption.BRAVE) {
                runCatching {
                    val ddgBody = httpGetJson(buildSuggestUrl(SearchEngineOption.DUCKDUCKGO, encoded))
                    parseEngineSuggestions(ddgBody, trimmed, engine)
                }.getOrDefault(emptyList())
            } else {
                emptyList()
            }
        }

        if (liveItems.isNotEmpty()) {
            results.addAll(liveItems)
        } else {
            // Always provide at least the direct search submission row when offline
            results.addAll(buildOfflineEngineFallback(trimmed, engine))
        }

        results.distinctBy { it.title.lowercase() }.take(10)
    }

    fun buildSuggestUrl(engine: SearchEngineOption, encodedQuery: String): String {
        return when (engine) {
            SearchEngineOption.DUCKDUCKGO ->
                "https://ac.duckduckgo.com/ac/?q=$encodedQuery&type=list"

            SearchEngineOption.GOOGLE ->
                "https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery"

            SearchEngineOption.BRAVE ->
                "https://search.brave.com/api/suggest?q=$encodedQuery"

            SearchEngineOption.BING ->
                "https://api.bing.com/osjson.aspx?query=$encodedQuery"

            SearchEngineOption.WIKIPEDIA ->
                "https://en.wikipedia.org/w/api.php?action=opensearch&search=$encodedQuery&limit=8&namespace=0&format=json"
        }
    }

    internal fun parseEngineSuggestions(
        rawJson: String,
        originalQuery: String,
        engine: SearchEngineOption
    ): List<SearchSuggestionItem> {
        val clean = rawJson.trim()
        if (clean.isEmpty()) return emptyList()

        val root = (LiteJsonParser.parse(clean) as? List<*>) ?: return emptyList()
        if (root.isEmpty()) return emptyList()

        // Format A: [{"phrase": "..."}, ...] (DuckDuckGo default object array)
        if (root[0] is Map<*, *>) {
            return buildList {
                for (i in root.indices) {
                    val obj = root[i] as? Map<*, *> ?: continue
                    val phrase = (obj["phrase"] as? String)?.trim().orEmpty()
                    if (phrase.isNotEmpty()) {
                        add(
                            SearchSuggestionItem(
                                id = "${engine.id}_obj_$i",
                                title = phrase,
                                subtitle = "",
                                submitValue = phrase,
                                fillText = phrase,
                                engineBadge = engine.shortBadge,
                                kind = SuggestionKind.ENGINE_SUGGESTION
                            )
                        )
                    }
                }
            }
        }

        // Format B: OpenSearch ["query", ["s1", "s2", ...], ["desc1", ...], ["url1", ...]]
        val phrasesList = root.getOrNull(1) as? List<*> ?: return emptyList()
        val descriptionsList = root.getOrNull(2) as? List<*>
        val urlsList = root.getOrNull(3) as? List<*>

        return buildList {
            val firstSuggestion = (phrasesList.firstOrNull() as? String)?.trim().orEmpty()
            if (!firstSuggestion.equals(originalQuery, ignoreCase = true)) {
                add(
                    SearchSuggestionItem(
                        id = "${engine.id}_exact",
                        title = originalQuery,
                        subtitle = "",
                        submitValue = originalQuery,
                        fillText = originalQuery,
                        engineBadge = engine.shortBadge,
                        kind = SuggestionKind.ENGINE_SUGGESTION
                    )
                )
            }

            for (i in phrasesList.indices) {
                val phrase = (phrasesList[i] as? String)?.trim().orEmpty()
                if (phrase.isEmpty()) continue

                val desc = (descriptionsList?.getOrNull(i) as? String)?.trim().orEmpty()
                val directUrl = (urlsList?.getOrNull(i) as? String)?.trim().orEmpty()

                val subtitle = when {
                    desc.isNotEmpty() -> desc
                    engine == SearchEngineOption.WIKIPEDIA && directUrl.isNotEmpty() -> directUrl
                    else -> ""
                }

                val submitTarget = if (engine == SearchEngineOption.WIKIPEDIA && directUrl.startsWith("https://")) {
                    directUrl
                } else {
                    phrase
                }

                add(
                    SearchSuggestionItem(
                        id = "${engine.id}_$i",
                        title = phrase,
                        subtitle = subtitle,
                        submitValue = submitTarget,
                        fillText = phrase,
                        engineBadge = engine.shortBadge,
                        kind = SuggestionKind.ENGINE_SUGGESTION
                    )
                )
            }
        }
    }

    private fun buildOfflineEngineFallback(
        query: String,
        engine: SearchEngineOption
    ): List<SearchSuggestionItem> {
        val suffixes = when (engine) {
            SearchEngineOption.WIKIPEDIA -> listOf("", " history", " overview")
            else -> listOf("", " news", " guide", " official site")
        }
        return suffixes.mapIndexed { idx, suffix ->
            val candidate = "$query$suffix".trim()
            SearchSuggestionItem(
                id = "fallback_${engine.id}_$idx",
                title = candidate,
                subtitle = "",
                submitValue = candidate,
                fillText = candidate,
                engineBadge = engine.shortBadge,
                kind = SuggestionKind.ENGINE_SUGGESTION
            )
        }
    }

    private fun httpGetJson(endpoint: String): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3_500
            readTimeout = 3_500
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ApexBrowser/1.5 (Android; Mobile)")
        }
        return try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code")
            }
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

private object LiteJsonParser {
    fun parse(input: String): Any? {
        val reader = Reader(input)
        reader.skipWhitespace()
        return reader.readValue()
    }

    private class Reader(private val src: String) {
        private var pos = 0

        fun skipWhitespace() {
            while (pos < src.length && src[pos].isWhitespace()) pos++
        }

        fun readValue(): Any? {
            skipWhitespace()
            if (pos >= src.length) return null
            return when (src[pos]) {
                '[' -> readArray()
                '{' -> readObject()
                '"' -> readString()
                't', 'f' -> readBoolean()
                'n' -> readNull()
                else -> readPrimitive()
            }
        }

        private fun readArray(): List<Any?> {
            pos++ // '['
            val list = mutableListOf<Any?>()
            skipWhitespace()
            if (pos < src.length && src[pos] == ']') {
                pos++
                return list
            }
            while (pos < src.length) {
                list.add(readValue())
                skipWhitespace()
                if (pos < src.length && src[pos] == ',') {
                    pos++
                    skipWhitespace()
                } else if (pos < src.length && src[pos] == ']') {
                    pos++
                    break
                } else {
                    break
                }
            }
            return list
        }

        private fun readObject(): Map<String, Any?> {
            pos++ // '{'
            val map = linkedMapOf<String, Any?>()
            skipWhitespace()
            if (pos < src.length && src[pos] == '}') {
                pos++
                return map
            }
            while (pos < src.length) {
                skipWhitespace()
                if (pos >= src.length || src[pos] != '"') break
                val key = readString()
                skipWhitespace()
                if (pos < src.length && src[pos] == ':') pos++
                val value = readValue()
                map[key] = value
                skipWhitespace()
                if (pos < src.length && src[pos] == ',') {
                    pos++
                } else if (pos < src.length && src[pos] == '}') {
                    pos++
                    break
                } else {
                    break
                }
            }
            return map
        }

        private fun readString(): String {
            pos++ // opening '"'
            val sb = StringBuilder()
            while (pos < src.length) {
                val c = src[pos++]
                if (c == '"') break
                if (c == '\\' && pos < src.length) {
                    when (val esc = src[pos++]) {
                        '"', '\\', '/' -> sb.append(esc)
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 <= src.length) {
                                val hex = src.substring(pos, pos + 4)
                                hex.toIntOrNull(16)?.let { sb.append(it.toChar()) }
                                pos += 4
                            }
                        }
                        else -> sb.append(esc)
                    }
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun readBoolean(): Boolean {
            return if (src.startsWith("true", pos)) {
                pos += 4
                true
            } else {
                pos = (pos + 5).coerceAtMost(src.length)
                false
            }
        }

        private fun readNull(): Any? {
            pos = (pos + 4).coerceAtMost(src.length)
            return null
        }

        private fun readPrimitive(): String {
            val start = pos
            while (pos < src.length && src[pos] !in charArrayOf(',', ']', '}') && !src[pos].isWhitespace()) {
                pos++
            }
            return src.substring(start, pos)
        }
    }
}
