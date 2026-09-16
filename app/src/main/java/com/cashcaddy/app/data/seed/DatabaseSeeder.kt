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

        // Visible Home rows match the design PDF, then extra demo spend fills Insights + Budgets.
        val rows = buildList {
            add(Tx(0, 9, 12, "Blue Tokai", "Food & Drink", 420))
            add(Tx(0, 8, 40, "Uber", "Transport", 236))
            add(Tx(0, 8, 5, "Zepto", "Groceries", 612))
            add(Tx(1, 19, 20, "BigBasket", "Groceries", 2_480))
            add(Tx(1, 14, 10, "Netflix", "Bills", 649))
            add(Tx(1, 6, 45, "Cult.fit", "Health", 1_200))
            add(Tx(7, 20, 30, "Swiggy", "Food & Drink", 680))
            add(Tx(7, 18, 0, "Apollo Pharmacy", "Health", 500))
            add(Tx(7, 13, 0, "Amazon", "Shopping", 1_899))
            add(Tx(7, 11, 0, "Electricity", "Bills", 1_000))
            add(Tx(7, 9, 15, "Metro", "Transport", 200))

            add(Tx(2, 13, 10, "Starbucks", "Food & Drink", 540))
            add(Tx(3, 21, 5, "Zomato", "Food & Drink", 860))
            add(Tx(4, 12, 40, "Lunch@Work", "Food & Drink", 350))
            add(Tx(5, 19, 45, "The Permit Room", "Food & Drink", 2_400))
            add(Tx(6, 11, 20, "Chaayos", "Food & Drink", 280))
            add(Tx(8, 20, 10, "Biryani Zone", "Food & Drink", 1_120))
            add(Tx(10, 13, 0, "Cafe Coffee Day", "Food & Drink", 310))
            add(Tx(12, 21, 30, "Ovenstory", "Food & Drink", 690))
            add(Tx(14, 12, 15, "Meal prep", "Food & Drink", 664))

            add(Tx(3, 9, 0, "Ola", "Transport", 340))
            add(Tx(5, 18, 20, "Rapido", "Transport", 90))
            add(Tx(6, 8, 10, "Namma Metro", "Transport", 60))
            add(Tx(9, 19, 0, "Uber", "Transport", 410))
            add(Tx(11, 8, 30, "FastTag", "Transport", 500))
            add(Tx(13, 17, 45, "Airport cab", "Transport", 1_311))
            add(Tx(15, 9, 10, "Auto", "Transport", 1_000))

            add(Tx(4, 16, 0, "DMart", "Groceries", 1_540))
            add(Tx(9, 11, 30, "Milk & eggs", "Groceries", 420))
            add(Tx(12, 19, 10, "Nature's Basket", "Groceries", 1_009))

            add(Tx(2, 10, 0, "Airtel", "Bills", 599))
            add(Tx(8, 9, 0, "Internet", "Bills", 1_199))
            add(Tx(11, 9, 30, "Electricity", "Bills", 1_976))

            add(Tx(4, 15, 20, "Myntra", "Shopping", 1_240))
            add(Tx(10, 16, 0, "IKEA", "Shopping", 1_327))

            add(Tx(6, 7, 30, "Health checkup", "Health", 1_809))

            val first = today.withDayOfMonth(1)
            val daysToFirst = java.time.temporal.ChronoUnit.DAYS.between(first, today).toInt().coerceAtLeast(0)
            add(Tx(daysToFirst, 9, 0, "Monthly salary", "Salary", 42_000, income))

            val lastMonth = YearMonth.from(today).minusMonths(1)
            val lastMid = lastMonth.atDay(minOf(15, lastMonth.lengthOfMonth()))
            val daysToLast = java.time.temporal.ChronoUnit.DAYS.between(lastMid, today).toInt()
            add(Tx(daysToLast, 10, 0, "Last month spend", "Food & Drink", 9_430))
            add(Tx(daysToLast + 2, 18, 0, "Last month groceries", "Groceries", 6_800))
            add(Tx(daysToLast + 4, 11, 0, "Last month bills", "Bills", 6_120))
            add(Tx(daysToLast + 5, 15, 0, "Last month shopping", "Shopping", 5_100))
            add(Tx(daysToLast + 6, 9, 0, "Last month transport", "Transport", 4_700))
            add(Tx(daysToLast + 7, 8, 0, "Last month health", "Health", 4_100))
            add(Tx(daysToLast + 1, 9, 0, "Last month salary", "Salary", 42_000, income))
        }

        val txEntities = rows.map { row ->
            val date = today.minusDays(row.daysAgo.toLong())
            TransactionEntity(
                amountMinor = rupees(row.rupees),
                title = row.title,
                categoryId = cat(row.category),
                type = row.type,
                occurredAt = date.toEpochMillis(row.hour, row.minute),
            )
        }
        db.transactionDao().insertAll(txEntities)

        val ym = YearMonth.from(today).toString()
        val budgets = listOf(
            BudgetEntity(name = "Groceries", categoryId = cat("Groceries"), limitMinor = rupees(10_000), yearMonth = ym),
            BudgetEntity(name = "Eating out", categoryId = cat("Food & Drink"), limitMinor = rupees(10_000), yearMonth = ym),
            BudgetEntity(name = "Transport", categoryId = cat("Transport"), limitMinor = rupees(5_000), yearMonth = ym),
            BudgetEntity(name = "Shopping", categoryId = cat("Shopping"), limitMinor = rupees(7_500), yearMonth = ym),
            BudgetEntity(name = "Bills", categoryId = cat("Bills"), limitMinor = rupees(6_400), yearMonth = ym),
            BudgetEntity(name = "Health", categoryId = cat("Health"), limitMinor = rupees(3_500), yearMonth = ym),
        )
        budgets.forEach { db.budgetDao().insert(it) }
    }
}