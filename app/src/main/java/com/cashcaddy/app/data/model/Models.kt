package com.cashcaddy.app.data.model

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class MoneyType(val storage: String) {
    Expense("expense"),
    Income("income");

    companion object {
        fun fromStorage(value: String): MoneyType =
            entries.firstOrNull { it.storage == value } ?: Expense
    }
}

enum class Period(val sheetLabel: String, val chipLabel: String) {
    Today("Today", "today"),
    ThisWeek("This week", "this week"),
    ThisMonth("This month", "this month"),
    ThisYear("This year", "this year"),
    AllTime("All time", "all time");

    fun range(today: LocalDate): ClosedRange<LocalDate>? = when (this) {
        Today -> today..today
        ThisWeek -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            start..end
        }
        ThisMonth -> today.withDayOfMonth(1)..YearMonth.from(today).atEndOfMonth()
        ThisYear -> LocalDate.of(today.year, 1, 1)..LocalDate.of(today.year, 12, 31)
        AllTime -> null
    }

    fun previousRange(today: LocalDate): ClosedRange<LocalDate>? = when (this) {
        Today -> {
            val d = today.minusDays(1)
            d..d
        }
        ThisWeek -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)
            start..start.plusDays(6)
        }
        ThisMonth -> {
            val prev = YearMonth.from(today).minusMonths(1)
            prev.atDay(1)..prev.atEndOfMonth()
        }
        ThisYear -> {
            val y = today.year - 1
            LocalDate.of(y, 1, 1)..LocalDate.of(y, 12, 31)
        }
        AllTime -> null
    }

    fun comparisonLabel(): String = when (this) {
        Today -> "yesterday"
        ThisWeek -> "last week"
        ThisMonth -> "last month"
        ThisYear -> "last year"
        AllTime -> ""
    }

    fun periodHeadline(today: LocalDate): String = when (this) {
        Today -> today.month.name + " " + today.dayOfMonth
        ThisWeek -> "THIS WEEK"
        ThisMonth -> YearMonth.from(today).month.name + " " + today.year
        ThisYear -> today.year.toString()
        AllTime -> "ALL TIME"
    }

    fun averageLabel(): String = when (this) {
        Today -> "SPENT / HR"
        ThisWeek -> "SPENT / DAY"
        ThisMonth -> "SPENT / WK"
        ThisYear -> "SPENT / MO"
        AllTime -> "SPENT / MO"
    }
}

enum class Appearance(val storage: String, val title: String, val subtitle: String) {
    System("system", "System default", "Follow the device theme"),
    Light("light", "Light", "Always light"),
    Dark("dark", "Dark", "Always dark");

    companion object {
        fun fromStorage(value: String): Appearance =
            entries.firstOrNull { it.storage == value } ?: System
    }
}

enum class AccentColor(
    val storage: String,
    val label: String,
    val seedLight: Long,
    val seedDark: Long,
) {
    Blue("blue", "Blue", 0xFF1559C7, 0xFFA8C7FA),
    Green("green", "Green", 0xFF1B7F4E, 0xFF7FDBA8),
    Orange("orange", "Orange", 0xFFC65D00, 0xFFFFB68A),
    Purple("purple", "Purple", 0xFF6750A4, 0xFFD0BCFF),
    Teal("teal", "Teal", 0xFF006A6A, 0xFF4CDADB);

    val swatch: Color get() = Color(seedLight)

    companion object {
        fun fromStorage(value: String): AccentColor =
            entries.firstOrNull { it.storage == value } ?: Blue
    }
}

enum class AppCurrency(
    val storage: String,
    val code: String,
    val symbol: String,
    val displayName: String,
    val locale: Locale,
) {
    INR("inr", "INR", "₹", "Indian Rupee (INR)", Locale("en", "IN")),
    USD("usd", "USD", "$", "US Dollar (USD)", Locale.US),
    EUR("eur", "EUR", "€", "Euro (EUR)", Locale.GERMANY),
    GBP("gbp", "GBP", "£", "British Pound (GBP)", Locale.UK),
    JPY("jpy", "JPY", "¥", "Japanese Yen (JPY)", Locale.JAPAN);

    companion object {
        fun fromStorage(value: String): AppCurrency =
            entries.firstOrNull { it.storage == value } ?: INR
    }
}

data class UserSettings(
    val currency: AppCurrency = AppCurrency.INR,
    val appearance: Appearance = Appearance.System,
    val accent: AccentColor = AccentColor.Blue,
)