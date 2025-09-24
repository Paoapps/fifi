package com.paoapps.fifi.sample.api.impl

import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.api.ClientApiImpl
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.CoffeeApi

class ApiImpl(
    environment: AppModelEnvironment,
    appVersion: String,
    isDebugMode: Boolean,
): ClientApiImpl<AppModelEnvironment>(environment, appVersion, isDebugMode = isDebugMode), Api {

    override val coffeeApi: CoffeeApi = CoffeeApiImpl(apiHelper, environment.configuration.apiBaseUrl)
}
