package com.paoapps.fifi.utils

import com.paoapps.fifi.ui.component.TextDefinition
import kotlin.jvm.JvmName

fun attributed(format: ((String) -> String), argument: String, attributes: List<AttributedText.Attribute>, defaultAttributes: List<AttributedText.Attribute> = emptyList()): AttributedText {
    val string = format(argument)
    val argumentIndex = string.indexOf(argument)
    return AttributedText.Composition(
        listOf(
            AttributedText.Text(string.substring(0, argumentIndex), defaultAttributes),
            AttributedText.Text(argument, attributes),
            AttributedText.Text(
                string.substring(argumentIndex + argument.length),
                defaultAttributes
            )
        )
    )
}

fun attributed(format: ((String, String) -> String), argument1: String, argument2: String, attributes: List<AttributedText.Attribute>, defaultAttributes: List<AttributedText.Attribute> = emptyList()): AttributedText =
    attributed(format, argument1, argument2, listOf(attributes, attributes), defaultAttributes)

@JvmName("attributedMultiList")
fun attributed(format: ((String, String) -> String), argument1: String, argument2: String, attributes: List<List<AttributedText.Attribute>>, defaultAttributes: List<AttributedText.Attribute> = emptyList()): AttributedText {
    val string = format(argument1, argument2)
    val argument1Index = string.indexOf(argument1)
    val argument2Index = string.indexOf(argument2)
    return AttributedText.Composition(
        listOf(
            AttributedText.Text(string.substring(0, argument1Index), defaultAttributes),
            AttributedText.Text(argument1, attributes[0]),
            AttributedText.Text(
                string.substring(argument1Index + argument1.length, argument2Index),
                defaultAttributes
            ),
            AttributedText.Text(argument2, attributes[1]),
            AttributedText.Text(
                string.substring(argument2Index + argument2.length),
                defaultAttributes
            )
        )
    )
}

@JvmName("attributedMulti3List")
fun attributed(format: ((String, String, String) -> String), argument1: String, argument2: String, argument3: String, attributes: List<AttributedText.Attribute>): AttributedText {
    val string = format(argument1, argument2, argument3)
    val argument1Index = string.indexOf(argument1)
    val argument2Index = string.indexOf(argument2)
    val argument3Index = string.indexOf(argument3)
    return AttributedText.Composition(listOf(
        AttributedText.Text(string.substring(0, argument1Index)),
        AttributedText.Text(argument1, attributes),
        AttributedText.Text(string.substring(argument1Index + argument1.length, argument2Index)),
        AttributedText.Text(argument2, attributes),
        AttributedText.Text(string.substring(argument2Index + argument2.length, argument3Index)),
        AttributedText.Text(argument3, attributes),
        AttributedText.Text(string.substring(argument3Index + argument3.length))
    ))
}

sealed class AttributedText {

    val string: String get() {
        val stringBuilder = StringBuilder()
        when(this) {
            is Text -> stringBuilder.append(text)
            is Composition -> nodes.forEach { stringBuilder.append(it.string) }
            // TODO: removed Image support
//            is Image -> {}
        }
        return stringBuilder.toString()
    }

    data class Text(val text: String, val attributes: List<Attribute> = emptyList()): AttributedText() {
        constructor(text: String, vararg attributes: Attribute): this(text, attributes.asList())
        constructor(text: String): this(text, emptyList())
    }
    data class Composition(val nodes: List<AttributedText>): AttributedText() {
        constructor(vararg nodes: AttributedText): this(nodes.asList())
    }

    // TODO: removed Image support
//    data class Image(val image: com.fificard.Image): AttributedText()

    sealed class Attribute {
        data class Font(val font: TextDefinition.Font) : Attribute()

        data class Color(val color: LightAndDarkColor): Attribute()
    }
}

fun String.attributed(
    textStyle: TextDefinition.Style
): AttributedText.Text =
    attributed(textStyle.font, textStyle.color)

fun String.attributed(vararg attributes: AttributedText.Attribute): AttributedText.Text =
    AttributedText.Text(this, attributes.asList())
fun String.attributed(font: TextDefinition.Font): AttributedText.Text = attributed(
    AttributedText.Attribute.Font(
        font
    )
)
fun String.attributed(font: TextDefinition.Font, color: LightAndDarkColor): AttributedText.Text = attributed(
    AttributedText.Attribute.Font(font),
    AttributedText.Attribute.Color(color)
)

fun String.replace(delimiter: String, replacement: AttributedText): AttributedText {
    return AttributedText.Composition(
        split(delimiter)
            .map { AttributedText.Text(it) }
            .insertInBetween(replacement)
    )
}

fun String.parseAttributedText(regularFont: TextDefinition.Font, mediumFont: TextDefinition.Font, vararg attributes: AttributedText.Attribute): AttributedText =
    AttributedText.Composition(this.split("**").mapIndexed { index, part ->
        val font =
            if ((index % 2) == 0) AttributedText.Attribute.Font(regularFont) else AttributedText.Attribute.Font(
                mediumFont
            )
        AttributedText.Text(part, attributes.asList().plus(font))
    })
