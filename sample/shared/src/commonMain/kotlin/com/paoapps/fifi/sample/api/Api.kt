package com.paoapps.fifi.sample.api

import com.paoapps.fifi.api.ClientApi

interface Api: ClientApi {
    val coffeeApi: CoffeeApi
}
