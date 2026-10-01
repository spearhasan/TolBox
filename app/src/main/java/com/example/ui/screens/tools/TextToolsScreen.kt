package com.example.ui.screens.tools

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.core.model.ExecutionState
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.util.FileUtils
import com.example.engine.pdf.PdfEngine
import com.example.engine.text.TextAnalysisResult
import com.example.engine.text.TextCaseOption
import com.example.engine.text.TextEngine
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultFileCard
import com.example.ui.components.ResultTextCard
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TextToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isOcr = tool.category == ToolCategory.OCR || tool.id.startsWith("ocr_")
    val isTxtToPdf = tool.id in listOf("doc_txt_to_pdf", "txt_to_pdf")
    val isCompare = tool.id.contains("diff") || tool.id.contains("compare")

    var inputText by remember { mutableStateOf("") }
    var secondaryText by remember { mutableStateOf("") }
    var docTitle by remember { mutableStateOf("My Document") }
    var selectedOcrImageUri by remember { mutableStateOf<Uri?>(null) }

    var outputResult by remember { mutableStateOf("") }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var savedLocationPath by remember { mutableStateOf<String?>(null) }
    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }

    // Case converter state
    var selectedCase by remember { mutableStateOf(TextCaseOption.UPPERCASE) }

    // Sorter state
    var sortAscending by remember { mutableStateOf(true) }

    // Find and replace
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }

    val isGenerator = tool.id.contains("lorem") || tool.id.contains("password") || tool.id.contains("random")

    val analysisStats = remember(inputText) {
        if (tool.id.contains("count") || tool.id.contains("reading")) TextEngine.analyze(inputText) else null
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedOcrImageUri = uri
        executionState = ExecutionState.Idle
        outputResult = ""
    }

    val actionButtonTitle = when {
        isOcr -> "Extract Text from Photo"
        isTxtToPdf -> "Generate PDF Document"
        isCompare -> "Compare Both Texts"
        tool.id.contains("lorem") -> "Generate Lorem Ipsum"
        tool.id.contains("password") -> "Generate Secure Password"
        tool.id.contains("random") -> "Generate Random Text"
        tool.id.contains("camel") -> "Convert to camelCase"
        tool.id.contains("snake") -> "Convert to snake_case"
        tool.id.contains("kebab") -> "Convert to kebab-case"
        tool.id.contains("pascal") -> "Convert to PascalCase"
        tool.id.contains("upper") -> "Convert to UPPERCASE"
        tool.id.contains("lower") -> "Convert to lowercase"
        tool.id.contains("title") -> "Convert to Title Case"
        tool.id.contains("sentence") -> "Convert to Sentence case"
        tool.id.contains("reverse_text") || tool.id == "reverse_text" -> "Reverse Text"
        tool.id.contains("reverse_line") -> "Reverse Lines"
        tool.id.contains("sort") -> "Sort Lines"
        tool.id.contains("dupe") || tool.id.contains("duplicate_line") -> "Remove Duplicate Lines"
        tool.id.contains("duplicate_word") -> "Find Duplicate Words"
        tool.id.contains("slug") -> "Generate URL Slug"
        tool.id.contains("clean") || tool.id.contains("space") -> "Clean Extra Spaces & Lines"
        tool.id.contains("find") || tool.id.contains("replace") -> "Find & Replace"
        tool.id.contains("markdown_to_html") -> "Convert Markdown to HTML"
        tool.id.contains("html_to_txt") -> "Extract Plain Text from HTML"
        tool.id.contains("txt_to_html") -> "Convert Text to HTML"
        tool.id.contains("txt_to_markdown") -> "Convert Text to Markdown"
        tool.id.contains("csv_to_json") -> "Convert CSV to JSON"
        tool.id.contains("json_to_csv") -> "Convert JSON to CSV"
        tool.id.contains("base64") -> "Encode / Decode Base64"
        tool.id.contains("url") -> "Encode / Decode URL"
        else -> "Process Text"
    }

    val isActionEnabled = when {
        isOcr -> selectedOcrImageUri != null || inputText.isNotBlank()
        isGenerator -> true
        isCompare -> inputText.isNotBlank() && secondaryText.isNotBlank()
        else -> inputText.isNotBlank()
    }

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = actionButtonTitle,
        actionButtonEnabled = isActionEnabled,
        executionState = executionState,
        onActionClick = {
            scope.launch {
                executionState = ExecutionState.Processing(message = "Processing...")
                try {
                    when {
                        // OCR Tool
                        isOcr -> {
                            val uri = selectedOcrImageUri
                            val extracted = if (uri != null) {
                                TextEngine.extractTextFromImage(context, uri)
                            } else {
                                "Scanned Text:\n$inputText"
                            }
                            outputResult = extracted
                            executionState = ExecutionState.Success(extracted)
                            onRecordHistory(tool.name, "Extracted ${extracted.length} chars from image")
                        }

                        // TXT to PDF Document
                        isTxtToPdf -> {
                            val pdf = PdfEngine.textToPdf(context, docTitle, inputText)
                            generatedFile = pdf
                            executionState = ExecutionState.Success(pdf)
                            val mime = FileUtils.getMimeType(pdf)
                            val (savedUri, path) = FileUtils.saveToDeviceMemory(context, pdf, mime)
                            if (savedUri != null) {
                                savedLocationPath = path
                                Toast.makeText(context, "${pdf.name} saved", Toast.LENGTH_SHORT).show()
                            }
                            onRecordHistory(tool.name, "Created PDF: ${pdf.name}")
                        }

                        // Compare / Diff
                        isCompare -> {
                            val diff = TextEngine.compareTexts(inputText, secondaryText)
                            outputResult = diff
                            executionState = ExecutionState.Success(diff)
                            onRecordHistory(tool.name, "Compared 2 text blocks")
                        }

                        // Text transformations
                        else -> {
                            val res = when {
                                tool.id.contains("lorem") -> TextEngine.generateLoremIpsum(3)
                                tool.id.contains("password") -> TextEngine.generatePassword(16)
                                tool.id.contains("random") -> TextEngine.generateLoremIpsum(2)
                                tool.id.contains("camel") -> TextEngine.convertCase(inputText, TextCaseOption.CAMEL_CASE)
                                tool.id.contains("snake") -> TextEngine.convertCase(inputText, TextCaseOption.SNAKE_CASE)
                                tool.id.contains("kebab") -> TextEngine.convertCase(inputText, TextCaseOption.KEBAB_CASE)
                                tool.id.contains("pascal") -> TextEngine.convertCase(inputText, TextCaseOption.PASCAL_CASE)
                                tool.id.contains("upper") -> TextEngine.convertCase(inputText, TextCaseOption.UPPERCASE)
                                tool.id.contains("lower") -> TextEngine.convertCase(inputText, TextCaseOption.LOWERCASE)
                                tool.id.contains("title") -> TextEngine.convertCase(inputText, TextCaseOption.TITLE_CASE)
                                tool.id.contains("sentence") -> TextEngine.convertCase(inputText, TextCaseOption.SENTENCE_CASE)
                                tool.id.contains("case") -> TextEngine.convertCase(inputText, selectedCase)
                                tool.id.contains("reverse_text") || tool.id == "reverse_text" -> TextEngine.reverseText(inputText)
                                tool.id.contains("reverse_line") -> TextEngine.reverseLines(inputText)
                                tool.id.contains("sort") -> TextEngine.sortLines(inputText, ascending = sortAscending)
                                tool.id.contains("dupe") || tool.id.contains("duplicate_line") -> TextEngine.removeDuplicateLines(inputText)
                                tool.id.contains("duplicate_word") -> TextEngine.findDuplicateWords(inputText)
                                tool.id.contains("slug") -> TextEngine.generateSlug(inputText)
                                tool.id.contains("clean") || tool.id.contains("space") -> TextEngine.cleanText(inputText)
                                tool.id.contains("find") || tool.id.contains("replace") -> TextEngine.findAndReplace(inputText, findText, replaceText)
                                tool.id.contains("markdown_to_html") -> TextEngine.markdownToHtml(inputText)
                                tool.id.contains("html_to_txt") -> TextEngine.htmlToTxt(inputText)
                                tool.id.contains("txt_to_html") -> TextEngine.txtToHtml(inputText)
                                tool.id.contains("txt_to_markdown") -> TextEngine.txtToMarkdown(inputText)
                                tool.id.contains("csv_to_json") -> TextEngine.csvToJson(inputText)
                                tool.id.contains("json_to_csv") -> TextEngine.jsonToCsv(inputText)
                                tool.id.contains("base64") -> {
                                    try { TextEngine.decodeBase64(inputText) } catch (_: Exception) { TextEngine.encodeBase64(inputText) }
                                }
                                tool.id.contains("url") -> {
                                    if (inputText.contains("%")) TextEngine.decodeUrl(inputText) else TextEngine.encodeUrl(inputText)
                                }
                                else -> inputText
                            }
                            outputResult = res
                            executionState = ExecutionState.Success(res)
                            onRecordHistory(tool.name, "Processed ${res.length} characters")
                        }
                    }
                } catch (e: Exception) {
                    executionState = ExecutionState.Error("Text Operation Failed", e.localizedMessage)
                }
            }
        }
    ) {
        // 1. OCR Image Picker Card
        if (isOcr) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Image for Text Extraction", fontWeight = FontWeight.Bold)

                    if (selectedOcrImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedOcrImageUri),
                            contentDescription = "Selected OCR photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedOcrImageUri == null) "Choose Photo / Screenshot" else "Change Selected Photo")
                    }
                }
            }
        }

        // 2. Document Title (for TXT to PDF)
        if (isTxtToPdf) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Document Header & Title", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Main Text Input Field (if not pure generator and not only OCR image)
        if (!isGenerator) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isCompare) "Original Text" else if (isOcr) "Or Paste Text / Keywords Directly" else "Input Text", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(if (isOcr) "Optional: enter notes or photo prompt..." else "Enter or paste text here...") },
                        minLines = if (isOcr) 2 else 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. Secondary Text Field for Diff / Compare
        if (isCompare) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Modified Text (to compare against original)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = secondaryText,
                        onValueChange = { secondaryText = it },
                        placeholder = { Text("Paste modified text here...") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 5. Find and replace fields
        if (tool.id.contains("find") || tool.id.contains("replace")) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Find & Replace Options", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = findText,
                        onValueChange = { findText = it },
                        label = { Text("Find text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = replaceText,
                        onValueChange = { replaceText = it },
                        label = { Text("Replace with") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 6. Live Stats for Word / Char Counters
        if (analysisStats != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Live Text Analysis", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Words: ${analysisStats.wordCount}", fontWeight = FontWeight.SemiBold)
                        Text("Characters: ${analysisStats.charCount}", fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Chars (no spaces): ${analysisStats.charCountNoSpaces}", style = MaterialTheme.typography.bodySmall)
                        Text("Lines: ${analysisStats.lineCount}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sentences: ${analysisStats.sentenceCount}", style = MaterialTheme.typography.bodySmall)
                        Text("Paragraphs: ${analysisStats.paragraphCount}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "Estimated reading time: ~${analysisStats.estimatedReadingTimeSeconds} seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 7. Output File Card (for PDF generator)
        generatedFile?.let { file ->
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

        // 8. Output Text Result Card
        if (outputResult.isNotBlank()) {
            ResultTextCard(
                title = "Output Result",
                text = outputResult
            )
        }
    }
}
