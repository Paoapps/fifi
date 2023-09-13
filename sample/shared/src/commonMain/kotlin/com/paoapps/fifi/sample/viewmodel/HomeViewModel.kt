package com.paoapps.fifi.sample.viewmodel

import com.paoapps.fifi.domain.network.LoadingState
import com.paoapps.fifi.viewmodel.AbstractEvent
import com.paoapps.fifi.viewmodel.AbstractViewModel

abstract class HomeViewModel: AbstractViewModel<HomeViewModel.Output, HomeViewModel.Event, HomeViewModel.Action>() {

    data class Output(
        val title: String,
        val buttons: List<Button>,
        val loadingState: LoadingState,
        val item: Item?
    ) {
        data class Button(
            val title: String,
            val onClick: Event
        )

        data class Item(
            val title: String,
            val description: String,
            val onClick: Event
        )
    }

    sealed class Event: AbstractEvent() {
        object Previous: Event()
        object Next: Event()
        data class OpenDetail(val id: Int): Event()
    }

    sealed class Action {
        data class Detail(val id: Int): Action()
    }
}
