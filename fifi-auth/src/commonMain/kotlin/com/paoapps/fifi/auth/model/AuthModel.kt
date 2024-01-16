package com.paoapps.fifi.auth.model

import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.domain.auth.Tokens
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.flow.Flow

interface AuthModel<ModelData, MockConfig, AccessTokenClaims: IdentifiableClaims<UserId>, Environment: ModelEnvironment, UserId, Api: AuthClientApi<UserId, AccessTokenClaims>>: Model<ModelData, MockConfig, Environment, Api> {

    val userIdFlow: Flow<UserId?>

    fun storeTokens(tokens: Tokens)
}
