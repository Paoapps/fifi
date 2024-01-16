package com.paoapps.fifi.api

import com.paoapps.fifi.model.ModelEnvironment
import org.koin.core.component.KoinComponent

open class ClientApiImpl<Environment: ModelEnvironment>(
    final override val environment: Environment,
    appVersion: String,
    additionalHeaders: Map<String, String> = emptyMap(),
): KoinComponent, ClientApi {

    protected val apiHelper = ApiHelper(environment, appVersion, additionalHeaders)
}
