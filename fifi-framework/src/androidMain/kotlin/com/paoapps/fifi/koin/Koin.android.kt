package com.paoapps.fifi.koin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.di.AppDefinition
import com.paoapps.fifi.di.initKoinApp
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelEnvironmentFactory
import com.paoapps.fifi.model.datacontainer.CDataContainer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

private const val APP_MODEL_JSON_KEY = "appModelJson"

private const val PREFERENCES_NAME = "appPrefs"
private const val ENVIRONMENT_KEY = "environment"

private class AndroidApp<Environment: ModelEnvironment, Api: ClientApi>(
    private val context: Context
): KoinComponent {

    private val model: Model<Environment, Api> by inject()

    private var persistAppModelJobs: Map<String, Job> = mutableMapOf()

    private val appPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val modelEnvironmentFactory: ModelEnvironmentFactory<Environment> by inject()

    fun setupAppModel() {
        model.dataContainers.forEach { (name, dataContainer) ->
            dataContainer.initFromPreferences(name)
        }
        model.dataContainers.forEach { (name, dataContainer) ->
            MainScope().launch {
                dataContainer.dataFlow.collect { data ->
                    persistAppModelJobs[name]?.cancel()
                    persistAppModelJobs = persistAppModelJobs.plus(name to GlobalScope.launch {
                        delay(1000L)
                        dataContainer.persist(name)
                    })
                }
            }
        }

        val environment = getEnvironment()
        model.updateEnvironment(environment)
    }

    fun CDataContainer<*>.initFromPreferences(name: String) {
        val jsonString = appPreferences.getString("${name}_$APP_MODEL_JSON_KEY", "")
        updateJson(jsonString?.ifEmpty { null }, true)
    }

    /**
     * Persists the appModel as json to preferences.
     */
    fun CDataContainer<*>.persist(name: String) {
        appPreferences.edit()
            .putString("${name}_$APP_MODEL_JSON_KEY", json).apply()
    }

    @SuppressLint("ApplySharedPref")
    fun removeAppModel(context: Context) {
        appPreferences.edit().remove(APP_MODEL_JSON_KEY).commit()
    }

    fun setEnvironment(environment: ModelEnvironment) {
        appPreferences.edit(commit = true) {
            putString(ENVIRONMENT_KEY, environment.name)
        }
    }

    fun getEnvironment(): Environment {
        val env = appPreferences.getString(ENVIRONMENT_KEY, modelEnvironmentFactory.defaultEnvironment.name)
        try {
            return modelEnvironmentFactory.fromName(env ?: modelEnvironmentFactory.defaultEnvironment.name)
        } catch (i: Exception) {
        }
        return modelEnvironmentFactory.defaultEnvironment
    }

}

fun <Environment: ModelEnvironment, Api: ClientApi> initKoinApp(
    context: Context,
    appDefinition: AppDefinition<Environment, Api>,
    appDeclaration: KoinAppDeclaration = {}
) {
    val androidAppDefinition = object: AppDefinition<Environment, Api> by appDefinition {
        override fun additionalInjections(module: Module) {
            module.single { AndroidApp<Environment, Api>(context) }
            appDefinition.additionalInjections(module)
        }

        override fun appDeclaration(): KoinAppDeclaration {
            return {
                androidContext(context)
                this.apply(appDefinition.appDeclaration())
                appDeclaration()
            }
        }
    }
    val koin = initKoinApp(androidAppDefinition)

    if (appDefinition.isDebugMode) koin.androidLogger(Level.ERROR)

    val app: AndroidApp<Environment, Api> = koin.koin.get()
    app.setupAppModel()
}
