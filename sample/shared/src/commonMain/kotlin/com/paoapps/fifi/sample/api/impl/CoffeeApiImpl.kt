package com.paoapps.fifi.sample.api.impl

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.api.ApiHelper
import com.paoapps.fifi.api.decodeFromString
import com.paoapps.fifi.api.withinTryCatch
import com.paoapps.fifi.log.debug
import com.paoapps.fifi.sample.api.CoffeeApi
import com.paoapps.fifi.sample.domain.Coffee
import io.ktor.client.request.get
import kotlinx.serialization.builtins.ListSerializer

class CoffeeApiImpl(
    private val apiHelper: ApiHelper,
    private val baseUrl: String
): CoffeeApi {
    override suspend fun hotCoffee(): FetcherResult<List<Coffee>> {
        debug("🔴 request hotCoffee")
        return withinTryCatch {
            apiHelper.client.get("$baseUrl/hot") {
                apiHelper.createHeaders(this)
            }
                .decodeFromString(ListSerializer(Coffee.serializer()))
        }
    }

}
