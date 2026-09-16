package com.cashcaddy.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cashcaddy.app.data.local.dao.BudgetDao
import com.cashcaddy.app.data.local.dao.CategoryDao
import com.cashcaddy.app.data.local.dao.TransactionDao
import com.cashcaddy.app.data.local.entity.BudgetEntity
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class CashCaddyDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}