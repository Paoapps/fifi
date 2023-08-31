package com.paoapps.fifi.sample.android

import android.app.Application
import com.paoapps.fifi.sample.model.AppModel
import com.paoapps.fifi.sample.initApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class App: Application() {

    val appModel: AppModel by inject()

    private var persistAppModelJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        val appPreferences = AppPreferences()
        val koin = initApp(appPreferences.getDefaultEnvironment(), BuildConfig.DEBUG) {
            androidContext(this@App)
            modules(module {
                single { appPreferences }
            })
        }
        if (BuildConfig.DEBUG) koin.androidLogger(Level.ERROR)

        setupAppModel(appPreferences)
    }

    private fun setupAppModel(appPreferences: AppPreferences) {
        appModel.initFromPreferences(this, appPreferences)
        MainScope().launch {
            appModel.modelData.dataFlow.collect { modelData ->
                persistAppModelJob?.cancel()
                persistAppModelJob = GlobalScope.launch {
                    delay(1000L)
                    appModel.persist(this@App, appPreferences)
                }
            }
        }

        appModel.registerLaunch(BuildConfig.VERSION_NAME)
        val environment = appPreferences.getEnvironment(applicationContext)
        appModel.updateEnvironment(environment)
    }
}
