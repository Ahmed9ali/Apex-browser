package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class SeedPalettePreset(val label: String, val seedColor: Color) {
    ELECTRIC_COBALT("Electric Cobalt", Color(0xFF0066FF)),
    CYBER_EMERALD("Cyber Emerald", Color(0xFF00A878)),
    AMETHYST_PULSE("Amethyst Pulse", Color(0xFF7B42F6)),
    SOLAR_CORAL("Solar Coral", Color(0xFFE85D3F))
}

/**
 * Tonal color options for Tab Groups and individual Tabs, derived dynamically
 * for light and dark Material 3 themes without hardcoded hex colors in UI composables.
 */
enum class TabGroupColorOption(
    val id: String,
    val label: String,
    val hueDegrees: Float
) {
    COBALT("cobalt", "Cobalt", 218f),
    EMERALD("emerald", "Emerald", 155f),
    VIOLET("violet", "Violet", 272f),
    CORAL("coral", "Coral", 14f),
    AMBER("amber", "Amber", 42f),
    CYAN("cyan", "Cyan", 192f),
    ROSE("rose", "Rose", 336f);

    fun next(): TabGroupColorOption {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

@Immutable
data class TabGroupTonalColors(
    val accent: Color,
    val onAccent: Color,
    val container: Color,
    val onContainer: Color,
    val subtleSurface: Color,
    val border: Color
)

@Composable
fun TabGroupColorOption.resolveTonalColors(
    darkTheme: Boolean = isSystemInDarkTheme()
): TabGroupTonalColors {
    return ApexColorSchemeBuilder.groupTonalColors(this, darkTheme)
}

/**
 * Generates a full Material 3 Expressive tonal [ColorScheme] from any [seed] color,
 * mirroring `ColorScheme.fromSeed()` with full tonal surface container roles.
 */
object ApexColorSchemeBuilder {

    fun groupTonalColors(option: TabGroupColorOption, darkTheme: Boolean): TabGroupTonalColors {
        val h = option.hueDegrees
        return if (darkTheme) {
            TabGroupTonalColors(
                accent = hslToComposeColor(h, 0.85f, 0.72f),
                onAccent = hslToComposeColor(h, 0.88f, 0.13f),
                container = hslToComposeColor(h, 0.65f, 0.25f),
                onContainer = hslToComposeColor(h, 0.90f, 0.91f),
                subtleSurface = hslToComposeColor(h, 0.42f, 0.16f),
                border = hslToComposeColor(h, 0.76f, 0.62f)
            )
        } else {
            TabGroupTonalColors(
                accent = hslToComposeColor(h, 0.82f, 0.42f),
                onAccent = hslToComposeColor(h, 0.20f, 0.99f),
                container = hslToComposeColor(h, 0.84f, 0.89f),
                onContainer = hslToComposeColor(h, 0.85f, 0.15f),
                subtleSurface = hslToComposeColor(h, 0.68f, 0.94f),
                border = hslToComposeColor(h, 0.72f, 0.48f)
            )
        }
    }

    fun fromSeed(seed: Color, darkTheme: Boolean): ColorScheme {
        val primaryHue = extractHueDegrees(seed)
        val secondaryHue = (primaryHue + 18f) % 360f
        val tertiaryHue = (primaryHue + 62f) % 360f
        val errorHue = 6f

        fun tone(hue: Float, sat: Float, lightness: Float): Color {
            return hslToComposeColor(
                hue = hue,
                saturation = sat.coerceIn(0f, 1f),
                lightness = lightness.coerceIn(0.02f, 0.99f)
            )
        }

        return if (darkTheme) {
            darkColorScheme(
                primary = tone(primaryHue, 0.88f, 0.76f),
                onPrimary = tone(primaryHue, 0.90f, 0.14f),
                primaryContainer = tone(primaryHue, 0.72f, 0.26f),
                onPrimaryContainer = tone(primaryHue, 0.92f, 0.90f),
                inversePrimary = tone(primaryHue, 0.82f, 0.40f),
                secondary = tone(secondaryHue, 0.42f, 0.76f),
                onSecondary = tone(secondaryHue, 0.48f, 0.16f),
                secondaryContainer = tone(secondaryHue, 0.38f, 0.25f),
                onSecondaryContainer = tone(secondaryHue, 0.55f, 0.90f),
                tertiary = tone(tertiaryHue, 0.68f, 0.78f),
                onTertiary = tone(tertiaryHue, 0.72f, 0.15f),
                tertiaryContainer = tone(tertiaryHue, 0.58f, 0.26f),
                onTertiaryContainer = tone(tertiaryHue, 0.78f, 0.91f),
                background = tone(primaryHue, 0.22f, 0.07f),
                onBackground = tone(primaryHue, 0.16f, 0.92f),
                surface = tone(primaryHue, 0.22f, 0.08f),
                onSurface = tone(primaryHue, 0.16f, 0.93f),
                surfaceVariant = tone(primaryHue, 0.24f, 0.19f),
                onSurfaceVariant = tone(primaryHue, 0.18f, 0.78f),
                surfaceTint = tone(primaryHue, 0.88f, 0.76f),
                inverseSurface = tone(primaryHue, 0.14f, 0.90f),
                inverseOnSurface = tone(primaryHue, 0.20f, 0.14f),
                error = tone(errorHue, 0.82f, 0.76f),
                onError = tone(errorHue, 0.85f, 0.16f),
                errorContainer = tone(errorHue, 0.70f, 0.26f),
                onErrorContainer = tone(errorHue, 0.88f, 0.90f),
                outline = tone(primaryHue, 0.16f, 0.54f),
                outlineVariant = tone(primaryHue, 0.20f, 0.26f),
                scrim = Color.Black,
                surfaceBright = tone(primaryHue, 0.20f, 0.22f),
                surfaceDim = tone(primaryHue, 0.24f, 0.06f),
                surfaceContainerLowest = tone(primaryHue, 0.26f, 0.05f),
                surfaceContainerLow = tone(primaryHue, 0.22f, 0.11f),
                surfaceContainer = tone(primaryHue, 0.22f, 0.14f),
                surfaceContainerHigh = tone(primaryHue, 0.22f, 0.18f),
                surfaceContainerHighest = tone(primaryHue, 0.22f, 0.23f)
            )
        } else {
            lightColorScheme(
                primary = tone(primaryHue, 0.86f, 0.40f),
                onPrimary = tone(primaryHue, 0.20f, 0.99f),
                primaryContainer = tone(primaryHue, 0.88f, 0.90f),
                onPrimaryContainer = tone(primaryHue, 0.88f, 0.13f),
                inversePrimary = tone(primaryHue, 0.85f, 0.78f),
                secondary = tone(secondaryHue, 0.38f, 0.38f),
                onSecondary = tone(secondaryHue, 0.20f, 0.99f),
                secondaryContainer = tone(secondaryHue, 0.48f, 0.90f),
                onSecondaryContainer = tone(secondaryHue, 0.48f, 0.14f),
                tertiary = tone(tertiaryHue, 0.64f, 0.36f),
                onTertiary = tone(tertiaryHue, 0.20f, 0.99f),
                tertiaryContainer = tone(tertiaryHue, 0.75f, 0.90f),
                onTertiaryContainer = tone(tertiaryHue, 0.72f, 0.13f),
                background = tone(primaryHue, 0.25f, 0.98f),
                onBackground = tone(primaryHue, 0.24f, 0.11f),
                surface = tone(primaryHue, 0.25f, 0.98f),
                onSurface = tone(primaryHue, 0.24f, 0.11f),
                surfaceVariant = tone(primaryHue, 0.26f, 0.91f),
                onSurfaceVariant = tone(primaryHue, 0.20f, 0.30f),
                surfaceTint = tone(primaryHue, 0.86f, 0.40f),
                inverseSurface = tone(primaryHue, 0.22f, 0.18f),
                inverseOnSurface = tone(primaryHue, 0.24f, 0.94f),
                error = tone(errorHue, 0.78f, 0.42f),
                onError = tone(errorHue, 0.20f, 0.99f),
                errorContainer = tone(errorHue, 0.85f, 0.91f),
                onErrorContainer = tone(errorHue, 0.82f, 0.14f),
                outline = tone(primaryHue, 0.16f, 0.46f),
                outlineVariant = tone(primaryHue, 0.22f, 0.82f),
                scrim = Color.Black,
                surfaceBright = tone(primaryHue, 0.25f, 0.99f),
                surfaceDim = tone(primaryHue, 0.22f, 0.88f),
                surfaceContainerLowest = Color.White,
                surfaceContainerLow = tone(primaryHue, 0.28f, 0.96f),
                surfaceContainer = tone(primaryHue, 0.26f, 0.93f),
                surfaceContainerHigh = tone(primaryHue, 0.26f, 0.90f),
                surfaceContainerHighest = tone(primaryHue, 0.26f, 0.86f)
            )
        }
    }

    private fun extractHueDegrees(color: Color): Float {
        val r = color.red
        val g = color.green
        val b = color.blue
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC
        if (delta == 0f) return 220f
        val rawHue = when (maxC) {
            r -> ((g - b) / delta) % 6f
            g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        } * 60f
        return if (rawHue < 0f) rawHue + 360f else rawHue
    }

    private fun hslToComposeColor(hue: Float, saturation: Float, lightness: Float): Color {
        val c = (1f - abs(2f * lightness - 1f)) * saturation
        val x = c * (1f - abs(((hue / 60f) % 2f) - 1f))
        val m = lightness - c / 2f
        val (r1, g1, b1) = when {
            hue < 60f -> Triple(c, x, 0f)
            hue < 120f -> Triple(x, c, 0f)
            hue < 180f -> Triple(0f, c, x)
            hue < 240f -> Triple(0f, x, c)
            hue < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color(
            red = (r1 + m).coerceIn(0f, 1f),
            green = (g1 + m).coerceIn(0f, 1f),
            blue = (b1 + m).coerceIn(0f, 1f),
            alpha = 1f
        )
    }
}
