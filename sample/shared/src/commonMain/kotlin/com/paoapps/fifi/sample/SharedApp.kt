package com.paoapps.fifi.sample

import com.paoapps.fifi.di.AppDefinition
import com.paoapps.fifi.di.PersistentDataRegistry
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.model.ModelEnvironment
import com.paoapps.fifi.model.ModelEnvironmentFactory
import com.paoapps.fifi.sample.api.Api
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
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

enum class PersistentDataName {
    MODEL_DATA,
}

data class SharedAppDefinition(
    override val appVersion: String,
    override val isDebugMode: Boolean
): AppDefinition<AppModelEnvironment, Api> {

    override val environmentFactory: ModelEnvironmentFactory<AppModelEnvironment> = object : ModelEnvironmentFactory<AppModelEnvironment> {
        override val defaultEnvironment: AppModelEnvironment
            get() = AppModelEnvironment.Production

        override fun fromName(name: String): AppModelEnvironment {
            return AppModelEnvironment.fromName(name)
        }
    }

    override fun model(appVersion: String): AppModel = AppModelImpl(appVersion, environmentFactory.defaultEnvironment)
    override fun sharedAppModule(): Module {
        return module {
            viewModel<HomeViewModel> { HomeViewModelImpl() }
            viewModel<CoffeeDetailViewModel> { (id: Int) -> CoffeeDetailViewModelImpl(id) }
        }
    }

    override fun dataRegistrations(): PersistentDataRegistry.() -> Unit {
        return {
            registerPersistentData(
                name = PersistentDataName.MODEL_DATA,
                serializer = ModelData.serializer(),
                initialData = ModelData(),
            )
        }
    }

    override fun appDeclaration(): KoinAppDeclaration {
        return {
            modules(module {
                single { get<Model<AppModelEnvironment, Api>>() as AppModel }

                single<CoffeeModel> { CoffeeModelImpl() }
            })
        }
    }
}
