package com.paoapps.fifi.api

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.localization.LanguageProvider
import com.paoapps.fifi.model.ModelEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.DeserializationStrategy
import org.koin.core.component.inject
import kotlin.collections.set

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

open class ClientApiHelper(
    client: HttpClient,
    val environment: ModelEnvironment,
    private val appVersion: String,
    additionalHeaders: Map<String, String> = emptyMap(),
): ApiHelper(
    client,
    additionalHeaders
) {

    private val languageProvider: LanguageProvider by inject()

    override fun headers(authHeader: String?): Map<String, String> {
        val mutableMap = super.headers(authHeader).toMutableMap()

        languageProvider.language?.acceptLanguageHeader?.let {
            mutableMap["Accept-Language"] = it
        }
        mutableMap["User-Agent"] = getUserAgentHeader(appVersion)

        return mutableMap
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

suspend fun <T: Any> HttpResponse.decodeFromString(deserializer: DeserializationStrategy<T>): T =
    jsonParser.decodeFromString(
        deserializer,
        bodyAsText()
    )

fun HttpResponse.noData() = FetcherResult.Data(Unit)
