package com.paoapps.fifi.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.BlockedCacheData
import com.paoapps.blockedcache.CacheResult
import com.paoapps.blockedcache.Fetch
import com.paoapps.blockedcache.FetcherResult
import com.paoapps.blockedcache.asCacheResult
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.datacontainer.DataContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class ModelHelper<ModelData, AccessTokenClaims: IdentifiableClaims, Api: ClientApi<AccessTokenClaims>>(val apiFlow: Flow<Api>, val modelDataContainer: DataContainer<ModelData>) {

    suspend fun api(): Api = apiFlow.first()

    fun <R: Any> createApiCallFlow(
        data: suspend (api: ClientApi<AccessTokenClaims>) -> FetcherResult<R>,
    ): Flow<CacheResult<R>> {
        return apiFlow.flatMapLatest { api ->
            flow<CacheResult<R>> {
                emit(CacheResult.Loading(null, 0L))
                val apiResponse = data(api)
                emit(apiResponse.asCacheResult())
            }
        }.distinctUntilChanged()
    }

    fun <T: Any> withApiBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T>,
        fetch: Fetch,
        apiCall: suspend (api: Api, modelData: ModelData) -> FetcherResult<T>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<CacheResult<T>> = apiFlow.flatMapLatest { api ->
        withBlockedCacheFlow(debugName, blockedCache, fetch, {
            apiCall(api, it)
        }, predicate, condition, processData)
    }

    fun <T: Any> withApiBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T>,
        forceRefresh: Boolean,
        forceRefreshDelay: Long? = null,
        apiCall: suspend (api: Api, modelData: ModelData) -> FetcherResult<T>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<CacheResult<T>> = apiFlow.flatMapLatest { api ->
        withBlockedCacheFlow(debugName, blockedCache, forceRefresh, forceRefreshDelay, {
            apiCall(api, it)
        }, predicate, condition, processData)
    }

    fun <T: Any> withBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T>,
        fetch: Fetch,
        apiCall: suspend (modelData: ModelData) -> FetcherResult<T>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<CacheResult<T>> =
        withBlockedCacheFlow(
            debugName,
            blockedCache = blockedCache,
            forceRefresh = fetch is Fetch.Force,
            forceRefreshDelay = (fetch as? Fetch.Force)?.minimumDelay,
            apiCall = apiCall,
            predicate = predicate,
            condition = condition.map { it && fetch !is Fetch.NoFetch },
            processData = processData
        )

    fun <T: Any> withBlockedCacheFlow(
        debugName: String? = null,
        blockedCache: BlockedCache<T>,
        forceRefresh: Boolean,
        forceRefreshDelay: Long? = null,
        apiCall: suspend (modelData: ModelData) -> FetcherResult<T>,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        processData: (responseData: BlockedCacheData<T>, modelData: ModelData) -> ModelData
    ): Flow<CacheResult<T>> =
        withModelDataFlow(
            debugName = debugName,
            apiCall = { modelData, updateData ->
                debugName?.let { debug("🔵 api call of $it") }
                blockedCache.getData(
                    forceRefresh = forceRefresh,
                    forceRefreshDelay = forceRefreshDelay,
                    predicate = predicate,
                    condition = condition,
                    fetcher = { apiCall(modelData) },
                    updateData = updateData
                )
            },
            processData = processData
        )


    fun <T: Any, D> withModelDataFlow(
        debugName: String? = null,
        apiCall: suspend (modelData: ModelData, processSuccessData: (D) -> Unit) -> Flow<CacheResult<T>>,
        processData: (responseData: D, modelData: ModelData) -> ModelData
    ): Flow<CacheResult<T>> {
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
        apiCall: suspend (data: ModelData, processSuccessData: (D) -> Unit) -> Flow<CacheResult<T>>,
        processData: ((responseData: D, data: ModelData) -> ModelData?)? = null
    ): Flow<CacheResult<T>> {
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

    suspend fun <T: Any> withData(apiCall: suspend (modelData: ModelData) -> FetcherResult<T>, processData: ((responseData: T, modelData: ModelData) -> ModelData)? = null): FetcherResult<T> {
        val modelData = modelDataContainer.data ?: return FetcherResult.Error.Message("No Data")
        val response = apiCall(modelData)
        if (response is FetcherResult.Data && processData != null) {
            val successData = response.value
            modelDataContainer.data?.let {
                val processedData = processData(successData, it)
                modelDataContainer.data = processedData
            }
        }
        return response
    }
}