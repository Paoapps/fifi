package com.paoapps.fifi.localization

import com.paoapps.fifi.domain.Language

interface LanguageProvider {
    val language: Language?
}
