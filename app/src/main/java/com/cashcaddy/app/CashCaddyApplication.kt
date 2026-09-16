package com.cashcaddy.app

import android.app.Application
import com.cashcaddy.app.di.AppContainer

class CashCaddyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}