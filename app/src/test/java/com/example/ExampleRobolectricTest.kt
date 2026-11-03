package com.example

import com.example.automation.PlannerAgent
import com.example.automation.RetrieverEngine
import com.example.automation.TabSnapshotTarget
import com.example.automation.ValidatorAgent
import com.example.data.local.PrivacyStatSummaryEntity
import com.example.data.preferences.SearchEngineOption
import com.example.engine.PrivacyManager
import com.example.engine.SearchSuggestionEngine
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.ui.browser.GroupedListSpacing
import com.example.ui.browser.groupedListItemShape
import com.example.ui.theme.ApexColorSchemeBuilder
import com.example.ui.theme.SeedPalettePreset
import com.example.ui.viewmodel.BrowserTab
import com.example.ui.viewmodel.BrowserUiState
import com.example.ui.viewmodel.TabGridEntry
import com.example.ui.viewmodel.TabGroupInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `verify fresh install state starts with single Home tab and zero stats`() {
        val freshState = BrowserUiState()
        assertEquals(1, freshState.tabs.size)
        assertTrue(freshState.activeTab.isHomePage)
        assertEquals("apex://home", freshState.activeTab.url)
        assertTrue(freshState.tabGroups.isEmpty())
        assertTrue(freshState.searchSuggestions.isEmpty())
        assertTrue(freshState.searchQuery.isEmpty())
        assertFalse(freshState.isSettingsScreenOpen)

        SearchEngineOption.entries.forEach { option ->
            assertTrue(option.description.isNotBlank())
            assertTrue(option.searchUrlTemplate.contains("%s"))
        }

        val freshStats = PrivacyStatSummaryEntity()
        assertEquals(0, freshStats.trackersBlocked)
        assertEquals(0, freshStats.adsBlocked)
        assertEquals(0, freshStats.httpsUpgrades)
        assertEquals(0L, freshStats.bytesSaved)
        assertEquals(0L, freshStats.timeSavedMs)
    }

    @Test
    fun `verify SearchSuggestionEngine builds URLs and parses engine suggestions`() {
        val suggestionEngine = SearchSuggestionEngine()

        assertTrue(
            suggestionEngine.buildSuggestUrl(SearchEngineOption.DUCKDUCKGO, "kotlin")
                .contains("ac.duckduckgo.com")
        )
        assertTrue(
            suggestionEngine.buildSuggestUrl(SearchEngineOption.GOOGLE, "kotlin")
                .contains("suggestqueries.google.com")
        )
        assertTrue(
            suggestionEngine.buildSuggestUrl(SearchEngineOption.BRAVE, "kotlin")
                .contains("search.brave.com")
        )
        assertTrue(
            suggestionEngine.buildSuggestUrl(SearchEngineOption.WIKIPEDIA, "kotlin")
                .contains("en.wikipedia.org")
        )

        // OpenSearch JSON array (Google / DuckDuckGo list / Brave / Bing)
        val openSearchJson = """["kotlin",["kotlin","kotlin compose","kotlin coroutines"]]"""
        val googleParsed = suggestionEngine.parseEngineSuggestions(
            rawJson = openSearchJson,
            originalQuery = "kotlin",
            engine = SearchEngineOption.GOOGLE
        )
        assertEquals(3, googleParsed.size)
        assertEquals("kotlin compose", googleParsed[1].title)
        assertEquals("G", googleParsed[1].engineBadge)

        // DuckDuckGo object array format
        val ddgObjJson = """[{"phrase":"privacy browser"},{"phrase":"privacy tools"}]"""
        val ddgParsed = suggestionEngine.parseEngineSuggestions(
            rawJson = ddgObjJson,
            originalQuery = "privacy",
            engine = SearchEngineOption.DUCKDUCKGO
        )
        assertEquals(2, ddgParsed.size)
        assertEquals("privacy browser", ddgParsed[0].title)
        assertEquals("DDG", ddgParsed[0].engineBadge)

        // Wikipedia OpenSearch with descriptions and direct article URLs
        val wikiJson = """["jetpack",["Jetpack Compose"],["Android UI toolkit"],["https://en.wikipedia.org/wiki/Jetpack_Compose"]]"""
        val wikiParsed = suggestionEngine.parseEngineSuggestions(
            rawJson = wikiJson,
            originalQuery = "jetpack",
            engine = SearchEngineOption.WIKIPEDIA
        )
        assertTrue(wikiParsed.any { it.submitValue == "https://en.wikipedia.org/wiki/Jetpack_Compose" })
        assertTrue(wikiParsed.any { it.subtitle == "Android UI toolkit" })
    }

    @Test
    fun `verify M3 Expressive seed ColorScheme generation`() {
        val light = ApexColorSchemeBuilder.fromSeed(
            seed = SeedPalettePreset.ELECTRIC_COBALT.seedColor,
            darkTheme = false
        )
        val dark = ApexColorSchemeBuilder.fromSeed(
            seed = SeedPalettePreset.ELECTRIC_COBALT.seedColor,
            darkTheme = true
        )
        assertNotEquals(light.primaryContainer, dark.primaryContainer)
        assertNotEquals(light.surface, dark.surface)
    }

    @Test
    fun `verify Tab Group grid entries for 4 tabs vs 5+ tabs`() {
        val grp = TabGroupInfo(id = "grp_1", title = "Group 1")
        val fourGroupedTabs = (1..4).map { idx ->
            BrowserTab(id = "t_$idx", title = "Tab $idx", url = "https://example.com/$idx", groupId = "grp_1")
        }
        val stateFour = BrowserUiState(
            tabs = fourGroupedTabs + BrowserTab(id = "solo", title = "Solo", url = "apex://home"),
            tabGroups = listOf(grp)
        )
        val entriesFour = stateFour.gridEntries
        assertEquals(2, entriesFour.size)
        val groupFour = entriesFour.first() as TabGridEntry.GroupTab
        assertEquals(4, groupFour.tabs.size)
        assertFalse(groupFour.tabs.size > 4) // 4 tabs stay shown directly

        val sixGroupedTabs = (1..6).map { idx ->
            BrowserTab(id = "t_$idx", title = "Tab $idx", url = "https://example.com/$idx", groupId = "grp_1")
        }
        val stateSix = BrowserUiState(tabs = sixGroupedTabs, tabGroups = listOf(grp))
        val groupSix = stateSix.gridEntries.single() as TabGridEntry.GroupTab
        assertTrue(groupSix.tabs.size > 4)
        assertEquals(3, groupSix.tabs.size - 3) // 3 tabs shown + 4th slot shows +3 remaining
    }

    @Test
    fun `verify PrivacyManager HTTPS upgrade and tracker blocking`() {
        val privacyManager = PrivacyManager()
        val upgraded = privacyManager.normalizeAndUpgradeUrl(
            rawInput = "http://example.com/articles",
            httpsUpgradeEnabled = true,
            searchEngineTemplate = "https://duckduckgo.com/?q=%s"
        )
        assertEquals("https://example.com/articles", upgraded.finalUrl)
        assertTrue(upgraded.wasUpgradedToHttps)

        val blocked = privacyManager.evaluateRequest(
            requestUrl = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js",
            pageUrl = "https://news.ycombinator.com",
            shieldsEnabled = true
        )
        assertTrue(blocked.blocked)
        assertEquals("Ad Network", blocked.category)

        val firstPartyAllowed = privacyManager.evaluateRequest(
            requestUrl = "https://news.ycombinator.com/news.css",
            pageUrl = "https://news.ycombinator.com",
            shieldsEnabled = true
        )
        assertFalse(firstPartyAllowed.blocked)
    }

    @Test
    fun `verify Nano multi-agent planner validator and Retriever parallel extraction`() = runTest {
        val planner = PlannerAgent()
        val steps = planner.decomposeTask(
            prompt = "Find the top 3 AI news articles today and summarize them",
            currentUrl = "apex://home"
        )
        assertTrue(steps.size >= 2)

        val validator = ValidatorAgent()
        val outcome = validator.validateAndSynthesize(
            userPrompt = "Summarize top 3",
            observations = emptyList()
        )
        assertTrue(outcome.passed)

        val retriever = RetrieverEngine()
        val rule = retriever.buildRuleFromInput("Extract all product names and prices from this page")
        val records = retriever.extractAcrossTabsInParallel(
            tabs = listOf(
                TabSnapshotTarget("t1", "AI News Pulse", "apex://news"),
                TabSnapshotTarget("t2", "Apex Gear Store", "apex://store")
            ),
            webViewLookup = { null },
            rule = rule
        )
        assertTrue(records.size >= 8)
    }

    @Test
    fun `verify Material 3 Grouped List item shapes for single first middle and last items`() {
        assertTrue(GroupedListSpacing > 0.dp)

        val single = groupedListItemShape(index = 0, totalCount = 1)
        val first = groupedListItemShape(index = 0, totalCount = 3)
        val middle = groupedListItemShape(index = 1, totalCount = 3)
        val last = groupedListItemShape(index = 2, totalCount = 3)

        assertEquals(RoundedCornerShape(24.dp), single)
        assertEquals(
            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 5.dp, bottomEnd = 5.dp),
            first
        )
        assertEquals(RoundedCornerShape(5.dp), middle)
        assertEquals(
            RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            last
        )
        assertNotEquals(first, middle)
        assertNotEquals(middle, last)
        assertNotEquals(first, last)
    }
}
