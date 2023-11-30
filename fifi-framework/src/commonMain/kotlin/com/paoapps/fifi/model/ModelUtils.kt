package com.paoapps.fifi.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.BlockedCacheData
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.random.Random
import kotlin.time.Duration

private val AppBecameActive = MutableStateFlow<String>(Random.nextInt().toString())
val AppBecameActiveFlow = AppBecameActive
fun appBecameActiveFlow(scope: CoroutineScope) = AppBecameActive.wrap(scope)

fun appBecameActive() {
    AppBecameActive.value = Random.nextInt().toString()
}

fun <T: Any, ModelData, MockConfig, AccessTokenClaims : IdentifiableClaims, Environment : ModelEnvironment, UserId, Api : ClientApi<AccessTokenClaims>> createBlockCache(
    model: Model<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api>,
    duration: Duration,
    expire: Duration?,
    selector: (ModelData) -> BlockedCacheData<T>?,
    name: String,
    triggerOnUserIdChange: Boolean = true,
    isDebugEnabled: Boolean = false
): BlockedCache<T> {

    val userIdChangedFlow = if (triggerOnUserIdChange) model.userIdFlow else flowOf(Unit)

    val dataFlow = model.modelData.dataFlow.map {
        if (isDebugEnabled) {
            debug("createBlockCache($name)")
        }
        it?.let(selector) ?: BlockedCacheData<T>(null, null)
    }.distinctUntilChanged()

    return BlockedCache(
        duration.inWholeMilliseconds,
        expire?.inWholeMilliseconds,
        combine(AppBecameActiveFlow, userIdChangedFlow) { _, _ -> },
        dataFlow,
        name = name,
        isDebugEnabled = isDebugEnabled
    )
}
