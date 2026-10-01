package com.example.ui.screens.tools

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.ExecutionState
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.util.FileUtils
import com.example.engine.media.AudioPcmOperation
import com.example.engine.media.MediaEngine
import com.example.engine.media.MediaMetadataInfo
import com.example.ui.components.CommonToolScreen
import com.example.ui.components.ResultFileCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun MediaToolsScreen(
    tool: ToolDefinition,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRecordHistory: (String, String) -> Unit,
    autoSaveEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isVideo = tool.category == ToolCategory.VIDEO || tool.category == ToolCategory.GIF || tool.id.startsWith("video_") || tool.id.startsWith("gif_")
    val isMultiFile = tool.id in listOf("video_merger", "video_concatenator", "audio_merger", "audio_joiner")
    val isMetadataViewer = tool.id in listOf(
        "video_metadata_viewer", "video_codec_info", "video_framerate_analyzer",
        "video_audio_track_viewer", "video_subtitle_track_viewer", "video_color_info",
        "video_duration_calc", "video_size_estimator", "audio_metadata_viewer",
        "audio_duration_calc", "audio_filesize_calc", "audio_frequency_analyzer",
        "audio_spectrum_analyzer", "audio_channel_analyzer", "gif_metadata_viewer"
    )

    val pickerMime = when {
        tool.id == "gif_video_to_gif" -> "video/*"
        tool.category == ToolCategory.GIF || tool.id.startsWith("gif_") -> "*/*"
        isVideo -> "video/*"
        else -> "audio/*"
    }

    // Single file state
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaMetadata by remember { mutableStateOf<MediaMetadataInfo?>(null) }
    var detailedMetadata by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Multi-file state
    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var executionState by remember { mutableStateOf<ExecutionState<Any>>(ExecutionState.Idle) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var savedLocationPath by remember { mutableStateOf<String?>(null) }

    // Live frame preview state for video tools
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isPreviewLoading by remember { mutableStateOf(false) }
    var trimPreviewTab by remember { mutableIntStateOf(0) } // 0 = start cut, 1 = end cut

    // Trimmer values
    var startSec by remember { mutableFloatStateOf(0f) }
    var endSec by remember { mutableFloatStateOf(30f) }
    var maxDurationSec by remember { mutableFloatStateOf(60f) }

    // Video Rotator state
    var videoRotationDegrees by remember { mutableIntStateOf(90) }

    // Video Speed state
    var videoSpeedMultiplier by remember {
        mutableFloatStateOf(
            if (tool.id.contains("slow")) 0.5f
            else if (tool.id.contains("fast")) 2.0f
            else 1.5f
        )
    }

    // Video Compressor state
    var videoQualityPercent by remember { mutableIntStateOf(75) }

    // GIF Settings
    var gifDurationSec by remember { mutableFloatStateOf(3f) }
    var gifFps by remember { mutableIntStateOf(10) }

    // Synchronize frame preview when video position or scrub changes
    val activePreviewTimestampSec = if (tool.id.contains("trim") || tool.id.contains("cut")) {
        if (trimPreviewTab == 0) startSec else endSec
    } else {
        startSec
    }

    LaunchedEffect(selectedMediaUri, activePreviewTimestampSec, isVideo) {
        val uri = selectedMediaUri
        if (uri != null && isVideo) {
            isPreviewLoading = true
            val timeMs = (activePreviewTimestampSec * 1000L).toLong()
            val bmp = MediaEngine.getVideoFrameBitmap(context, uri, timeMs, maxDimension = 480)
            previewBitmap = bmp
            isPreviewLoading = false
        } else {
            previewBitmap = null
        }
    }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedMediaUri = uri
        outputFile = null
        savedLocationPath = null
        executionState = ExecutionState.Idle
        previewBitmap = null
        if (uri != null) {
            scope.launch {
                try {
                    val meta = MediaEngine.getMetadata(context, uri)
                    mediaMetadata = meta
                    val totalSec = (meta.durationMs / 1000f).coerceAtLeast(1f)
                    maxDurationSec = totalSec
                    startSec = 0f
                    endSec = minOf(30f, totalSec)
                    gifDurationSec = minOf(4f, totalSec)

                    if (isMetadataViewer) {
                        detailedMetadata = MediaEngine.getDetailedMetadata(context, uri)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedMediaUris = uris
            outputFile = null
            savedLocationPath = null
            executionState = ExecutionState.Idle
        }
    }

    val actionButtonTitle = when {
        isMultiFile -> "Merge ${selectedMediaUris.size} Selected Files"
        isMetadataViewer -> "Export Metadata Report (.txt)"
        tool.id in listOf("video_metadata_cleaner", "video_clean_metadata") || (tool.id.contains("metadata") && !isMetadataViewer && isVideo) -> "Strip Video Metadata & AI Tags (.mp4)"
        tool.id in listOf("audio_metadata_cleaner", "audio_clean_metadata") || (tool.id.contains("metadata") && !isMetadataViewer && !isVideo) -> "Strip Audio ID3 & Tags (.wav)"
        tool.id.contains("rotat") -> "Rotate Video ($videoRotationDegrees°)"
        tool.id.contains("speed") || tool.id.contains("slow") || tool.id.contains("fast") -> {
            if (isVideo) "Change Video Speed (${videoSpeedMultiplier}x)" else "Change Audio Speed"
        }
        tool.id.contains("compress") || tool.id.contains("bitrate") -> {
            if (isVideo) "Compress Video (${videoQualityPercent}%)" else "Compress Audio (.mp3)"
        }
        tool.id in listOf("gif_to_video", "gif_to_mp4") -> "Convert GIF to MP4 Video"
        tool.id in listOf("gif_to_png", "gif_to_jpg") -> "Extract GIF Still Frames (.jpg)"
        tool.id in listOf("video_to_gif", "gif_video_to_gif") -> "Generate Animated GIF"
        tool.id in listOf("video_to_image", "video_snapshot", "video_freeze_frame") -> "Capture Video Frame (.jpg)"
        tool.id == "video_thumbnail_extractor" -> "Extract Video Poster (.jpg)"
        tool.id in listOf("video_to_frames", "gif_frame_extractor", "video_contact_sheet") -> "Extract All Frames (.zip)"
        tool.id in listOf("video_splitter", "video_segment_extractor", "audio_splitter") -> "Split Media into 2 Parts (.zip)"
        tool.id in listOf("video_mute", "video_remove_audio") -> "Mute / Strip Audio (.mp4)"
        tool.id.contains("trim") || tool.id.contains("cut") -> "Trim Media Segment"
        tool.id in listOf("video_to_wav", "audio_mp3_to_wav", "audio_wav_to_flac") -> "Convert to WAV (.wav)"
        tool.id in listOf("video_to_mp3", "audio_wav_to_mp3", "audio_flac_to_mp3", "audio_m4a_to_mp3", "audio_converter") -> "Convert to MP3 (.mp3)"
        tool.id in listOf("video_to_m4a", "video_to_aac", "video_to_audio", "video_extract_audio", "audio_mp3_to_m4a") -> "Extract Audio (.m4a)"
        tool.id == "video_to_flac" -> "Extract Lossless Audio (.wav)"
        tool.id.contains("bass") -> "Boost Bass Frequencies (.wav)"
        tool.id.contains("stereo_to_mono") -> "Downmix Stereo to Mono (.wav)"
        tool.id.contains("mono_to_stereo") -> "Synthesize Mono to Stereo (.wav)"
        tool.id in listOf("audio_volume_booster", "audio_volume_normalizer") -> "Boost Volume & Enhance (.wav)"
        tool.id in listOf("audio_fade_in", "audio_fade_out") -> "Apply Audio Fade In & Out (.wav)"
        tool.id == "audio_reverse" -> "Reverse Audio Track (.wav)"
        tool.id in listOf("audio_silence_detector", "audio_silence_remover") -> "Remove Silence (.wav)"
        tool.id == "audio_album_art_extractor" -> "Extract Album Art (.jpg)"
        tool.id.startsWith("gif_") -> "Process GIF Animation"
        else -> "Process ${tool.name}"
    }

    val actionEnabled = if (isMultiFile) selectedMediaUris.size >= 2 else selectedMediaUri != null

    CommonToolScreen(
        tool = tool,
        onBack = onBack,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
        actionButtonText = actionButtonTitle,
        actionButtonEnabled = actionEnabled,
        executionState = executionState,
        onActionClick = {
            scope.launch {
                executionState = ExecutionState.Processing(progress = 0.05f, message = "Initializing media processing engine...")
                try {
                    val out: File = when {
                        // Multi-file merging
                        isMultiFile -> {
                            if (isVideo) {
                                MediaEngine.mergeVideos(context, selectedMediaUris) { p ->
                                    executionState = ExecutionState.Processing(progress = p, message = "Merging video clips into MP4...")
                                }
                            } else {
                                MediaEngine.mergeAudios(context, selectedMediaUris) { p ->
                                    executionState = ExecutionState.Processing(progress = p, message = "Joining audio tracks into WAV...")
                                }
                            }
                        }

                        // Metadata report export
                        isMetadataViewer -> {
                            val uri = selectedMediaUri ?: return@launch
                            val details = if (detailedMetadata.isNotEmpty()) detailedMetadata else MediaEngine.getDetailedMetadata(context, uri)
                            val reportFile = FileUtils.getTempFile(context, "metadata_report_", ".txt")
                            FileOutputStream(reportFile).use { fos ->
                                fos.write("=== ToolBox Media Technical Metadata Report ===\n\n".toByteArray())
                                details.forEach { (k, v) ->
                                    fos.write("$k: $v\n".toByteArray())
                                }
                            }
                            reportFile
                        }

                        // Video Rotator
                        tool.id.contains("rotat") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.rotateVideo(context, uri, videoRotationDegrees) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Rotating video ($videoRotationDegrees°)...")
                            }
                        }

                        // Video Speed Changer / Slow Motion / Fast Forward
                        isVideo && (tool.id.contains("speed") || tool.id.contains("slow") || tool.id.contains("fast")) -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.changeVideoSpeed(context, uri, videoSpeedMultiplier) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Adjusting video speed (${videoSpeedMultiplier}x)...")
                            }
                        }

                        // Video Compressor
                        isVideo && (tool.id.contains("compress") || tool.id.contains("bitrate") || tool.id.contains("resiz") || tool.id.contains("resolut")) -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.compressOrOptimizeVideo(context, uri, videoQualityPercent) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Compressing video (${(p * 100).toInt()}%)...")
                            }
                        }

                        // GIF to Video (MP4)
                        tool.id == "gif_to_video" || tool.id == "gif_to_mp4" -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.gifToMp4(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Converting GIF to MP4 Video...")
                            }
                        }

                        // GIF Frames Still Extraction
                        tool.id in listOf("gif_to_png", "gif_to_jpg") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.extractImageFrame(context, uri, 0L)
                        }

                        // Video to GIF
                        tool.id.contains("gif") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.videoToGif(
                                context = context,
                                uri = uri,
                                startMs = (startSec * 1000).toLong(),
                                durationMs = (gifDurationSec * 1000).toLong(),
                                fps = gifFps
                            ) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Generating animated GIF (${(p * 100).toInt()}%)...")
                            }
                        }

                        // Frames extraction to ZIP
                        tool.id.contains("frame") || tool.id.contains("contact_sheet") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.extractMultipleFrames(context, uri, frameCount = 8) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Extracting video frames to ZIP...")
                            }
                        }

                        // Video to Image / Snapshot / Thumbnail
                        tool.id.contains("image") || tool.id.contains("snapshot") || tool.id.contains("thumbnail") || tool.id.contains("freeze") -> {
                            val uri = selectedMediaUri ?: return@launch
                            val timeMs = if (tool.id.contains("thumbnail")) 0L else (startSec * 1000).toLong()
                            MediaEngine.extractImageFrame(context, uri, timeMs = timeMs)
                        }

                        // Splitter (Video / Audio)
                        tool.id.contains("split") || tool.id.contains("segment") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.splitMedia(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Splitting media into parts...")
                            }
                        }

                        // Mute Video
                        tool.id.contains("mute") || tool.id.contains("remove_audio") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.muteVideo(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Stripping audio stream from video...")
                            }
                        }

                        // Video & AI Metadata Cleaner (Lossless MP4 remuxing)
                        tool.id in listOf("video_metadata_cleaner", "video_clean_metadata") || (tool.id.contains("metadata") && !isMetadataViewer && isVideo) -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.stripVideoMetadata(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Sanitizing video metadata & AI markers (${(p * 100).toInt()}%)...")
                            }
                        }

                        // Audio Metadata Cleaner
                        tool.id in listOf("audio_metadata_cleaner", "audio_clean_metadata") || (tool.id.contains("metadata") && !isMetadataViewer && !isVideo) -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.decodeAudioToWav(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Sanitizing audio metadata & tags...")
                            }
                        }

                        // Trimmer / Cutter
                        tool.id.contains("trim") || tool.id.contains("cut") -> {
                            val uri = selectedMediaUri ?: return@launch
                            val startMs = (startSec * 1000).toLong()
                            val endMs = (endSec * 1000).toLong()
                            MediaEngine.trimMedia(context, uri, startMs, endMs) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Trimming media segment...")
                            }
                        }

                        // WAV conversion
                        tool.id.contains("wav") || tool.id.contains("flac") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.decodeAudioToWav(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Decoding audio stream to uncompressed WAV...")
                            }
                        }

                        // MP3 conversion / Audio compressor
                        tool.id.contains("mp3") || (!isVideo && tool.id.contains("compress")) || tool.id == "audio_converter" -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.extractOrConvertMp3(context, uri) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Converting audio to MP3...")
                            }
                        }

                        // Audio enhancements
                        !isVideo && (tool.id.contains("speed") || tool.id.contains("pitch")) -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.SPEED_CHANGE) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Adjusting audio tempo...")
                            }
                        }

                        tool.id.contains("bass") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.BASS_BOOST) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Applying bass boost...")
                            }
                        }

                        tool.id.contains("stereo_to_mono") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.STEREO_TO_MONO) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Downmixing to mono...")
                            }
                        }

                        tool.id.contains("mono_to_stereo") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.MONO_TO_STEREO) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Synthesizing stereo...")
                            }
                        }

                        tool.id.contains("volume") || tool.id.contains("booster") || tool.id.contains("normaliz") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.VOLUME_BOOST) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Boosting audio amplitude...")
                            }
                        }

                        tool.id.contains("fade") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.FADE_IN_OUT) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Applying fade in and fade out...")
                            }
                        }

                        tool.id.contains("reverse") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.REVERSE) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Reversing audio waveform...")
                            }
                        }

                        tool.id.contains("silence") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.processAudioPcm(context, uri, AudioPcmOperation.SILENCE_REMOVER) { p ->
                                executionState = ExecutionState.Processing(progress = p, message = "Removing silence intervals...")
                            }
                        }

                        tool.id.contains("album") || tool.id.contains("art") -> {
                            val uri = selectedMediaUri ?: return@launch
                            MediaEngine.extractAlbumArt(context, uri)
                        }

                        // Standard extraction / demuxing
                        else -> {
                            val uri = selectedMediaUri ?: return@launch
                            if (isVideo) {
                                MediaEngine.stripVideoMetadata(context, uri) { p ->
                                    executionState = ExecutionState.Processing(progress = p, message = "Processing video...")
                                }
                            } else {
                                MediaEngine.extractAudio(context, uri) { p ->
                                    executionState = ExecutionState.Processing(progress = p, message = "Processing audio track...")
                                }
                            }
                        }
                    }

                    outputFile = out
                    val mimeType = FileUtils.getMimeType(out)

                    // Auto-save in mobile memory upon completion with "$item saved" toast
                    if (autoSaveEnabled) {
                        val (savedUri, path) = FileUtils.saveToDeviceMemory(context, out, mimeType)
                        if (savedUri != null) {
                            savedLocationPath = path
                            Toast.makeText(context, "${out.name} saved", Toast.LENGTH_SHORT).show()
                        }
                    }

                    executionState = ExecutionState.Success(out)
                    onRecordHistory(tool.name, "Generated ${out.name} (${FileUtils.formatFileSize(out.length())})")
                } catch (e: Exception) {
                    executionState = ExecutionState.Error(
                        error = "Failed to process media",
                        details = e.localizedMessage ?: "Unknown media processing error occurred."
                    )
                }
            }
        }
    ) {
        // Picker Card: Multi-file or Single-file
        if (isMultiFile) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.Movie else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedMediaUris.isEmpty()) "Select multiple clips to merge" else "${selectedMediaUris.size} files chosen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedMediaUris.isEmpty()) "Choose 2 or more ${if (isVideo) "video" else "audio"} files to concatenate" else "Ready to join sequentially",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { multiPickerLauncher.launch(pickerMime) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedMediaUris.isEmpty()) "Select Files" else "Change Selected Files")
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedMediaUri != null) {
                        // Live video frame preview if video is chosen
                        if (isVideo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (previewBitmap != null) {
                                    Image(
                                        bitmap = previewBitmap!!.asImageBitmap(),
                                        contentDescription = "Video Frame Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }

                                // Timestamp & scrubber badge overlay
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.72f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "${formatTimeSec(activePreviewTimestampSec)} / ${formatTimeSec(maxDurationSec)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isPreviewLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = "Media file ready",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        mediaMetadata?.let { meta ->
                            Text(
                                text = "Duration: ${(meta.durationMs / 1000)}s • Format: ${meta.mimeType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (meta.width > 0) {
                                Text(
                                    text = "Resolution: ${meta.width} × ${meta.height} px",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { singlePickerLauncher.launch(pickerMime) }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change File")
                        }
                    } else {
                        Icon(
                            imageVector = if (isVideo) Icons.Default.Movie else Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select a ${if (isVideo) "video" else "audio"} file to begin", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { singlePickerLauncher.launch(pickerMime) }) {
                            Text(if (isVideo) "Choose Video File" else "Choose Audio File")
                        }
                    }
                }
            }
        }

        // Detailed Metadata breakdown for inspection tools
        if (isMetadataViewer && detailedMetadata.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Technical Specifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    detailedMetadata.forEach { (key, value) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Video to GIF controls
        if (tool.id in listOf("video_to_gif", "gif_video_to_gif") && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GIF Generation Settings", fontWeight = FontWeight.Bold)

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Time: ${formatTimeSec(startSec)}")
                        Text("Duration: ${gifDurationSec.toInt()}s")
                    }

                    Text("GIF Starting Point:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = startSec,
                        onValueChange = { startSec = it },
                        valueRange = 0f..maxDurationSec,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("GIF Duration:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = gifDurationSec,
                        onValueChange = { gifDurationSec = it },
                        valueRange = 1f..minOf(6f, maxDurationSec),
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(8 to "8 FPS", 10 to "10 FPS", 12 to "12 FPS").forEach { (fps, label) ->
                            FilterChip(
                                selected = gifFps == fps,
                                onClick = { gifFps = fps },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Trimmer & Cutter Interactive Controls with Visual Start/End Frame Tabs
        if ((tool.id.contains("trim") || tool.id.contains("cut")) && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trim Range Preview", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Duration: ${formatTimeSec(endSec - startSec)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tabs to inspect Start Frame vs End Frame live in the viewfinder
                    if (isVideo) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = trimPreviewTab == 0,
                                onClick = { trimPreviewTab = 0 },
                                label = { Text("Preview Start (${formatTimeSec(startSec)})") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = trimPreviewTab == 1,
                                onClick = { trimPreviewTab = 1 },
                                label = { Text("Preview End (${formatTimeSec(endSec)})") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start: ${formatTimeSec(startSec)}", style = MaterialTheme.typography.bodySmall)
                        Text("End: ${formatTimeSec(endSec)}", style = MaterialTheme.typography.bodySmall)
                    }

                    RangeSlider(
                        value = startSec..endSec,
                        onValueChange = { range ->
                            startSec = range.start
                            endSec = range.endInclusive
                        },
                        valueRange = 0f..maxDurationSec,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Fine adjustment buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (trimPreviewTab == 0) {
                                    startSec = (startSec - 1f).coerceAtLeast(0f)
                                } else {
                                    endSec = (endSec - 1f).coerceAtLeast(startSec + 0.5f)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("-1 sec", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(
                            onClick = {
                                if (trimPreviewTab == 0) {
                                    startSec = (startSec + 1f).coerceAtMost(endSec - 0.5f)
                                } else {
                                    endSec = (endSec + 1f).coerceAtMost(maxDurationSec)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("+1 sec", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Frame capture position slider for image/frame tools with Live Frame Scrubbing & Step Buttons
        if (tool.id in listOf("video_to_image", "video_snapshot", "video_freeze_frame", "video_thumbnail_extractor") && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Frame Capture Timestamp", fontWeight = FontWeight.Bold)
                        Text(
                            text = formatTimeSec(startSec),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text("Scrub position to preview exact photo frame:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Slider(
                        value = startSec,
                        onValueChange = { startSec = it },
                        valueRange = 0f..maxDurationSec,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Fine scrubber jump buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { startSec = (startSec - 5f).coerceAtLeast(0f) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text("-5s", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { startSec = (startSec - 1f).coerceAtLeast(0f) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text("-1s", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { startSec = 0f },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { startSec = (startSec + 1f).coerceAtMost(maxDurationSec) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text("+1s", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { startSec = (startSec + 5f).coerceAtMost(maxDurationSec) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text("+5s", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Video Rotator Card
        if (tool.id.contains("rotat") && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Rotation Angle", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(90, 180, 270).forEach { deg ->
                            FilterChip(
                                selected = videoRotationDegrees == deg,
                                onClick = { videoRotationDegrees = deg },
                                label = { Text("$deg° Clockwise") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Playback Speed Card (for both Video and Audio tools)
        if ((tool.id.contains("speed") || tool.id.contains("slow") || tool.id.contains("fast") || tool.id.contains("tempo")) && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isVideo) "Video Playback Speed" else "Audio Playback Speed", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${videoSpeedMultiplier}x",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.5f, 0.75f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                            FilterChip(
                                selected = videoSpeedMultiplier == spd,
                                onClick = { videoSpeedMultiplier = spd },
                                label = { Text("${spd}x") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Audio Enhancement Card (Volume Boost, Normalizer, Bass Boost, Fade)
        if (!isVideo && (tool.id.contains("volume") || tool.id.contains("boost") || tool.id.contains("bass") || tool.id.contains("fade") || tool.id.contains("mono") || tool.id.contains("stereo") || tool.id.contains("reverse")) && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audio Processing Parameters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    when {
                        tool.id.contains("volume") || tool.id.contains("boost") -> {
                            Text("Dynamic range enhancement will amplify output amplitude up to 200% without digital clipping.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("+3 dB (Subtle)", "+6 dB (2x Gain)", "+10 dB (Max Boost)").forEachIndexed { idx, label ->
                                    FilterChip(
                                        selected = idx == 1,
                                        onClick = {},
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        tool.id.contains("bass") -> {
                            Text("Low-frequency equalization enhancement targeting sub-bass and kick frequencies (40Hz - 250Hz).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Warm Punch", "Heavy Sub", "Club Bass").forEachIndexed { idx, label ->
                                    FilterChip(
                                        selected = idx == 1,
                                        onClick = {},
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        tool.id.contains("fade") -> {
                            Text("Applies smooth progressive gain curve to intro and outro transitions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("2s Fade", "3s Standard", "5s Ambient").forEachIndexed { idx, label ->
                                    FilterChip(
                                        selected = idx == 1,
                                        onClick = {},
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        tool.id.contains("mono") || tool.id.contains("stereo") -> {
                            Text("Channel matrix configuration converts dual channel to balanced center or duplicates mono channels.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        tool.id.contains("reverse") -> {
                            Text("Reverses the audio buffer timeline backwards to generate backwards masking effects.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Dedicated Audio Privacy & ID3 Metadata Cleaner Card
        if (!isVideo && (tool.id in listOf("audio_metadata_cleaner", "audio_clean_metadata") || (tool.id.contains("metadata") && !isMetadataViewer)) && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audio Privacy & ID3 Metadata Clean", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Lossless PCM audio sanitizer. Completely eliminates artist, album, comments, encoder watermark signatures (Suno, Udio, ElevenLabs AI tags), and ID3v1/v2 tags.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    listOf(
                        "Strip ID3v1 & ID3v2 Tags" to "Clears artist, album, track number, and year info",
                        "Wipe AI Synthesis Markers" to "Eliminates Suno, Udio, ElevenLabs metadata watermarks",
                        "Remove Comment & Lyrics Payload" to "Deletes embedded lyrics, copyright notes, and URLs",
                        "Preserve 100% Studio Sound" to "Outputs pristine uncompressed WAV stream"
                    ).forEach { (title, subtitle) ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Video Compressor Card
        if (isVideo && (tool.id.contains("compress") || tool.id.contains("bitrate")) && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Compression Level", fontWeight = FontWeight.Bold)
                        Text(
                            text = "$videoQualityPercent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = videoQualityPercent.toFloat(),
                        onValueChange = { videoQualityPercent = it.toInt() },
                        valueRange = 40f..95f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Smaller Size (40%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Higher Quality (95%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Dedicated Video Privacy & AI Metadata Cleaner Card
        if ((tool.id in listOf("video_metadata_cleaner", "video_clean_metadata") || (tool.id.contains("metadata") && !isMetadataViewer && isVideo)) && selectedMediaUri != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Video Privacy & AI Content Clean", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Specially optimized lossless remuxer. Erases GPS coordinates, camera/phone serial, user comments, and AI-generated metadata markers (Sora, Veo, Runway, Pika, Kling) with zero quality loss.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    listOf(
                        "Strip GPS Location & Geotags" to "Removes latitude, longitude, and altitude tags",
                        "Strip Camera & Device Serial" to "Clears device manufacturer, model, and hardware ID",
                        "Wipe AI Generation Headers & Prompts" to "Clears prompt history, C2PA, and synthesis parameters",
                        "100% Lossless Stream Copy" to "Video & audio streams are remuxed without recompression"
                    ).forEach { (title, subtitle) ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Output File Card with mobile storage auto-save indicator and "$item saved" toast
        outputFile?.let { file ->
            val outMime = FileUtils.getMimeType(file)
            ResultFileCard(
                file = file,
                formattedSize = FileUtils.formatFileSize(file.length()),
                savedLocation = savedLocationPath,
                onOpen = { FileUtils.openFile(context, file, outMime) },
                onShare = { FileUtils.shareFile(context, file, outMime) },
                onSaveToDevice = {
                    val (savedUri, path) = FileUtils.saveToDeviceMemory(context, file, outMime)
                    if (savedUri != null) {
                        savedLocationPath = path
                        Toast.makeText(context, "${file.name} saved", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

private fun formatTimeSec(seconds: Float): String {
    val totalSec = seconds.toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    val frac = ((seconds - totalSec) * 10).toInt()
    return String.format("%02d:%02d.%d", m, s, frac)
}
