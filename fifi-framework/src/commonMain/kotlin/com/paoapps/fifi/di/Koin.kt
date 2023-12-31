package com.paoapps.fifi.di

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.api.TokenDecoder
import com.paoapps.fifi.auth.AuthApi
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.SettingsTokenStore
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.auth.Tokens
import com.paoapps.fifi.localization.CommonStringsProvider
import com.paoapps.fifi.localization.DefaultLanguageProvider
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelHelper
import io.ktor.client.statement.HttpResponse
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

private fun <ModelData, MockConfig, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, Api: ClientApi<AccessTokenClaims>> initKoin(serviceName: String, sharedAppModule: Module, model: () -> Model<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api>, appDeclaration: KoinAppDeclaration = {}, additionalInjections: (Module) -> (Unit)) = startKoin {
    appDeclaration()
    modules(
        sharedModule(model, additionalInjections),
        sharedAppModule,
        platformModule(serviceName)
    )
}

data class Authentication<AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims>(
    val tokenDecoder: TokenDecoder<AccessTokenClaims, RefreshTokenClaims>,
    val authApi: (scope: Scope) -> AuthApi
)

/**
 * This initializes the FiFi framework and registers the app module with Koin.
 *
 * @param serviceName The name of the service. This should be a unique identifier for your app that's used with platform specific services such as KeyChain and Secure Preferences to store tokens. We recommend using your app's bundle identifier.
 * @param sharedAppModule The Koin module with app specific services and view models.
 * @param model The model that is used by the app. This is the "main" model of the app that provides access to data, repositories and apis.
 * @param languageProvider The language provider that is used to localize the app.
 * @param stringsProvider The strings provider that is to provide common strings for the app.
 * @param serverErrorParser The server error parser that is used to parse server errors. In case your api returns server errors, you can use this to parse them and return a [Failure] with the parsed error.
 * @param appDeclaration The Koin app declaration that is used to declare additional modules.
 */
fun <ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi<IdentifiableClaims>> initKoinSharedWithoutAuthentication(
    serviceName: String,
    sharedAppModule: Module,
    model: () -> Model<ModelData, MockConfig, IdentifiableClaims, Environment, Unit, Api>,
    languageProvider: LanguageProvider = DefaultLanguageProvider(fallbackLanguageCode = "en"),
    stringsProvider: CommonStringsProvider = object : CommonStringsProvider {
        override val errorNoConnection: String
            get() = "No connection"
        override val errorUnknown: String
            get() = "Unknown error"

    },
    appDeclaration: KoinAppDeclaration = {}
) = initKoinShared<ModelData, MockConfig, Environment, Unit, IdentifiableClaims, Claims, Api>(
    serviceName,
    sharedAppModule,
    model,
    null,
    languageProvider,
    stringsProvider,
    appDeclaration
)

/**
 * This initializes the FiFi framework and registers the app module with Koin.
 *
 * @param serviceName The name of the service. This should be a unique identifier for your app that's used with platform specific services such as KeyChain and Secure Preferences to store tokens. We recommend using your app's bundle identifier.
 * @param sharedAppModule The Koin module with app specific services and view models.
 * @param model The model that is used by the app. This is the "main" model of the app that provides access to data, repositories and apis.
 * @param authentication The authentication that is used to authenticate with an Api. Omit when you don't use tokens to authenticate with an Api. Alternatively you can use [initKoinSharedWithoutAuthentication].
 * @param languageProvider The language provider that is used to localize the app.
 * @param stringsProvider The strings provider that is to provide common strings for the app.
 * @param serverErrorParser The server error parser that is used to parse server errors. In case your api returns server errors, you can use this to parse them and return a [Failure] with the parsed error.
 * @param appDeclaration The Koin app declaration that is used to declare additional modules.
 */
fun <ModelData, MockConfig, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims, Api: ClientApi<AccessTokenClaims>> initKoinShared(
    serviceName: String,
    sharedAppModule: Module,
    model: () -> Model<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api>,
    authentication: Authentication<AccessTokenClaims, RefreshTokenClaims>?,
    languageProvider: LanguageProvider = DefaultLanguageProvider(fallbackLanguageCode = "en"),
    stringsProvider: CommonStringsProvider = object : CommonStringsProvider {
        override val errorNoConnection: String
            get() = "No connection"
        override val errorUnknown: String
            get() = "Unknown error"

    },
    appDeclaration: KoinAppDeclaration = {}
) =
    initKoin(serviceName, sharedAppModule, model, appDeclaration) {
        it.single<TokenStore> { SettingsTokenStore(get(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS))) }
        it.single { authentication?.tokenDecoder ?: object: TokenDecoder<IdentifiableClaims, Claims> {
            override fun accessTokenClaims(accessToken: String): IdentifiableClaims {
                throw NotImplementedError()
            }
            override fun refreshTokenClaims(refreshToken: String): Claims? {
                throw NotImplementedError()
            }
            override fun encodeRefreshTokenClaims(claims: Claims): String {
                throw NotImplementedError()
            }
            override fun encodeAccessTokenClaims(claims: IdentifiableClaims): String {
                throw NotImplementedError()
            }
        } }
        it.single { languageProvider }
        it.single { stringsProvider }
        it.single { authentication?.authApi?.invoke(this) ?: object: AuthApi {
            override suspend fun refresh(refreshToken: String): FetcherResult<Tokens> {
                throw NotImplementedError()
            }
        } }
        it.single {
            val m: Model<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api> = get()
            ModelHelper<ModelData, AccessTokenClaims, Api>(m.apiFlow, m.modelData)
        }
    }

fun <ModelData, MockConfig, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims, Api: ClientApi<AccessTokenClaims>> sharedModule(model: () -> Model<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api>, additionalInjections: (Module) -> (Unit)): Module {
    return module {
        additionalInjections(this)

        single { model() }
    }
}
