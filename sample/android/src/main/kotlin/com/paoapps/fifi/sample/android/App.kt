package com.paoapps.fifi.sample.android

import android.app.Application
import com.paoapps.fifi.koin.initKoinApp
import com.paoapps.fifi.sample.SharedAppDefinition

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoinApp(
            context = this,
            appDefinition = SharedAppDefinition(
                appVersion = BuildConfig.VERSION_NAME,
                isDebugMode = BuildConfig.DEBUG
            )
        )
    }
}
