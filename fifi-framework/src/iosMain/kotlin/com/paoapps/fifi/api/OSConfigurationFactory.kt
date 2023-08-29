package com.paoapps.fifi.api

import platform.Foundation.NSProcessInfo

actual class OSConfigurationFactory {
    actual fun defaultConfiguration(): OSConfiguration {
        return OSConfiguration(
            NSProcessInfo.processInfo.environment["LOCAL_IP_ADDRESS"] as? String ?: "127.0.0.1:8081"
        )
    }
}
