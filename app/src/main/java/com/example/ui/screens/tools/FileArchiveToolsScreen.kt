package com.example.ui.screens.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.ExecutionState
import com.example.core.model.ToolDefinition
import com.example.core.util.FileUtils
import com.example.engine.archive.ArchiveEngine
import com.example.engine.archive.DetailedFileInfo
import com.example.engine.archive.FileChecksumResult
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultFileCard
import com.example.ui.components.ResultTextCard
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FileArchiveToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var singleUri by remember { mutableStateOf<Uri?>(null) }
    var multiUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var fileInfo by remember { mutableStateOf<DetailedFileInfo?>(null) }
    var checksumResult by remember { mutableStateOf<FileChecksumResult?>(null) }
    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }
    var outputArchiveFile by remember { mutableStateOf<File?>(null) }
    var extractedList by remember { mutableStateOf<List<File>>(emptyList()) }

    var archiveNameInput by remember { mutableStateOf("archive") }

    val singlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        singleUri = uri
        outputArchiveFile = null
        extractedList = emptyList()
        executionState = ExecutionState.Idle
        if (uri != null) {
            scope.launch {
                try {
                    val info = ArchiveEngine.getDetailedFileInfo(context, uri)
                    fileInfo = info
                    if (tool.id.contains("checksum")) {
                        checksumResult = ArchiveEngine.calculateChecksum(context, uri)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val multiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        multiUris = uris
        outputArchiveFile = null
        executionState = ExecutionState.Idle
    }

    val isReady = when {
        tool.id == "zip_creator" -> multiUris.isNotEmpty()
        tool.id == "file_size_analyzer" -> true
        else -> singleUri != null
    }

    val actionButtonTitle = when {
        tool.id == "zip_creator" -> "Create ZIP Archive (${multiUris.size} files)"
        tool.id == "zip_extractor" -> "Extract ZIP Archive"
        tool.id == "file_metadata_cleaner" -> "Wipe File Metadata & AI Tags"
        tool.id.contains("checksum") -> "Verify File Checksum"
        tool.id == "file_size_analyzer" -> "Analyze App Cache Storage"
        else -> "Inspect File"
    }

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = actionButtonTitle,
        actionButtonEnabled = isReady,
        executionState = executionState,
        onActionClick = {
            scope.launch {
                executionState = ExecutionState.Processing(message = "Processing archive...")
                try {
                    when {
                        tool.id == "file_metadata_cleaner" -> {
                            val uri = singleUri ?: return@launch
                            executionState = ExecutionState.Processing(message = "Sanitizing file metadata and AI tags...")
                            val cleanFile = ArchiveEngine.sanitizeFileMetadata(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Wiping metadata and tracking tags...")
                            }
                            outputArchiveFile = cleanFile
                            executionState = ExecutionState.Success(cleanFile)
                            val mime = FileUtils.getMimeType(cleanFile)
                            val (savedUri, path) = FileUtils.saveToDeviceMemory(context, cleanFile, mime)
                            if (savedUri != null) {
                                android.widget.Toast.makeText(context, "${cleanFile.name} saved", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            onRecordHistory(tool.name, "Sanitized ${cleanFile.name} (${FileUtils.formatFileSize(cleanFile.length())})")
                        }

                        tool.id == "zip_creator" -> {
                            val zip = ArchiveEngine.createZip(context, multiUris, archiveNameInput)
                            outputArchiveFile = zip
                            executionState = ExecutionState.Success(zip)
                            onRecordHistory(tool.name, "Created ${zip.name} (${FileUtils.formatFileSize(zip.length())})")
                        }

                        tool.id == "zip_extractor" -> {
                            val uri = singleUri ?: return@launch
                            val files = ArchiveEngine.extractZip(context, uri)
                            extractedList = files
                            executionState = ExecutionState.Success(files)
                            onRecordHistory(tool.name, "Extracted ${files.size} files")
                        }

                        tool.id.contains("checksum") -> {
                            val uri = singleUri ?: return@launch
                            val cs = ArchiveEngine.calculateChecksum(context, uri)
                            checksumResult = cs
                            executionState = ExecutionState.Success(cs)
                            onRecordHistory(tool.name, "Computed checksum for ${cs.fileName}")
                        }

                        tool.id == "file_size_analyzer" -> {
                            val cacheSize = FileUtils.getCacheSize(context)
                            val formatted = "ToolBox Cache Storage: ${FileUtils.formatFileSize(cacheSize)}"
                            executionState = ExecutionState.Success(formatted)
                            onRecordHistory(tool.name, formatted)
                        }

                        else -> {
                            val uri = singleUri ?: return@launch
                            val info = ArchiveEngine.getDetailedFileInfo(context, uri)
                            fileInfo = info
                            executionState = ExecutionState.Success(info)
                            onRecordHistory(tool.name, "${info.fileName} (${info.formattedSize})")
                        }
                    }
                } catch (e: Exception) {
                    executionState = ExecutionState.Error("Archive Error", e.localizedMessage)
                }
            }
        }
    ) {
        if (tool.id != "file_size_analyzer") {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (tool.id.contains("checksum")) Icons.Default.VerifiedUser else Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (tool.id == "zip_creator") {
                        Text(
                            text = if (multiUris.isNotEmpty()) "${multiUris.size} files selected for ZIP" else "Choose multiple files to compress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { multiPicker.launch("*/*") }) {
                            Text(if (multiUris.isNotEmpty()) "Change Files" else "Select Files")
                        }
                    } else {
                        Text(
                            text = if (singleUri != null) "File Selected" else "Choose a file to proceed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { singlePicker.launch(if (tool.id == "zip_extractor") "application/zip" else "*/*") }) {
                            Text(if (singleUri != null) "Change File" else "Select File")
                        }
                    }
                }
            }
        }

        // Live File Information
        fileInfo?.let { info ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("File Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Name: ${info.fileName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Size: ${info.formattedSize}", style = MaterialTheme.typography.bodySmall)
                    Text("MIME: ${info.mimeType}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Live Checksum breakdown
        checksumResult?.let { cs ->
            ResultTextCard(
                title = "File Checksum Integrity",
                text = "File: ${cs.fileName}\n\nMD5:\n${cs.md5}\n\nSHA-1:\n${cs.sha1}\n\nSHA-256:\n${cs.sha256}"
            )
        }

        // Dedicated Files Privacy & AI Content Cleaner Card
        if (tool.id == "file_metadata_cleaner" && singleUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Files Privacy & AI Content Clean", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Specially optimized document & file sanitizer. Removes hidden author names, company tags, revision history, embedded comments, and AI generation parameters (ChatGPT, Claude prompts, Midjourney tags, Office metadata).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    listOf(
                        "Strip Author & Organization Info" to "Erases personal name, company title, and account username",
                        "Wipe AI Prompts & Model Headers" to "Cleans embedded prompt logs, system instructions, and tool fingerprints",
                        "Clear Revision & Modification History" to "Removes change tracking, timestamps, and previous version marks",
                        "Sanitize Extended Attributes (xattr)" to "Wipes OS download sources, quarantine tags, and zone identifiers"
                    ).forEach { (title, subtitle) ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Created Archive / Sanitized File output
        outputArchiveFile?.let { outFile ->
            val outMime = FileUtils.getMimeType(outFile)
            ResultFileCard(
                file = outFile,
                formattedSize = FileUtils.formatFileSize(outFile.length()),
                onOpen = { FileUtils.openFile(context, outFile, outMime) },
                onShare = { FileUtils.shareFile(context, outFile, outMime) },
                onSaveToDevice = {
                    val (savedUri, path) = FileUtils.saveToDeviceMemory(context, outFile, outMime)
                    if (savedUri != null) {
                        android.widget.Toast.makeText(context, "${outFile.name} saved", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Extracted files list
        if (extractedList.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Extracted Files (${extractedList.size})", fontWeight = FontWeight.Bold)
                    extractedList.forEach { f ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(f.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(FileUtils.formatFileSize(f.length()), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
