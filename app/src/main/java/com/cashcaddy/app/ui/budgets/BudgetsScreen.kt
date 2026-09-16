package com.cashcaddy.app.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.cashcaddy.app.data.local.entity.BudgetWithCategory
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.ui.components.BudgetStatus
import com.cashcaddy.app.ui.components.ItemActionsBox
import com.cashcaddy.app.util.BudgetSnapshot
import com.cashcaddy.app.util.budgetSnapshot
import com.cashcaddy.app.util.monthExpenseByCategory
import com.cashcaddy.app.util.parseHexColor
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun BudgetsScreen(
    budgets: List<BudgetWithCategory>,
    transactions: List<TransactionWithCategory>,
    currency: AppCurrency,
    onAdd: () -> Unit,
    onEdit: (BudgetWithCategory) -> Unit,
    onDelete: (BudgetWithCategory) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val spentByCategory = remember(transactions, today) {
        monthExpenseByCategory(transactions, YearMonth.from(today))
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
                    val snapshot = budgetSnapshot(
                        limitMinor = item.budget.limitMinor,
                        spentMinor = spentByCategory[item.budget.categoryId] ?: 0L,
                        today = today,
                    )
                    BudgetCard(
                        item = item,
                        snapshot = snapshot,
                        currency = currency,
                        onEdit = { onEdit(item) },
                        onDelete = { onDelete(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(
    item: BudgetWithCategory,
    snapshot: BudgetSnapshot,
    currency: AppCurrency,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ItemActionsBox(
        onOpen = onEdit,
        onDelete = onDelete,
        contentDescription = "Budget ${item.budget.name}",
        deleteTitle = "Delete budget?",
        deleteBody = "“${item.budget.name}” will be removed. This can’t be undone.",
        shape = RoundedCornerShape(22.dp),
    ) { actionModifier ->
        Surface(
            modifier = actionModifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            BudgetStatus(
                snapshot = snapshot,
                currency = currency,
                accent = parseHexColor(item.category.colorHex),
                name = item.budget.name,
                card = true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
