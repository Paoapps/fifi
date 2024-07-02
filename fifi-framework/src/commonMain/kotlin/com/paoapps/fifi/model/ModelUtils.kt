package com.paoapps.fifi.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.BlockedCacheData
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.model.datacontainer.DataContainer
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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

fun <ModelData: Any, T: Any> createBlockCache(
    dataContainer: DataContainer<ModelData>,
    duration: Duration,
    expire: Duration?,
    selector: (ModelData) -> BlockedCacheData<T>?,
    name: String,
    trigger: Flow<Any?> = flowOf(Unit),
    triggerOnAppBecomeActive: Boolean = true,
    isDebugEnabled: Boolean = false,
): BlockedCache<T> {

    val dataFlow = dataContainer.dataFlow.map {
        if (isDebugEnabled) {
            debug("createBlockCache($name)")
        }
        it?.let(selector) ?: BlockedCacheData<T>(null, null)
    }.distinctUntilChanged()

    return BlockedCache(
        duration.inWholeMilliseconds,
        expire?.inWholeMilliseconds,
        if (triggerOnAppBecomeActive) {
            combine(AppBecameActiveFlow, trigger) { _, _ -> }
        } else {
            trigger.map {  }
        },
        dataFlow,
        name = name,
        isDebugEnabled = isDebugEnabled
    )
}
