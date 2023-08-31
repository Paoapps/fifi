package com.paoapps.fifi.sample.viewmodel

import com.paoapps.fifi.sample.model.CoffeeModel
import com.paoapps.fifi.viewmodel.viewModelOutput
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.component.inject

class CoffeeDetailViewModelImpl(
    val id: Int
): CoffeeDetailViewModel() {

    private val coffeeModel: CoffeeModel by inject()

    private val _coffee = createRefreshableFetchFlow(coffeeModel::coffee).mapNotNull { it.actualOrStaleData?.firstOrNull { it.id == id }}

    private val _output = _coffee.map { coffee ->
        Output(
            title = coffee.title,
            description = coffee.description,
            ingredients = coffee.ingredients.takeIf { it.isNotEmpty() }?.let {
                 "Ingredients: ${it.joinToString(", ")}"
            } ?: "No ingredients",
        )
    }

    override val output = _output.viewModelOutput(viewModelScope)
}
