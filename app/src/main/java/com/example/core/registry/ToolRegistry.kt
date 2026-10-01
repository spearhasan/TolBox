package com.example.core.registry

import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.registry.modules.*

/**
 * Central registry implementing the modular plugin architecture.
 *
 * All utility categories are decoupled into modular definition providers:
 * - [VideoToolsModule]
 * - [AudioToolsModule]
 * - [ImageToolsModule]
 * - [GifToolsModule]
 * - [PdfToolsModule]
 * - [DocumentToolsModule]
 * - [TextToolsModule]
 * - [OcrToolsModule]
 * - [QrToolsModule]
 * - [DevToolsModule]
 * - [MathCalcToolsModule]
 * - [FileArchiveToolsModule]
 *
 * Easily scalable to 500+ tools while maintaining lightweight startup via lazy evaluation.
 */
object ToolRegistry {

    val allTools: List<ToolDefinition> by lazy {
        listOf(
            VideoToolsModule.tools,
            AudioToolsModule.tools,
            ImageToolsModule.tools,
            GifToolsModule.tools,
            PdfToolsModule.tools,
            DocumentToolsModule.tools,
            TextToolsModule.tools,
            OcrToolsModule.tools,
            QrToolsModule.tools,
            DevToolsModule.tools,
            MathCalcToolsModule.tools,
            FileArchiveToolsModule.tools
        ).flatten()
    }

    private val toolIndexMap: Map<String, ToolDefinition> by lazy {
        allTools.associateBy { it.id }
    }

    fun getToolById(id: String): ToolDefinition? = toolIndexMap[id]

    fun searchTools(query: String, selectedCategory: ToolCategory = ToolCategory.ALL): List<ToolDefinition> {
        val q = query.trim().lowercase()
        return allTools.filter { tool ->
            val matchesCategory = (selectedCategory == ToolCategory.ALL || tool.category == selectedCategory)
            if (!matchesCategory) return@filter false

            if (q.isEmpty()) return@filter true

            tool.name.lowercase().contains(q) ||
            tool.category.title.lowercase().contains(q) ||
            tool.description.lowercase().contains(q) ||
            tool.keywords.any { it.contains(q) }
        }
    }

    val popularTools: List<ToolDefinition> by lazy {
        listOfNotNull(
            getToolById("image_metadata_cleaner"),
            getToolById("video_metadata_cleaner"),
            getToolById("file_metadata_cleaner"),
            getToolById("video_to_mp3"),
            getToolById("image_compressor"),
            getToolById("pdf_merge"),
            getToolById("qr_generator"),
            getToolById("audio_cutter"),
            getToolById("ocr_image_to_text"),
            getToolById("hash_generator"),
            getToolById("unit_converter")
        )
    }
}
