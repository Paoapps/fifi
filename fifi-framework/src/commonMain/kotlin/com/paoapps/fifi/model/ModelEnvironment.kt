package com.paoapps.fifi.model

interface ModelEnvironment {
    val name: String

    val isOffline: Boolean
    val isMock: Boolean
}

interface ModelEnvironmentFactory<Environment: ModelEnvironment> {
    val defaultEnvironment: Environment

    fun fromName(name: String): Environment
}
