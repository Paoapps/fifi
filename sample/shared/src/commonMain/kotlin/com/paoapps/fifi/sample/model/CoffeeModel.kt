package com.paoapps.fifi.sample.model

import com.paoapps.blockedcache.CacheResult
import com.paoapps.fifi.sample.domain.Coffee
import com.paoapps.fifi.utils.flow.Fetch
import kotlinx.coroutines.flow.Flow

interface CoffeeModel {
    fun coffee(fetch: Fetch): Flow<CacheResult<List<Coffee>>>
}
