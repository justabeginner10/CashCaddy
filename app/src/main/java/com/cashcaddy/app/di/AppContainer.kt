package com.cashcaddy.app.di

import android.content.Context
import androidx.room.Room
import com.cashcaddy.app.data.local.CashCaddyDatabase
import com.cashcaddy.app.data.preferences.SettingsRepository
import com.cashcaddy.app.data.repository.BudgetRepository
import com.cashcaddy.app.data.repository.CategoryRepository
import com.cashcaddy.app.data.repository.TransactionRepository
import com.cashcaddy.app.data.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: CashCaddyDatabase = Room.databaseBuilder(
        context.applicationContext,
        CashCaddyDatabase::class.java,
        "cashcaddy.db",
    ).fallbackToDestructiveMigration().build()

    val categoryRepository = CategoryRepository(database.categoryDao())
    val transactionRepository = TransactionRepository(database.transactionDao())
    val budgetRepository = BudgetRepository(database.budgetDao())
    val settingsRepository = SettingsRepository(context.applicationContext)
    val seeder = DatabaseSeeder(database)

    init {
        applicationScope.launch { seeder.seedIfNeeded() }
    }
}