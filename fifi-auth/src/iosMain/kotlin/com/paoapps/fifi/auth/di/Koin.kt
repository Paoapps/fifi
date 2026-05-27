package com.paoapps.fifi.auth.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import platform.Foundation.CFBridgingRetain
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccessibleWhenUnlocked
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrService

@OptIn(ExperimentalSettingsImplementation::class, ExperimentalForeignApi::class, ExperimentalSettingsApi::class)
internal actual fun platformInjections(serviceName: String, module: Module) {
    module.single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS)) {
        KeychainSettings(Pair(kSecAttrAccessible, kSecAttrAccessibleWhenUnlocked)).clear()
        KeychainSettings(
            kSecAttrService to CFBridgingRetain(serviceName),
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlock
        )
    }

    // We migrated from kSecAttrAccessibleWhenUnlockedThisDeviceOnly to kSecAttrAccessibleAfterFirstUnlock
    // However existing tokens stored with the old accessibility setting are not accessible with the new setting
    // So we need to keep a fallback to the old setting to allow existing users to keep
    // This can be removed in a future release once we are sure most users have migrated (somewhere in 2026)
    module.single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS_FALLBACK)) {
        KeychainSettings(
            kSecAttrService to CFBridgingRetain(serviceName),
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
    }

    module.single<Settings>(named(PlatformModuleQualifier.SETTINGS)) {
        NSUserDefaultsSettings.Factory().create(serviceName)
    }
}
