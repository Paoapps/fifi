package com.paoapps.fifi.sample.viewmodel

import com.paoapps.fifi.sample.model.CoffeeModel
import com.paoapps.fifi.utils.ActionHandler
import com.paoapps.fifi.viewmodel.viewModelOutput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import org.koin.core.component.inject

class HomeViewModelImpl: HomeViewModel() {

    private data class State(
        val index: Int = 0
    )

    private val stateFlow = MutableStateFlow(State())

    private val coffeeModel: CoffeeModel by inject()

    private val _coffee = createRefreshableFetchFlow(coffeeModel::coffee)

    private val _output = combine(stateFlow, _coffee) { state, coffeeContainer ->
        Output(
            title = "Fifi sample app - browse coffee",
            buttons = listOf(
                Output.Button("Previous", Event.Previous),
                Output.Button("Next", Event.Next),
            ),
            loadingState = coffeeContainer.loadingState,
            item = coffeeContainer.actualOrStaleData?.getOrNull(state.index)?.let { coffee ->
                Output.Item(
                    title = coffee.title,
                    description = coffee.description,
                    onClick = Event.OpenDetail(coffee.id)
                )
            },
        )
    }

    override val output = _output.viewModelOutput(viewModelScope)

    override suspend fun handleEvent(event: Event): ActionHandler.EventResult<Action, Event, Unit>? {
        return when(event) {
            Event.Next -> {
                val coffee = _coffee.first().actualOrStaleData
                if ((stateFlow.value.index + 1) < (coffee?.size ?: 0)) {
                    stateFlow.getAndUpdate { it.copy(index = it.index + 1) }
                }
                null
            }
            Event.Previous -> {
                if (stateFlow.value.index > 0) {
                    stateFlow.getAndUpdate { it.copy(index = it.index - 1) }
                }
                null
            }
            is Event.OpenDetail -> ActionHandler.EventResult.Action(Action.Detail(event.id))
        }
    }
}
