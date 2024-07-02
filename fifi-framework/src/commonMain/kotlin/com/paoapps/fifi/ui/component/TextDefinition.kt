package com.paoapps.fifi.ui.component

import com.paoapps.fifi.utils.LightAndDarkColor

/**
 * TextDefinition contains the style definitions for text components.
 */
object TextDefinition {

    /**
     * Style defines the style attributes for text, including font and color.
     *
     * @param font The font properties for the text.
     * @param color The color of the text, supporting light and dark modes.
     */
    data class Style(
        val font: Font,
        val color: LightAndDarkColor
    )

    /**
     * Font defines the properties of the text font.
     *
     * @param family The font family.
     * @param size The size of the font.
     * @param weight The weight of the font.
     * @param lineHeight The line height of the font.
     * @param letterSpacing The letter spacing of the font.
     * @param style The style of the font (e.g., regular or italic).
     * @param decoration The decoration applied to the text (e.g., underline).
     * @param textStyle The general style category of the text (e.g., body, headline).
     */
    data class Font(
        val family: String? = null,
        val size: Int,
        val weight: Weight = Weight.REGULAR,
        val lineHeight: Double = size.toFloat() * 1.3,
        val letterSpacing: Double = 0.0,
        val style: Style = Style.REGULAR,
        val decoration: TextDecoration = TextDecoration.NONE,
        val textStyle: TextStyle = TextStyle.BODY
    ) {
        constructor(
            family: String? = null,
            size: Int,
            weight: Weight = Weight.REGULAR,
            lineHeight: Int,
            letterSpacing: Double = 0.0,
            style: Style = Style.REGULAR,
            decoration: TextDecoration = TextDecoration.NONE,
            textStyle: TextStyle = TextStyle.BODY
        ) : this(family, size, weight, lineHeight.toDouble(), letterSpacing, style, decoration, textStyle)

        /**
         * Weight defines the weight of the font.
         */
        enum class Weight {
            REGULAR,
            MEDIUM,
            SEMI_BOLD,
            BOLD
        }

        /**
         * Style defines the style of the font (e.g., regular or italic).
         */
        enum class Style {
            REGULAR,
            ITALIC
        }

        /**
         * TextStyle defines the general style category of the text.
         */
        enum class TextStyle {
            LARGE_TITLE,
            TITLE_1,
            TITLE_2,
            TITLE_3,
            HEADLINE,
            SUBHEADLINE,
            BODY,
            CALLOUT,
            FOOTNOTE,
            CAPTION_1,
            CAPTION_2
        }

        /**
         * TextDecoration defines the decoration applied to the text (e.g., underline).
         */
        enum class TextDecoration {
            NONE,
            UNDERLINE,
            STRIKETHROUGH
        }
    }
}
