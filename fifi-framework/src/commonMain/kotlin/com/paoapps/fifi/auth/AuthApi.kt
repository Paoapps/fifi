package com.paoapps.fifi.auth

import com.paoapps.fifi.api.domain.ApiResponse

interface AuthApi<ServerError> {
    suspend fun refresh(refreshToken: String): ApiResponse<Tokens, ServerError>
}
