package com.paoapps.fifi.viewmodel

import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.instance.InstanceFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier

//expect inline fun <reified T : ViewModel> Module.viewModel(
//    qualifier: Qualifier? = null,
//    noinline definition: Definition<T>
//): Pair<Module, InstanceFactory<T>>


expect inline fun <reified T : ViewModel> Module.viewModel(
    qualifier: Qualifier? = null,
    noinline definition: Definition<T>
): KoinDefinition<T>
