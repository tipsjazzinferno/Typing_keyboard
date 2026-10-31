package com.example.keyboard.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.firebase.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class UserProfile(
    val isLoggedIn: Boolean = false,
    val accountType: String = "guest", // "google" or "guest"
    val displayName: String = "Guest User",
    val email: String = "guest@minimakey.local",
    val avatarColor: Long = 0xFF06B6D4
)

data class GiftCardItem(
    val id: String,
    val name: String,
    val brand: String,
    val valueStr: String,
    val costCoins: Int,
    val iconEmoji: String,
    val brandColorHex: Long = 0xFF00C853
)

data class RedeemedCard(
    val id: String,
    val cardName: String,
    val code: String,
    val dateStr: String,
    val brandEmoji: String
)

class RewardsManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minima_rewards_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val userRepository by lazy { UserRepository(context.applicationContext) }

    private val _coins = MutableStateFlow(prefs.getInt(KEY_COINS, 150)) // 150 welcome bonus
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _redeemedCards = MutableStateFlow(loadRedeemedCards())
    val redeemedCards: StateFlow<List<RedeemedCard>> = _redeemedCards.asStateFlow()

    private val _totalWordsTyped = MutableStateFlow(prefs.getInt(KEY_WORDS_TYPED, 0))
    val totalWordsTyped: StateFlow<Int> = _totalWordsTyped.asStateFlow()

    init {
        // Sync with Firebase Auth state on initialization if Firebase is initialized
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                val authUser = Firebase.auth.currentUser
                if (authUser != null) {
                    val p = UserProfile(
                        isLoggedIn = true,
                        accountType = "google",
                        displayName = authUser.displayName ?: "Google Typist",
                        email = authUser.email ?: "typist@gmail.com",
                        avatarColor = 0xFF3B82F6
                    )
                    saveProfile(p)
                    listenToCloudData()
                }
            }
        } catch (e: Exception) {
            // Headless unit tests without FirebaseApp
        }
    }

    private fun listenToCloudData() {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty() || Firebase.auth.currentUser == null) return
            scope.launch {
                try {
                    userRepository.observeRedemptions()
                        .catch { /* error logged by repository */ }
                        .collect { cloudList ->
                            if (cloudList.isNotEmpty()) {
                                val mapped = cloudList.map { doc ->
                                    RedeemedCard(
                                        id = doc.id,
                                        cardName = doc.cardTitle,
                                        code = doc.code,
                                        dateStr = doc.createdAt?.toDate()?.let {
                                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                                        } ?: "Recent",
                                        brandEmoji = resolveEmojiForBrand(doc.cardTitle)
                                    )
                                }
                                _redeemedCards.value = mapped
                                saveRedeemedCards(mapped)
                            }
                        }
                } catch (e: Exception) {
                    // Ignore background listen cancellations
                }
            }
        } catch (e: Exception) {
            // No-op
        }
    }

    private fun resolveEmojiForBrand(name: String): String {
        return when {
            name.contains("Google", ignoreCase = true) -> "🎮"
            name.contains("Amazon", ignoreCase = true) -> "📦"
            name.contains("Steam", ignoreCase = true) -> "🕹️"
            name.contains("Spotify", ignoreCase = true) -> "🎵"
            name.contains("Discord", ignoreCase = true) -> "💬"
            else -> "🎁"
        }
    }

    fun loginWithGoogle(name: String, email: String) {
        val newProfile = UserProfile(
            isLoggedIn = true,
            accountType = "google",
            displayName = name,
            email = email,
            avatarColor = 0xFF3B82F6
        )
        saveProfile(newProfile)
        addCoins(100) // 100 bonus coins for linking Google account!

        scope.launch {
            userRepository.saveUserProfile(
                displayName = name,
                email = email,
                coinBalance = _coins.value.toLong()
            )
            listenToCloudData()
        }
    }

    fun logout() {
        val guest = UserProfile()
        saveProfile(guest)
    }

    fun addCoins(amount: Int) {
        val newBalance = _coins.value + amount
        prefs.edit().putInt(KEY_COINS, newBalance).apply()
        _coins.value = newBalance

        if (Firebase.auth.currentUser != null) {
            scope.launch {
                userRepository.updateCoinBalance(newBalance.toLong())
            }
        }
    }

    fun onWordsTyped(count: Int) {
        val newTotal = _totalWordsTyped.value + count
        _totalWordsTyped.value = newTotal
        prefs.edit().putInt(KEY_WORDS_TYPED, newTotal).apply()

        // Award +5 coins for every 25 words typed!
        if (newTotal > 0 && newTotal % 25 == 0) {
            addCoins(5)
        }
    }

    fun claimDailyBonus(): Boolean {
        val lastClaimDay = prefs.getInt(KEY_LAST_DAILY_CLAIM, -1)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (lastClaimDay != today) {
            prefs.edit().putInt(KEY_LAST_DAILY_CLAIM, today).apply()
            addCoins(75)
            return true
        }
        return false
    }

    fun redeemGiftCard(card: GiftCardItem): RedeemedCard? {
        if (_coins.value < card.costCoins) return null

        val newBalance = _coins.value - card.costCoins
        prefs.edit().putInt(KEY_COINS, newBalance).apply()
        _coins.value = newBalance

        // Generate authentic-looking voucher code
        val generatedCode = generateCode(card.brand)
        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val cardTitle = "${card.brand} ${card.valueStr}"
        val redeemed = RedeemedCard(
            id = UUID.randomUUID().toString(),
            cardName = cardTitle,
            code = generatedCode,
            dateStr = date,
            brandEmoji = card.iconEmoji
        )

        val updated = listOf(redeemed) + _redeemedCards.value
        _redeemedCards.value = updated
        saveRedeemedCards(updated)

        if (Firebase.auth.currentUser != null) {
            scope.launch {
                userRepository.addRedemption(
                    cardTitle = cardTitle,
                    cardValue = card.valueStr,
                    code = generatedCode,
                    cost = card.costCoins.toLong()
                )
                userRepository.updateCoinBalance(newBalance.toLong())
            }
        }

        return redeemed
    }

    private fun generateCode(brand: String): String {
        val prefix = when (brand.lowercase()) {
            "google play" -> "GP"
            "amazon" -> "AMZN"
            "steam" -> "STM"
            "spotify" -> "SPOT"
            "discord" -> "DISC"
            else -> "CARD"
        }
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        fun part() = (1..4).map { chars.random() }.joinToString("")
        return "$prefix-${part()}-${part()}-${part()}"
    }

    private fun loadProfile(): UserProfile {
        val loggedIn = prefs.getBoolean(KEY_LOGGED_IN, false)
        val type = prefs.getString(KEY_ACCT_TYPE, "guest") ?: "guest"
        val name = prefs.getString(KEY_NAME, if (loggedIn) "Sniper Gamer" else "Guest Typist") ?: "Guest Typist"
        val email = prefs.getString(KEY_EMAIL, if (loggedIn) "snipergamerpk@gmail.com" else "guest@minima.app") ?: "guest@minima.app"
        return UserProfile(loggedIn, type, name, email)
    }

    private fun saveProfile(p: UserProfile) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, p.isLoggedIn)
            .putString(KEY_ACCT_TYPE, p.accountType)
            .putString(KEY_NAME, p.displayName)
            .putString(KEY_EMAIL, p.email)
            .apply()
        _profile.value = p
    }

    private fun loadRedeemedCards(): List<RedeemedCard> {
        val json = prefs.getString(KEY_REDEEMED_JSON, null) ?: return emptyList()
        val list = mutableListOf<RedeemedCard>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RedeemedCard(
                        id = obj.getString("id"),
                        cardName = obj.getString("name"),
                        code = obj.getString("code"),
                        dateStr = obj.getString("date"),
                        brandEmoji = obj.getString("emoji")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveRedeemedCards(cards: List<RedeemedCard>) {
        try {
            val array = JSONArray()
            for (c in cards) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("name", c.cardName)
                obj.put("code", c.code)
                obj.put("date", c.dateStr)
                obj.put("emoji", c.brandEmoji)
                array.put(obj)
            }
            prefs.edit().putString(KEY_REDEEMED_JSON, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_COINS = "user_coins"
        private const val KEY_LOGGED_IN = "user_logged_in"
        private const val KEY_ACCT_TYPE = "user_acct_type"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_WORDS_TYPED = "user_words_typed"
        private const val KEY_LAST_DAILY_CLAIM = "user_last_daily"
        private const val KEY_REDEEMED_JSON = "user_redeemed_json"

        val Catalog = listOf(
            GiftCardItem("gp_5", "$5 Code", "Google Play", "$5.00", 300, "🎮", 0xFF0F9D58),
            GiftCardItem("gp_10", "$10 Code", "Google Play", "$10.00", 600, "🎮", 0xFF0F9D58),
            GiftCardItem("amz_5", "$5 Balance", "Amazon", "$5.00", 300, "📦", 0xFFFF9900),
            GiftCardItem("amz_10", "$10 Balance", "Amazon", "$10.00", 600, "📦", 0xFFFF9900),
            GiftCardItem("stm_5", "$5 Wallet", "Steam", "$5.00", 350, "🕹️", 0xFF171A21),
            GiftCardItem("spt_1m", "1 Month Pass", "Spotify", "Premium", 400, "🎵", 0xFF1DB954),
            GiftCardItem("dsc_1m", "1 Month Nitro", "Discord", "Nitro", 450, "💬", 0xFF5865F2)
        )

        @Volatile
        private var instance: RewardsManager? = null

        fun getInstance(context: Context): RewardsManager {
            return instance ?: synchronized(this) {
                instance ?: RewardsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
