package com.paoapps.fifi.api

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class OSConfigurationFactory {
    actual fun defaultConfiguration(port: String): OSConfiguration {
        return OSConfiguration("127.0.0.1:$port")
    }
}
