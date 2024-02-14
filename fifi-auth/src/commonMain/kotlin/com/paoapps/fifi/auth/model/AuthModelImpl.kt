package com.paoapps.fifi.auth.model

import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.di.API_FLOW_QUALIFIER
import com.paoapps.fifi.domain.auth.Tokens
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class AuthModelImpl<AccessTokenClaims: IdentifiableClaims<UserId>, Environment: ModelEnvironment, Api: AuthClientApi<UserId, AccessTokenClaims>, UserId>(
    override val model: Model<Environment, Api>
): KoinComponent, AuthModel<AccessTokenClaims, Environment, UserId, Api> {

    private val apiFlow: Flow<Api> by inject(API_FLOW_QUALIFIER)

    override val userIdFlow: Flow<UserId?> by lazy { apiFlow.flatMapLatest { it.claimsFlow }.map { it?.id }.distinctUntilChanged() }

    override fun storeTokens(tokens: Tokens) {
        getKoin().get<TokenStore>().saveTokens(tokens, model.currentEnvironment)
    }
}
