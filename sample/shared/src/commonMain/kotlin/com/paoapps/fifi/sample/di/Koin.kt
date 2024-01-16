package com.paoapps.fifi.sample.di

import com.paoapps.fifi.di.initKoinShared
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.domain.ModelData
import com.paoapps.fifi.sample.model.AppModel
import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.sample.model.CoffeeModel
import com.paoapps.fifi.sample.model.CoffeeModelImpl
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * This initializes the FiFi framework and registers the app module with Koin.
 *
 * @param sharedAppModule The Koin module that is shared between client and server.
 */
fun initKoinApp(
    sharedAppModule: Module,
    model: () -> AppModel,
    appDeclaration: KoinAppDeclaration = {}
) = initKoinShared<ModelData, Unit, AppModelEnvironment, Api>(
    sharedAppModule,
    model,
    appDeclaration = {
        appDeclaration()
        modules(module {
            single { get<Model<ModelData, Unit, AppModelEnvironment, Api>>() as AppModel }

            single<CoffeeModel> { CoffeeModelImpl(get(), get()) }
        }, sharedAppModule)
    }
)

