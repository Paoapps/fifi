package com.paoapps.fifi.localization

import com.paoapps.fifi.domain.Language
import java.util.*

actual fun getLanguage(fallbackLanguageCode: String): Language? {
    val preferred = Locale.getDefault().toLanguageTag()
    return Language.parse(preferred, fallbackLanguageCode)
}
