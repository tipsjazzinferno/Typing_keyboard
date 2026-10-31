package com.example.keyboard.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.keyboard.engine.KeyboardLanguage
import com.example.keyboard.engine.PredictionResult
import com.example.keyboard.model.*
import java.io.File
import kotlin.math.abs

interface KeyboardActionListener {
    fun onText(text: String)
    fun onAcceptSuggestion(word: String, prefixLength: Int)
    fun onBackspace()
    fun onDeleteWord()
    fun onEnter()
    fun onMoveCursor(delta: Int)
    fun onOpenRewardsAd()
    fun onSwitchLanguage(code: String)
    fun onOpenSettings()
    fun onHideKeyboard()
}

@Composable
fun MinimaKeyboardView(
    theme: KeyboardTheme,
    prediction: PredictionResult,
    isIncognito: Boolean,
    enableHaptic: Boolean,
    enableSwipeDelete: Boolean,
    enableSpaceGlide: Boolean,
    enableSwipeUpSymbols: Boolean,
    showSymbolHints: Boolean,
    keyHeightScale: Float = 1.0f,
    wallpaperType: String = "none",
    customWallpaperPath: String? = null,
    wallpaperDim: Float = 0.45f,
    keyOpacity: Float = 0.85f,
    isLowEndMode: Boolean = false,
    currentLanguageCode: String = "en",
    coinBalance: Int = 150,
    listener: KeyboardActionListener,
    modifier: Modifier = Modifier
) {
    // Resolve dynamic system default theme (Dark or Light)
    val isSystemDark = isSystemInDarkTheme()
    val activeTheme = remember(theme, isSystemDark) {
        KeyboardTheme.resolveForSystem(theme.id, isSystemDark)
    }

    var keyboardPage by remember { mutableStateOf(KeyboardPage.LETTERS) }
    var shiftState by remember { mutableStateOf(ShiftState.LOWERCASE) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    val baseKeyHeight: Dp = (46.dp * keyHeightScale)
    val effectiveKeyOpacity = if (wallpaperType != "none") keyOpacity else 1.0f

    val currentLang = remember(currentLanguageCode) {
        KeyboardLanguage.find(currentLanguageCode)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(activeTheme.keyboardBackground)
    ) {
        // Background Wallpaper Layer
        if (wallpaperType != "none") {
            when (wallpaperType) {
                "preset_abstract" -> {
                    Image(
                        painter = painterResource(id = R.drawable.kb_wall_abstract),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
                "preset_mountains" -> {
                    Image(
                        painter = painterResource(id = R.drawable.kb_wall_mountains),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
                "custom" -> {
                    val customBitmap = remember(customWallpaperPath) {
                        if (!customWallpaperPath.isNullOrBlank()) {
                            val f = File(customWallpaperPath)
                            if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                        } else null
                    }
                    if (customBitmap != null) {
                        Image(
                            bitmap = customBitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }

            // Dark Scrim Overlay for Perfect Contrast
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = wallpaperDim.coerceIn(0.1f, 0.9f)))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Clean Suggestion Bar with Rewards Gift Icon
            SuggestionBar(
                prediction = prediction,
                theme = activeTheme,
                isIncognito = isIncognito,
                enableHaptic = enableHaptic,
                coinBalance = coinBalance,
                onSuggestionSelected = { word, prefixLen ->
                    listener.onAcceptSuggestion(word, prefixLen)
                },
                onEmojiSelected = { emoji ->
                    listener.onText(emoji)
                },
                onOpenRewardsAd = {
                    listener.onOpenRewardsAd()
                },
                onOpenSettings = {
                    listener.onOpenSettings()
                }
            )

            if (keyboardPage == KeyboardPage.EMOJI) {
                EmojiKeyboardPanel(
                    theme = activeTheme,
                    enableHaptic = enableHaptic,
                    onEmojiClick = { emoji ->
                        listener.onText(emoji)
                    },
                    onBackToLetters = {
                        keyboardPage = KeyboardPage.LETTERS
                    },
                    onBackspace = {
                        listener.onBackspace()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp, vertical = 2.dp)
                ) {
                    when (keyboardPage) {
                        KeyboardPage.LETTERS -> {
                            QwertyRows(
                                theme = activeTheme,
                                shiftState = shiftState,
                                showSymbolHints = showSymbolHints,
                                enableSwipeUp = enableSwipeUpSymbols,
                                enableHaptic = enableHaptic,
                                enableSwipeDelete = enableSwipeDelete,
                                enableSpaceGlide = enableSpaceGlide,
                                keyHeight = baseKeyHeight,
                                keyOpacity = effectiveKeyOpacity,
                                isLowEndMode = isLowEndMode,
                                currentLanguage = currentLang,
                                onKeyClick = { text ->
                                    listener.onText(text)
                                    if (shiftState == ShiftState.SHIFTED) {
                                        shiftState = ShiftState.LOWERCASE
                                    }
                                },
                                onShiftClick = {
                                    shiftState = when (shiftState) {
                                        ShiftState.LOWERCASE -> ShiftState.SHIFTED
                                        ShiftState.SHIFTED -> ShiftState.CAPS_LOCKED
                                        ShiftState.CAPS_LOCKED -> ShiftState.LOWERCASE
                                    }
                                },
                                onBackspace = { listener.onBackspace() },
                                onDeleteWord = { listener.onDeleteWord() },
                                onEnter = { listener.onEnter() },
                                onSwitchPage = { page -> keyboardPage = page },
                                onMoveCursor = { delta -> listener.onMoveCursor(delta) },
                                onToggleEmoji = { keyboardPage = KeyboardPage.EMOJI },
                                onOpenLanguageDialog = { showLanguagePicker = true },
                                onCycleLanguage = {
                                    val all = KeyboardLanguage.AllLanguages
                                    val curIdx = all.indexOfFirst { it.code == currentLang.code }
                                    val next = all[(curIdx + 1) % all.size]
                                    listener.onSwitchLanguage(next.code)
                                },
                                onHideKeyboard = { listener.onHideKeyboard() }
                            )
                        }
                        KeyboardPage.NUMBERS -> {
                            NumbersSymbolsRows(
                                isAlt = false,
                                theme = activeTheme,
                                enableHaptic = enableHaptic,
                                enableSwipeDelete = enableSwipeDelete,
                                enableSpaceGlide = enableSpaceGlide,
                                keyHeight = baseKeyHeight,
                                keyOpacity = effectiveKeyOpacity,
                                isLowEndMode = isLowEndMode,
                                currentLanguage = currentLang,
                                onKeyClick = { text -> listener.onText(text) },
                                onBackspace = { listener.onBackspace() },
                                onDeleteWord = { listener.onDeleteWord() },
                                onEnter = { listener.onEnter() },
                                onSwitchPage = { page -> keyboardPage = page },
                                onMoveCursor = { delta -> listener.onMoveCursor(delta) },
                                onToggleEmoji = { keyboardPage = KeyboardPage.EMOJI },
                                onOpenLanguageDialog = { showLanguagePicker = true }
                            )
                        }
                        KeyboardPage.SYMBOLS -> {
                            NumbersSymbolsRows(
                                isAlt = true,
                                theme = activeTheme,
                                enableHaptic = enableHaptic,
                                enableSwipeDelete = enableSwipeDelete,
                                enableSpaceGlide = enableSpaceGlide,
                                keyHeight = baseKeyHeight,
                                keyOpacity = effectiveKeyOpacity,
                                isLowEndMode = isLowEndMode,
                                currentLanguage = currentLang,
                                onKeyClick = { text -> listener.onText(text) },
                                onBackspace = { listener.onBackspace() },
                                onDeleteWord = { listener.onDeleteWord() },
                                onEnter = { listener.onEnter() },
                                onSwitchPage = { page -> keyboardPage = page },
                                onMoveCursor = { delta -> listener.onMoveCursor(delta) },
                                onToggleEmoji = { keyboardPage = KeyboardPage.EMOJI },
                                onOpenLanguageDialog = { showLanguagePicker = true }
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }

        // Quick Language Switcher Dialog
        if (showLanguagePicker) {
            AlertDialog(
                onDismissRequest = { showLanguagePicker = false },
                title = { Text("Select Keyboard Language") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KeyboardLanguage.AllLanguages.forEach { lang ->
                            val isSelected = lang.code == currentLanguageCode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        listener.onSwitchLanguage(lang.code)
                                        showLanguagePicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lang.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = lang.code.uppercase(),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLanguagePicker = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun QwertyRows(
    theme: KeyboardTheme,
    shiftState: ShiftState,
    showSymbolHints: Boolean,
    enableSwipeUp: Boolean,
    enableHaptic: Boolean,
    enableSwipeDelete: Boolean,
    enableSpaceGlide: Boolean,
    keyHeight: Dp,
    keyOpacity: Float,
    isLowEndMode: Boolean,
    currentLanguage: KeyboardLanguage,
    onKeyClick: (String) -> Unit,
    onShiftClick: () -> Unit,
    onBackspace: () -> Unit,
    onDeleteWord: () -> Unit,
    onEnter: () -> Unit,
    onSwitchPage: (KeyboardPage) -> Unit,
    onMoveCursor: (Int) -> Unit,
    onToggleEmoji: () -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onCycleLanguage: () -> Unit,
    onHideKeyboard: () -> Unit
) {
    val row1 = listOf(
        KeyModel("q", secondaryText = "1"),
        KeyModel("w", secondaryText = "2"),
        KeyModel("e", secondaryText = "3"),
        KeyModel("r", secondaryText = "4"),
        KeyModel("t", secondaryText = "5"),
        KeyModel("y", secondaryText = "6"),
        KeyModel("u", secondaryText = "7"),
        KeyModel("i", secondaryText = "8"),
        KeyModel("o", secondaryText = "9"),
        KeyModel("p", secondaryText = "0")
    )

    val row2 = listOf(
        KeyModel("a", secondaryText = "@"),
        KeyModel("s", secondaryText = "#"),
        KeyModel("d", secondaryText = "$"),
        KeyModel("f", secondaryText = "%"),
        KeyModel("g", secondaryText = "&"),
        KeyModel("h", secondaryText = "*"),
        KeyModel("j", secondaryText = "-"),
        KeyModel("k", secondaryText = "+"),
        KeyModel("l", secondaryText = "=")
    )

    val row3Letters = listOf(
        KeyModel("z", secondaryText = "("),
        KeyModel("x", secondaryText = ")"),
        KeyModel("c", secondaryText = "/"),
        KeyModel("v", secondaryText = "'"),
        KeyModel("b", secondaryText = "\""),
        KeyModel("n", secondaryText = ";"),
        KeyModel("m", secondaryText = ":")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEach { key ->
                KeyComponent(
                    key = key,
                    theme = theme,
                    shiftState = shiftState,
                    showSymbolHints = showSymbolHints,
                    enableSwipeUp = enableSwipeUp,
                    enableHaptic = enableHaptic,
                    keyHeight = keyHeight,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        val text = if (shiftState.isCapitalized()) key.primaryText.uppercase() else key.primaryText.lowercase()
                        onKeyClick(text)
                    },
                    onSwipeUp = { key.secondaryText?.let { onKeyClick(it) } }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            row2.forEach { key ->
                KeyComponent(
                    key = key,
                    theme = theme,
                    shiftState = shiftState,
                    showSymbolHints = showSymbolHints,
                    enableSwipeUp = enableSwipeUp,
                    enableHaptic = enableHaptic,
                    keyHeight = keyHeight,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        val text = if (shiftState.isCapitalized()) key.primaryText.uppercase() else key.primaryText.lowercase()
                        onKeyClick(text)
                    },
                    onSwipeUp = { key.secondaryText?.let { onKeyClick(it) } }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            KeyComponent(
                key = KeyModel("Shift", actionType = KeyActionType.SHIFT, weight = 1.4f, isSpecial = true),
                theme = theme,
                shiftState = shiftState,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1.4f),
                onTap = { onShiftClick() }
            )

            row3Letters.forEach { key ->
                KeyComponent(
                    key = key,
                    theme = theme,
                    shiftState = shiftState,
                    showSymbolHints = showSymbolHints,
                    enableSwipeUp = enableSwipeUp,
                    enableHaptic = enableHaptic,
                    keyHeight = keyHeight,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        val text = if (shiftState.isCapitalized()) key.primaryText.uppercase() else key.primaryText.lowercase()
                        onKeyClick(text)
                    },
                    onSwipeUp = { key.secondaryText?.let { onKeyClick(it) } }
                )
            }

            BackspaceKeyWithGesture(
                theme = theme,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                enableHaptic = enableHaptic,
                enableSwipeDelete = enableSwipeDelete,
                modifier = Modifier.weight(1.4f),
                onTap = { onBackspace() },
                onSwipeLeft = { onDeleteWord() }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            KeyComponent(
                key = KeyModel("?123", actionType = KeyActionType.SWITCH_PAGE, weight = 1.3f, isSpecial = true),
                theme = theme,
                shiftState = shiftState,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1.3f),
                onTap = { onSwitchPage(KeyboardPage.NUMBERS) }
            )

            // Emoji Button
            KeyComponent(
                key = KeyModel("Emoji", actionType = KeyActionType.EMOJI_TOGGLE, weight = 1f, isSpecial = true),
                theme = theme,
                shiftState = shiftState,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1f),
                onTap = { onToggleEmoji() }
            )

            // Comma
            KeyComponent(
                key = KeyModel(",", weight = 0.9f),
                theme = theme,
                shiftState = shiftState,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(0.9f),
                onTap = { onKeyClick(",") }
            )

            // Spacebar with Multi-Language Support & Glide Cursor
            SpacebarWithLanguage(
                languageLabel = currentLanguage.spaceLabel,
                theme = theme,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                enableHaptic = enableHaptic,
                enableSpaceGlide = enableSpaceGlide,
                modifier = Modifier.weight(4.4f),
                onTap = { onKeyClick(" ") },
                onGlide = { delta -> onMoveCursor(delta) },
                onLongPress = onOpenLanguageDialog,
                onSwipeCycleLanguage = onCycleLanguage
            )

            // Period
            KeyComponent(
                key = KeyModel(".", weight = 0.9f),
                theme = theme,
                shiftState = shiftState,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(0.9f),
                onTap = { onKeyClick(".") }
            )

            KeyComponent(
                key = KeyModel("Enter", actionType = KeyActionType.ENTER, weight = 1.5f, isSpecial = true),
                theme = theme,
                shiftState = shiftState,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = 1.0f,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1.5f),
                onTap = { onEnter() }
            )
        }
    }
}

@Composable
private fun NumbersSymbolsRows(
    isAlt: Boolean,
    theme: KeyboardTheme,
    enableHaptic: Boolean,
    enableSwipeDelete: Boolean,
    enableSpaceGlide: Boolean,
    keyHeight: Dp,
    keyOpacity: Float,
    isLowEndMode: Boolean,
    currentLanguage: KeyboardLanguage,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onDeleteWord: () -> Unit,
    onEnter: () -> Unit,
    onSwitchPage: (KeyboardPage) -> Unit,
    onMoveCursor: (Int) -> Unit,
    onToggleEmoji: () -> Unit,
    onOpenLanguageDialog: () -> Unit
) {
    val row1 = if (!isAlt) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    else listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")

    val row2 = if (!isAlt) listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")")
    else listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\")

    val row3 = if (!isAlt) listOf("!", "\"", "'", ":", ";", "/", "?")
    else listOf("%", "©", "®", "™", "✓", "[", "]")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEach { char ->
                KeyComponent(
                    key = KeyModel(char),
                    theme = theme,
                    shiftState = ShiftState.LOWERCASE,
                    showSymbolHints = false,
                    enableSwipeUp = false,
                    enableHaptic = enableHaptic,
                    keyHeight = keyHeight,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    modifier = Modifier.weight(1f),
                    onTap = { onKeyClick(char) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            row2.forEach { char ->
                KeyComponent(
                    key = KeyModel(char),
                    theme = theme,
                    shiftState = ShiftState.LOWERCASE,
                    showSymbolHints = false,
                    enableSwipeUp = false,
                    enableHaptic = enableHaptic,
                    keyHeight = keyHeight,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    modifier = Modifier.weight(1f),
                    onTap = { onKeyClick(char) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            KeyComponent(
                key = KeyModel(if (!isAlt) "=/<" else "?123", actionType = KeyActionType.SWITCH_PAGE, weight = 1.4f, isSpecial = true),
                theme = theme,
                shiftState = ShiftState.LOWERCASE,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1.4f),
                onTap = { onSwitchPage(if (!isAlt) KeyboardPage.SYMBOLS else KeyboardPage.NUMBERS) }
            )

            row3.forEach { char ->
                KeyComponent(
                    key = KeyModel(char),
                    theme = theme,
                    shiftState = ShiftState.LOWERCASE,
                    showSymbolHints = false,
                    enableSwipeUp = false,
                    enableHaptic = enableHaptic,
                    keyHeight = keyHeight,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    modifier = Modifier.weight(1f),
                    onTap = { onKeyClick(char) }
                )
            }

            BackspaceKeyWithGesture(
                theme = theme,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                enableHaptic = enableHaptic,
                enableSwipeDelete = enableSwipeDelete,
                modifier = Modifier.weight(1.4f),
                onTap = { onBackspace() },
                onSwipeLeft = { onDeleteWord() }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            KeyComponent(
                key = KeyModel("ABC", actionType = KeyActionType.SWITCH_PAGE, weight = 1.3f, isSpecial = true),
                theme = theme,
                shiftState = ShiftState.LOWERCASE,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1.3f),
                onTap = { onSwitchPage(KeyboardPage.LETTERS) }
            )

            KeyComponent(
                key = KeyModel("Emoji", actionType = KeyActionType.EMOJI_TOGGLE, weight = 1f, isSpecial = true),
                theme = theme,
                shiftState = ShiftState.LOWERCASE,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1f),
                onTap = { onToggleEmoji() }
            )

            KeyComponent(
                key = KeyModel(",", weight = 0.9f),
                theme = theme,
                shiftState = ShiftState.LOWERCASE,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(0.9f),
                onTap = { onKeyClick(",") }
            )

            SpacebarWithLanguage(
                languageLabel = currentLanguage.spaceLabel,
                theme = theme,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                enableHaptic = enableHaptic,
                enableSpaceGlide = enableSpaceGlide,
                modifier = Modifier.weight(4.4f),
                onTap = { onKeyClick(" ") },
                onGlide = { delta -> onMoveCursor(delta) },
                onLongPress = onOpenLanguageDialog,
                onSwipeCycleLanguage = {}
            )

            KeyComponent(
                key = KeyModel(".", weight = 0.9f),
                theme = theme,
                shiftState = ShiftState.LOWERCASE,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = keyOpacity,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(0.9f),
                onTap = { onKeyClick(".") }
            )

            KeyComponent(
                key = KeyModel("Enter", actionType = KeyActionType.ENTER, weight = 1.5f, isSpecial = true),
                theme = theme,
                shiftState = ShiftState.LOWERCASE,
                showSymbolHints = false,
                enableSwipeUp = false,
                enableHaptic = enableHaptic,
                keyHeight = keyHeight,
                keyOpacity = 1.0f,
                isLowEndMode = isLowEndMode,
                modifier = Modifier.weight(1.5f),
                onTap = { onEnter() }
            )
        }
    }
}

@Composable
private fun SpacebarWithLanguage(
    languageLabel: String,
    theme: KeyboardTheme,
    keyHeight: Dp,
    keyOpacity: Float,
    isLowEndMode: Boolean,
    enableHaptic: Boolean,
    enableSpaceGlide: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onGlide: (Int) -> Unit,
    onLongPress: () -> Unit,
    onSwipeCycleLanguage: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    var accumulatedGlideX by remember { mutableStateOf(0f) }
    var isGliding by remember { mutableStateOf(false) }

    val rawBg = if (isPressed) theme.accentColor.copy(alpha = 0.45f) else theme.keyBackground
    val backgroundColor = if (keyOpacity < 1.0f) rawBg.copy(alpha = (rawBg.alpha * keyOpacity).coerceIn(0.2f, 1.0f)) else rawBg

    val shape = RoundedCornerShape(theme.cornerRadius)

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.5.dp)
            .height(keyHeight)
            .clip(shape)
            .background(backgroundColor)
            .pointerInput(enableSpaceGlide) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        isGliding = false
                        accumulatedGlideX = 0f
                        if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (released && !isGliding) {
                            onTap()
                        }
                    },
                    onLongPress = {
                        if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongPress()
                    }
                )
            }
            .pointerInput(enableSpaceGlide) {
                if (enableSpaceGlide) {
                    detectDragGestures(
                        onDragStart = {
                            accumulatedGlideX = 0f
                            isGliding = false
                        },
                        onDragEnd = {
                            accumulatedGlideX = 0f
                            isGliding = false
                        },
                        onDrag = { change, dragAmount ->
                            accumulatedGlideX += dragAmount.x
                            val threshold = 22f
                            if (abs(accumulatedGlideX) > threshold) {
                                isGliding = true
                                val steps = (accumulatedGlideX / threshold).toInt()
                                if (steps != 0) {
                                    change.consume()
                                    if (enableHaptic) {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    }
                                    onGlide(steps)
                                    accumulatedGlideX %= threshold
                                }
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = if (isGliding) theme.accentColor else theme.keySecondaryTextColor.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGliding) "◀ Glide Cursor ▶" else languageLabel,
                color = if (isGliding) theme.accentColor else theme.keySecondaryTextColor.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BackspaceKeyWithGesture(
    theme: KeyboardTheme,
    keyHeight: Dp,
    keyOpacity: Float,
    isLowEndMode: Boolean,
    enableHaptic: Boolean,
    enableSwipeDelete: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    var totalDragX by remember { mutableStateOf(0f) }
    var swipedWord by remember { mutableStateOf(false) }

    val rawBg = if (isPressed) theme.accentColor.copy(alpha = 0.45f) else theme.keyAltBackground
    val backgroundColor = if (keyOpacity < 1.0f) rawBg.copy(alpha = (rawBg.alpha * keyOpacity).coerceIn(0.2f, 1.0f)) else rawBg

    val shape = RoundedCornerShape(theme.cornerRadius)

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.5.dp)
            .height(keyHeight)
            .clip(shape)
            .background(backgroundColor)
            .pointerInput(enableSwipeDelete) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        swipedWord = false
                        totalDragX = 0f
                        if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (released && !swipedWord) {
                            onTap()
                        }
                    }
                )
            }
            .pointerInput(enableSwipeDelete) {
                if (enableSwipeDelete) {
                    detectDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onDragEnd = { totalDragX = 0f },
                        onDrag = { change, dragAmount ->
                            totalDragX += dragAmount.x
                            if (totalDragX < -36f && !swipedWord) {
                                swipedWord = true
                                change.consume()
                                if (enableHaptic) {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                }
                                onSwipeLeft()
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Backspace,
            contentDescription = "Backspace",
            tint = theme.keyTextColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmojiKeyboardPanel(
    theme: KeyboardTheme,
    enableHaptic: Boolean,
    onEmojiClick: (String) -> Unit,
    onBackToLetters: () -> Unit,
    onBackspace: () -> Unit
) {
    val emojis = remember {
        listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛",
            "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "😏", "😒",
            "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢",
            "😭", "😮‍💨", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨",
            "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈",
            "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤝", "👏", "🙌",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹",
            "✨", "⭐", "🌟", "💫", "💥", "🔥", "💯", "🎉", "🎊", "☕", "🍕", "🚀"
        )
    }
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(theme.keyboardBackground)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onBackToLetters()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.keyAltBackground,
                    contentColor = theme.keyTextColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("ABC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onBackspace()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = theme.keyTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 42.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onEmojiClick(emoji)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}
