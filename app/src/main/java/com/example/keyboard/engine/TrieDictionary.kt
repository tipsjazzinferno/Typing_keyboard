package com.example.keyboard.engine

import kotlin.math.min

class TrieNode {
    val children = HashMap<Char, TrieNode>(4)
    var isWord: Boolean = false
    var frequency: Int = 0
}

data class KeyboardLanguage(
    val code: String,
    val name: String,
    val spaceLabel: String
) {
    companion object {
        val English = KeyboardLanguage("en", "English", "English (US)")
        val Spanish = KeyboardLanguage("es", "Español", "Español")
        val French = KeyboardLanguage("fr", "Français", "Français")
        val German = KeyboardLanguage("de", "Deutsch", "Deutsch")
        val Portuguese = KeyboardLanguage("pt", "Português", "Português")

        val AllLanguages = listOf(English, Spanish, French, German, Portuguese)

        fun find(code: String): KeyboardLanguage {
            return AllLanguages.firstOrNull { it.code == code } ?: English
        }
    }
}

class TrieDictionary(var currentLanguageCode: String = "en") {
    private var root = TrieNode()
    private val userWords = mutableMapOf<String, Int>()

    init {
        loadDictionaryForLanguage(currentLanguageCode)
    }

    fun switchLanguage(languageCode: String) {
        if (currentLanguageCode != languageCode) {
            currentLanguageCode = languageCode
            root = TrieNode()
            loadDictionaryForLanguage(languageCode)
            // Re-insert user words
            userWords.forEach { (word, freq) -> insert(word, freq) }
        }
    }

    fun insert(word: String, frequency: Int = 1) {
        if (word.isBlank()) return
        val clean = word.lowercase()
        var current = root
        for (ch in clean) {
            current = current.children.getOrPut(ch) { TrieNode() }
        }
        current.isWord = true
        current.frequency = maxOf(current.frequency, frequency)
    }

    fun learnUserWord(word: String) {
        if (word.length < 2 || word.contains(' ')) return
        val clean = word.lowercase()
        val count = (userWords[clean] ?: 0) + 5
        userWords[clean] = count
        insert(clean, 100 + count)
    }

    fun clearUserLearnedWords() {
        userWords.clear()
        root = TrieNode()
        loadDictionaryForLanguage(currentLanguageCode)
    }

    fun getSuggestions(prefix: String, maxCount: Int = 4): List<String> {
        if (prefix.isBlank()) return emptyList()
        val clean = prefix.lowercase()
        var current = root
        for (ch in clean) {
            current = current.children[ch] ?: return getFuzzyCorrections(clean, maxCount)
        }

        val results = mutableListOf<Pair<String, Int>>()
        collectWords(current, StringBuilder(clean), results, maxCount * 2)

        val sorted = results.sortedByDescending { it.second }.map { it.first }
        if (sorted.isEmpty()) {
            return getFuzzyCorrections(clean, maxCount)
        }
        return sorted.take(maxCount)
    }

    private fun collectWords(
        node: TrieNode,
        currentWord: StringBuilder,
        results: MutableList<Pair<String, Int>>,
        maxLimit: Int
    ) {
        if (results.size >= maxLimit * 3) return
        if (node.isWord) {
            results.add(Pair(currentWord.toString(), node.frequency))
        }
        for ((char, child) in node.children) {
            currentWord.append(char)
            collectWords(child, currentWord, results, maxLimit)
            currentWord.deleteCharAt(currentWord.length - 1)
        }
    }

    fun getFuzzyCorrections(word: String, maxCount: Int = 3): List<String> {
        if (word.length < 3) return emptyList()
        val results = mutableListOf<Pair<String, Int>>()

        val directTypo = CommonTypos[word]
        if (directTypo != null) {
            results.add(Pair(directTypo, 999))
        }

        searchFuzzy(root, word, "", 0, results, maxDistance = 1)
        return results.sortedByDescending { it.second }.map { it.first }.distinct().take(maxCount)
    }

