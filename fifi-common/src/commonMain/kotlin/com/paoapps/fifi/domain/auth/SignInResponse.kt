package com.paoapps.fifi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse<AuthenticationChallenge>(
    val tokens: Tokens? = null,
    val challenge: AuthenticationChallenge? = null,
    val session: String? = null,
    val message: String? = null,
)
