package com.paoapps.fifi.model

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

interface ModelEnvironment {
    val name: String

    val isOffline: Boolean
    val isMock: Boolean
}

interface ModelEnvironmentFactory<Environment: ModelEnvironment> {
    val defaultEnvironment: Environment

    fun fromName(name: String): Environment
}

internal class EnvironmentSettings {
    val settings = Settings()

    val environmentName: String?
        get() = settings["environment"]

    fun setEnvironmentName(name: String) {
        settings["environment"] = name
    }
}