package com.paoapps.fifi.utils

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.scope.Scope

actual fun settings(scope: Scope): Settings {
    val context: Context = scope.get()
    val preferencesName = "${context.packageName}_preferences"
    return SharedPreferencesSettings.Factory(context).create(preferencesName)
}
