package com.shakeexpense.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.ui.design.*

enum class NotificationCategory {
    BUDGET,
    UNUSUAL_SPENDING,
    RECURRING,
    FAMILY,
    SYSTEM
}

data class InAppNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val category: NotificationCategory,
    val isRead: Boolean = false,
    val isWarning: Boolean = false
)

@Composable
fun NotificationsScreen(
    notifications: List<InAppNotificationItem>,
    onBackClick: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (InAppNotificationItem) -> Unit,
    onClearNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = "Notifications",
                subtitle = "${notifications.count { !it.isRead }} unread",
                onBackClick = onBackClick,
                actions = {
                    if (notifications.any { !it.isRead }) {
                        NeuButton(
                            onClick = onMarkAllRead,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Mark all read",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        containerColor = if (isDark) ShakeDesignTokens.CanvasDark else ShakeDesignTokens.CanvasLight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Text(
                            text = "All Caught Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "You have no active financial alerts or pending reminders.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClick = { onNotificationClick(item) },
                        onDismiss = { onClearNotification(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: InAppNotificationItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val icon = when (item.category) {
        NotificationCategory.BUDGET -> Icons.Default.Warning
        NotificationCategory.UNUSUAL_SPENDING -> Icons.Default.TrendingUp
        NotificationCategory.RECURRING -> Icons.Default.Schedule
        NotificationCategory.FAMILY -> Icons.Default.Group
        NotificationCategory.SYSTEM -> Icons.Default.Info
    }

    val iconColor = when {
        item.isWarning -> ShakeDesignTokens.ExceededRed
        item.category == NotificationCategory.BUDGET -> ShakeDesignTokens.WarningAmber
        item.category == NotificationCategory.RECURRING -> ShakeDesignTokens.AccentCyan
        else -> MaterialTheme.colorScheme.primary
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = if (item.isRead) 1.dp else 2.5.dp,
        backgroundColor = if (item.isRead) null else {
            if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color(0xFFFFFFFF)
        },
        borderColor = if (item.isRead) null else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!item.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Text(
                        text = item.message,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
