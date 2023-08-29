package com.paoapps.fifi.log.ktor.logging

import com.paoapps.fifi.log.debug

/**
 * [HttpClient] Logger.
 */
interface Logger {
    /**
     * Add [message] to log.
     */
    fun log(message: String)

    companion object
}

/**
 * Default logger to use.
 */
//expect val Logger.Companion.DEFAULT: Logger

/**
 * [Logger] using [println].
 */
val Logger.Companion.SIMPLE: Logger get() = SimpleLogger()

private class SimpleLogger : Logger {
    override fun log(message: String) {
        debug("HttpClient: $message")
    }
}
