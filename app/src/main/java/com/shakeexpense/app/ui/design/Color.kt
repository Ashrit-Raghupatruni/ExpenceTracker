package com.shakeexpense.app.ui.design

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ShakeDesignTokens {
    // Canvas Backgrounds
    val CanvasLight = Color(0xFFF7F9FC)
    val CanvasDark = Color(0xFF0F172A)

    // Glass Surfaces
    val GlassSurfaceLight = Color(0xB8FFFFFF)   // 72% frosted white
    val GlassSurfaceDark = Color(0xB31E293B)    // 70% frosted slate-800
    val GlassBorderLight = Color(0xD9FFFFFF)    // 85% translucent white highlight
    val GlassBorderDark = Color(0x33FFFFFF)     // 20% white highlight for dark mode

    // Neumorphic Surface & Dual Shadows
    val NeuSurfaceLight = Color(0xFFF7F9FC)
    val NeuSurfaceDark = Color(0xFF1E293B)
    val NeuShadowLight = Color(0xFFFFFFFF)
    val NeuShadowDark = Color(0x228A94A6)       // Soft graphite shadow

    // Semantic Accents
    val PrimaryIndigo = Color(0xFF4F46E5)
    val PrimaryBlue = Color(0xFF2563EB)
    val AccentCyan = Color(0xFF06B6D4)
    val HealthyGreen = Color(0xFF10B981)
    val WarningAmber = Color(0xFFF59E0B)
    val ExceededRed = Color(0xFFEF4444)

    // Neutral Typography
    val TextPrimaryLight = Color(0xFF0F172A)
    val TextSecondaryLight = Color(0xFF64748B)
    val TextPrimaryDark = Color(0xFFF8FAFC)
    val TextSecondaryDark = Color(0xFF94A3B8)

    // Gradients
    val GlassGradientLight = Brush.verticalGradient(
        colors = listOf(
            Color(0xD9FFFFFF),
            Color(0xA6FFFFFF)
        )
    )

    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4F46E5),
            Color(0xFF2563EB)
        )
    )

    val SafeRunwayGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF10B981),
            Color(0xFF06B6D4)
        )
    )
}

@androidx.compose.runtime.Composable
fun isAppDarkTheme(): Boolean {
    val bg = androidx.compose.material3.MaterialTheme.colorScheme.background
    return bg == Color(0xFF0F172A) || (bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f) < 0.5f
}
