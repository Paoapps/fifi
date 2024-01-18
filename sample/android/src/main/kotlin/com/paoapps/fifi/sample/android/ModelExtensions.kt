package com.paoapps.fifi.sample.android

import android.annotation.SuppressLint
import android.content.Context
import com.paoapps.fifi.model.datacontainer.CDataContainer
import com.paoapps.fifi.sample.model.AppModel

const val APP_MODEL_JSON_KEY = "appModelJson"

/**
 * Retrieve the appModel json from preferences initialize appModel with
 * it if not empty.
 */

fun CDataContainer<*>.initFromPreferences(name: String, context: Context, appPreferences: AppPreferences) {
    val jsonString = appPreferences.getPreferences(context).getString("${name}_$APP_MODEL_JSON_KEY", "")
    updateJson(jsonString?.ifEmpty { null }, true)
}

/**
 * Persists the appModel as json to preferences.
 */
fun CDataContainer<*>.persist(name: String, context: Context, appPreferences: AppPreferences) {
    appPreferences.getPreferences(context).edit()
        .putString("${name}_$APP_MODEL_JSON_KEY", json).apply()
}

@SuppressLint("ApplySharedPref")
fun AppModel.remove(context: Context, appPreferences: AppPreferences) {
    appPreferences.getPreferences(context).edit().remove(APP_MODEL_JSON_KEY).commit()
}