    private fun searchFuzzy(
        node: TrieNode,
        target: String,
        currentStr: String,
        depth: Int,
        results: MutableList<Pair<String, Int>>,
        maxDistance: Int
    ) {
        if (results.size >= 8) return
        if (node.isWord && currentStr.length in (target.length - 1)..(target.length + 1)) {
            if (levenshteinDistance(target, currentStr) <= maxDistance) {
                results.add(Pair(currentStr, node.frequency))
            }
        }
        if (depth > target.length + 1) return

        for ((ch, child) in node.children) {
            searchFuzzy(child, target, currentStr + ch, depth + 1, results, maxDistance)
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..s2.length) {
                val temp = dp[j]
                dp[j] = if (s1[i - 1] == s2[j - 1]) prev else 1 + min(prev, min(dp[j], dp[j - 1]))
                prev = temp
            }
        }
        return dp[s2.length]
    }

    private fun loadDictionaryForLanguage(lang: String) {
        val words = when (lang) {
            "es" -> SpanishWords
            "fr" -> FrenchWords
            "de" -> GermanWords
            "pt" -> PortugueseWords
            else -> EnglishWords
        }
        for ((word, freq) in words) {
            insert(word, freq)
        }
    }

    companion object {
        val EnglishWords = listOf(
            "how" to 615, "the" to 1000, "be" to 990, "to" to 980, "of" to 970, "and" to 960, "a" to 950,
            "in" to 940, "that" to 930, "have" to 920, "i" to 910, "it" to 900, "for" to 890,
            "not" to 880, "on" to 870, "with" to 860, "he" to 850, "as" to 840, "you" to 830,
            "do" to 820, "at" to 810, "this" to 800, "but" to 790, "his" to 780, "by" to 770,
            "from" to 760, "they" to 750, "we" to 740, "say" to 730, "her" to 720, "she" to 710,
            "or" to 700, "an" to 690, "will" to 680, "my" to 670, "one" to 660, "all" to 650,
            "would" to 640, "there" to 630, "their" to 620, "what" to 610, "so" to 600,
            "up" to 590, "out" to 580, "if" to 570, "about" to 560, "who" to 550, "get" to 540,
            "which" to 530, "go" to 520, "me" to 510, "when" to 500, "make" to 490, "can" to 480,
            "like" to 470, "time" to 460, "no" to 450, "just" to 440, "him" to 430, "know" to 420,
            "take" to 410, "people" to 400, "into" to 390, "year" to 380, "your" to 370, "good" to 360,
            "some" to 350, "could" to 340, "them" to 330, "see" to 320, "other" to 310, "than" to 300,
            "then" to 290, "now" to 280, "look" to 270, "only" to 260, "come" to 250, "its" to 240,
            "over" to 230, "think" to 220, "also" to 210, "back" to 200, "after" to 190, "use" to 180,
            "hello" to 350, "thanks" to 340, "thank" to 330, "please" to 320, "welcome" to 310,
            "yes" to 300, "okay" to 290, "awesome" to 280, "today" to 270, "tomorrow" to 260,
            "love" to 250, "happy" to 240, "coffee" to 230, "music" to 220, "home" to 320,
            "hope" to 310, "hold" to 300, "hour" to 290, "house" to 280, "hotel" to 270
        )

        val SpanishWords = listOf(
            "como" to 700, "hola" to 800, "que" to 950, "para" to 900, "por" to 850, "con" to 800,
            "gracias" to 750, "bueno" to 700, "amigo" to 650, "tiempo" to 600, "siempre" to 550,
            "vida" to 500, "casa" to 450, "dia" to 400, "noche" to 350, "bien" to 750, "si" to 800,
            "no" to 850, "donde" to 600, "cuando" to 600, "quien" to 550, "hacer" to 650, "todo" to 700
        )

        val FrenchWords = listOf(
            "bonjour" to 800, "merci" to 750, "comment" to 700, "avec" to 850, "pour" to 900,
            "vous" to 800, "nous" to 750, "bien" to 700, "temps" to 600, "toujours" to 550,
            "jour" to 500, "nuit" to 450, "oui" to 800, "non" to 850, "pourquoi" to 600, "salut" to 750
        )

        val GermanWords = listOf(
            "hallo" to 800, "danke" to 750, "bitte" to 700, "wie" to 750, "was" to 800, "gut" to 700,
            "immer" to 600, "tag" to 550, "nacht" to 500, "zeit" to 650, "leben" to 500, "ja" to 800,
            "nein" to 850, "warum" to 600, "alles" to 700, "freund" to 550, "morgen" to 600
        )

        val PortugueseWords = listOf(
            "ola" to 800, "como" to 750, "obrigado" to 750, "bom" to 700, "tempo" to 600,
            "voce" to 800, "mais" to 750, "para" to 900, "dia" to 500, "noite" to 450, "sim" to 800,
            "nao" to 850, "tudo" to 700, "amigo" to 600, "vida" to 550, "casa" to 500
        )

        val CommonTypos = mapOf(
            "teh" to "the", "hte" to "the", "thansk" to "thanks", "thx" to "thanks",
            "becuase" to "because", "beacuse" to "because", "woudl" to "would",
            "coudl" to "could", "peopel" to "people", "recieved" to "received",
            "helo" to "hello", "helllo" to "hello", "pls" to "please", "plz" to "please"
        )

        val EmojiPredictor = mapOf(
            // Greetings & Social
            "hi" to listOf("👋", "😊", "✨"),
            "hey" to listOf("👋", "😄", "✨"),
            "hello" to listOf("👋", "😊", "🌟"),
            "thanks" to listOf("🙏", "💖", "😊"),
            "thank" to listOf("🙏", "🥰", "✨"),
            "welcome" to listOf("🤝", "🤗", "✨"),
            "please" to listOf("🥺", "🙏", "✨"),
            "bye" to listOf("👋", "🥺", "💨"),
            "goodbye" to listOf("👋", "🕊️", "❤️"),

            // Question words
            "how" to listOf("🤔", "❓", "💭"),
            "what" to listOf("❓", "🧐", "🤔"),
            "why" to listOf("🤔", "💭", "❓"),
            "where" to listOf("📍", "🗺️", "🔍"),
            "when" to listOf("⏰", "📅", "⏳"),
            "who" to listOf("👤", "👀", "🕵️"),

            // Moods & Feelings
            "happy" to listOf("😊", "😄", "🎉", "✨"),
            "love" to listOf("❤️", "🥰", "💖", "😍"),
            "heart" to listOf("❤️", "💓", "💘", "💕"),
            "fire" to listOf("🔥", "💥", "⚡", "🌋"),
            "cool" to listOf("😎", "🤙", "🕶️", "🧊"),
            "party" to listOf("🎉", "🥳", "🍾", "💃"),
            "sad" to listOf("😢", "😭", "💔", "🥺"),
            "cry" to listOf("😭", "🥺", "💧"),
            "lol" to listOf("😂", "🤣", "💀"),
            "laugh" to listOf("😆", "😹", "🤣"),
            "angry" to listOf("😡", "🤬", "😤"),
            "tired" to listOf("🥱", "😫", "💤"),
            "sleep" to listOf("😴", "💤", "🛏️"),
            "kiss" to listOf("😘", "💋", "😽"),
            "good" to listOf("👍", "🌟", "✨"),
            "great" to listOf("🌟", "👍", "💯"),
            "awesome" to listOf("🔥", "🤩", "🎉"),
            "perfect" to listOf("👌", "💯", "✨"),
            "win" to listOf("🏆", "🥇", "🎉"),
            "congrats" to listOf("🎉", "🍾", "👏"),

            // Food & Drinks
            "coffee" to listOf("☕", "🍵", "🥐"),
            "tea" to listOf("🍵", "🫖", "🧋"),
            "pizza" to listOf("🍕", "🧀", "🍅"),
            "burger" to listOf("🍔", "🍟", "🥤"),
            "beer" to listOf("🍺", "🍻", "🥂"),
            "wine" to listOf("🍷", "🥂", "🍇"),
            "cake" to listOf("🎂", "🍰", "🧁"),
            "food" to listOf("🍔", "🍕", "🍜"),

            // Animals
            "cat" to listOf("🐱", "🐈", "🐾"),
            "dog" to listOf("🐶", "🐕", "🦴"),
            "bear" to listOf("🐻", "ʕ•ᴥ•ʔ", "🐾"),
            "lion" to listOf("🦁", "👑", "🐾"),

            // Tech & Rewards
            "code" to listOf("💻", "⌨️", "🚀"),
            "gift" to listOf("🎁", "🎀", "🎉"),
            "reward" to listOf("🎁", "🪙", "🏆"),
            "money" to listOf("💰", "💵", "🤑"),
            "cash" to listOf("💵", "💰", "💳"),
            "star" to listOf("⭐", "🌟", "✨"),
            "sun" to listOf("☀️", "🌅", "🌞"),
            "moon" to listOf("🌙", "🌕", "✨"),
            "music" to listOf("🎵", "🎶", "🎧"),
            "car" to listOf("🚗", "🏎️", "🚙"),
            "game" to listOf("🎮", "🕹️", "👾")
        )
    }
}
