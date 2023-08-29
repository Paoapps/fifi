package com.paoapps.fifi.viewmodel
// TODO: Remove
//import com.paoapps.fifi.api.domain.ApiResponse
//import com.paoapps.fifi.api.domain.Failure
//import com.paoapps.fifi.api.domain.Success
//import com.paoapps.fifi.domain.network.LoadingState
//import com.paoapps.fifi.domain.network.NetworkDataContainer
//import com.paoapps.fifi.domain.network.asCommonDataContainer
//import com.paoapps.fifi.utils.flow.CFlow
//import com.paoapps.fifi.utils.flow.FlowRefreshTrigger
//import com.paoapps.fifi.utils.flow.wrap
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.yield
//
//typealias BaseCommonViewModel<State, ServerError> = ViewModelState<State, ServerError>
//
//open class ViewModelState<State, ServerError>(state: State, val scope: CoroutineScope) {
//    private val flowRefreshTrigger: MutableStateFlow<FlowRefreshTrigger> = MutableStateFlow(
//        FlowRefreshTrigger.FromCache()
//    )
//    val stateFlow = MutableStateFlow(state)
//    protected val baseScope = scope
//
//    open val loadingStateFlow: CFlow<LoadingState> = flowOf(LoadingState.Idle).wrap()
//
//    fun refresh() {
//        baseScope.launch {
//            yield()
//            flowRefreshTrigger.value = FlowRefreshTrigger.Refresh()
//        }
//    }
//
//    fun updateState(update: suspend (State) -> (State)) {
//        baseScope.launch {
//            val updated = update(stateFlow.value)
//            stateFlow.value = updated
//        }
//    }
//
//    fun <R, F> createRefreshableFlow(
//        data: suspend (refresh: Boolean) -> ApiResponse<R, ServerError>,
//        transform: (R, State) -> F
//    ): Flow<NetworkDataContainer<F, ServerError>> {
//        return flowRefreshTrigger.flatMapLatest { trigger ->
//            val refresh = trigger is FlowRefreshTrigger.Refresh
//            stateFlow.flatMapLatest { state ->
//                flow {
//                    emit(NetworkDataContainer.Loading(null, 0L))
//                    when (val apiResponse = data(refresh)) {
//                        is Success -> emit(
//                            NetworkDataContainer.Success(
//                                transform(
//                                    apiResponse.data,
//                                    state
//                                )
//                            )
//                        )
//                        is Failure -> emit(NetworkDataContainer.Error(apiResponse.map()))
//                    }
//                }
//            }
//        }
//    }
//
//    fun suspendRefresh(done: (()->(Unit))) {
//        refresh()
//
//        scope.launch {
//            loadingStateFlow
//                .filterNot { it.isLoading }.take(1).collect()
//            done()
//        }
//    }
//
//    fun <R> createApiCallRefreshableFlow(
//        data: suspend (refresh: Boolean) -> ApiResponse<R, ServerError>,
//    ): Flow<NetworkDataContainer<R, ServerError>> {
//        return flowRefreshTrigger.flatMapLatest { trigger ->
//            val refresh = trigger is FlowRefreshTrigger.Refresh
//            flow {
//                emit(NetworkDataContainer.Loading(null, 0L))
//                val apiResponse = data(refresh)
//                emit(apiResponse.asCommonDataContainer())
//            }
//        }.distinctUntilChanged()
//    }
//
//    fun <R> createRefreshableFlow(data: (refresh: Boolean) -> Flow<R>): Flow<R> {
//        return flowRefreshTrigger.flatMapLatest { trigger ->
//            val refresh = trigger is FlowRefreshTrigger.Refresh
//            data(refresh)
//        }.distinctUntilChanged()//.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
//    }
//
////    protected fun <R> createRefreshableFetchFlow(data: (refresh: Fetch) -> Flow<CommonDataContainer<R>>): Flow<CommonDataContainer<R>> = createRefreshableFlow { data(if (it) Fetch.FORCE else Fetch.CACHE) }
//
////    protected fun <R> apiRequestToFlow(
////        request: suspend () -> ApiResponse<R>,
////        success: (R) -> Unit = {}
////    ): Flow<CommonDataContainer<R>> {
////
////        return flow {
////            emit(CommonDataContainer.Loading())
////
////            when (val response = request()) {
////                is Success -> {
////                    emit(CommonDataContainer.Success(response.data))
////                    success(response.data)
////                }
////                is Failure -> {
////                    emit(CommonDataContainer.Failure.create(0))
////                }
////            }
////        }
////    }
//}