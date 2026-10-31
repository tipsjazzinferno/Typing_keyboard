package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.keyboard.engine.KeyboardLanguage
import com.example.keyboard.engine.KeyboardPreferences

@Composable
fun GestureSettingsScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val lowEndOpt by preferences.lowEndOptimization.collectAsState()
    val currentLanguageCode by preferences.languageCode.collectAsState()
    val swipeLeftDelete by preferences.swipeLeftDelete.collectAsState()
    val spaceCursorGlide by preferences.spaceCursorGlide.collectAsState()
    val swipeUpSymbols by preferences.swipeUpSymbols.collectAsState()
    val showSymbolHints by preferences.showSymbolHints.collectAsState()
    val autoCorrect by preferences.autoCorrect.collectAsState()
    val predictiveEmoji by preferences.predictiveEmoji.collectAsState()
    val autoCaps by preferences.autoCapitalization.collectAsState()
    val hapticFeedback by preferences.hapticFeedback.collectAsState()
    val keyHeightScale by preferences.keyHeightScale.collectAsState()
    val incognitoMode by preferences.incognitoMode.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var dictionaryCleared by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Typing & Performance",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure gesture shortcuts, speed optimizations, and privacy",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Low-End Device Mode Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (lowEndOpt) Color(0xFF064E3B).copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (lowEndOpt) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ultra-Fast Low-End Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Disables shadows, scales down animations, and optimizes RAM for entry-level phones",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = lowEndOpt,
                        onCheckedChange = { preferences.setLowEndOptimization(it) }
                    )
                }
            }
        }

        // Multi-Language Selection Card
        item {
            val curLang = KeyboardLanguage.find(currentLanguageCode)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLanguageDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Keyboard Language", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap spacebar or here to change language", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(curLang.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
        }

        // Gesture Shortcuts Guide Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Gesture Shortcuts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    GestureGuideRow("Swipe ◀ on Delete", "Deletes preceding word instantly")
                    GestureGuideRow("Glide ◀ ▶ on Space", "Smoothly scrubs cursor character by character")
                    GestureGuideRow("Swipe ▲ on any Key", "Inserts secondary number/symbol directly")
                }
            }
        }

        item {
            Text(
                text = "Smart Typing Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "Auto-Correction & Suggestions",
                        description = "Lightweight on-device suggestion engine with typo fixes",
                        checked = autoCorrect,
                        onCheckedChange = { preferences.setAutoCorrect(it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Predictive Emoji Suggestions",
                        description = "Displays matching emojis (e.g. 'fire' 🔥, 'party' 🎉) as you type",
                        checked = predictiveEmoji,
                        onCheckedChange = { preferences.setPredictiveEmoji(it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Swipe Left to Delete Word",
                        description = "Quick drag across backspace key erases previous word",
                        checked = swipeLeftDelete,
                        onCheckedChange = { preferences.setSwipeLeftDelete(it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Spacebar Cursor Glide",
                        description = "Slide across spacebar to move the cursor with precision",
                        checked = spaceCursorGlide,
                        onCheckedChange = { preferences.setSpaceCursorGlide(it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Swipe-Up Symbol Typing",
                        description = "Flick up on any key to type its secondary symbol",
                        checked = swipeUpSymbols,
                        onCheckedChange = { preferences.setSwipeUpSymbols(it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Show Symbol Hints",
                        description = "Display subtle secondary characters in key corners",
                        checked = showSymbolHints,
                        onCheckedChange = { preferences.setShowSymbolHints(it) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Tactile Haptic Feedback",
                        description = "Vibrate subtly on keypress",
                        checked = hapticFeedback,
                        onCheckedChange = { preferences.setHapticFeedback(it) }
                    )
                }
            }
        }

        item {
            Text(
                text = "Key Height & Privacy",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Keyboard Height",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when {
                                keyHeightScale < 0.95f -> "Compact (90%)"
                                keyHeightScale > 1.1f -> "Tall (115%)"
                                else -> "Standard (100%)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = keyHeightScale,
                        onValueChange = { preferences.setKeyHeightScale(it) },
                        valueRange = 0.9f..1.15f,
                        steps = 1
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    SettingToggleRow(
                        title = "Private / Incognito Mode",
                        description = "Do not save newly typed words to local dictionary",
                        checked = incognitoMode,
                        onCheckedChange = { preferences.setIncognitoMode(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Learned Vocabulary", fontSize = 12.sp)
                    }

                    if (dictionaryCleared) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✓ Learned vocabulary cleared",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose Keyboard Language") },
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
                                    preferences.setLanguage(lang.code)
                                    showLanguageDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lang.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                Text(lang.code.uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Reset Learned Words?") },
            text = { Text("This removes all words learned from your typing history on this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        dictionaryCleared = true
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GestureGuideRow(gesture: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gesture,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = action,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

