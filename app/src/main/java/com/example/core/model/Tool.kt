package com.example.core.model

import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File

enum class ToolCategory(val title: String, val icon: ImageVector) {
    ALL("All", Icons.Default.Apps),
    VIDEO("Video", Icons.Default.Movie),
    AUDIO("Audio", Icons.Default.Audiotrack),
    IMAGE("Image", Icons.Default.Image),
    GIF("GIF", Icons.Default.Animation),
    PDF("PDF", Icons.Default.PictureAsPdf),
    DOCUMENT("Document", Icons.Default.Description),
    TEXT("Text", Icons.Default.Notes),
    OCR("OCR", Icons.Default.DocumentScanner),
    QR("QR Code", Icons.Default.QrCode),
    DEVELOPER("Developer", Icons.Default.Code),
    CALCULATOR("Calculator", Icons.Default.Calculate),
    CONVERTER("Converter", Icons.Default.SwapHoriz),
    FILE("Files", Icons.Default.FolderZip)
}

enum class ToolInputType {
    NONE,
    IMAGE_SINGLE,
    IMAGE_MULTIPLE,
    VIDEO_SINGLE,
    AUDIO_SINGLE,
    PDF_SINGLE,
    FILE_SINGLE,
    FILE_MULTIPLE,
    TEXT_INPUT,
    NUMBER_INPUT
}

/**
 * Polymorphic input wrapper passed into a [Tool]'s execution method.
 */
sealed interface ToolInput {
    object Empty : ToolInput
    data class Text(val value: String) : ToolInput
    data class Number(val value: Double) : ToolInput
    data class SingleUri(val uri: Uri, val mimeType: String = "*/*") : ToolInput
    data class MultipleUris(val uris: List<Uri>) : ToolInput
    data class KeyValue(val parameters: Map<String, Any?>) : ToolInput
}

/**
 * Standardized output result produced by a [Tool] execution.
 */
sealed interface ToolResult {
    data class Text(val content: String) : ToolResult
    data class SingleFile(val file: File, val mimeType: String = "*/*", val formattedSize: String = "") : ToolResult
    data class MultipleFiles(val files: List<File>) : ToolResult
    data class Data<T>(val value: T) : ToolResult
    data class Error(val message: String, val cause: Throwable? = null) : ToolResult
}

/**
 * Core interface defining the structure and execution contract for all modular utility tools.
 *
 * Implements the modular plugin architecture: Core App ≠ Individual Tools.
 * Each tool encapsulates its metadata (id, name, category, description, icon, keywords)
 * and execution routine, allowing dynamic discovery, registration, and pluggability.
 */
interface Tool {
    val id: String
    val name: String
    val category: ToolCategory
    val description: String
    val icon: ImageVector
    val keywords: List<String> get() = emptyList()
    val inputType: ToolInputType get() = ToolInputType.NONE
    val isOffline: Boolean get() = true
    val route: String get() = id

    /**
     * Executes the tool's core logic asynchronously.
     *
     * @param context Application/Activity context for accessing resources or storage.
     * @param input Dynamic input parameters or data payload required by the tool.
     * @param onProgress Optional callback to report processing progress (0.0 to 1.0) and status message.
     * @return [ToolResult] containing output data or throws exception on error.
     */
    suspend fun execute(
        context: Context,
        input: ToolInput = ToolInput.Empty,
        onProgress: ((Float, String) -> Unit)? = null
    ): ToolResult
}

data class ToolDefinition(
    override val id: String,
    override val name: String,
    override val category: ToolCategory,
    override val description: String,
    override val icon: ImageVector,
    override val keywords: List<String> = emptyList(),
    override val inputType: ToolInputType = ToolInputType.NONE,
    override val isOffline: Boolean = true,
    override val route: String = id,
    val executor: (suspend (context: Context, input: ToolInput, onProgress: ((Float, String) -> Unit)?) -> ToolResult)? = null
) : Tool {
    override suspend fun execute(
        context: Context,
        input: ToolInput,
        onProgress: ((Float, String) -> Unit)?
    ): ToolResult {
        return executor?.invoke(context, input, onProgress) ?: ToolResult.Text("Executed $name successfully")
    }
}

sealed interface ExecutionState<out T> {
    object Idle : ExecutionState<Nothing>
    data class Processing(val progress: Float? = null, val message: String = "Processing...") : ExecutionState<Nothing>
    data class Success<T>(val data: T, val message: String = "Completed successfully") : ExecutionState<T>
    data class Error(val error: String, val details: String? = null) : ExecutionState<Nothing>
}

