package com.paoapps.fifi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class RespondToAuthChallenge<AuthenticationChallenge, AuthenticationChallengeResponse>(
    val challenge: AuthenticationChallenge,
    val challengeResponse: AuthenticationChallengeResponse,
    val session: String?
)
