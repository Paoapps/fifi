package com.paoapps.fifi.di

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.localization.DefaultLanguageProvider
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelHelper
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

enum class PlatformModuleQualifier {
    ENCRYPTED_SETTINGS,
    SETTINGS
}

private fun <ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi> initKoin(sharedAppModule: Module, model: () -> Model<ModelData, MockConfig, Environment, Api>, appDeclaration: KoinAppDeclaration = {}, additionalInjections: (Module) -> (Unit)) = startKoin {
    appDeclaration()
    modules(
        sharedModule(model, additionalInjections),
        sharedAppModule,
    )
}

/**
 * This initializes the FiFi framework and registers the app module with Koin.
 *
 * @param sharedAppModule The Koin module with app specific services and view models.
 * @param model The model that is used by the app. This is the "main" model of the app that provides access to data, repositories and apis.
 * @param languageProvider The language provider that is used to localize the app.
 * @param appDeclaration The Koin app declaration that is used to declare additional modules.
 */
fun <ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi> initKoinShared(
    sharedAppModule: Module,
    model: () -> Model<ModelData, MockConfig, Environment, Api>,
    languageProvider: LanguageProvider = DefaultLanguageProvider(fallbackLanguageCode = "en"),
    appDeclaration: KoinAppDeclaration = {},
    additionalInjections: (Module) -> (Unit) = {}
) =
    initKoin(sharedAppModule, model, appDeclaration) {
        additionalInjections(it)
        it.single { languageProvider }
        it.single {
            val m: Model<ModelData, MockConfig, Environment, Api> = get()
            ModelHelper<ModelData, Api>(m.apiFlow, m.modelData)
        }
    }

fun <ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi> sharedModule(model: () -> Model<ModelData, MockConfig, Environment, Api>, additionalInjections: (Module) -> (Unit)): Module {
    return module {
        additionalInjections(this)

        single { model() }
    }
}
