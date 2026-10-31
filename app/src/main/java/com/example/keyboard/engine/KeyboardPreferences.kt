package com.example.keyboard.engine

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.keyboard.model.ExtensionItem
import com.example.keyboard.model.ExtensionPack
import com.example.keyboard.model.KeyboardTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class KeyboardPreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minima_keyboard_prefs", Context.MODE_PRIVATE)

    private val _themeId = MutableStateFlow(prefs.getString(KEY_THEME_ID, KeyboardTheme.SystemAuto.id) ?: KeyboardTheme.SystemAuto.id)
    val themeId: StateFlow<String> = _themeId.asStateFlow()

    private val _languageCode = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    private val _wallpaperType = MutableStateFlow(prefs.getString(KEY_WALLPAPER_TYPE, "none") ?: "none")
    val wallpaperType: StateFlow<String> = _wallpaperType.asStateFlow()

    private val _customWallpaperPath = MutableStateFlow(prefs.getString(KEY_CUSTOM_WALLPAPER_PATH, null))
    val customWallpaperPath: StateFlow<String?> = _customWallpaperPath.asStateFlow()

    private val _wallpaperDim = MutableStateFlow(prefs.getFloat(KEY_WALLPAPER_DIM, 0.45f))
    val wallpaperDim: StateFlow<Float> = _wallpaperDim.asStateFlow()

    private val _keyOpacity = MutableStateFlow(prefs.getFloat(KEY_KEY_OPACITY, 0.85f))
    val keyOpacity: StateFlow<Float> = _keyOpacity.asStateFlow()

    private val _lowEndOptimization = MutableStateFlow(prefs.getBoolean(KEY_LOW_END_OPT, false))
    val lowEndOptimization: StateFlow<Boolean> = _lowEndOptimization.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _soundFeedback = MutableStateFlow(prefs.getBoolean(KEY_SOUND, false))
    val soundFeedback: StateFlow<Boolean> = _soundFeedback.asStateFlow()

    private val _swipeLeftDelete = MutableStateFlow(prefs.getBoolean(KEY_SWIPE_DELETE, true))
    val swipeLeftDelete: StateFlow<Boolean> = _swipeLeftDelete.asStateFlow()

    private val _spaceCursorGlide = MutableStateFlow(prefs.getBoolean(KEY_SPACE_GLIDE, true))
    val spaceCursorGlide: StateFlow<Boolean> = _spaceCursorGlide.asStateFlow()

    private val _swipeUpSymbols = MutableStateFlow(prefs.getBoolean(KEY_SWIPE_UP_SYMBOLS, true))
    val swipeUpSymbols: StateFlow<Boolean> = _swipeUpSymbols.asStateFlow()

    private val _showSymbolHints = MutableStateFlow(prefs.getBoolean(KEY_SYMBOL_HINTS, true))
    val showSymbolHints: StateFlow<Boolean> = _showSymbolHints.asStateFlow()

    private val _autoCapitalization = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CAPS, true))
    val autoCapitalization: StateFlow<Boolean> = _autoCapitalization.asStateFlow()

    private val _autoCorrect = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CORRECT, true))
    val autoCorrect: StateFlow<Boolean> = _autoCorrect.asStateFlow()

    private val _predictiveEmoji = MutableStateFlow(prefs.getBoolean(KEY_PREDICTIVE_EMOJI, true))
    val predictiveEmoji: StateFlow<Boolean> = _predictiveEmoji.asStateFlow()

    private val _incognitoMode = MutableStateFlow(prefs.getBoolean(KEY_INCOGNITO, false))
    val incognitoMode: StateFlow<Boolean> = _incognitoMode.asStateFlow()

    private val _keyHeightScale = MutableStateFlow(prefs.getFloat(KEY_HEIGHT_SCALE, 1.0f))
    val keyHeightScale: StateFlow<Float> = _keyHeightScale.asStateFlow()

    private val _installedPacks = MutableStateFlow(loadExtensionPacks())
    val installedPacks: StateFlow<List<ExtensionPack>> = _installedPacks.asStateFlow()

    fun setTheme(id: String) {
        prefs.edit().putString(KEY_THEME_ID, id).apply()
        _themeId.value = id
    }

    fun setLanguage(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
        _languageCode.value = code
    }

    fun cycleNextLanguage(): KeyboardLanguage {
        val current = _languageCode.value
        val list = KeyboardLanguage.AllLanguages
        val currentIndex = list.indexOfFirst { it.code == current }
        val next = if (currentIndex != -1) list[(currentIndex + 1) % list.size] else list[0]
        setLanguage(next.code)
        return next
    }

    fun setWallpaperType(type: String) {
        prefs.edit().putString(KEY_WALLPAPER_TYPE, type).apply()
        _wallpaperType.value = type
    }

    fun setWallpaperDim(dim: Float) {
        prefs.edit().putFloat(KEY_WALLPAPER_DIM, dim).apply()
        _wallpaperDim.value = dim
    }

    fun setKeyOpacity(opacity: Float) {
        prefs.edit().putFloat(KEY_KEY_OPACITY, opacity).apply()
        _keyOpacity.value = opacity
    }

    fun setLowEndOptimization(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_END_OPT, enabled).apply()
        _lowEndOptimization.value = enabled
    }

    /**
     * Downsample and save user photo safely into app storage so low-end devices never OOM
     */
    fun saveCustomWallpaperFromUri(uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Downsample target max 1080x1080 to conserve low memory
            var inSampleSize = 1
            val maxDimension = 1080
            while (options.outWidth / (inSampleSize * 2) >= maxDimension &&
                options.outHeight / (inSampleSize * 2) >= maxDimension
            ) {
                inSampleSize *= 2
            }

            val decodeStream = context.contentResolver.openInputStream(uri) ?: return false
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                this.inPreferredConfig = Bitmap.Config.RGB_565 // 50% less RAM than ARGB_8888!
            }
            val bitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream.close()

            if (bitmap != null) {
                val file = File(context.filesDir, "custom_keyboard_wallpaper.jpg")
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                fos.flush()
                fos.close()
                bitmap.recycle()

                prefs.edit().putString(KEY_CUSTOM_WALLPAPER_PATH, file.absolutePath).apply()
                _customWallpaperPath.value = file.absolutePath
                setWallpaperType("custom")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setSoundFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundFeedback.value = enabled
    }

    fun setSwipeLeftDelete(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SWIPE_DELETE, enabled).apply()
        _swipeLeftDelete.value = enabled
    }

    fun setSpaceCursorGlide(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPACE_GLIDE, enabled).apply()
        _spaceCursorGlide.value = enabled
    }

    fun setSwipeUpSymbols(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SWIPE_UP_SYMBOLS, enabled).apply()
        _swipeUpSymbols.value = enabled
    }

    fun setShowSymbolHints(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYMBOL_HINTS, enabled).apply()
        _showSymbolHints.value = enabled
    }

    fun setAutoCapitalization(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CAPS, enabled).apply()
        _autoCapitalization.value = enabled
    }

    fun setAutoCorrect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CORRECT, enabled).apply()
        _autoCorrect.value = enabled
    }

    fun setPredictiveEmoji(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PREDICTIVE_EMOJI, enabled).apply()
        _predictiveEmoji.value = enabled
    }

    fun setIncognitoMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_INCOGNITO, enabled).apply()
        _incognitoMode.value = enabled
    }

    fun setKeyHeightScale(scale: Float) {
        prefs.edit().putFloat(KEY_HEIGHT_SCALE, scale).apply()
        _keyHeightScale.value = scale
    }

    fun togglePackEnabled(packId: String) {
        val current = _installedPacks.value.toMutableList()
        val index = current.indexOfFirst { it.id == packId }
        if (index != -1) {
            val pack = current[index]
            current[index] = pack.copy(isEnabled = !pack.isEnabled)
            _installedPacks.value = current
            saveCustomPacks(current)
        }
    }

    fun installPack(pack: ExtensionPack) {
        val current = _installedPacks.value.toMutableList()
        if (current.none { it.id == pack.id }) {
            current.add(pack.copy(isInstalled = true, isEnabled = true))
            _installedPacks.value = current
            saveCustomPacks(current)
        }
    }

    fun uninstallPack(packId: String) {
        val current = _installedPacks.value.toMutableList()
        current.removeAll { it.id == packId && !it.isBuiltIn }
        _installedPacks.value = current
        saveCustomPacks(current)
    }

    fun addCustomSnippet(packId: String, item: ExtensionItem) {
        val current = _installedPacks.value.toMutableList()
        val index = current.indexOfFirst { it.id == packId }
        if (index != -1) {
            val pack = current[index]
            val newItems = pack.items + item
            current[index] = pack.copy(items = newItems)
            _installedPacks.value = current
            saveCustomPacks(current)
        }
    }

    private fun loadExtensionPacks(): List<ExtensionPack> {
        val list = ExtensionPack.BuiltInPacks.toMutableList()
        val customJson = prefs.getString(KEY_CUSTOM_PACKS, null)
        if (!customJson.isNullOrBlank()) {
            try {
                val array = JSONArray(customJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val name = obj.getString("name")
                    val desc = obj.optString("desc", "")
                    val author = obj.optString("author", "User")
                    val icon = obj.optString("icon", "⚡")
                    val isEnabled = obj.optBoolean("isEnabled", true)
                    val itemsArray = obj.getJSONArray("items")
                    val items = mutableListOf<ExtensionItem>()
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        items.add(
                            ExtensionItem(
                                label = itemObj.getString("label"),
                                preview = itemObj.optString("preview", itemObj.getString("label")),
                                textToInsert = itemObj.getString("text"),
                                category = itemObj.optString("category", "General")
                            )
                        )
                    }
                    val pack = ExtensionPack(
                        id = id,
                        name = name,
                        description = desc,
                        author = author,
                        icon = icon,
                        isInstalled = true,
                        isEnabled = isEnabled,
                        isBuiltIn = false,
                        items = items
                    )
                    list.add(pack)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    private fun saveCustomPacks(packs: List<ExtensionPack>) {
        val customPacks = packs.filter { !it.isBuiltIn }
        try {
            val array = JSONArray()
            for (p in customPacks) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("desc", p.description)
                obj.put("author", p.author)
                obj.put("icon", p.icon)
                obj.put("isEnabled", p.isEnabled)
                val itemsArr = JSONArray()
                for (item in p.items) {
                    val itemObj = JSONObject()
                    itemObj.put("label", item.label)
                    itemObj.put("preview", item.preview)
                    itemObj.put("text", item.textToInsert)
                    itemObj.put("category", item.category)
                    itemsArr.put(itemObj)
                }
                obj.put("items", itemsArr)
                array.put(obj)
            }
            prefs.edit().putString(KEY_CUSTOM_PACKS, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_THEME_ID = "pref_theme_id"
        private const val KEY_WALLPAPER_TYPE = "pref_wallpaper_type"
        private const val KEY_CUSTOM_WALLPAPER_PATH = "pref_custom_wallpaper_path"
        private const val KEY_WALLPAPER_DIM = "pref_wallpaper_dim"
        private const val KEY_KEY_OPACITY = "pref_key_opacity"
        private const val KEY_LOW_END_OPT = "pref_low_end_opt"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_SWIPE_DELETE = "pref_swipe_delete"
        private const val KEY_SPACE_GLIDE = "pref_space_glide"
        private const val KEY_SWIPE_UP_SYMBOLS = "pref_swipe_up_symbols"
        private const val KEY_SYMBOL_HINTS = "pref_symbol_hints"
        private const val KEY_AUTO_CAPS = "pref_auto_caps"
        private const val KEY_AUTO_CORRECT = "pref_auto_correct"
        private const val KEY_PREDICTIVE_EMOJI = "pref_predictive_emoji"
        private const val KEY_INCOGNITO = "pref_incognito"
        private const val KEY_HEIGHT_SCALE = "pref_height_scale"
        private const val KEY_CUSTOM_PACKS = "pref_custom_packs"

        @Volatile
        private var instance: KeyboardPreferences? = null

        fun getInstance(context: Context): KeyboardPreferences {
            return instance ?: synchronized(this) {
                instance ?: KeyboardPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
