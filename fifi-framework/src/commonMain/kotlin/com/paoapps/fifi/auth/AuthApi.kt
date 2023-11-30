package com.paoapps.fifi.auth

import com.paoapps.blockedcache.FetcherResult

interface AuthApi {
    suspend fun refresh(refreshToken: String): FetcherResult<Tokens>
}
