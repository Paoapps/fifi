package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.utils.flow.FlowAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Model<ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi> {

    val modelData: CDataContainer<ModelData>
    val mockConfigData: CDataContainer<MockConfig>
    val apiFlow: StateFlow<Api>

    val environmentFlow: FlowAdapter<Environment>

    fun updateEnvironment(environment: Environment)
    fun registerLaunch(version: String)

}
