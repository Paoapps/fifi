package com.paoapps.fifi.sample.android

import android.annotation.SuppressLint
import android.content.Context
import com.paoapps.fifi.sample.model.AppModel

const val APP_MODEL_JSON_KEY = "appModelJson"

/**
 * Retrieve the appModel json from preferences initialize appModel with
 * it if not empty.
 */
fun AppModel.initFromPreferences(context: Context, appPreferences: AppPreferences) {
    val jsonString = appPreferences.getPreferences(context).getString(APP_MODEL_JSON_KEY, "")
    if (jsonString?.isNotEmpty() == true) {
        modelData.updateJson(jsonString, true)
    }
}

/**
 * Persists the appModel as json to preferences.
 */
fun AppModel.persist(context: Context, appPreferences: AppPreferences) {
    appPreferences.getPreferences(context).edit()
        .putString(APP_MODEL_JSON_KEY, modelData.json).apply()
}

@SuppressLint("ApplySharedPref")
fun AppModel.remove(context: Context, appPreferences: AppPreferences) {
    appPreferences.getPreferences(context).edit().remove(APP_MODEL_JSON_KEY).commit()
}
