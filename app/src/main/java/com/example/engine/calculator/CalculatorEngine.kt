package com.example.engine.calculator

import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.*

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val daysUntilNextBirthday: Int
)

data class DateDiffResult(
    val totalDays: Long,
    val totalWeeks: Long,
    val totalMonths: Double,
    val formatted: String
)

data class BmiResult(
    val bmi: Double,
    val category: String,
    val healthyWeightRange: String,
    val primeIndex: Double
)

object CalculatorEngine {

    fun calculateExpression(expr: String): Double {
        val clean = expr.replace("×", "*").replace("÷", "/").replace(" ", "")
        return ExpressionParser(clean).parse()
    }

    fun calculatePercentage(val1: Double, val2: Double, mode: Int): Double {
        return when (mode) {
            0 -> (val1 / 100.0) * val2 // val1% of val2
            1 -> if (val2 != 0.0) (val1 / val2) * 100.0 else 0.0 // val1 is what % of val2
            2 -> if (val1 != 0.0) ((val2 - val1) / val1) * 100.0 else 0.0 // % change from val1 to val2
            else -> 0.0
        }
    }

    fun calculateDiscount(originalPrice: Double, discountPercent: Double, taxPercent: Double = 0.0): Triple<Double, Double, Double> {
        val discountAmount = (originalPrice * (discountPercent.coerceIn(0.0, 100.0) / 100.0))
        val discountedPrice = (originalPrice - discountAmount).coerceAtLeast(0.0)
        val taxAmount = (discountedPrice * (taxPercent.coerceAtLeast(0.0) / 100.0))
        val finalPrice = discountedPrice + taxAmount
        return Triple(discountAmount, taxAmount, finalPrice)
    }

    fun calculateAge(birthYear: Int, birthMonth: Int, birthDay: Int): AgeResult {
        val now = Calendar.getInstance()
        val birth = Calendar.getInstance().apply {
            set(birthYear, birthMonth, birthDay, 0, 0, 0)
        }

        var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        var days = now.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val prevMonth = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years--
            months += 12
        }

        val totalDays = (now.timeInMillis - birth.timeInMillis) / (1000 * 60 * 60 * 24)

        // Next birthday
        val nextBday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthMonth)
            set(Calendar.DAY_OF_MONTH, birthDay)
        }
        if (nextBday.before(now)) {
            nextBday.add(Calendar.YEAR, 1)
        }
        val daysUntilBday = ((nextBday.timeInMillis - now.timeInMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

        return AgeResult(
            years = years.coerceAtLeast(0),
            months = months.coerceAtLeast(0),
            days = days.coerceAtLeast(0),
            totalDays = totalDays.coerceAtLeast(0L),
            daysUntilNextBirthday = daysUntilBday
        )
    }

    fun calculateDateDiff(startMillis: Long, endMillis: Long): DateDiffResult {
        val diff = abs(endMillis - startMillis)
        val days = diff / (1000 * 60 * 60 * 24)
        val weeks = days / 7
        val months = days / 30.4375

        return DateDiffResult(
            totalDays = days,
            totalWeeks = weeks,
            totalMonths = months,
            formatted = "$days days (${weeks} weeks, ${DecimalFormat("#.#").format(months)} months)"
        )
    }

    fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult {
        val heightM = heightCm / 100.0
        if (heightM <= 0.0 || weightKg <= 0.0) {
            return BmiResult(0.0, "Invalid input", "N/A", 0.0)
        }
        val bmi = weightKg / (heightM * heightM)
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
        val minHealthyWeight = 18.5 * (heightM * heightM)
        val maxHealthyWeight = 24.9 * (heightM * heightM)
        val df = DecimalFormat("#.#")
        val range = "${df.format(minHealthyWeight)} kg - ${df.format(maxHealthyWeight)} kg"

        return BmiResult(
            bmi = bmi,
            category = category,
            healthyWeightRange = range,
            primeIndex = bmi / 25.0
        )
    }

    // Lightweight Recursive Descent Parser for math
    private class ExpressionParser(val str: String) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                } else if (eat('%'.code)) x %= parseFactor()
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch in 'a'.code..'z'.code) {
                while (ch in 'a'.code..'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else {
                    x = parseFactor()
                }
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    "abs" -> abs(x)
                    else -> throw IllegalArgumentException("Unknown function: $func")
                }
            } else {
                throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())

            return x
        }
    }
}
