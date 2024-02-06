package com.paoapps.fifi.di

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.localization.DefaultLanguageProvider
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelEnvironmentFactory
import com.paoapps.fifi.model.ModelHelper
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.model.datacontainer.DataProcessor
import com.paoapps.fifi.model.datacontainer.wrap
import kotlinx.serialization.KSerializer
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private fun <Environment: ModelEnvironment, Api: ClientApi> initKoin(
    appVersion: String,
    modules: List<Module>,
    model: (String) -> Model<Environment, Api>,
    logger: Logger? = null,
    appDeclaration: KoinAppDeclaration = {},
) = startKoin {
    logger?.let { logger(it) }
    appDeclaration()
    modules(
        sharedModule(appVersion, model) + modules
    )
}

interface PersistentDataRegistry {

    fun <T: Any> registerPersistentData(
        name: String,
        serializer: KSerializer<T>,
        initialData: T,
        dataPreProcessors: List<DataProcessor<T>> = emptyList(),
    )

    fun <T: Any, E : Enum<E>> registerPersistentData(
        enum: E,
        serializer: KSerializer<T>,
        initialData: T,
        dataPreProcessors: List<DataProcessor<T>> = emptyList(),
    )
}

val LAUNCH_DATA_QUALIFIER = named("launchData")
internal val DATA_CONTAINERS_QUALIFIER = named("dataContainers")

/**
 * This is the main app definition that is used to initialize the FiFi framework.
 */
interface AppDefinition<Environment: ModelEnvironment, Api: ClientApi> {
    /**
     * The version of the app.
     */
    val appVersion: String

    /**
     * Whether the app is in debug mode or not.
     */
    val isDebugMode: Boolean

    /**
     * The factory that is used to create the environment.
     */
    val environmentFactory: ModelEnvironmentFactory<Environment>

    /**
     * The koin modules that are shared between the app and the model.
     */
    val modules: List<Module>

    /**
     * The Koin app declaration that is used to declare additional modules.
     */
    fun appDeclaration(): KoinAppDeclaration = {}

    /**
     * The model that is used by the app. This is the "main" model of the app that provides access to data, repositories and apis.
     */
    fun model(appVersion: String, environment: Environment): Model<Environment, Api>

    /**
     * The data registrations that are used to register persistent data.
     */
    fun dataRegistrations(): PersistentDataRegistry.() -> Unit = {}

    /**
     * The language provider that is used to localize the app.
     */
    fun languageProvider(): LanguageProvider = DefaultLanguageProvider(fallbackLanguageCode = "en")
}

/**
 * This initializes the FiFi framework and registers the app module with Koin.
 *
 * @param appDefinition The app definition that is used to initialize the FiFi framework.
 */
fun <Environment: ModelEnvironment, Api: ClientApi> initKoinApp(
    appDefinition: AppDefinition<Environment, Api>,
    additionalModules: List<Module> = emptyList(),
    logger: Logger? = null,
    appDeclaration: KoinAppDeclaration = {},
) = initKoin(
    appVersion = appDefinition.appVersion,
    modules = module {

        single { appDefinition.languageProvider() }
        single { appDefinition.environmentFactory }

        val registry = object : PersistentDataRegistry {

            val dataContainersQualifiers = mutableSetOf<Qualifier>()

            fun <T: Any> registerPersistentData(
                qualifier: Qualifier,
                serializer: KSerializer<T>,
                initialData: T,
                dataPreProcessors: List<DataProcessor<T>>
            ) {
                single(qualifier) {
                    val m: Model<Environment, Api> = get()
                    val dataContainer = m.registerPersistentData(qualifier.value, serializer, initialData, dataPreProcessors)
                    ModelHelper(qualifier.value, m.apiFlow, dataContainer)
                }

                dataContainersQualifiers.add(qualifier)
            }

            override fun <T: Any> registerPersistentData(
                name: String,
                serializer: KSerializer<T>,
                initialData: T,
                dataPreProcessors: List<DataProcessor<T>>
            ) {
                registerPersistentData(named(name), serializer, initialData, dataPreProcessors)
            }

            override fun <T: Any, E : Enum<E>> registerPersistentData(
                name: E,
                serializer: KSerializer<T>,
                initialData: T,
                dataPreProcessors: List<DataProcessor<T>>
            ) {
                registerPersistentData(name.qualifier, serializer, initialData, dataPreProcessors)
            }
        }

        registry.registerPersistentData(
            qualifier = LAUNCH_DATA_QUALIFIER,
            serializer = LaunchData.serializer(),
            initialData = LaunchData(currentAppVersion = appDefinition.appVersion),
            dataPreProcessors = listOf(object: DataProcessor<LaunchData> {
                override fun process(data: LaunchData): LaunchData {
                    return data.copy(
                        isFirstLaunch = false,
                        previousAppVersion = if (data.currentAppVersion == appDefinition.appVersion) data.previousAppVersion else data.currentAppVersion,
                        currentAppVersion = appDefinition.appVersion
                    )
                }
            })
        )

        registry.apply(appDefinition.dataRegistrations())

        single(DATA_CONTAINERS_QUALIFIER) {
            val dataContainers = mutableMapOf<String, CDataContainer<*>>()

            registry.dataContainersQualifiers.forEach { qualifier ->
                val modelHelper: ModelHelper<*, Api> = get(qualifier)
                dataContainers[modelHelper.name] = modelHelper.modelDataContainer.wrap()
            }

            dataContainers
        }
    } + appDefinition.modules + additionalModules,
    model = { appDefinition.model(it, appDefinition.environmentFactory.defaultEnvironment) },
    logger = logger,
    appDeclaration = {
        this.apply(appDefinition.appDeclaration())
        appDeclaration()
    }
)

fun <Environment: ModelEnvironment, Api: ClientApi> sharedModule(
    appVersion: String,
    model: (String) -> Model<Environment, Api>,
): Module {
    return module {
        single { model(appVersion) }
    }
}
