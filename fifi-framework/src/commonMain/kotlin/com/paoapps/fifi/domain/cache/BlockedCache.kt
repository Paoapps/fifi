package com.paoapps.fifi.domain.cache

import com.paoapps.fifi.api.domain.ApiResponse
import com.paoapps.fifi.api.domain.Failure
import com.paoapps.fifi.api.domain.Success
import com.paoapps.fifi.domain.network.NetworkDataContainer
import com.paoapps.fifi.domain.network.NetworkStatus
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Transient
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * A cache which avoids multiple threads to retrieve the same information from a backend when no
 * data is available in the cache.
 * The cache is based on Flow which enables data to change if the data within the cache changes.
 * When requesting data old data will be emitted in a Loading object. When new information becomes
 * available the new data will be emitted in an success object.
 *
 * @param expireTime The expire time of the item in millis.
 * @param cacheData The data (or no data if it is not present) and additional information the cache needs.
 * @param nowProvider Provider of now(). Useful for unit testing.
 * @param creationTime Set using getData but can also be set external for example unit tests.
 * @param name Can be used for debugging.
 */

class BlockedCache<T, E>(
    private val refreshTime: Long,
    private val expireTime: Long?,
    private val trigger: Flow<Unit>,
    private val dataFlow: Flow<BlockedCacheData<T>>,
    private val networkStatusFlow: Flow<NetworkStatus>,
    @Transient private val nowProvider: NowProvider = DatetimeNowProvider(),
    private val name: String = "genericBlockedCache",
    private val isDebugEnabled: Boolean
) {

    data class RefreshTrigger(val random: String = Random.nextInt().toString(), val forceRefresh: Boolean = false)

    private val refreshTriggerState = MutableStateFlow(RefreshTrigger())
    private val refreshTrigger = combine(trigger, refreshTriggerState) { _, trigger -> RefreshTrigger(forceRefresh = trigger.forceRefresh) }

    private val mutex = Mutex()
    private val lastForceRefresh: MutableStateFlow<Long> = MutableStateFlow(0)

    private fun debugCache(message: String) {
        if (isDebugEnabled) {
            debug("BlockedCache($name): $message")
        }
    }

    suspend fun getData(
        forceRefresh: Boolean = false,
        environment: ModelEnvironment,
        predicate: (T, Instant) -> Boolean = { _, _ -> true },
        condition: Flow<Boolean> = flowOf(true),
        fetcher: suspend () -> ApiResponse<T, E>,
        updateData: (BlockedCacheData<T>) -> Unit
    ): Flow<NetworkDataContainer<T, E>> {
        debugCache("start")

        var lockedByMe = false

        val responseFlow: Flow<NetworkDataContainer<T, E>> = refreshTrigger.flatMapLatest { trigger ->
            val apiResponseFlow: Flow<NetworkDataContainer<T, E>> = if (environment.isOffline)
                dataFlow.take(1).transformLatest { cacheData ->
                    emit(cacheData.asCommonDataContainer())
                }
            else {

                fun getData(): Flow<NetworkDataContainer<T, E>> {
                    return dataFlow.take(1).transformLatest { cacheData ->
                        debugCache("cacheData = $cacheData")
                        try {
                            debugCache("within try")
                            val result = cacheData.data
                            if (result == null || (predicate(result, cacheData.creationTime?.let { Instant.fromEpochMilliseconds(it) } ?: Clock.System.now()) && shouldFetchNewData(
                                    cacheData,
                                    forceRefresh || trigger.forceRefresh,
                                    environment
                                ))
                            ) {
                                emit(NetworkDataContainer.Loading(result, 0))
                                debugCache("Loading")

                                val response = fetcher.invoke()

                                if (response is Success) {
                                    updateData(BlockedCacheData(response.data, nowProvider.now()))
                                } else if (response is Failure && response.throwable !is CancellationException && isExpired(cacheData)) {
                                    updateData(BlockedCacheData(null, null))
                                }

                                if (forceRefresh) lastForceRefresh.value = nowProvider.now()
                                when (response) {
                                    is Success -> {
                                        emit(NetworkDataContainer.Success(response.data))
                                        debugCache("new data Success")
                                    }
                                    is Failure -> {
                                        emit(
                                            NetworkDataContainer.Error(
                                                response,
                                                cacheData.data,
                                                cacheData.creationTime
                                            )
                                        )
                                        debugCache("new data Failure")
                                    }
                                }
                            } else {
                                emit(NetworkDataContainer.Success(result))
                                debugCache("cached data Success (${cacheData.creationTime})")
                            }

                        } finally {
                            debugCache("🟢 unlock")
                            mutex.unlock()
                            lockedByMe = false
                        }
                    }.onCompletion {
                        if (mutex.isLocked && lockedByMe) {
                            debugCache("🟢 unlock in completion")
                            mutex.unlock()
                            lockedByMe = false
                        }
                    }
                }

                condition.distinctUntilChanged().flatMapLatest { shouldFetch ->
                    debugCache("shouldFetch = $shouldFetch")
                    if (!shouldFetch) {
                        return@flatMapLatest dataFlow.take(1).transformLatest { cacheData ->
                            emit(cacheData.asCommonDataContainer())
                        }
                    }

                    if (!mutex.tryLock()) {
                        debugCache("locked")
                        flow {
                            emit(NetworkDataContainer.Loading(null, 0))

                            mutex.lock()
                            lockedByMe = true
                            debugCache("🔴 lock")

                            getData().collect { value ->
                                try {
                                    debugCache("within collect")

                                    emit(value)
                                } catch (e: Throwable) {
                                    debug("🔴 e: $e")
                                }
                            }
                        }
                    } else {
                        lockedByMe = true
                        debugCache("🔴 lock")
                        getData()
                    }
                }
            }
            apiResponseFlow
        }

        return responseFlow.flatMapLatest { state ->
            combine(dataFlow, networkStatusFlow) { data, networkStatus ->
                when (state) {
                    is NetworkDataContainer.Loading -> NetworkDataContainer.Loading(data.data, state.creationTimeStaleData)
                    is NetworkDataContainer.Success -> data.asCommonDataContainer()
                    is NetworkDataContainer.Error -> when(networkStatus) {
                        NetworkStatus.AVAILABLE, NetworkStatus.UNKNOWN -> state.copy(staleData = data.data, creationTimeStaleData = data.creationTime)
                        NetworkStatus.UNAVAILABLE -> NetworkDataContainer.Offline(staleData = data.data, creationTimeStaleData = data.creationTime)
                    }
                    is NetworkDataContainer.Empty -> NetworkDataContainer.Empty()
                    is NetworkDataContainer.Offline -> NetworkDataContainer.Offline(data.data, state.creationTimeStaleData)
                }
            }
        }.onCompletion {
            debugCache("🟢 unlock on completion")
            if (mutex.isLocked && lockedByMe) {
                mutex.unlock()
            }
        }
    }

    private fun shouldFetchNewData(
        cacheData: BlockedCacheData<T>,
        forceRefresh: Boolean,
        environment: ModelEnvironment
    ): Boolean {
        val now = nowProvider.now()
        return (shouldRefresh(cacheData, refreshTime, now)
                || shouldforceRefresh(cacheData, forceRefresh, now)
                || (environment.isMock && (cacheData.creationTime
            ?: 0L) + 5.seconds.inWholeMilliseconds < now))
    }

    private fun shouldforceRefresh(
        cacheData: BlockedCacheData<T>,
        forceRefresh: Boolean,
        now: Long): Boolean {
        return forceRefresh && (lastForceRefresh.value + FORCE_REFRESH_DELAY) < now && (cacheData.creationTime ?: 0L) + FORCE_REFRESH_DELAY < now
    }

    private fun shouldRefresh(
        cacheData: BlockedCacheData<T>,
        expireTimeMillis: Long,
        now: Long
    ): Boolean {
        return (cacheData.creationTime ?: 0L) + expireTimeMillis < now
    }

    private fun isExpired(cacheData: BlockedCacheData<T>): Boolean =
        expireTime != null && (cacheData.creationTime ?: 0L) + expireTime < nowProvider.now()

    fun refresh(forceRefresh: Boolean = true) {
        refreshTriggerState.value = RefreshTrigger(forceRefresh = forceRefresh)
    }

    companion object {
        private const val FORCE_REFRESH_DELAY = 5000 // After 5 seconds a new force refresh can be performed
    }
}

fun <T, E> BlockedCacheData<T>.asCommonDataContainer(): NetworkDataContainer<T, E> =
        data?.let { NetworkDataContainer.Success(it) } ?: NetworkDataContainer.Empty()