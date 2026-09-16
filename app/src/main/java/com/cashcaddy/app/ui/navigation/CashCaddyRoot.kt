package com.cashcaddy.app.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashcaddy.app.data.local.entity.BudgetWithCategory
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.Appearance
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.data.model.Period
import com.cashcaddy.app.ui.MainViewModel
import com.cashcaddy.app.ui.add.AddScreen
import com.cashcaddy.app.ui.budgets.BudgetsScreen
import com.cashcaddy.app.ui.components.AccentColorSheet
import com.cashcaddy.app.ui.components.AppearanceSheet
import com.cashcaddy.app.ui.components.BudgetEditorSheet
import com.cashcaddy.app.ui.components.CashCaddyTabBar
import com.cashcaddy.app.ui.components.CategoryEditSheet
import com.cashcaddy.app.ui.components.CategoryPickerSheet
import com.cashcaddy.app.ui.components.CurrencySheet
import com.cashcaddy.app.ui.components.EditTransactionSheet
import com.cashcaddy.app.ui.components.NewCategoryDialog
import com.cashcaddy.app.ui.components.PeriodSheet
import com.cashcaddy.app.ui.home.HomeScreen
import com.cashcaddy.app.ui.insights.InsightsScreen
import com.cashcaddy.app.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CashCaddyRoot(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(AppTab.Home) }
    var period by rememberSaveable { mutableStateOf(Period.ThisMonth) }
    var showPeriod by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    var showCurrency by remember { mutableStateOf(false) }
    var showAccent by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCategoryEdit by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var editingBudget by remember { mutableStateOf<BudgetWithCategory?>(null) }
    var showBudgetEditor by remember { mutableStateOf(false) }

    var addType by rememberSaveable { mutableStateOf(MoneyType.Expense) }
    var addDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var categoryEditType by rememberSaveable { mutableStateOf(MoneyType.Expense) }

    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterOpen by rememberSaveable { mutableStateOf(false) }
    var filterCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val typedCategories = categories.filter { it.isActive && it.moneyType == addType }
    val selectedCategory = typedCategories.firstOrNull { it.id == selectedCategoryId }
        ?: typedCategories.firstOrNull()

    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.appearance) {
        Appearance.System -> systemDark
        Appearance.Light -> false
        Appearance.Dark -> true
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    AppTab.Home -> HomeScreen(
                        transactions = transactions,
                        categories = categories,
                        period = period,
                        currency = settings.currency,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchOpen = searchOpen,
                        onSearchOpenChange = { searchOpen = it; if (!it) searchQuery = "" },
                        filterCategoryId = filterCategoryId,
                        onFilterCategoryId = { filterCategoryId = it },
                        filterOpen = filterOpen,
                        onFilterOpenChange = { filterOpen = it },
                        onPeriodClick = { showPeriod = true },
                        onTransactionClick = { editingTx = it },
                    )
                    AppTab.Insights -> InsightsScreen(
                        transactions = transactions,
                        period = period,
                        currency = settings.currency,
                        darkTheme = dark,
                        onPeriodClick = { showPeriod = true },
                    )
                    AppTab.Add -> AddScreen(
                        categories = categories,
                        currency = settings.currency,
                        selectedCategory = selectedCategory,
                        type = addType,
                        onTypeChange = {
                            addType = it
                            selectedCategoryId = categories.firstOrNull { c -> c.isActive && c.moneyType == it }?.id
                        },
                        showDatePicker = showDatePicker,
                        onShowDatePicker = { showDatePicker = it },
                        date = LocalDate.parse(addDate),
                        onDateChange = { addDate = it.toString() },
                        onPickCategory = { showCategoryPicker = true },
                        onSave = { amount, title, catId, type, occurred ->
                            viewModel.addTransaction(amount, title, catId, type, occurred)
                            tab = AppTab.Home
                            scope.launch { snackbar.showSnackbar("Saved") }
                        },
                    )
                    AppTab.Budgets -> BudgetsScreen(
                        budgets = budgets,
                        transactions = transactions,
                        currency = settings.currency,
                        onAdd = {
                            editingBudget = null
                            showBudgetEditor = true
                        },
                        onEdit = {
                            editingBudget = it
                            showBudgetEditor = true
                        },
                    )
                    AppTab.Settings -> SettingsScreen(
                        settings = settings,
                        categoryCount = categories.count { it.isActive && it.moneyType == MoneyType.Expense },
                        onCurrency = { showCurrency = true },
                        onAppearance = { showAppearance = true },
                        onAccent = { showAccent = true },
                        onCategories = {
                            categoryEditType = MoneyType.Expense
                            showCategoryEdit = true
                        },
                    )
                }
                SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter))
            }
            CashCaddyTabBar(selected = tab, onSelect = { tab = it })
        }
    }

    if (showPeriod) {
        PeriodSheet(
            selected = period,
            onSelect = { period = it },
            onDismiss = { showPeriod = false },
        )
    }
    if (showAppearance) {
        AppearanceSheet(
            selected = settings.appearance,
            onSelect = { viewModel.setAppearance(it) },
            onDismiss = { showAppearance = false },
        )
    }
    if (showCurrency) {
        CurrencySheet(
            selected = settings.currency,
            onSelect = { viewModel.setCurrency(it) },
            onDismiss = { showCurrency = false },
        )
    }
    if (showAccent) {
        AccentColorSheet(
            selected = settings.accent,
            onSelect = { viewModel.setAccent(it) },
            onDismiss = { showAccent = false },
        )
    }
    if (showCategoryPicker) {
        CategoryPickerSheet(
            categories = categories,
            selectedId = selectedCategory?.id,
            type = addType,
            onSelect = { selectedCategoryId = it.id },
            onEdit = {
                showCategoryPicker = false
                categoryEditType = addType
                showCategoryEdit = true
            },
            onDismiss = { showCategoryPicker = false },
        )
    }
    if (showCategoryEdit) {
        CategoryEditSheet(
            categories = categories,
            type = categoryEditType,
            onTypeChange = { categoryEditType = it },
            onRemove = { viewModel.deleteCategory(it) },
            onActivateSuggestion = { viewModel.activateSuggestion(it) },
            onNew = { showNewCategory = true },
            onDismiss = { showCategoryEdit = false },
        )
    }
    if (showNewCategory) {
        NewCategoryDialog(
            initialType = categoryEditType,
            onDismiss = { showNewCategory = false },
            onCreate = { name, emoji, color, type ->
                viewModel.addCategory(name, emoji, color, type)
            },
        )
    }
    editingTx?.let { item ->
        EditTransactionSheet(
            item = item,
            categories = categories,
            currency = settings.currency,
            onSave = { amount, title, catId ->
                viewModel.updateTransaction(
                    item.transaction.copy(amountMinor = amount, title = title, categoryId = catId),
                )
                editingTx = null
            },
            onDelete = {
                viewModel.deleteTransaction(item.transaction)
                editingTx = null
            },
            onDismiss = { editingTx = null },
        )
    }
    if (showBudgetEditor) {
        BudgetEditorSheet(
            existing = editingBudget,
            categories = categories,
            currency = settings.currency,
            onSave = { name, catId, limit ->
                val existing = editingBudget
                if (existing == null) {
                    viewModel.addBudget(name, catId, limit, YearMonth.now().toString())
                } else {
                    viewModel.updateBudget(
                        existing.budget.copy(name = name, categoryId = catId, limitMinor = limit),
                    )
                }
                showBudgetEditor = false
                editingBudget = null
            },
            onDelete = editingBudget?.let { current ->
                {
                    viewModel.deleteBudget(current.budget)
                    showBudgetEditor = false
                    editingBudget = null
                }
            },
            onDismiss = {
                showBudgetEditor = false
                editingBudget = null
            },
        )
    }
}