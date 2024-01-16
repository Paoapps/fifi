package com.paoapps.fifi.auth.di

import com.paoapps.fifi.auth.AuthApi
import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.SettingsTokenStore
import com.paoapps.fifi.auth.TokenDecoder
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.auth.model.AuthModel
import com.paoapps.fifi.localization.DefaultLanguageProvider
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.model.ModelEnvironment
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration

internal expect fun platformInjections(serviceName: String, module: Module)

enum class PlatformModuleQualifier {
    ENCRYPTED_SETTINGS,
    SETTINGS
}

data class Authentication<UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims>(
    val tokenDecoder: TokenDecoder<UserId, AccessTokenClaims, RefreshTokenClaims>,
    val authApi: (scope: Scope) -> AuthApi
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
fun <ModelData, MockConfig, Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims, Api: AuthClientApi<UserId, AccessTokenClaims>> initKoinShared(
    serviceName: String,
    sharedAppModule: Module,
    model: () -> AuthModel<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api>,
    authentication: Authentication<UserId, AccessTokenClaims, RefreshTokenClaims>,
    languageProvider: LanguageProvider = DefaultLanguageProvider(fallbackLanguageCode = "en"),
    appDeclaration: KoinAppDeclaration = {}
) = com.paoapps.fifi.di.initKoinShared(sharedAppModule, model, languageProvider, appDeclaration) {
    it.single<TokenStore> { SettingsTokenStore(get(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS))) }
    it.single {
        authentication.tokenDecoder
    }

    it.single {
        authentication.authApi.invoke(this)
    }

    platformInjections(serviceName, it)
}



