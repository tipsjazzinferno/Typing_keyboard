package com.example.keyboard.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.engine.PredictionResult
import com.example.keyboard.model.KeyboardTheme

@Composable
fun SuggestionBar(
    prediction: PredictionResult,
    theme: KeyboardTheme,
    isIncognito: Boolean,
    enableHaptic: Boolean,
    coinBalance: Int = 150,
    onSuggestionSelected: (String, Int) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onOpenRewardsAd: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(theme.keyboardBackground)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rewards Gift Button in Keyboard Header
        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .clickable {
                    if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onOpenRewardsAd()
                }
                .padding(horizontal = 8.dp)
                .testTag("suggestion_bar_rewards_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎁", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$coinBalance",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isIncognito) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6B21A8).copy(alpha = 0.35f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Incognito",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Private",
                        fontSize = 10.sp,
                        color = Color(0xFFE9D5FF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Center: Word Suggestions or Placeholder
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (prediction.suggestions.isEmpty()) {
                Text(
                    text = "Minima",
                    color = theme.keySecondaryTextColor.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                prediction.suggestions.forEachIndexed { index, suggestion ->
                    val isCandidate = index == 0 && prediction.isAutoCorrectCandidate
                    val shape = RoundedCornerShape(12.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp, vertical = 6.dp)
                            .clip(shape)
                            .then(
                                if (isCandidate) {
                                    Modifier
                                        .background(theme.accentColor.copy(alpha = 0.2f))
                                        .border(1.dp, theme.accentColor.copy(alpha = 0.7f), shape)
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable {
                                if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSuggestionSelected(suggestion, prediction.currentWord.length)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = suggestion,
                            color = if (isCandidate) theme.accentColor else theme.keyTextColor,
                            fontSize = 14.sp,
                            fontWeight = if (isCandidate) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (index < prediction.suggestions.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(theme.keySecondaryTextColor.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }

        // Right: Improved Predictive Emojis
        val displayEmojis = if (prediction.emojis.isNotEmpty()) {
            prediction.emojis
        } else {
            listOf("😊", "❤️", "🔥")
        }

        Row(
            modifier = Modifier
                .padding(start = 2.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayEmojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(theme.keyAltBackground.copy(alpha = 0.6f))
                        .clickable {
                            if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onEmojiSelected(emoji)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
        }

        // Quick Settings Gear
        IconButton(
            onClick = {
                if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onOpenSettings()
            },
            modifier = Modifier
                .size(36.dp)
                .testTag("suggestion_bar_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = theme.keySecondaryTextColor,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
