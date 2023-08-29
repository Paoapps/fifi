package com.paoapps.fifi.utils

import com.paoapps.fifi.extensions.round
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class StateColor(
    val active: LightAndDarkColor,
    val pressed: LightAndDarkColor = active,
    val disabled: LightAndDarkColor = active,
    val hover: LightAndDarkColor = active,
    val focused: LightAndDarkColor = active,
    val selected: LightAndDarkColor = active
) {
    constructor(color: LightAndDarkColor) : this(color, color, color, color, color)
    constructor(active: LightAndDarkColor, pressed: LightAndDarkColor) : this(
        active,
        pressed,
        active,
        active,
        active
    )

    val alwaysLight = active.alwaysLight

    fun colorFor(state: ComponentState) = when (state) {
        ComponentState.ACTIVE -> active
        ComponentState.PRESSED -> pressed
        ComponentState.DISABLED -> disabled
        ComponentState.HOVER -> hover
        ComponentState.FOCUSED -> focused
        ComponentState.SELECTED -> selected
    }
}

enum class ComponentState {
    ACTIVE,
    PRESSED,
    DISABLED,
    HOVER,
    FOCUSED,
    SELECTED
}

@kotlinx.serialization.Serializable
data class LightAndDarkColor(val lightColor: ARGBColor, val darkColor: ARGBColor) {

    constructor(lightColor: ARGBColor) : this(lightColor, lightColor)
    constructor(
        lightColor: LightAndDarkColor,
        darkColor: LightAndDarkColor
    ) : this(lightColor.lightColor, darkColor.lightColor)

    val alwaysLight: LightAndDarkColor get() = LightAndDarkColor(lightColor)

    fun withAlpha(alpha: Float) = copy(lightColor = lightColor.withAlpha(alpha), darkColor = darkColor.withAlpha(alpha))
}

data class GradientColor(val colors: List<LightAndDarkColor>)

@Serializable
class ARGBColor(val value: Long) {
    init {
        require(value >= 0) {
            "Color value must be equal or greater to zero"
        }
    }

    val alpha: Float
        get() {
            return if (value <= ONLY_RGB && value > 0) {
                1f
            } else {
                return (((value and 0xFF000000L) shr 24) / 255f).round(2)
            }
        }

    val red: Float
        get() {
            return (((value and 0x00FF0000L) shr 16) / 255f).round(2)
        }

    val green: Float
        get() {
            return (((value and 0x0000FF00L) shr 8) / 255f).round(2)
        }

    val blue: Float
        get() {
            return (((value and 0x000000FFL)) / 255f).round(2)
        }

    val argb: ARGB
        get() {
            return ARGB(
                a = (alpha * 255).roundToInt().coerceIn(0, 255),
                r = (red * 255).roundToInt().coerceIn(0, 255),
                g = (green * 255).roundToInt().coerceIn(0, 255),
                b = (blue * 255).roundToInt().coerceIn(0, 255)
            )
        }

    operator fun component1(): Float = alpha
    operator fun component2(): Float = red
    operator fun component3(): Float = green
    operator fun component4(): Float = blue

    fun withAlpha(alpha: Float): ARGBColor = RGBAColor(r = red, g = green, b = blue, a = alpha)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ARGBColor

        if (value != other.value) return false

        return true
    }

    companion object {
        private const val ONLY_RGB = 16777215L

        val Black = ARGBColor(0xFF000000)
        val DarkGray = ARGBColor(0xFF444444)
        val Gray = ARGBColor(0xFF888888)
        val LightGray = ARGBColor(0xFFCCCCCC)
        val White = ARGBColor(0xFFFFFFFF)
        val Red = ARGBColor(0xFFFF0000)
        val Green = ARGBColor(0xFF00FF00)
        val Blue = ARGBColor(0xFF0000FF)
        val Yellow = ARGBColor(0xFFFFFF00)
        val Cyan = ARGBColor(0xFF00FFFF)
        val Magenta = ARGBColor(0xFFFF00FF)
        val Transparent = ARGBColor(0x00000000)
    }
}

fun RGBAColor(r: Float, g: Float, b: Float, a: Float = 1f): ARGBColor {
    return RGBAColor(
        (r * 255).roundToInt(),
        (g * 255).roundToInt(),
        (b * 255).roundToInt(),
        (a * 255).roundToInt()
    )
}

fun RGBAColor(
    red: Int,
    green: Int,
    blue: Int,
    alpha: Int = 0xFF
): ARGBColor {
    val color = ((alpha.toLong() and 0xFFL) shl 24) or
            ((red.toLong() and 0xFFL) shl 16) or
            ((green.toLong() and 0xFFL) shl 8) or
            (blue.toLong() and 0xFFL)
    return ARGBColor(color)
}

data class ARGB(val a: Int, val r: Int, val g: Int, val b: Int)
