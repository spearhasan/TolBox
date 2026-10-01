package com.example.engine.image

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import com.example.core.util.FileUtils
import com.example.engine.pdf.PdfEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs

enum class ImageOutputFormat(val extension: String, val compressFormat: Bitmap.CompressFormat, val mimeType: String) {
    JPEG("jpg", Bitmap.CompressFormat.JPEG, "image/jpeg"),
    PNG("png", Bitmap.CompressFormat.PNG, "image/png"),
    WEBP("webp", Bitmap.CompressFormat.WEBP, "image/webp")
}

data class ProcessedImageResult(
    val file: File,
    val originalSize: Long,
    val newSize: Long,
    val width: Int,
    val height: Int
)

data class ImageMetadataInfo(
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val format: String,
    val aspectRatio: String
)

data class DuplicateMatch(
    val file1Index: Int,
    val file2Index: Int,
    val similarityPercent: Int,
    val isDuplicate: Boolean
)

object ImageEngine {

    suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun compressImage(
        context: Context,
        uri: Uri,
        quality: Int,
        format: ImageOutputFormat
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val outFile = FileUtils.getTempFile(context, "compressed_", ".${format.extension}")
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(format.compressFormat, quality.coerceIn(1, 100), fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = bitmap.width,
            height = bitmap.height
        )
    }

    suspend fun resizeImage(
        context: Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int,
        format: ImageOutputFormat = ImageOutputFormat.JPEG,
        quality: Int = 90
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth.coerceAtLeast(10), targetHeight.coerceAtLeast(10), true)
        val outFile = FileUtils.getTempFile(context, "resized_", ".${format.extension}")

        FileOutputStream(outFile).use { fos ->
            scaled.compress(format.compressFormat, quality, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = scaled.width,
            height = scaled.height
        )
    }

    suspend fun convertFormat(
        context: Context,
        uri: Uri,
        format: ImageOutputFormat,
        quality: Int = 95
    ): ProcessedImageResult = compressImage(context, uri, quality, format)

