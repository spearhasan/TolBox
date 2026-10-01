package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.ExecutionState
import com.example.core.model.ToolDefinition
import com.example.engine.developer.DeveloperEngine
import com.example.engine.developer.HashResult
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultTextCard

@Composable
fun DeveloperToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputStr by remember { mutableStateOf("") }
    var regexInput by remember { mutableStateOf("") }
    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }

    // UUID options
    var uuidCount by remember { mutableIntStateOf(5) }
    var uuidUppercase by remember { mutableStateOf(false) }

    // Results
    var hashResult by remember { mutableStateOf<HashResult?>(null) }
    var outputText by remember { mutableStateOf("") }
    var previewColorInt by remember { mutableIntStateOf(android.graphics.Color.BLUE) }

    val isGenerator = tool.id.contains("uuid") || tool.id.contains("ulid")

    val actionButtonTitle = when {
        tool.id.contains("uuid") -> "Generate UUIDs"
        tool.id.contains("ulid") -> "Generate ULID"
        tool.id.contains("md5") -> "Generate MD5 Hash"
        tool.id.contains("sha") -> "Generate SHA Hash"
        tool.id.contains("hash") -> "Generate All Hashes"
        tool.id.contains("timestamp") -> "Convert Timestamp"
        tool.id.contains("regex") -> "Test Regex"
        tool.id.contains("jwt") -> "Decode JWT"
        tool.id.contains("color") -> "Convert Color Code"
        tool.id.contains("json") -> "Format / Minify JSON"
        tool.id.contains("xml") -> "Format XML"
        tool.id.contains("binary") -> "Convert Binary"
        tool.id.contains("hex") -> "Convert Hex"
        tool.id.contains("http") || tool.id.contains("status") -> "Lookup HTTP Status"
        else -> "Execute Tool"
    }

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = actionButtonTitle,
        actionButtonEnabled = isGenerator || inputStr.isNotBlank(),
        executionState = executionState,
        onActionClick = {
            try {
                when {
                    tool.id.contains("uuid") -> {
                        val uuids = DeveloperEngine.generateUuids(uuidCount, uppercase = uuidUppercase)
                        val text = uuids.joinToString("\n")
                        outputText = text
                        executionState = ExecutionState.Success(text)
                        onRecordHistory(tool.name, "Generated $uuidCount UUIDs")
                    }

                    tool.id.contains("ulid") -> {
                        val ulid = DeveloperEngine.generateUlid()
                        outputText = ulid
                        executionState = ExecutionState.Success(ulid)
                        onRecordHistory(tool.name, "Generated ULID: $ulid")
                    }

                    tool.id.contains("md5") -> {
                        val h = DeveloperEngine.singleHash(inputStr, "MD5")
                        outputText = "MD5: $h"
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "MD5 hash generated")
                    }

                    tool.id.contains("sha") -> {
                        val h = DeveloperEngine.singleHash(inputStr, "SHA-256")
                        outputText = "SHA-256: $h"
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "SHA-256 hash generated")
                    }

                    tool.id.contains("hash") -> {
                        val hashes = DeveloperEngine.generateHashes(inputStr)
                        hashResult = hashes
                        outputText = "MD5: ${hashes.md5}\nSHA-1: ${hashes.sha1}\nSHA-256: ${hashes.sha256}\nSHA-512: ${hashes.sha512}"
                        executionState = ExecutionState.Success(hashes)
                        onRecordHistory(tool.name, "Generated MD5/SHA hashes")
                    }

                    tool.id.contains("timestamp") -> {
                        val epoch = inputStr.trim().toLongOrNull() ?: System.currentTimeMillis()
                        val isSeconds = inputStr.length <= 10
                        val res = DeveloperEngine.convertTimestamp(epoch, isSeconds)
                        outputText = "Local: ${res.localDateTime}\nUTC: ${res.utcDateTime}\nRelative: ${res.relativeTime}\nEpoch ms: ${res.epochMillis}"
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "Converted epoch $epoch")
                    }

                    tool.id.contains("regex") -> {
                        val matches = DeveloperEngine.testRegex(regexInput.ifEmpty { ".*" }, inputStr)
                        outputText = if (matches.isEmpty()) "No matches found." else "Found ${matches.size} match(es):\n" + matches.joinToString("\n") { "• $it" }
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "Regex test: ${matches.size} matches")
                    }

                    tool.id.contains("jwt") -> {
                        val jwt = DeveloperEngine.decodeJwt(inputStr)
                        outputText = "=== HEADER ===\n${jwt.headerJson}\n\n=== PAYLOAD ===\n${jwt.payloadJson}\n\nIssued At: ${jwt.issuedAt ?: "N/A"}\nExpires: ${jwt.expiration ?: "N/A"}"
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "Decoded JWT Token")
                    }

                    tool.id.contains("color") -> {
                        val c = DeveloperEngine.parseColor(inputStr)
                        previewColorInt = c.colorInt
                        outputText = "HEX: ${c.hex}\nRGB: ${c.rgb}\nHSL: ${c.hsl}"
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "Converted color ${c.hex}")
                    }

                    tool.id.contains("json") -> {
                        val formatted = try {
                            DeveloperEngine.formatJson(inputStr)
                        } catch (e: Exception) {
                            DeveloperEngine.minifyJson(inputStr)
                        }
                        outputText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "Formatted JSON")
                    }

                    tool.id.contains("xml") -> {
                        val formatted = DeveloperEngine.formatXml(inputStr)
                        outputText = formatted
                        executionState = ExecutionState.Success(formatted)
                        onRecordHistory(tool.name, "Formatted XML")
                    }

                    tool.id.contains("binary") -> {
                        outputText = if (inputStr.trim().all { it == '0' || it == '1' || it.isWhitespace() }) {
                            DeveloperEngine.fromBinary(inputStr)
                        } else {
                            DeveloperEngine.toBinary(inputStr)
                        }
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "Binary conversion")
                    }

                    tool.id.contains("hex") -> {
                        outputText = if (inputStr.trim().all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it.isWhitespace() }) {
                            try { DeveloperEngine.fromHex(inputStr) } catch (_: Exception) { DeveloperEngine.toHex(inputStr) }
                        } else {
                            DeveloperEngine.toHex(inputStr)
                        }
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "Hex conversion")
                    }

                    tool.id.contains("http") || tool.id.contains("status") -> {
                        val code = inputStr.trim().toIntOrNull() ?: 200
                        outputText = DeveloperEngine.lookupHttpStatus(code)
                        executionState = ExecutionState.Success(outputText)
                        onRecordHistory(tool.name, "HTTP $code lookup")
                    }

                    else -> {
                        outputText = inputStr
                        executionState = ExecutionState.Success(inputStr)
                    }
                }
            } catch (e: Exception) {
                executionState = ExecutionState.Error("Operation Failed", e.localizedMessage)
            }
        }
    ) {
        // Dynamic Input Section
        if (tool.id.contains("uuid")) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("UUID Settings", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Count: $uuidCount")
                        Slider(
                            value = uuidCount.toFloat(),
                            onValueChange = { uuidCount = it.toInt() },
                            valueRange = 1f..20f,
                            steps = 18,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uppercase UUIDs")
                        Switch(checked = uuidUppercase, onCheckedChange = { uuidUppercase = it })
                    }
                }
            }
        } else if (tool.id.contains("regex")) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Regex Test Setup", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regexInput,
                        onValueChange = { regexInput = it },
                        placeholder = { Text("Regex Pattern, e.g. \\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b") },
                        label = { Text("Regular Expression") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputStr,
                        onValueChange = { inputStr = it },
                        placeholder = { Text("Target text to match against...") },
                        label = { Text("Test Input String") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (!isGenerator) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Input Value / Payload", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputStr,
                        onValueChange = { inputStr = it },
                        placeholder = {
                            Text(
                                when {
                                    tool.id.contains("color") -> "e.g. #3B82F6 or rgb(59, 130, 246)"
                                    tool.id.contains("timestamp") -> "e.g. 1711392842 or empty for current time"
                                    tool.id.contains("http") -> "e.g. 404, 200, 500"
                                    tool.id.contains("jwt") -> "Paste JWT token..."
                                    else -> "Enter data here..."
                                }
                            )
                        },
                        minLines = if (tool.id.contains("json") || tool.id.contains("xml") || tool.id.contains("jwt")) 4 else 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Color preview box
        if (tool.id.contains("color") && outputText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(previewColorInt))
            )
        }

        // Results
        if (outputText.isNotBlank()) {
            ResultTextCard(
                title = "Tool Output",
                text = outputText
            )
        }
    }
}
