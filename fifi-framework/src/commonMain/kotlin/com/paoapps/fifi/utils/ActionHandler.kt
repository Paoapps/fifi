package com.paoapps.fifi.utils

import com.paoapps.fifi.ui.component.ConfirmationDialogDefinition
import com.paoapps.fifi.utils.flow.FlowAdapter
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface Emitter<A, E> {
    fun event(event: E)
    fun action(action: A)
}

data class ActionHandler<A, E>(val scope: CoroutineScope, private val handler: suspend (E, Any?) -> EventResult<A, E>?) {

    val globalActionsFlow = MutableSharedFlow<Any>()
    val globalActions = globalActionsFlow.asSharedFlow().wrap(scope)

    val actionsFlow = MutableSharedFlow<A>()
    val actions = FlowAdapter(scope, actionsFlow.asSharedFlow())

    private val _links = MutableSharedFlow<String>()
    val links = _links.asSharedFlow().wrap(scope)

    val events = MutableSharedFlow<Pair<E, Any?>>()

    private val _confirmationDialogs = MutableSharedFlow<ConfirmationDialogDefinition.Properties<E>>()
    val confirmationDialogs = _confirmationDialogs.asSharedFlow().wrap(scope)

    sealed interface EventResult<A, E> {
        data class Event<A, E>(val event: E): EventResult<A, E>
        data class Action<A, E>(val action: A): EventResult<A, E>
//        data class Link<A, E>(val link: String): EventResult<A, E>
        data class ConfirmationDialog<A, E>(val confirmationDialog: ConfirmationDialogDefinition.Properties<E>): EventResult<A, E>
        data class Global<A, E>(val action: Any): EventResult<A, E>
    }

    init {
        scope.launch {
            events.collect {
                handleEvent(it.first, it.second)
            }
        }
    }

    suspend fun emitEvent(event: E) {
        events.emit(Pair(event, null))
    }

    fun <Output, Data> mapToOutput(dataFlow: Flow<Data>, transform: suspend (Data, Emitter<A, E>) -> Output): Flow<Output> =
        flatMapToOutput(dataFlow) { data, emitEvent ->
            flowOf(transform(data, emitEvent))
        }

    fun <Output> mapToOutput(transform: (Emitter<A, E>) -> Output): Flow<Output> =
        flatMapToOutput { emitEvent ->
            flowOf(transform(emitEvent))
        }

    fun <Output, Data> flatMapToOutput(dataFlow: Flow<Data>, transform: suspend (Data, Emitter<A, E>) -> Flow<Output>): Flow<Output> =
        dataFlow.flatMapConcat { data ->
            val scope = this.scope
            val events = this.events
            val actions = this.actionsFlow
            transform(data, object: Emitter<A, E> {
                override fun event(event: E) {
                    scope.launch {
                        events.emit(Pair(event, null))
                    }
                }

                override fun action(action: A) {
                    scope.launch {
                        actions.emit(action)
                    }
                }
            })
        }
            .distinctUntilChanged()


    fun <Output> flatMapToOutput(transform: (Emitter<A, E>) -> Flow<Output>): Flow<Output> = flatMapToOutput(flowOf(Unit)) { _, emitEvent ->
        transform(emitEvent)
    }

    fun <Output> toOutput(transform: (Emitter<A, E>) -> Output): Output {
        val scope = this.scope
        val events = this.events
        val actions = this.actionsFlow
        return transform(object: Emitter<A, E> {
            override fun event(event: E) {
                scope.launch {
                    events.emit(Pair(event, null))
                }
            }

            override fun action(action: A) {
                scope.launch {
                    actions.emit(action)
                }
            }
        })
    }

    private suspend fun handleEvent(event: E, input: Any?) {
        when(val result = handler(event, input)) {
            is EventResult.Action -> actionsFlow.emit(result.action)
            is EventResult.Event -> handleEvent(result.event, input)
//            is EventResult.Link -> _links.emit(result.link)
            is EventResult.ConfirmationDialog -> _confirmationDialogs.emit(result.confirmationDialog)
            is EventResult.Global -> globalActionsFlow.emit(result.action)
            null -> {}
        }
    }

}
