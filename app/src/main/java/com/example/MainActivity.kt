package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.keyboard.engine.KeyboardPreferences
import com.example.keyboard.engine.RewardsManager
import com.example.ui.MainScreen
import com.example.ui.NavTab
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = KeyboardPreferences.getInstance(this)
        val rewardsManager = RewardsManager.getInstance(this)

        val openTabExtra = intent?.getStringExtra("open_tab")
        val initialTab = if (openTabExtra == "rewards") NavTab.REWARDS else NavTab.SANDBOX

        setContent {
            MyApplicationTheme {
                MainScreen(
                    preferences = preferences,
                    rewardsManager = rewardsManager,
                    initialTab = initialTab
                )
            }
        }
    }
}
