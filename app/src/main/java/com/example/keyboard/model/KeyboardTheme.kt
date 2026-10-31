package com.example.keyboard.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyboardTheme(
    val id: String,
    val name: String,
    val description: String,
    val isDark: Boolean = true,
    val keyboardBackground: Color,
    val keyBackground: Color,
    val keyAltBackground: Color, // For Shift, Backspace, Enter, Mode Switch
    val keyTextColor: Color,
    val keySecondaryTextColor: Color,
    val accentColor: Color,
    val cornerRadius: Dp = 8.dp,
    val keyElevation: Dp = 1.dp,
    val borderWidth: Dp = 0.dp,
    val borderColor: Color = Color.Transparent
) {
    companion object {
        val OledDark = KeyboardTheme(
            id = "oled_dark",
            name = "OLED Pure Dark",
            description = "Battery-efficient pitch black with electric cyan accents",
            isDark = true,
            keyboardBackground = Color(0xFF000000),
            keyBackground = Color(0xFF141416),
            keyAltBackground = Color(0xFF1E1E24),
            keyTextColor = Color(0xFFF4F4F5),
            keySecondaryTextColor = Color(0xFF71717A),
            accentColor = Color(0xFF06B6D4),
            cornerRadius = 8.dp,
            keyElevation = 0.dp,
            borderWidth = 1.dp,
            borderColor = Color(0xFF27272A)
        )

        val NordicFrost = KeyboardTheme(
            id = "nordic_frost",
            name = "Nordic Frost",
            description = "Crisp deep navy slate with icy sky-blue glow",
            isDark = true,
            keyboardBackground = Color(0xFF0B132B),
            keyBackground = Color(0xFF1C2541),
            keyAltBackground = Color(0xFF27355B),
            keyTextColor = Color(0xFFF0F4F8),
            keySecondaryTextColor = Color(0xFF829AB1),
            accentColor = Color(0xFF38BDF8),
            cornerRadius = 10.dp,
            keyElevation = 1.dp
        )

        val CyberNeon = KeyboardTheme(
            id = "cyber_neon",
            name = "Cyber Neon",
            description = "Futuristic midnight indigo with vivid magenta highlights",
            isDark = true,
            keyboardBackground = Color(0xFF0A071E),
            keyBackground = Color(0xFF161234),
            keyAltBackground = Color(0xFF241C54),
            keyTextColor = Color(0xFFFFFFFF),
            keySecondaryTextColor = Color(0xFF9D8DF1),
            accentColor = Color(0xFFEC4899),
            cornerRadius = 6.dp,
            borderWidth = 1.dp,
            borderColor = Color(0xFF382B7D)
        )

        val MinimalPaper = KeyboardTheme(
            id = "minimal_paper",
            name = "Minimal Paper",
            description = "Clean stark monochrome with high-contrast tactile keys",
            isDark = false,
            keyboardBackground = Color(0xFFF4F4F5),
            keyBackground = Color(0xFFFFFFFF),
            keyAltBackground = Color(0xFFE4E4E7),
            keyTextColor = Color(0xFF18181B),
            keySecondaryTextColor = Color(0xFF71717A),
            accentColor = Color(0xFF2563EB),
            cornerRadius = 8.dp,
            keyElevation = 1.5.dp,
            borderWidth = 0.5.dp,
            borderColor = Color(0xFFE4E4E7)
        )

        val ForestSage = KeyboardTheme(
            id = "forest_sage",
            name = "Forest Sage",
            description = "Earthy deep pine slate with calming mint emerald",
            isDark = true,
            keyboardBackground = Color(0xFF0F1A14),
            keyBackground = Color(0xFF1A2E24),
            keyAltBackground = Color(0xFF253F32),
            keyTextColor = Color(0xFFE8F5E9),
            keySecondaryTextColor = Color(0xFF81C784),
            accentColor = Color(0xFF10B981),
            cornerRadius = 10.dp
        )

        val SunsetEmber = KeyboardTheme(
            id = "sunset_ember",
            name = "Sunset Ember",
            description = "Warm dusky violet backdrop with glowing fiery coral",
            isDark = true,
            keyboardBackground = Color(0xFF181024),
            keyBackground = Color(0xFF261A38),
            keyAltBackground = Color(0xFF362550),
            keyTextColor = Color(0xFFFFF7ED),
            keySecondaryTextColor = Color(0xFFFDBA74),
            accentColor = Color(0xFFF97316),
            cornerRadius = 12.dp
        )

        val CarbonSlate = KeyboardTheme(
            id = "carbon_slate",
            name = "Carbon Slate",
            description = "Ultra sleek graphite gray with crisp amber focus",
            isDark = true,
            keyboardBackground = Color(0xFF18181B),
            keyBackground = Color(0xFF27272A),
            keyAltBackground = Color(0xFF3F3F46),
            keyTextColor = Color(0xFFFAFAFA),
            keySecondaryTextColor = Color(0xFFA1A1AA),
            accentColor = Color(0xFFF59E0B),
            cornerRadius = 8.dp
        )

        val SystemAuto = KeyboardTheme(
            id = "system_auto",
            name = "System Auto (Dark / Light)",
            description = "Automatically adapts to your Android device system dark or light theme",
            isDark = true,
            keyboardBackground = Color(0xFF0F172A),
            keyBackground = Color(0xFF1E293B),
            keyAltBackground = Color(0xFF334155),
            keyTextColor = Color(0xFFF8FAFC),
            keySecondaryTextColor = Color(0xFF94A3B8),
            accentColor = Color(0xFF06B6D4)
        )

        val BuiltInThemes = listOf(
            SystemAuto,
            OledDark,
            NordicFrost,
            CyberNeon,
            MinimalPaper,
            ForestSage,
            SunsetEmber,
            CarbonSlate
        )

        fun findById(id: String): KeyboardTheme {
            return BuiltInThemes.firstOrNull { it.id == id } ?: SystemAuto
        }

        fun resolveForSystem(id: String, isSystemDark: Boolean): KeyboardTheme {
            if (id == "system_auto") {
                return if (isSystemDark) OledDark else MinimalPaper
            }
            return findById(id)
        }
    }
}
