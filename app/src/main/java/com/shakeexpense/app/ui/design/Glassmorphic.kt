package com.shakeexpense.app.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val defaultBg = if (isDark) ShakeDesignTokens.GlassSurfaceDark else ShakeDesignTokens.GlassSurfaceLight
    val defaultBorder = if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight
    val finalBg = backgroundColor ?: defaultBg
    val finalBorder = borderColor ?: defaultBorder

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = finalBg
        ),
        border = BorderStroke(1.dp, finalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val defaultBg = if (isDark) ShakeDesignTokens.GlassSurfaceDark.copy(alpha = 0.5f) else ShakeDesignTokens.GlassSurfaceLight.copy(alpha = 0.6f)
    val defaultBorder = if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight
    val finalBg = backgroundColor ?: defaultBg
    val finalBorder = borderColor ?: defaultBorder

    Surface(
        shape = shape,
        color = finalBg,
        border = BorderStroke(1.dp, finalBorder),
        modifier = modifier
    ) {
        Box(content = content)
    }
}
