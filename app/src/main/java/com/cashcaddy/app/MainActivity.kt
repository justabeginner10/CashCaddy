package com.cashcaddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashcaddy.app.ui.MainViewModel
import com.cashcaddy.app.ui.navigation.CashCaddyRoot
import com.cashcaddy.app.ui.theme.CashCaddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CashCaddyApplication).container
        setContent {
            val vm: MainViewModel = viewModel(factory = MainViewModel.factory(container))
            val settings by vm.settings.collectAsStateWithLifecycle()
            CashCaddyTheme(
                appearance = settings.appearance,
                accent = settings.accent,
            ) {
                CashCaddyRoot(viewModel = vm)
            }
        }
    }
}