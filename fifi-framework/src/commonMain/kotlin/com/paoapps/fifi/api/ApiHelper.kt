package com.paoapps.fifi.api

import com.paoapps.fifi.api.domain.*
import com.paoapps.fifi.auth.*
import com.paoapps.fifi.localization.CommonStringsProvider
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.log.ktor.logging.LogLevel
import com.paoapps.fifi.log.ktor.logging.Logger
import com.paoapps.fifi.log.ktor.logging.Logging
import com.paoapps.fifi.log.ktor.logging.SIMPLE
import com.paoapps.fifi.log.warn
import com.paoapps.fifi.model.ModelEnvironment
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.serialization.DeserializationStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

expect fun getUserAgentHeader(appVersion: String): String

expect class OSConfigurationFactory() {
    fun defaultConfiguration(): OSConfiguration
}

data class OSConfiguration(
    val localIpAddress: String,
) {
    companion object {
        var configuration = OSConfigurationFactory().defaultConfiguration()
    }
}

interface TokenDecoder<AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims> {
    fun accessTokenClaims(accessToken: String): AccessTokenClaims
    fun refreshTokenClaims(refreshToken: String): RefreshTokenClaims?

    fun encodeAccessTokenClaims(claims: AccessTokenClaims): String
    fun encodeRefreshTokenClaims(claims: RefreshTokenClaims): String
}

interface ServerErrorParser<E> {
    suspend fun parseError(response: HttpResponse): E?
    suspend fun <T> toClientFailure(response: HttpResponse, error: E?, exception: Exception): Failure<T, E>
}

open class ApiHelper<AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims, ServerError : Any>(
    val tokensFlow: MutableStateFlow<Tokens?>,
    val environment: ModelEnvironment,
    private val appVersion: String,
    private val additionalHeaders: Map<String, String> = emptyMap(),
    val client: HttpClient = HttpClient {
        expectSuccess = true
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }
):
    KoinComponent {

    private val refreshMutex = Mutex()

    val tokenDecoder: TokenDecoder<AccessTokenClaims, RefreshTokenClaims> by inject()
    val tokenStore: TokenStore by inject()
    private val stringsProvider: CommonStringsProvider by inject()
    private val serverErrorParser: ServerErrorParser<ServerError> by inject()

    private val authApi: AuthApi<ServerError> by inject {
        parametersOf(environment)
    }
    private val languageProvider: LanguageProvider by inject()

    suspend fun <T> authenticated(debug: String? = null, optional: Boolean = false, block: suspend (String?, IdentifiableClaims?) -> ApiResponse<T, ServerError>): ApiResponse<T, ServerError> = authenticated(debug, 0, optional, requireLock = true, block)

    suspend fun <T> authenticated(debug: String? = null, recursiveCount: Int, optional: Boolean = false, requireLock: Boolean, block: suspend (String?, IdentifiableClaims?) -> ApiResponse<T, ServerError>): ApiResponse<T, ServerError> {
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
                tokenStore.deleteTokens(environment)
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
                        tokenStore.deleteTokens(environment)
                        tokensFlow.value = null
                        return Failure.create(HttpStatusCode.NotFound)
                    }

                    debug("${debugPrefix}Refresh tokens not expired, attempt to refresh tokens")
                    val refreshResponse = refreshTokens(refreshToken)

                    if (requireLock) {
                        refreshMutex.unlock()
                    }
                    debug("${debugPrefix}Lock unlocked")

                    if (refreshResponse is Failure) {
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
            is Success -> {
                var tokens = response.data
                if (tokens.refreshToken == null) {
                    debug("Refresh token is null, use old refresh token: $refreshToken")
                    tokens = tokens.copy(refreshToken = refreshToken)
                }
                saveTokens(tokens)
            }
            is Failure -> {
                if (isPermanentError(response)) {
                    deleteTokens()
                }
            }
        }
        response
    }

    fun saveTokens(tokens: Tokens) {
        tokenStore.saveTokens(tokens, environment)
        tokensFlow.value = tokens
    }

    fun deleteTokens() {
        tokenStore.deleteTokens(environment)
        tokensFlow.value = null
    }

    private fun headers(authHeader: String? = null): Map<String, String> {
        val mutableMap = additionalHeaders.toMutableMap()
        authHeader?.let { mutableMap["Authorization"] = it }
        languageProvider.language?.acceptLanguageHeader?.let {
            mutableMap["Accept-Language"] = it
        }
        mutableMap["User-Agent"] = getUserAgentHeader(appVersion)
        return mutableMap
    }

    fun createHeaders(httpRequestBuilder: HttpRequestBuilder, authHeader: String? = null) {
        httpRequestBuilder.headers {
            headers(authHeader).entries.forEach {
                httpRequestBuilder.header(it.key, it.value)
            }
        }
    }

    suspend fun <T> withinTryCatch(block: suspend () -> ApiResponse<T, ServerError>) =
        withinTryCatch(stringsProvider.errorNoConnection, stringsProvider.errorUnknown, serverErrorParser, block)

    companion object {
        private fun isPermanentError(response: Failure<Any, Any>) =
            response.status == HttpStatusCode.Unauthorized.value
    }
}

suspend fun <T, ServerError> withinTryCatch(noConnectionError: String, unknownError: String, serverErrorParser: ServerErrorParser<ServerError>, block: suspend () -> ApiResponse<T, ServerError>): ApiResponse<T, ServerError> {
    try {
        return block()
    } catch (exception: Throwable) {
        // TODO: allow for custom error handling/recording
//        recordException(exception)
        com.paoapps.fifi.log.error("Request failed, $exception", exception)
        return when {
            exception.message?.contains("Code=-1009") == true -> { //Is Network down, code..
                Failure(-1009, noConnectionError, FailureKind.NETWORK, exception)
            }
            exception.message?.contains("Code=-1004") == true -> { //Is Network down, code..
                Failure(-1004, noConnectionError, FailureKind.NETWORK, exception)
            }
            exception is ClientRequestException -> {

                val response = exception.response
                // TODO: do we need this?
                var serverError: ServerError? = null
                try {
                    serverError = serverErrorParser.parseError(response)
//                    serverError = jsonParser.decodeFromString(
//                        ServerError.serializer(),
//                        response.readText(Charset.forName("UTF-8"))
//                    )
                } catch (_: Exception) {

                }
                serverErrorParser.toClientFailure(response, serverError, exception)
            }
            else -> {
                Failure(500, exception.message ?: unknownError, FailureKind.SERVER, exception) //Unknown failure
            }
        }
    }
}

suspend fun <T, E> HttpResponse.decodeFromString(deserializer: DeserializationStrategy<T>): Success<T, E> =
    Success(status.value,
        jsonParser.decodeFromString(
            deserializer,
            bodyAsText()
        )
    )


fun <E> HttpResponse.noData() = Success.noData<E>(status)
