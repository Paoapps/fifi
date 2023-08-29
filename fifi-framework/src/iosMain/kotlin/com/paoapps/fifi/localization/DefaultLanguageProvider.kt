package com.paoapps.fifi.localization

import com.paoapps.fifi.domain.Language
import platform.Foundation.NSBundle

actual fun getLanguage(fallbackLanguageCode: String): Language? {
    val preferred = (NSBundle.mainBundle.preferredLocalizations.first() as? String)
    return preferred?.let { Language.parse(it, fallbackLanguageCode) } ?: Language("en")
}
