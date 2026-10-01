package com.example.engine.converter

enum class UnitCategory(val title: String) {
    LENGTH("Length"),
    WEIGHT("Weight / Mass"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    SPEED("Speed"),
    STORAGE("Digital Storage"),
    ENERGY("Energy")
}

data class ConversionUnit(
    val name: String,
    val symbol: String,
    val factorToBase: Double = 1.0 // multiplier to convert to base unit
)

object UnitConverterEngine {

    val unitsByCategory: Map<UnitCategory, List<ConversionUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("Meters", "m", 1.0),
            ConversionUnit("Kilometers", "km", 1000.0),
            ConversionUnit("Centimeters", "cm", 0.01),
            ConversionUnit("Millimeters", "mm", 0.001),
            ConversionUnit("Inches", "in", 0.0254),
            ConversionUnit("Feet", "ft", 0.3048),
            ConversionUnit("Yards", "yd", 0.9144),
            ConversionUnit("Miles", "mi", 1609.344)
        ),
        UnitCategory.WEIGHT to listOf(
            ConversionUnit("Kilograms", "kg", 1.0),
            ConversionUnit("Grams", "g", 0.001),
            ConversionUnit("Milligrams", "mg", 0.000001),
            ConversionUnit("Pounds", "lb", 0.45359237),
            ConversionUnit("Ounces", "oz", 0.028349523)
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit("Celsius", "°C", 1.0),
            ConversionUnit("Fahrenheit", "°F", 1.0),
            ConversionUnit("Kelvin", "K", 1.0)
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("Square Meters", "m²", 1.0),
            ConversionUnit("Square Kilometers", "km²", 1_000_000.0),
            ConversionUnit("Square Feet", "ft²", 0.092903),
            ConversionUnit("Acres", "ac", 4046.86),
            ConversionUnit("Hectares", "ha", 10_000.0)
        ),
        UnitCategory.SPEED to listOf(
            ConversionUnit("Kilometers per hour", "km/h", 1.0),
            ConversionUnit("Miles per hour", "mph", 1.60934),
            ConversionUnit("Meters per second", "m/s", 3.6),
            ConversionUnit("Knots", "kn", 1.852)
        ),
        UnitCategory.STORAGE to listOf(
            ConversionUnit("Bytes", "B", 1.0),
            ConversionUnit("Kilobytes", "KB", 1024.0),
            ConversionUnit("Megabytes", "MB", 1024.0 * 1024.0),
            ConversionUnit("Gigabytes", "GB", 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("Terabytes", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
        ),
        UnitCategory.ENERGY to listOf(
            ConversionUnit("Joules", "J", 1.0),
            ConversionUnit("Kilojoules", "kJ", 1000.0),
            ConversionUnit("Calories", "cal", 4.184),
            ConversionUnit("Kilocalories", "kcal", 4184.0),
            ConversionUnit("Kilowatt-hours", "kWh", 3_600_000.0)
        )
    )

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit, category: UnitCategory): Double {
        if (from == to) return value

        if (category == UnitCategory.TEMPERATURE) {
            // Temperature requires formula rather than simple factor
            val celsius = when (from.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * (5.0 / 9.0)
                "K" -> value - 273.15
                else -> value
            }
            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> (celsius * (9.0 / 5.0)) + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        // Standard ratio conversion: value * from.factor / to.factor
        val baseValue = value * from.factorToBase
        return baseValue / to.factorToBase
    }
}
