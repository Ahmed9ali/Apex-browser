package com.example.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OfflineStorageUtil {

    /**
     * Unescapes the JSON-string output returned by Android WebView's `evaluateJavascript`.
     * Pure Kotlin implementation so local JVM unit tests run without Android JSON stubs.
     */
    fun unescapeJsString(rawOutput: String): String {
        val trimmed = rawOutput.trim()
        if (trimmed == "null" || trimmed.isEmpty()) return ""
        val content = if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }

        val sb = StringBuilder(content.length)
        var i = 0
        while (i < content.length) {
            val c = content[i]
            if (c == '\\' && i + 1 < content.length) {
                when (val next = content[i + 1]) {
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    'b' -> { sb.append('\b'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    '\'' -> { sb.append('\''); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    '/' -> { sb.append('/'); i += 2 }
                    'u' -> {
                        if (i + 5 < content.length) {
                            val hex = content.substring(i + 2, i + 6)
                            val code = hex.toIntOrNull(16)
                            if (code != null) {
                                sb.append(code.toChar())
                                i += 6
                            } else {
                                sb.append("\\u")
                                i += 2
                            }
                        } else {
                            sb.append(c)
                            i++
                        }
                    }
                    else -> { sb.append(next); i += 2 }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Formats bytes into a human-readable string (e.g., 42 KB, 1.4 MB).
     */
    fun formatByteSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    /**
     * Formats timestamp into relative or friendly human-readable date.
     */
    fun formatSavedTimestamp(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        val diffMs = (nowMs - timestampMs).coerceAtLeast(0L)
        val diffSecs = diffMs / 1000L
        val diffMins = diffSecs / 60L
        val diffHours = diffMins / 60L
        val diffDays = diffHours / 24L

        return when {
            diffSecs < 45 -> "Just now"
            diffMins < 60 -> "${diffMins}m ago"
            diffHours < 24 -> "${diffHours}h ago"
            diffDays < 7 -> "${diffDays}d ago"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestampMs))
        }
    }

    /**
     * Extracts a concise plain-text snippet from an HTML document string.
     */
    fun extractSnippet(html: String, maxLength: Int = 180): String {
        val stripped = html
            .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (stripped.length > maxLength) {
            stripped.substring(0, maxLength) + "…"
        } else {
            stripped
        }
    }
}
