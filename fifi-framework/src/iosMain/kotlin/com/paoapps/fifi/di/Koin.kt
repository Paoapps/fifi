package com.paoapps.fifi.di

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.CFBridgingRetain
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrService

@OptIn(ExperimentalSettingsImplementation::class)
internal actual fun platformModule(serviceName: String): Module = module {
    single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS)) {
        KeychainSettings(
            kSecAttrService to CFBridgingRetain(serviceName),
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
    }
    single<Settings>(named(PlatformModuleQualifier.SETTINGS)) {
        NSUserDefaultsSettings.Factory().create(serviceName)
    }
}
