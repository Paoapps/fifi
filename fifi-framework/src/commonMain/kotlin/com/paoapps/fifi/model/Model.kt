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

    val appVersion: String
    val apiFlow: StateFlow<Api>

    val currentEnvironment: Environment
    val environmentFlow: FlowAdapter<Environment>

    val dataContainers: Map<String, CDataContainer<*>>

    val launchDataFlow: Flow<LaunchData>

    fun updateEnvironment(environment: Environment)

    fun <T: Any> registerPersistentData(
        name: String,
        serializer: KSerializer<T>,
        initialData: T,
        dataPreProcessors: List<DataProcessor<T>> = emptyList(),
    ): CDataContainer<T>
}
