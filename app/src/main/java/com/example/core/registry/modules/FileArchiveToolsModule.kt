package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object FileArchiveToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "file_inspector",
            name = "File Inspector",
            category = ToolCategory.FILE,
            description = "Analyze file name, exact byte size, MIME content type, and extension.",
            icon = Icons.Default.Info,
            keywords = listOf("file", "inspect", "size", "mime type", "extension", "info"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "file_metadata_cleaner",
            name = "Files Metadata Remover",
            category = ToolCategory.FILE,
            description = "Wipe tracking tags, author, creator, history, and AI metadata from documents and files.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("file metadata remover", "metadata", "cleaner", "strip", "sanitize", "privacy", "clear tags"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "zip_creator",
            name = "ZIP Archive Creator",
            category = ToolCategory.FILE,
            description = "Bundle multiple files into a single compressed .zip archive.",
            icon = Icons.Default.FolderZip,
            keywords = listOf("zip", "archive", "compress", "bundle", "package"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "zip_extractor",
            name = "ZIP Extractor",
            category = ToolCategory.FILE,
            description = "Extract files from a .zip archive securely with Zip-Slip path traversal protection.",
            icon = Icons.Default.Unarchive,
            keywords = listOf("unzip", "extract", "zip", "decompress", "archive"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "file_checksum",
            name = "File Checksum Verifier",
            category = ToolCategory.FILE,
            description = "Compute and verify SHA-256 and MD5 hash checksums to check file integrity.",
            icon = Icons.Default.VerifiedUser,
            keywords = listOf("checksum", "hash", "sha256", "md5", "integrity", "verify"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "file_size_analyzer",
            name = "Storage & File Analyzer",
            category = ToolCategory.FILE,
            description = "Inspect storage consumption and temporary app cache allocation.",
            icon = Icons.Default.Storage,
            keywords = listOf("storage", "space", "disk", "cache", "analyzer"),
            inputType = ToolInputType.NONE
        )
    )
}
