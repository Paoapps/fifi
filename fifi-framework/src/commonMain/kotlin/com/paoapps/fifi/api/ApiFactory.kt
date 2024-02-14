package com.paoapps.fifi.api

import com.paoapps.fifi.model.ModelEnvironment

interface ApiFactory<Api: ClientApi, Environment: ModelEnvironment> {
    fun createApi(environment: Environment): Api

}
