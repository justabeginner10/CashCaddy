package com.cashcaddy.app.data.seed

import com.cashcaddy.app.data.local.CashCaddyDatabase
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.model.MoneyType

class DatabaseSeeder(private val db: CashCaddyDatabase) {

    suspend fun seedIfNeeded() {
        if (db.categoryDao().count() > 0) return
        seedCategories()
    }

    private suspend fun seedCategories() {
        val expense = MoneyType.Expense.storage
        val income = MoneyType.Income.storage
        db.categoryDao().insertAll(
            listOf(
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
            ),
        )
    }
}
