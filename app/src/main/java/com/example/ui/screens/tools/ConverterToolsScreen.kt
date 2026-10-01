package com.example.ui.screens.tools

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.ToolDefinition
import com.example.engine.converter.ConversionUnit
import com.example.engine.converter.UnitCategory
import com.example.engine.converter.UnitConverterEngine
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultTextCard
import java.text.DecimalFormat

@Composable
fun ConverterToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCategory = remember(tool.id) {
        when {
            tool.id.contains("weight") || tool.id.contains("mass") -> UnitCategory.WEIGHT
            tool.id.contains("temp") -> UnitCategory.TEMPERATURE
            tool.id.contains("speed") -> UnitCategory.SPEED
            tool.id.contains("area") -> UnitCategory.AREA
            tool.id.contains("energy") -> UnitCategory.ENERGY
            tool.id.contains("data") || tool.id.contains("storage") || tool.id.contains("byte") -> UnitCategory.STORAGE
            else -> UnitCategory.LENGTH
        }
    }
    var selectedCategory by remember(tool.id) { mutableStateOf(initialCategory) }
    var inputValueStr by remember { mutableStateOf("100") }

    val unitsForCategory = remember(selectedCategory) {
        UnitConverterEngine.unitsByCategory[selectedCategory] ?: emptyList()
    }

    var fromUnit by remember(selectedCategory) {
        mutableStateOf(unitsForCategory.firstOrNull() ?: ConversionUnit("Base", ""))
    }
    var toUnit by remember(selectedCategory) {
        mutableStateOf(unitsForCategory.getOrNull(1) ?: unitsForCategory.firstOrNull() ?: ConversionUnit("Base", ""))
    }

    val convertedValue = remember(inputValueStr, fromUnit, toUnit, selectedCategory) {
        val input = inputValueStr.toDoubleOrNull() ?: 0.0
        val res = UnitConverterEngine.convert(input, fromUnit, toUnit, selectedCategory)
        DecimalFormat("#,##0.######").format(res)
    }

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = "Save Conversion",
        onActionClick = {
            onRecordHistory(tool.name, "$inputValueStr ${fromUnit.symbol} = $convertedValue ${toUnit.symbol}")
        }
    ) {
        // Unit Category Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            UnitCategory.values().forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.title) }
                )
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Convert ${selectedCategory.title}", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = inputValueStr,
                    onValueChange = { inputValueStr = it },
                    label = { Text("Value to Convert") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("From", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        unitsForCategory.forEach { unit ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(selected = fromUnit == unit, onClick = { fromUnit = unit })
                                Text(unit.name, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("To", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        unitsForCategory.forEach { unit ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(selected = toUnit == unit, onClick = { toUnit = unit })
                                Text(unit.name, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        ResultTextCard(
            title = "Conversion Result",
            text = "$inputValueStr ${fromUnit.symbol} = $convertedValue ${toUnit.symbol}"
        )
    }
}
