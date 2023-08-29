package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.TokenStore
import com.paoapps.fifi.auth.Tokens
import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.model.datacontainer.DataContainerImpl
import com.paoapps.fifi.model.datacontainer.DataProcessor
import com.paoapps.fifi.model.datacontainer.wrap
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import org.koin.core.component.KoinComponent

abstract class ModelImpl<ModelData, AccessTokenClaims: IdentifiableClaims, Environment: ModelEnvironment, Api: ClientApi<AccessTokenClaims>, UserId, ServerError>(
    scope: CoroutineScope,
    environment: Environment,
): KoinComponent, Model<ModelData, AccessTokenClaims, Environment, UserId, Api> {

    abstract val modelDataSerializer: KSerializer<ModelData>
    open val dataPreProcessors: List<DataProcessor<ModelData>> = emptyList()

    private val environmentStateFlow = MutableStateFlow(environment)
    override val environmentFlow = environmentStateFlow.wrap(scope)
    private val appVersionFlow = MutableStateFlow("")

    override val apiFlow: MutableStateFlow<Api> by lazy { MutableStateFlow(createApi(environment, "")) }

    override val modelData: CDataContainer<ModelData> by lazy {
        DataContainerImpl(modelDataSerializer, dataPreProcessors).wrap()
    }

    init {
        scope.launch {
            combine(environmentFlow, appVersionFlow) { environment, appVersion ->
                createApi(environment, appVersion)
            }.collect(apiFlow::emit)
        }
    }

    abstract fun createApi(environment: Environment, appVersion: String): Api

    // TODO: remove. hint: ModelData(AppData(LaunchData(currentAppVersion = version)))
    abstract fun createModelData(launchData: LaunchData): ModelData
    abstract fun getLaunchData(modelData: ModelData): LaunchData
    abstract fun copyLaunchData(modelData: ModelData, launchData: LaunchData): ModelData

    override fun updateEnvironment(environment: Environment) {
        environmentStateFlow.value = environment
    }

    override fun registerLaunch(version: String) {
        debug("🔵 register launch of version ${version}")
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

    override fun storeTokens(tokens: Tokens) {
        getKoin().get<TokenStore>().saveTokens(tokens, environmentStateFlow.value)
    }
}
