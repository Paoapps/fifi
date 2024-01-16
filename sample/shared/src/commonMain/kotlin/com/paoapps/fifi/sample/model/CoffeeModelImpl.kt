package com.paoapps.fifi.sample.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.Fetch
import com.paoapps.fifi.model.ModelHelper
import com.paoapps.fifi.model.createBlockCache
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.domain.Coffee
import com.paoapps.fifi.sample.domain.ModelData
import kotlin.time.Duration.Companion.minutes

class CoffeeModelImpl(
    private val modelHelper: ModelHelper<ModelData, Api>,
    model: AppModel
): CoffeeModel {
    private val hotCoffeeCache: BlockedCache<List<Coffee>> = createBlockCache(model, 5.minutes, null, ModelData::hotCoffee, "hotCoffee", isDebugEnabled = true)

    override fun coffee(fetch: Fetch) =
        modelHelper.withApiBlockedCacheFlow(
            blockedCache = hotCoffeeCache,
            fetch = fetch,
            apiCall = { api, _ -> api.coffeeApi.hotCoffee() },
            processData = { updatedData, modelData ->
                modelData.copy(hotCoffee = updatedData.copy(
                    data = updatedData.data
                ))
            }
        )
}
