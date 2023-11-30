package com.paoapps.fifi.sample.api

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.sample.domain.Coffee

interface CoffeeApi {
    suspend fun hotCoffee(): FetcherResult<List<Coffee>>
}
