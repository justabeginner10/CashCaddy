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
    return try {
        val cleaned = hex.trim().removePrefix("#")
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000L or value)
            8 -> Color(value)
            else -> Color(0xFF9AA0A6)
        }
    } catch (_: Exception) {
        Color(0xFF9AA0A6)
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

const val MaxAmountIntegerDigits = 12
const val MaxAmountMinor = 99_999_999_999_900L

fun isValidAmountInput(text: String): Boolean {
    if (text.isEmpty()) return true
    if (text.count { it == '.' } > 1) return false
    val integer = text.substringBefore('.')
    val decimals = if ('.' in text) text.substringAfter('.') else ""
    if (integer.any { !it.isDigit() } || decimals.any { !it.isDigit() }) return false
    if (integer.length > MaxAmountIntegerDigits) return false
    if (decimals.length > 2) return false
    return true
}

fun parseMajorToMinor(text: String): Long? {
    val value = text.toDoubleOrNull() ?: return null
    if (value <= 0.0) return null
    return Math.round(value * 100.0).coerceAtMost(MaxAmountMinor)
}

private fun compactToken(value: Double, suffix: String): String {
    val rounded = if (kotlin.math.abs(value % 1.0) < 0.05) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    return rounded + suffix
}

fun formatCompact(amountMinor: Long, currency: AppCurrency = AppCurrency.INR): String {
    val major = abs(amountMinor) / 100.0
    return when (currency) {
        AppCurrency.INR -> when {
            major >= 1_00_00_000 -> compactToken(major / 1_00_00_000, "Cr")
            major >= 1_00_000 -> compactToken(major / 1_00_000, "L")
            major >= 10_000 -> compactToken(major / 1_000, "k")
            else -> NumberFormat.getIntegerInstance(Locale("en", "IN")).format(major.toLong())
        }
        else -> when {
            major >= 1_000_000_000_000 -> compactToken(major / 1_000_000_000_000, "T")
            major >= 1_000_000_000 -> compactToken(major / 1_000_000_000, "B")
            major >= 1_000_000 -> compactToken(major / 1_000_000, "M")
            major >= 10_000 -> compactToken(major / 1_000, "k")
            else -> NumberFormat.getIntegerInstance(currency.locale).format(major.toLong())
        }
    }
}

private fun formatInr(major: Double, fractionDigits: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = fractionDigits
    nf.minimumFractionDigits = fractionDigits
    return "₹${nf.format(major)}"
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
    return (end.dayOfMonth - today.dayOfMonth + 1).coerceAtLeast(1)
}

fun monthPaceFraction(today: LocalDate = LocalDate.now()): Float {
    val length = YearMonth.from(today).lengthOfMonth()
    return (today.dayOfMonth.toFloat() / length.toFloat()).coerceIn(0f, 1f)
}