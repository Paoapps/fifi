package com.paoapps.fifi.api

actual fun getUserAgentHeader(appVersion: String): String {
    return "JVM ($appVersion)"
}
