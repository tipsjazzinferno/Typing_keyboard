package com.example

import com.example.keyboard.engine.KeyboardLanguage
import com.example.keyboard.engine.PredictionEngine
import com.example.keyboard.engine.RewardsManager
import com.example.keyboard.engine.TrieDictionary
import com.example.keyboard.model.KeyboardTheme
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testTrieDictionarySuggestions() {
        val dict = TrieDictionary()
        val suggestions = dict.getSuggestions("th")
        assertTrue("Suggestions for 'th' should not be empty", suggestions.isNotEmpty())
        assertTrue("Suggestions should contain 'the'", suggestions.contains("the"))
    }

    @Test
    fun testHowSuggestionAndPrefixReplacement() {
        val dict = TrieDictionary()
        val suggestions = dict.getSuggestions("ho")
        assertTrue("Suggestions for 'ho' should contain 'how'", suggestions.contains("how"))

        val currentTyped = "ho"
        val chosenSuggestion = suggestions.first { it == "how" }
        val prefixLen = currentTyped.length
        val afterDeletion = currentTyped.dropLast(prefixLen)
        val finalResult = afterDeletion + chosenSuggestion + " "
        assertEquals("how ", finalResult)
        assertNotEquals("hohow ", finalResult)
    }

    @Test
    fun testMultiLanguageSwitching() {
        val dict = TrieDictionary()
        dict.switchLanguage("es")
        val spanishSuggestions = dict.getSuggestions("ho")
        assertTrue("Spanish suggestions for 'ho' should contain 'hola'", spanishSuggestions.contains("hola"))
    }

    @Test
    fun testSystemAutoThemeResolution() {
        val darkResolved = KeyboardTheme.resolveForSystem("system_auto", isSystemDark = true)
        assertTrue(darkResolved.isDark)

        val lightResolved = KeyboardTheme.resolveForSystem("system_auto", isSystemDark = false)
        assertFalse(lightResolved.isDark)
    }

    @Test
    fun testPredictiveEmojiLookup() {
        val fireEmojis = TrieDictionary.EmojiPredictor["fire"]
        assertNotNull(fireEmojis)
        assertTrue("Predictive emojis for fire should contain 🔥", fireEmojis!!.contains("🔥"))
    }

    @Test
    fun testGiftCardCatalog() {
        assertTrue("Rewards catalog should have cards available", RewardsManager.Catalog.isNotEmpty())
        val gpCard = RewardsManager.Catalog.firstOrNull { it.brand == "Google Play" }
        assertNotNull(gpCard)
    }
}
