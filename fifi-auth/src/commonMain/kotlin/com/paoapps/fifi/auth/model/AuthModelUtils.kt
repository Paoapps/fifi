package com.paoapps.fifi.auth.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.BlockedCacheData
import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelHelper
import com.paoapps.fifi.model.datacontainer.DataContainer
import com.paoapps.fifi.model.createBlockCache
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

fun <ModelData: Any, T: Any, AccessTokenClaims : IdentifiableClaims<UserId>, Environment : ModelEnvironment, UserId> createBlockCache(
    authModel: AuthModel<AccessTokenClaims, Environment, UserId>,
    dataContainer: DataContainer<ModelData>,
    duration: Duration,
    expire: Duration?,
    selector: (ModelData) -> BlockedCacheData<T>?,
    name: String,
    triggerOnUserIdChange: Boolean = true,
    isDebugEnabled: Boolean = false
): BlockedCache<T> {

    val userIdChangedFlow = if (triggerOnUserIdChange) authModel.userIdFlow else flowOf(Unit)

    return createBlockCache(dataContainer, duration, expire, selector, name, userIdChangedFlow, isDebugEnabled)
}

fun <T: Any, AccessTokenClaims : IdentifiableClaims<UserId>, Environment : ModelEnvironment, UserId, Api: ClientApi> ModelHelper<BlockedCacheData<T>, Api>.createBlockCache(
    authModel: AuthModel<AccessTokenClaims, Environment, UserId>,
    duration: Duration,
    expire: Duration?,
    triggerOnUserIdChange: Boolean = true,
    isDebugEnabled: Boolean = false
): BlockedCache<T> {
    return createBlockCache(
        authModel = authModel,
        dataContainer = modelDataContainer,
        duration = duration,
        expire = expire,
        selector = { it },
        name = name,
        triggerOnUserIdChange = triggerOnUserIdChange,
        isDebugEnabled = isDebugEnabled
    )
}
