package com.cashcaddy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashcaddy.app.ui.navigation.AppTab
import com.cashcaddy.app.ui.theme.isDark

@Composable
fun CashCaddyTabBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.background),
    ) {
        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.45f), thickness = 0.5.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabItem(
                    label = "Home",
                    selected = selected == AppTab.Home,
                    filled = Icons.Filled.Home,
                    outlined = Icons.Outlined.Home,
                    onClick = { onSelect(AppTab.Home) },
                    modifier = Modifier.weight(1f),
                )
                TabItem(
                    label = "Insights",
                    selected = selected == AppTab.Insights,
                    filled = Icons.Filled.BarChart,
                    outlined = Icons.Outlined.BarChart,
                    onClick = { onSelect(AppTab.Insights) },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(64.dp))
                TabItem(
                    label = "Budgets",
                    selected = selected == AppTab.Budgets,
                    filled = Icons.Filled.Savings,
                    outlined = Icons.Outlined.Savings,
                    onClick = { onSelect(AppTab.Budgets) },
                    modifier = Modifier.weight(1f),
                )
                TabItem(
                    label = "Settings",
                    selected = selected == AppTab.Settings,
                    filled = Icons.Filled.Settings,
                    outlined = Icons.Outlined.Settings,
                    onClick = { onSelect(AppTab.Settings) },
                    modifier = Modifier.weight(1f),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-14).dp)
                    .size(58.dp)
                    .shadow(10.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.28f), spotColor = Color.Black.copy(alpha = 0.28f))
                    .clip(CircleShape)
                    .background(scheme.primary)
                    .clickable { onSelect(AppTab.Add) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    selected: Boolean,
    filled: ImageVector,
    outlined: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.isDark()
    val interaction = remember { MutableInteractionSource() }
    val indicator = when {
        !selected -> Color.Transparent
        dark -> scheme.primary
        else -> scheme.primaryContainer
    }
    val iconTint = when {
        !selected -> scheme.onSurfaceVariant
        dark -> scheme.onPrimary
        else -> scheme.primary
    }
    val labelColor = if (selected) scheme.primary else scheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .clip(CircleShape)
                .background(indicator),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) filled else outlined,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 11.sp,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.1.sp),
        )
    }
}
