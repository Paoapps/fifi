package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.model.datacontainer.DataContainerImpl
import com.paoapps.fifi.model.datacontainer.DataProcessor
import com.paoapps.fifi.model.datacontainer.wrap
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import org.koin.core.component.KoinComponent

abstract class ModelImpl<ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi>(
    scope: CoroutineScope,
    environment: Environment,
): KoinComponent, Model<ModelData, MockConfig, Environment, Api> {

    abstract val modelDataSerializer: KSerializer<ModelData>
    abstract val mockConfigSerializer: KSerializer<MockConfig>

    open val dataPreProcessors: List<DataProcessor<ModelData>> = emptyList()

    protected val environmentStateFlow = MutableStateFlow(environment)
    override val environmentFlow = environmentStateFlow.wrap(scope)
    private val appVersionFlow = MutableStateFlow("")

    final override val apiFlow: MutableStateFlow<Api> by lazy { MutableStateFlow(createApi(environment, "")) }

    override val modelData: CDataContainer<ModelData> by lazy {
        DataContainerImpl(modelDataSerializer, dataPreProcessors).wrap()
    }

    override val mockConfigData: CDataContainer<MockConfig> by lazy {
        DataContainerImpl(mockConfigSerializer).wrap()
    }

    init {
        scope.launch {
            combine(environmentFlow, appVersionFlow) { environment, appVersion ->
                createApi(environment, appVersion)
            }.collect(apiFlow::emit)
        }
    }

    abstract fun createApi(environment: Environment, appVersion: String): Api

    abstract fun createModelData(launchData: LaunchData): ModelData
    abstract fun createMockConfig(): MockConfig
    abstract fun getLaunchData(modelData: ModelData): LaunchData
    abstract fun copyLaunchData(modelData: ModelData, launchData: LaunchData): ModelData

    override fun updateEnvironment(environment: Environment) {
        environmentStateFlow.value = environment
    }

    override fun registerLaunch(version: String) {
        debug("register launch of version ${version}")

        val mock = mockConfigData
        if (mock.data == null) {
            mock.data = createMockConfig()
        }

        val data = modelData.data
        if (data == null) {
            this.modelData.data = createModelData(LaunchData(currentAppVersion = version))
        } else {
            val launchData = getLaunchData(data)
            val updatedLaunchData = launchData.copy(
                isFirstLaunch = false,
                previousAppVersion = launchData.currentAppVersion,
                currentAppVersion = version
            )
            if (launchData != updatedLaunchData) {
                this.modelData.data = copyLaunchData(data, updatedLaunchData)
            }
        }
        appVersionFlow.value = data?.let { getLaunchData(it) }?.currentAppVersion ?: ""
    }
}
