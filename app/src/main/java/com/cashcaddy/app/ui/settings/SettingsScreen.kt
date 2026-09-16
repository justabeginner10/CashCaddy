package com.cashcaddy.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashcaddy.app.data.model.UserSettings
import com.cashcaddy.app.ui.theme.isDark

@Composable
fun SettingsScreen(
    settings: UserSettings,
    categoryCount: Int,
    onCurrency: () -> Unit,
    onAppearance: () -> Unit,
    onAccent: () -> Unit,
    onCategories: () -> Unit,
) {
    val appearanceValue = when (settings.appearance) {
        com.cashcaddy.app.data.model.Appearance.System -> "System"
        com.cashcaddy.app.data.model.Appearance.Light -> "Light"
        com.cashcaddy.app.data.model.Appearance.Dark -> "Dark"
    }
    val scheme = MaterialTheme.colorScheme
    val tinted = if (scheme.isDark()) scheme.surfaceContainerHighest else scheme.primaryContainer

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Text(
            "GENERAL",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        SettingsRow(
            icon = {
                Text("₹", color = scheme.primary, style = MaterialTheme.typography.titleMedium)
            },
            iconBg = tinted,
            title = "Currency",
            subtitle = settings.currency.displayName,
            onClick = onCurrency,
        )
        SettingsRow(
            icon = {
                Icon(Icons.Outlined.Contrast, contentDescription = null, tint = scheme.primary)
            },
            iconBg = tinted,
            title = "Appearance",
            subtitle = appearanceValue,
            onClick = onAppearance,
        )
        SettingsRow(
            icon = { },
            iconBg = settings.accent.swatch,
            title = "Accent color",
            subtitle = settings.accent.label,
            onClick = onAccent,
        )
        SettingsRow(
            icon = {
                Icon(Icons.Outlined.Sell, contentDescription = null, tint = scheme.onSurfaceVariant)
            },
            iconBg = scheme.surfaceContainerHighest,
            title = "Categories",
            subtitle = "$categoryCount categories",
            onClick = onCategories,
        )
        HorizontalDivider(
            Modifier.padding(top = 12.dp, bottom = 16.dp),
            color = scheme.outlineVariant.copy(alpha = 0.6f),
        )
        Text(
            "More settings will be added gradually.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
        )
    }
}
