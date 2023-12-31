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

private data class Poll(val delayTime: Duration, val initial: Boolean = false)

// TODO: implement when needed
//fun <T: CacheResult<D>, D> Flow<T>.poll(
//    delayDuration: Duration = 1.minutes,
//    errorDelayDuration: Duration = 5.seconds,
//    delayFactor: Int = 2
//): Flow<T> {
//    var currentErrorDelay = errorDelayDuration
//    val delayDurationFlow = MutableStateFlow(Poll(delayTime = delayDuration, initial = true))
//    return delayDurationFlow
//        .flatMapLatest { poll ->
//            flow {
//                if (poll.initial) {
//                    emit(Unit)
//                }
//                while(true) {
//                    delay(poll.delayTime)
//                    emit(Unit)
//                }
//            }
//        }
//        .flatMapLatest {
//            this.map {
//                if (it.isFailure) {
//                    delayDurationFlow.value = Poll(currentErrorDelay)
//                    currentErrorDelay *= delayFactor
//                } else if (it.isSuccess) {
//                    delayDurationFlow.value = Poll(delayDuration)
//                    currentErrorDelay = errorDelayDuration
//                }
//                it
//            }
//        }
//}
