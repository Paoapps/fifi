package com.paoapps.fifi.localization

import com.paoapps.fifi.domain.Language

expect fun getLanguage(fallbackLanguageCode: String): Language?

class DefaultLanguageProvider(fallbackLanguageCode: String): LanguageProvider {
    override val language: Language? = getLanguage(fallbackLanguageCode)
}
