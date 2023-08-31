package com.paoapps.fifi.sample.model

import com.paoapps.fifi.domain.network.NetworkDataContainer
import com.paoapps.fifi.sample.domain.Coffee
import com.paoapps.fifi.utils.flow.Fetch
import kotlinx.coroutines.flow.Flow

interface CoffeeModel {
    fun coffee(fetch: Fetch): Flow<NetworkDataContainer<List<Coffee>, Unit>>
}
