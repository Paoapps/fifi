package com.paoapps.fifi.model

import com.paoapps.fifi.api.ApiFactory
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.di.API_STATE_FLOW_QUALIFIER
import com.paoapps.fifi.di.DATA_CONTAINERS_QUALIFIER
import com.paoapps.fifi.di.LAUNCH_DATA_QUALIFIER
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.utils.flow.FlowAdapter
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class ModelImpl<Environment: ModelEnvironment, Api: ClientApi>(
    scope: CoroutineScope,
): KoinComponent, Model<Environment, Api> {

    private val environmentFactory: ModelEnvironmentFactory<Environment> by inject()
    private val apiFactory: ApiFactory<Api, Environment> by inject()
    private val apiFlow: StateFlow<Api> by inject<StateFlow<Api>>(API_STATE_FLOW_QUALIFIER)

    protected val environmentStateFlow: MutableStateFlow<Environment> by lazy { MutableStateFlow(environmentFactory.defaultEnvironment) }
    override val environmentFlow: FlowAdapter<Environment> by lazy { environmentStateFlow.wrap(scope) }

    override val dataContainers: MutableMap<String, CDataContainer<*>> by inject(DATA_CONTAINERS_QUALIFIER)

    override val currentEnvironment: Environment
        get() = environmentStateFlow.value

    private val launchDataModelHelper: ModelHelper<LaunchData, Api> by inject(LAUNCH_DATA_QUALIFIER)
    override val launchDataFlow: Flow<LaunchData>
        get() = launchDataModelHelper.modelDataContainer.dataFlow.filterNotNull()

    init {
        scope.launch {
            environmentFlow.map { environment ->
                apiFactory.createApi(environment)
            }.collect((apiFlow as MutableStateFlow<Api>)::emit)
        }
    }

    override fun updateEnvironment(environment: Environment) {
        environmentStateFlow.value = environment
    }
}
