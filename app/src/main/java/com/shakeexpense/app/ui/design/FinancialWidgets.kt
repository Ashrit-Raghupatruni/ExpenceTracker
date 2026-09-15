package com.shakeexpense.app.ui.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun getCategoryVectorIcon(categoryName: String?): ImageVector {
    val name = categoryName?.lowercase() ?: ""
    return when {
        name.contains("food") || name.contains("dining") || name.contains("restaurant") || name.contains("cafe") || name.contains("snack") -> Icons.Default.Restaurant
        name.contains("travel") || name.contains("transport") || name.contains("fuel") || name.contains("cab") || name.contains("auto") || name.contains("metro") -> Icons.Default.DirectionsCar
        name.contains("shop") || name.contains("cloth") || name.contains("grocer") || name.contains("mart") -> Icons.Default.ShoppingBag
        name.contains("bill") || name.contains("util") || name.contains("electric") || name.contains("water") || name.contains("gas") || name.contains("wifi") -> Icons.Default.Receipt
        name.contains("entertain") || name.contains("movie") || name.contains("game") || name.contains("subscript") || name.contains("ott") -> Icons.Default.Movie
        name.contains("health") || name.contains("medic") || name.contains("doctor") || name.contains("pharm") -> Icons.Default.LocalHospital
        name.contains("edu") || name.contains("course") || name.contains("book") || name.contains("school") || name.contains("college") -> Icons.Default.School
        name.contains("income") || name.contains("salary") || name.contains("bonus") || name.contains("credit") -> Icons.Default.Paid
        name.contains("invest") || name.contains("sip") || name.contains("stock") || name.contains("mutual") -> Icons.Default.AccountBalance
        name.contains("rent") || name.contains("home") || name.contains("house") -> Icons.Default.Home
        name.contains("gym") || name.contains("fit") || name.contains("sport") -> Icons.Default.FitnessCenter
        else -> Icons.Default.Category
    }
}

@Composable
fun SafeToSpendHeroCard(
    safeTodayCents: Long,
    remainingMonthCents: Long,
    incomeCents: Long,
    spentThisMonthCents: Long,
    daysRemainingInCycle: Int,
    onConfigureClick: () -> Unit,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfigured = incomeCents > 0L
    val isSafe = safeTodayCents > 0L

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = 3.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleCollapse() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSafe) ShakeDesignTokens.HealthyGreen else ShakeDesignTokens.WarningAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAFE TO SPEND TODAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onConfigureClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Configure Budget",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onToggleCollapse, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = "Toggle Collapse",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Hero Amount Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "₹${FinancialFormatter.formatCents(safeTodayCents)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FinancialFormatter.TabularFontFamily,
                        color = if (isSafe) MaterialTheme.colorScheme.onSurface else ShakeDesignTokens.ExceededRed
                    )
                    Text(
                        text = if (isConfigured) "Safe daily runway for the remaining $daysRemainingInCycle days" else "Tap configure above to set monthly income",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))

                    val budgetRatio = if (incomeCents > 0L) {
                        (spentThisMonthCents.toFloat() / incomeCents.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ₹${FinancialFormatter.formatCents(spentThisMonthCents)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Monthly Allowance: ₹${FinancialFormatter.formatCents(remainingMonthCents)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { budgetRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                budgetRatio > 0.9f -> ShakeDesignTokens.ExceededRed
                                budgetRatio > 0.75f -> ShakeDesignTokens.WarningAmber
                                else -> ShakeDesignTokens.PrimaryIndigo
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    amountCents: Long,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = 1.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "₹${FinancialFormatter.formatCents(amountCents)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FinancialFormatter.TabularFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GlassTransactionRow(
    record: ExpenseRecordItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val isDebit = record.transactionType.equals("DEBIT", ignoreCase = true)
    val categoryIcon = getCategoryVectorIcon(record.categoryName)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = record.categoryName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.displayCategory,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!record.customName.isNullOrBlank() && !record.customName.equals(record.displayCategory, ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "· ${record.customName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = "${dateFormat.format(Date(record.timestamp))} · ${record.transactionSource.replace("MANUAL_", "")}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isDebit) "- " else "+ "}₹${FinancialFormatter.formatCents(record.amountCents)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FinancialFormatter.TabularFontFamily,
                color = if (isDebit) ShakeDesignTokens.ExceededRed else ShakeDesignTokens.HealthyGreen
            )
        }
    }
}

@Composable
fun FinancialSafetyPillCard(
    score: Int,
    primaryOpportunity: String?,
    spendingSubScore: String,
    budgetSubScore: String,
    recurringSubScore: String,
    riskSignalsSubScore: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rating = when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Healthy"
        score >= 40 -> "Fair"
        score > 0 -> "Needs Attention"
        else -> "Unrated"
    }

    val scoreColor = when {
        score >= 80 -> ShakeDesignTokens.HealthyGreen
        score >= 60 -> ShakeDesignTokens.AccentCyan
        score >= 40 -> ShakeDesignTokens.WarningAmber
        else -> ShakeDesignTokens.ExceededRed
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = scoreColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FINANCIAL SAFETY SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scoreColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$score / 100 · $rating",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (primaryOpportunity != null && primaryOpportunity.isNotBlank()) {
                Text(
                    text = primaryOpportunity,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }

            // 4 mini sub-score pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScoreSubPill("Spending", spendingSubScore, Modifier.weight(1f))
                ScoreSubPill("Budget", budgetSubScore, Modifier.weight(1f))
                ScoreSubPill("Recurring", recurringSubScore, Modifier.weight(1f))
                ScoreSubPill("Risk", riskSignalsSubScore, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScoreSubPill(
    label: String,
    score: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = score, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickActionFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = ShakeDesignTokens.PrimaryIndigo,
        shadowElevation = 6.dp,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

