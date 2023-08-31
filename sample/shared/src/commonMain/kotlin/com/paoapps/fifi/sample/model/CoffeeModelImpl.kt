package com.paoapps.fifi.sample.model

import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.domain.cache.BlockedCache
import com.paoapps.fifi.model.ModelHelper
import com.paoapps.fifi.model.createBlockCache
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.domain.Coffee
import com.paoapps.fifi.sample.domain.ModelData
import com.paoapps.fifi.utils.flow.Fetch
import kotlin.time.Duration.Companion.minutes

class CoffeeModelImpl(
    private val modelHelper: ModelHelper<ModelData, IdentifiableClaims, Api, Unit>,
    model: AppModel
): CoffeeModel {
    private val hotCoffeeCache: BlockedCache<List<Coffee>, Unit> = createBlockCache(model, 5.minutes, null, ModelData::hotCoffee, "hotCoffee", isDebugEnabled = true)

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
