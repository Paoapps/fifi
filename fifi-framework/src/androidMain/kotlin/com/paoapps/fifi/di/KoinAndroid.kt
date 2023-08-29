package com.paoapps.fifi.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

internal actual fun platformModule(serviceName: String): Module = module {
    single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS)) {
        val context = get() as Context
        try {
            context.initialiseSecurePrefs(serviceName)
        } catch (e: Exception) {
            context.clearSharedPreferences()
            context.initialiseSecurePrefs(serviceName)
        }
    }
    single<Settings>(named(PlatformModuleQualifier.SETTINGS)) {
        SharedPreferencesSettings.Factory(get()).create(serviceName)
    }
}

// Work around for https://issuetracker.google.com/issues/164901843
private fun Context.initialiseSecurePrefs(serviceName: String): SharedPreferencesSettings {
    return SharedPreferencesSettings(
        EncryptedSharedPreferences.create(
        this,
            serviceName,
        MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    ), false)
}

private fun Context.clearSharedPreferences() {
    val dir = File("${this.filesDir.parent}/shared_prefs/")
    val children: Array<String> = dir.list()
    for (i in children.indices) {
        this.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit()
            .clear().commit()
        File(dir, children[i]).delete()
    }
}
