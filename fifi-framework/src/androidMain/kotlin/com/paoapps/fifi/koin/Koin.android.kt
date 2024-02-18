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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private const val APP_MODEL_JSON_KEY = "appModelJson"

private const val PREFERENCES_NAME = "appPrefs"

class AndroidApp<Environment: ModelEnvironment, Api: ClientApi>(
    private val context: Context
): KoinComponent {

    private val model: Model<Environment, Api> by inject()

    private var persistAppModelJobs: Map<String, Job> = mutableMapOf()

    private val appPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun setupAppModel() {
        model.dataContainers.forEach { (name, dataContainer) ->
            MainScope().launch {
                model.environmentFlow.collect { environment ->
                    dataContainer.initFromPreferences("${environment.name}-$name")
                }
            }
        }
        model.dataContainers.forEach { (dataName, dataContainer) ->
            MainScope().launch {
                combine(model.environmentFlow, dataContainer.dataFlow) { environment, data ->
                    environment
                }.collect { environment ->
                    val name = "${environment.name}-$dataName"
                    persistAppModelJobs[dataName]?.cancel()
                    persistAppModelJobs = persistAppModelJobs.plus(name to GlobalScope.launch {
                        delay(1000L)
                        dataContainer.persist(name)
                    })
                }
            }
        }
    }

    private fun CDataContainer<*>.initFromPreferences(name: String) {
        val jsonString = appPreferences.getString("${name}_$APP_MODEL_JSON_KEY", "")
        updateJson(jsonString?.ifEmpty { null }, true)
    }

    /**
     * Persists the appModel as json to preferences.
     */
    private fun CDataContainer<*>.persist(name: String) {
        appPreferences.edit()
            .putString("${name}_$APP_MODEL_JSON_KEY", json).apply()
    }

    @SuppressLint("ApplySharedPref")
    fun removeAppModel(context: Context) {
        appPreferences.edit().remove(APP_MODEL_JSON_KEY).commit()
    }

}

fun <Definition: AppDefinition<Environment, Api>, Environment: ModelEnvironment, Api: ClientApi> initKoinApp(
    context: Context,
    appDefinition: Definition,
    initialization: (Definition, List<Module>, Logger?, KoinAppDeclaration) -> KoinApplication = { definition, modules, logger, appDeclaration -> initKoinApp(definition, modules, logger, appDeclaration) },
    logger: Logger? = null,
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    val koin = initialization(
        appDefinition,
        listOf(
            module {
                single { AndroidApp<Environment, Api>(context) }
            }
        ),
        logger
    ) {
        androidContext(context)
        appDeclaration()
    }

    if (appDefinition.isDebugMode) koin.androidLogger(Level.ERROR)

    val app: AndroidApp<Environment, Api> = koin.koin.get()
    app.setupAppModel()

    return koin
}
