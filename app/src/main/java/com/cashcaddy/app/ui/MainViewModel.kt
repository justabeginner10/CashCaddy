package com.cashcaddy.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cashcaddy.app.data.local.entity.BudgetEntity
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionEntity
import com.cashcaddy.app.data.model.AccentColor
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.Appearance
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.data.model.UserSettings
import com.cashcaddy.app.di.AppContainer
import com.cashcaddy.app.util.MaxAmountMinor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val container: AppContainer) : ViewModel() {

    val transactions = container.transactionRepository.observeWithCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories = container.categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val budgets = container.budgetRepository.observeWithCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun addTransaction(
        amountMinor: Long,
        title: String,
        categoryId: Long,
        type: MoneyType,
        occurredAt: Long,
    ) {
        viewModelScope.launch {
            container.transactionRepository.insert(
                TransactionEntity(
                    amountMinor = amountMinor.coerceIn(1L, MaxAmountMinor),
                    title = title.ifBlank { type.name },
                    categoryId = categoryId,
                    type = type.storage,
                    occurredAt = occurredAt,
                ),
            )
        }
    }

    fun updateTransaction(entity: TransactionEntity) {
        viewModelScope.launch { container.transactionRepository.update(entity) }
    }

    fun deleteTransaction(entity: TransactionEntity) {
        viewModelScope.launch { container.transactionRepository.delete(entity) }
    }

    fun addCategory(name: String, emoji: String, colorHex: String, type: MoneyType) {
        viewModelScope.launch {
            val maxOrder = container.categoryRepository.getAll().maxOfOrNull { it.sortOrder } ?: 0
            container.categoryRepository.insert(
                CategoryEntity(
                    name = name,
                    emoji = emoji,
                    colorHex = colorHex,
                    type = type.storage,
                    isActive = true,
                    isSuggested = false,
                    sortOrder = maxOrder + 1,
                ),
            )
        }
    }

    fun activateSuggestion(category: CategoryEntity) {
        viewModelScope.launch {
            container.categoryRepository.update(category.copy(isActive = true, isSuggested = false))
        }
    }

    fun deactivateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            container.categoryRepository.update(category.copy(isActive = false))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            if (container.categoryRepository.usageCount(category.id) == 0) {
                container.categoryRepository.delete(category)
            } else {
                container.categoryRepository.update(category.copy(isActive = false))
            }
        }
    }

    fun addBudget(name: String, categoryId: Long, limitMinor: Long, yearMonth: String) {
        viewModelScope.launch {
            container.budgetRepository.insert(
                BudgetEntity(
                    name = name,
                    categoryId = categoryId,
                    limitMinor = limitMinor.coerceIn(1L, MaxAmountMinor),
                    yearMonth = yearMonth,
                ),
            )
        }
    }

    fun updateBudget(entity: BudgetEntity) {
        viewModelScope.launch { container.budgetRepository.update(entity) }
    }

    fun deleteBudget(entity: BudgetEntity) {
        viewModelScope.launch { container.budgetRepository.delete(entity) }
    }

    fun setCurrency(currency: AppCurrency) {
        viewModelScope.launch { container.settingsRepository.setCurrency(currency) }
    }

    fun setAppearance(appearance: Appearance) {
        viewModelScope.launch { container.settingsRepository.setAppearance(appearance) }
    }

    fun setAccent(accent: AccentColor) {
        viewModelScope.launch { container.settingsRepository.setAccent(accent) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(container) as T
                }
            }
    }
}