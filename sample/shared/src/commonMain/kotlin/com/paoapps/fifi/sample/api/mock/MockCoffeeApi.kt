package com.paoapps.fifi.sample.api.mock

import com.paoapps.blockedcache.FetcherResult
import com.paoapps.fifi.sample.api.CoffeeApi
import com.paoapps.fifi.sample.domain.Coffee

class MockCoffeeApi: CoffeeApi {
    override suspend fun hotCoffee(): FetcherResult<List<Coffee>> {
        return FetcherResult.Data(listOf(
            Coffee(
                id = 1,
                title = "Cappuccino",
                description = "A cappuccino is an espresso-based coffee drink that originated in Italy, and is traditionally prepared with steamed milk foam.",
                ingredients = listOf(
                    "1/3 espresso",
                    "1/3 steamed milk",
                    "1/3 milk foam",
                ),
            ),
            Coffee(
                id = 2,
                title = "Latte",
                description = "A latte is a coffee drink made with espresso and steamed milk.",
                ingredients = listOf(
                    "1/3 espresso",
                    "2/3 steamed milk",
                ),
            ),
            Coffee(
                id = 3,
                title = "Americano",
                description = "Caffè Americano or Americano is a type of coffee drink prepared by diluting an espresso with hot water, giving it a similar strength to, but different flavor from, traditionally brewed coffee.",
                ingredients = listOf(
                    "1/3 espresso",
                    "2/3 hot water",
                ),
            ),
            Coffee(
                id = 4,
                title = "Espresso",
                description = "Espresso is a coffee-brewing method of Italian origin, in which a small amount of nearly boiling water is forced under pressure through finely-ground coffee beans.",
                ingredients = listOf(
                    "1/3 espresso",
                ),
            ),
            Coffee(
                id = 5,
                title = "Mocha",
                description = "A caffè mocha, also called mocaccino, is a chocolate-flavored variant of a caffè latte.",
                ingredients = listOf(
                    "1/3 espresso",
                    "1/3 steamed milk",
                    "1/3 chocolate syrup",
                ),
            ),
        ))
    }

}
