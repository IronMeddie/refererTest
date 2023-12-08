package com.ironmeddie.installreferer

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        val config = AppMetricaConfig.newConfigBuilder("e7baffa4-8d92-474b-bdad-43594d753998").build()
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)
    }
}