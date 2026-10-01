package com.example.ui.screens.tools

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.example.engine.pdf.PdfEngine
import com.example.engine.pdf.PdfMetadata
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultFileCard
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PdfToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    autoSaveEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pdfMetadata by remember { mutableStateOf<PdfMetadata?>(null) }
    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var savedLocationPath by remember { mutableStateOf<String?>(null) }

    // Text to PDF inputs
    var docTitle by remember { mutableStateOf("My Document") }
    var docContent by remember { mutableStateOf("Write or paste your document notes here...") }

    // Page manipulation state
    var startPage by remember { mutableIntStateOf(1) }
    var endPage by remember { mutableIntStateOf(1) }
    var pageToDelete by remember { mutableIntStateOf(1) }
    var rotateDegrees by remember { mutableFloatStateOf(90f) }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }

    val isMerge = tool.id in listOf("pdf_merge", "pdf_joiner", "pdf_combiner")
    val isFromImages = tool.id in listOf("pdf_jpg_to_pdf", "pdf_images_to_pdf", "jpg_to_pdf", "png_to_pdf")
    val isFromText = tool.id in listOf("pdf_text_to_pdf", "text_to_pdf", "txt_to_pdf")

    val singlePdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedPdfUri = uri
        resultFile = null
        savedLocationPath = null
        executionState = ExecutionState.Idle
        if (uri != null) {
            scope.launch {
                try {
                    val meta = PdfEngine.getPdfMetadata(context, uri)
                    pdfMetadata = meta
                    startPage = 1
                    endPage = meta.pageCount.coerceAtLeast(1)
                    pageToDelete = 1
                } catch (_: Exception) {}
            }
        }
    }

    val multiplePdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedPdfUris = uris
        resultFile = null
        savedLocationPath = null
        executionState = ExecutionState.Idle
    }

    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = uris
        resultFile = null
        savedLocationPath = null
        executionState = ExecutionState.Idle
    }

    val isReady = when {
        isMerge -> selectedPdfUris.size >= 2
        isFromImages -> selectedImageUris.isNotEmpty()
        isFromText -> docContent.isNotBlank()
        else -> selectedPdfUri != null
    }

    val actionButtonTitle = when {
        isMerge -> "Merge ${selectedPdfUris.size} PDFs"
        isFromImages -> "Generate PDF from ${selectedImageUris.size} Images"
        isFromText -> "Create PDF Document"
        tool.id.contains("extract") -> "Extract Pages $startPage to $endPage"
        tool.id.contains("delete") -> "Delete Page $pageToDelete"
        tool.id.contains("watermark") -> "Apply Watermark Stamp"
        tool.id.contains("rotate") -> "Rotate Pages ${rotateDegrees.toInt()}°"
        tool.id.contains("compress") -> "Compress PDF Document"
        tool.id.contains("clean") -> "Strip Metadata & AI Signatures"
        tool.id.contains("split") -> "Split PDF into 2 Parts (.zip)"
        tool.id.contains("jpg") || tool.id.contains("image") || tool.id.contains("png") || tool.id.contains("webp") -> "Export Pages to Images (.zip)"
        else -> "Inspect PDF Document"
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
                executionState = ExecutionState.Processing(message = "Processing PDF document...")
                try {
                    val out: File = when {
                        isMerge -> {
                            PdfEngine.mergePdfs(context, selectedPdfUris)
                        }
                        isFromImages -> {
                            PdfEngine.imagesToPdf(context, selectedImageUris)
                        }
                        isFromText -> {
                            PdfEngine.textToPdf(context, docTitle, docContent)
                        }
                        tool.id.contains("extract") -> {
                            val uri = selectedPdfUri ?: return@launch
                            PdfEngine.extractPages(context, uri, startPage, endPage)
                        }
                        tool.id.contains("delete") -> {
                            val uri = selectedPdfUri ?: return@launch
                            PdfEngine.deletePages(context, uri, setOf(pageToDelete))
                        }
                        tool.id.contains("watermark") -> {
                            val uri = selectedPdfUri ?: return@launch
                            PdfEngine.addWatermark(context, uri, watermarkText)
                        }
                        tool.id.contains("rotate") -> {
                            val uri = selectedPdfUri ?: return@launch
                            PdfEngine.rotatePdf(context, uri, rotateDegrees)
                        }
                        tool.id.contains("compress") || tool.id.contains("clean") -> {
                            val uri = selectedPdfUri ?: return@launch
                            PdfEngine.compressPdf(context, uri)
                        }
                        tool.id.contains("split") -> {
                            val uri = selectedPdfUri ?: return@launch
                            PdfEngine.splitPdf(context, uri)
                        }
                        tool.id.contains("jpg") || tool.id.contains("png") || tool.id.contains("webp") || tool.id.contains("image") -> {
                            val uri = selectedPdfUri ?: return@launch
                            val fmt = if (tool.id.contains("jpg")) "JPG" else "PNG"
                            PdfEngine.pdfToImagesZip(context, uri, fmt)
                        }
                        else -> {
                            val uri = selectedPdfUri ?: return@launch
                            val meta = PdfEngine.getPdfMetadata(context, uri)
                            pdfMetadata = meta
                            val report = FileUtils.getTempFile(context, "pdf_info_", ".txt")
                            report.writeText("=== PDF Document Metadata Report ===\nTotal Pages: ${meta.pageCount}\nFile Size: ${FileUtils.formatFileSize(meta.sizeBytes)}\nStatus: Verified Document")
                            report
                        }
                    }

                    resultFile = out
                    executionState = ExecutionState.Success(out)

                    val mime = FileUtils.getMimeType(out)
                    if (autoSaveEnabled) {
                        val (savedUri, path) = FileUtils.saveToDeviceMemory(context, out, mime)
                        if (savedUri != null) {
                            savedLocationPath = path
                            Toast.makeText(context, "${out.name} saved", Toast.LENGTH_SHORT).show()
                        }
                    }

                    onRecordHistory(tool.name, "Processed ${out.name} (${FileUtils.formatFileSize(out.length())})")
                } catch (e: Exception) {
                    executionState = ExecutionState.Error(
                        error = "PDF Operation Failed",
                        details = e.localizedMessage ?: "Unknown PDF error"
                    )
                }
            }
        }
    ) {
        // Picker Card based on input type
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
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isMerge -> {
                        Text(
                            text = if (selectedPdfUris.isEmpty()) "Select multiple PDFs to merge" else "${selectedPdfUris.size} PDF files chosen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { multiplePdfPicker.launch("application/pdf") }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedPdfUris.isEmpty()) "Select PDF Files" else "Change Selected Files")
                        }
                    }

                    isFromImages -> {
                        Text(
                            text = if (selectedImageUris.isEmpty()) "Select photos to convert to PDF" else "${selectedImageUris.size} images chosen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { multipleImagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedImageUris.isEmpty()) "Select Images" else "Change Images")
                        }
                    }

                    isFromText -> {
                        Text("Create PDF Document from Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    else -> {
                        if (selectedPdfUri != null) {
                            Text("PDF file selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            pdfMetadata?.let { meta ->
                                Text("Total Pages: ${meta.pageCount} • Size: ${FileUtils.formatFileSize(meta.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { singlePdfPicker.launch("application/pdf") }) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change PDF")
                            }
                        } else {
                            Text("Select a PDF file to begin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { singlePdfPicker.launch("application/pdf") }) {
                                Text("Choose PDF File")
                            }
                        }
                    }
                }
            }
        }

        // Dedicated Controls for Specific PDF Tools

        // 1. Text to PDF Editor
        if (isFromText) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Document Configuration", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Header / Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = { Text("Content Text") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6
                    )
                }
            }
        }

        // 2. Page Extraction Range
        if (tool.id.contains("extract") && selectedPdfUri != null) {
            val total = (pdfMetadata?.pageCount ?: 1).coerceAtLeast(1)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Extract Page Range", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("From Page: $startPage", style = MaterialTheme.typography.bodyMedium)
                        Text("To Page: $endPage", style = MaterialTheme.typography.bodyMedium)
                    }
                    RangeSlider(
                        value = startPage.toFloat()..endPage.toFloat(),
                        onValueChange = { range ->
                            startPage = range.start.toInt().coerceIn(1, total)
                            endPage = range.endInclusive.toInt().coerceIn(startPage, total)
                        },
                        valueRange = 1f..total.toFloat(),
                        steps = if (total > 2) total - 2 else 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Page Deletion Selector
        if (tool.id.contains("delete") && selectedPdfUri != null) {
            val total = (pdfMetadata?.pageCount ?: 1).coerceAtLeast(1)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Page to Delete", fontWeight = FontWeight.Bold)
                    Text("Deleting: Page $pageToDelete of $total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Slider(
                        value = pageToDelete.toFloat(),
                        onValueChange = { pageToDelete = it.toInt().coerceIn(1, total) },
                        valueRange = 1f..total.toFloat(),
                        steps = if (total > 2) total - 2 else 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. Watermark Input
        if (tool.id.contains("watermark") && selectedPdfUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Watermark Stamp Settings", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark Text (Diagonal across pages)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CONFIDENTIAL", "DRAFT", "DO NOT COPY", "SAMPLE").forEach { preset ->
                            FilterChip(
                                selected = watermarkText == preset,
                                onClick = { watermarkText = preset },
                                label = { Text(preset) }
                            )
                        }
                    }
                }
            }
        }

        // 5. PDF Rotation Controls
        if (tool.id.contains("rotate") && selectedPdfUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Page Rotation Angle", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(90f to "90° Clockwise", 180f to "180° Flip", 270f to "270° Counter").forEach { (deg, label) ->
                            FilterChip(
                                selected = rotateDegrees == deg,
                                onClick = { rotateDegrees = deg },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Result Card
        resultFile?.let { file ->
            val outMime = FileUtils.getMimeType(file)
            ResultFileCard(
                file = file,
                formattedSize = FileUtils.formatFileSize(file.length()),
                savedLocation = savedLocationPath,
                onOpen = { FileUtils.openFile(context, file, outMime) },
                onShare = { FileUtils.shareFile(context, file, outMime) },
                onSaveToDevice = {
                    val (savedUri, path) = FileUtils.saveToDeviceMemory(context, file, outMime)
                    if (savedUri != null) {
                        savedLocationPath = path
                        Toast.makeText(context, "${file.name} saved", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
