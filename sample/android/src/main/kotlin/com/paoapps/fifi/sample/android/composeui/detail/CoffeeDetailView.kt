package com.paoapps.fifi.sample.android.composeui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paoapps.fifi.sample.android.composeui.ViewModelComposable
import com.paoapps.fifi.sample.viewmodel.CoffeeDetailViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CoffeeDetailView(
    modifier: Modifier = Modifier,
    id: Int,
    viewModel: CoffeeDetailViewModel = getViewModel { parametersOf(id) }
) {
    ViewModelComposable(
        viewModel = viewModel,
    ) { output ->
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(text = output.title)
            Text(text = output.description)
            Text(text = output.ingredients)
        }
    }
}
