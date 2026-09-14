package com.shakeexpense.app.ui.design

import androidx.compose.ui.text.font.FontFamily
import java.text.NumberFormat
import java.util.Locale

object FinancialFormatter {
    private val indianLocale = Locale("en", "IN")
    private val rupeeFormatter = NumberFormat.getNumberInstance(indianLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatRupees(rupees: Long): String {
        return rupeeFormatter.format(rupees)
    }

    fun formatCents(cents: Long): String {
        val rupees = cents / 100L
        return rupeeFormatter.format(rupees)
    }

    val TabularFontFamily = FontFamily.Monospace
}
