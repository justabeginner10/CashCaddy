package com.cashcaddy.app.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.ui.components.EmojiTile
import com.cashcaddy.app.ui.components.MoneyTypeToggle
import com.cashcaddy.app.ui.components.SelectorPill
import com.cashcaddy.app.ui.components.SoftTextField
import com.cashcaddy.app.util.formatShortDate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    categories: List<CategoryEntity>,
    currency: AppCurrency,
    onSave: (amountMinor: Long, title: String, categoryId: Long, type: MoneyType, occurredAt: Long) -> Unit,
    onPickCategory: () -> Unit,
    selectedCategory: CategoryEntity?,
    type: MoneyType,
    onTypeChange: (MoneyType) -> Unit,
    showDatePicker: Boolean,
    onShowDatePicker: (Boolean) -> Unit,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("0") }
    var note by rememberSaveable { mutableStateOf("") }
    val scheme = MaterialTheme.colorScheme

    fun press(digit: String) {
        amount = when {
            digit == "back" -> {
                if (amount.length <= 1) "0" else amount.dropLast(1)
            }
            digit == "." -> if (amount.contains('.')) amount else "$amount."
            amount == "0" -> digit
            else -> {
                val decimals = amount.substringAfter('.', missingDelimiterValue = "").length
                if (amount.contains('.') && decimals >= 2) amount else amount + digit
            }
        }
    }

    val amountMinor = parseAmount(amount)
    val canSave = amountMinor > 0 && selectedCategory != null
    val amountMuted = amount == "0"

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        MoneyTypeToggle(selected = type, onSelect = onTypeChange)

        Spacer(Modifier.weight(0.7f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                currency.symbol,
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp, bottom = 10.dp),
            )
            Text(
                displayAmount(amount),
                style = MaterialTheme.typography.displayLarge,
                color = if (amountMuted) scheme.outline else scheme.onSurface,
            )
        }

        Spacer(Modifier.weight(0.35f))
        SoftTextField(
            value = note,
            onValueChange = { note = it },
            placeholder = "Add note",
            leadingIcon = Icons.Outlined.EditNote,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(14.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SelectorPill(
                onClick = { onShowDatePicker(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "Date",
                    tint = scheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(formatShortDate(date), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
            SelectorPill(
                onClick = onPickCategory,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                if (selectedCategory != null) {
                    EmojiTile(selectedCategory.emoji, selectedCategory.colorHex, size = 22.dp, corner = 7.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        selectedCategory.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text("Category", modifier = Modifier.weight(1f), maxLines = 1)
                }
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = scheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
        Keypad(onPress = { press(it) }, modifier = Modifier.weight(1.15f))

        Button(
            onClick = {
                val occurred = date.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                onSave(
                    amountMinor,
                    note.ifBlank { selectedCategory?.name ?: type.name },
                    selectedCategory!!.id,
                    type,
                    occurred,
                )
                amount = "0"
                note = ""
            },
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(),
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (type == MoneyType.Expense) "Save expense" else "Save income", fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { onShowDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateChange(
                                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
                        }
                        onShowDatePicker(false)
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onShowDatePicker(false) }) { Text("Cancel") }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text("SELECT DATE", modifier = Modifier.padding(start = 24.dp, top = 16.dp))
                },
            )
        }
    }
}

@Composable
private fun Keypad(onPress: (String) -> Unit, modifier: Modifier = Modifier) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "back"),
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceEvenly) {
        keys.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { key ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { onPress(key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (key == "back") {
                            Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Delete", modifier = Modifier.size(22.dp))
                        } else {
                            Text(key, fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

private fun parseAmount(text: String): Long {
    val value = text.toDoubleOrNull() ?: return 0L
    return Math.round(value * 100.0)
}

private fun displayAmount(text: String): String {
    if (text.isEmpty()) return "0"
    val parts = text.split('.')
    val whole = parts[0].toLongOrNull() ?: 0L
    val grouped = java.text.NumberFormat.getIntegerInstance(java.util.Locale("en", "IN")).format(whole)
    return if (parts.size > 1) "$grouped.${parts[1]}" else grouped
}
