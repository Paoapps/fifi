package com.paoapps.fifi.auth

import com.paoapps.fifi.domain.auth.Credentials
import com.paoapps.fifi.domain.auth.Tokens
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.ModelEnvironment
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

interface TokenStore {
    fun loadTokens(environment: ModelEnvironment): Tokens?
    fun saveTokens(tokens: Tokens, environment: ModelEnvironment)
    fun deleteTokens(environment: ModelEnvironment)

    fun loadCredentials(environment: ModelEnvironment): Credentials?
    fun saveCredentials(credentials: Credentials, environment: ModelEnvironment)
    fun deleteCredentials(environment: ModelEnvironment)
}

class SettingsTokenStore(
    private val encryptedSettings: Settings,
    private val fallbackEncryptedSettings: Settings,
): TokenStore {

    override fun loadTokens(environment: ModelEnvironment): Tokens? {
        val accessToken = encryptedSettings.getStringOrNull(environment.accessTokenKey) ?: run {
            // Fallback to old storage if available
            val fallbackTokenString = fallbackEncryptedSettings.getStringOrNull(environment.accessTokenKey) ?: return null

            // Migrate to new storage
            fallbackEncryptedSettings.clear()
            encryptedSettings[environment.accessTokenKey] = fallbackTokenString
            fallbackTokenString
        }
        val refreshToken = encryptedSettings.getStringOrNull(environment.refreshTokenKey) ?: run {
            // Fallback to old storage if available
            val fallbackTokenString = fallbackEncryptedSettings.getStringOrNull(environment.refreshTokenKey) ?: return@run null

            // Migrate to new storage
            fallbackEncryptedSettings.clear()
            encryptedSettings[environment.refreshTokenKey] = fallbackTokenString
            fallbackTokenString
        }
        return Tokens(accessToken, refreshToken)
    }

    override fun saveTokens(tokens: Tokens, environment: ModelEnvironment) {
        encryptedSettings.set(environment.accessTokenKey, tokens.accessToken)
        tokens.refreshToken?.let { encryptedSettings.set(environment.refreshTokenKey, it) }
    }

    override fun deleteTokens(environment: ModelEnvironment) {
        encryptedSettings.remove(environment.accessTokenKey)
        encryptedSettings.remove(environment.refreshTokenKey)
    }

    override fun loadCredentials(environment: ModelEnvironment): Credentials? {
        val username = encryptedSettings.getStringOrNull(environment.usernameKey) ?: return null
        val password = encryptedSettings.getStringOrNull(environment.passwordKey) ?: return null
        return Credentials(username, password)
    }

    override fun saveCredentials(credentials: Credentials, environment: ModelEnvironment) {
        encryptedSettings.set(environment.usernameKey, credentials.username)
        encryptedSettings.set(environment.passwordKey, credentials.password)
    }

    override fun deleteCredentials(environment: ModelEnvironment) {
        encryptedSettings.remove(environment.usernameKey)
        encryptedSettings.remove(environment.passwordKey)
    }
}

private val ModelEnvironment.accessTokenKey get() = "${name}_accessToken"
private val ModelEnvironment.refreshTokenKey get() = "${name}_refreshToken"
private val ModelEnvironment.usernameKey get() = "${name}_username"
private val ModelEnvironment.passwordKey get() = "${name}_password"
