package com.paoapps.fifi.sample.viewmodel

import com.paoapps.fifi.viewmodel.AbstractViewModel
import com.paoapps.fifi.viewmodel.VoidEvent

abstract class CoffeeDetailViewModel: AbstractViewModel<CoffeeDetailViewModel.Output, VoidEvent, Unit>() {
    data class Output(
        val title: String,
        val description: String,
        val ingredients: String,
    )
}