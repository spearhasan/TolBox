package com.example.core.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat

object FileUtils {

    fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        return when (ext) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "gif" -> "image/gif"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "ogg" -> "audio/ogg"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "txt" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "html" -> "text/html"
            else -> "*/*"
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            .coerceIn(0, units.size - 1)
        return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun getTempFile(context: Context, prefix: String, suffix: String): File {
        val dir = File(context.cacheDir, "toolbox_temp").apply { if (!exists()) mkdirs() }
        return File.createTempFile(prefix, suffix, dir)
    }

    fun clearCache(context: Context): Long {
        val dir = File(context.cacheDir, "toolbox_temp")
        var freedBytes = 0L
        if (dir.exists()) {
            dir.listFiles()?.forEach {
                freedBytes += it.length()
                it.delete()
            }
        }
        return freedBytes
    }

    fun getCacheSize(context: Context): Long {
        val dir = File(context.cacheDir, "toolbox_temp")
        var total = 0L
        if (dir.exists()) {
            dir.listFiles()?.forEach { total += it.length() }
        }
        return total
    }

    fun getShareUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun shareFile(context: Context, file: File, mimeType: String = "*/*") {
        val uri = getShareUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via ToolBox"))
    }

    fun openFile(context: Context, file: File, mimeType: String = "*/*") {
        val uri = getShareUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareFile(context, file, mimeType)
        }
    }

    /**
     * Automatically saves a media/document file to the mobile device's public storage
     * (Movies/ToolBox, Music/ToolBox, Pictures/ToolBox, or Download/ToolBox).
     */
    fun saveToDeviceMemory(
        context: Context,
        sourceFile: File,
        mimeType: String = "*/*",
        folderName: String = "ToolBox"
    ): Pair<Uri?, String> {
        if (!sourceFile.exists()) return null to ""
        val resolver = context.contentResolver
        val fileName = sourceFile.name

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val (collectionUri, subDir) = when {
                    mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MOVIES}/$folderName"
                    mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MUSIC}/$folderName"
                    mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_PICTURES}/$folderName"
                    else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_DOWNLOADS}/$folderName"
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, subDir)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val itemUri = resolver.insert(collectionUri, values)
                if (itemUri != null) {
                    resolver.openOutputStream(itemUri)?.use { out ->
                        sourceFile.inputStream().use { input -> input.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(itemUri, values, null, null)
                    itemUri to "$subDir/$fileName"
                } else {
                    null to ""
                }
            } else {
                val publicDir = when {
                    mimeType.startsWith("video/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    mimeType.startsWith("audio/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    mimeType.startsWith("image/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                }
                val targetDir = File(publicDir, folderName).apply { if (!exists()) mkdirs() }
                val targetFile = File(targetDir, fileName)
                sourceFile.copyTo(targetFile, overwrite = true)
                Uri.fromFile(targetFile) to targetFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null to ""
        }
    }
}
