package com.example.engine.media

import android.graphics.Bitmap
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.abs

/**
 * Pure Kotlin GIF89a encoder for generating animated GIF files from Bitmaps.
 */
object GifEncoder {

    /**
     * Encodes a list of bitmaps into an animated GIF file.
     * @param frames List of Bitmap frames
     * @param delayMs Delay between frames in milliseconds (e.g. 100ms = 10fps)
     * @param outputFile Target file for writing GIF
     * @param onProgress Callback with progress from 0f to 1f
     */
    fun encode(
        frames: List<Bitmap>,
        delayMs: Int,
        outputFile: File,
        onProgress: (Float) -> Unit = {}
    ): File {
        if (frames.isEmpty()) {
            throw IllegalArgumentException("Frames list cannot be empty")
        }

        val firstFrame = frames[0]
        val width = firstFrame.width
        val height = firstFrame.height

        FileOutputStream(outputFile).use { fos ->
            BufferedOutputStream(fos).use { out ->
                // 1. Header: GIF89a
                writeString(out, "GIF89a")

                // 2. Logical Screen Descriptor
                writeShort(out, width)
                writeShort(out, height)
                // Packed fields: GCT Flag (1), 7 (256 colors), sort (0), size (7) -> 0xF7
                out.write(0xF7)
                out.write(0) // Background color index
                out.write(0) // Pixel aspect ratio

                // 3. Global Color Table (Standard 256-color palette: 6x6x6 cube + 40 grays)
                val globalPalette = generateStandardPalette()
                out.write(globalPalette)

                // 4. Netscape 2.0 Loop Extension (Infinite loop)
                out.write(0x21) // Extension Introducer
                out.write(0xFF) // Application Extension Label
                out.write(0x0B) // Block size (11 bytes)
                writeString(out, "NETSCAPE2.0")
                out.write(0x03) // Sub-block data size
                out.write(0x01) // Loop sub-block index
                writeShort(out, 0) // Loop count: 0 = infinite
                out.write(0x00) // Block Terminator

                // 5. Frames
                val totalFrames = frames.size
                val delayCentisecs = (delayMs / 10).coerceAtLeast(1)

                frames.forEachIndexed { index, frame ->
                    val scaledFrame = if (frame.width != width || frame.height != height) {
                        Bitmap.createScaledBitmap(frame, width, height, true)
                    } else {
                        frame
                    }

                    // Graphic Control Extension
                    out.write(0x21)
                    out.write(0xF9)
                    out.write(0x04) // Block size
                    out.write(0x04) // Disposal: do not dispose (0x04)
                    writeShort(out, delayCentisecs)
                    out.write(0) // Transparent color index
                    out.write(0x00) // Block Terminator

                    // Image Descriptor
                    out.write(0x2C) // Image separator
                    writeShort(out, 0) // Left
                    writeShort(out, 0) // Top
                    writeShort(out, width)
                    writeShort(out, height)
                    out.write(0x00) // Use Global Color Table

                    // Extract pixels and quantize to palette indices
                    val pixels = IntArray(width * height)
                    scaledFrame.getPixels(pixels, 0, width, 0, 0, width, height)
                    val indexedPixels = ByteArray(width * height)

                    for (i in pixels.indices) {
                        indexedPixels[i] = quantizeColor(pixels[i]).toByte()
                    }

                    // LZW Encode image data
                    writeLzw(out, indexedPixels)

                    onProgress((index + 1).toFloat() / totalFrames)
                }

                // 6. GIF Trailer
                out.write(0x3B)
                out.flush()
            }
        }

        return outputFile
    }

    private fun generateStandardPalette(): ByteArray {
        val palette = ByteArray(256 * 3)
        var idx = 0

        // 6x6x6 color cube = 216 colors
        val levels = intArrayOf(0, 51, 102, 153, 204, 255)
        for (r in levels) {
            for (g in levels) {
                for (b in levels) {
                    palette[idx++] = r.toByte()
                    palette[idx++] = g.toByte()
                    palette[idx++] = b.toByte()
                }
            }
        }

        // 40 grayscale levels from 0 to 255
        for (i in 0 until 40) {
            val gray = (i * 255 / 39)
            palette[idx++] = gray.toByte()
            palette[idx++] = gray.toByte()
            palette[idx++] = gray.toByte()
        }

        return palette
    }

    private fun quantizeColor(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF

        // Check for grayscale (R, G, B are very close)
        if (abs(r - g) <= 8 && abs(g - b) <= 8 && abs(r - b) <= 8) {
            val gray = (r + g + b) / 3
            val grayIndex = (gray * 39 / 255).coerceIn(0, 39)
            return 216 + grayIndex
        }

        val ri = (r * 5 + 127) / 255
        val gi = (g * 5 + 127) / 255
        val bi = (b * 5 + 127) / 255
        return (ri * 36 + gi * 6 + bi).coerceIn(0, 215)
    }

    private fun writeString(out: OutputStream, str: String) {
        for (ch in str) {
            out.write(ch.code)
        }
    }

    private fun writeShort(out: OutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
    }

    /**
     * GIF LZW compression algorithm
     */
    private fun writeLzw(out: OutputStream, pixels: ByteArray) {
        val initCodeSize = 8
        out.write(initCodeSize) // Minimum LZW code size

        val clearCode = 1 shl initCodeSize // 256
        val eoiCode = clearCode + 1 // 257

        var codeSize = initCodeSize + 1 // 9
        var nextCode = eoiCode + 1 // 258

        val hSize = 5003
        val hTab = IntArray(hSize) { -1 }
        val codeTab = IntArray(hSize)

        // Bit packing accumulator
        var curAccum = 0
        var curBits = 0
        val packet = ByteArray(256)
        var packetLen = 0

        fun flushPacket() {
            if (packetLen > 0) {
                out.write(packetLen)
                out.write(packet, 0, packetLen)
                packetLen = 0
            }
        }

        fun outputCode(code: Int) {
            curAccum = curAccum or (code shl curBits)
            curBits += codeSize

            while (curBits >= 8) {
                packet[packetLen++] = (curAccum and 0xFF).toByte()
                if (packetLen >= 254) {
                    flushPacket()
                }
                curAccum = curAccum shr 8
                curBits -= 8
            }

            if (nextCode >= (1 shl codeSize) && codeSize < 12) {
                codeSize++
            }
        }

        fun clearTable() {
            hTab.fill(-1)
            codeSize = initCodeSize + 1
            nextCode = eoiCode + 1
        }

        // Output initial clear code
        outputCode(clearCode)

        var prefix = pixels[0].toInt() and 0xFF

        for (i in 1 until pixels.size) {
            val c = pixels[i].toInt() and 0xFF
            val key = (prefix shl 8) or c
            var h = ((c shl 4) xor prefix) % hSize
            if (h < 0) h += hSize

            var found = false
            var step = 1
            while (hTab[h] != -1) {
                if (hTab[h] == key) {
                    prefix = codeTab[h]
                    found = true
                    break
                }
                h = (h + step) % hSize
                step += 2
            }

            if (!found) {
                outputCode(prefix)
                if (nextCode < 4096) {
                    codeTab[h] = nextCode++
                    hTab[h] = key
                } else {
                    outputCode(clearCode)
                    clearTable()
                }
                prefix = c
            }
        }

        outputCode(prefix)
        outputCode(eoiCode)

        // Flush remaining bits
        if (curBits > 0) {
            packet[packetLen++] = (curAccum and 0xFF).toByte()
        }
        flushPacket()

        // Block terminator
        out.write(0x00)
    }
}
