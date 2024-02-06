package com.paoapps.fifi.auth.di

import com.paoapps.fifi.auth.AuthApi
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.SettingsTokenStore
import com.paoapps.fifi.auth.TokenDecoder
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.model.AuthModel
import com.paoapps.fifi.auth.model.AuthModelImpl
import com.paoapps.fifi.di.AppDefinition
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

internal expect fun platformInjections(serviceName: String, module: Module)

enum class PlatformModuleQualifier {
    ENCRYPTED_SETTINGS,
    SETTINGS
}

data class Authentication<UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims>(
    val tokenDecoder: TokenDecoder<UserId, AccessTokenClaims, RefreshTokenClaims>,
    val authApi: (scope: Scope) -> AuthApi
)

fun <Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims, Api: AuthClientApi<UserId, AccessTokenClaims>> initKoinApp(
    authAppDefinition: AuthAppDefinition<Environment, UserId, AccessTokenClaims, RefreshTokenClaims, Api>,
    additionalModules: List<Module> = emptyList(),
    logger: Logger? = null,
    appDeclaration: KoinAppDeclaration = {},
) = com.paoapps.fifi.di.initKoinApp(
    authAppDefinition,
    additionalModules + module {
        single<AuthModel<AccessTokenClaims, Environment, UserId, Api>> {
            val model: Model<Environment, Api> = get()
            authAppDefinition.authModel (model)
        }
        single<TokenStore> { SettingsTokenStore(get(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS))) }
        single {
            authAppDefinition.authentication.tokenDecoder
        }

        single {
            authAppDefinition.authentication.authApi.invoke(this)
        }

        platformInjections(authAppDefinition.serviceName, this)

    },
    logger,
    appDeclaration
)

interface AuthAppDefinition<Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims, Api: AuthClientApi<UserId, AccessTokenClaims>>: AppDefinition<Environment, Api> {
    val serviceName: String
    val authentication: Authentication<UserId, AccessTokenClaims, RefreshTokenClaims>

    fun authModel(model: Model<Environment, Api>): AuthModel<AccessTokenClaims, Environment, UserId, Api> {
        return AuthModelImpl(model)
    }
}
