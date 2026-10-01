package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object PdfToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "pdf_merge",
            name = "PDF Merge",
            category = ToolCategory.PDF,
            description = "Combine multiple PDF documents into a single organized file.",
            icon = Icons.Default.CallMerge,
            keywords = listOf("merge", "combine", "pdf", "join", "stitch"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "pdf_split",
            name = "PDF Split",
            category = ToolCategory.PDF,
            description = "Divide a large PDF document into separate page intervals or single pages.",
            icon = Icons.Default.CallSplit,
            keywords = listOf("split", "separate", "pages", "divide"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_extract_pages",
            name = "PDF Extract Pages",
            category = ToolCategory.PDF,
            description = "Extract specific chosen pages (e.g. 1, 3-5) into a new PDF document.",
            icon = Icons.Default.SelectAll,
            keywords = listOf("extract pages", "select pages", "export pages"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_delete_pages",
            name = "PDF Delete Pages",
            category = ToolCategory.PDF,
            description = "Remove unwanted blank or extra pages from a PDF document.",
            icon = Icons.Default.Delete,
            keywords = listOf("delete page", "remove page", "cut page"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_reorder_pages",
            name = "PDF Reorder Pages",
            category = ToolCategory.PDF,
            description = "Rearrange the visual sequence of pages in any order.",
            icon = Icons.Default.Reorder,
            keywords = listOf("reorder", "sort pages", "sequence", "arrange"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_rotate_pages",
            name = "PDF Rotate Pages",
            category = ToolCategory.PDF,
            description = "Rotate upside-down or sideways pages by 90°, 180°, or 270°.",
            icon = Icons.Default.RotateRight,
            keywords = listOf("rotate page", "orientation", "turn page"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_to_jpg",
            name = "PDF → JPG",
            category = ToolCategory.PDF,
            description = "Render and export PDF document pages as crisp JPG photos.",
            icon = Icons.Default.Image,
            keywords = listOf("pdf to jpg", "convert", "export images"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_to_png",
            name = "PDF → PNG",
            category = ToolCategory.PDF,
            description = "Render PDF pages into lossless high-resolution PNG images.",
            icon = Icons.Default.Image,
            keywords = listOf("pdf to png", "lossless", "high res"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_to_webp",
            name = "PDF → WebP",
            category = ToolCategory.PDF,
            description = "Convert PDF pages into modern space-saving WebP images.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("pdf to webp", "next-gen", "convert"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_jpg_to_pdf",
            name = "JPG → PDF",
            category = ToolCategory.PDF,
            description = "Convert JPG photo into a standard A4 or Letter size PDF page.",
            icon = Icons.Default.PictureAsPdf,
            keywords = listOf("jpg to pdf", "photo to pdf", "convert"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "pdf_png_to_pdf",
            name = "PNG → PDF",
            category = ToolCategory.PDF,
            description = "Convert high-resolution PNG graphic or document into PDF.",
            icon = Icons.Default.PictureAsPdf,
            keywords = listOf("png to pdf", "image to pdf"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "pdf_multiple_images_to_pdf",
            name = "Multiple Images → PDF",
            category = ToolCategory.PDF,
            description = "Bundle multiple receipts, notes, or photos into a single PDF.",
            icon = Icons.Default.Collections,
            keywords = listOf("multiple images", "batch pdf", "combine", "scan"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        ),
        ToolDefinition(
            id = "pdf_compressor",
            name = "PDF Compressor",
            category = ToolCategory.PDF,
            description = "Reduce PDF file size for email attachment limits without losing text clarity.",
            icon = Icons.Default.Compress,
            keywords = listOf("compress pdf", "shrink pdf", "reduce mb", "optimize"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_metadata_viewer",
            name = "PDF Metadata Viewer",
            category = ToolCategory.PDF,
            description = "Inspect document title, author, creator software, PDF version, and dates.",
            icon = Icons.Default.Info,
            keywords = listOf("pdf metadata", "author", "creator", "details"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_metadata_cleaner",
            name = "PDF Metadata Cleaner",
            category = ToolCategory.PDF,
            description = "Strip hidden author names, company tags, and edit timestamps.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("clean pdf", "privacy", "strip metadata", "sanitize"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_page_counter",
            name = "PDF Page Counter",
            category = ToolCategory.PDF,
            description = "Instantly inspect total page count and document dimensions.",
            icon = Icons.Default.Numbers,
            keywords = listOf("page count", "how many pages", "length"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_size_analyzer",
            name = "PDF Size Analyzer",
            category = ToolCategory.PDF,
            description = "Analyze memory breakdown: embedded fonts, images, and text streams.",
            icon = Icons.Default.Analytics,
            keywords = listOf("size analyzer", "breakdown", "fonts", "stream size"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_text_extractor",
            name = "PDF Text Extractor",
            category = ToolCategory.PDF,
            description = "Extract all selectable text into an editable plain text note.",
            icon = Icons.Default.TextFields,
            keywords = listOf("extract text", "copy text", "pdf to txt"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_text_search",
            name = "PDF Text Search",
            category = ToolCategory.PDF,
            description = "Search keywords across all pages with occurrence counts and highlights.",
            icon = Icons.Default.Search,
            keywords = listOf("search pdf", "find word", "lookup", "locate"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_bookmark_viewer",
            name = "PDF Bookmark Viewer",
            category = ToolCategory.PDF,
            description = "View interactive table of contents, outline bookmarks, and page links.",
            icon = Icons.Default.Bookmark,
            keywords = listOf("bookmark", "toc", "table of contents", "outline"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_attachment_viewer",
            name = "PDF Attachment Viewer",
            category = ToolCategory.PDF,
            description = "Inspect and extract files embedded as attachments inside a PDF.",
            icon = Icons.Default.AttachFile,
            keywords = listOf("attachment", "embedded file", "extract file"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_thumbnail_generator",
            name = "PDF Page Thumbnail Generator",
            category = ToolCategory.PDF,
            description = "Generate visual thumbnail preview grid of all document pages.",
            icon = Icons.Default.GridOn,
            keywords = listOf("thumbnail", "preview grid", "page preview"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_password_protection",
            name = "PDF Password Protection",
            category = ToolCategory.PDF,
            description = "Check AES encryption state and password lock on protected documents.",
            icon = Icons.Default.Lock,
            keywords = listOf("password", "encrypt", "protect", "locked"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_permission_viewer",
            name = "PDF Permission Viewer",
            category = ToolCategory.PDF,
            description = "Check allowed document rights: printing, text copying, and form filling.",
            icon = Icons.Default.Security,
            keywords = listOf("permissions", "printing rights", "copy rights"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_form_viewer",
            name = "PDF Form Field Viewer",
            category = ToolCategory.PDF,
            description = "Inspect interactive AcroForm text inputs, checkboxes, and signatures.",
            icon = Icons.Default.FactCheck,
            keywords = listOf("form", "acroform", "fields", "inputs"),
            inputType = ToolInputType.PDF_SINGLE
        ),
        ToolDefinition(
            id = "pdf_annotation_viewer",
            name = "PDF Annotation Viewer",
            category = ToolCategory.PDF,
            description = "View comments, highlights, stamps, and sticky notes inside PDF.",
            icon = Icons.Default.Notes,
            keywords = listOf("annotations", "comments", "highlights", "notes"),
            inputType = ToolInputType.PDF_SINGLE
        )
    )
}
