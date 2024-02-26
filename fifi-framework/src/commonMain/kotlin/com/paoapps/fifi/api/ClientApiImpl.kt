package com.paoapps.fifi.api

import com.paoapps.fifi.log.ktor.logging.LogLevel
import com.paoapps.fifi.log.ktor.logging.Logger
import com.paoapps.fifi.log.ktor.logging.Logging
import com.paoapps.fifi.log.ktor.logging.SIMPLE
import com.paoapps.fifi.model.ModelEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.component.KoinComponent

open class ClientApiImpl<Environment: ModelEnvironment>(
    final override val environment: Environment,
    appVersion: String,
    additionalHeaders: Map<String, String> = emptyMap(),
    isDebugMode: Boolean,
    httpClient: HttpClient = HttpClient {
        expectSuccess = true
        install(Logging) {
            logger = Logger.SIMPLE
            level = if (isDebugMode) LogLevel.ALL else LogLevel.INFO
        }
        install(ContentNegotiation) {
            json(jsonParser)
        }
    },
): KoinComponent, ClientApi {

    protected val apiHelper = ClientApiHelper(httpClient, environment, appVersion, additionalHeaders)
}
