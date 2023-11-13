package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.utils.flow.FlowAdapter
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.MainScope

val <ModelData, AccessTokenClaims: IdentifiableClaims, Environment: ModelEnvironment, UserId, Api: ClientApi<AccessTokenClaims>> Model<ModelData, AccessTokenClaims, Environment, UserId, Api>.dataFlow: FlowAdapter<ModelData?> get() = modelData.dataFlow.wrap(MainScope())
