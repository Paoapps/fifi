package com.paoapps.fifi.model

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ModelEnvironment {
    val name: String

    val isOffline: Boolean
    val isMock: Boolean
}

interface ModelEnvironmentFactory<Environment: ModelEnvironment> {
    val defaultEnvironment: Environment

    fun fromName(name: String): Environment
}

internal class EnvironmentSettings: KoinComponent {
    val settings: Settings by inject()

    val environmentName: String?
        get() = settings["environment"]

    fun setEnvironmentName(name: String) {
        settings["environment"] = name
    }
}