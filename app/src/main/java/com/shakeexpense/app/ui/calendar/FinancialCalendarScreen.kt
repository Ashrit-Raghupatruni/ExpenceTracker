package com.shakeexpense.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.ui.design.*
import java.text.SimpleDateFormat
import java.util.*

sealed class CalendarFinancialEvent(
    val id: String,
    val title: String,
    val amountCents: Long,
    val isCredit: Boolean,
    val categoryName: String,
    val timestamp: Long
) {
    class ExpenseEvent(val record: ExpenseRecordItem) : CalendarFinancialEvent(
        id = record.expenseUuid,
        title = record.displayCategory + if (!record.customName.isNullOrBlank()) " (${record.customName})" else "",
        amountCents = record.amountCents,
        isCredit = record.transactionType.equals("CREDIT", ignoreCase = true),
        categoryName = record.categoryName,
        timestamp = record.timestamp
    )

    class RecurringEvent(val payment: RecurringPaymentEntity, scheduledDate: Long) : CalendarFinancialEvent(
        id = "rec_${payment.id}_$scheduledDate",
        title = payment.name,
        amountCents = payment.amountCents,
        isCredit = false,
        categoryName = "Subscription",
        timestamp = scheduledDate
    )
}

@Composable
fun FinancialCalendarScreen(
    records: List<ExpenseRecordItem>,
    recurringPayments: List<RecurringPaymentEntity>,
    onBackClick: () -> Unit,
    onRecordClick: (ExpenseRecordItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }

    var selectedDateMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    // Map records and recurring payments into calendar events
    val eventsByDay = remember(records, recurringPayments, currentMonthCalendar) {
        val map = mutableMapOf<Int, MutableList<CalendarFinancialEvent>>()
        val cal = Calendar.getInstance()

        // 1. Past & Present expenses
        records.forEach { record ->
            cal.timeInMillis = record.timestamp
            if (cal.get(Calendar.YEAR) == currentMonthCalendar.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == currentMonthCalendar.get(Calendar.MONTH)
            ) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(CalendarFinancialEvent.ExpenseEvent(record))
            }
        }

        // 2. Recurring payments due this month
        recurringPayments.filter { it.isActive }.forEach { payment ->
            if (payment.nextDueTimestamp > 0L) {
                cal.timeInMillis = payment.nextDueTimestamp
                if (cal.get(Calendar.YEAR) == currentMonthCalendar.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == currentMonthCalendar.get(Calendar.MONTH)
                ) {
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    map.getOrPut(day) { mutableListOf() }.add(CalendarFinancialEvent.RecurringEvent(payment, payment.nextDueTimestamp))
                }
            }
        }
        map
    }

    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val selectedDayOfMonth = if (
        selectedCal.get(Calendar.YEAR) == currentMonthCalendar.get(Calendar.YEAR) &&
        selectedCal.get(Calendar.MONTH) == currentMonthCalendar.get(Calendar.MONTH)
    ) {
        selectedCal.get(Calendar.DAY_OF_MONTH)
    } else 1

    val selectedDayEvents = eventsByDay[selectedDayOfMonth] ?: emptyList()
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonthCalendar.time)

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = "Financial Calendar",
                subtitle = "Monthly income, expenses & recurring bills",
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
            // Month Switcher & Grid Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeuIconButton(
                                    icon = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    onClick = {
                                        val newCal = (currentMonthCalendar.clone() as Calendar).apply {
                                            add(Calendar.MONTH, -1)
                                        }
                                        currentMonthCalendar = newCal
                                    },
                                    size = 32.dp
                                )
                                NeuIconButton(
                                    icon = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    onClick = {
                                        val newCal = (currentMonthCalendar.clone() as Calendar).apply {
                                            add(Calendar.MONTH, 1)
                                        }
                                        currentMonthCalendar = newCal
                                    },
                                    size = 32.dp
                                )
                            }
                        }

                        // Day of Week Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                            daysOfWeek.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Month Days Grid
                        val firstDayOfWeek = currentMonthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sun
                        val maxDaysInMonth = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val totalCells = ((firstDayOfWeek + maxDaysInMonth + 6) / 7) * 7

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (week in 0 until (totalCells / 7)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (dayOfWeek in 0..6) {
                                        val cellIndex = week * 7 + dayOfWeek
                                        val dayNumber = cellIndex - firstDayOfWeek + 1

                                        if (dayNumber in 1..maxDaysInMonth) {
                                            val isSelected = dayNumber == selectedDayOfMonth
                                            val hasEvents = eventsByDay.containsKey(dayNumber)
                                            val isToday = Calendar.getInstance().let { today ->
                                                today.get(Calendar.YEAR) == currentMonthCalendar.get(Calendar.YEAR) &&
                                                today.get(Calendar.MONTH) == currentMonthCalendar.get(Calendar.MONTH) &&
                                                today.get(Calendar.DAY_OF_MONTH) == dayNumber
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> MaterialTheme.colorScheme.primary
                                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .clickable {
                                                        val newSelectedCal = (currentMonthCalendar.clone() as Calendar).apply {
                                                            set(Calendar.DAY_OF_MONTH, dayNumber)
                                                        }
                                                        selectedDateMillis = newSelectedCal.timeInMillis
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = dayNumber.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isToday -> MaterialTheme.colorScheme.primary
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                    if (hasEvents) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Header & Events
            item {
                val selectedDateString = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(
                    (currentMonthCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, selectedDayOfMonth) }.time
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${selectedDayEvents.size} event${if (selectedDayEvents.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selectedDayEvents.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No financial activity recorded or scheduled for this date.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            } else {
                items(selectedDayEvents, key = { it.id }) { event ->
                    CalendarEventRow(
                        event = event,
                        onClick = {
                            if (event is CalendarFinancialEvent.ExpenseEvent) {
                                onRecordClick(event.record)
                            }
                        }
                    )
                }
            }

            // Upcoming Recurring / Scheduled Bills Section
            val upcomingRecurring = recurringPayments.filter { it.isActive && it.nextDueTimestamp >= System.currentTimeMillis() }
            if (upcomingRecurring.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Upcoming Recurring Obligations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(upcomingRecurring, key = { "upcoming_${it.id}" }) { payment ->
                    val dueDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(payment.nextDueTimestamp))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                                    color = ShakeDesignTokens.AccentCyan.copy(alpha = 0.12f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = ShakeDesignTokens.AccentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = payment.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Due on $dueDateStr · ${payment.cadence.lowercase().replaceFirstChar { it.uppercase() }}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "₹${FinancialFormatter.formatCents(payment.amountCents)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FinancialFormatter.TabularFontFamily,
                                color = ShakeDesignTokens.ExceededRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarEventRow(
    event: CalendarFinancialEvent,
    onClick: () -> Unit
) {
    val isDebit = !event.isCredit
    val icon = if (event is CalendarFinancialEvent.RecurringEvent) {
        Icons.Default.Schedule
    } else {
        getCategoryVectorIcon(event.categoryName)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
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
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = event.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (event is CalendarFinancialEvent.RecurringEvent) "Recurring Payment" else event.categoryName,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isDebit) "- " else "+ "}₹${FinancialFormatter.formatCents(event.amountCents)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FinancialFormatter.TabularFontFamily,
                color = if (isDebit) ShakeDesignTokens.ExceededRed else ShakeDesignTokens.HealthyGreen
            )
        }
    }
}
