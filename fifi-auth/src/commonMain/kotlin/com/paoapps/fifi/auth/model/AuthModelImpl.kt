package com.paoapps.fifi.auth.model

import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.domain.auth.Tokens
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

abstract class AuthModelImpl<ModelData, MockConfig, AccessTokenClaims: IdentifiableClaims<UserId>, Environment: ModelEnvironment, Api: AuthClientApi<UserId, AccessTokenClaims>, UserId>(
    scope: CoroutineScope,
    environment: Environment,
): ModelImpl<ModelData, MockConfig, Environment, Api>(scope, environment), AuthModel<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api> {

    override val userIdFlow: Flow<UserId?> by lazy { apiFlow.flatMapLatest { it.claimsFlow }.map { it?.id }.distinctUntilChanged() }

    override fun storeTokens(tokens: Tokens) {
        getKoin().get<TokenStore>().saveTokens(tokens, environmentStateFlow.value)
    }
}
