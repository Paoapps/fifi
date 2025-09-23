package com.paoapps.fifi.api

import platform.Foundation.NSProcessInfo

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class OSConfigurationFactory {
    actual fun defaultConfiguration(port: String): OSConfiguration {
        return OSConfiguration(
            NSProcessInfo.processInfo.environment["LOCAL_IP_ADDRESS"] as? String ?: "127.0.0.1:$port"
        )
    }
}
