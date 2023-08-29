package com.paoapps.fifi.model

interface ModelEnvironment {
    val name: String

    val isOffline: Boolean
    val isMock: Boolean
}

interface ModelEnvironmentProvider {
    val environments: List<ModelEnvironment>
}
