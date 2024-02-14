package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.model.datacontainer.DataProcessor
import com.paoapps.fifi.utils.flow.FlowAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer

interface Model<Environment: ModelEnvironment, Api: ClientApi> {

    val currentEnvironment: Environment
    val environmentFlow: FlowAdapter<Environment>

    val dataContainers: Map<String, CDataContainer<*>>

    val launchDataFlow: Flow<LaunchData>

    fun updateEnvironment(environment: Environment)
}
