package com.paoapps.fifi.api

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.log.ktor.logging.LogLevel
import com.paoapps.fifi.log.ktor.logging.Logger
import com.paoapps.fifi.log.ktor.logging.Logging
import com.paoapps.fifi.log.ktor.logging.SIMPLE
import com.paoapps.fifi.model.ModelEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.DeserializationStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.collections.Map
import kotlin.collections.emptyMap
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.toMutableMap

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

// TODO: do we still need this?
//interface ServerErrorParser<E> {
//    suspend fun parseError(response: HttpResponse): E?
//    suspend fun <T> toClientFailure(response: HttpResponse, error: E?, exception: Exception): FetcherResult.Error
//}

open class ApiHelper(
    val environment: ModelEnvironment,
    private val appVersion: String,
    private val additionalHeaders: Map<String, String> = emptyMap(),
    val client: HttpClient,
): KoinComponent {

    private val languageProvider: LanguageProvider by inject()

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
}

suspend fun <T: Any> withinTryCatch(block: suspend () -> FetcherResult<T>): FetcherResult<T> {
    return try {
        block()
    } catch (exception: Throwable) {
        if (exception is ResponseException)  {
            val response = exception.response
            val statusCode = response.status.value
            val message = response.bodyAsText()
            FetcherResult.Error.Message(message, statusCode)
        } else {
            FetcherResult.Error.Exception(exception)
        }
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
