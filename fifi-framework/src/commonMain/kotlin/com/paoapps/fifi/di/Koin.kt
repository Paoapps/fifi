package com.paoapps.fifi.di

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.api.ServerErrorParser
import com.paoapps.fifi.api.TokenDecoder
import com.paoapps.fifi.auth.*
import com.paoapps.fifi.localization.CommonStringsProvider
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelHelper
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

internal expect fun platformModule(serviceName: String): Module

enum class PlatformModuleQualifier {
    ENCRYPTED_SETTINGS,
    SETTINGS
}

private fun <ModelData, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, Api: ClientApi<AccessTokenClaims>> initKoin(serviceName: String, sharedAppModule: Module, model: () -> Model<ModelData, AccessTokenClaims, Environment, UserId, Api>, appDeclaration: KoinAppDeclaration = {}, additionalInjections: (Module) -> (Unit)) = startKoin {
    appDeclaration()
    modules(
        sharedModule(model, additionalInjections),
        sharedAppModule,
        platformModule(serviceName)
    )
}

fun <ModelData, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims, ServerError, Api: ClientApi<AccessTokenClaims>> initKoinShared(
    serviceName: String,
    sharedAppModule: Module,
    model: () -> Model<ModelData, AccessTokenClaims, Environment, UserId, Api>,
    tokenDecoder: TokenDecoder<AccessTokenClaims, RefreshTokenClaims>,
    authApi: (scope: Scope) -> AuthApi<ServerError>,
    languageProvider: LanguageProvider,
    stringsProvider: CommonStringsProvider,
    serverErrorParser: ServerErrorParser<ServerError>,
    appDeclaration: KoinAppDeclaration = {}
) =
    initKoin(serviceName, sharedAppModule, model, appDeclaration) {
        it.single<TokenStore> { SettingsTokenStore(get(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS))) }
        it.single { tokenDecoder }
        it.single { languageProvider }
        it.single { stringsProvider }
        it.single { serverErrorParser }
        it.single { authApi(this) }
        it.single {
            val m: Model<ModelData, AccessTokenClaims, Environment, UserId, Api> = get()
            ModelHelper<ModelData, AccessTokenClaims, Api, ServerError>(m.apiFlow, m.modelData)
        }
    }

fun <ModelData, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, Api: ClientApi<AccessTokenClaims>> sharedModule(model: () -> Model<ModelData, AccessTokenClaims, Environment, UserId, Api>, additionalInjections: (Module) -> (Unit)): Module {
    return module {
        additionalInjections(this)

        single { model() }
    }
}
