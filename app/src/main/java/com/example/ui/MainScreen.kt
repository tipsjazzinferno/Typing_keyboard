package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.keyboard.engine.KeyboardPreferences
import com.example.keyboard.engine.RewardsManager

enum class NavTab(val title: String, val icon: ImageVector) {
    SANDBOX("Keyboard", Icons.Default.Keyboard),
    THEMES("Themes & Wall", Icons.Default.Palette),
    REWARDS("Rewards & Cards", Icons.Default.CardGiftcard),
    SETTINGS("Settings", Icons.Default.Tune)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    preferences: KeyboardPreferences,
    rewardsManager: RewardsManager,
    initialTab: NavTab = NavTab.SANDBOX
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { tab ->
            when (tab) {
                NavTab.SANDBOX -> SandboxScreen(
                    preferences = preferences,
                    rewardsManager = rewardsManager,
                    onNavigateToThemes = { currentTab = NavTab.THEMES },
                    onNavigateToRewards = { currentTab = NavTab.REWARDS }
                )
                NavTab.THEMES -> ThemeManagerScreen(preferences = preferences)
                NavTab.REWARDS -> RewardsScreen(
                    rewardsManager = rewardsManager,
                    onWatchAdRequest = { showRewardedAdDialog = true }
                )
                NavTab.SETTINGS -> GestureSettingsScreen(preferences = preferences)
            }
        }
    }

    if (showRewardedAdDialog) {
        RewardedAdDialog(
            onDismiss = { showRewardedAdDialog = false },
            onRewardEarned = { amount ->
                rewardsManager.addCoins(amount)
            }
        )
    }
}
