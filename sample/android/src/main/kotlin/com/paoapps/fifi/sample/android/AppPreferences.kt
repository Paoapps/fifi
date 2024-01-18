package com.paoapps.fifi.sample.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.model.ModelEnvironment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppPreferences: KoinComponent {

    val app: Application by inject()

    private val NAME = "appPrefs"
    private val ENVIRONMENT = "environment"

    fun getPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun setEnvironment(environment: ModelEnvironment) {
        getPreferences(app.applicationContext).edit(commit = true) {
            putString(ENVIRONMENT, environment.name)
        }
    }

    fun getEnvironment(context: Context): AppModelEnvironment {
        val env = getPreferences(context).getString(ENVIRONMENT, getDefaultEnvironment().name)
        try {
            return AppModelEnvironment.EnvironmentName.valueOf(env ?: getDefaultEnvironment().name).environment
        } catch (i: Exception) {
        }
        return getDefaultEnvironment()
    }

    fun getDefaultEnvironment() = AppModelEnvironment.Production

}