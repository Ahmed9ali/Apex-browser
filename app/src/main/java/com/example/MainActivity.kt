package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.browser.BrowserScreen
import com.example.ui.theme.ApexBrowserTheme
import com.example.ui.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialIntentUrl = extractBrowsableUrl(intent)

        setContent {
            val browserViewModel: BrowserViewModel = viewModel(
                factory = BrowserViewModel.provideFactory(applicationContext)
            )
            val preferences by browserViewModel.preferencesState.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(activity = this)

            LaunchedEffect(initialIntentUrl) {
                if (!initialIntentUrl.isNullOrBlank()) {
                    browserViewModel.submitNavigation(initialIntentUrl)
                }
            }

            ApexBrowserTheme(
                dynamicColor = preferences.dynamicColorEnabled,
                seedPreset = preferences.seedPreset
            ) {
                BrowserScreen(
                    viewModel = browserViewModel,
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }

    private fun extractBrowsableUrl(intent: Intent?): String? {
        if (intent?.action == Intent.ACTION_VIEW) {
            return intent.dataString
        }
        return null
    }
}
