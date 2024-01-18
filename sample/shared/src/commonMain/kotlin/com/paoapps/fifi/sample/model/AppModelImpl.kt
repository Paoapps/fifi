package com.paoapps.fifi.sample.model

import com.paoapps.fifi.model.ModelImpl
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.impl.ApiImpl
import com.paoapps.fifi.sample.api.mock.MockApi
import kotlinx.coroutines.MainScope

class AppModelImpl(
    appVersion: String,
    environment: AppModelEnvironment,
): ModelImpl<AppModelEnvironment, Api>(
    appVersion,
    MainScope(),
    environment,
), AppModel {

    override fun createApi(environment: AppModelEnvironment, appVersion: String) = when(environment.environmentName) {
        AppModelEnvironment.EnvironmentName.MOCK -> MockApi()
        AppModelEnvironment.EnvironmentName.PRODUCTION -> ApiImpl(environment, appVersion)
    }
}
