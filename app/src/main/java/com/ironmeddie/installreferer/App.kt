package com.ironmeddie.installreferer

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        val config = AppMetricaConfig.newConfigBuilder("API_KEY").build()
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)
    }
}