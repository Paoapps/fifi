package com.paoapps.fifi.model

import com.paoapps.fifi.api.ClientApi
import com.paoapps.fifi.utils.flow.FlowAdapter
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.MainScope

val <ModelData, MockConfig, Environment: ModelEnvironment, Api: ClientApi> Model<ModelData, MockConfig, Environment, Api>.dataFlow: FlowAdapter<ModelData?> get() = modelData.dataFlow.wrap(MainScope())
