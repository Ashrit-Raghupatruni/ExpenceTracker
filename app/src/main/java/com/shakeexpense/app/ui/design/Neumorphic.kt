package com.shakeexpense.app.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color? = null,
    contentColor: Color? = null,
    isPrimary: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = when {
        isPrimary -> ShakeDesignTokens.PrimaryIndigo
        containerColor != null -> containerColor
        isDark -> ShakeDesignTokens.NeuSurfaceDark
        else -> ShakeDesignTokens.NeuSurfaceLight
    }

    val textColor = when {
        isPrimary -> Color.White
        contentColor != null -> contentColor
        isDark -> ShakeDesignTokens.TextPrimaryDark
        else -> ShakeDesignTokens.TextPrimaryLight
    }

    val border = if (isPrimary) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    } else {
        BorderStroke(
            1.dp,
            if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight
        )
    }

    Surface(
        shape = shape,
        color = bgColor,
        contentColor = textColor,
        border = border,
        shadowElevation = if (isPrimary) 4.dp else 2.dp,
        modifier = modifier
            .clip(shape)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun NeuIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    isSelected: Boolean = false,
    tint: Color? = null,
    size: Dp = 42.dp
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = when {
        isSelected -> ShakeDesignTokens.PrimaryIndigo
        isDark -> ShakeDesignTokens.NeuSurfaceDark
        else -> ShakeDesignTokens.NeuSurfaceLight
    }

    val iconColor = when {
        isSelected -> Color.White
        tint != null -> tint
        isDark -> ShakeDesignTokens.TextPrimaryDark
        else -> ShakeDesignTokens.TextPrimaryLight
    }

    Surface(
        shape = shape,
        color = bgColor,
        border = BorderStroke(
            1.dp,
            if (isSelected) Color.White.copy(alpha = 0.2f) else if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight
        ),
        shadowElevation = if (isSelected) 3.dp else 1.5.dp,
        modifier = modifier
            .size(size)
            .clip(shape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
fun NeuSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val containerBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                val itemBg = when {
                    isSelected && isDark -> ShakeDesignTokens.NeuSurfaceDark
                    isSelected && !isDark -> Color.White
                    else -> Color.Transparent
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = itemBg,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    border = if (isSelected) BorderStroke(1.dp, if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectIndex(index) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NeuKeypadButton(
    symbol: String,
    subText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = when {
        isAccent -> ShakeDesignTokens.PrimaryIndigo
        isDark -> ShakeDesignTokens.NeuSurfaceDark
        else -> Color.White
    }

    val textColor = when {
        isAccent -> Color.White
        isDark -> ShakeDesignTokens.TextPrimaryDark
        else -> ShakeDesignTokens.TextPrimaryLight
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            1.dp,
            if (isAccent) Color.White.copy(alpha = 0.3f) else if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight
        ),
        shadowElevation = 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symbol,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FinancialFormatter.TabularFontFamily
            )
            if (subText != null) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    color = if (isAccent) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NeuPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = ShakeDesignTokens.PrimaryIndigo,
    icon: ImageVector? = null
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = when {
        isSelected -> color.copy(alpha = 0.15f)
        isDark -> ShakeDesignTokens.NeuSurfaceDark
        else -> ShakeDesignTokens.NeuSurfaceLight
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(
            1.dp,
            if (isSelected) color else if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight
        ),
        shadowElevation = if (isSelected) 1.5.dp else 0.5.dp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
