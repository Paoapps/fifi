package com.paoapps.fifi.domain

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * represents an amount. At the moment de Digital Core returns amount as Double but this will change to a proper amount representation. For now the custom serializer serializes and deserializes a Double to/from CurrencyAmount.
 *
 * When creating an CurrencyAmount with a value of 15050 and scale of 2, the actual amount is 150,50
 *
 * @param value The unscaled value
 * @param scale The scale of the amount (position of the decimal separator)
 * @param currency The currency, for the moment the currency is fixed to EUR
 */
@Serializable
data class CurrencyAmount(
    val value: Long = 0,
    val scale: Int = 2,
    val currency: String = "EUR"
) : Comparable<CurrencyAmount> {
    val isZero: Boolean
        get() {
            return amount.isZero
        }

    val isMoreThanZero: Boolean
        get() {
            return amount.isMoreThanZero
        }

    val isLessThanZero: Boolean
        get() {
            return amount.isLessThanZero
        }

    val amount: DecimalAmount
        get() {
            return DecimalAmount(value, scale)
        }

    fun getWhole(): Long {
        return (value / getMultiplier(scale)).toLong()
    }

    fun hasFraction() = value % 10.0.pow(scale) != 0.0

    operator fun plus(other: CurrencyAmount): CurrencyAmount {
        val newScale = max(this.scale, other.scale)
        val newValue = this.transform(newScale).value + other.transform(newScale).value
        return CurrencyAmount(newValue, newScale, currency)
    }

    operator fun div(other: CurrencyAmount): CurrencyAmount {
        return div(other.amount)
    }

    operator fun div(other: Int): CurrencyAmount {
        return div(DecimalAmount(other.toLong(), 0))
    }

    operator fun div(other: DecimalAmount): CurrencyAmount {
        return (amount / other).toCurrency(currency)
    }

    // this function is not precise and should only be used for mocking
    operator fun div(other: Double): CurrencyAmount {
        val newValue = value / other
        return CurrencyAmount(newValue.roundToLong(), scale, currency)
    }

    operator fun minus(other: CurrencyAmount): CurrencyAmount {
        val newScale = max(this.scale, other.scale)
        val newValue = this.transform(newScale).value - other.transform(newScale).value
        return CurrencyAmount(newValue, newScale, currency)
    }

    operator fun times(other: CurrencyAmount): CurrencyAmount {
        return this * other.amount
    }

    fun abs() = if (this.isLessThanZero) this * CurrencyAmount(-1, 0) else this

    operator fun times(other: DecimalAmount): CurrencyAmount {
        return (this.amount * other).toCurrency(currency)
    }

    operator fun times(multiplier: Int): CurrencyAmount {
        val newValue = value * multiplier
        return CurrencyAmount(newValue, scale, currency)
    }

    // this function is not precise and should only be used for mocking
    operator fun times(multiplierDouble: Double): CurrencyAmount {
        val newValue = value * multiplierDouble
        return CurrencyAmount(newValue.roundToLong(), scale, currency)
    }

    fun toDouble(): Double = value.toDouble() / getMultiplier(scale)

    /**
     * @param newScale
     */
    fun transform(newScale: Int): CurrencyAmount {
        val d = newScale - scale
        return if (d >= 0) {
            CurrencyAmount(value * getMultiplier(d), newScale)
        } else {
            CurrencyAmount(value / getMultiplier(-d), newScale)
        }
    }

    companion object {
        /**
         * Convenience function to create an CurrencyAmount using constructor invocation.
         */
        operator fun invoke(amount: String) = parse(amount)

        private val supportedDecimalSeparators = arrayOf(",", ".")

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
        fun parse(amount: String, defaultScale: Int = 2, currency: String = "EUR"): CurrencyAmount {
            return supportedDecimalSeparators
                .map { amount.lastIndexOf(it) }
                .firstOrNull { it != -1 }
                ?.let { separatorIndex ->
                    createAmount(amount, separatorIndex, currency)
                } ?: CurrencyAmount(
                amount.toLong() * getMultiplier(defaultScale),
                defaultScale,
                currency
            )
        }

        private fun createAmount(
            amount: String,
            separatorIndex: Int,
            currency: String
        ): CurrencyAmount {
            val whole = amount.substring(0, separatorIndex)
            val fraction = amount.substring(separatorIndex + 1, amount.length)
            val doubleValue = "$whole.$fraction".toDouble()
            val scale = fraction.length
            return CurrencyAmount((doubleValue * getMultiplier(scale)).toLong(), scale, currency)
        }

//        todo mag 10.0.pow(scale) worden
        /**
         * @return The scale multiplier. For example 2 returns 100 and 3 returns 1000.
         */
        fun getMultiplier(scale: Int) = 10.0.pow(scale).toLong()

    }

    override fun equals(other: Any?): Boolean {
        if (other is CurrencyAmount) {
            return this.amount == other.amount && this.currency == other.currency
        }
        return false
    }

    override fun compareTo(other: CurrencyAmount): Int {
        return toDouble().compareTo(other.toDouble())
    }
}

fun DecimalAmount.toCurrency(currency: String = "EUR"): CurrencyAmount = CurrencyAmount(value, scale, currency)

fun List<CurrencyAmount>.sum() = reduceOrNull { left, right -> left + right }
