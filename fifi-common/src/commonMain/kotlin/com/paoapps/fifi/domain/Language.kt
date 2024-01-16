package com.paoapps.fifi.domain

import kotlinx.serialization.Serializable

@Serializable
data class Language(val languageCode: String, val countryCode: String? = null) {
    val acceptLanguageHeader: String get() = countryCode?.let { "$languageCode-$it" } ?: languageCode

    companion object {
        fun parse(acceptLanguageHeader: String?, fallbackLanguageCode: String): Language {
            if (acceptLanguageHeader == null) {
                return Language(fallbackLanguageCode)
            }
            val parts = acceptLanguageHeader.split('-')
            val languageCode = if (parts.size > 0) parts[0] else fallbackLanguageCode
            return Language(
                languageCode,
                if (parts.size > 1) parts[1] else null
            )
        }
    }
}
