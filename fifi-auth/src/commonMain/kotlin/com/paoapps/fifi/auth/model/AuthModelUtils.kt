package com.paoapps.fifi.auth.model

import com.paoapps.blockedcache.BlockedCache
import com.paoapps.blockedcache.BlockedCacheData
import com.paoapps.fifi.auth.api.AuthClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.model.ModelEnvironment
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

fun <T: Any, ModelData, MockConfig, AccessTokenClaims : IdentifiableClaims<UserId>, Environment : ModelEnvironment, UserId, Api : AuthClientApi<UserId, AccessTokenClaims>> createBlockCache(
    model: AuthModel<ModelData, MockConfig, AccessTokenClaims, Environment, UserId, Api>,
    duration: Duration,
    expire: Duration?,
    selector: (ModelData) -> BlockedCacheData<T>?,
    name: String,
    triggerOnUserIdChange: Boolean = true,
    isDebugEnabled: Boolean = false
): BlockedCache<T> {

    val userIdChangedFlow = if (triggerOnUserIdChange) model.userIdFlow else flowOf(Unit)

    return com.paoapps.fifi.model.createBlockCache(model, duration, expire, selector, name, userIdChangedFlow, isDebugEnabled)
}
