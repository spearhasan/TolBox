package com.example.engine.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.example.core.util.FileUtils
import com.example.engine.image.ImageEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class PdfMetadata(
    val pageCount: Int,
    val sizeBytes: Long
)

object PdfEngine {

    suspend fun imagesToPdf(
        context: Context,
        imageUris: List<Uri>,
        pageWidth: Int = 595,  // A4 standard @ 72 DPI
        pageHeight: Int = 842
    ): File = withContext(Dispatchers.IO) {
        if (imageUris.isEmpty()) throw IllegalArgumentException("Please select at least one image.")

        val document = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "document_", ".pdf")

        try {
            for ((index, uri) in imageUris.withIndex()) {
                val bitmap = ImageEngine.loadBitmap(context, uri) ?: continue
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val margin = 36f
                val availableWidth = pageWidth - (margin * 2)
                val availableHeight = pageHeight - (margin * 2)

                val scaleX = availableWidth / bitmap.width
                val scaleY = availableHeight / bitmap.height
                val scale = minOf(scaleX, scaleY)

                val scaledWidth = bitmap.width * scale
                val scaledHeight = bitmap.height * scale

                val left = margin + (availableWidth - scaledWidth) / 2
                val top = margin + (availableHeight - scaledHeight) / 2

                val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                canvas.drawBitmap(bitmap, null, destRect, null)

                document.finishPage(page)
            }

            FileOutputStream(outFile).use { fos ->
                document.writeTo(fos)
            }
        } finally {
            document.close()
        }

