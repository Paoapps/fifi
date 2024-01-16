package com.paoapps.fifi.sample.api.mock

import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.CoffeeApi
import com.paoapps.fifi.sample.model.AppModelEnvironment

class MockApi: Api {
    override val environment: ModelEnvironment = AppModelEnvironment.Mock

    override val coffeeApi: CoffeeApi = MockCoffeeApi()
}
