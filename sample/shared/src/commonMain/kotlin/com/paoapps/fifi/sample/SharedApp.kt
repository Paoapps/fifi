package com.paoapps.fifi.sample

import com.paoapps.fifi.api.ApiFactory
import com.paoapps.fifi.di.AppDefinition
import com.paoapps.fifi.di.PersistentDataRegistry
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelEnvironmentFactory
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.api.impl.ApiImpl
import com.paoapps.fifi.sample.api.mock.MockApi
import com.paoapps.fifi.sample.domain.ModelData
import com.paoapps.fifi.sample.model.AppModel
import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.sample.model.AppModelImpl
import com.paoapps.fifi.sample.model.CoffeeModel
import com.paoapps.fifi.sample.model.CoffeeModelImpl
import com.paoapps.fifi.sample.viewmodel.CoffeeDetailViewModel
import com.paoapps.fifi.sample.viewmodel.CoffeeDetailViewModelImpl
import com.paoapps.fifi.sample.viewmodel.HomeViewModel
import com.paoapps.fifi.sample.viewmodel.HomeViewModelImpl
import com.paoapps.fifi.viewmodel.viewModel
import kotlinx.coroutines.MainScope
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

enum class PersistentDataName {
    MODEL_DATA,
}

fun sharedAppModule() = module {
    viewModel<HomeViewModel> { HomeViewModelImpl() }
    viewModel<CoffeeDetailViewModel> { (id: Int) -> CoffeeDetailViewModelImpl(id) }
}

fun appModule() = module {
    single { get<Model<AppModelEnvironment, Api>>() as AppModel }
    single<CoffeeModel> { CoffeeModelImpl() }
}

class SampleApiFactory(
    private val appVersion: String,
    private val isDebugMode: Boolean
) : ApiFactory<Api, AppModelEnvironment> {
    override fun createApi(environment: AppModelEnvironment): Api = when(environment.environmentName) {
        AppModelEnvironment.EnvironmentName.MOCK -> MockApi()
        AppModelEnvironment.EnvironmentName.PRODUCTION -> ApiImpl(environment, appVersion, isDebugMode)
    }
}

data class SharedAppDefinition(
    override val appVersion: String,
    override val isDebugMode: Boolean
): AppDefinition<AppModelEnvironment, Api> {

    override val modules: List<Module>
        get() = listOf(
            sharedAppModule(),
            appModule(),
        )

    override val environmentFactory: ModelEnvironmentFactory<AppModelEnvironment> = object : ModelEnvironmentFactory<AppModelEnvironment> {
        override val defaultEnvironment: AppModelEnvironment
            get() = AppModelEnvironment.Production

        override fun fromName(name: String): AppModelEnvironment {
            return AppModelEnvironment.fromName(name)
        }
    }

    override fun apiFactory(appVersion: String): ApiFactory<Api, AppModelEnvironment> = SampleApiFactory(appVersion, isDebugMode)

    override fun model(): Model<AppModelEnvironment, Api> = AppModelImpl(MainScope())

    override fun dataRegistrations(): PersistentDataRegistry.() -> Unit {
        return {
            registerPersistentData(
                PersistentDataName.MODEL_DATA,
                serializer = ModelData.serializer(),
                initialData = ModelData(),
            )
        }
    }
}
