package com.paoapps.fifi.utils

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.scope.Scope

actual fun settings(scope: Scope): Settings {
    return NSUserDefaultsSettings.Factory().create(null)
}
