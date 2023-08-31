package com.paoapps.fifi.common

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.paoapps.fifi.viewmodel.AbstractEvent

data class EventHandler<Event>(private val onEvent: (Event, Any?) -> Unit) {
    fun handle(event: Event?, input: Any? = null) = event?.let { onEvent(it, input) }
}

val emptyEventHandler = compositionLocalOf {
    EventHandler<Any> { event, _ ->
        assert(false) { "LocalEventHandler missing for event $event" }
    }
}

fun <Event: AbstractEvent> LocalEventHandler(): ProvidableCompositionLocal<EventHandler<Event>> =
    emptyEventHandler as ProvidableCompositionLocal<EventHandler<Event>>
