package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.api.domain.ApiResponse
import com.paoapps.fifi.api.domain.Failure
import com.paoapps.fifi.api.domain.Success
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.domain.cache.BlockedCache
import com.paoapps.fifi.domain.cache.BlockedCacheData
import com.paoapps.fifi.domain.network.NetworkDataContainer
import com.paoapps.fifi.domain.network.asCommonDataContainer
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.datacontainer.DataContainer
import com.paoapps.fifi.utils.flow.Fetch
import com.paoapps.fifi.utils.flow.debug
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant

class ModelHelper<ModelData, AccessTokenClaims: IdentifiableClaims, Api: ClientApi<AccessTokenClaims>, ServerError>(val apiFlow: Flow<Api>, val modelDataContainer: DataContainer<ModelData>) {

    suspend fun api(): Api = apiFlow.first()

    fun <R> createApiCallFlow(
        data: suspend (api: ClientApi<AccessTokenClaims>) -> ApiResponse<R, ServerError>,
    ): Flow<NetworkDataContainer<R, ServerError>> {
        return apiFlow.flatMapLatest { api ->
            flow<NetworkDataContainer<R, ServerError>> {
                emit(NetworkDataContainer.Loading(null, 0L))
                val apiResponse = data(api)
                emit(apiResponse.asCommonDataContainer())
            }
        }.distinctUntilChanged()
    }

    fun <T> withApiBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T, ServerError>,
        fetch: Fetch,
        apiCall: suspend (api: Api, modelData: ModelData) -> ApiResponse<T, ServerError>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<NetworkDataContainer<T, ServerError>> = apiFlow.flatMapLatest { api ->
        withBlockedCacheFlow(debugName, blockedCache, fetch, {
            apiCall(api, it)
        }, predicate, condition, processData)
    }

    fun <T> withApiBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T, ServerError>,
        forceRefresh: Boolean,
        apiCall: suspend (api: Api, modelData: ModelData) -> ApiResponse<T, ServerError>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<NetworkDataContainer<T, ServerError>> = apiFlow.flatMapLatest { api ->
        withBlockedCacheFlow(debugName, blockedCache, forceRefresh, {
            apiCall(api, it)
        }, predicate, condition, processData)
    }

    fun <T> withBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T, ServerError>,
        fetch: Fetch,
        apiCall: suspend (modelData: ModelData) -> ApiResponse<T, ServerError>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<NetworkDataContainer<T, ServerError>> =
        withBlockedCacheFlow(
            debugName,
            blockedCache = blockedCache,
            forceRefresh = fetch == Fetch.FORCE,
            apiCall = apiCall,
            predicate = predicate,
            condition = condition.map { it && fetch != Fetch.NO_FETCH },
            processData = processData
        )

    fun <T> withBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T, ServerError>,
        forceRefresh: Boolean,
        apiCall: suspend (modelData: ModelData) -> ApiResponse<T, ServerError>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<NetworkDataContainer<T, ServerError>> =
        withModelDataFlow(
            debugName = debugName,
            apiCall = { modelData, updateData ->
                debugName?.let { debug("🔵 api call of $it") }
                blockedCache.getData(
                    forceRefresh = forceRefresh,
                    environment = api().environment,
                    predicate = predicate,
                    condition = condition,
                    fetcher = { apiCall(modelData) },
                    updateData = updateData
                )
            },
            processData = processData
        )


    fun <T, D> withModelDataFlow(
        debugName: String? = null,
        apiCall: suspend (modelData: ModelData, processSuccessData: (D) -> Unit) -> Flow<NetworkDataContainer<T, ServerError>>,
        processData: (responseData: D, modelData: ModelData) -> ModelData
    ): Flow<NetworkDataContainer<T, ServerError>> {
        return flow {
            val r = withDataFlow<T, D>(
                { data, processSuccess ->
                    val flow = apiCall(data, processSuccess)
                    flow
                },
                { responseData, data ->
                    val updatedData = processData(responseData, data)
                    if (updatedData != data) {
                        updatedData
                    } else {
                        null
                    }
                }
            )
            r.collect {
                emit(it)
            }
        }
    }

    private suspend fun <T, D> withDataFlow(
        apiCall: suspend (data: ModelData, processSuccessData: (D) -> Unit) -> Flow<NetworkDataContainer<T, ServerError>>,
        processData: ((responseData: D, data: ModelData) -> ModelData?)? = null
    ): Flow<NetworkDataContainer<T, ServerError>> {
        return flow {
            modelDataContainer.data?.let { modelData ->
                val response = apiCall(modelData) { successData ->
                    if (processData != null) {
                        modelDataContainer.data?.let {
                            val processedData = processData(successData, it)
                            if (processedData != null) {
                                modelDataContainer.data = processedData
                            }
                        }
                    }
                }
                response.collect { response ->
                    emit(response)
                }
            }

        }
    }

    suspend fun <T> withData(apiCall: suspend (modelData: ModelData) -> ApiResponse<T, ServerError>, processData: ((responseData: T, modelData: ModelData) -> ModelData)? = null): ApiResponse<T, ServerError> {
        val modelData = modelDataContainer.data ?: return Failure(0, "No Data")
        val response = apiCall(modelData)
        if (response is Success && processData != null) {
            val successData = response.data
            modelDataContainer.data?.let {
                val processedData = processData(successData, it)
                modelDataContainer.data = processedData
            }
        }
        return response
    }
}