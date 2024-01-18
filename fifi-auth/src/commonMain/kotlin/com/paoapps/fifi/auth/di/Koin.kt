package com.paoapps.fifi.auth.di

import com.paoapps.fifi.auth.AuthApi
import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.SettingsTokenStore
import com.paoapps.fifi.auth.TokenDecoder
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.auth.model.AuthModel
import com.paoapps.fifi.di.AppDefinition
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

fun <Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims, Api: AuthClientApi<UserId, AccessTokenClaims>> initKoinShared(
    authAppDefinition: AuthAppDefinition<Environment, UserId, AccessTokenClaims, RefreshTokenClaims, Api>,
) = com.paoapps.fifi.di.initKoinApp(
    appDefinition = object : AuthAppDefinition<Environment, UserId, AccessTokenClaims, RefreshTokenClaims, Api> by authAppDefinition {
        override fun additionalInjections(module: Module) {
            authAppDefinition.additionalInjections(module)

            module.single<AuthModel<AccessTokenClaims, Environment, UserId>> { authAppDefinition.authModel }
            module.single<TokenStore> { SettingsTokenStore(get(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS))) }
            module.single {
                authentication.tokenDecoder
            }

            module.single {
                authentication.authApi.invoke(this)
            }

            platformInjections(serviceName, module)
        }
    },
)

interface AuthAppDefinition<Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims, Api: AuthClientApi<UserId, AccessTokenClaims>>: AppDefinition<Environment, Api> {
    val serviceName: String
    val authModel: AuthModel<AccessTokenClaims, Environment, UserId>
    val authentication: Authentication<UserId, AccessTokenClaims, RefreshTokenClaims>
}
