package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.di.DATA_CONTAINERS_QUALIFIER
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.model.datacontainer.DataContainer
import com.paoapps.fifi.model.datacontainer.DataContainerImpl
import com.paoapps.fifi.model.datacontainer.DataProcessor
import com.paoapps.fifi.model.datacontainer.wrap
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

abstract class ModelImpl<Environment: ModelEnvironment, Api: ClientApi>(
    override val appVersion: String,
    scope: CoroutineScope,
    environment: Environment,
): KoinComponent, Model<Environment, Api> {

    protected val environmentStateFlow = MutableStateFlow(environment)
    override val environmentFlow = environmentStateFlow.wrap(scope)

    final override val apiFlow: MutableStateFlow<Api> by lazy { MutableStateFlow(createApi(environment, appVersion)) }

    override val dataContainers: MutableMap<String, CDataContainer<*>> by inject(named(DATA_CONTAINERS_QUALIFIER))

    override val currentEnvironment: Environment
        get() = environmentStateFlow.value

    final override fun <T: Any> registerPersistentData(
        name: String,
        serializer: KSerializer<T>,
        initialData: T,
        dataPreProcessors: List<DataProcessor<T>>,
    ): CDataContainer<T> {
        return DataContainerImpl(serializer, initialData, dataPreProcessors).wrap()
    }

    init {
        scope.launch {
            environmentFlow.map { environment ->
                createApi(environment, appVersion)
            }.collect(apiFlow::emit)
        }
    }

    abstract fun createApi(environment: Environment, appVersion: String): Api

    override fun updateEnvironment(environment: Environment) {
        environmentStateFlow.value = environment
    }
}
