package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MinimaCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004F5C),
    onPrimaryContainer = MinimaCyanLight,
    secondary = MinimaViolet,
    tertiary = MinimaEmerald,
    background = MinimaDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = MinimaDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MinimaDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F2937)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0891B2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF0E7490),
    secondary = Color(0xFF7C3AED),
    tertiary = Color(0xFF059669),
    background = MinimaLightBg,
    onBackground = Color(0xFF0F172A),
    surface = MinimaLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MinimaLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our sleek custom branded theme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Keyboard tools look best in sleek dark theme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
