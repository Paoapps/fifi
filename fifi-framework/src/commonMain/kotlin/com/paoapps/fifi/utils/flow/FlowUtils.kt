package com.paoapps.fifi.utils.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.time.Duration

fun <T> Flow<T>.wrap(scope: CoroutineScope): FlowAdapter<T> =
    FlowAdapter(scope, this)

fun <T> FlowAdapter<T>.distinctUntilChanged(): FlowAdapter<T> = FlowAdapter(scope, flow.distinctUntilChanged())

fun <T> FlowAdapter<T>.debug(message: String): FlowAdapter<T> = FlowAdapter(scope, internalDebug(message))
fun <T> Flow<T>.debug(message: String, describeValue: (T) -> (String) = { it.toString() }): Flow<T> = internalDebug(message, describeValue)


private val maxContentLength = 5000
fun <T> Flow<T>.internalDebug(message: String, describeValue: (T) -> (String) = { it.toString() }): Flow<T> = onStart {
    com.paoapps.fifi.log.debug(
        "Start ${message}"
    )
}.onEach {
    val content = describeValue(it)

    com.paoapps.fifi.log.debug(
        "${message}: $content"
    )
}.onCompletion { exception ->
    com.paoapps.fifi.log.debug(
        "Completed ${message}: $exception"
    )
}
