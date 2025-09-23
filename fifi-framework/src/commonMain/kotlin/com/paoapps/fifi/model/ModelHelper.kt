@file:OptIn(ExperimentalTime::class)

package com.paoapps.fifi.model

import com.paoapps.blockedcache.*
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.di.API_STATE_FLOW_QUALIFIER
import com.paoapps.fifi.model.datacontainer.DataContainer
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun <T: Any, Api: ClientApi> ModelHelper<BlockedCacheData<T>, Api>.withApiBlockedCacheFlow(
    debugName: String? = null,
    blockedCache: BlockedCache<T>,
    fetch: Fetch,
    apiCall: suspend (api: Api, modelData: BlockedCacheData<T>) -> FetcherResult<T>,
    predicate: (T, Instant) -> Boolean = { _, _ -> true },
    condition: Flow<Boolean> = flowOf(true),
): Flow<CacheResult<T>> = apiFlow.flatMapLatest { api ->
    withBlockedCacheFlow(debugName ?: name, blockedCache, fetch, {
        apiCall(api, it)
    }, predicate, condition) { updatedData, _ ->
        updatedData
    }
}

class ModelHelper<ModelData: Any, Api: ClientApi>(
    val name: String,
    val modelDataContainer: DataContainer<ModelData>
): KoinComponent {

    val apiFlow: StateFlow<Api> by inject(API_STATE_FLOW_QUALIFIER)
    suspend fun api(): Api = apiFlow.first()

    fun <R: Any> createApiCallFlow(
        data: suspend (api: Api) -> FetcherResult<R>,
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
            predicate = { data, time -> predicate(data, time) && if (fetch is Fetch.Cache) !fetch.ignoreExpiration else true },
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

    fun <T: Any> createBlockCache(
        duration: Duration,
        expire: Duration?,
        selector: (ModelData) -> BlockedCacheData<T>?,
        trigger: Flow<Any?> = flowOf(Unit),
        isDebugEnabled: Boolean = false
    ): BlockedCache<T> {
        return createBlockCache(
            dataContainer = modelDataContainer,
            duration = duration,
            expire = expire,
            selector = selector,
            name = name,
            trigger = trigger,
            isDebugEnabled = isDebugEnabled
        )
    }
}

fun <T: Any, Api: ClientApi> ModelHelper<BlockedCacheData<T>, Api>.createBlockCache(
    duration: Duration,
    expire: Duration?,
    trigger: Flow<Any?> = flowOf(Unit),
    isDebugEnabled: Boolean = false
): BlockedCache<T> {
    return createBlockCache(
        dataContainer = modelDataContainer,
        duration = duration,
        expire = expire,
        selector = { it },
        name = name,
        trigger = trigger,
        isDebugEnabled = isDebugEnabled
    )
}
