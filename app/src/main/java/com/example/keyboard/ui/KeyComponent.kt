package com.example.keyboard.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.model.KeyActionType
import com.example.keyboard.model.KeyModel
import com.example.keyboard.model.KeyboardTheme
import com.example.keyboard.model.ShiftState
import kotlin.math.abs

@Composable
fun KeyComponent(
    key: KeyModel,
    theme: KeyboardTheme,
    shiftState: ShiftState,
    showSymbolHints: Boolean,
    enableSwipeUp: Boolean,
    enableHaptic: Boolean,
    keyHeight: Dp,
    keyOpacity: Float = 1.0f,
    isLowEndMode: Boolean = false,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onSwipeUp: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale = if (isLowEndMode) {
        1.0f
    } else {
        val animScale by animateFloatAsState(
            targetValue = if (isPressed) 0.94f else 1.0f,
            label = "keyScale"
        )
        animScale
    }

    val displayText = remember(key, shiftState) {
        when {
            key.actionType != KeyActionType.CHARACTER -> key.primaryText
            shiftState.isCapitalized() -> key.shiftText ?: key.primaryText.uppercase()
            else -> key.primaryText.lowercase()
        }
    }

    val rawBg = when {
        isPressed -> theme.accentColor.copy(alpha = 0.45f)
        key.actionType == KeyActionType.ENTER -> theme.accentColor
        key.isSpecial || key.actionType != KeyActionType.CHARACTER -> theme.keyAltBackground
        else -> theme.keyBackground
    }

    // Apply key opacity for wallpapers
    val backgroundColor = if (key.actionType != KeyActionType.ENTER && keyOpacity < 1.0f) {
        rawBg.copy(alpha = (rawBg.alpha * keyOpacity).coerceIn(0.2f, 1.0f))
    } else rawBg

    val contentColor = when {
        key.actionType == KeyActionType.ENTER -> if (theme.isDark) Color.Black else Color.White
        key.actionType == KeyActionType.SHIFT && shiftState != ShiftState.LOWERCASE -> theme.accentColor
        else -> theme.keyTextColor
    }

    val shape = RoundedCornerShape(theme.cornerRadius)

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.5.dp)
            .height(keyHeight)
            .scale(scale)
            .then(
                if (!isLowEndMode && theme.keyElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = if (isPressed) 0.dp else theme.keyElevation,
                        shape = shape,
                        clip = false
                    )
                } else Modifier
            )
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (theme.borderWidth > 0.dp) {
                    Modifier.border(theme.borderWidth, theme.borderColor, shape)
                } else Modifier
            )
            .testTag("key_${key.primaryText.lowercase()}")
            .pointerInput(key, enableSwipeUp) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (released) {
                            onTap()
                        }
                    },
                    onLongPress = {
                        if (enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                        if (onLongPress != null) {
                            onLongPress()
                        } else if (key.secondaryText != null) {
                            onSwipeUp?.invoke()
                        }
                    }
                )
            }
            .pointerInput(enableSwipeUp, key) {
                if (enableSwipeUp && key.secondaryText != null && onSwipeUp != null) {
                    detectDragGestures(
                        onDragStart = {},
                        onDragEnd = {},
                        onDragCancel = {},
                        onDrag = { change, dragAmount ->
                            if (dragAmount.y < -16f && abs(dragAmount.y) > abs(dragAmount.x) * 1.5f) {
                                change.consume()
                                if (enableHaptic) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                onSwipeUp()
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Secondary hint symbol in top-right corner
        if (showSymbolHints && key.secondaryText != null && key.actionType == KeyActionType.CHARACTER) {
            Text(
                text = key.secondaryText,
                color = theme.keySecondaryTextColor.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }

        // Main key content (Icon or Text)
        when (key.actionType) {
            KeyActionType.SHIFT -> {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Shift",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            KeyActionType.BACKSPACE -> {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            KeyActionType.ENTER -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            KeyActionType.EMOJI_TOGGLE -> {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Emoji",
                    tint = contentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            KeyActionType.EXTENSIONS_TOGGLE -> {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = "Extensions",
                    tint = theme.accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            KeyActionType.SPACE -> {
                Text(
                    text = "Minima",
                    color = theme.keySecondaryTextColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
            KeyActionType.HIDE_KEYBOARD -> {
                Icon(
                    imageVector = Icons.Default.KeyboardHide,
                    contentDescription = "Hide Keyboard",
                    tint = contentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            else -> {
                Text(
                    text = displayText,
                    color = contentColor,
                    fontSize = if (displayText.length > 2) 13.sp else 19.sp,
                    fontWeight = if (key.isSpecial) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
