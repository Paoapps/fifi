package com.paoapps.fifi.viewmodel.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.paoapps.fifi.viewmodel.AbstractEvent
import com.paoapps.fifi.viewmodel.AbstractViewModel

/**
 * Binds a FiFi [AbstractViewModel] to Compose: collects [AbstractViewModel.output] and
 * [AbstractViewModel.action], forwarding actions to [onAction].
 *
 * For nested component events, wrap content with your own `EventHandler` CompositionLocal.
 */
@Composable
fun <Output, Event : AbstractEvent, Action> ViewModelComposable(
    viewModel: AbstractViewModel<Output, Event, Action>,
    onAction: (Action) -> Unit = {},
    onGlobalAction: (Any) -> Unit = {},
    content: @Composable (Output) -> Unit,
) {
    val output by viewModel.output.collectAsState(initial = null)

    LaunchedEffect(viewModel.action) {
        viewModel.action.collect(onAction)
    }

    LaunchedEffect(viewModel.globalActions) {
        viewModel.globalActions.collect(onGlobalAction)
    }

    output?.let(content)
}
