package com.paoapps.fifi.log.ktor.logging

import com.paoapps.fifi.log.info
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * [HttpClient] logging feature.
 */
class Logging(
    val logger: Logger,
    var level: LogLevel
) {

    private val maxBodyLength = 5000

    /**
     * [Logging] feature configuration
     */
    class Config {
        /**
         * [Logger] instance to use
         */
        var logger: Logger = Logger.SIMPLE

        /**
         * log [LogLevel]
         */
        var level: LogLevel = LogLevel.HEADERS
    }

    private suspend fun logRequest(request: HttpRequestBuilder) {
        if (level.info) {
            info("REQUEST: ${request.url.buildString()}")
            info("METHOD: ${request.method}")
        }
        if (level.headers) logHeaders(request.headers.entries())
        if (level.body) logRequestBody(request.body as OutgoingContent)
    }

    private suspend fun logResponse(response: HttpResponse) {
        if (level == LogLevel.NONE) {
            response.bodyAsChannel().discard(Long.MAX_VALUE)
            return
        }

        info("_______ begin ________")
        info("RESPONSE: ${response.status}")
        info("METHOD: ${response.call.request.method}")
        info("FROM: ${response.call.request.url}")

        if (level.headers) logHeaders(response.headers.entries())
        if (level.body) {
            logResponseBody(response.contentType(), response.bodyAsChannel())
        } else {
            response.bodyAsChannel().discard(Long.MAX_VALUE)
        }
        info("_______ end ________")
    }

    private fun logHeaders(headersMap: Set<Map.Entry<String, List<String>>>) {
        info("HEADERS")

        headersMap.filter { it.key != "Authorization" && it.key != "X-Api-Key" }.forEach { (key, values) ->
            info("-> $key: ${values.joinToString("; ")}")
        }
    }

    private suspend fun logResponseBody(contentType: ContentType?, content: ByteReadChannel) {
        info("BODY Content-Type: $contentType")
        info("BODY START")
        val message = content.readText(contentType?.charset() ?: Charsets.UTF_8).let {
            if (it.length > maxBodyLength) {
                it.substring(0, maxBodyLength).plus("...[${it.length - maxBodyLength} characters]")
            } else {
                it
            }
        }
        info(message)
        info("BODY END")
    }

    private suspend fun logRequestBody(content: OutgoingContent) {
        info("BODY Content-Type: ${content.contentType}")

        val charset = content.contentType?.charset() ?: Charsets.UTF_8

        val text = when (content) {
            is OutgoingContent.WriteChannelContent -> {
                val textChannel = ByteChannel()
                GlobalScope.launch(Dispatchers.Unconfined) {
                    content.writeTo(textChannel)
                    textChannel.close()
                }
                textChannel.readText(charset)
            }
            is OutgoingContent.ReadChannelContent -> {
                content.readFrom().readText(charset)
            }
            is OutgoingContent.ByteArrayContent -> String(
                content.bytes(),
                charset = charset
            )
            else -> null
        }
        info("BODY START")
        text?.let { info(it) }
        info("BODY END")
    }

    companion object : HttpClientPlugin<Config, Logging> {
        override val key: AttributeKey<Logging> = AttributeKey("ClientLogging")

        override fun prepare(block: Config.() -> Unit): Logging {
            val config = Config().apply(block)
            return Logging(config.logger, config.level)
        }

        override fun install(feature: Logging, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.Before) {
                try {
                    feature.logRequest(context)
                } catch (_: Throwable) {
                }
            }

            val observer: ResponseHandler = {
                try {
                    feature.logResponse(it)
                } catch (_: Throwable) {
                }
            }

            ResponseObserver.install(ResponseObserver(observer), scope)
        }
    }
}

private suspend inline fun ByteReadChannel.readText(charset: Charset): String {
    val packet = readRemaining(Long.MAX_VALUE)
    return packet.readText(charset = charset)
}
