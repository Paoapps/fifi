package com.paoapps.fifi.viewmodel

import com.paoapps.blockedcache.Fetch
import com.paoapps.fifi.log.warn
import com.paoapps.fifi.ui.component.ToastDefinition
import com.paoapps.fifi.utils.ActionHandler
import com.paoapps.fifi.utils.flow.FlowAdapter
import com.paoapps.fifi.utils.flow.FlowRefreshTrigger
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.core.component.KoinComponent

abstract class AbstractEvent(val animate: Boolean = false)
object VoidEvent : AbstractEvent()

abstract class AbstractViewModel<Output, Event: AbstractEvent, Action>(): ViewModel(), KoinComponent {
    val actionHandler = ActionHandler<Action, Event>(viewModelScope, ::_handleEvent)

    private val flowRefreshTrigger: MutableStateFlow<FlowRefreshTrigger> = MutableStateFlow(
        FlowRefreshTrigger.FromCache()
    )

    open suspend fun _handleEvent(event: Event, input: Any?): ActionHandler.EventResult<Action, Event>? {
        animateFlow.value = event.animate
        return handleEvent(event, input)
    }

    open suspend fun handleEvent(event: Event, input: Any?): ActionHandler.EventResult<Action, Event>? {
        return handleEvent(event)
    }

    open suspend fun handleEvent(event: Event): ActionHandler.EventResult<Action, Event>? {
        warn("Unhandled event: $event")
        return null
    }

    inner class OutputUpdate(
        val output: Output,
        val animate: Boolean = false
    )

    abstract val output: FlowAdapter<Output>
    val animateFlow = MutableStateFlow(false)
    val viewModelOutput by lazy {
        combine(output, animateFlow) { output, animate ->
            OutputUpdate(output, animate)
        }.wrap(viewModelScope)
    }
    open val action: FlowAdapter<Action> = actionHandler.actions

    protected val notificationsSharedFlow = MutableSharedFlow<ToastDefinition.Properties>()
    val notifications = merge(notificationsSharedFlow.asSharedFlow(), actionHandler.notifications).wrap(viewModelScope)
    val notificationDismissEvent: Event? = null

    val confirmationDialogs = actionHandler.confirmationDialogs

    val globalActions = merge(actionHandler.globalActions).wrap(viewModelScope)

    open fun emitEvent(event: Event, input: Any? = null) {
        viewModelScope.launch {
            actionHandler.events.emit(Pair(event, input))
        }
    }

    open fun refresh() {
        viewModelScope.launch {
            yield()
            flowRefreshTrigger.value = FlowRefreshTrigger.Refresh()
        }
    }

    open suspend fun suspendRefresh() {
        refresh()
    }

    fun <R> createRefreshableFlow(data: suspend (refresh: Boolean) -> Flow<R>): Flow<R> {
        return flowRefreshTrigger.flatMapLatest { trigger ->
            val refresh = trigger is FlowRefreshTrigger.Refresh
            data(refresh)
        }.distinctUntilChanged()
    }

    fun <R> createRefreshableFetchFlow(data: (refresh: Fetch) -> Flow<R>) = createRefreshableFetchFlow(default = Fetch.Cache, data = data)

    fun <R> createRefreshableFetchFlow(default: Fetch, data: (refresh: Fetch) -> Flow<R>): Flow<R> =
        createRefreshableFlow { data(if (it) Fetch.Force() else default) }
}
