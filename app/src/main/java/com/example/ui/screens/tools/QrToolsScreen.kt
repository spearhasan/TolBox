package com.example.ui.screens.tools

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.ExecutionState
import com.example.core.model.ToolDefinition
import com.example.core.util.FileUtils
import com.example.core.util.QrCodeGenerator
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultFileCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun QrToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var qrTextInput by remember { mutableStateOf("https://github.com/google") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrFile by remember { mutableStateOf<File?>(null) }
    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }

    LaunchedEffect(Unit) {
        generatedBitmap = QrCodeGenerator.generateQrBitmap(qrTextInput)
    }

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = "Generate & Save QR Code",
        actionButtonEnabled = qrTextInput.isNotBlank(),
        executionState = executionState,
        onActionClick = {
            scope.launch {
                executionState = ExecutionState.Processing(message = "Generating QR Code...")
                try {
                    val bmp = withContext(Dispatchers.Default) {
                        QrCodeGenerator.generateQrBitmap(qrTextInput, size = 600)
                    }
                    generatedBitmap = bmp

                    val file = FileUtils.getTempFile(context, "qr_code_", ".png")
                    withContext(Dispatchers.IO) {
                        FileOutputStream(file).use { fos ->
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        }
                    }
                    qrFile = file
                    executionState = ExecutionState.Success(file)
                    onRecordHistory(tool.name, "QR for: ${qrTextInput.take(30)}...")
                } catch (e: Exception) {
                    executionState = ExecutionState.Error("Failed to generate QR", e.localizedMessage)
                }
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("QR Code Content (Text, Link, Wi-Fi)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qrTextInput,
                    onValueChange = { qrTextInput = it },
                    placeholder = { Text("Enter URL or text...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        generatedBitmap?.let { bmp ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "QR Code Preview",
                        modifier = Modifier
                            .size(240.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("100% Offline Standard QR Matrix", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        qrFile?.let { file ->
            ResultFileCard(
                file = file,
                formattedSize = FileUtils.formatFileSize(file.length()),
                onOpen = { FileUtils.openFile(context, file, "image/png") },
                onShare = { FileUtils.shareFile(context, file, "image/png") }
            )
        }
    }
}
