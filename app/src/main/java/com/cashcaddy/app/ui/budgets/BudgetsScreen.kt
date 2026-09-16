package com.cashcaddy.app.ui.budgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashcaddy.app.data.local.entity.BudgetWithCategory
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.util.daysLeftInMonth
import com.cashcaddy.app.util.formatMoney
import com.cashcaddy.app.util.monthPaceFraction
import com.cashcaddy.app.util.parseHexColor
import com.cashcaddy.app.util.toLocalDate
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun BudgetsScreen(
    budgets: List<BudgetWithCategory>,
    transactions: List<TransactionWithCategory>,
    currency: AppCurrency,
    onAdd: () -> Unit,
    onEdit: (BudgetWithCategory) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val ym = YearMonth.from(today).toString()
    val daysLeft = daysLeftInMonth(today)
    val pace = monthPaceFraction(today)
    val monthTx = transactions.filter {
        it.transaction.moneyType == MoneyType.Expense &&
            YearMonth.from(it.transaction.occurredAt.toLocalDate()).toString() == ym
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Budgets", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest, tonalElevation = 0.dp) {
                IconButton(onClick = onAdd, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add budget", modifier = Modifier.size(22.dp))
                }
            }
        }

        if (budgets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No budgets yet. Tap + to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(budgets, key = { it.budget.id }) { item ->
                    val spent = monthTx
                        .filter { it.transaction.categoryId == item.budget.categoryId }
                        .sumOf { it.transaction.amountMinor }
                    BudgetCard(
                        item = item,
                        spentMinor = spent,
                        daysLeft = daysLeft,
                        pace = pace,
                        currency = currency,
                        onClick = { onEdit(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(
    item: BudgetWithCategory,
    spentMinor: Long,
    daysLeft: Int,
    pace: Float,
    currency: AppCurrency,
    onClick: () -> Unit,
) {
    val limit = item.budget.limitMinor.coerceAtLeast(1L)
    val fraction = (spentMinor.toFloat() / limit.toFloat()).coerceIn(0f, 1.2f)
    val pct = ((spentMinor.toDouble() / limit) * 100).roundToInt().coerceAtMost(999)
    val left = (limit - spentMinor).coerceAtLeast(0L)
    val color = parseHexColor(item.category.colorHex)
    val pctColor = when {
        pct >= 90 -> parseHexColor("#F87171")
        pct >= 70 -> parseHexColor("#E8A066")
        else -> color
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.budget.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "$daysLeft days left",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "$pct% spent",
                style = MaterialTheme.typography.labelMedium,
                color = pctColor,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            Text(formatMoney(left, currency), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Normal)
            Text(
                "left this month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            val track = MaterialTheme.colorScheme.surfaceContainerHighest
            val thumbOuter = androidx.compose.ui.graphics.Color.White
            val thumbInner = androidx.compose.ui.graphics.Color(0xFF1A1C1E)
            Canvas(Modifier.fillMaxWidth().height(16.dp)) {
                val h = 8.dp.toPx()
                val y = (size.height - h) / 2f
                drawRoundRect(
                    color = track,
                    topLeft = Offset(0f, y),
                    size = Size(size.width, h),
                    cornerRadius = CornerRadius(h / 2, h / 2),
                )
                val spentW = size.width * fraction.coerceAtMost(1f)
                if (spentW > 0f) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(0f, y),
                        size = Size(spentW.coerceAtLeast(h), h),
                        cornerRadius = CornerRadius(h / 2, h / 2),
                    )
                }
                val markerX = size.width * pace.coerceIn(0.03f, 0.97f)
                val thumbW = 5.dp.toPx()
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
    }
}