package com.paoapps.fifi.auth.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.qualifier.named

internal actual fun platformInjections(serviceName: String, module: Module) {
    module.single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS)) {
        // we don't provide secure settings for JVM
        PreferencesSettings.Factory().create(serviceName)
    }
    module.single<Settings>(named(PlatformModuleQualifier.SETTINGS)) {
        PreferencesSettings.Factory().create(serviceName)
    }
}
