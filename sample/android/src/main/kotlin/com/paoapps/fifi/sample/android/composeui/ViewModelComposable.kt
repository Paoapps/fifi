package com.paoapps.fifi.sample.android.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.paoapps.fifi.sample.viewmodel.EventHandler
import com.paoapps.fifi.sample.viewmodel.LocalEventHandler
import com.paoapps.fifi.viewmodel.AbstractEvent
import com.paoapps.fifi.viewmodel.AbstractViewModel
import com.paoapps.fifi.viewmodel.compose.ViewModelComposable as FifiViewModelComposable

@Composable
fun <Output, Event : AbstractEvent, Action> ViewModelComposable(
    viewModel: AbstractViewModel<Output, Event, Action>,
    onAction: (Action) -> Unit = {},
    content: @Composable (Output) -> Unit,
) {
    FifiViewModelComposable(viewModel, onAction = onAction) { output ->
        CompositionLocalProvider(LocalEventHandler<Event>() provides EventHandler { event, data ->
            viewModel.emitEvent(event, data)
        }) {
            content(output)
        }
    }
}
