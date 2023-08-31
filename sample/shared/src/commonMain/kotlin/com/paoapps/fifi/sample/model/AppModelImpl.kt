package com.paoapps.fifi.sample.model

import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.model.ModelImpl
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.impl.ApiImpl
import com.paoapps.fifi.sample.api.mock.MockApi
import com.paoapps.fifi.sample.domain.AppData
import com.paoapps.fifi.sample.domain.ModelData
import com.paoapps.fifi.sample.model.AppModel
import com.paoapps.fifi.sample.model.AppModelEnvironment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer

class AppModelImpl(
    environment: AppModelEnvironment,
): ModelImpl<ModelData, IdentifiableClaims, AppModelEnvironment, Api, Unit, Unit>(
    MainScope(),
    environment,
), AppModel {
    override val modelDataSerializer: KSerializer<ModelData> get() {
        val serializer = ModelData.serializer()
        return serializer
    }

    override fun createModelData(launchData: LaunchData) = ModelData(
        appData = AppData(
            launchData = launchData,
        )
    )

    override fun copyLaunchData(modelData: ModelData, launchData: LaunchData) = modelData.copy(appData = modelData.appData.copy(launchData = launchData))

    override fun getLaunchData(modelData: ModelData) = modelData.appData.launchData

    override fun createApi(environment: AppModelEnvironment, appVersion: String) = when(environment.environmentName) {
        AppModelEnvironment.EnvironmentName.MOCK -> MockApi()
        AppModelEnvironment.EnvironmentName.PRODUCTION -> ApiImpl(environment, appVersion)
    }

    override val userIdFlow: Flow<Unit?> = flowOf(null)



}
