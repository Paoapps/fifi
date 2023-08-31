package com.paoapps.fifi.sample.api

import com.paoapps.fifi.api.domain.ApiResponse
import com.paoapps.fifi.sample.domain.Coffee

interface CoffeeApi {
    suspend fun hotCoffee(): ApiResponse<List<Coffee>, Unit>
}
