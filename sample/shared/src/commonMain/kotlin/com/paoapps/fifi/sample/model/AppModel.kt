package com.paoapps.fifi.sample.model

import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.sample.api.Api

sealed class AppModelEnvironment(val environmentName: EnvironmentName, val configuration: Configuration):
    ModelEnvironment {

    enum class EnvironmentName {
        MOCK,
        PRODUCTION;

        val environment: AppModelEnvironment
            get() = when(this) {
            MOCK -> Mock
            PRODUCTION -> Production
        }
    }

    override val name: String
        get() = environmentName.name

    override val isMock: Boolean
        get() = this == Mock

    override val isOffline: Boolean
        get() = false

    object Mock: AppModelEnvironment(
        EnvironmentName.MOCK,
        Configuration(
            apiBaseUrl = ""
        )
    )

    object Production: AppModelEnvironment(
        EnvironmentName.PRODUCTION,
        Configuration(
            apiBaseUrl = "https://api.sampleapis.com/coffee"
        )
    )

    data class Configuration(
        val apiBaseUrl: String
    )

    companion object {
        fun fromName(name: String?): AppModelEnvironment = name?.let { EnvironmentName.values().first { it.name.equals(name, true) }.environment } ?: EnvironmentName.PRODUCTION.environment
    }
}

interface AppModel: Model<AppModelEnvironment, Api> {
}
