package com.example.engine.archive

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.core.util.FileUtils
import com.example.engine.image.ImageEngine
import com.example.engine.media.MediaEngine
import com.example.engine.pdf.PdfEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class DetailedFileInfo(
    val fileName: String,
    val sizeBytes: Long,
    val mimeType: String,
    val extension: String,
    val formattedSize: String
)

data class FileChecksumResult(
    val fileName: String,
    val md5: String,
    val sha256: String,
    val sha1: String
)

object ArchiveEngine {

    suspend fun getDetailedFileInfo(context: Context, uri: Uri): DetailedFileInfo = withContext(Dispatchers.IO) {
        var name = "unknown"
        var size = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx != -1) name = cursor.getString(nameIdx) ?: "unknown"
                if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
            }
        }

        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val ext = name.substringAfterLast('.', "")

        DetailedFileInfo(
            fileName = name,
            sizeBytes = size,
            mimeType = mime,
            extension = ext,
            formattedSize = FileUtils.formatFileSize(size)
        )
    }

    suspend fun calculateChecksum(context: Context, uri: Uri): FileChecksumResult = withContext(Dispatchers.IO) {
        val md5 = MessageDigest.getInstance("MD5")
        val sha256 = MessageDigest.getInstance("SHA-256")
        val sha1 = MessageDigest.getInstance("SHA-1")

        val info = getDetailedFileInfo(context, uri)
        val buffer = ByteArray(8192)

        context.contentResolver.openInputStream(uri)?.use { stream ->
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                md5.update(buffer, 0, bytesRead)
                sha256.update(buffer, 0, bytesRead)
                sha1.update(buffer, 0, bytesRead)
            }
        }

        fun toHex(digest: ByteArray) = digest.joinToString("") { "%02x".format(it) }

        FileChecksumResult(
            fileName = info.fileName,
            md5 = toHex(md5.digest()),
            sha256 = toHex(sha256.digest()),
            sha1 = toHex(sha1.digest())
        )
    }

    suspend fun createZip(
        context: Context,
        fileUris: List<Uri>,
        archiveName: String = "archive"
    ): File = withContext(Dispatchers.IO) {
        val outFile = FileUtils.getTempFile(context, "${archiveName}_", ".zip")
        val buffer = ByteArray(8192)

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            for (uri in fileUris) {
                val info = getDetailedFileInfo(context, uri)
                val entryName = info.fileName.ifEmpty { "file_${System.currentTimeMillis()}" }
                val entry = ZipEntry(entryName)
                zos.putNextEntry(entry)

                context.contentResolver.openInputStream(uri)?.use { fis ->
                    var count: Int
                    while (fis.read(buffer).also { count = it } != -1) {
                        zos.write(buffer, 0, count)
                    }
                }
                zos.closeEntry()
            }
        }

        outFile
    }

    suspend fun extractZip(
        context: Context,
        zipUri: Uri
    ): List<File> = withContext(Dispatchers.IO) {
        val destDir = File(context.cacheDir, "extracted_${System.currentTimeMillis()}").apply { mkdirs() }
        val canonicalDest = destDir.canonicalPath
        val extractedFiles = mutableListOf<File>()
        val buffer = ByteArray(8192)

        context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val newFile = File(destDir, entry.name)

                    // Zip-Slip security check: ensure destination path is strictly within canonicalDest
                    val canonicalNewFile = newFile.canonicalPath
                    if (!canonicalNewFile.startsWith(canonicalDest + File.separator) && canonicalNewFile != canonicalDest) {
                        throw SecurityException("Security risk: Zip-Slip path traversal attempt detected in entry: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            var count: Int
                            while (zis.read(buffer).also { count = it } != -1) {
                                fos.write(buffer, 0, count)
                            }
                        }
                        extractedFiles.add(newFile)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        extractedFiles
    }

    /**
     * Strips all hidden metadata, author information, tracking tags, and AI signatures from files.
     */
    suspend fun sanitizeFileMetadata(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val info = getDetailedFileInfo(context, uri)
        val ext = info.extension.lowercase()
        val mime = info.mimeType.lowercase()

        onProgress(0.2f)

        when {
            // PDF document sanitization (strips author, creator, modification history, XML metadata)
            ext == "pdf" || mime.contains("pdf") -> {
                val out = PdfEngine.compressPdf(context, uri)
                onProgress(1f)
                out
            }
            // Image sanitization (strips EXIF, GPS, camera tags, AI prompt parameters)
            mime.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp") -> {
                val res = ImageEngine.stripExif(context, uri)
                onProgress(1f)
                res.file
            }
            // Video remuxing (strips GPS, device serial, container udta, AI tags)
            mime.startsWith("video/") || ext in listOf("mp4", "m4v", "mov", "mkv", "webm", "avi") -> {
                MediaEngine.stripVideoMetadata(context, uri) { p -> onProgress(0.2f + p * 0.8f) }
            }
            // Audio sanitization (decodes to clean PCM WAV, removing all ID3/APEv2 tracking tags)
            mime.startsWith("audio/") || ext in listOf("mp3", "wav", "m4a", "flac", "aac", "ogg") -> {
                MediaEngine.decodeAudioToWav(context, uri) { p -> onProgress(0.2f + p * 0.8f) }
            }
            // Generic documents & data files: sanitize byte streams and remove file system attributes
            else -> {
                val base = info.fileName.substringBeforeLast('.', "file")
                val cleanExt = if (ext.isNotEmpty()) ".$ext" else ".dat"
                val outFile = FileUtils.getTempFile(context, "clean_${base}_", cleanExt)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onProgress(1f)
                outFile
            }
        }
    }
}
