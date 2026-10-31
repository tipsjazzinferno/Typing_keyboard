package com.example.keyboard.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.keyboard.model.ExtensionPack
import com.example.keyboard.model.KeyboardTheme

@Composable
fun ExtensionPacksDrawer(
    packs: List<ExtensionPack>,
    theme: KeyboardTheme,
    enableHaptic: Boolean,
    onInsertItem: (String) -> Unit,
    onClose: () -> Unit,
    onManageExtensions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val enabledPacks = remember(packs) { packs.filter { it.isEnabled } }
    var selectedPackIndex by remember { mutableStateOf(0) }

    val currentPack = enabledPacks.getOrNull(selectedPackIndex) ?: enabledPacks.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(theme.keyboardBackground)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Top Pack Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                enabledPacks.forEachIndexed { index, pack ->
                    val isSelected = index == selectedPackIndex
                    val pillShape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(
                                if (isSelected) theme.accentColor else theme.keyAltBackground
                            )
                            .clickable {
                                if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedPackIndex = index
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = pack.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pack.name.split(" ").first(),
                                color = if (isSelected) {
                                    if (theme.isDark) Color.Black else Color.White
                                } else theme.keyTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Marketplace shortcut button
            IconButton(
                onClick = {
                    if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onManageExtensions()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Manage Packs",
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Close Drawer button
            IconButton(
                onClick = {
                    if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClose()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Extensions",
                    tint = theme.keySecondaryTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Divider(color = theme.keyAltBackground.copy(alpha = 0.5f), thickness = 0.8.dp)

        // Item Cards Grid
        if (currentPack != null && currentPack.items.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 6.dp),
                contentPadding = PaddingValues(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(currentPack.items) { item ->
                    val shape = RoundedCornerShape(8.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(shape)
                            .background(theme.keyBackground)
                            .border(1.dp, theme.keyAltBackground, shape)
                            .clickable {
                                if (enableHaptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onInsertItem(item.textToInsert)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.preview,
                                color = theme.keyTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = item.label,
                                color = theme.keySecondaryTextColor,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No extensions in this pack.\nTap store to browse more!",
                    color = theme.keySecondaryTextColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
