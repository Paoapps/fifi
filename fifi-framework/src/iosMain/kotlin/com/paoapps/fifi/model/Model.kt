package com.paoapps.fifi.model

import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.utils.flow.FlowAdapter
import com.paoapps.fifi.utils.flow.wrap
import kotlinx.coroutines.MainScope

val <ModelData: Any> CDataContainer<ModelData>.dataFlowAdapter: FlowAdapter<ModelData?> get() = dataFlow.wrap(MainScope())
