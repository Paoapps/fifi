package com.paoapps.fifi.sample.api

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims

interface Api: ClientApi<IdentifiableClaims> {
    val coffeeApi: CoffeeApi
}
