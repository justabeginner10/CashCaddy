package com.cashcaddy.app.data.seed

import com.cashcaddy.app.data.local.CashCaddyDatabase
import com.cashcaddy.app.data.local.entity.BudgetEntity
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionEntity
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.util.rupees
import com.cashcaddy.app.util.toEpochMillis
import java.time.LocalDate
import java.time.YearMonth

class DatabaseSeeder(private val db: CashCaddyDatabase) {

    suspend fun seedIfNeeded() {
        if (db.categoryDao().count() > 0) return
        val today = LocalDate.now()
        seed(today)
    }

    private suspend fun seed(today: LocalDate) {
        val expense = MoneyType.Expense.storage
        val income = MoneyType.Income.storage

        val categories = listOf(
            CategoryEntity(name = "Food & Drink", emoji = "🍜", colorHex = "#E8A066", type = expense, sortOrder = 0),
            CategoryEntity(name = "Transport", emoji = "🚕", colorHex = "#5B9BFF", type = expense, sortOrder = 1),
            CategoryEntity(name = "Shopping", emoji = "🛍️", colorHex = "#C084FC", type = expense, sortOrder = 2),
            CategoryEntity(name = "Groceries", emoji = "🥦", colorHex = "#4ADE80", type = expense, sortOrder = 3),
            CategoryEntity(name = "Bills", emoji = "🧾", colorHex = "#F87171", type = expense, sortOrder = 4),
            CategoryEntity(name = "Health", emoji = "💪", colorHex = "#2DD4BF", type = expense, sortOrder = 5),
            CategoryEntity(name = "Travel", emoji = "✈️", colorHex = "#60A5FA", type = expense, isActive = false, isSuggested = true, sortOrder = 6),
            CategoryEntity(name = "Fuel", emoji = "⛽", colorHex = "#FB7185", type = expense, isActive = false, isSuggested = true, sortOrder = 7),
            CategoryEntity(name = "Rent", emoji = "🏠", colorHex = "#A78BFA", type = expense, isActive = false, isSuggested = true, sortOrder = 8),
            CategoryEntity(name = "Salary", emoji = "💼", colorHex = "#34D399", type = income, sortOrder = 9),
            CategoryEntity(name = "Freelance", emoji = "💻", colorHex = "#38BDF8", type = income, sortOrder = 10),
        )
        val ids = db.categoryDao().insertAll(categories)
        val byName = categories.mapIndexed { index, entity -> entity.name to ids[index] }.toMap()

        fun cat(name: String): Long = byName.getValue(name)

        data class Tx(
            val daysAgo: Int,
            val hour: Int,
            val minute: Int,
            val title: String,
            val category: String,
            val rupees: Long,
            val type: String = expense,
        )

        fun daysAgoOn(date: LocalDate): Int =
            java.time.temporal.ChronoUnit.DAYS.between(date, today).toInt().coerceAtLeast(0)

        val rows = listOf(
            Tx(0, 9, 12, "Blue Tokai", "Food & Drink", 420),
            Tx(0, 8, 40, "Uber", "Transport", 236),
            Tx(0, 8, 5, "Zepto", "Groceries", 612),
            Tx(1, 19, 20, "BigBasket", "Groceries", 2_480),
            Tx(1, 14, 10, "Netflix", "Bills", 649),
            Tx(1, 6, 45, "Cult.fit", "Health", 1_200),
            Tx(3, 21, 5, "Zomato", "Food & Drink", 860),
            Tx(7, 11, 0, "Amazon", "Shopping", 1_899),
            Tx(daysAgoOn(today.withDayOfMonth(1)), 9, 0, "Monthly salary", "Salary", 42_000, income),
            Tx(daysAgoOn(YearMonth.from(today).minusMonths(1).atDay(12)), 10, 0, "Last month groceries", "Groceries", 3_200),
            Tx(daysAgoOn(YearMonth.from(today).minusMonths(2).atDay(10)), 18, 0, "Older coffee run", "Food & Drink", 1_150),
            Tx(daysAgoOn(YearMonth.from(today).minusMonths(5).atDay(8)), 16, 0, "Older shopping", "Shopping", 2_400),
        )

        db.transactionDao().insertAll(
            rows.map { row ->
                val date = today.minusDays(row.daysAgo.toLong())
                TransactionEntity(
                    amountMinor = rupees(row.rupees),
                    title = row.title,
                    categoryId = cat(row.category),
                    type = row.type,
                    occurredAt = date.toEpochMillis(row.hour, row.minute),
                )
            },
        )

        val ym = YearMonth.from(today).toString()
        db.budgetDao().insert(
            BudgetEntity(name = "Groceries", categoryId = cat("Groceries"), limitMinor = rupees(10_000), yearMonth = ym),
        )
        db.budgetDao().insert(
            BudgetEntity(name = "Eating out", categoryId = cat("Food & Drink"), limitMinor = rupees(8_000), yearMonth = ym),
        )
    }
}
