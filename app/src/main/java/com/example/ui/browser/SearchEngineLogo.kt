package com.example.ui.browser

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.SearchEngineOption

@Composable
fun SearchEngineLogo(
    engine: SearchEngineOption,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val isDark = isSystemInDarkTheme()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("search_engine_logo_${engine.id}")
    ) {
        when (engine) {
            SearchEngineOption.GOOGLE -> {
                Image(
                    painter = painterResource(R.drawable.ic_search_google),
                    contentDescription = engine.displayName,
                    modifier = Modifier.size(size)
                )
            }
            SearchEngineOption.DUCKDUCKGO -> {
                Image(
                    painter = painterResource(R.drawable.ic_search_duckduckgo),
                    contentDescription = engine.displayName,
                    modifier = Modifier.size(size)
                )
            }
            SearchEngineOption.BRAVE -> {
                Image(
                    painter = painterResource(R.drawable.ic_search_brave),
                    contentDescription = engine.displayName,
                    modifier = Modifier.size(size)
                )
            }
            SearchEngineOption.BING -> {
                Image(
                    painter = painterResource(R.drawable.ic_logo_bing),
                    contentDescription = engine.displayName,
                    modifier = Modifier.size(size)
                )
            }
            SearchEngineOption.WIKIPEDIA -> {
                Image(
                    painter = painterResource(R.drawable.ic_logo_wikipedia),
                    contentDescription = engine.displayName,
                    colorFilter = if (isDark) ColorFilter.tint(MaterialTheme.colorScheme.onSurface) else null,
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}
