package com.paoapps.fifi.utils.flow

import kotlin.random.Random

enum class Fetch {
    FORCE,
    CACHE,
    NO_FETCH;
}

sealed class FlowRefreshTrigger {
    data class FromCache(private val id: String = Random.nextInt().toString()) : FlowRefreshTrigger()

    /**
     * Every instance has a unique id so the refresh StateFlow sees it as a new value. 
     * StateFlow does not emit the same value twice if the current value is the same as the new value.
     */
    data class Refresh(private val id: String = Random.nextInt().toString()) : FlowRefreshTrigger()
}