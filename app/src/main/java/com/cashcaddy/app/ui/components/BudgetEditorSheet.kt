package com.cashcaddy.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cashcaddy.app.data.local.entity.BudgetWithCategory
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.util.budgetSnapshot
import com.cashcaddy.app.util.isValidAmountInput
import com.cashcaddy.app.util.parseHexColor
import com.cashcaddy.app.util.parseMajorToMinor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(
    existing: BudgetWithCategory?,
    categories: List<CategoryEntity>,
    currency: AppCurrency,
    spentByCategory: Map<Long, Long>,
    onSave: (name: String, categoryId: Long, limitMinor: Long) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    val expenseCats = categories.filter { it.isActive && it.moneyType == MoneyType.Expense }
    var name by remember { mutableStateOf(existing?.budget?.name.orEmpty()) }
    var categoryId by remember {
        mutableStateOf(existing?.budget?.categoryId ?: expenseCats.firstOrNull()?.id ?: 0L)
    }
    var amountText by remember {
        mutableStateOf(
            existing?.budget?.limitMinor?.let { (it / 100L).toString() }.orEmpty(),
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val selected = expenseCats.firstOrNull { it.id == categoryId }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val previewLimit = parseMajorToMinor(amountText) ?: existing?.budget?.limitMinor
    val snapshot = previewLimit?.let { limit ->
        budgetSnapshot(
            limitMinor = limit,
            spentMinor = spentByCategory[categoryId] ?: 0L,
            today = today,
        )
    }

    CashCaddySheet(onDismiss, state) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                if (existing == null) "New budget" else "Edit budget",
                style = MaterialTheme.typography.titleLarge,
            )
            if (snapshot != null) {
                Spacer(Modifier.height(16.dp))
                BudgetStatus(
                    snapshot = snapshot,
                    currency = currency,
                    accent = parseHexColor(selected?.colorHex ?: existing?.category?.colorHex ?: "#E8A066"),
                    name = name.ifBlank { selected?.name },
                    showDailyAllowance = true,
                )
            }
            Spacer(Modifier.height(16.dp))
            SoftTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = "Name",
                label = "Name",
            )
            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                SelectorPill(
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    if (selected != null) {
                        EmojiTile(selected.emoji, selected.colorHex, size = 28.dp, corner = 8.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Category",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(selected.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        }
                    } else {
                        Text(
                            "Category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    expenseCats.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.emoji}  ${cat.name}") },
                            onClick = {
                                categoryId = cat.id
                                if (name.isBlank()) name = cat.name
                                expanded = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            SoftTextField(
                value = amountText,
                onValueChange = { incoming ->
                    val cleaned = incoming.filter { ch -> ch.isDigit() || ch == '.' }
                    if (isValidAmountInput(cleaned)) amountText = cleaned
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = "0",
                label = "Monthly limit",
                prefix = currency.symbol + " ",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val minor = parseMajorToMinor(amountText)
                    if (name.isNotBlank() && categoryId != 0L && minor != null) {
                        onSave(name.trim(), categoryId, minor)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = name.isNotBlank() && parseMajorToMinor(amountText) != null,
                shape = CircleShape,
            ) {
                Text("Save")
            }
            if (onDelete != null) {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete budget", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (confirmDelete && onDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete budget?",
            body = "“${existing?.budget?.name.orEmpty()}” will be removed. This can’t be undone.",
            onConfirm = onDelete,
            onDismiss = { confirmDelete = false },
        )
    }
}
