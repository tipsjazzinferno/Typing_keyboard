package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.keyboard.engine.KeyboardPreferences
import com.example.keyboard.model.KeyboardTheme

@Composable
fun ThemeManagerScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentThemeId by preferences.themeId.collectAsState()
    val currentTheme = remember(currentThemeId) { KeyboardTheme.findById(currentThemeId) }

    val wallpaperType by preferences.wallpaperType.collectAsState()
    val customWallpaperPath by preferences.customWallpaperPath.collectAsState()
    val wallpaperDim by preferences.wallpaperDim.collectAsState()
    val keyOpacity by preferences.keyOpacity.collectAsState()

    // Android Zero-Permission Photo Picker for Custom Wallpaper
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val success = preferences.saveCustomWallpaperFromUri(uri)
            if (success) {
                Toast.makeText(context, "Custom wallpaper applied!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    text = "Themes & Wallpaper",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Set custom background wallpapers and minimal color styles",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Preview with Wallpaper
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = currentTheme.keyboardBackground),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Preview Wallpaper Background
                    if (wallpaperType == "preset_abstract") {
                        Image(
                            painter = painterResource(id = R.drawable.kb_wall_abstract),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else if (wallpaperType == "preset_mountains") {
                        Image(
                            painter = painterResource(id = R.drawable.kb_wall_mountains),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    if (wallpaperType != "none") {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = wallpaperDim))
                        )
                    }

                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE PREVIEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = currentTheme.accentColor
                            )
                            Text(
                                text = "${currentTheme.name} • ${if (wallpaperType == "none") "No Wallpaper" else "Wallpaper Active"}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Key Row Simulation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { char ->
                                val shape = RoundedCornerShape(currentTheme.cornerRadius)
                                val rawBg = currentTheme.keyBackground
                                val bg = if (wallpaperType != "none") rawBg.copy(alpha = keyOpacity) else rawBg

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(shape)
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        color = currentTheme.keyTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Spacebar simulation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val shape = RoundedCornerShape(currentTheme.cornerRadius)
                            val bg = if (wallpaperType != "none") currentTheme.keyBackground.copy(alpha = keyOpacity) else currentTheme.keyBackground

                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(36.dp)
                                    .clip(shape)
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?123", color = currentTheme.keyTextColor, fontSize = 11.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(4f)
                                    .height(36.dp)
                                    .clip(shape)
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Minima", color = currentTheme.keySecondaryTextColor, fontSize = 11.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(36.dp)
                                    .clip(shape)
                                    .background(currentTheme.accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Enter",
                                    color = if (currentTheme.isDark) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Wallpaper Section
        item {
            Text(
                text = "Keyboard Wallpaper",
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
                    Text(
                        text = "Choose Wallpaper Background",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: None (Clean Solid Theme)
                        WallpaperOptionCard(
                            title = "Solid Theme",
                            isSelected = wallpaperType == "none",
                            onClick = { preferences.setWallpaperType("none") },
                            content = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(currentTheme.keyboardBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = currentTheme.keySecondaryTextColor
                                    )
                                }
                            }
                        )

                        // Option 2: Geometric Cyber Glow Preset
                        WallpaperOptionCard(
                            title = "Cyber Glow",
                            isSelected = wallpaperType == "preset_abstract",
                            onClick = { preferences.setWallpaperType("preset_abstract") },
                            content = {
                                Image(
                                    painter = painterResource(id = R.drawable.kb_wall_abstract),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )

                        // Option 3: Starry Mountains Preset
                        WallpaperOptionCard(
                            title = "Dark Mountains",
                            isSelected = wallpaperType == "preset_mountains",
                            onClick = { preferences.setWallpaperType("preset_mountains") },
                            content = {
                                Image(
                                    painter = painterResource(id = R.drawable.kb_wall_mountains),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )

                        // Option 4: Pick Custom Photo from Gallery
                        WallpaperOptionCard(
                            title = if (wallpaperType == "custom") "My Photo ✓" else "Choose Photo",
                            isSelected = wallpaperType == "custom",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            content = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Gallery",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // Sliders for Wallpaper Dimming & Key Opacity (when wallpaper active)
                    if (wallpaperType != "none") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Darkness Scrim Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Wallpaper Dimmer (Contrast)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(wallpaperDim * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = wallpaperDim,
                                onValueChange = { preferences.setWallpaperDim(it) },
                                valueRange = 0.2f..0.85f
                            )
                        }

                        // Key Transparency Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Key Surface Translucency",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(keyOpacity * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = keyOpacity,
                                onValueChange = { preferences.setKeyOpacity(it) },
                                valueRange = 0.35f..1.0f
                            )
                        }
                    }
                }
            }
        }

        // Color Themes
        item {
            Text(
                text = "Colorway Themes",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(KeyboardTheme.BuiltInThemes) { theme ->
            val isSelected = theme.id == currentThemeId

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, theme.accentColor)
                } else {
                    CardDefaults.outlinedCardBorder()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { preferences.setTheme(theme.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.keyboardBackground)
                            .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.keyBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = theme.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = theme.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = if (theme.isDark) Color.Black else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp, 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onClick() }
        ) {
            content()
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
