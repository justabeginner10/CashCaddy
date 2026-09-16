package com.cashcaddy.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cashcaddy.app.data.model.AccentColor
import com.cashcaddy.app.data.model.AppCurrency
import com.cashcaddy.app.data.model.Appearance
import com.cashcaddy.app.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("cashcaddy_settings")

class SettingsRepository(private val context: Context) {
    private val currencyKey = stringPreferencesKey("currency")
    private val appearanceKey = stringPreferencesKey("appearance")
    private val accentKey = stringPreferencesKey("accent")

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            currency = AppCurrency.fromStorage(prefs[currencyKey] ?: AppCurrency.INR.storage),
            appearance = Appearance.fromStorage(prefs[appearanceKey] ?: Appearance.System.storage),
            accent = AccentColor.fromStorage(prefs[accentKey] ?: AccentColor.Blue.storage),
        )
    }

    suspend fun setCurrency(currency: AppCurrency) {
        context.dataStore.edit { it[currencyKey] = currency.storage }
    }

    suspend fun setAppearance(appearance: Appearance) {
        context.dataStore.edit { it[appearanceKey] = appearance.storage }
    }

    suspend fun setAccent(accent: AccentColor) {
        context.dataStore.edit { it[accentKey] = accent.storage }
    }
}