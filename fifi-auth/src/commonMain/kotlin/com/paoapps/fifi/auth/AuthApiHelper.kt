package com.paoapps.fifi.auth

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.blockedcache.map
import com.paoapps.fifi.api.ClientApiHelper
import com.paoapps.fifi.api.withinTryCatch
import com.paoapps.fifi.auth.domain.NotAuthenticatedException
import com.paoapps.fifi.auth.domain.UserIdChangedException
import com.paoapps.fifi.domain.auth.Tokens
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.log.warn
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

interface TokenDecoder<UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims> {
    fun accessTokenClaims(accessToken: String): AccessTokenClaims
    fun refreshTokenClaims(refreshToken: String): RefreshTokenClaims?

    fun encodeAccessTokenClaims(claims: AccessTokenClaims): String
    fun encodeRefreshTokenClaims(claims: RefreshTokenClaims): String
}

open class AuthApiHelper<UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims>(
    val apiHelper: ClientApiHelper,
    val tokensFlow: MutableStateFlow<Tokens?>,
): KoinComponent {
    val tokenDecoder: TokenDecoder<UserId, AccessTokenClaims, RefreshTokenClaims> by inject()
    val tokenStore: TokenStore by inject()

    private val refreshMutex = Mutex()

    private val authApi: AuthApi by inject {
        parametersOf(apiHelper.environment)
    }

    suspend fun <T: Any> authenticated(debug: String? = null, optional: Boolean = false, block: suspend (String?, AccessTokenClaims?) -> FetcherResult<T>): FetcherResult<T> = authenticated(debug, 0, optional, requireLock = true, block)

    suspend fun <T: Any> authenticated(debug: String? = null, recursiveCount: Int, optional: Boolean = false, requireLock: Boolean, block: suspend (String?, AccessTokenClaims?) -> FetcherResult<T>): FetcherResult<T> {
        val debugPrefix = "($recursiveCount)" + (debug?.let { "$it: " } ?: "")
        debug("${debugPrefix}Start authenticated call")
        val tokens = tokensFlow.value
        if (tokens == null) {
            if (optional) {
                return block(null, null)
            }
            debug("No tokens found, require user login")
            throw NotAuthenticatedException()
        }

        val accessTokenClaims = tokenDecoder.accessTokenClaims(tokens.accessToken)
        if (accessTokenClaims.exp < Clock.System.now()) {
            debug("${debugPrefix}Access token expired")
            val refreshToken = tokens.refreshToken
            val refreshTokenClaims = refreshToken?.let(tokenDecoder::refreshTokenClaims)
            debug("${debugPrefix}Got refreshTokenClaims: ${refreshTokenClaims}")
            if (refreshTokenClaims != null && refreshTokenClaims.exp < Clock.System.now()) {
                debug("${debugPrefix}Refresh token expired, require user login")
                tokenStore.deleteTokens(apiHelper.environment)
                throw NotAuthenticatedException()
            } else {
                debug("${debugPrefix}Try lock")
                if (!requireLock || refreshMutex.tryLock()) {
                    debug("${debugPrefix}Lock acquired")
                    if (refreshTokenClaims == null) {

                        if (requireLock) {
                            refreshMutex.unlock()
                        }

                        debug("${debugPrefix}Lock unlocked")

                        warn("${debugPrefix}Don't have refreshToken. Unable to refresh.")
                        tokenStore.deleteTokens(apiHelper.environment)
                        tokensFlow.value = null
                        return FetcherResult.Error.Message(HttpStatusCode.NotFound.description)
                    }

                    debug("${debugPrefix}Refresh tokens not expired, attempt to refresh tokens")
                    val refreshResponse = refreshTokens(refreshToken)

                    if (requireLock) {
                        refreshMutex.unlock()
                    }
                    debug("${debugPrefix}Lock unlocked")

                    if (refreshResponse is FetcherResult.Error) {
                        debug("${debugPrefix}Refresh tokens failed")
                        return refreshResponse.map()
                    }
                    debug("${debugPrefix}Access tokens refreshed")

                    return authenticated(debug, recursiveCount + 1, optional, requireLock = true, block)
                } else {
                    debug("${debugPrefix}Waiting for lock")
                    // since the mutex was locked another thread is already refreshing
                    // we wait for the other thread and then try again
                    refreshMutex.lock()

                    debug("${debugPrefix}Lock acquired")

                    try {
                        val response = authenticated(debug, recursiveCount + 1, optional, requireLock = false, block)

                        return response
                    } finally {

                        debug("${debugPrefix}Try unlock")

                        refreshMutex.unlock()

                        debug("${debugPrefix}Lock unlocked")
                    }
                }
            }
        } else {
            debug("${debugPrefix}Continue authentication with access token")

            val authenticationHeader = "Bearer ${tokens.accessToken}"
            val userId = accessTokenClaims.identifier
            val result = block(authenticationHeader, accessTokenClaims)
            if (userId != tokenDecoder.accessTokenClaims(tokens.accessToken).identifier) {
                throw UserIdChangedException()
            }
            return result
        }

    }

    private suspend fun refreshTokens(refreshToken: String) = withinTryCatch {
        val response = authApi.refresh(refreshToken)
        when(response) {
            is FetcherResult.Data -> {
                var tokens = response.value
                if (tokens.refreshToken == null) {
                    debug("Refresh token is null, use old refresh token: $refreshToken")
                    tokens = tokens.copy(refreshToken = refreshToken)
                }
                saveTokens(tokens)
            }
            is FetcherResult.Error -> {
                if (AuthApiHelper.isPermanentError(response)) {
                    deleteTokens()
                }
            }
        }
        response
    }

    fun saveTokens(tokens: Tokens) {
        tokenStore.saveTokens(tokens, apiHelper.environment)
        tokensFlow.value = tokens
    }

    fun deleteTokens() {
        tokenStore.deleteTokens(apiHelper.environment)
        tokensFlow.value = null
    }

    companion object {
        private fun isPermanentError(response: FetcherResult.Error) = when(response) {
            is FetcherResult.Error.Exception -> (response.error as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
            is FetcherResult.Error.Message -> false
        }
    }
}
