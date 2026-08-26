package com.paoapps.fifi.viewmodel

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class CollectedActions<Action>(
    val actions: List<Action> = emptyList(),
    val globalActions: List<Any> = emptyList(),
)

data class CollectedActionsExpectations<Action>(
    val containsActions: Boolean? = null,
    val containsGlobalActions: Boolean? = null,
    val containsAnyActions: Boolean = true,
    val verify: CollectedActions<Action>.() -> Unit = {},
) {
    fun matches(actions: CollectedActions<Action>): Boolean =
        (containsActions == null || containsActions == actions.actions.isNotEmpty()) &&
            (containsGlobalActions == null || containsGlobalActions == actions.globalActions.isNotEmpty()) &&
            (containsAnyActions == (actions.actions.isNotEmpty() || actions.globalActions.isNotEmpty()))
}

fun <Output, Event : AbstractEvent, Action> AbstractViewModel<Output, Event, Action>.testOutput(
    timeout: Duration = 5.seconds,
    block: suspend Output.() -> Unit,
) = runBlocking {
    val firstOutput = try {
        withTimeout(timeout) {
            output.take(1).first()
        }
    } catch (_: TimeoutCancellationException) {
        throw AssertionError("Output not emitted within $timeout")
    }
    block(firstOutput)
}

@OptIn(DelicateCoroutinesApi::class)
fun <Output, Event : AbstractEvent, Action> AbstractViewModel<Output, Event, Action>.testActions(
    expectations: CollectedActionsExpectations<Action> = CollectedActionsExpectations(),
    timeout: Duration = 5.seconds,
    block: suspend Output.((Event) -> Unit) -> Unit,
): CollectedActions<Action> = runBlocking {
    var collected = CollectedActions<Action>()

    val actionJob = launch {
        action.collect {
            collected = collected.copy(actions = collected.actions + it)
        }
    }

    val globalActionJob = launch {
        globalActions.collect {
            collected = collected.copy(globalActions = collected.globalActions + it)
        }
    }

    assertTrue(collected.actions.isEmpty())
    assertTrue(collected.globalActions.isEmpty())

    try {
        testOutput {
            block { emitEvent(it) }

            try {
                withTimeout(timeout) {
                    delay(1)
                    while (!expectations.matches(collected)) {
                        delay(100)
                    }
                }
                expectations.verify(collected)
            } catch (_: TimeoutCancellationException) {
                throw AssertionError("Expectations not met within $timeout")
            }
        }
    } finally {
        actionJob.cancel()
        globalActionJob.cancel()
    }

    collected
}
