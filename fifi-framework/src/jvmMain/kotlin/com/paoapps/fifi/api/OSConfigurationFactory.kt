package com.paoapps.fifi.api

actual class OSConfigurationFactory {
    actual fun defaultConfiguration(port: String): OSConfiguration {
        return OSConfiguration("127.0.0.1:$port")
    }
}
