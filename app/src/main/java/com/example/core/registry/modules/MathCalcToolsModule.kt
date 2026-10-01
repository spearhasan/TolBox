package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object MathCalcToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "calculator",
            name = "Expression Calculator",
            category = ToolCategory.CALCULATOR,
            description = "Solve mathematical formulas with parentheses, powers, and scientific functions.",
            icon = Icons.Default.Calculate,
            keywords = listOf("calculator", "math", "evaluate", "formula", "scientific"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "percentage_calc",
            name = "Percentage Calculator",
            category = ToolCategory.CALCULATOR,
            description = "Calculate percentage of a number, percentage ratios, and percent changes.",
            icon = Icons.Default.Percent,
            keywords = listOf("percentage", "ratio", "percent change", "increase", "decrease"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "discount_calc",
            name = "Discount & Tax Calculator",
            category = ToolCategory.CALCULATOR,
            description = "Calculate final price after discount savings and sales tax addition.",
            icon = Icons.Default.LocalOffer,
            keywords = listOf("discount", "sale", "tax", "price", "savings", "shopping"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "age_calc",
            name = "Age Calculator",
            category = ToolCategory.CALCULATOR,
            description = "Calculate exact age in years, months, days, and countdown to next birthday.",
            icon = Icons.Default.Cake,
            keywords = listOf("age", "birthday", "born", "years old", "days lived"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "date_diff_calc",
            name = "Date Difference",
            category = ToolCategory.CALCULATOR,
            description = "Find the exact duration in days, weeks, and months between two dates.",
            icon = Icons.Default.DateRange,
            keywords = listOf("date", "difference", "duration", "days between", "calendar"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "bmi_calc",
            name = "BMI Calculator",
            category = ToolCategory.CALCULATOR,
            description = "Calculate Body Mass Index and healthy weight ranges according to WHO.",
            icon = Icons.Default.FitnessCenter,
            keywords = listOf("bmi", "body mass index", "health", "weight", "fitness"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "calc_tip_splitter",
            name = "Tip & Bill Splitter",
            category = ToolCategory.CALCULATOR,
            description = "Evenly divide restaurant bill and tip per person with custom percentage.",
            icon = Icons.Default.Receipt,
            keywords = listOf("tip", "bill split", "restaurant", "divide check"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "calc_fuel_mileage",
            name = "Fuel & Mileage Calculator",
            category = ToolCategory.CALCULATOR,
            description = "Calculate fuel efficiency (MPG, L/100km) and estimated road trip travel costs.",
            icon = Icons.Default.LocalGasStation,
            keywords = listOf("fuel", "mileage", "trip cost", "gas", "mpg"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "unit_converter",
            name = "Unit Converter",
            category = ToolCategory.CONVERTER,
            description = "Convert Length, Weight, Temperature, Area, Speed, Digital Storage, and Energy.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("unit", "converter", "length", "weight", "temperature", "speed", "storage", "bytes"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "base_converter",
            name = "Number Base Converter",
            category = ToolCategory.CONVERTER,
            description = "Convert numbers between Binary (Base 2), Octal (Base 8), Decimal, and Hex (Base 16).",
            icon = Icons.Default.Pin,
            keywords = listOf("binary", "hex", "octal", "decimal", "base converter"),
            inputType = ToolInputType.TEXT_INPUT
        )
    )
}
