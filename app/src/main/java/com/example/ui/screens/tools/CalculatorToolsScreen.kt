package com.example.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.ExecutionState
import com.example.core.model.ToolDefinition
import com.example.engine.calculator.CalculatorEngine
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultTextCard
import java.text.DecimalFormat

@Composable
fun CalculatorToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }
    var resultText by remember { mutableStateOf("") }

    // Math Expression
    var expressionInput by remember { mutableStateOf("12 * (4.5 + 2.5) - sqrt(16)") }

    // Percentage
    var percentVal1 by remember { mutableStateOf("15") }
    var percentVal2 by remember { mutableStateOf("250") }

    // Discount & Tax
    var originalPriceStr by remember { mutableStateOf("120") }
    var discountPercentStr by remember { mutableStateOf("20") }
    var taxPercentStr by remember { mutableStateOf("8") }

    // Age
    var birthYearStr by remember { mutableStateOf("1998") }
    var birthMonthStr by remember { mutableStateOf("5") }
    var birthDayStr by remember { mutableStateOf("15") }

    // Tip Splitter
    var billAmountStr by remember { mutableStateOf("85.00") }
    var tipPercentStr by remember { mutableStateOf("15") }
    var peopleCountStr by remember { mutableStateOf("3") }

    // Base converter
    var baseInputStr by remember { mutableStateOf("255") }

    // BMI
    var weightKgStr by remember { mutableStateOf("70") }
    var heightCmStr by remember { mutableStateOf("175") }

    val df = remember { DecimalFormat("#,##0.##") }

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = "Calculate",
        executionState = executionState,
        onActionClick = {
            try {
                when {
                    tool.id.contains("percent") -> {
                        val v1 = percentVal1.toDoubleOrNull() ?: 0.0
                        val v2 = percentVal2.toDoubleOrNull() ?: 0.0
                        val res = CalculatorEngine.calculatePercentage(v1, v2, 0)
                        val formatted = "$v1% of $v2 = ${df.format(res)}"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, formatted)
                    }

                    tool.id.contains("discount") -> {
                        val orig = originalPriceStr.toDoubleOrNull() ?: 0.0
                        val disc = discountPercentStr.toDoubleOrNull() ?: 0.0
                        val tax = taxPercentStr.toDoubleOrNull() ?: 0.0
                        val (saving, taxAmt, finalTotal) = CalculatorEngine.calculateDiscount(orig, disc, tax)
                        val formatted = "Original: \$${df.format(orig)}\nDiscount Savings: -\$${df.format(saving)} ($disc%)\nTax: +\$${df.format(taxAmt)} ($tax%)\nFinal Price: \$${df.format(finalTotal)}"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "Final: \$${df.format(finalTotal)}")
                    }

                    tool.id.contains("age") -> {
                        val y = birthYearStr.toIntOrNull() ?: 2000
                        val m = (birthMonthStr.toIntOrNull() ?: 1) - 1
                        val d = birthDayStr.toIntOrNull() ?: 1
                        val age = CalculatorEngine.calculateAge(y, m, d)
                        val formatted = "Age: ${age.years} years, ${age.months} months, ${age.days} days\nTotal days lived: ${df.format(age.totalDays)} days\nDays until next birthday: ${age.daysUntilNextBirthday} days"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "${age.years} years old")
                    }

                    tool.id.contains("tip") -> {
                        val bill = billAmountStr.toDoubleOrNull() ?: 0.0
                        val tipPct = tipPercentStr.toDoubleOrNull() ?: 0.0
                        val people = (peopleCountStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                        val tipTotal = bill * (tipPct / 100.0)
                        val grandTotal = bill + tipTotal
                        val perPerson = grandTotal / people
                        val tipPerPerson = tipTotal / people
                        val formatted = "Total Bill: \$${df.format(grandTotal)}\nTip: \$${df.format(tipTotal)} ($tipPct%)\nPer Person: \$${df.format(perPerson)} (incl. \$${df.format(tipPerPerson)} tip)"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "\$${df.format(perPerson)} per person")
                    }

                    tool.id.contains("base") -> {
                        val n = baseInputStr.trim().toLongOrNull() ?: 0L
                        val formatted = "Decimal: $n\nBinary: ${n.toString(2)}\nHexadecimal: 0x${n.toString(16).uppercase()}\nOctal: ${n.toString(8)}"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "Base conversion for $n")
                    }

                    tool.id.contains("bmi") -> {
                        val w = weightKgStr.toDoubleOrNull() ?: 70.0
                        val h = heightCmStr.toDoubleOrNull() ?: 175.0
                        val bmi = CalculatorEngine.calculateBmi(w, h)
                        val formatted = "BMI: ${df.format(bmi.bmi)}\nCategory: ${bmi.category}\nRecommended Weight: ${bmi.healthyWeightRange}"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "BMI: ${df.format(bmi.bmi)} (${bmi.category})")
                    }

                    else -> {
                        val value = CalculatorEngine.calculateExpression(expressionInput)
                        val formatted = "$expressionInput = ${df.format(value)}"
                        resultText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, formatted)
                    }
                }
            } catch (e: Exception) {
                executionState = ExecutionState.Error("Calculation Error", e.localizedMessage)
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    tool.id.contains("percent") -> {
                        Text("What is X% of Y?", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = percentVal1,
                                onValueChange = { percentVal1 = it },
                                label = { Text("Percentage (%)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = percentVal2,
                                onValueChange = { percentVal2 = it },
                                label = { Text("Total (Y)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    tool.id.contains("discount") -> {
                        Text("Discount & Tax", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = originalPriceStr,
                            onValueChange = { originalPriceStr = it },
                            label = { Text("Original Price ($)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = discountPercentStr,
                                onValueChange = { discountPercentStr = it },
                                label = { Text("Discount (%)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = taxPercentStr,
                                onValueChange = { taxPercentStr = it },
                                label = { Text("Tax (%)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    tool.id.contains("tip") -> {
                        Text("Bill & Tip Splitter", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = billAmountStr,
                            onValueChange = { billAmountStr = it },
                            label = { Text("Bill Amount ($)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tipPercentStr,
                                onValueChange = { tipPercentStr = it },
                                label = { Text("Tip (%)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = peopleCountStr,
                                onValueChange = { peopleCountStr = it },
                                label = { Text("Split between") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    tool.id.contains("base") -> {
                        Text("Number Base Converter", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = baseInputStr,
                            onValueChange = { baseInputStr = it },
                            label = { Text("Decimal Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    tool.id.contains("age") -> {
                        Text("Birth Date (YYYY / MM / DD)", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = birthYearStr,
                                onValueChange = { birthYearStr = it },
                                label = { Text("Year") },
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = birthMonthStr,
                                onValueChange = { birthMonthStr = it },
                                label = { Text("Month") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = birthDayStr,
                                onValueChange = { birthDayStr = it },
                                label = { Text("Day") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    tool.id.contains("bmi") -> {
                        Text("Body Metrics", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = weightKgStr,
                                onValueChange = { weightKgStr = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = heightCmStr,
                                onValueChange = { heightCmStr = it },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    else -> {
                        Text("Math Expression", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = expressionInput,
                            onValueChange = { expressionInput = it },
                            placeholder = { Text("e.g. 50 * (2 + 3) / 4") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (resultText.isNotEmpty()) {
            ResultTextCard(title = "Calculated Output", text = resultText)
        }
    }
}
