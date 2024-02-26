package com.paoapps.fifi.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.headers
import org.koin.core.component.KoinComponent

open class ApiHelper(
    val client: HttpClient,
    private val additionalHeaders: Map<String, String> = emptyMap(),
): KoinComponent {

    open fun headers(authHeader: String? = null): Map<String, String> {
        val mutableMap = additionalHeaders.toMutableMap()
        authHeader?.let { mutableMap["Authorization"] = it }
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
