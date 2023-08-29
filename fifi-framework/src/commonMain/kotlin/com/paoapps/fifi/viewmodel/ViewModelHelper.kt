package com.paoapps.fifi.viewmodel

import com.paoapps.fifi.utils.flow.FlowAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

fun <T> Flow<T>.viewModelOutput(scope: CoroutineScope): FlowAdapter<T> = FlowAdapter(scope, this.distinctUntilChanged())
