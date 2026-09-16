package com.cashcaddy.app.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.data.model.Period
import com.cashcaddy.app.ui.theme.ExpenseRed
import com.cashcaddy.app.ui.theme.ExpenseRedContainerDark
import com.cashcaddy.app.ui.theme.ExpenseRedContainerLight
import com.cashcaddy.app.ui.theme.IncomeGreen
import com.cashcaddy.app.ui.theme.IncomeGreenContainerDark
import com.cashcaddy.app.ui.theme.IncomeGreenContainerLight
import com.cashcaddy.app.util.formatCompact
import com.cashcaddy.app.util.formatMoney
import com.cashcaddy.app.util.parseHexColor
import com.cashcaddy.app.util.toLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun InsightsScreen(
    transactions: List<TransactionWithCategory>,
    period: Period,
    currency: AppCurrency,
    darkTheme: Boolean,
    onPeriodClick: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val range = period.range(today)
    val inPeriod = transactions.filter { item ->
        val date = item.transaction.occurredAt.toLocalDate()
        range == null || date in range
    }
    val expenses = inPeriod.filter { it.transaction.moneyType == MoneyType.Expense }
    val income = inPeriod.filter { it.transaction.moneyType == MoneyType.Income }
    val expenseTotal = expenses.sumOf { it.transaction.amountMinor }
    val incomeTotal = income.sumOf { it.transaction.amountMinor }

    val average = averageForPeriod(period, expenseTotal, today, range)
    val bars = spendBars(period, expenses, today, range)
    val breakdown = expenses
        .groupBy { it.category }
        .map { (cat, list) -> cat to list.sumOf { it.transaction.amountMinor } }
        .sortedByDescending { it.second }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Insights", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            AssistChip(
                onClick = onPeriodClick,
                label = { Text(period.chipLabel) },
                trailingIcon = {
                    Icon(Icons.Outlined.UnfoldMore, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                shape = CircleShape,
            )
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    period.periodHeadline(today),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatMoney(expenseTotal, currency, signedExpenseNegative = true),
                    style = MaterialTheme.typography.displayMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    period.averageLabel(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatMoney(average, currency),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Income",
                amount = formatMoney(incomeTotal, currency),
                icon = { Icon(Icons.Outlined.ArrowUpward, contentDescription = null, tint = IncomeGreen) },
                iconBg = if (darkTheme) IncomeGreenContainerDark else IncomeGreenContainerLight,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                title = "Expenses",
                amount = formatMoney(expenseTotal, currency),
                icon = { Icon(Icons.Outlined.ArrowDownward, contentDescription = null, tint = ExpenseRed) },
                iconBg = if (darkTheme) ExpenseRedContainerDark else ExpenseRedContainerLight,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Spend over time", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                SpendChart(bars = bars, averageMinor = average)
            }
        }

        Spacer(Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Category breakdown", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                if (breakdown.isEmpty() || expenseTotal == 0L) {
                    Text("No expenses in this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                    ) {
                        breakdown.forEach { (cat, amount) ->
                            Box(
                                Modifier
                                    .weight(amount.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(parseHexColor(cat.colorHex)),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val rows = breakdown.chunked(2)
                    rows.forEach { pair ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            pair.forEach { (cat, amount) ->
                                val pct = ((amount.toDouble() / expenseTotal) * 100).toInt()
                                Row(
                                    Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(cat.colorHex)),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(cat.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("$pct%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    icon: @Composable () -> Unit,
    iconBg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) { icon() }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SpendChart(bars: List<Pair<String, Long>>, averageMinor: Long) {
    val maxVal = max(bars.maxOfOrNull { it.second } ?: 0L, averageMinor)
    val niceMax = niceCeiling(maxVal)
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.surfaceContainerHighest
    val grid = MaterialTheme.colorScheme.outlineVariant
    val onVar = MaterialTheme.colorScheme.onSurfaceVariant
    val peak = bars.maxOfOrNull { it.second } ?: 0L

    Row(Modifier.fillMaxWidth().height(180.dp)) {
        Column(
            Modifier.fillMaxHeight().padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatCompact(niceMax), style = MaterialTheme.typography.labelSmall, color = onVar)
            Text(formatCompact(niceMax / 2), style = MaterialTheme.typography.labelSmall, color = onVar)
            Text("0", style = MaterialTheme.typography.labelSmall, color = onVar)
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Canvas(Modifier.fillMaxSize()) {
                    val n = bars.size.coerceAtLeast(1)
                    val gap = 10.dp.toPx()
                    val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(8.dp.toPx())
                    val avgY = size.height * (1f - (averageMinor.toFloat() / niceMax.toFloat()).coerceIn(0f, 1f))
                    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    drawLine(
                        color = grid,
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dash,
                        cap = StrokeCap.Round,
                    )
                    bars.forEachIndexed { index, (_, value) ->
                        val h = if (niceMax == 0L) 0f else size.height * (value.toFloat() / niceMax.toFloat())
                        val x = index * (barW + gap)
                        val color = if (value == peak && value > 0) primary else muted
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, size.height - h),
                            size = Size(barW, h.coerceAtLeast(0f)),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        )
                    }
                }
                if (averageMinor > 0 && niceMax > 0) {
                    val frac = 1f - (averageMinor.toFloat() / niceMax.toFloat()).coerceIn(0f, 1f)
                    Text(
                        formatCompact(averageMinor),
                        style = MaterialTheme.typography.labelSmall,
                        color = onVar,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = (160.dp * frac).coerceAtLeast(0.dp)),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                bars.forEach { (label, _) ->
                    Text(label, style = MaterialTheme.typography.labelSmall, color = onVar, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun niceCeiling(value: Long): Long {
    if (value <= 0L) return 100_000L
    val major = value / 100.0
    val mag = when {
        major <= 7_000 -> 7_000.0
        major <= 14_000 -> 14_000.0
        major <= 20_000 -> 20_000.0
        else -> ceil(major / 10_000.0) * 10_000.0
    }
    return (mag * 100).toLong()
}

private fun averageForPeriod(
    period: Period,
    expenseTotal: Long,
    today: LocalDate,
    range: ClosedRange<LocalDate>?,
): Long {
    if (expenseTotal == 0L) return 0L
    return when (period) {
        Period.Today -> expenseTotal / 24
        Period.ThisWeek -> expenseTotal / 7
        Period.ThisMonth -> expenseTotal / 4
        Period.ThisYear -> expenseTotal / today.monthValue.coerceAtLeast(1)
        Period.AllTime -> {
            val start = range?.start ?: today.minusMonths(6)
            val months = ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.from(today)).toInt().coerceAtLeast(1)
            expenseTotal / months
        }
    }
}

private fun spendBars(
    period: Period,
    expenses: List<TransactionWithCategory>,
    today: LocalDate,
    range: ClosedRange<LocalDate>?,
): List<Pair<String, Long>> {
    fun sumOn(predicate: (LocalDate) -> Boolean): Long =
        expenses.filter { predicate(it.transaction.occurredAt.toLocalDate()) }
            .sumOf { it.transaction.amountMinor }

    return when (period) {
        Period.Today -> (0 until 5).map { b ->
            val from = b * 5
            val to = if (b == 4) 24 else (b + 1) * 5
            "H$from" to expenses.filter {
                val dt = java.time.Instant.ofEpochMilli(it.transaction.occurredAt)
                    .atZone(java.time.ZoneId.systemDefault())
                dt.toLocalDate() == today && dt.hour in from until to
            }.sumOf { it.transaction.amountMinor }
        }
        Period.ThisWeek -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            (0 until 7).map { i ->
                val d = start.plusDays(i.toLong())
                d.dayOfWeek.name.take(2) to sumOn { it == d }
            }
        }
        Period.ThisMonth, Period.AllTime -> {
            val ym = YearMonth.from(today)
            val first = ym.atDay(1)
            val startWeek = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val buckets = (0 until 5).map { w ->
                val ws = startWeek.plusWeeks(w.toLong())
                val we = ws.plusDays(6)
                "W${w + 1}" to sumOn { date -> date in ws..we && date.month == today.month && date.year == today.year }
            }
            buckets
        }
        Period.ThisYear -> {
            (1..6).map { m ->
                val month = if (today.monthValue <= 6) m else m + 6
                val label = YearMonth.of(today.year, month.coerceIn(1, 12)).month.name.take(1)
                label to sumOn { it.year == today.year && it.monthValue == month }
            }
        }
    }
}