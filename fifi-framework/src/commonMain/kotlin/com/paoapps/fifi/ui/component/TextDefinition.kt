package com.paoapps.fifi.ui.component

import com.paoapps.fifi.utils.LightAndDarkColor

object TextDefinition {
    data class Style(
        val font: Font,
        val color: LightAndDarkColor
    )

    data class Font(
        val family: String? = null,
        val size: Int,
        val weight: Weight = Weight.REGULAR,
        val lineHeight: Int = (size.toFloat() * 1.3).toInt(),
        val letterSpacing: Double = 0.0,
        val style: Style = Style.REGULAR,
    ) {
        enum class Weight {
            REGULAR,
            MEDIUM,
            SEMI_BOLD,
            BOLD
        }

        enum class Style {
            REGULAR,
            ITALIC
        }
    }
}
