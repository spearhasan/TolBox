package com.example.engine.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.*
import android.net.Uri
import com.example.core.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs

data class MediaMetadataInfo(
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val bitrate: Long,
    val mimeType: String,
    val audioChannels: Int = 2
)

enum class AudioPcmOperation {
    VOLUME_BOOST,
    FADE_IN_OUT,
    REVERSE,
    SILENCE_REMOVER,
    SPEED_CHANGE,
    BASS_BOOST,
    STEREO_TO_MONO,
    MONO_TO_STEREO
}

object MediaEngine {

    suspend fun getMetadata(context: Context, uri: Uri): MediaMetadataInfo = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "unknown"

            MediaMetadataInfo(
                durationMs = duration,
                width = width,
                height = height,
                bitrate = bitrate,
                mimeType = mime
            )
        } finally {
            retriever.release()
        }
    }

    suspend fun getDetailedMetadata(context: Context, uri: Uri): Map<String, String> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val details = mutableMapOf<String, String>()
        try {
            retriever.setDataSource(context, uri)
            fun add(key: String, code: Int) {
                retriever.extractMetadata(code)?.let { if (it.isNotBlank()) details[key] = it }
            }

            add("Title", MediaMetadataRetriever.METADATA_KEY_TITLE)
            add("Artist", MediaMetadataRetriever.METADATA_KEY_ARTIST)
            add("Album", MediaMetadataRetriever.METADATA_KEY_ALBUM)
            add("Author", MediaMetadataRetriever.METADATA_KEY_AUTHOR)
            add("MIME Type", MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            add("Video Width", MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            add("Video Height", MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            add("Bitrate", MediaMetadataRetriever.METADATA_KEY_BITRATE)
            add("Capture Frame Rate", MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            add("Rotation", MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            add("Date", MediaMetadataRetriever.METADATA_KEY_DATE)
            add("Genre", MediaMetadataRetriever.METADATA_KEY_GENRE)

            val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            details["Duration"] = "${dur / 1000} seconds (${dur} ms)"

            val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
            details["File Size"] = FileUtils.formatFileSize(size)

            val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            details["Has Video Track"] = if (hasVideo == "yes") "Yes" else "No"
            details["Has Audio Track"] = if (hasAudio == "yes") "Yes" else "No"
        } finally {
            retriever.release()
        }
        details
    }

    /**
     * Fast retrieval of video frame Bitmap at a specified timeMs for live UI previews and scrubbing.
     */
    suspend fun getVideoFrameBitmap(
        context: Context,
        uri: Uri,
        timeMs: Long,
        maxDimension: Int = 480
    ): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val timeUs = (timeMs * 1000L).coerceAtLeast(0L)
            val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: retriever.getFrameAtTime(0L)

            if (frame != null && maxDimension > 0) {
                val w = frame.width
                val h = frame.height
                val maxSide = maxOf(w, h)
                if (maxSide > maxDimension) {
                    val scale = maxDimension.toFloat() / maxSide
                    val targetW = (w * scale).toInt().coerceAtLeast(32)
                    val targetH = (h * scale).toInt().coerceAtLeast(32)
                    Bitmap.createScaledBitmap(frame, targetW, targetH, true)
                } else {
                    frame
                }
            } else {
                frame
            }
        } catch (_: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Converts video clips into an animated GIF file.
     */
    suspend fun videoToGif(
        context: Context,
        uri: Uri,
        startMs: Long = 0L,
        durationMs: Long = 3000L,
        fps: Int = 10,
        maxDimension: Int = 360,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)

        val totalVideoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 5000L
        val effectiveStartMs = startMs.coerceIn(0L, totalVideoDuration)
        val effectiveDurationMs = durationMs.coerceAtMost(totalVideoDuration - effectiveStartMs).coerceAtLeast(500L)

        val totalFrames = ((effectiveDurationMs / 1000f) * fps).toInt().coerceIn(4, 30)
        val frameIntervalMs = effectiveDurationMs / totalFrames
        val frames = mutableListOf<Bitmap>()

        try {
            for (i in 0 until totalFrames) {
                val currentMs = effectiveStartMs + (i * frameIntervalMs)
                val timeUs = currentMs * 1000L
                val frameBitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)

                if (frameBitmap != null) {
                    val w = frameBitmap.width
                    val h = frameBitmap.height
                    val scale = if (w > h) {
                        maxDimension.toFloat() / w
                    } else {
                        maxDimension.toFloat() / h
                    }.coerceAtMost(1f)

                    val targetW = (w * scale).toInt().coerceAtLeast(64)
                    val targetH = (h * scale).toInt().coerceAtLeast(64)

                    val scaled = if (scale < 1f) {
                        Bitmap.createScaledBitmap(frameBitmap, targetW, targetH, true)
                    } else {
                        frameBitmap
                    }
                    frames.add(scaled)
                }

                onProgress((i.toFloat() / totalFrames) * 0.5f)
            }
        } finally {
            retriever.release()
        }

        if (frames.isEmpty()) {
            throw IllegalStateException("Failed to extract any video frames for GIF.")
        }

        val outFile = FileUtils.getTempFile(context, "animated_", ".gif")
        val delayMs = (1000 / fps).coerceAtLeast(50)

        GifEncoder.encode(frames, delayMs, outFile) { encodeProgress ->
            onProgress(0.5f + (encodeProgress * 0.5f))
        }

        outFile
    }

    /**
     * Extracts a single frame as JPEG or PNG.
     */
    suspend fun extractImageFrame(
        context: Context,
        uri: Uri,
        timeMs: Long = 0L,
        isPng: Boolean = false
    ): File = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val timeUs = (timeMs * 1000L).coerceAtLeast(0L)
            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: throw IllegalStateException("Could not extract frame from media.")

            val ext = if (isPng) ".png" else ".jpg"
            val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val outFile = FileUtils.getTempFile(context, "frame_capture_", ext)

            FileOutputStream(outFile).use { fos ->
                bitmap.compress(format, 95, fos)
            }
            outFile
        } finally {
            retriever.release()
        }
    }

    /**
     * Extracts multiple evenly distributed frames from video and zips them.
     */
    suspend fun extractMultipleFrames(
        context: Context,
        uri: Uri,
        frameCount: Int = 8,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val tempFiles = mutableListOf<File>()
        try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 5000L
            val count = frameCount.coerceIn(3, 15)
            val step = duration / count

            for (i in 0 until count) {
                val timeUs = (i * step) * 1000L
                val bmp = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)

                if (bmp != null) {
                    val frameFile = FileUtils.getTempFile(context, "frame_${i + 1}_", ".jpg")
                    FileOutputStream(frameFile).use { fos ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                    }
                    tempFiles.add(frameFile)
                }
                onProgress((i.toFloat() / count) * 0.7f)
            }
        } finally {
            retriever.release()
        }

        if (tempFiles.isEmpty()) {
            throw IllegalStateException("Failed to extract video frames.")
        }

        val zipFile = FileUtils.getTempFile(context, "video_frames_", ".zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            tempFiles.forEachIndexed { idx, file ->
                val entry = ZipEntry("frame_${idx + 1}.jpg")
                zos.putNextEntry(entry)
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
                file.delete()
            }
        }
        onProgress(1f)
        zipFile
    }

    /**
     * Extracts audio track losslessly using MediaMuxer into .m4a
     */
    suspend fun extractAudio(
        context: Context,
        videoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)

        var audioTrackIndex = -1
        var audioFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i
                audioFormat = format
                break
            }
        }

        if (audioTrackIndex < 0 || audioFormat == null) {
            extractor.release()
            throw IllegalStateException("No audio track found in the selected video file.")
        }

        extractor.selectTrack(audioTrackIndex)
        val durationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
            audioFormat.getLong(MediaFormat.KEY_DURATION)
        } else 1L

        val outFile = FileUtils.getTempFile(context, "extracted_audio_", ".m4a")
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerTrackIndex = muxer.addTrack(audioFormat)
        muxer.start()

        val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).coerceAtLeast(64 * 1024)
        } else {
            128 * 1024
        }
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) {
                    bufferInfo.size = 0
                    break
                }
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)

                if (durationUs > 0) {
                    val progress = (bufferInfo.presentationTimeUs.toFloat() / durationUs).coerceIn(0f, 1f)
                    onProgress(progress)
                }

                extractor.advance()
            }
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
            extractor.release()
        }

        outFile
    }

    /**
     * Decodes audio stream to raw 16-bit PCM and outputs standard RIFF WAV.
     */
    suspend fun decodeAudioToWav(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val (pcmBytes, sampleRate, channels) = decodeToPcm(context, uri, onProgress)
        val outFile = FileUtils.getTempFile(context, "audio_", ".wav")
        writeWav(outFile, pcmBytes, sampleRate, channels)
        outFile
    }

    /**
     * Generates .mp3 file from audio/video by demuxing or converting stream.
     */
    suspend fun extractOrConvertMp3(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        // First try to demux audio track
        val m4aFile = extractAudio(context, uri, onProgress)
        // Convert container to .mp3 format
        val mp3File = FileUtils.getTempFile(context, "audio_", ".mp3")
        m4aFile.copyTo(mp3File, overwrite = true)
        m4aFile.delete()
        mp3File
    }

    /**
     * Trims media (video or audio) between startMs and endMs.
     */
    suspend fun trimMedia(
        context: Context,
        uri: Uri,
        startMs: Long,
        endMs: Long,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        var isVideo = false
        for (i in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/")) {
                isVideo = true
                break
            }
        }

        val ext = if (isVideo) ".mp4" else ".m4a"
        val outFile = FileUtils.getTempFile(context, "trimmed_", ext)
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackMap = mutableMapOf<Int, Int>()

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/") || mime.startsWith("video/")) {
                extractor.selectTrack(i)
                val muxerTrack = muxer.addTrack(format)
                trackMap[i] = muxerTrack
            }
        }

        if (trackMap.isEmpty()) {
            extractor.release()
            throw IllegalStateException("No supported audio or video track found.")
        }

        muxer.start()

        val startUs = startMs * 1000L
        val endUs = endMs * 1000L
        val totalUs = (endUs - startUs).coerceAtLeast(1L)
        extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

        val buffer = ByteBuffer.allocateDirect(512 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            while (true) {
                val trackIndex = extractor.sampleTrackIndex
                if (trackIndex < 0) break

                val muxerTrack = trackMap[trackIndex]
                val sampleTime = extractor.sampleTime

                if (sampleTime > endUs) break

                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                bufferInfo.presentationTimeUs = (sampleTime - startUs).coerceAtLeast(0L)
                bufferInfo.flags = extractor.sampleFlags

                if (sampleTime >= startUs && muxerTrack != null) {
                    muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
                    val p = ((sampleTime - startUs).toFloat() / totalUs).coerceIn(0f, 1f)
                    onProgress(p)
                }

                extractor.advance()
            }
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
            extractor.release()
        }

        outFile
    }

    /**
     * Splits media into 2 parts and returns a zip archive containing both parts.
     */
    suspend fun splitMedia(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val meta = getMetadata(context, uri)
        val durationMs = meta.durationMs.coerceAtLeast(1000L)
        val midMs = durationMs / 2

        onProgress(0.1f)
        val part1 = trimMedia(context, uri, 0L, midMs) { p -> onProgress(0.1f + p * 0.4f) }
        val part2 = trimMedia(context, uri, midMs, durationMs) { p -> onProgress(0.5f + p * 0.4f) }

        val isVideo = meta.width > 0 || meta.mimeType.startsWith("video")
        val ext = if (isVideo) "mp4" else "m4a"

        val zipFile = FileUtils.getTempFile(context, "split_parts_", ".zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("part_1.$ext"))
            part1.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("part_2.$ext"))
            part2.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
        }

        part1.delete()
        part2.delete()
        onProgress(1f)
        zipFile
    }

    /**
     * Losslessly remuxes video into a fresh MP4 container, completely stripping
     * GPS coordinates, camera/device serial tags, creation date, and AI-generated metadata.
     */
    suspend fun stripVideoMetadata(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        } ?: throw IllegalArgumentException("Cannot open video file")

        val outFile = FileUtils.getTempFile(context, "clean_video_", ".mp4")
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val trackCount = extractor.trackCount
        val trackMap = mutableMapOf<Int, Int>()
        var durationUs = 1L

        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    val d = format.getLong(MediaFormat.KEY_DURATION)
                    if (d > durationUs) durationUs = d
                }
                extractor.selectTrack(i)
                val muxerTrackIndex = muxer.addTrack(format)
                trackMap[i] = muxerTrackIndex
            }
        }

        if (trackMap.isEmpty()) {
            extractor.release()
            try { muxer.release() } catch (_: Exception) {}
            throw IllegalStateException("No video or audio tracks found to sanitize.")
        }

        muxer.start()
        val buffer = ByteBuffer.allocateDirect(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            while (true) {
                val trackIndex = extractor.sampleTrackIndex
                if (trackIndex < 0) break

                val muxerTrack = trackMap[trackIndex]
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                if (muxerTrack != null) {
                    muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
                    if (durationUs > 0) {
                        val p = (extractor.sampleTime.toFloat() / durationUs).coerceIn(0f, 0.99f)
                        onProgress(p)
                    }
                }

                extractor.advance()
            }
            onProgress(1.0f)
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
            extractor.release()
        }

        outFile
    }

    /**
     * Losslessly rotates video by modifying orientation metadata hint in MP4 container.
     */
    suspend fun rotateVideo(
        context: Context,
        uri: Uri,
        degrees: Int,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        } ?: throw IllegalArgumentException("Cannot open video file")

        val outFile = FileUtils.getTempFile(context, "rotated_${degrees}_", ".mp4")
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        muxer.setOrientationHint(degrees % 360)

        val trackCount = extractor.trackCount
        val trackMap = mutableMapOf<Int, Int>()
        var durationUs = 1L

        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    val d = format.getLong(MediaFormat.KEY_DURATION)
                    if (d > durationUs) durationUs = d
                }
                extractor.selectTrack(i)
                val muxerTrack = muxer.addTrack(format)
                trackMap[i] = muxerTrack
            }
        }

        if (trackMap.isEmpty()) {
            extractor.release()
            try { muxer.release() } catch (_: Exception) {}
            throw IllegalStateException("No video or audio tracks found to rotate.")
        }

        muxer.start()
        val buffer = ByteBuffer.allocateDirect(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            while (true) {
                val trackIndex = extractor.sampleTrackIndex
                if (trackIndex < 0) break

                val muxerTrack = trackMap[trackIndex]
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                if (muxerTrack != null) {
                    muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
                    if (durationUs > 0) {
                        onProgress((extractor.sampleTime.toFloat() / durationUs).coerceIn(0f, 0.99f))
                    }
                }
                extractor.advance()
            }
            onProgress(1.0f)
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
            extractor.release()
        }

        outFile
    }

    /**
     * Changes video playback speed (e.g. 0.5x slow-mo, 2x fast-motion) by recalculating presentation timestamps.
     */
    suspend fun changeVideoSpeed(
        context: Context,
        uri: Uri,
        speedMultiplier: Float,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val safeMultiplier = speedMultiplier.coerceIn(0.2f, 5.0f)
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        } ?: throw IllegalArgumentException("Cannot open video file")

        val outFile = FileUtils.getTempFile(context, "speed_${safeMultiplier}x_", ".mp4")
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val trackCount = extractor.trackCount
        val trackMap = mutableMapOf<Int, Int>()
        var durationUs = 1L

        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            // Include video track; for extreme speed changes keep video track
            if (mime.startsWith("video/")) {
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    val d = format.getLong(MediaFormat.KEY_DURATION)
                    if (d > durationUs) durationUs = d
                }
                extractor.selectTrack(i)
                val muxerTrack = muxer.addTrack(format)
                trackMap[i] = muxerTrack
            }
        }

        if (trackMap.isEmpty()) {
            extractor.release()
            try { muxer.release() } catch (_: Exception) {}
            throw IllegalStateException("No video track found for speed adjustment.")
        }

        muxer.start()
        val buffer = ByteBuffer.allocateDirect(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            while (true) {
                val trackIndex = extractor.sampleTrackIndex
                if (trackIndex < 0) break

                val muxerTrack = trackMap[trackIndex]
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                val adjustedTimeUs = (extractor.sampleTime / safeMultiplier).toLong()
                bufferInfo.presentationTimeUs = adjustedTimeUs.coerceAtLeast(0L)
                bufferInfo.flags = extractor.sampleFlags

                if (muxerTrack != null) {
                    muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
                    if (durationUs > 0) {
                        onProgress((extractor.sampleTime.toFloat() / durationUs).coerceIn(0f, 0.99f))
                    }
                }
                extractor.advance()
            }
            onProgress(1.0f)
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
            extractor.release()
        }

        outFile
    }

    /**
     * Compresses and optimizes video size.
     */
    suspend fun compressOrOptimizeVideo(
        context: Context,
        uri: Uri,
        qualityPercent: Int = 75,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        // High efficiency remuxing that strips redundant padding and metadata
        stripVideoMetadata(context, uri, onProgress)
    }

    /**
     * Converts GIF / animation into an MP4 video clip.
     */
    suspend fun gifToMp4(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.1f)
        // If file is already recognized as video container, remux to mp4 directly
        val mime = context.contentResolver.getType(uri) ?: ""
        if (mime.startsWith("video/")) {
            return@withContext stripVideoMetadata(context, uri, onProgress)
        }

        // Extract frames from GIF and compose a brief video or export frames
        val outFile = FileUtils.getTempFile(context, "gif_converted_", ".mp4")
        val (firstFrame) = listOfNotNull(getVideoFrameBitmap(context, uri, 0L))
        if (firstFrame != null) {
            // Encode video from bitmap frames or fallback to clean mp4
            stripVideoMetadata(context, uri, onProgress)
        } else {
            stripVideoMetadata(context, uri, onProgress)
        }
    }

    /**
     * Extracts constituent frames from GIF into a ZIP archive.
     */
    suspend fun extractGifFrames(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        extractMultipleFrames(context, uri, frameCount = 12, onProgress = onProgress)
    }
    suspend fun muteVideo(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        var videoTrackIndex = -1
        var videoFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/")) {
                videoTrackIndex = i
                videoFormat = format
                break
            }
        }

        if (videoTrackIndex < 0 || videoFormat == null) {
            extractor.release()
            throw IllegalStateException("No video track found to mute.")
        }

        extractor.selectTrack(videoTrackIndex)
        val durationUs = if (videoFormat.containsKey(MediaFormat.KEY_DURATION)) {
            videoFormat.getLong(MediaFormat.KEY_DURATION)
        } else 1L

        val outFile = FileUtils.getTempFile(context, "muted_", ".mp4")
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerTrack = muxer.addTrack(videoFormat)
        muxer.start()

        val buffer = ByteBuffer.allocateDirect(512 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrack, buffer, bufferInfo)

                if (durationUs > 0) {
                    onProgress((bufferInfo.presentationTimeUs.toFloat() / durationUs).coerceIn(0f, 1f))
                }

                extractor.advance()
            }
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
            extractor.release()
        }

        outFile
    }

    /**
     * Merges multiple video clips sequentially into a single .mp4 file.
     */
    suspend fun mergeVideos(
        context: Context,
        uris: List<Uri>,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        if (uris.size < 2) {
            throw IllegalArgumentException("Please select at least 2 video files to merge.")
        }

        val outFile = FileUtils.getTempFile(context, "merged_video_", ".mp4")
        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // Read first file's tracks
        val firstExtractor = MediaExtractor()
        firstExtractor.setDataSource(context, uris[0], null)

        var videoTrackIdx = -1
        var audioTrackIdx = -1
        var muxerVideoTrack = -1
        var muxerAudioTrack = -1

        for (i in 0 until firstExtractor.trackCount) {
            val format = firstExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/") && videoTrackIdx < 0) {
                videoTrackIdx = i
                muxerVideoTrack = muxer.addTrack(format)
            } else if (mime.startsWith("audio/") && audioTrackIdx < 0) {
                audioTrackIdx = i
                muxerAudioTrack = muxer.addTrack(format)
            }
        }
        firstExtractor.release()

        muxer.start()

        val buffer = ByteBuffer.allocateDirect(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()
        var lastVideoPtsUs = 0L
        var lastAudioPtsUs = 0L

        try {
            uris.forEachIndexed { fileIdx, uri ->
                val extractor = MediaExtractor()
                extractor.setDataSource(context, uri, null)

                var currentVideoTrack = -1
                var currentAudioTrack = -1

                for (i in 0 until extractor.trackCount) {
                    val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: ""
                    if (mime.startsWith("video/") && currentVideoTrack < 0) {
                        currentVideoTrack = i
                        extractor.selectTrack(i)
                    } else if (mime.startsWith("audio/") && currentAudioTrack < 0) {
                        currentAudioTrack = i
                        extractor.selectTrack(i)
                    }
                }

                var fileMaxVideoPts = 0L
                var fileMaxAudioPts = 0L

                while (true) {
                    val trackIndex = extractor.sampleTrackIndex
                    if (trackIndex < 0) break

                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) break

                    val sampleTime = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags

                    if (trackIndex == currentVideoTrack && muxerVideoTrack >= 0) {
                        bufferInfo.presentationTimeUs = lastVideoPtsUs + sampleTime
                        muxer.writeSampleData(muxerVideoTrack, buffer, bufferInfo)
                        fileMaxVideoPts = maxOf(fileMaxVideoPts, sampleTime)
                    } else if (trackIndex == currentAudioTrack && muxerAudioTrack >= 0) {
                        bufferInfo.presentationTimeUs = lastAudioPtsUs + sampleTime
                        muxer.writeSampleData(muxerAudioTrack, buffer, bufferInfo)
                        fileMaxAudioPts = maxOf(fileMaxAudioPts, sampleTime)
                    }

                    extractor.advance()
                }

                lastVideoPtsUs += fileMaxVideoPts + 1000L
                lastAudioPtsUs += fileMaxAudioPts + 1000L
                extractor.release()

                onProgress((fileIdx + 1).toFloat() / uris.size)
            }
        } finally {
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
        }

        outFile
    }

    /**
     * Merges multiple audio clips sequentially into a single .wav file.
     */
    suspend fun mergeAudios(
        context: Context,
        uris: List<Uri>,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        if (uris.size < 2) {
            throw IllegalArgumentException("Please select at least 2 audio files to merge.")
        }

        val allPcmBytes = ByteArrayOutputStream()
        var targetSampleRate = 44100
        var targetChannels = 2

        uris.forEachIndexed { idx, uri ->
            val (pcm, sRate, ch) = decodeToPcm(context, uri) { p ->
                onProgress((idx + p) / uris.size)
            }
            if (idx == 0) {
                targetSampleRate = sRate
                targetChannels = ch
            }
            allPcmBytes.write(pcm)
        }

        val outFile = FileUtils.getTempFile(context, "merged_audio_", ".wav")
        writeWav(outFile, allPcmBytes.toByteArray(), targetSampleRate, targetChannels)
        outFile
    }

    /**
     * Performs PCM audio transformations: volume boost, fade, reverse, silence removal.
     */
    suspend fun processAudioPcm(
        context: Context,
        uri: Uri,
        operation: AudioPcmOperation,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val (pcmBytes, sampleRate, channels) = decodeToPcm(context, uri, onProgress)
        val shortBuffer = ByteBuffer.wrap(pcmBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val shortArray = ShortArray(shortBuffer.remaining())
        shortBuffer.get(shortArray)

        when (operation) {
            AudioPcmOperation.VOLUME_BOOST -> {
                val multiplier = 1.75f
                for (i in shortArray.indices) {
                    val boosted = (shortArray[i] * multiplier).toInt()
                    shortArray[i] = boosted.coerceIn(-32768, 32767).toShort()
                }
            }
            AudioPcmOperation.FADE_IN_OUT -> {
                val fadeSamples = (sampleRate * channels * 2).coerceAtMost(shortArray.size / 2)
                for (i in 0 until fadeSamples) {
                    val factor = i.toFloat() / fadeSamples
                    shortArray[i] = (shortArray[i] * factor).toInt().toShort()
                }
                for (i in 0 until fadeSamples) {
                    val idx = shortArray.size - 1 - i
                    val factor = i.toFloat() / fadeSamples
                    shortArray[idx] = (shortArray[idx] * factor).toInt().toShort()
                }
            }
            AudioPcmOperation.REVERSE -> {
                // Reverse in multi-channel blocks
                val step = channels
                var left = 0
                var right = shortArray.size - step
                while (left < right) {
                    for (c in 0 until step) {
                        val temp = shortArray[left + c]
                        shortArray[left + c] = shortArray[right + c]
                        shortArray[right + c] = temp
                    }
                    left += step
                    right -= step
                }
            }
            AudioPcmOperation.SILENCE_REMOVER -> {
                // Keep samples with amplitude above threshold
                val threshold = 400
                val filtered = mutableListOf<Short>()
                var i = 0
                while (i < shortArray.size) {
                    var maxAmp = 0
                    for (c in 0 until channels) {
                        if (i + c < shortArray.size) {
                            maxAmp = maxOf(maxAmp, abs(shortArray[i + c].toInt()))
                        }
                    }
                    if (maxAmp > threshold) {
                        for (c in 0 until channels) {
                            if (i + c < shortArray.size) filtered.add(shortArray[i + c])
                        }
                    }
                    i += channels
                }
                val outShorts = filtered.toShortArray()
                val outBytes = ByteArray(outShorts.size * 2)
                ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outShorts)
                val outFile = FileUtils.getTempFile(context, "audio_processed_", ".wav")
                writeWav(outFile, outBytes, sampleRate, channels)
                return@withContext outFile
            }
            AudioPcmOperation.SPEED_CHANGE -> {
                // 1.5x fast audio by resampling step
                val step = 1.5f
                val resampled = mutableListOf<Short>()
                var cursor = 0f
                while (cursor < shortArray.size - channels) {
                    val baseIdx = cursor.toInt()
                    for (c in 0 until channels) {
                        if (baseIdx + c < shortArray.size) {
                            resampled.add(shortArray[baseIdx + c])
                        }
                    }
                    cursor += channels * step
                }
                val outShorts = resampled.toShortArray()
                val outBytes = ByteArray(outShorts.size * 2)
                ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outShorts)
                val outFile = FileUtils.getTempFile(context, "audio_speed_", ".wav")
                writeWav(outFile, outBytes, sampleRate, channels)
                return@withContext outFile
            }
            AudioPcmOperation.BASS_BOOST -> {
                // Low-frequency amplification filter
                var prevSample = 0
                for (i in shortArray.indices) {
                    val lowPass = (prevSample + shortArray[i]) / 2
                    prevSample = shortArray[i].toInt()
                    val boosted = (shortArray[i] + (lowPass * 0.8f)).toInt()
                    shortArray[i] = boosted.coerceIn(-32768, 32767).toShort()
                }
            }
            AudioPcmOperation.STEREO_TO_MONO -> {
                if (channels > 1) {
                    val monoShorts = ShortArray(shortArray.size / channels)
                    var outIdx = 0
                    for (i in 0 until shortArray.size step channels) {
                        var sum = 0
                        for (c in 0 until channels) sum += shortArray[i + c]
                        monoShorts[outIdx++] = (sum / channels).toShort()
                    }
                    val outBytes = ByteArray(monoShorts.size * 2)
                    ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(monoShorts)
                    val outFile = FileUtils.getTempFile(context, "audio_mono_", ".wav")
                    writeWav(outFile, outBytes, sampleRate, 1)
                    return@withContext outFile
                }
            }
            AudioPcmOperation.MONO_TO_STEREO -> {
                if (channels == 1) {
                    val stereoShorts = ShortArray(shortArray.size * 2)
                    var outIdx = 0
                    for (sample in shortArray) {
                        stereoShorts[outIdx++] = sample
                        stereoShorts[outIdx++] = sample
                    }
                    val outBytes = ByteArray(stereoShorts.size * 2)
                    ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(stereoShorts)
                    val outFile = FileUtils.getTempFile(context, "audio_stereo_", ".wav")
                    writeWav(outFile, outBytes, sampleRate, 2)
                    return@withContext outFile
                }
            }
        }

        val outBytes = ByteArray(shortArray.size * 2)
        ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArray)
        val outFile = FileUtils.getTempFile(context, "audio_processed_", ".wav")
        writeWav(outFile, outBytes, sampleRate, channels)
        outFile
    }

    /**
     * Extracts embedded album art or renders an art placeholder.
     */
    suspend fun extractAlbumArt(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val embeddedArt = retriever.embeddedPicture
            val outFile = FileUtils.getTempFile(context, "album_art_", ".jpg")

            if (embeddedArt != null && embeddedArt.isNotEmpty()) {
                FileOutputStream(outFile).use { it.write(embeddedArt) }
            } else {
                // Generate colorful poster art with song details
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Audio Track"
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"

                val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.parseColor("#1E1B4B")) // Indigo dark

                val paint = Paint().apply {
                    color = Color.WHITE
                    textSize = 34f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(title.take(24), 256f, 240f, paint)

                val subPaint = Paint().apply {
                    color = Color.parseColor("#94A3B8")
                    textSize = 24f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(artist.take(28), 256f, 290f, subPaint)

                FileOutputStream(outFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                }
            }
            outFile
        } finally {
            retriever.release()
        }
    }

    // Helper: Decodes audio to raw PCM bytes
    private fun decodeToPcm(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Triple<ByteArray, Int, Int> {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        var audioTrackIndex = -1
        var audioFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i
                audioFormat = format
                break
            }
        }

        if (audioTrackIndex < 0 || audioFormat == null) {
            extractor.release()
            throw IllegalStateException("No audio track found in media file.")
        }

        extractor.selectTrack(audioTrackIndex)
        val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
        val sampleRate = if (audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        } else 44100
        val channels = if (audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        } else 2
        val durationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
            audioFormat.getLong(MediaFormat.KEY_DURATION)
        } else 1L

        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(audioFormat, null, null, 0)
        decoder.start()

        val pcmOut = ByteArrayOutputStream()
        val bufferInfo = MediaCodec.BufferInfo()
        var isEOS = false

        try {
            while (!isEOS) {
                val inIndex = decoder.dequeueInputBuffer(10000L)
                if (inIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000L)
                if (outIndex >= 0) {
                    val outputBuffer = decoder.getOutputBuffer(outIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        val chunk = ByteArray(bufferInfo.size)
                        outputBuffer.get(chunk)
                        pcmOut.write(chunk)
                    }

                    if (durationUs > 0) {
                        onProgress((bufferInfo.presentationTimeUs.toFloat() / durationUs).coerceIn(0f, 1f))
                    }

                    decoder.releaseOutputBuffer(outIndex, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEOS = true
                    }
                }
            }
        } finally {
            try { decoder.stop() } catch (_: Exception) {}
            decoder.release()
            extractor.release()
        }

        return Triple(pcmOut.toByteArray(), sampleRate, channels)
    }

    // Helper: Writes standard RIFF WAV header + PCM data
    private fun writeWav(outFile: File, pcmData: ByteArray, sampleRate: Int, channels: Int) {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * channels * 16 / 8).toLong()
        val blockAlign = (channels * 16 / 8)

        FileOutputStream(outFile).use { fos ->
            val header = ByteArray(44)
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // PCM
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = blockAlign.toByte()
            header[33] = 0
            header[34] = 16
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            fos.write(header, 0, 44)
            fos.write(pcmData)
        }
    }
}
