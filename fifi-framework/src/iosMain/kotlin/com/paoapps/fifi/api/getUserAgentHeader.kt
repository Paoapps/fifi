package com.paoapps.fifi.api

actual fun getUserAgentHeader(appVersion: String): String {
    return "iOS/$appVersion"
}

actual fun recordException(throwable: Throwable) {
    // TODO: record to Firebase
}