package com.paoapps.fifi.api

actual class OSConfigurationFactory {
    actual fun defaultConfiguration(port: String): OSConfiguration {
        // AVD uses 10.0.2.2 for localhost
        return OSConfiguration("10.0.2.2:$port")
    }
}
