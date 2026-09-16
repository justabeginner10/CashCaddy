package com.cashcaddy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.util.parseHexColor

val CategoryEmojis = listOf(
    "🍜", "🍕", "☕", "🚕", "🚌", "✈️",
    "🛍️", "👕", "🥦", "🧾", "💡", "💪",
    "🎬", "🎧", "🐾", "🎁", "📚", "🏠",
)

val CategoryColors = listOf(
    "#E8A066", "#5B9BFF", "#C084FC", "#4ADE80", "#F87171", "#2DD4BF",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    type: MoneyType,
    onSelect: (CategoryEntity) -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val visible = categories.filter { it.isActive && it.moneyType == type }
    CashCaddySheet(onDismiss, state) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Category", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                EditPillButton(onClick = onEdit)
            }
            visible.forEach { category ->
                val selected = category.id == selectedId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(category); onDismiss() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmojiTile(category.emoji, category.colorHex, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (selected) {
                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditSheet(
    categories: List<CategoryEntity>,
    type: MoneyType,
    onTypeChange: (MoneyType) -> Unit,
    onRemove: (CategoryEntity) -> Unit,
    onActivateSuggestion: (CategoryEntity) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val yours = categories.filter { it.isActive && it.moneyType == type }
    val suggestions = categories.filter { it.isSuggested && !it.isActive && it.moneyType == type }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(Modifier.navigationBarsPadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Categories", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
            ) {
                item {
                    Text(
                        "YOUR CATEGORIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                items(yours, key = { it.id }) { category ->
                    CategoryEditRow(
                        category = category,
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(category.colorHex)),
                                )
                                IconButton(onClick = { onRemove(category) }) {
                                    Icon(
                                        Icons.Outlined.RemoveCircleOutline,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                    )
                }
                item {
                    Text(
                        "SUGGESTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp),
                    )
                }
                items(suggestions, key = { it.id }) { category ->
                    CategoryEditRow(
                        category = category,
                        trailing = {
                            IconButton(
                                onClick = { onActivateSuggestion(category) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add ${category.name}")
                            }
                        },
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MoneyTypeToggle(
                    selected = type,
                    onSelect = onTypeChange,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onNew,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        }
    }
}

@Composable
private fun CategoryEditRow(
    category: CategoryEntity,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiTile(category.emoji, category.colorHex, size = 40.dp)
        Spacer(Modifier.width(14.dp))
        Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        trailing()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewCategoryDialog(
    initialType: MoneyType,
    onDismiss: () -> Unit,
    onCreate: (name: String, emoji: String, colorHex: String, type: MoneyType) -> Unit,
) {
    var type by remember { mutableStateOf(initialType) }
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(CategoryEmojis.first()) }
    var color by remember { mutableStateOf(CategoryColors.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("New category", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                MoneyTypeToggle(selected = type, onSelect = { type = it })
                Spacer(Modifier.height(14.dp))
                SoftTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Category name",
                    leadingIcon = Icons.Outlined.Sell,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "EMOJI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryEmojis.forEach { item ->
                        val selected = item == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF16191D))
                                .then(
                                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                    else Modifier,
                                )
                                .clickable { emoji = item },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(item, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CategoryColors.forEach { hex ->
                        val selected = hex == color
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .then(
                                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier,
                                )
                                .clickable { color = hex },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name.trim(), emoji, color, type)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add category", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}