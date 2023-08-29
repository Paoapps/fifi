package com.paoapps.fifi.domain.cache

import kotlinx.serialization.Serializable

@Serializable
data class BlockedCacheData<T>(
    val data: T? = null,
    val creationTime: Long? = null
)
