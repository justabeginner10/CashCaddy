package com.cashcaddy.app.util

import androidx.compose.ui.graphics.Color
import com.cashcaddy.app.data.model.AppCurrency
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    val value = cleaned.toLong(16)
    return when (cleaned.length) {
        6 -> Color(0xFF000000L or value)
        8 -> Color(value)
        else -> Color(0xFF9AA0A6)
    }
}

fun formatMoney(
    amountMinor: Long,
    currency: AppCurrency,
    withSign: Boolean = false,
    signedExpenseNegative: Boolean = false,
): String {
    val major = amountMinor / 100.0
    val absMajor = abs(major)
    val fraction = if (abs(amountMinor) % 100L == 0L) 0 else 2
    val formatted = when (currency) {
        AppCurrency.INR -> formatInr(absMajor, fraction)
        AppCurrency.JPY -> {
            val nf = NumberFormat.getNumberInstance(Locale.JAPAN)
            nf.maximumFractionDigits = 0
            nf.minimumFractionDigits = 0
            currency.symbol + nf.format(absMajor)
        }
        else -> {
            val nf = NumberFormat.getCurrencyInstance(currency.locale)
            nf.currency = Currency.getInstance(currency.code)
            nf.maximumFractionDigits = fraction
            nf.minimumFractionDigits = fraction
            nf.format(absMajor)
        }
    }
    val negative = signedExpenseNegative && amountMinor > 0 || amountMinor < 0
    return when {
        withSign && amountMinor > 0 && !signedExpenseNegative -> "+$formatted"
        negative -> "−$formatted"
        else -> formatted
    }
}

private fun formatInr(major: Double, fractionDigits: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = fractionDigits
    nf.minimumFractionDigits = fractionDigits
    return "₹${nf.format(major)}"
}

fun formatCompact(amountMinor: Long): String {
    val major = abs(amountMinor) / 100.0
    return when {
        major >= 10_000 -> "${(major / 1000).toInt()}k"
        major >= 1_000 -> {
            val k = major / 1000.0
            if (k % 1.0 == 0.0) "${k.toInt()}k" else String.format(Locale.US, "%.1fk", k)
        }
        else -> NumberFormat.getIntegerInstance().format(major.toLong())
    }
}

fun rupees(amount: Long): Long = amount * 100L

fun LocalDate.toEpochMillis(hour: Int, minute: Int, zone: ZoneId = ZoneId.systemDefault()): Long =
    atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun Long.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

private val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private val dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)

fun formatTime(epochMillis: Long): String =
    epochMillis.toLocalDateTime().format(timeFmt)

fun formatDayHeader(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "TODAY"
    today.minusDays(1) -> "YESTERDAY"
    else -> date.format(dayFmt).uppercase(Locale.ENGLISH)
}

fun formatShortDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))

fun daysLeftInMonth(today: LocalDate = LocalDate.now()): Int {
    val end = YearMonth.from(today).atEndOfMonth()
    return (end.dayOfMonth - today.dayOfMonth).coerceAtLeast(0)
}

fun monthPaceFraction(today: LocalDate = LocalDate.now()): Float {
    val length = YearMonth.from(today).lengthOfMonth()
    return (today.dayOfMonth.toFloat() / length.toFloat()).coerceIn(0f, 1f)
}