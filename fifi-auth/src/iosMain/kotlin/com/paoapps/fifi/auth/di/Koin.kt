package com.paoapps.fifi.auth.di

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.CFBridgingRetain
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrService

@OptIn(ExperimentalSettingsImplementation::class, ExperimentalForeignApi::class)
internal actual fun platformInjections(serviceName: String, module: Module) {
    module.single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS)) {
        KeychainSettings(
            kSecAttrService to CFBridgingRetain(serviceName),
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
    }
    module.single<Settings>(named(PlatformModuleQualifier.SETTINGS)) {
        NSUserDefaultsSettings.Factory().create(serviceName)
    }
}
