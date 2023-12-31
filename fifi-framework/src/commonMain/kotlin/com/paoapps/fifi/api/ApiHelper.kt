package com.paoapps.fifi.api

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.blockedcache.map
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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
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

// TODO: do we still need this?
//interface ServerErrorParser<E> {
//    suspend fun parseError(response: HttpResponse): E?
//    suspend fun <T> toClientFailure(response: HttpResponse, error: E?, exception: Exception): FetcherResult.Error
//}

open class ApiHelper<AccessTokenClaims: IdentifiableClaims, RefreshTokenClaims: Claims>(
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
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }
):
    KoinComponent {

    private val refreshMutex = Mutex()

    val tokenDecoder: TokenDecoder<AccessTokenClaims, RefreshTokenClaims> by inject()
    val tokenStore: TokenStore by inject()

    private val authApi: AuthApi by inject {
        parametersOf(environment)
    }
    private val languageProvider: LanguageProvider by inject()

    suspend fun <T: Any> authenticated(debug: String? = null, optional: Boolean = false, block: suspend (String?, IdentifiableClaims?) -> FetcherResult<T>): FetcherResult<T> = authenticated(debug, 0, optional, requireLock = true, block)

    suspend fun <T: Any> authenticated(debug: String? = null, recursiveCount: Int, optional: Boolean = false, requireLock: Boolean, block: suspend (String?, IdentifiableClaims?) -> FetcherResult<T>): FetcherResult<T> {
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

    companion object {
        private fun isPermanentError(response: FetcherResult.Error) = when(response) {
            is FetcherResult.Error.Exception -> (response.error as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
            is FetcherResult.Error.Message -> false
        }
    }
}

suspend fun <T: Any> withinTryCatch(block: suspend () -> FetcherResult<T>): FetcherResult<T> {
    return try {
        block()
    } catch (exception: Throwable) {
        FetcherResult.Error.Exception(exception)
    }
}

//suspend fun <T: Any> withinTryCatch(noConnectionError: String, unknownError: String, serverErrorParser: ServerErrorParser, block: suspend () -> FetcherResult<T>): FetcherResult<T> {
//    try {
//        return block()
//    } catch (exception: Throwable) {
//        // TODO: allow for custom error handling/recording
////        recordException(exception)
//        com.paoapps.fifi.log.error("Request failed, $exception", exception)
//        return when {
//            exception.message?.contains("Code=-1009") == true -> { //Is Network down, code..
//                Failure(-1009, noConnectionError, FailureKind.NETWORK, exception)
//            }
//            exception.message?.contains("Code=-1004") == true -> { //Is Network down, code..
//                Failure(-1004, noConnectionError, FailureKind.NETWORK, exception)
//            }
//            exception is ClientRequestException -> {
//
//                val response = exception.response
//                // TODO: do we need this?
//                var serverError: ServerError? = null
//                try {
//                    serverError = serverErrorParser.parseError(response)
////                    serverError = jsonParser.decodeFromString(
////                        ServerError.serializer(),
////                        response.readText(Charset.forName("UTF-8"))
////                    )
//                } catch (_: Exception) {
//
//                }
//                serverErrorParser.toClientFailure(response, serverError, exception)
//            }
//            else -> {
//                Failure(500, exception.message ?: unknownError, FailureKind.SERVER, exception) //Unknown failure
//            }
//        }
//    }
//}

suspend fun <T: Any> HttpResponse.decodeFromString(deserializer: DeserializationStrategy<T>): FetcherResult.Data<T> =
    FetcherResult.Data(
        jsonParser.decodeFromString(
            deserializer,
            bodyAsText()
        )
    )


fun HttpResponse.noData() = FetcherResult.Data(Unit)
