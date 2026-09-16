package com.cashcaddy.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.MoneyType
import com.cashcaddy.app.data.model.Period
import com.cashcaddy.app.ui.components.EmojiTile
import com.cashcaddy.app.ui.theme.ComparisonGreen
import com.cashcaddy.app.ui.theme.ComparisonRed
import com.cashcaddy.app.util.formatDayHeader
import com.cashcaddy.app.util.formatMoney
import com.cashcaddy.app.util.formatTime
import com.cashcaddy.app.util.toLocalDate
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    transactions: List<TransactionWithCategory>,
    categories: List<CategoryEntity>,
    period: Period,
    currency: AppCurrency,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchOpen: Boolean,
    onSearchOpenChange: (Boolean) -> Unit,
    filterCategoryId: Long?,
    onFilterCategoryId: (Long?) -> Unit,
    filterOpen: Boolean,
    onFilterOpenChange: (Boolean) -> Unit,
    onPeriodClick: () -> Unit,
    onTransactionClick: (TransactionWithCategory) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val range = period.range(today)
    val previous = period.previousRange(today)

    fun inRange(item: TransactionWithCategory, r: ClosedRange<LocalDate>?): Boolean {
        val date = item.transaction.occurredAt.toLocalDate()
        return r == null || date in r
    }

    val filtered = transactions.filter { item ->
        val matchesPeriod = inRange(item, range)
        val matchesSearch = searchQuery.isBlank() ||
            item.transaction.title.contains(searchQuery, ignoreCase = true) ||
            item.category.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = filterCategoryId == null || item.transaction.categoryId == filterCategoryId
        matchesPeriod && matchesSearch && matchesCategory
    }

    val spent = filtered.filter { it.transaction.moneyType == MoneyType.Expense }
        .sumOf { it.transaction.amountMinor }
    val prevSpent = transactions
        .filter { it.transaction.moneyType == MoneyType.Expense && inRange(it, previous) }
        .sumOf { it.transaction.amountMinor }

    val groups = filtered
        .filter { it.transaction.moneyType == MoneyType.Expense || searchQuery.isNotBlank() }
        .groupBy { it.transaction.occurredAt.toLocalDate() }
        .toSortedMap(compareByDescending { it })

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onSearchOpenChange(!searchOpen) }) {
                Icon(
                    if (searchOpen) Icons.Filled.Close else Icons.Outlined.Search,
                    contentDescription = "Search",
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onFilterOpenChange(!filterOpen) }) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = if (filterCategoryId != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        AnimatedVisibility(searchOpen) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                placeholder = { Text("Search logs") },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
            )
        }

        AnimatedVisibility(filterOpen) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filterCategoryId == null,
                    onClick = { onFilterCategoryId(null) },
                    label = { Text("All") },
                )
                categories.filter { it.isActive && it.moneyType == MoneyType.Expense }.take(5).forEach { cat ->
                    FilterChip(
                        selected = filterCategoryId == cat.id,
                        onClick = {
                            onFilterCategoryId(if (filterCategoryId == cat.id) null else cat.id)
                        },
                        label = { Text(cat.emoji + " " + cat.name) },
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Net spent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = onPeriodClick,
                    label = { Text(period.chipLabel) },
                    trailingIcon = {
                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    shape = CircleShape,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                formatMoney(spent, currency),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Normal,
            )
            if (previous != null && prevSpent > 0) {
                val delta = ((prevSpent - spent).toDouble() / prevSpent.toDouble() * 100.0).roundToInt()
                val less = delta >= 0
                val color = if (less) ComparisonGreen else ComparisonRed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Icon(
                        if (less) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${kotlin.math.abs(delta)}% ${if (less) "less" else "more"} than ${period.comparisonLabel()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                    )
                }
            }
        }

        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (searchQuery.isNotBlank()) "No matching logs" else "No spend in this period",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                groups.forEach { (date, items) ->
                    val dayTotal = items.filter { it.transaction.moneyType == MoneyType.Expense }
                        .sumOf { it.transaction.amountMinor }
                    item(key = "h-$date") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                formatDayHeader(date, today),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                formatMoney(dayTotal, currency),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(items, key = { it.transaction.id }) { item ->
                        TransactionRow(item, currency, onClick = { onTransactionClick(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    item: TransactionWithCategory,
    currency: AppCurrency,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiTile(item.category.emoji, item.category.colorHex)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.transaction.title, style = MaterialTheme.typography.titleSmall)
            Text(
                "${item.category.name} · ${formatTime(item.transaction.occurredAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            formatMoney(
                item.transaction.amountMinor,
                currency,
                withSign = item.transaction.moneyType == MoneyType.Income,
            ),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}