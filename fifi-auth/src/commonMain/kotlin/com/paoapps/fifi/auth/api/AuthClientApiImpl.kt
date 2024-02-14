package com.paoapps.fifi.auth.api

import com.paoapps.fifi.api.ClientApiImpl
import com.paoapps.fifi.api.jsonParser
import com.paoapps.fifi.auth.AuthApiHelper
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.TokenDecoder
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.log.ktor.logging.LogLevel
import com.paoapps.fifi.log.ktor.logging.Logger
import com.paoapps.fifi.log.ktor.logging.Logging
import com.paoapps.fifi.log.ktor.logging.SIMPLE
import com.paoapps.fifi.model.ModelEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.inject

open class AuthClientApiImpl<Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims>(
    environment: Environment,
    appVersion: String,
    additionalHeaders: Map<String, String> = emptyMap(),
    isDebugMode: Boolean,
    httpClient: HttpClient = HttpClient {
        expectSuccess = true
        install(Logging) {
            logger = Logger.SIMPLE
            level = if (isDebugMode) LogLevel.ALL else LogLevel.INFO
        }
        install(ContentNegotiation) {
            json(jsonParser)
        }
    },
): ClientApiImpl<Environment>(
    environment,
    appVersion,
    additionalHeaders,
    isDebugMode,
    httpClient
), AuthClientApi<UserId, AccessTokenClaims> {

    private val tokensStore: TokenStore by inject()
    val tokenDecoder: TokenDecoder<UserId, AccessTokenClaims, RefreshTokenClaims> by inject()

    val tokensFlow = MutableStateFlow(tokensStore.loadTokens(environment))
    override val claimsFlow: Flow<AccessTokenClaims?> = tokensFlow.map { it?.accessToken?.let(tokenDecoder::accessTokenClaims) }.distinctUntilChanged()

    val authApiHelper = AuthApiHelper<UserId, AccessTokenClaims, RefreshTokenClaims>(apiHelper, tokensFlow)
}
