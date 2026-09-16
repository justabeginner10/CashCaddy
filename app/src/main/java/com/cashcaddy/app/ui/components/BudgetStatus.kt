package com.cashcaddy.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.ui.theme.ComparisonRed
import com.cashcaddy.app.util.BudgetSnapshot
import com.cashcaddy.app.util.formatMoney

@Composable
fun BudgetStatus(
    snapshot: BudgetSnapshot,
    currency: AppCurrency,
    accent: Color,
    modifier: Modifier = Modifier,
    name: String? = null,
    showDailyAllowance: Boolean = false,
    card: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val percentColor = when {
        snapshot.overBudget -> ComparisonRed
        snapshot.aheadOfPace -> Color(0xFFE8A066)
        else -> accent
    }
    val remainingColor = if (snapshot.overBudget) ComparisonRed else scheme.onSurface

    val body: @Composable () -> Unit = {
        Column(Modifier.padding(if (card) 0.dp else 16.dp)) {
            if (!name.isNullOrBlank()) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
            }
            Text(
                snapshot.daysLeftLabel,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(if (showDailyAllowance) 12.dp else 18.dp))
            Text(
                "${snapshot.percentSpent}% spent",
                style = MaterialTheme.typography.labelMedium,
                color = percentColor,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            MoneyText(
                formatMoney(snapshot.remainingMinor, currency),
                style = if (showDailyAllowance) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                color = remainingColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.fillMaxWidth(),
                minTextSize = 16.sp,
            )
            Text(
                snapshot.remainingCaption,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            BudgetPaceBar(
                fillFraction = snapshot.fillFraction,
                paceFraction = snapshot.paceFraction,
                fillColor = if (snapshot.overBudget) ComparisonRed else accent,
                modifier = Modifier.fillMaxWidth(),
            )
            if (showDailyAllowance) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "${formatMoney(snapshot.spentMinor, currency)} of ${formatMoney(snapshot.limitMinor, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                MoneyText(
                    "${formatMoney(snapshot.perDayMinor, currency)} / day",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    minTextSize = 16.sp,
                )
                Text(
                    snapshot.dailyCaption,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }

    if (card) {
        Column(modifier) { body() }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = scheme.surface,
            tonalElevation = 0.dp,
        ) { body() }
    }
}

@Composable
fun BudgetPaceBar(
    fillFraction: Float,
    paceFraction: Float,
    fillColor: Color,
    modifier: Modifier = Modifier,
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val thumbOuter = Color.White
    val thumbInner = Color(0xFF1A1C1E)
    val fill = fillFraction.coerceIn(0f, 1f)
    val pace = paceFraction.coerceIn(0f, 1f)

    Canvas(modifier.height(16.dp)) {
        val h = 8.dp.toPx()
        val y = (size.height - h) / 2f
        drawRoundRect(
            color = track,
            topLeft = Offset(0f, y),
            size = Size(size.width, h),
            cornerRadius = CornerRadius(h / 2, h / 2),
        )
        val leftW = size.width * fill
        if (leftW > 0f) {
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(0f, y),
                size = Size(leftW.coerceAtLeast(h).coerceAtMost(size.width), h),
                cornerRadius = CornerRadius(h / 2, h / 2),
            )
        }
        val thumbW = 5.dp.toPx()
        val minX = thumbW / 2f
        val maxX = size.width - thumbW / 2f
        val markerX = minX + (maxX - minX) * pace
        drawRoundRect(
            color = thumbInner,
            topLeft = Offset(markerX - thumbW / 2f, 1.dp.toPx()),
            size = Size(thumbW, size.height - 2.dp.toPx()),
            cornerRadius = CornerRadius(thumbW / 2, thumbW / 2),
        )
        drawRoundRect(
            color = thumbOuter,
            topLeft = Offset(markerX - thumbW / 2f + 0.8.dp.toPx(), 2.dp.toPx()),
            size = Size(thumbW - 1.6.dp.toPx(), size.height - 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
    }
}
