package com.paoapps.fifi.api

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class OSConfigurationFactory {
    actual fun defaultConfiguration(port: String): OSConfiguration {
        // AVD uses 10.0.2.2 for localhost
        return OSConfiguration("10.0.2.2:$port")
    }
}
