package com.cashcaddy.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val DarkBackground = Color(0xFF0F1215)
val LightBackground = Color(0xFFF4F5F7)

val IncomeGreen = Color(0xFF5FCF8A)
val IncomeGreenContainerDark = Color(0xFF163226)
val IncomeGreenContainerLight = Color(0xFFE3F8EC)
val ExpenseRed = Color(0xFFFF8A80)
val ExpenseRedContainerDark = Color(0xFF3A1C1C)
val ExpenseRedContainerLight = Color(0xFFFFE8E6)
val ComparisonGreen = Color(0xFF5FCF8A)
val ComparisonRed = Color(0xFFFF8A80)

val DarkCard = Color(0xFF1A1C21)
val LightCard = Color(0xFFFFFFFF)
val DarkTile = Color(0xFF22262B)
val LightTile = Color(0xFFF0F1F3)

val DarkChartMuted = Color(0xFF3A4452)
val LightChartMuted = Color(0xFFC5D0DE)

fun ColorScheme.isDark(): Boolean = background.luminance() < 0.4f
