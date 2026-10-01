package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object OcrToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "ocr_image_to_text",
            name = "Image → Text",
            category = ToolCategory.OCR,
            description = "Extract printed or handwritten text directly from photos and scans.",
            icon = Icons.Default.DocumentScanner,
            keywords = listOf("ocr", "image to text", "extract text", "scan text"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "ocr_screenshot_to_text",
            name = "Screenshot → Text",
            category = ToolCategory.OCR,
            description = "Instantly copy text, codes, or error messages from screenshots.",
            icon = Icons.Default.Screenshot,
            keywords = listOf("screenshot to text", "copy text from image", "screen ocr"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "ocr_pdf_to_text",
            name = "PDF → Text",
            category = ToolCategory.OCR,
            description = "Perform optical character recognition on scanned non-searchable PDFs.",
            icon = Icons.Default.PictureAsPdf,
            keywords = listOf("pdf to text", "scanned pdf", "searchable pdf"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "ocr_multiple_images",
            name = "Extract Text from Multiple Images",
            category = ToolCategory.OCR,
            description = "Batch OCR processing across multiple pages or book scans.",
            icon = Icons.Default.Collections,
            keywords = listOf("batch ocr", "multiple images", "book scan"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        ),
        ToolDefinition(
            id = "ocr_language_detector",
            name = "OCR Language Detector",
            category = ToolCategory.OCR,
            description = "Automatically identify the natural written language of extracted text.",
            icon = Icons.Default.Translate,
            keywords = listOf("language detector", "identify language", "script"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "ocr_region_detector",
            name = "Text Region Detector",
            category = ToolCategory.OCR,
            description = "Detect bounding boxes, paragraphs, and columnar text blocks in image.",
            icon = Icons.Default.SelectAll,
            keywords = listOf("bounding box", "layout", "text regions"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "ocr_result_cleaner",
            name = "OCR Result Cleaner",
            category = ToolCategory.OCR,
            description = "Fix typical OCR misread characters (0/O, 1/l, broken hyphens).",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("clean ocr", "fix typos", "sanitize ocr"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "ocr_result_formatter",
            name = "OCR Result Formatter",
            category = ToolCategory.OCR,
            description = "Reformat raw scanned text into structured paragraphs and bullet lists.",
            icon = Icons.Default.FormatAlignLeft,
            keywords = listOf("format ocr", "paragraphs", "clean reading"),
            inputType = ToolInputType.TEXT_INPUT
        )
    )
}
