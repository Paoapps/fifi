package com.paoapps.fifi.auth.api

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import kotlinx.coroutines.flow.Flow

interface AuthClientApi<UserId, AccessTokenClaims: IdentifiableClaims<UserId>>: ClientApi {

    val claimsFlow: Flow<AccessTokenClaims?>
}
