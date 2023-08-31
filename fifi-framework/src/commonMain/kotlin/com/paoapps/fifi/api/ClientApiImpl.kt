package com.paoapps.fifi.api

import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ClientApiImpl<Environment: ModelEnvironment, AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims, ServerError: Any>(
    final override val environment: Environment,
    appVersion: String,
    additionalHeaders: Map<String, String> = emptyMap(),
): KoinComponent, ClientApi<AccessTokenClaims> {

    val tokensStore: TokenStore by inject()
    val tokenDecoder: TokenDecoder<AccessTokenClaims, RefreshTokenClaims> by inject()

    val tokensFlow = MutableStateFlow(tokensStore.loadTokens(environment))
    override val claimsFlow: Flow<AccessTokenClaims?> = tokensFlow.map { it?.accessToken?.let(tokenDecoder::accessTokenClaims) }.distinctUntilChanged()

    protected val apiHelper = ApiHelper<AccessTokenClaims, RefreshTokenClaims, ServerError>(tokensFlow, environment, appVersion, additionalHeaders)
}
