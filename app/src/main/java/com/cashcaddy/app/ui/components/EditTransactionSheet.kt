package com.cashcaddy.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.AppCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    item: TransactionWithCategory,
    categories: List<CategoryEntity>,
    currency: AppCurrency,
    onSave: (amountMinor: Long, title: String, categoryId: Long) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val major = item.transaction.amountMinor / 100.0
    var amountText by remember {
        mutableStateOf(
            if (item.transaction.amountMinor % 100L == 0L) {
                (item.transaction.amountMinor / 100L).toString()
            } else {
                String.format("%.2f", major)
            },
        )
    }
    var title by remember { mutableStateOf(item.transaction.title) }
    var categoryId by remember { mutableStateOf(item.transaction.categoryId) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == categoryId } ?: item.category
    val scheme = MaterialTheme.colorScheme
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = scheme.surfaceContainerHighest.copy(alpha = 0.4f),
        unfocusedContainerColor = scheme.surfaceContainerHighest.copy(alpha = 0.4f),
        focusedBorderColor = scheme.outlineVariant,
        unfocusedBorderColor = scheme.outlineVariant.copy(alpha = 0.6f),
    )

    CashCaddySheet(onDismiss, state) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Edit transaction", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                prefix = { Text(currency.symbol + " ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = CircleShape,
                colors = fieldColors,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
                shape = CircleShape,
                colors = fieldColors,
            )
            Spacer(Modifier.height(16.dp))
            SelectorPill(onClick = { showCategoryPicker = true }, modifier = Modifier.fillMaxWidth()) {
                EmojiTile(selected.emoji, selected.colorHex, size = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Category", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    Text(selected.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = scheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", color = scheme.error)
                }
                Button(
                    onClick = {
                        val parsed = parseMajorToMinor(amountText)
                        if (parsed != null && parsed > 0) onSave(parsed, title.trim(), categoryId)
                    },
                    enabled = parseMajorToMinor(amountText)?.let { it > 0 } == true && title.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text("Save")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            categories = categories,
            selectedId = categoryId,
            type = item.transaction.moneyType,
            onSelect = { categoryId = it.id },
            onEdit = { showCategoryPicker = false },
            onDismiss = { showCategoryPicker = false },
        )
    }
}

private fun parseMajorToMinor(text: String): Long? {
    val value = text.toDoubleOrNull() ?: return null
    return (value * 100.0).toLong()
}
