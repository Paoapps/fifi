package com.paoapps.fifi.utils

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.scope.Scope

actual fun settings(scope: Scope): Settings {
    val preferencesName = "preferences"
    return PreferencesSettings.Factory().create(preferencesName)
}
