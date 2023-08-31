package com.paoapps.fifi.sample.domain

import com.paoapps.fifi.domain.LaunchData
import com.paoapps.fifi.domain.cache.BlockedCacheData
import kotlinx.serialization.Serializable

@Serializable
data class ModelData(
    val appData: AppData,
    val hotCoffee: BlockedCacheData<List<Coffee>> = BlockedCacheData(),
)

@Serializable
data class AppData(
    val launchData: LaunchData,
)
