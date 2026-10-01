package com.example.ui.screens.tools

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.model.ExecutionState
import com.example.core.model.ToolDefinition
import com.example.core.util.FileUtils
import com.example.engine.image.*
import com.example.engine.pdf.PdfEngine
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultFileCard
import com.example.ui.components.ResultTextCard
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageToolsScreen(
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
    val clipboardManager = LocalClipboardManager.current

    // Categorization
    val isMultiImage = tool.id in listOf(
        "multiple_images_to_pdf", "image_to_zip", "image_contact_sheet",
        "image_collage_maker", "image_duplicate_detector"
    )
    val isTextInput = tool.id == "base64_to_image"
    val isColorTool = tool.id in listOf(
        "image_color_picker", "image_palette_generator",
        "image_dominant_color", "image_hex_extractor"
    )
    val isExifMetadataTool = tool.id in listOf(
        "image_metadata_viewer", "image_exif_viewer",
        "image_dimensions_viewer", "image_filesize_calc"
    )
    val isExifCleaner = tool.id in listOf("image_metadata_cleaner", "image_exif_remover")

    // State: Picked files & Inputs
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var base64InputText by remember { mutableStateOf("") }

    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }
    var resultImage by remember { mutableStateOf<ProcessedImageResult?>(null) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var savedLocationPath by remember { mutableStateOf<String?>(null) }
    var base64ResultText by remember { mutableStateOf<String?>(null) }
    var metadataInfo by remember { mutableStateOf<ImageMetadataInfo?>(null) }
    var exifDataMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var extractedPalette by remember { mutableStateOf<List<String>>(emptyList()) }
    var duplicateMatches by remember { mutableStateOf<List<DuplicateMatch>>(emptyList()) }

    // Compressor options
    var qualitySlider by remember { mutableFloatStateOf(80f) }
    var selectedFormat by remember {
        mutableStateOf(
            when {
                tool.id.contains("png") -> ImageOutputFormat.PNG
                tool.id.contains("webp") -> ImageOutputFormat.WEBP
                else -> ImageOutputFormat.JPEG
            }
        )
    }

    // Photo Metadata & AI Cleaner options
    var cleanOutputFormat by remember { mutableStateOf("JPEG") }

    // Resizer options
    var targetWidthStr by remember { mutableStateOf("1920") }
    var targetHeightStr by remember { mutableStateOf("1080") }
    var lockAspectRatio by remember { mutableStateOf(true) }

    // Cropper aspect ratios & interactive framing states
    var cropRatioW by remember { mutableIntStateOf(1) }
    var cropRatioH by remember { mutableIntStateOf(1) }
    var cropIsFreeform by remember { mutableStateOf(false) }
    var cropPanX by remember { mutableFloatStateOf(0.5f) } // 0..1 (0.5 is center)
    var cropPanY by remember { mutableFloatStateOf(0.5f) } // 0..1 (0.5 is center)
    var cropZoomScale by remember { mutableFloatStateOf(1.0f) } // 0.3..1.0

    // Compute normalized crop window coordinates
    val imgW = (metadataInfo?.width ?: 1920).coerceAtLeast(1)
    val imgH = (metadataInfo?.height ?: 1080).coerceAtLeast(1)
    val imgAspect = imgW.toFloat() / imgH.toFloat()
    val targetAspect = if (cropIsFreeform) 1.0f else (cropRatioW.toFloat() / cropRatioH.toFloat())

    val (baseWFraction, baseHFraction) = if (imgAspect > targetAspect) {
        val wFrac = (targetAspect / imgAspect).coerceIn(0.08f, 1.0f)
        Pair(wFrac * cropZoomScale, 1.0f * cropZoomScale)
    } else {
        val hFrac = (imgAspect / targetAspect).coerceIn(0.08f, 1.0f)
        Pair(1.0f * cropZoomScale, hFrac * cropZoomScale)
    }

    val finalCropW = baseWFraction.coerceIn(0.05f, 1.0f)
    val finalCropH = baseHFraction.coerceIn(0.05f, 1.0f)
    val maxPanLeft = (1.0f - finalCropW).coerceAtLeast(0f)
    val maxPanTop = (1.0f - finalCropH).coerceAtLeast(0f)
    val finalCropLeft = (maxPanLeft * cropPanX).coerceIn(0f, 1.0f - finalCropW)
    val finalCropTop = (maxPanTop * cropPanY).coerceIn(0f, 1.0f - finalCropH)

    // Rotator & Flipper
    var rotationDegrees by remember { mutableFloatStateOf(90f) }
    var flipHorizontal by remember { mutableStateOf(true) }
    var flipVertical by remember { mutableStateOf(false) }

    // Filter controls
    var blurRadius by remember { mutableFloatStateOf(10f) }
    var pixelBlockSize by remember { mutableIntStateOf(16) }
    var sharpenIntensity by remember { mutableFloatStateOf(1.2f) }
    var brightnessOffset by remember { mutableFloatStateOf(30f) }
    var contrastFactor by remember { mutableFloatStateOf(1.4f) }
    var saturationFactor by remember { mutableFloatStateOf(1.5f) }

    // Border & Rounded Corners
    var borderWidthPx by remember { mutableIntStateOf(24) }
    var borderColorInt by remember { mutableIntStateOf(android.graphics.Color.WHITE) }
    var cornerRadiusPercent by remember { mutableFloatStateOf(0.12f) }

    // Watermark & Text Overlay
    var watermarkText by remember { mutableStateOf("ToolBox Watermark") }
    var watermarkPosition by remember { mutableStateOf("BOTTOM_RIGHT") }
    var watermarkAlpha by remember { mutableIntStateOf(180) }

    var overlayCaption by remember { mutableStateOf("Caption Here") }
    var overlayPosition by remember { mutableStateOf("BOTTOM") }
    var overlayFontSize by remember { mutableFloatStateOf(36f) }
    var overlayHasBg by remember { mutableStateOf(true) }

    // Collage
    var collageColumns by remember { mutableIntStateOf(2) }

    // Launchers
    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        resultImage = null
        resultFile = null
        base64ResultText = null
        savedLocationPath = null
        extractedPalette = emptyList()
        executionState = ExecutionState.Idle
        if (uri != null) {
            scope.launch {
                try {
                    val meta = ImageEngine.getImageMetadata(context, uri)
                    metadataInfo = meta
                    targetWidthStr = meta.width.toString()
                    targetHeightStr = meta.height.toString()
                    if (isExifMetadataTool || isExifCleaner) {
                        exifDataMap = ImageEngine.getExifData(context, uri)
                    }
                    if (isColorTool) {
                        extractedPalette = ImageEngine.extractPalette(context, uri, 6)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            resultImage = null
            resultFile = null
            savedLocationPath = null
            duplicateMatches = emptyList()
            executionState = ExecutionState.Idle
        }
    }

    val actionButtonTitle = when {
        isMultiImage -> when (tool.id) {
            "multiple_images_to_pdf" -> "Generate Multi-Page PDF (${selectedImageUris.size} Images)"
            "image_to_zip" -> "Compress into ZIP Archive (${selectedImageUris.size} Images)"
            "image_collage_maker" -> "Create Photo Collage (${selectedImageUris.size} Images)"
            "image_contact_sheet" -> "Create Contact Sheet (${selectedImageUris.size} Images)"
            "image_duplicate_detector" -> "Scan for Duplicate Photos (${selectedImageUris.size} Images)"
            else -> "Process ${selectedImageUris.size} Images"
        }
        isTextInput -> "Decode Base64 to Image"
        tool.id == "image_to_base64" -> "Convert Image to Base64"
        tool.id == "image_to_pdf" -> "Convert to PDF Document"
        isColorTool -> "Extract Color Palette"
        isExifCleaner -> "Strip Photo Metadata & AI Tags"
        isExifMetadataTool -> "Export Technical Metadata"
        tool.id.contains("rotat") -> "Rotate Image (${rotationDegrees.toInt()}°)"
        tool.id.contains("flip") -> "Flip Photo"
        tool.id.contains("crop") -> "Crop Photo (${cropRatioW}:${cropRatioH})"
        tool.id.contains("resiz") -> "Resize Image (${targetWidthStr}×${targetHeightStr})"
        tool.id.contains("pixelate") -> "Apply Pixelate Mosaic"
        tool.id.contains("sharpen") -> "Sharpen Edges"
        tool.id.contains("blur") -> "Apply Soft Blur"
        tool.id.contains("gray") -> "Convert to Grayscale (B&W)"
        tool.id.contains("invert") -> "Invert Photo (Negative)"
        tool.id.contains("brightness") -> "Adjust Brightness"
        tool.id.contains("contrast") -> "Adjust Contrast"
        tool.id.contains("saturation") -> "Adjust Saturation"
        tool.id.contains("border") -> "Add Photo Border"
        tool.id.contains("rounded") -> "Create Rounded PNG"
        tool.id.contains("watermark") -> "Apply Watermark Stamp"
        tool.id.contains("text_overlay") -> "Add Text Caption"
        tool.id.contains("png") -> "Convert to PNG"
        tool.id.contains("webp") -> "Convert to WebP"
        tool.id.contains("jpg") || tool.id.contains("jpeg") -> "Convert to JPG"
        else -> "Compress & Optimize Image"
    }

    val isActionEnabled = when {
        isMultiImage -> selectedImageUris.size >= 2
        isTextInput -> base64InputText.isNotBlank()
        else -> selectedImageUri != null
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
                executionState = ExecutionState.Processing(message = "Processing image tool...")
                try {
                    when {
                        // Multi-image operations
                        isMultiImage -> {
                            when (tool.id) {
                                "multiple_images_to_pdf" -> {
                                    val pdf = PdfEngine.imagesToPdf(context, selectedImageUris)
                                    resultFile = pdf
                                    executionState = ExecutionState.Success(pdf)
                                    onRecordHistory(tool.name, "Created PDF with ${selectedImageUris.size} images")
                                }
                                "image_to_zip" -> {
                                    val zip = ImageEngine.imagesToZip(context, selectedImageUris)
                                    resultFile = zip
                                    executionState = ExecutionState.Success(zip)
                                    onRecordHistory(tool.name, "Packed ${selectedImageUris.size} images into ZIP")
                                }
                                "image_collage_maker" -> {
                                    val res = ImageEngine.createCollage(context, selectedImageUris, collageColumns)
                                    resultImage = res
                                    resultFile = res.file
                                    executionState = ExecutionState.Success(res)
                                    onRecordHistory(tool.name, "Created collage with ${selectedImageUris.size} photos")
                                }
                                "image_contact_sheet" -> {
                                    val res = ImageEngine.createContactSheet(context, selectedImageUris)
                                    resultImage = res
                                    resultFile = res.file
                                    executionState = ExecutionState.Success(res)
                                    onRecordHistory(tool.name, "Generated contact sheet (${selectedImageUris.size} photos)")
                                }
                                "image_duplicate_detector" -> {
                                    val dups = ImageEngine.detectDuplicates(context, selectedImageUris)
                                    duplicateMatches = dups
                                    executionState = ExecutionState.Success(dups)
                                    val count = dups.count { it.isDuplicate }
                                    onRecordHistory(tool.name, "Found $count duplicate pair(s)")
                                }
                            }
                        }

                        // Base64 to Image
                        isTextInput -> {
                            val imgFile = ImageEngine.base64ToImage(context, base64InputText)
                            resultFile = imgFile
                            executionState = ExecutionState.Success(imgFile)
                            onRecordHistory(tool.name, "Decoded image (${FileUtils.formatFileSize(imgFile.length())})")
                        }

                        // Image to Base64
                        tool.id == "image_to_base64" -> {
                            val uri = selectedImageUri ?: return@launch
                            val b64 = ImageEngine.imageToBase64(context, uri)
                            base64ResultText = b64
                            executionState = ExecutionState.Success(b64)
                            onRecordHistory(tool.name, "Converted to Base64 (${b64.length} chars)")
                        }

                        // Image to PDF
                        tool.id == "image_to_pdf" -> {
                            val uri = selectedImageUri ?: return@launch
                            val pdf = PdfEngine.imagesToPdf(context, listOf(uri))
                            resultFile = pdf
                            executionState = ExecutionState.Success(pdf)
                            onRecordHistory(tool.name, "Created PDF (${FileUtils.formatFileSize(pdf.length())})")
                        }

                        // Color / Palette tools
                        isColorTool -> {
                            val uri = selectedImageUri ?: return@launch
                            val palette = ImageEngine.extractPalette(context, uri, 6)
                            extractedPalette = palette
                            executionState = ExecutionState.Success(palette)
                            onRecordHistory(tool.name, "Extracted palette of ${palette.size} colors")
                        }

                        // Metadata Report
                        isExifMetadataTool -> {
                            val uri = selectedImageUri ?: return@launch
                            val meta = ImageEngine.getImageMetadata(context, uri)
                            val exif = ImageEngine.getExifData(context, uri)
                            metadataInfo = meta
                            exifDataMap = exif

                            val reportFile = FileUtils.getTempFile(context, "image_spec_report_", ".txt")
                            FileOutputStream(reportFile).use { fos ->
                                fos.write("=== Image Specifications & EXIF Report ===\n\n".toByteArray())
                                fos.write("Resolution: ${meta.width} x ${meta.height} px\n".toByteArray())
                                fos.write("Aspect Ratio: ${meta.aspectRatio}\n".toByteArray())
                                fos.write("Format: ${meta.format}\n".toByteArray())
                                fos.write("File Size: ${FileUtils.formatFileSize(meta.sizeBytes)}\n\n".toByteArray())
                                fos.write("--- EXIF Details ---\n".toByteArray())
                                exif.forEach { (k, v) -> fos.write("$k: $v\n".toByteArray()) }
                            }
                            resultFile = reportFile
                            executionState = ExecutionState.Success(reportFile)
                            onRecordHistory(tool.name, "Inspected ${meta.width}x${meta.height} image")
                        }

                        // EXIF Cleaner
                        isExifCleaner -> {
                            val uri = selectedImageUri ?: return@launch
                            val cleaned = ImageEngine.stripExif(context, uri, cleanOutputFormat, 98)
                            resultImage = cleaned
                            resultFile = cleaned.file
                            executionState = ExecutionState.Success(cleaned)
                            onRecordHistory(tool.name, "Sanitized photo, stripped all EXIF, GPS & AI metadata")
                        }

                        // Single image filters and transformations
                        else -> {
                            val uri = selectedImageUri ?: return@launch
                            val res = when {
                                tool.id.contains("rotat") -> ImageEngine.rotateImage(context, uri, rotationDegrees)
                                tool.id.contains("flip") -> ImageEngine.flipImage(context, uri, flipHorizontal, flipVertical)
                                tool.id.contains("crop") -> ImageEngine.cropImageCustom(context, uri, finalCropLeft, finalCropTop, finalCropW, finalCropH)
                                tool.id.contains("resiz") -> {
                                    val w = targetWidthStr.toIntOrNull() ?: 1080
                                    val h = targetHeightStr.toIntOrNull() ?: 1080
                                    ImageEngine.resizeImage(context, uri, w, h, selectedFormat, qualitySlider.toInt())
                                }
                                tool.id.contains("pixelate") -> ImageEngine.pixelateImage(context, uri, pixelBlockSize)
                                tool.id.contains("sharpen") -> ImageEngine.sharpenImage(context, uri, sharpenIntensity)
                                tool.id.contains("blur") -> ImageEngine.applyFilter(context, uri, isGrayscale = false, blurRadius = blurRadius)
                                tool.id.contains("gray") -> ImageEngine.applyFilter(context, uri, isGrayscale = true)
                                tool.id.contains("invert") -> ImageEngine.applyFilter(context, uri, isGrayscale = false, isInvert = true)
                                tool.id.contains("brightness") -> ImageEngine.applyFilter(context, uri, isGrayscale = false, brightness = brightnessOffset)
                                tool.id.contains("contrast") -> ImageEngine.applyFilter(context, uri, isGrayscale = false, contrast = contrastFactor)
                                tool.id.contains("saturation") -> ImageEngine.adjustSaturation(context, uri, saturationFactor)
                                tool.id.contains("border") -> ImageEngine.addBorder(context, uri, borderWidthPx, borderColorInt)
                                tool.id.contains("rounded") -> ImageEngine.roundedCorners(context, uri, cornerRadiusPercent)
                                tool.id.contains("watermark") -> ImageEngine.addWatermark(context, uri, watermarkText, watermarkPosition, watermarkAlpha)
                                tool.id.contains("text_overlay") -> ImageEngine.textOverlay(context, uri, overlayCaption, overlayFontSize, android.graphics.Color.WHITE, overlayPosition, overlayHasBg)
                                tool.id.contains("png") -> ImageEngine.convertFormat(context, uri, ImageOutputFormat.PNG)
                                tool.id.contains("webp") -> ImageEngine.convertFormat(context, uri, ImageOutputFormat.WEBP, qualitySlider.toInt())
                                tool.id.contains("jpg") || tool.id.contains("jpeg") -> ImageEngine.convertFormat(context, uri, ImageOutputFormat.JPEG, qualitySlider.toInt())
                                else -> ImageEngine.compressImage(context, uri, qualitySlider.toInt(), selectedFormat)
                            }

                            resultImage = res
                            resultFile = res.file
                            executionState = ExecutionState.Success(res)
                            onRecordHistory(tool.name, "Processed ${res.width}x${res.height} (${FileUtils.formatFileSize(res.newSize)})")
                        }
                    }

                    // Auto-save result if file generated
                    resultFile?.let { f ->
                        if (autoSaveEnabled) {
                            val mime = FileUtils.getMimeType(f)
                            val (savedUri, path) = FileUtils.saveToDeviceMemory(context, f, mime)
                            if (savedUri != null) {
                                savedLocationPath = path
                                Toast.makeText(context, "${f.name} saved", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    executionState = ExecutionState.Error(
                        error = "Operation Failed",
                        details = e.localizedMessage ?: "Unknown image processing error"
                    )
                }
            }
        }
    ) {
        // 1. INPUT SELECTION UI
        if (isTextInput) {
            // Base64 Text Input Area
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Paste Base64 Image String", fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                base64InputText = clipText
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste")
                        }
                    }
                    OutlinedTextField(
                        value = base64InputText,
                        onValueChange = { base64InputText = it },
                        placeholder = { Text("data:image/png;base64,iVBORw0KGgo...") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (isMultiImage) {
            // Multi-Photo Picker Area
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
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedImageUris.isEmpty()) "Select multiple photos" else "${selectedImageUris.size} photos selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedImageUris.isEmpty()) "Choose 2 or more images from gallery" else "Ready to process batch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedImageUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(selectedImageUris) { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { multiPickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedImageUris.isEmpty()) "Select Photos" else "Change Photos")
                    }
                }
            }
        } else {
            // Single Photo Picker Area
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
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        metadataInfo?.let { meta ->
                            Text(
                                text = "${meta.width} × ${meta.height} px • ${FileUtils.formatFileSize(meta.sizeBytes)} • ${meta.aspectRatio}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { singlePickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Photo")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select a photo to proceed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { singlePickerLauncher.launch("image/*") }) {
                            Text("Choose from Gallery")
                        }
                    }
                }
            }
        }

        // 2. DEDICATED OPTIMIZED CONTROLS FOR EACH TOOL TYPE

        // Cropper Interactive Viewfinder & Precision Controls (Editing App Style)
        if (tool.id.contains("crop") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Interactive Crop Viewfinder", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${(finalCropW * imgW).toInt()} × ${(finalCropH * imgH).toInt()} px",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Interactive Viewfinder Box with Rule of Thirds Grid and Drag to Pan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Panning based on drag gestures
                                    if (maxPanLeft > 0.001f) {
                                        val deltaX = (dragAmount.x / 260f) / maxPanLeft
                                        cropPanX = (cropPanX + deltaX).coerceIn(0f, 1f)
                                    }
                                    if (maxPanTop > 0.001f) {
                                        val deltaY = (dragAmount.y / 260f) / maxPanTop
                                        cropPanY = (cropPanY + deltaY).coerceIn(0f, 1f)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Photo to crop",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Canvas overlay for crop window, rule of thirds lines, and corner brackets
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cW = size.width
                            val cH = size.height

                            val leftPx = cW * finalCropLeft
                            val topPx = cH * finalCropTop
                            val rightPx = leftPx + (cW * finalCropW)
                            val bottomPx = topPx + (cH * finalCropH)

                            val scrimColor = Color.Black.copy(alpha = 0.55f)

                            // 4 outer dimmed scrim rectangles
                            drawRect(scrimColor, topLeft = Offset(0f, 0f), size = Size(cW, topPx))
                            drawRect(scrimColor, topLeft = Offset(0f, bottomPx), size = Size(cW, cH - bottomPx))
                            drawRect(scrimColor, topLeft = Offset(0f, topPx), size = Size(leftPx, bottomPx - topPx))
                            drawRect(scrimColor, topLeft = Offset(rightPx, topPx), size = Size(cW - rightPx, bottomPx - topPx))

                            // Crisp white crop frame border
                            val frameStroke = Stroke(width = 2.dp.toPx())
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(leftPx, topPx),
                                size = Size(rightPx - leftPx, bottomPx - topPx),
                                style = frameStroke
                            )

                            // Rule of Thirds 3x3 Guidelines
                            val gridColor = Color.White.copy(alpha = 0.5f)
                            val gridStroke = Stroke(width = 1.dp.toPx())
                            val thirdW = (rightPx - leftPx) / 3f
                            val thirdH = (bottomPx - topPx) / 3f

                            drawLine(gridColor, Offset(leftPx + thirdW, topPx), Offset(leftPx + thirdW, bottomPx), strokeWidth = gridStroke.width)
                            drawLine(gridColor, Offset(leftPx + (thirdW * 2), topPx), Offset(leftPx + (thirdW * 2), bottomPx), strokeWidth = gridStroke.width)
                            drawLine(gridColor, Offset(leftPx, topPx + thirdH), Offset(rightPx, topPx + thirdH), strokeWidth = gridStroke.width)
                            drawLine(gridColor, Offset(leftPx, topPx + (thirdH * 2)), Offset(rightPx, topPx + (thirdH * 2)), strokeWidth = gridStroke.width)

                            // Bold Corner L-Brackets
                            val bLen = 18.dp.toPx()
                            val bStroke = 3.5.dp.toPx()
                            val bColor = Color.White

                            // Top Left
                            drawLine(bColor, Offset(leftPx - 1, topPx), Offset(leftPx + bLen, topPx), strokeWidth = bStroke)
                            drawLine(bColor, Offset(leftPx, topPx - 1), Offset(leftPx, topPx + bLen), strokeWidth = bStroke)

                            // Top Right
                            drawLine(bColor, Offset(rightPx - bLen, topPx), Offset(rightPx + 1, topPx), strokeWidth = bStroke)
                            drawLine(bColor, Offset(rightPx, topPx - 1), Offset(rightPx, topPx + bLen), strokeWidth = bStroke)

                            // Bottom Left
                            drawLine(bColor, Offset(leftPx - 1, bottomPx), Offset(leftPx + bLen, bottomPx), strokeWidth = bStroke)
                            drawLine(bColor, Offset(leftPx, bottomPx - bLen), Offset(leftPx, bottomPx + 1), strokeWidth = bStroke)

                            // Bottom Right
                            drawLine(bColor, Offset(rightPx - bLen, bottomPx), Offset(rightPx + 1, bottomPx), strokeWidth = bStroke)
                            drawLine(bColor, Offset(rightPx, bottomPx - bLen), Offset(rightPx, bottomPx + 1), strokeWidth = bStroke)
                        }

                        // Guidance tip chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Drag to reposition framing",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Aspect Ratio Presets
                    Text("Aspect Ratio", fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = cropIsFreeform,
                            onClick = { cropIsFreeform = true },
                            label = { Text("Freeform") }
                        )

                        listOf(
                            Triple(1, 1, "1:1 Square"),
                            Triple(4, 3, "4:3"),
                            Triple(16, 9, "16:9"),
                            Triple(9, 16, "9:16 Story"),
                            Triple(3, 2, "3:2"),
                            Triple(2, 3, "2:3")
                        ).forEach { (w, h, label) ->
                            FilterChip(
                                selected = !cropIsFreeform && cropRatioW == w && cropRatioH == h,
                                onClick = {
                                    cropIsFreeform = false
                                    cropRatioW = w
                                    cropRatioH = h
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Precision Zoom & Crop Framing Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Framing Zoom Scale: ${(cropZoomScale * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = cropZoomScale,
                        onValueChange = { cropZoomScale = it },
                        valueRange = 0.3f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Horizontal Pan Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Horizontal Pan", style = MaterialTheme.typography.bodySmall)
                        Text(if (cropPanX < 0.45f) "Left" else if (cropPanX > 0.55f) "Right" else "Center", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = cropPanX,
                        onValueChange = { cropPanX = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vertical Pan Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vertical Pan", style = MaterialTheme.typography.bodySmall)
                        Text(if (cropPanY < 0.45f) "Top" else if (cropPanY > 0.55f) "Bottom" else "Center", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = cropPanY,
                        onValueChange = { cropPanY = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Reset / Center Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                cropPanX = 0.5f
                                cropPanY = 0.5f
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Center Crop")
                        }
                        OutlinedButton(
                            onClick = {
                                cropPanX = 0.5f
                                cropPanY = 0.5f
                                cropZoomScale = 1.0f
                                cropRatioW = 1
                                cropRatioH = 1
                                cropIsFreeform = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }
                }
            }
        }

        // Resizer Dimension Presets & Custom Inputs
        if (tool.id.contains("resiz") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Resize Target Resolution", fontWeight = FontWeight.Bold)

                    // Quick Presets
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val metaW = metadataInfo?.width ?: 1920
                        val metaH = metadataInfo?.height ?: 1080
                        listOf(
                            "Original" to Pair(metaW, metaH),
                            "4K (3840)" to Pair(3840, (3840 * metaH / metaW)),
                            "1080p FHD" to Pair(1920, (1920 * metaH / metaW)),
                            "720p HD" to Pair(1280, (1280 * metaH / metaW)),
                            "50% Half" to Pair(metaW / 2, metaH / 2),
                            "25% Small" to Pair(metaW / 4, metaH / 4)
                        ).forEach { (label, dims) ->
                            FilterChip(
                                selected = targetWidthStr == dims.first.toString() && targetHeightStr == dims.second.toString(),
                                onClick = {
                                    targetWidthStr = dims.first.toString()
                                    targetHeightStr = dims.second.toString()
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetWidthStr,
                            onValueChange = { newW ->
                                targetWidthStr = newW
                                if (lockAspectRatio && metadataInfo != null && metadataInfo!!.width > 0) {
                                    val nw = newW.toIntOrNull() ?: 0
                                    targetHeightStr = ((nw * metadataInfo!!.height) / metadataInfo!!.width).toString()
                                }
                            },
                            label = { Text("Width (px)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetHeightStr,
                            onValueChange = { targetHeightStr = it },
                            label = { Text("Height (px)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lock Aspect Ratio", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = lockAspectRatio, onCheckedChange = { lockAspectRatio = it })
                    }
                }
            }
        }

        // Rotator Angle Selector
        if (tool.id.contains("rotat") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Rotation Angle", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(90f to "90° CW", 180f to "180°", 270f to "90° CCW").forEach { (deg, label) ->
                            FilterChip(
                                selected = rotationDegrees == deg,
                                onClick = { rotationDegrees = deg },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Slider(
                        value = rotationDegrees,
                        onValueChange = { rotationDegrees = it },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Custom Angle: ${rotationDegrees.toInt()}°", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Flipper Direction Selector
        if (tool.id.contains("flip") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Flip Orientation", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = flipHorizontal && !flipVertical,
                            onClick = { flipHorizontal = true; flipVertical = false },
                            label = { Text("Horizontal (Mirror)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !flipHorizontal && flipVertical,
                            onClick = { flipHorizontal = false; flipVertical = true },
                            label = { Text("Vertical (Upside Down)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Filter: Blur Slider
        if (tool.id.contains("blur") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Blur Intensity: ${blurRadius.toInt()} px", fontWeight = FontWeight.Bold)
                    Slider(
                        value = blurRadius,
                        onValueChange = { blurRadius = it },
                        valueRange = 2f..28f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Filter: Pixelate Block Size
        if (tool.id.contains("pixelate") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pixel Mosaic Block Size: ${pixelBlockSize} px", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(8 to "Subtle", 16 to "Medium", 32 to "Heavy Censor", 48 to "8-Bit Retro").forEach { (size, label) ->
                            FilterChip(
                                selected = pixelBlockSize == size,
                                onClick = { pixelBlockSize = size },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Filter: Sharpen Intensity
        if (tool.id.contains("sharpen") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edge Sharpening Clarity: ${(sharpenIntensity * 10).toInt() / 10f}x", fontWeight = FontWeight.Bold)
                    Slider(
                        value = sharpenIntensity,
                        onValueChange = { sharpenIntensity = it },
                        valueRange = 0.4f..2.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Brightness Adjuster Slider
        if (tool.id.contains("brightness") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Brightness: ${if (brightnessOffset > 0) "+${brightnessOffset.toInt()}" else brightnessOffset.toInt()}", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { brightnessOffset = 0f }) { Text("Reset") }
                    }
                    Slider(
                        value = brightnessOffset,
                        onValueChange = { brightnessOffset = it },
                        valueRange = -100f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Contrast Adjuster Slider
        if (tool.id.contains("contrast") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contrast Dynamic Range: ${(contrastFactor * 10).toInt() / 10f}x", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { contrastFactor = 1.0f }) { Text("Reset") }
                    }
                    Slider(
                        value = contrastFactor,
                        onValueChange = { contrastFactor = it },
                        valueRange = 0.5f..2.4f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Saturation Adjuster Slider
        if (tool.id.contains("saturation") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Color Saturation: ${(saturationFactor * 10).toInt() / 10f}x", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { saturationFactor = 1.0f }) { Text("Reset") }
                    }
                    Slider(
                        value = saturationFactor,
                        onValueChange = { saturationFactor = it },
                        valueRange = 0f..2.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Border Generator Controls
        if (tool.id.contains("border") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Border Thickness: ${borderWidthPx} px", fontWeight = FontWeight.Bold)
                    Slider(
                        value = borderWidthPx.toFloat(),
                        onValueChange = { borderWidthPx = it.toInt() },
                        valueRange = 4f..80f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Border Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            android.graphics.Color.WHITE to "White",
                            android.graphics.Color.BLACK to "Black",
                            android.graphics.Color.parseColor("#3B82F6") to "Blue",
                            android.graphics.Color.parseColor("#EF4444") to "Red",
                            android.graphics.Color.parseColor("#F59E0B") to "Gold"
                        ).forEach { (cInt, label) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(cInt))
                                    .border(
                                        width = if (borderColorInt == cInt) 3.dp else 1.dp,
                                        color = if (borderColorInt == cInt) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { borderColorInt = cInt }
                            )
                        }
                    }
                }
            }
        }

        // Rounded Corners Controls
        if (tool.id.contains("rounded") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Corner Roundness: ${(cornerRadiusPercent * 100).toInt()}%", fontWeight = FontWeight.Bold)
                    Slider(
                        value = cornerRadiusPercent,
                        onValueChange = { cornerRadiusPercent = it },
                        valueRange = 0.03f..0.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Output will be saved as transparent PNG format.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Watermark Controls
        if (tool.id.contains("watermark") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Watermark Settings", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "BOTTOM_RIGHT" to "Bottom Right",
                            "CENTER" to "Center",
                            "TOP_LEFT" to "Top Left"
                        ).forEach { (pos, label) ->
                            FilterChip(
                                selected = watermarkPosition == pos,
                                onClick = { watermarkPosition = pos },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text("Opacity: ${(watermarkAlpha * 100 / 255)}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = watermarkAlpha.toFloat(),
                        onValueChange = { watermarkAlpha = it.toInt() },
                        valueRange = 50f..255f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Text Overlay Controls
        if (tool.id.contains("text_overlay") && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Text Caption / Meme Overlay", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = overlayCaption,
                        onValueChange = { overlayCaption = it },
                        label = { Text("Overlay Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "TOP" to "Top Header",
                            "CENTER" to "Center",
                            "BOTTOM" to "Bottom Caption"
                        ).forEach { (pos, label) ->
                            FilterChip(
                                selected = overlayPosition == pos,
                                onClick = { overlayPosition = pos },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Banner Background", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = overlayHasBg, onCheckedChange = { overlayHasBg = it })
                    }
                }
            }
        }

        // Collage Columns selector
        if (tool.id == "image_collage_maker" && selectedImageUris.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Collage Grid Layout", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = collageColumns == 2,
                            onClick = { collageColumns = 2 },
                            label = { Text("2 Columns Grid") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = collageColumns == 3,
                            onClick = { collageColumns = 3 },
                            label = { Text("3 Columns Grid") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Compressor & Format Quality Controls
        if ((tool.id.contains("compress") || tool.id.contains("convert")) && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quality & Compression", fontWeight = FontWeight.Bold)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ImageOutputFormat.values().forEach { fmt ->
                            FilterChip(
                                selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt },
                                label = { Text(fmt.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Quality: ${qualitySlider.toInt()}%")
                        Text(
                            text = if (qualitySlider < 60) "High Compression" else "High Quality",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Slider(
                        value = qualitySlider,
                        onValueChange = { qualitySlider = it },
                        valueRange = 10f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Live Technical Metadata & EXIF specifications display
        if (isExifMetadataTool && (metadataInfo != null || exifDataMap.isNotEmpty())) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Technical Metadata & Camera EXIF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    metadataInfo?.let { m ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Resolution", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${m.width} × ${m.height} px", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("File Size", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FileUtils.formatFileSize(m.sizeBytes), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    exifDataMap.forEach { (key, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Photo Metadata & AI Content Cleaner Card
        if (isExifCleaner && selectedImageUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Photo Privacy & AI Content Clean", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Wipes camera information, GPS coordinates, author tags, and AI generation parameters (Midjourney, DALL-E, Stable Diffusion, Firefly, ComfyUI, C2PA) to guarantee total privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Text("Clean Output Format", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "JPEG" to "Clean JPG (High Quality)",
                            "PNG" to "Clean PNG (Lossless)"
                        ).forEach { (fmt, label) ->
                            FilterChip(
                                selected = cleanOutputFormat == fmt,
                                onClick = { cleanOutputFormat = fmt },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (exifDataMap.isNotEmpty() && !exifDataMap.containsKey("Privacy Status")) {
                        Text("Detected Privacy Tags in Photo:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        exifDataMap.entries.take(5).forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(k, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                Text(v, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }

                    listOf(
                        "Wipe AI Prompts & Generation Markers" to "Clears prompt text, seed numbers, and software signatures",
                        "Strip GPS Location & Geotags" to "Removes latitude, longitude, and elevation data",
                        "Remove Camera & Hardware Serial" to "Clears camera make, lens model, and device fingerprint",
                        "Wipe Creation Timestamps" to "Clears camera date/time and editing history"
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

        // Color Palette Swatches Display (For Color Tools)
        if (isColorTool && extractedPalette.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extracted Palette (Tap to copy HEX)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    extractedPalette.forEach { hex ->
                        val colorParsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(hex))
                                    Toast.makeText(context, "Copied $hex to clipboard", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colorParsed)
                                        .border(1.dp, Color.LightGray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hex, fontWeight = FontWeight.Bold)
                                    val r = (colorParsed.red * 255).toInt()
                                    val g = (colorParsed.green * 255).toInt()
                                    val b = (colorParsed.blue * 255).toInt()
                                    Text("rgb($r, $g, $b)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Duplicate Matches Table
        if (duplicateMatches.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Perceptual Hash Comparison", fontWeight = FontWeight.Bold)
                    duplicateMatches.forEach { match ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Image #${match.file1Index + 1} vs Image #${match.file2Index + 1}", style = MaterialTheme.typography.bodyMedium)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (match.isDuplicate) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (match.isDuplicate) "Duplicate (${match.similarityPercent}%)" else "${match.similarityPercent}% Match",
                                    color = if (match.isDuplicate) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Base64 Result Text Card
        base64ResultText?.let { b64 ->
            ResultTextCard(
                title = "Base64 Image String",
                text = b64.take(1000) + if (b64.length > 1000) "\n\n... (${b64.length} characters total)" else ""
            )
        }

        // Result File Card with Device Storage Auto-Save Path
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
