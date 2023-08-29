package com.paoapps.fifi.auth

import kotlinx.serialization.Serializable

@Serializable
data class Tokens(val accessToken: String, val refreshToken: String?)

@Serializable
data class Credentials(val username: String, val password: String)
