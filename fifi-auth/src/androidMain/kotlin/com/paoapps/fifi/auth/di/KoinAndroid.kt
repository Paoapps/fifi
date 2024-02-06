package com.paoapps.fifi.auth.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.model.ModelEnvironment
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.KoinApplication
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import java.io.File

internal actual fun platformInjections(serviceName: String, module: Module) {
    module.single<Settings>(named(PlatformModuleQualifier.ENCRYPTED_SETTINGS)) {
        val context = get() as Context
        try {
            context.initialiseSecurePrefs(serviceName)
        } catch (e: Exception) {
            context.clearSharedPreferences()
            context.initialiseSecurePrefs(serviceName)
        }
    }
    module.single<Settings>(named(PlatformModuleQualifier.SETTINGS)) {
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

fun <Environment: ModelEnvironment, UserId, AccessTokenClaims: IdentifiableClaims<UserId>, RefreshTokenClaims: Claims, Api: AuthClientApi<UserId, AccessTokenClaims>> initKoinApp(
    context: Context,
    appDefinition: AuthAppDefinition<Environment, UserId, AccessTokenClaims, RefreshTokenClaims, Api>,
    logger: Logger? = null,
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    return com.paoapps.fifi.koin.initKoinApp(
        context = context,
        appDefinition = appDefinition,
        initialization = { appDefinition, modules, logger, appDeclaration ->
            initKoinApp(
                appDefinition,
                modules,
                logger,
                appDeclaration
            )
        },
        logger = logger,
        appDeclaration = appDeclaration
    )
}
