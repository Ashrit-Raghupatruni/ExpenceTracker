package com.shakeexpense.app.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import com.shakeexpense.app.domain.usecase.PredictionResult
import com.shakeexpense.app.ui.design.*

@Composable
fun CashFlowForecastScreen(
    financialProfile: FinancialProfileEntity?,
    recurringPayments: List<RecurringPaymentEntity>,
    predictionResult: PredictionResult?,
    currentBalanceCents: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    var selectedHorizonIndex by remember { mutableIntStateOf(0) } // 0 -> 30d, 1 -> 60d, 2 -> 90d
    val horizonDays = when (selectedHorizonIndex) {
        0 -> 30
        1 -> 60
        else -> 90
    }
    val horizonMonths = horizonDays / 30

    val monthlyIncomeCents = financialProfile?.monthlyIncomeCents ?: 0L
    val totalExpectedIncomeCents = monthlyIncomeCents * horizonMonths

    // Active recurring payment obligations per month
    val monthlyRecurringCents = recurringPayments.filter { it.isActive }.sumOf { payment ->
        when (payment.cadence.uppercase()) {
            "YEARLY", "ANNUAL" -> payment.amountCents / 12
            "WEEKLY" -> payment.amountCents * 4
            else -> payment.amountCents // Monthly
        }
    }
    val totalRecurringCents = monthlyRecurringCents * horizonMonths

    // Predicted variable expense per month
    val monthlyPredictedVariableCents = if (predictionResult is PredictionResult.Success) {
        (predictionResult.predictedNextMonthCents - monthlyRecurringCents).coerceAtLeast(0L)
    } else {
        0L
    }
    val totalPredictedVariableCents = monthlyPredictedVariableCents * horizonMonths
    val totalExpensesCents = totalRecurringCents + totalPredictedVariableCents
    val projectedNetCashFlowCents = totalExpectedIncomeCents - totalExpensesCents
    val projectedEndingBalanceCents = currentBalanceCents + projectedNetCashFlowCents

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = "Cash Flow Forecast",
                subtitle = "$horizonDays-day forward liquidity projection",
                onBackClick = onBackClick
            )
        },
        containerColor = if (isDark) ShakeDesignTokens.CanvasDark else ShakeDesignTokens.CanvasLight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Income Setup Notice if income is 0
            if (monthlyIncomeCents <= 0L) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                        borderColor = Color(0xFF3B82F6).copy(alpha = 0.4f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Monthly income is currently ₹0. Set up your monthly income in Safe-to-Spend setup to calculate an authentic forward runway.",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Horizon Switcher
            item {
                NeuSegmentedControl(
                    items = listOf("30 Days", "60 Days", "90 Days"),
                    selectedIndex = selectedHorizonIndex,
                    onSelectIndex = { selectedHorizonIndex = it }
                )
            }

            // Projected Ending Runway Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = 3.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROJECTED ENDING BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (projectedEndingBalanceCents >= 0L) ShakeDesignTokens.HealthyGreen.copy(alpha = 0.14f) else ShakeDesignTokens.ExceededRed.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = if (monthlyIncomeCents <= 0L) "Income Unset" else if (projectedNetCashFlowCents >= 0L) "Net Positive" else "Deficit Risk",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (projectedEndingBalanceCents >= 0L) ShakeDesignTokens.HealthyGreen else ShakeDesignTokens.ExceededRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "₹${FinancialFormatter.formatCents(projectedEndingBalanceCents)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FinancialFormatter.TabularFontFamily,
                            color = if (projectedEndingBalanceCents >= 0L) MaterialTheme.colorScheme.onSurface else ShakeDesignTokens.ExceededRed
                        )

                        Text(
                            text = "Net Record Balance: ₹${FinancialFormatter.formatCents(currentBalanceCents)} · Forward Net: ${if (projectedNetCashFlowCents >= 0L) "+" else ""}₹${FinancialFormatter.formatCents(projectedNetCashFlowCents)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Inflows vs Outflows Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialMetricCard(
                        title = "Expected Inflow",
                        amountCents = totalExpectedIncomeCents,
                        subtitle = "Known recurring income",
                        icon = Icons.Default.TrendingUp,
                        accentColor = ShakeDesignTokens.HealthyGreen,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialMetricCard(
                        title = "Expected Outflow",
                        amountCents = totalExpensesCents,
                        subtitle = "Bills + predicted spend",
                        icon = Icons.Default.TrendingDown,
                        accentColor = ShakeDesignTokens.ExceededRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Detailed Itemized Projections Breakdown
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Cash Flow Components",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        ForecastItemRow(
                            title = "Expected Monthly Income",
                            amountCents = totalExpectedIncomeCents,
                            isPositive = true,
                            isKnown = true,
                            subtitle = "Based on your configured profile income"
                        )

                        ForecastItemRow(
                            title = "Known Subscriptions & Bills",
                            amountCents = totalRecurringCents,
                            isPositive = false,
                            isKnown = true,
                            subtitle = "${recurringPayments.count { it.isActive }} active recurring commitments"
                        )

                        ForecastItemRow(
                            title = "Predicted Variable Expenses",
                            amountCents = totalPredictedVariableCents,
                            isPositive = false,
                            isKnown = false,
                            subtitle = if (predictionResult is PredictionResult.Success) "ML spending prediction based on recent activity" else "Historical pattern estimation"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        // Net Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Projected Net Cash Flow",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (projectedNetCashFlowCents >= 0L) "+ " else "- "}₹${FinancialFormatter.formatCents(projectedNetCashFlowCents)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FinancialFormatter.TabularFontFamily,
                                color = if (projectedNetCashFlowCents >= 0L) ShakeDesignTokens.HealthyGreen else ShakeDesignTokens.ExceededRed
                            )
                        }
                    }
                }
            }

            // Cash Flow Safety Tip
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Forecast Guidance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (projectedEndingBalanceCents >= 0L)
                                    "Your cash runway is healthy. Consider directing the surplus towards your target savings."
                                else
                                    "Projected outflows exceed expected income for this period. Review discretionary categories.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastItemRow(
    title: String,
    amountCents: Long,
    isPositive: Boolean,
    isKnown: Boolean,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = if (isKnown) "Known" else "Predicted",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${if (isPositive) "+ " else "- "}₹${FinancialFormatter.formatCents(amountCents)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FinancialFormatter.TabularFontFamily,
            color = if (isPositive) ShakeDesignTokens.HealthyGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}