        outFile
    }

    suspend fun textToPdf(
        context: Context,
        title: String,
        content: String,
        pageWidth: Int = 595,
        pageHeight: Int = 842
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "text_doc_", ".pdf")

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }

        val margin = 50f
        val lineSpacing = 16f
        var currentY = margin + 30f
        var pageNum = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        if (title.isNotBlank()) {
            canvas.drawText(title, margin, currentY, titlePaint)
            currentY += 40f
        }

        val lines = content.lines()
        for (rawLine in lines) {
            val words = rawLine.split(" ")
            var lineBuf = StringBuilder()

            for (word in words) {
                val candidate = if (lineBuf.isEmpty()) word else "$lineBuf $word"
                if (textPaint.measureText(candidate) <= (pageWidth - margin * 2)) {
                    lineBuf.append(if (lineBuf.isEmpty()) word else " $word")
                } else {
                    if (currentY > pageHeight - margin) {
                        document.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = margin + 20f
                    }
                    canvas.drawText(lineBuf.toString(), margin, currentY, textPaint)
                    currentY += lineSpacing
                    lineBuf = StringBuilder(word)
                }
            }

            if (lineBuf.isNotEmpty()) {
                if (currentY > pageHeight - margin) {
                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = margin + 20f
                }
                canvas.drawText(lineBuf.toString(), margin, currentY, textPaint)
                currentY += lineSpacing
            }
        }

        document.finishPage(page)
        FileOutputStream(outFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        outFile
    }

    suspend fun pdfToImages(context: Context, pdfUri: Uri, maxPages: Int = 10): List<File> = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IllegalArgumentException("Could not read PDF file")

        val renderer = PdfRenderer(pfd)
        val images = mutableListOf<File>()

        try {
            val pagesToRender = minOf(renderer.pageCount, maxPages)
            for (i in 0 until pagesToRender) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val outFile = FileUtils.getTempFile(context, "pdf_page_${i + 1}_", ".png")
                FileOutputStream(outFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                images.add(outFile)
            }
        } finally {
            renderer.close()
            pfd.close()
        }

        images
    }

    suspend fun mergePdfs(context: Context, uris: List<Uri>): File = withContext(Dispatchers.IO) {
        if (uris.size < 2) throw IllegalArgumentException("Select at least 2 PDF files to merge.")
        val newDoc = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "merged_pdf_", ".pdf")
        var pageCounter = 1

        try {
            for (uri in uris) {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: continue
                val renderer = PdfRenderer(pfd)
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pageCounter++).create()
                    val newPage = newDoc.startPage(pageInfo)
                    newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    newDoc.finishPage(newPage)
                }
                renderer.close()
                pfd.close()
            }

            FileOutputStream(outFile).use { fos ->
                newDoc.writeTo(fos)
            }
        } finally {
            newDoc.close()
        }
        outFile
    }

    suspend fun splitPdf(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("Could not open PDF")
        val renderer = PdfRenderer(pfd)
        val totalPages = renderer.pageCount

        val doc1 = PdfDocument()
        val doc2 = PdfDocument()
        val mid = (totalPages / 2).coerceAtLeast(1)

        for (i in 0 until totalPages) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val targetDoc = if (i < mid) doc1 else doc2
            val pageNum = if (i < mid) (i + 1) else (i - mid + 1)
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pageNum).create()
            val newPage = targetDoc.startPage(pageInfo)
            newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
            targetDoc.finishPage(newPage)
        }
        renderer.close()
        pfd.close()

        val f1 = FileUtils.getTempFile(context, "part_1_", ".pdf")
        val f2 = FileUtils.getTempFile(context, "part_2_", ".pdf")
        FileOutputStream(f1).use { doc1.writeTo(it) }
        FileOutputStream(f2).use { doc2.writeTo(it) }
        doc1.close()
        doc2.close()

        val zip = FileUtils.getTempFile(context, "split_pdf_parts_", ".zip")
        ZipOutputStream(FileOutputStream(zip)).use { zos ->
            zos.putNextEntry(ZipEntry("part_1.pdf"))
            f1.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("part_2.pdf"))
            f2.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
        }
        f1.delete()
        f2.delete()
        zip
    }

    suspend fun rotatePdf(context: Context, uri: Uri, degrees: Float = 90f): File = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalArgumentException("Could not open PDF")
        val renderer = PdfRenderer(pfd)
        val newDoc = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "rotated_pdf_", ".pdf")

        try {
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val matrix = Matrix().apply { postRotate(degrees) }
                val rotatedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                val pageInfo = PdfDocument.PageInfo.Builder(rotatedBmp.width, rotatedBmp.height, i + 1).create()
                val newPage = newDoc.startPage(pageInfo)
                newPage.canvas.drawBitmap(rotatedBmp, 0f, 0f, null)
                newDoc.finishPage(newPage)
            }
            FileOutputStream(outFile).use { fos -> newDoc.writeTo(fos) }
        } finally {
            newDoc.close()
            renderer.close()
            pfd.close()
        }
        outFile
    }

    suspend fun compressPdf(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalArgumentException("Could not open PDF")
        val renderer = PdfRenderer(pfd)
        val newDoc = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "compressed_pdf_", ".pdf")

        try {
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val w = (page.width * 0.75f).toInt().coerceAtLeast(100)
                val h = (page.height * 0.75f).toInt().coerceAtLeast(100)
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                val newPage = newDoc.startPage(pageInfo)
                newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                newDoc.finishPage(newPage)
            }
            FileOutputStream(outFile).use { fos -> newDoc.writeTo(fos) }
        } finally {
            newDoc.close()
            renderer.close()
            pfd.close()
        }
        outFile
    }

    suspend fun getPdfMetadata(context: Context, pdfUri: Uri): PdfMetadata = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: return@withContext PdfMetadata(0, 0L)
        val size = pfd.statSize
        val renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount
        renderer.close()
        pfd.close()
        PdfMetadata(pageCount, size)
    }

    suspend fun extractPages(context: Context, uri: Uri, startPage: Int, endPage: Int): File = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalArgumentException("Could not open PDF")
        val renderer = PdfRenderer(pfd)
        val newDoc = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "extracted_pages_", ".pdf")

        val s = (startPage - 1).coerceIn(0, renderer.pageCount - 1)
        val e = (endPage - 1).coerceIn(s, renderer.pageCount - 1)
        var outPageIndex = 1

        try {
            for (i in s..e) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, outPageIndex++).create()
                val newPage = newDoc.startPage(pageInfo)
                newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                newDoc.finishPage(newPage)
            }
            FileOutputStream(outFile).use { fos -> newDoc.writeTo(fos) }
        } finally {
            newDoc.close()
            renderer.close()
            pfd.close()
        }
        outFile
    }

    suspend fun deletePages(context: Context, uri: Uri, pagesToDelete: Set<Int>): File = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalArgumentException("Could not open PDF")
        val renderer = PdfRenderer(pfd)
        val newDoc = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "pages_removed_", ".pdf")
        var outPageIndex = 1

        try {
            for (i in 0 until renderer.pageCount) {
                val pageNumber = i + 1
                if (pagesToDelete.contains(pageNumber)) continue

                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, outPageIndex++).create()
                val newPage = newDoc.startPage(pageInfo)
                newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                newDoc.finishPage(newPage)
            }
            FileOutputStream(outFile).use { fos -> newDoc.writeTo(fos) }
        } finally {
            newDoc.close()
            renderer.close()
            pfd.close()
        }
        outFile
    }

    suspend fun addWatermark(context: Context, uri: Uri, watermarkText: String): File = withContext(Dispatchers.IO) {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalArgumentException("Could not open PDF")
        val renderer = PdfRenderer(pfd)
        val newDoc = PdfDocument()
        val outFile = FileUtils.getTempFile(context, "watermarked_pdf_", ".pdf")

        val paint = Paint().apply {
            color = Color.parseColor("#40FF0000") // 25% transparent red watermark
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        try {
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, i + 1).create()
                val newPage = newDoc.startPage(pageInfo)
                val canvas = newPage.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                // Draw diagonal watermark across center of page
                canvas.save()
                canvas.rotate(-45f, bitmap.width / 2f, bitmap.height / 2f)
                canvas.drawText(watermarkText.ifBlank { "CONFIDENTIAL" }, bitmap.width / 2f, bitmap.height / 2f, paint)
                canvas.restore()

                newDoc.finishPage(newPage)
            }
            FileOutputStream(outFile).use { fos -> newDoc.writeTo(fos) }
        } finally {
            newDoc.close()
            renderer.close()
            pfd.close()
        }
        outFile
    }

    suspend fun pdfToImagesZip(context: Context, uri: Uri, format: String = "PNG"): File = withContext(Dispatchers.IO) {
        val images = pdfToImages(context, uri, maxPages = 20)
        val zipFile = FileUtils.getTempFile(context, "pdf_images_", ".zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            images.forEachIndexed { idx, file ->
                val ext = if (format.uppercase() == "JPG" || format.uppercase() == "JPEG") ".jpg" else ".png"
                zos.putNextEntry(ZipEntry("page_${idx + 1}$ext"))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
                file.delete()
            }
        }
        zipFile
    }
}
