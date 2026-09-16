package com.cashcaddy.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(
    existing: BudgetWithCategory?,
    categories: List<CategoryEntity>,
    currency: AppCurrency,
    onSave: (name: String, categoryId: Long, limitMinor: Long) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
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
    val selected = expenseCats.firstOrNull { it.id == categoryId }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    CashCaddySheet(onDismiss, state) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                if (existing == null) "New budget" else "Edit budget",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selected?.let { "${it.emoji}  ${it.name}" }.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
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
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly limit") },
                prefix = { Text(currency.symbol + " ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val minor = ((amountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    if (name.isNotBlank() && categoryId != 0L && minor > 0) {
                        onSave(name.trim(), categoryId, minor)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = name.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                shape = CircleShape,
            ) {
                Text("Save")
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete budget", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}