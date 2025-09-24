package com.paoapps.fifi.sample.model

import com.paoapps.fifi.model.ModelImpl
import com.paoapps.fifi.sample.api.Api
import kotlinx.coroutines.CoroutineScope

class AppModelImpl(
    scope: CoroutineScope,
): ModelImpl<AppModelEnvironment, Api>(scope), AppModel
