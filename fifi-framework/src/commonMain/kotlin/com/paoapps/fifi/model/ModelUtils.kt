package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.domain.cache.BlockedCache
import com.paoapps.fifi.domain.cache.BlockedCacheData
import com.paoapps.fifi.domain.network.NetworkStatusMonitor
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.random.Random
import kotlin.time.Duration

private val AppBecameActive = MutableStateFlow<String>(Random.nextInt().toString())
val AppBecameActiveFlow = AppBecameActive
fun appBecameActiveFlow(scope: CoroutineScope) = AppBecameActive.wrap(scope)

fun appBecameActive() {
    AppBecameActive.value = Random.nextInt().toString()
}

fun <T : Any, ModelData, AccessTokenClaims: IdentifiableClaims, Environment: ModelEnvironment, UserId, ServerError, Api: ClientApi<AccessTokenClaims>> createBlockCache(
    model: Model<ModelData, AccessTokenClaims, Environment, UserId, Api>,
    duration: Duration,
    expire: Duration?,
    selector: (ModelData) -> BlockedCacheData<T>?,
    name: String,
    triggerOnUserIdChange: Boolean = true,
    isDebugEnabled: Boolean = false
): BlockedCache<T, ServerError> {

    val userIdChangedFlow = if (triggerOnUserIdChange) model.userIdFlow else flowOf(Unit)

    val dataFlow = model.modelData.dataFlow.map {
        if (isDebugEnabled) {
            debug("createBlockCache($name): modelData: $it")
        }
        it?.let(selector) ?: BlockedCacheData<T>(null, null)
    }.distinctUntilChanged()

    val blockedCache = BlockedCache<T, ServerError>(
        duration.inWholeMilliseconds,
        expire?.inWholeMilliseconds,
        combine(AppBecameActiveFlow, userIdChangedFlow) { _, _ -> },
        dataFlow,
        name = name,
        isDebugEnabled = isDebugEnabled,
        networkStatusFlow = NetworkStatusMonitor.networkStatus
    )
    return blockedCache
}
