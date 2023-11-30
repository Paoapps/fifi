package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.Tokens
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.utils.flow.FlowAdapter
import kotlinx.coroutines.flow.Flow

interface Model<ModelData, MockConfig, AccessTokenClaims: IdentifiableClaims, Environment: ModelEnvironment, UserId, Api: ClientApi<AccessTokenClaims>> {
    val userIdFlow: Flow<UserId?>
    val modelData: CDataContainer<ModelData>
    val mockConfigData: CDataContainer<MockConfig>
    val apiFlow: Flow<Api>

    val environmentFlow: FlowAdapter<Environment>

    fun updateEnvironment(environment: Environment)
    fun registerLaunch(version: String)

    fun storeTokens(tokens: Tokens)
}