    suspend fun applyFilter(
        context: Context,
        uri: Uri,
        isGrayscale: Boolean,
        blurRadius: Float = 0f,
        isInvert: Boolean = false,
        brightness: Float = 0f,
        contrast: Float = 1f
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val original = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val resultBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix()
        if (isGrayscale) {
            val grayMatrix = ColorMatrix().apply { setSaturation(0f) }
            cm.postConcat(grayMatrix)
        }

        if (isInvert) {
            val invertMatrix = ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(invertMatrix)
        }

        if (brightness != 0f || contrast != 1f) {
            val scale = contrast
            val translate = brightness + (1f - scale) * 128f
            val bcMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(bcMatrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(original, 0f, 0f, paint)

        val finalBitmap = if (blurRadius > 1f) {
            boxBlur(resultBitmap, blurRadius.toInt().coerceIn(1, 30))
        } else {
            resultBitmap
        }

        val outFile = FileUtils.getTempFile(context, "filtered_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = finalBitmap.width,
            height = finalBitmap.height
        )
    }

    suspend fun adjustSaturation(
        context: Context,
        uri: Uri,
        saturation: Float
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val original = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val resultBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix().apply { setSaturation(saturation.coerceIn(0f, 3f)) }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(original, 0f, 0f, paint)

        val outFile = FileUtils.getTempFile(context, "saturated_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = resultBitmap.width,
            height = resultBitmap.height
        )
    }

    suspend fun pixelateImage(
        context: Context,
        uri: Uri,
        blockSize: Int = 16
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val original = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val block = blockSize.coerceIn(4, 64)
        val smallW = (original.width / block).coerceAtLeast(2)
        val smallH = (original.height / block).coerceAtLeast(2)

        val scaledSmall = Bitmap.createScaledBitmap(original, smallW, smallH, false)
        val pixelated = Bitmap.createScaledBitmap(scaledSmall, original.width, original.height, false)

        val outFile = FileUtils.getTempFile(context, "pixelated_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            pixelated.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = pixelated.width,
            height = pixelated.height
        )
    }

    suspend fun sharpenImage(
        context: Context,
        uri: Uri,
        intensity: Float = 1.2f
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val original = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val blurred = boxBlur(original, 2)
        val w = original.width
        val h = original.height
        val origPixels = IntArray(w * h)
        val blurPixels = IntArray(w * h)
        original.getPixels(origPixels, 0, w, 0, 0, w, h)
        blurred.getPixels(blurPixels, 0, w, 0, 0, w, h)

        val resultPixels = IntArray(w * h)
        val weight = intensity.coerceIn(0.2f, 3.0f)

        for (i in origPixels.indices) {
            val orig = origPixels[i]
            val blur = blurPixels[i]

            val r = (Color.red(orig) + (Color.red(orig) - Color.red(blur)) * weight).toInt().coerceIn(0, 255)
            val g = (Color.green(orig) + (Color.green(orig) - Color.green(blur)) * weight).toInt().coerceIn(0, 255)
            val b = (Color.blue(orig) + (Color.blue(orig) - Color.blue(blur)) * weight).toInt().coerceIn(0, 255)

            resultPixels[i] = Color.rgb(r, g, b)
        }

        val sharpened = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        sharpened.setPixels(resultPixels, 0, w, 0, 0, w, h)

        val outFile = FileUtils.getTempFile(context, "sharpened_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            sharpened.compress(Bitmap.CompressFormat.JPEG, 92, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = w,
            height = h
        )
    }

    suspend fun addBorder(
        context: Context,
        uri: Uri,
        borderWidthPx: Int,
        borderColor: Int
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val original = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val bWidth = borderWidthPx.coerceIn(4, 120)
        val newW = original.width + (bWidth * 2)
        val newH = original.height + (bWidth * 2)

        val result = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(borderColor)
        canvas.drawBitmap(original, bWidth.toFloat(), bWidth.toFloat(), null)

        val outFile = FileUtils.getTempFile(context, "bordered_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            result.compress(Bitmap.CompressFormat.JPEG, 92, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = newW,
            height = newH
        )
    }

    suspend fun roundedCorners(
        context: Context,
        uri: Uri,
        radiusPercent: Float = 0.1f
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val original = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val w = original.width
        val h = original.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, w, h)
        val rectF = RectF(rect)
        val roundPx = (minOf(w, h) * radiusPercent.coerceIn(0.01f, 0.5f))

        paint.color = Color.BLACK
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(original, rect, rect, paint)

        val outFile = FileUtils.getTempFile(context, "rounded_", ".png")
        FileOutputStream(outFile).use { fos ->
            output.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = w,
            height = h
        )
    }

    suspend fun rotateImage(
        context: Context,
        uri: Uri,
        degrees: Float
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val outFile = FileUtils.getTempFile(context, "rotated_", ".jpg")

        FileOutputStream(outFile).use { fos ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = rotated.width,
            height = rotated.height
        )
    }

    suspend fun flipImage(
        context: Context,
        uri: Uri,
        horizontal: Boolean = true,
        vertical: Boolean = false
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val matrix = Matrix().apply {
            postScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val outFile = FileUtils.getTempFile(context, "flipped_", ".jpg")

        FileOutputStream(outFile).use { fos ->
            flipped.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = flipped.width,
            height = flipped.height
        )
    }

    suspend fun cropImage(
        context: Context,
        uri: Uri,
        aspectRatioNumerator: Int = 1,
        aspectRatioDenominator: Int = 1
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val targetRatio = aspectRatioNumerator.toFloat() / aspectRatioDenominator.toFloat()
        val currentRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val cropW: Int
        val cropH: Int
        if (currentRatio > targetRatio) {
            cropH = bitmap.height
            cropW = (cropH * targetRatio).toInt()
        } else {
            cropW = bitmap.width
            cropH = (cropW / targetRatio).toInt()
        }

        val startX = (bitmap.width - cropW) / 2
        val startY = (bitmap.height - cropH) / 2

        val cropped = Bitmap.createBitmap(bitmap, startX, startY, cropW, cropH)
        val outFile = FileUtils.getTempFile(context, "cropped_", ".jpg")

        FileOutputStream(outFile).use { fos ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = cropped.width,
            height = cropped.height
        )
    }

    suspend fun cropImageCustom(
        context: Context,
        uri: Uri,
        leftFraction: Float,
        topFraction: Float,
        widthFraction: Float,
        heightFraction: Float
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val l = (bitmap.width * leftFraction.coerceIn(0f, 0.9f)).toInt()
        val t = (bitmap.height * topFraction.coerceIn(0f, 0.9f)).toInt()
        val maxAvailableW = bitmap.width - l
        val maxAvailableH = bitmap.height - t
        val w = (bitmap.width * widthFraction.coerceIn(0.05f, 1f)).toInt().coerceIn(1, maxAvailableW)
        val h = (bitmap.height * heightFraction.coerceIn(0.05f, 1f)).toInt().coerceIn(1, maxAvailableH)

        val cropped = Bitmap.createBitmap(bitmap, l, t, w, h)
        val outFile = FileUtils.getTempFile(context, "cropped_", ".jpg")

        FileOutputStream(outFile).use { fos ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = cropped.width,
            height = cropped.height
        )
    }

    suspend fun addWatermark(
        context: Context,
        uri: Uri,
        watermarkText: String,
        position: String = "BOTTOM_RIGHT",
        alpha: Int = 180
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val textSize = (mutableBitmap.width / 22f).coerceIn(24f, 90f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.alpha = alpha.coerceIn(30, 255)
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
        }

        val textWidth = paint.measureText(watermarkText)
        val padding = 36f

        val (x, y) = when (position) {
            "TOP_LEFT" -> Pair(padding, padding + textSize)
            "TOP_RIGHT" -> Pair(mutableBitmap.width - textWidth - padding, padding + textSize)
            "CENTER" -> Pair((mutableBitmap.width - textWidth) / 2f, (mutableBitmap.height + textSize) / 2f)
            else -> Pair(mutableBitmap.width - textWidth - padding, mutableBitmap.height - padding)
        }

        canvas.drawText(watermarkText, x.coerceAtLeast(10f), y.coerceAtLeast(textSize), paint)

        val outFile = FileUtils.getTempFile(context, "watermarked_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = mutableBitmap.width,
            height = mutableBitmap.height
        )
    }

    suspend fun textOverlay(
        context: Context,
        uri: Uri,
        text: String,
        fontSizeSp: Float = 36f,
        textColor: Int = Color.WHITE,
        position: String = "BOTTOM",
        hasBackground: Boolean = true
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = (mutableBitmap.width * (fontSizeSp / 400f)).coerceIn(24f, 120f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textHeight = textBounds.height()

        val yPos = when (position) {
            "TOP" -> (textHeight + 60f)
            "CENTER" -> (mutableBitmap.height / 2f + textHeight / 2f)
            else -> (mutableBitmap.height - 60f)
        }

        if (hasBackground) {
            val bgPaint = Paint().apply {
                color = Color.BLACK
                alpha = 150
            }
            val rectTop = yPos - textHeight - 24f
            val rectBottom = yPos + 24f
            canvas.drawRect(0f, rectTop, mutableBitmap.width.toFloat(), rectBottom, bgPaint)
        }

        canvas.drawText(text, mutableBitmap.width / 2f, yPos, paint)

        val outFile = FileUtils.getTempFile(context, "text_overlay_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = mutableBitmap.width,
            height = mutableBitmap.height
        )
    }

    suspend fun stripExif(
        context: Context,
        uri: Uri,
        outputFormat: String = "JPEG",
        quality: Int = 98
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val originalSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")

        val ext = when (outputFormat.uppercase()) {
            "PNG" -> ".png"
            "WEBP" -> ".webp"
            else -> ".jpg"
        }
        val compressFormat = when (outputFormat.uppercase()) {
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.JPEG
            else -> Bitmap.CompressFormat.JPEG
        }

        // Freshly write bitmap without writing any EXIF or AI metadata tags
        val outFile = FileUtils.getTempFile(context, "privacy_clean_", ext)
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(compressFormat, quality.coerceIn(50, 100), fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = originalSize,
            newSize = outFile.length(),
            width = bitmap.width,
            height = bitmap.height
        )
    }

    suspend fun getExifData(context: Context, uri: Uri): Map<String, String> = withContext(Dispatchers.IO) {
        val tags = mutableMapOf<String, String>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                fun putIf(key: String, tag: String) {
                    val value = exif.getAttribute(tag)
                    if (!value.isNullOrBlank()) tags[key] = value
                }

                putIf("Camera Make", ExifInterface.TAG_MAKE)
                putIf("Camera Model", ExifInterface.TAG_MODEL)
                putIf("Software / AI Engine", ExifInterface.TAG_SOFTWARE)
                putIf("AI Prompt / Description", ExifInterface.TAG_IMAGE_DESCRIPTION)
                putIf("Generation Parameters", ExifInterface.TAG_USER_COMMENT)
                putIf("Creator / Artist", ExifInterface.TAG_ARTIST)
                putIf("Copyright / License", ExifInterface.TAG_COPYRIGHT)
                putIf("Date / Time", ExifInterface.TAG_DATETIME)
                putIf("ISO Speed", ExifInterface.TAG_ISO_SPEED_RATINGS)
                putIf("Aperture (F-Stop)", ExifInterface.TAG_F_NUMBER)
                putIf("Exposure Time", ExifInterface.TAG_EXPOSURE_TIME)
                putIf("Focal Length", ExifInterface.TAG_FOCAL_LENGTH)
                putIf("Flash", ExifInterface.TAG_FLASH)
                putIf("White Balance", ExifInterface.TAG_WHITE_BALANCE)
                putIf("Orientation", ExifInterface.TAG_ORIENTATION)
                putIf("Image Width", ExifInterface.TAG_IMAGE_WIDTH)
                putIf("Image Height", ExifInterface.TAG_IMAGE_LENGTH)

                val latLong = FloatArray(2)
                if (exif.getLatLong(latLong)) {
                    tags["GPS Coordinates"] = "${latLong[0]}, ${latLong[1]}"
                }
            }
        } catch (_: Exception) {}

        if (tags.isEmpty()) {
            tags["Privacy Status"] = "No EXIF or metadata tags found (Privacy Clean)"
        }
        tags
    }

    suspend fun extractPalette(context: Context, uri: Uri, count: Int = 6): List<String> = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(context, uri) ?: return@withContext listOf("#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#6366F1", "#14B8A6")
        val small = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val pixels = IntArray(64 * 64)
        small.getPixels(pixels, 0, 64, 0, 0, 64, 64)

        // Cluster colors by quantizing RGB to 4 bits
        val colorCounts = mutableMapOf<Int, Int>()
        for (color in pixels) {
            val r = (Color.red(color) shr 4) shl 4
            val g = (Color.green(color) shr 4) shl 4
            val b = (Color.blue(color) shr 4) shl 4
            val key = Color.rgb(r, g, b)
            colorCounts[key] = (colorCounts[key] ?: 0) + 1
        }

        val topColors = colorCounts.entries
            .sortedByDescending { it.value }
            .take(count)
            .map { entry ->
                String.format("#%06X", (0xFFFFFF and entry.key))
            }

        topColors.ifEmpty { listOf("#2563EB", "#7C3AED", "#DB2777", "#F59E0B", "#10B981") }
    }

