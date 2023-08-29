package com.paoapps.fifi.domain

import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.pow

/**
 * @param value The unscaled value
 * @param scale The scale of the amount (position of the decimal separator)
 */
@Serializable
data class DecimalAmount(
    val value: Long = 0,
    val scale: Int = 2
) {

    val isZero: Boolean
        get() {
            return this == zero
        }

    val isMoreThanZero: Boolean
        get() {
            return this > zero
        }

    val isLessThanZero: Boolean
        get() {
            return this < zero
        }

    val withSmallestPossibleScale: DecimalAmount
        get() {
            var newValue = this.value
            var newScale = this.scale

            while (newScale > 0 && newValue % 10 == 0L) {
                newValue /= 10
                newScale--
            }
            return DecimalAmount(newValue, newScale)
        }

    val absoluteValue: DecimalAmount get() = copy(value = value.absoluteValue)

    /**
     * Formats the amount without a currency symbol, for example 6,50
     *
     * @param decimalSeparator The decimal sperator to use. Platforms can provide a separator based on the users locale.
     */
    fun format(decimalSeparator: String = "."): String {
        val multiplier = getMultiplier(scale)
        val whole = value / multiplier

        if (scale == 0) {
            return whole.toString()
        }

        var stringvalue = value.toString()
        if (stringvalue.length < scale) {
            val numberOfLeadingZeros = scale - stringvalue.length
            stringvalue = "${"0".repeat(numberOfLeadingZeros)}$stringvalue"
        }
        val fraction = stringvalue.substring(stringvalue.length - scale, stringvalue.length)
        return "$whole$decimalSeparator$fraction"
    }

    fun toDouble(): Double = value.toDouble() / 10.0.pow(scale)

    fun wholePart() = transform(0).value

    /**
     * @return The fractional part of the DecimalAmount.
     */
    fun fractionPart() = value % getMultiplier(scale)

    fun fractionPartString(): String {
        return value.toString().substring(max(value.toString().length - scale, 0), value.toString().length)
    }

    companion object {
        val zero: DecimalAmount = whole(0)

        /**
         * Convenience function to create an CurrencyAmount using constructor invocation.
         */
        operator fun invoke(amount: String) = parse(amount)

        operator fun invoke(amount: Double) = DecimalAmount((amount * 10.0.pow(8)).toLong(), 8).withSmallestPossibleScale // keep precision of 8 should be enough

        private val supportedDecimalSeparators = arrayOf(",", ".")

        /**
         * Creates a DecimalAmount without fraction (scale = 0).
         */
        fun whole(value: Long) = DecimalAmount(value, 0)

        fun wholeAndFraction(whole: Long?, fractions: String? = null): DecimalAmount {
            val hasFractions = fractions != null && fractions.isNotEmpty() && fractions.toLong() != 0L
            val fractionsSize = if (hasFractions) {
                fractions.toString().length
            } else {
                0
            }
            val fractionPart = if (hasFractions) {
                fractions.toString()
            } else {
                ""
            }
            return DecimalAmount("${whole ?: 0}${fractionPart}".toLong(), fractionsSize)
        }

        /**
         * Parse the input and returns an CurrencyAmount representing the input.
         *
         * Examples: 6,50 = (CurrencyAmount(650, 2, "EUR")
         * Examples: 6,5 = (CurrencyAmount(65, 1, "EUR")
         * Examples: 6,5643 = (CurrencyAmount(65643, 4, "EUR")
         * Examples: 6.51 = (CurrencyAmount(65, 2, "EUR")
         * Examples: 65 = (CurrencyAmount(6500, 2, "EUR") (if no fraction is specified, the default scale is 2).
         * Examples: 150 = (CurrencyAmount(15000, 2, "EUR") (if no fraction is specified, the default scale is 2).
         *
         * @param amount The amount to parse.
         * @param defaultScale If the amount parameter contains no fraction, the default scale of 2 is used.
         * @exception NumberFormatException if the amount could not be parsed.
         */
        fun parse(amount: String, defaultScale: Int = 2): DecimalAmount {
            return supportedDecimalSeparators
                .map { amount.lastIndexOf(it) }
                .firstOrNull { it != -1 }
                ?.let { separatorIndex ->
                    createDecimal(amount, separatorIndex)
                } ?: DecimalAmount(
                amount.toLong() * getMultiplier(defaultScale),
                defaultScale
            )
        }

        private fun createDecimal(
            amount: String,
            separatorIndex: Int
        ): DecimalAmount {
            val whole = amount.substring(0, separatorIndex)
            val fraction = amount.substring(separatorIndex + 1, amount.length)
            val doubleValue = "$whole.$fraction".toDouble()
            val scale = fraction.length
            return DecimalAmount(
                (doubleValue * getMultiplier(scale)).toLong(),
                scale
            )
        }

        /**
         * @return The scale multiplier. For example 2 returns 100 and 3 returns 1000.
         */
        fun getMultiplier(scale: Int): Long {
            val reduce = 1L.rangeTo(scale + 1).reduce { acc, _ -> acc * 10L }
            return max(1, reduce)
        }
    }

    operator fun plus(other: DecimalAmount): DecimalAmount {
        val newScale = max(this.scale, other.scale)
        val newValue = this.transform(newScale).value + other.transform(newScale).value
        return DecimalAmount(newValue, newScale)
    }

    operator fun plus(other: Long): DecimalAmount {
        return this + DecimalAmount(other, 0)
    }

    operator fun div(other: DecimalAmount): DecimalAmount {
        val precision = 3
        val newScale = max(this.scale, other.scale)
        val newValue = this.transform(newScale).value * getMultiplier(precision) / other.transform(newScale).value
        return DecimalAmount(newValue, precision)
    }

    operator fun div(other: Int): DecimalAmount {
        return div(DecimalAmount(other.toLong(), 0))
    }

    operator fun minus(other: DecimalAmount): DecimalAmount {
        val newScale = max(this.scale, other.scale)
        val newValue = this.transform(newScale).value - other.transform(newScale).value
        return DecimalAmount(newValue, newScale)
    }

    operator fun minus(other: Long): DecimalAmount {
        return this - DecimalAmount(other, 0)
    }

    operator fun times(other: DecimalAmount): DecimalAmount {
        val thisTransformed = this.transform(8).withSmallestPossibleScale
        val otherTransformed = other.transform(8).withSmallestPossibleScale
        val newScale = thisTransformed.scale + otherTransformed.scale
        val newValue = thisTransformed.value * otherTransformed.value
        return DecimalAmount(newValue, newScale).withSmallestPossibleScale
    }

    operator fun times(multiplier: Int): DecimalAmount {
        val newValue = value * multiplier
        return DecimalAmount(newValue, scale)
    }

    /**
     * @param scale The new scale
     */
    fun transform(scale: Int): DecimalAmount {
        if (this.scale == scale) {
            return this
        }
        if (scale > this.scale) {
            val d = scale - this.scale
            return DecimalAmount(value * CurrencyAmount.getMultiplier(d), scale)
        } else {
            val d = this.scale - scale
            return DecimalAmount(value / CurrencyAmount.getMultiplier(d), scale)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is DecimalAmount) {
            val newScale = max(this.scale, other.scale)
            return transform(newScale).value == other.transform(newScale).value
        }
        return false
    }

    operator fun compareTo(other: DecimalAmount): Int {
        val newScale = max(this.scale, other.scale)
        return this.transform(newScale).value.compareTo(other.transform(newScale).value)
    }

    operator fun compareTo(other: Int): Int {
        return compareTo(DecimalAmount(other.toLong(), 0))
    }
}


fun List<DecimalAmount>.sum(): DecimalAmount {
    return fold(DecimalAmount.zero) { left, right -> right + left }
}
