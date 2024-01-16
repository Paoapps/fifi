package com.paoapps.fifi.sample.model

import com.paoapps.blockedcache.CacheResult
import com.paoapps.blockedcache.Fetch
import com.paoapps.fifi.sample.domain.Coffee
import kotlinx.coroutines.flow.Flow

interface CoffeeModel {
    fun coffee(fetch: Fetch): Flow<CacheResult<List<Coffee>>>
}
