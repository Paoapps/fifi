package com.paoapps.fifi.auth.model

import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.domain.auth.Tokens
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.flow.Flow

interface AuthModel<AccessTokenClaims: IdentifiableClaims<UserId>, Environment: ModelEnvironment, UserId> {

    val model: Model<Environment, AuthClientApi<UserId, AccessTokenClaims>>

    val userIdFlow: Flow<UserId?>

    fun storeTokens(tokens: Tokens)
}