    suspend fun getDominantColor(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val palette = extractPalette(context, uri, 1)
        palette.firstOrNull() ?: "#3B82F6"
    }

    suspend fun createCollage(context: Context, uris: List<Uri>, columns: Int = 2): ProcessedImageResult = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) throw IllegalArgumentException("Select at least 2 images for collage")

        val bitmaps = uris.take(9).mapNotNull { loadBitmap(context, it) }
        if (bitmaps.isEmpty()) throw IllegalStateException("Could not load selected images")

        val cols = columns.coerceIn(2, 3)
        val rows = (bitmaps.size + cols - 1) / cols

        val cellW = 600
        val cellH = 600
        val padding = 16

        val totalW = (cols * cellW) + ((cols + 1) * padding)
        val totalH = (rows * cellH) + ((rows + 1) * padding)

        val collage = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)
        canvas.drawColor(Color.WHITE)

        bitmaps.forEachIndexed { index, bmp ->
            val col = index % cols
            val row = index / cols

            val left = padding + (col * (cellW + padding))
            val top = padding + (row * (cellH + padding))

            // Center-crop to cell
            val scale = maxOf(cellW.toFloat() / bmp.width, cellH.toFloat() / bmp.height)
            val sw = (bmp.width * scale).toInt()
            val sh = (bmp.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(bmp, sw, sh, true)

            val xOff = (sw - cellW) / 2
            val yOff = (sh - cellH) / 2
            val cellBmp = Bitmap.createBitmap(scaled, xOff, yOff, cellW, cellH)

            canvas.drawBitmap(cellBmp, left.toFloat(), top.toFloat(), null)
        }

        val outFile = FileUtils.getTempFile(context, "collage_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            collage.compress(Bitmap.CompressFormat.JPEG, 92, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = 0L,
            newSize = outFile.length(),
            width = totalW,
            height = totalH
        )
    }

    suspend fun createContactSheet(context: Context, uris: List<Uri>): ProcessedImageResult = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) throw IllegalArgumentException("Select images for contact sheet")

        val bitmaps = uris.take(12).mapNotNull { loadBitmap(context, it) }
        val cols = 3
        val rows = (bitmaps.size + cols - 1) / cols

        val thumbSize = 380
        val pad = 24
        val totalW = (cols * thumbSize) + ((cols + 1) * pad)
        val totalH = (rows * (thumbSize + 40)) + ((rows + 1) * pad)

        val sheet = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.parseColor("#18181B")) // Dark studio background

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }

        bitmaps.forEachIndexed { i, bmp ->
            val c = i % cols
            val r = i / cols
            val x = pad + (c * (thumbSize + pad))
            val y = pad + (r * (thumbSize + 40 + pad))

            val scale = minOf(thumbSize.toFloat() / bmp.width, thumbSize.toFloat() / bmp.height)
            val sw = (bmp.width * scale).toInt()
            val sh = (bmp.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(bmp, sw, sh, true)

            val drawX = x + (thumbSize - sw) / 2f
            val drawY = y + (thumbSize - sh) / 2f
            canvas.drawBitmap(scaled, drawX, drawY, null)
            canvas.drawText("#${i + 1} (${bmp.width}x${bmp.height})", x + thumbSize / 2f, y + thumbSize + 28f, paint)
        }

        val outFile = FileUtils.getTempFile(context, "contact_sheet_", ".jpg")
        FileOutputStream(outFile).use { fos ->
            sheet.compress(Bitmap.CompressFormat.JPEG, 92, fos)
        }

        ProcessedImageResult(
            file = outFile,
            originalSize = 0L,
            newSize = outFile.length(),
            width = totalW,
            height = totalH
        )
    }

    suspend fun detectDuplicates(context: Context, uris: List<Uri>): List<DuplicateMatch> = withContext(Dispatchers.IO) {
        if (uris.size < 2) return@withContext emptyList()

        // Compute 8x8 average perceptual hash for each image
        val hashes = uris.map { uri ->
            val bmp = loadBitmap(context, uri)
            if (bmp != null) computeAHash(bmp) else 0L
        }

        val results = mutableListOf<DuplicateMatch>()
        for (i in 0 until hashes.size) {
            for (j in i + 1 until hashes.size) {
                val dist = java.lang.Long.bitCount(hashes[i] xor hashes[j])
                val similarity = ((64 - dist) * 100) / 64
                val isDup = dist <= 10
                results.add(DuplicateMatch(i, j, similarity, isDup))
            }
        }
        results.sortedByDescending { it.similarityPercent }
    }

    private fun computeAHash(bitmap: Bitmap): Long {
        val small = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        val pixels = IntArray(64)
        small.getPixels(pixels, 0, 8, 0, 0, 8, 8)

        var totalLum = 0L
        val grays = IntArray(64)
        for (i in 0 until 64) {
            val c = pixels[i]
            val gray = (Color.red(c) * 299 + Color.green(c) * 587 + Color.blue(c) * 114) / 1000
            grays[i] = gray
            totalLum += gray
        }

        val avg = totalLum / 64
        var hash = 0L
        for (i in 0 until 64) {
            if (grays[i] >= avg) {
                hash = hash or (1L shl i)
            }
        }
        return hash
    }

    suspend fun imagesToZip(context: Context, uris: List<Uri>): File = withContext(Dispatchers.IO) {
        val outFile = FileUtils.getTempFile(context, "images_bundle_", ".zip")
        val buffer = ByteArray(8192)

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            uris.forEachIndexed { index, uri ->
                val entryName = "image_${index + 1}.jpg"
                zos.putNextEntry(ZipEntry(entryName))
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

    suspend fun imageToBase64(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not load image")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun base64ToImage(context: Context, base64Str: String): File = withContext(Dispatchers.IO) {
        val clean = base64Str.substringAfter("base64,").trim()
        val decoded = Base64.decode(clean, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            ?: throw IllegalArgumentException("Invalid Base64 image data")

        val outFile = FileUtils.getTempFile(context, "base64_image_", ".png")
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        outFile
    }

    suspend fun getImageMetadata(context: Context, uri: Uri): ImageMetadataInfo = withContext(Dispatchers.IO) {
        val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        val bitmap = loadBitmap(context, uri) ?: throw IllegalArgumentException("Could not read image")
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ratio = "${bitmap.width}:${bitmap.height}"
        ImageMetadataInfo(
            width = bitmap.width,
            height = bitmap.height,
            sizeBytes = size,
            format = mime,
            aspectRatio = ratio
        )
    }

    private fun boxBlur(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val r = radius.coerceAtLeast(1)
        val newPixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                var red = 0; var green = 0; var blue = 0; var count = 0
                for (dy in -r..r step 2) {
                    val ny = (y + dy).coerceIn(0, h - 1)
                    for (dx in -r..r step 2) {
                        val nx = (x + dx).coerceIn(0, w - 1)
                        val color = pixels[ny * w + nx]
                        red += Color.red(color)
                        green += Color.green(color)
                        blue += Color.blue(color)
                        count++
                    }
                }
                newPixels[y * w + x] = Color.rgb(red / count, green / count, blue / count)
            }
        }
        out.setPixels(newPixels, 0, w, 0, 0, w, h)
        return out
    }
}
