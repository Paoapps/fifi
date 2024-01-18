package com.paoapps.fifi.sample.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.Fetch
import com.paoapps.fifi.model.ModelHelper
import com.paoapps.fifi.sample.PersistentDataName
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.domain.Coffee
import com.paoapps.fifi.sample.domain.ModelData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.qualifier
import kotlin.time.Duration.Companion.minutes

class CoffeeModelImpl: CoffeeModel, KoinComponent {
    private val modelHelper: ModelHelper<ModelData, Api> by inject(PersistentDataName.MODEL_DATA.qualifier)

    private val hotCoffeeCache: BlockedCache<List<Coffee>> = modelHelper.createBlockCache(5.minutes, null, ModelData::hotCoffee, isDebugEnabled = true)

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
