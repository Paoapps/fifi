package com.paoapps.fifi.sample.domain

import com.paoapps.blockedcache.BlockedCacheData
import com.paoapps.fifi.domain.LaunchData
import kotlinx.serialization.Serializable

@Serializable
data class ModelData(
    val hotCoffee: BlockedCacheData<List<Coffee>> = BlockedCacheData(),
)
