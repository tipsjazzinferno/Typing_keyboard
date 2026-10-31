package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.engine.KeyboardLanguage
import com.example.keyboard.engine.KeyboardPreferences
import com.example.keyboard.engine.PredictionEngine
import com.example.keyboard.engine.RewardsManager
import com.example.keyboard.model.KeyboardTheme
import com.example.keyboard.ui.KeyboardActionListener
import com.example.keyboard.ui.MinimaKeyboardView

@Composable
fun SandboxScreen(
    preferences: KeyboardPreferences,
    rewardsManager: RewardsManager,
    onNavigateToThemes: () -> Unit,
    onNavigateToRewards: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val themeId by preferences.themeId.collectAsState()
    val theme = remember(themeId) { KeyboardTheme.findById(themeId) }
    val isIncognito by preferences.incognitoMode.collectAsState()
    val enableHaptic by preferences.hapticFeedback.collectAsState()
    val enableSwipeDelete by preferences.swipeLeftDelete.collectAsState()
    val enableSpaceGlide by preferences.spaceCursorGlide.collectAsState()
    val enableSwipeUpSymbols by preferences.swipeUpSymbols.collectAsState()
    val showSymbolHints by preferences.showSymbolHints.collectAsState()
    val keyHeightScale by preferences.keyHeightScale.collectAsState()

    val wallpaperType by preferences.wallpaperType.collectAsState()
    val customWallpaperPath by preferences.customWallpaperPath.collectAsState()
    val wallpaperDim by preferences.wallpaperDim.collectAsState()
    val keyOpacity by preferences.keyOpacity.collectAsState()
    val isLowEndMode by preferences.lowEndOptimization.collectAsState()
    val currentLangCode by preferences.languageCode.collectAsState()
    val coins by rewardsManager.coins.collectAsState()

    var showAdDialog by remember { mutableStateOf(false) }

    var testText by remember {
        mutableStateOf("Testing Minima! Type 'ho' for 'how', 'fire' for emojis 🔥, or switch language on spacebar.")
    }
    var cursorPosition by remember { mutableStateOf(testText.length) }

    val predictionEngine = remember { PredictionEngine() }
    val prediction by predictionEngine.predictionState.collectAsState()

    LaunchedEffect(currentLangCode) {
        predictionEngine.switchLanguage(currentLangCode)
    }

    LaunchedEffect(testText, cursorPosition) {
        val safeCursor = cursorPosition.coerceIn(0, testText.length)
        val textBefore = testText.substring(0, safeCursor)
        predictionEngine.onTextContextChanged(textBefore)
    }

    var isImeSelected by remember { mutableStateOf(false) }

    fun checkImeStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            isImeSelected = currentIme != null && currentIme.contains(context.packageName)
        }
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable Top Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // IME Activation Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isImeSelected) {
                        Color(0xFF064E3B).copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isImeSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isImeSelected) Icons.Default.Check else Icons.Default.Keyboard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isImeSelected) "Minima is Active System Keyboard" else "Keyboard Activation",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isImeSelected) "Ready in all apps" else "Enable Minima to type system-wide",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!isImeSelected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("1. Enable in Settings", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    imm?.showInputMethodPicker()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("2. Switch to Minima", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Quick Rewards & Coins Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRewards() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎁", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rewards Wallet: ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$coins Coins",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            Text(
                                text = "Watch short videos to earn gift cards",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showAdDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("+50 🪙", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Interactive Live Playground Input Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Typing Sandbox (${KeyboardLanguage.find(currentLangCode).name})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(testText)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    testText = ""
                                    cursorPosition = 0
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text display area with cursor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(10.dp)
                    ) {
                        val safePos = cursorPosition.coerceIn(0, testText.length)
                        val beforeCursor = testText.substring(0, safePos)
                        val afterCursor = testText.substring(safePos)

                        if (testText.isEmpty()) {
                            Text(
                                text = "Tap the keyboard keys below to start typing...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = beforeCursor,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 19.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(theme.accentColor)
                                )
                                Text(
                                    text = afterCursor,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick test snippets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                testText = "ho"
                                cursorPosition = testText.length
                            },
                            label = { Text("Test 'ho' -> 'how'", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                testText = "fire "
                                cursorPosition = testText.length
                            },
                            label = { Text("Emoji 🔥", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                val next = preferences.cycleNextLanguage()
                                predictionEngine.switchLanguage(next.code)
                            },
                            label = { Text("Lang: ${KeyboardLanguage.find(currentLangCode).code.uppercase()}", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        Divider(color = theme.keyAltBackground.copy(alpha = 0.5f), thickness = 1.dp)

        // Docked Live Keyboard
        MinimaKeyboardView(
            theme = theme,
            prediction = prediction,
            isIncognito = isIncognito,
            enableHaptic = enableHaptic,
            enableSwipeDelete = enableSwipeDelete,
            enableSpaceGlide = enableSpaceGlide,
            enableSwipeUpSymbols = enableSwipeUpSymbols,
            showSymbolHints = showSymbolHints,
            keyHeightScale = keyHeightScale,
            wallpaperType = wallpaperType,
            customWallpaperPath = customWallpaperPath,
            wallpaperDim = wallpaperDim,
            keyOpacity = keyOpacity,
            isLowEndMode = isLowEndMode,
            currentLanguageCode = currentLangCode,
            coinBalance = coins,
            listener = object : KeyboardActionListener {
                override fun onText(text: String) {
                    val safePos = cursorPosition.coerceIn(0, testText.length)
                    val before = testText.substring(0, safePos)
                    val after = testText.substring(safePos)
                    testText = before + text + after
                    cursorPosition = safePos + text.length
                    if (text.endsWith(" ")) {
                        predictionEngine.onWordCommitted(text.trim())
                        rewardsManager.onWordsTyped(1)
                    }
                }

                override fun onAcceptSuggestion(word: String, prefixLength: Int) {
                    val safePos = cursorPosition.coerceIn(0, testText.length)
                    val deleteStart = (safePos - prefixLength).coerceAtLeast(0)
                    val before = testText.substring(0, deleteStart)
                    val after = testText.substring(safePos)
                    val replacement = word + " "
                    testText = before + replacement + after
                    cursorPosition = before.length + replacement.length
                    predictionEngine.onWordCommitted(word)
                    rewardsManager.onWordsTyped(1)
                }

                override fun onBackspace() {
                    if (cursorPosition > 0 && testText.isNotEmpty()) {
                        val safePos = cursorPosition.coerceIn(0, testText.length)
                        val before = testText.substring(0, safePos - 1)
                        val after = testText.substring(safePos)
                        testText = before + after
                        cursorPosition = (safePos - 1).coerceAtLeast(0)
                    }
                }

                override fun onDeleteWord() {
                    if (cursorPosition > 0 && testText.isNotEmpty()) {
                        val safePos = cursorPosition.coerceIn(0, testText.length)
                        val before = testText.substring(0, safePos)
                        var i = before.length - 1
                        while (i >= 0 && Character.isWhitespace(before[i])) {
                            i--
                        }
                        while (i >= 0 && !Character.isWhitespace(before[i])) {
                            i--
                        }
                        val newBefore = before.substring(0, (i + 1).coerceAtLeast(0))
                        val after = testText.substring(safePos)
                        testText = newBefore + after
                        cursorPosition = newBefore.length
                    }
                }

                override fun onEnter() {
                    val safePos = cursorPosition.coerceIn(0, testText.length)
                    val before = testText.substring(0, safePos)
                    val after = testText.substring(safePos)
                    testText = before + "\n" + after
                    cursorPosition = safePos + 1
                    rewardsManager.onWordsTyped(1)
                }

                override fun onMoveCursor(delta: Int) {
                    cursorPosition = (cursorPosition + delta).coerceIn(0, testText.length)
                }

                override fun onOpenRewardsAd() {
                    showAdDialog = true
                }

                override fun onSwitchLanguage(code: String) {
                    preferences.setLanguage(code)
                    predictionEngine.switchLanguage(code)
                }

                override fun onOpenSettings() {
                    onNavigateToThemes()
                }

                override fun onHideKeyboard() {}
            }
        )
    }

    if (showAdDialog) {
        RewardedAdDialog(
            onDismiss = { showAdDialog = false },
            onRewardEarned = { amount ->
                rewardsManager.addCoins(amount)
            }
        )
    }
}
