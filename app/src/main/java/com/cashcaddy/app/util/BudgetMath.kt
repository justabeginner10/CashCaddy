package com.cashcaddy.app.util

import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.MoneyType
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Calendar-aware budget progress. Remaining may be negative when overspent.
 * [fillFraction] is how much of the limit is still left (0 when overspent).
 * [paceFraction] is where remaining should sit for an even daily spend.
 */
data class BudgetSnapshot(
    val limitMinor: Long,
    val spentMinor: Long,
    val remainingMinor: Long,
    val percentSpent: Int,
    val fillFraction: Float,
    val paceFraction: Float,
    val daysRemaining: Int,
    val daysElapsed: Int,
    val daysInMonth: Int,
    val expectedSpentMinor: Long,
    val perDayMinor: Long,
    val overBudget: Boolean,
    val aheadOfPace: Boolean,
) {
    val remainingCaption: String
        get() = if (overBudget) "over this month" else "left this month"

    val daysLeftLabel: String
        get() = if (daysRemaining == 1) "1 day left" else "$daysRemaining days left"

    val dailyCaption: String
        get() = when {
            overBudget -> "No daily budget left"
            remainingMinor == 0L -> "Budget used up"
            daysRemaining == 1 -> "left for today"
            else -> "to last $daysRemaining days"
        }
}

fun budgetSnapshot(
    limitMinor: Long,
    spentMinor: Long,
    today: LocalDate = LocalDate.now(),
): BudgetSnapshot {
    val limit = limitMinor.coerceAtLeast(0L)
    val spent = spentMinor.coerceAtLeast(0L)
    val remaining = limit - spent
    val overBudget = remaining < 0

    val daysInMonth = YearMonth.from(today).lengthOfMonth().coerceAtLeast(1)
    val daysElapsed = today.dayOfMonth.coerceIn(1, daysInMonth)
    val daysRemaining = (daysInMonth - daysElapsed + 1).coerceAtLeast(1)
    val elapsedFraction = (daysElapsed.toFloat() / daysInMonth.toFloat()).coerceIn(0f, 1f)
    val paceFraction = (1f - elapsedFraction).coerceIn(0f, 1f)

    val percentSpent = when {
        limit <= 0L -> if (spent > 0L) 100 else 0
        spent <= 0L -> 0
        spent >= limit -> 100
        else -> ((spent * 100.0) / limit).roundToInt().coerceIn(1, 99)
    }
    val fillFraction = when {
        limit <= 0L -> if (spent > 0L) 0f else 1f
        remaining <= 0L -> 0f
        remaining >= limit -> 1f
        else -> (remaining.toDouble() / limit.toDouble()).toFloat().coerceIn(0f, 1f)
    }
    val expectedSpentMinor =
        if (limit <= 0L) 0L else (limit.toDouble() * elapsedFraction).roundToLong()
    val aheadOfPace = spent > expectedSpentMinor
    val perDayMinor = when {
        remaining <= 0L -> 0L
        else -> (remaining.toDouble() / daysRemaining.toDouble())
            .roundToLong()
            .coerceAtLeast(1L)
    }

    return BudgetSnapshot(
        limitMinor = limit,
        spentMinor = spent,
        remainingMinor = remaining,
        percentSpent = percentSpent,
        fillFraction = fillFraction,
        paceFraction = paceFraction,
        daysRemaining = daysRemaining,
        daysElapsed = daysElapsed,
        daysInMonth = daysInMonth,
        expectedSpentMinor = expectedSpentMinor,
        perDayMinor = perDayMinor,
        overBudget = overBudget,
        aheadOfPace = aheadOfPace,
    )
}

fun monthExpenseByCategory(
    transactions: List<TransactionWithCategory>,
    month: YearMonth = YearMonth.now(),
): Map<Long, Long> =
    transactions.asSequence()
        .filter { it.transaction.moneyType == MoneyType.Expense }
        .filter { YearMonth.from(it.transaction.occurredAt.toLocalDate()) == month }
        .groupBy { it.transaction.categoryId }
        .mapValues { (_, rows) -> rows.sumOf { it.transaction.amountMinor.coerceAtLeast(0L) } }
