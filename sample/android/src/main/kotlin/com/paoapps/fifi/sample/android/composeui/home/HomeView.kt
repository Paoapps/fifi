package com.paoapps.fifi.sample.android.composeui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paoapps.fifi.common.LocalEventHandler
import com.paoapps.fifi.sample.android.composeui.ViewModelComposable
import com.paoapps.fifi.sample.viewmodel.HomeViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeView(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = getViewModel(),
    openDetail: (Int) -> Unit
) {
    ViewModelComposable(
        viewModel = viewModel,
        onAction = { action ->
            when(action) {
                is HomeViewModel.Action.Detail -> {
                    openDetail(action.id)
                }
            }
        }
    ) { output ->

        val eventHandler = LocalEventHandler<HomeViewModel.Event>().current

        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(text = output.title)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                output.buttons.forEach { button ->
                    Button(onClick = {
                        eventHandler.handle(button.onClick)
                    }) {
                        Text(text = button.title)
                    }
                }
            }

            if (output.isLoading) {
                Text(text = "Loading...")
            }

            output.item?.let {
                Text(text = it.title)
                Text(text = it.description)
                Button(onClick = {
                    eventHandler.handle(it.onClick)
                }) {
                    Text(text = "Open Detail")
                }
            }
        }
    }
}
