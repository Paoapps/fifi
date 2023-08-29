package com.paoapps.fifi.api

import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.flow.Flow

interface ClientApi<AccessTokenClaims: IdentifiableClaims> {

    val environment: ModelEnvironment
    val claimsFlow: Flow<AccessTokenClaims?>
}
