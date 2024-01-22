package com.paoapps.fifi.sample.model

import com.paoapps.fifi.di.LAUNCH_DATA_QUALIFIER
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.model.ModelHelper
import com.paoapps.fifi.model.ModelImpl
import com.paoapps.fifi.sample.PersistentDataName
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.impl.ApiImpl
import com.paoapps.fifi.sample.api.mock.MockApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import org.koin.core.component.inject

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
