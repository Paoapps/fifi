package com.paoapps.fifi.sample.api.impl

import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.api.ClientApiImpl
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.CoffeeApi

class ApiImpl(
    environment: AppModelEnvironment,
    appVersion: String,
): ClientApiImpl<AppModelEnvironment, IdentifiableClaims, Claims, Unit>(environment, appVersion), Api {

    override val coffeeApi: CoffeeApi = CoffeeApiImpl(apiHelper, environment.configuration.apiBaseUrl)
}
