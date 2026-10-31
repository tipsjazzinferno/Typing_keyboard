package com.example.keyboard.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PredictionResult(
    val currentWord: String = "",
    val suggestions: List<String> = emptyList(),
    val emojis: List<String> = emptyList(),
    val isAutoCorrectCandidate: Boolean = false
)

class PredictionEngine(
    private val dictionary: TrieDictionary = TrieDictionary()
) {
    private val _predictionState = MutableStateFlow(PredictionResult())
    val predictionState: StateFlow<PredictionResult> = _predictionState.asStateFlow()

    private var isIncognito: Boolean = false

    fun setIncognito(enabled: Boolean) {
        isIncognito = enabled
    }

    fun switchLanguage(languageCode: String) {
        dictionary.switchLanguage(languageCode)
    }

    fun onTextContextChanged(textBeforeCursor: CharSequence?) {
        if (textBeforeCursor == null || textBeforeCursor.isEmpty()) {
            _predictionState.value = PredictionResult()
            return
        }

        // Check if cursor is right after whitespace
        var i = textBeforeCursor.length - 1
        while (i >= 0 && Character.isWhitespace(textBeforeCursor[i])) {
            i--
        }

        // If after whitespace, suggest next words and emojis for previous word
        if (i < textBeforeCursor.length - 1) {
            val endIdx = i + 1
            while (i >= 0 && !Character.isWhitespace(textBeforeCursor[i])) {
                i--
            }
            val lastWord = textBeforeCursor.subSequence(i + 1, endIdx).toString().trim().lowercase()
            val emojis = TrieDictionary.EmojiPredictor[lastWord] ?: emptyList()
            _predictionState.value = PredictionResult(
                currentWord = "",
                suggestions = listOf("and", "the", "to"),
                emojis = emojis
            )
            return
        }

        // Extract currently typing word token
        val end = textBeforeCursor.length
        while (i >= 0 && !Character.isWhitespace(textBeforeCursor[i])) {
            i--
        }
        val currentWord = textBeforeCursor.subSequence(i + 1, end).toString().trim()
        if (currentWord.isEmpty()) {
            _predictionState.value = PredictionResult()
            return
        }

        val cleanLower = currentWord.lowercase()
        val isFirstUpper = currentWord[0].isUpperCase()

        val rawSuggestions = dictionary.getSuggestions(cleanLower, maxCount = 4).toMutableList()
        val emojis = TrieDictionary.EmojiPredictor[cleanLower] ?: emptyList()

        val isAutoCorrect = rawSuggestions.isNotEmpty() && !rawSuggestions.contains(cleanLower)

        // Preserve case
        val formattedSuggestions = rawSuggestions.map { word ->
            if (isFirstUpper && word.isNotEmpty()) {
                word.replaceFirstChar { it.uppercase() }
            } else {
                word
            }
        }.toMutableList()

        if (formattedSuggestions.isEmpty()) {
            formattedSuggestions.add(currentWord)
        } else if (!rawSuggestions.contains(cleanLower)) {
            // Keep user's verbatim input available at the end
            formattedSuggestions.add(currentWord)
        }

        _predictionState.value = PredictionResult(
            currentWord = currentWord,
            suggestions = formattedSuggestions.distinct().take(3),
            emojis = emojis,
            isAutoCorrectCandidate = isAutoCorrect
        )
    }

    fun onWordCommitted(word: String) {
        if (!isIncognito && word.isNotBlank() && word.length > 2) {
            dictionary.learnUserWord(word)
        }
        _predictionState.value = PredictionResult()
    }

    fun clearHistory() {
        dictionary.clearUserLearnedWords()
        _predictionState.value = PredictionResult()
    }
}
