package com.paoapps.fifi.sample.api.mock

import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.CoffeeApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockApi: Api {
    override val environment: ModelEnvironment = AppModelEnvironment.Mock
    override val claimsFlow: Flow<IdentifiableClaims?> = flowOf(null)

    override val coffeeApi: CoffeeApi = MockCoffeeApi()
}
