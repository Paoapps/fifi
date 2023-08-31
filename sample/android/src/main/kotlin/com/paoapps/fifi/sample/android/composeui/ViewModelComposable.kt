package com.paoapps.fifi.sample.android.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.paoapps.fifi.common.EventHandler
import com.paoapps.fifi.common.LocalEventHandler
import com.paoapps.fifi.viewmodel.AbstractEvent
import com.paoapps.fifi.viewmodel.AbstractViewModel

val LocalLoading = compositionLocalOf { false }

// TODO: ideally, this should also come with the framework

@Composable
fun <Output, Event : AbstractEvent, Action, GlobalAction> ViewModelComposable(
    viewModel: AbstractViewModel<Output, Event, Action, GlobalAction>,
    onAction: (Action) -> Unit = {},
    content: @Composable (Output) -> Unit
) {
    ViewModelLayout(
        viewModel = viewModel,
        onAction
    ) { output ->
        CompositionLocalProvider(LocalEventHandler<Event>() provides EventHandler { event, data ->
            viewModel.emitEvent(event, data)
        }) {
            content.invoke(output)
        }
    }
}

@Composable
private fun <Output, Event : AbstractEvent, Action, GlobalAction> ViewModelLayout(
    viewModel: AbstractViewModel<Output, Event, Action, GlobalAction>,
    onAction: (Action) -> Unit = {},
    content: @Composable (Output) -> Unit
) {
    val output by viewModel.output.collectAsState(initial = null)

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    LaunchedEffect(viewModel.globalActions) {
        viewModel.globalActions.collect { action ->
            when(action) {
            }
        }
    }

    // FIXME: support toasts

    LaunchedEffect(viewModel.action) {
        viewModel.action.collect {
            onAction(it)
        }
    }

    output?.let { data ->
        content(data)
    }

}
