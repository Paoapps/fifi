package com.paoapps.fifi.sample

import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.sample.di.initKoinApp
import com.paoapps.fifi.sample.model.AppModelImpl
import com.paoapps.fifi.sample.viewmodel.CoffeeDetailViewModel
import com.paoapps.fifi.sample.viewmodel.CoffeeDetailViewModelImpl
import com.paoapps.fifi.sample.viewmodel.HomeViewModel
import com.paoapps.fifi.sample.viewmodel.HomeViewModelImpl
import com.paoapps.fifi.viewmodel.viewModel
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

class SharedApp: KoinComponent {

    val appModule = module {
        viewModel<HomeViewModel> { HomeViewModelImpl() }
        viewModel<CoffeeDetailViewModel> { (id: Int) -> CoffeeDetailViewModelImpl(id) }
    }

}

fun initApp(environment: AppModelEnvironment, isDebugMode: Boolean, appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return initKoinApp(
        SharedApp().appModule,
        { AppModelImpl(environment) },
    ) {
        appDeclaration()
    }
}
