package com.paoapps.fifi.auth

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.domain.auth.Tokens

interface AuthApi {
    suspend fun refresh(refreshToken: String): FetcherResult<Tokens>
}
